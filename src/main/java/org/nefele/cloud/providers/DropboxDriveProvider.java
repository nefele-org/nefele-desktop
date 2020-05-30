/*
 * The MIT License
 *
 * Copyright (c) 2020 Nefele <https://github.com/nefele-org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.nefele.cloud.providers;

import com.dropbox.core.*;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.*;
import org.nefele.fs.MergeChunk;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.dialog.InputDialog;
import org.nefele.ui.dialog.InputDialogResult;
import org.nefele.utils.PlatformUtils;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class DropboxDriveProvider extends DriveProvider {

    public final static String SERVICE_ID = "dropbox-drive-service";
    public final static String SERVICE_DEFAULT_DESCRIPTION = "Dropbox";

    private static final int TRANSFER_BLOCK_SIZE = 65536;


    private final Path servicePath;
    private final Path accessToken;

    private DbxClientV2 driveService;
    private FullAccount userAccount;
    private SpaceUsage spaceUsage;



    public DropboxDriveProvider(String id, String service, String description, long quota, long blocks) {
        super(id, service, description, quota, blocks);


        servicePath = Application.getInstance().getDataPath().resolve(
                Paths.get("drive", SERVICE_ID, getId()));

        accessToken = servicePath.resolve("accessToken");

    }

    @Override
    public void writeChunk(MergeChunk chunk, InputStream inputStream, TransferInfoCallback callback) throws TransferInfoException {

        try {

            UploadUploader dbxUploader = driveService.files()
                    .uploadBuilder("/" + chunk.getId())
                    .withClientModified(Date.from(
                            Instant.ofEpochMilli(chunk.getRevision())))
                    .start();


            dbxUploader.uploadAndFinish(inputStream, new IOUtil.ProgressListener() {

                private long currentNumBytesWritten = 0;

                @Override
                public void onProgress(long bytesWritten) {

                    if (callback.isCancelled())
                        throw new RuntimeException("stopped by user");


                    final long n = currentNumBytesWritten;
                    currentNumBytesWritten = bytesWritten;

                    callback.updateProgress((int) (bytesWritten - n));

                }

            });


        } catch (RateLimitException | NetworkIOException e) {
            throw new TransferInfoTryAgainException("Too many request, waiting a bit and try again...", 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }

    @Override
    public ByteBuffer readChunk(MergeChunk chunk, TransferInfoCallback callback) throws TransferInfoException {

        try {

            DbxDownloader<FileMetadata> dbxDownloader = driveService.files()
                    .download("/" + chunk.getId());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    (int) dbxDownloader.getResult().getSize());


            dbxDownloader.download(byteArrayOutputStream, new IOUtil.ProgressListener() {

                private long currentNumBytesWritten = 0;

                @Override
                public void onProgress(long bytesWritten) {

                    if (callback.isCancelled())
                        throw new RuntimeException("stopped by user");


                    final long n = currentNumBytesWritten;
                    currentNumBytesWritten = bytesWritten;

                    callback.updateProgress((int) (bytesWritten - n));

                }
            });


            return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());


        } catch (RateLimitException | NetworkIOException e) {
            throw new TransferInfoTryAgainException("Too many request, waiting a bit and try again...", 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }

    @Override
    public void removeChunk(MergeChunk chunk) throws TransferInfoException {

        try {

            driveService
                    .files()
                    .deleteV2("/" + chunk.getId());

        } catch (RateLimitException | NetworkIOException e) {
            throw new TransferInfoTryAgainException("Too many request, waiting a bit and try again...", 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }


    @Override
    public int isChunkUpdated(MergeChunk chunk) throws TransferInfoException {

        try {

            long updatedRev = driveService
                    .files()
                    .download("/" + chunk.getId())
                    .getResult()
                    .getClientModified()
                        .toInstant()
                        .toEpochMilli();

            long currentRev = chunk.getRevision();


            return (int) (currentRev - updatedRev);

        } catch (RateLimitException | NetworkIOException e) {
            throw new TransferInfoTryAgainException("Too many request, waiting a bit and try again...", 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }

    @Override
    public long getMaxQuota() {

        if(spaceUsage != null)
            return (spaceUsage.getAllocation().getIndividualValue().getAllocated() - spaceUsage.getUsed());

        return 0;

    }


    @Override
    public DriveProvider initialize() {

        Application.log(getClass(), "Intializing %s %s", SERVICE_ID, getId());
        setStatus(STATUS_CONNECTING);


        try {


            if(Files.notExists(servicePath.getParent()))
                Files.createDirectory(servicePath.getParent());

            if(Files.notExists(servicePath))
                Files.createDirectory(servicePath);




            if(Files.notExists(accessToken)) {


                if(!Desktop.isDesktopSupported())
                    throw new UnsupportedOperationException("Desktop is not supported");



                DbxAppInfo appInfo = DbxAppInfo.Reader.readFromFile(
                        Resources.getURL(this, "/services/dropbox/credentials.json").getPath());


                DbxRequestConfig requestConfig = new DbxRequestConfig("nefele-desktop");
                DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
                DbxWebAuth.Request request = DbxWebAuth.newRequestBuilder()
                        .withNoRedirect()
                        .build();



                String URL = webAuth.authorize(request);
                String accessCode = null;

                AtomicReference<InputDialogResult> result = new AtomicReference<>();

                do {

                    Application.log(getClass(), "Opening Login Page: %s", URL);

                    Desktop.getDesktop()
                            .browse(URI.create(URL));


                    PlatformUtils.runLaterAndWait(() -> {
                        result.set(Dialogs.showInputBox(
                                "DROPBOX_PROVIDER_ADVISE",
                                InputDialog.DIALOG_RETRY, InputDialog.DIALOG_CONTINUE));
                    });


                } while (result.get().getButton() == InputDialog.DIALOG_RETRY);




                if (result.get().getButton() != InputDialog.DIALOG_CONTINUE)
                    throw new IllegalStateException("Connection attempted with dropbox canceled by user");

                accessCode = result.get().getText();




                DbxAuthFinish authFinish = webAuth.finishFromCode(accessCode);

                Application.log(getClass(), "Authorization complete for %s %s!", SERVICE_ID, getId());
                Application.log(getClass(), " - User ID: %s", authFinish.getUserId());
                Application.log(getClass(), " - Account ID: %s", authFinish.getAccountId());
                Application.log(getClass(), " - Access Token: %s", authFinish.getAccessToken());


                if(Files.notExists(accessToken))
                    Files.createFile(accessToken);

                DbxAuthInfo.Writer.writeToFile(
                        new DbxAuthInfo(authFinish.getAccessToken(), appInfo.getHost()), accessToken.toFile());


            }





            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("nefele-desktop")
                    .withUserLocaleFrom(Locale.ENGLISH)
                    .withAutoRetryEnabled()
                    .build();

            driveService = new DbxClientV2(requestConfig, DbxAuthInfo.Reader.readFromFile(accessToken.toFile()).getAccessToken());



            userAccount = driveService.users().getCurrentAccount();
            spaceUsage = driveService.users().getSpaceUsage();

            Application.log(getClass(), "Login complete for %s %s", SERVICE_ID, getId());
            Application.log(getClass(), " - Account: %s <%s>", userAccount.getName().getDisplayName(), userAccount.getEmail());
            Application.log(getClass(), " - Space: %d bytes of %d bytes", spaceUsage.getUsed(), spaceUsage.getAllocation().getIndividualValue().getAllocated());



            Application.getInstance().runWorker(new Thread(() -> {

                try {

                    if(getStatus() == STATUS_READY)
                        driveService.refreshAccessToken();

                } catch (DbxException | RuntimeException e) {
                    Application.log(getClass(), "WARNING! refreshAccessToken() failed for %s %s: %s", SERVICE_ID, getId(), e.getMessage());
                }

            }, String.format("Dropbox-#%s::refreshAccessToken()", getId())), 30, 30, TimeUnit.MINUTES);



            setStatus(STATUS_READY);



        } catch (JsonReader.FileLoadException | RuntimeException | IOException | DbxException e) {

            Application.log(getClass(), e,"%s %s", SERVICE_ID, getId());

            setStatus(STATUS_ERROR);
            setError(ERROR_LOGIN_FAIL);

        }


        return this;
    }

    @Override
    public DriveProvider exit() {
        return this;
    }

}