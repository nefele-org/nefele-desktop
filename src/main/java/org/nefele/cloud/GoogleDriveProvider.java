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

package org.nefele.cloud;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.TransferInfoCallback;
import org.nefele.fs.MergeChunk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class GoogleDriveProvider extends Drive {

    public final static String SERVICE_ID = "google-drive-service";
    public final static String SERVICE_DEFAULT_DESCRIPTION = "Google Drive";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_APPDATA);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final int DOWNLOAD_BLOCK_SIZE = 65536;


    private final Path servicePath;
    private com.google.api.services.drive.Drive driveService;
    private long storageQuotaUsed;
    private long storageQuotaLimit;





    public GoogleDriveProvider(String id, String service, String description, long quota, long blocks) {
        super(id, service, description, quota, blocks);

        servicePath = Application.getInstance().getDataPath()
                .resolve(Paths.get("drive", SERVICE_ID, id));

    }

    @Override
    public void writeChunk(MergeChunk chunk, InputStream inputStream, TransferInfoCallback callback) throws IOException {

        String id;
        if((id = findChunk(chunk)) == null)
            id = createChunk(chunk);

        requireNonNull(id);


        final com.google.api.services.drive.Drive.Files.Update update = driveService.files()
                .update(id, null, new AbstractInputStreamContent("") {

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return inputStream;
                    }

                    @Override
                    public long getLength() throws IOException {
                        return inputStream.available();
                    }

                    @Override
                    public boolean retrySupported() {
                        return false;
                    }

                });

        update.getMediaHttpUploader().setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
        update.getMediaHttpUploader().setDirectUploadEnabled(false);

        update.getMediaHttpUploader().setProgressListener(new MediaHttpUploaderProgressListener() {

            private long currentNumBytesUploaded = 0;

            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {

                if(callback.isCanceled())
                    throw new IOException("stopped by user");


                final long d = uploader.getNumBytesUploaded();
                final long n = currentNumBytesUploaded;

                currentNumBytesUploaded = d;
                callback.updateProgress((int) (d - n));

            }
        });

        update.execute();

    }

    @Override
    public ByteBuffer readChunk(MergeChunk chunk, TransferInfoCallback callback) throws IOException {

        String id = findChunk(chunk);

        if(id == null)
            throw new FileNotFoundException(chunk.getId());



        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() {
            @Override
            public synchronized void write(byte[] b, int off, int len) {
                super.write(b, off, len);
                callback.updateProgress(len);
            }
        }) {

            driveService.files()
                    .get(id)
                    .executeMediaAndDownloadTo(byteArrayOutputStream);

            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            return byteBuffer.rewind();

        }

    }



    @Override
    public void removeChunk(MergeChunk chunk) throws IOException {

        String id = findChunk(chunk);

        if(id == null)
            throw new FileNotFoundException(chunk.getId());

        else {
            driveService.files()
                    .delete(id)
                    .execute();
        }

    }


    @Override
    public long getMaxQuota() {
        return (storageQuotaLimit - storageQuotaUsed) / MergeChunk.getSize() - 1;
    }

    @Override
    public Drive initialize() {

        Application.log(getClass(), "Intializing %s %s", SERVICE_ID, getId());
        setStatus(STATUS_CONNECTING);

        try {

            if(Files.notExists(servicePath.getParent()))
                Files.createDirectory(servicePath.getParent());

            if(Files.notExists(servicePath))
                Files.createDirectory(servicePath);



            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            driveService = new com.google.api.services.drive.Drive.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                    .setApplicationName("Nefele")
                    .build();


            About about = driveService.about()
                    .get()
                    .setFields("*")
                    .execute();


            storageQuotaLimit = about.getStorageQuota().getLimit();
            storageQuotaUsed = about.getStorageQuota().getUsage();

            Application.log(getClass(), "Login complete for %s %s!", SERVICE_ID, getId());
            Application.log(getClass(), " - %s: %s <%s>", "User", about.getUser().getDisplayName(), about.getUser().getEmailAddress());
            Application.log(getClass(), " - %s: %s", "AppInstalled", about.getAppInstalled());
            Application.log(getClass(), " - %s: %d", "getMaxUploadSize", about.getMaxUploadSize());
            Application.log(getClass(), " - %s: %d", "StorageQuota::Usage", about.getStorageQuota().getUsage());
            Application.log(getClass(), " - %s: %d", "StorageQuota::Limit", about.getStorageQuota().getLimit());



            setStatus(STATUS_READY);

        } catch (GeneralSecurityException | IOException e) {

            Application.log(getClass(), "Unhandled exception %s for %s %s: %s", e.getClass().getName(), SERVICE_ID, getId(), e.getMessage());

            setStatus(STATUS_ERROR);
            setError(ERROR_UNREACHABLE);

        }

        return this;
    }

    @Override
    public Drive exit() {
        return this;
    }



    private Credential getCredentials(NetHttpTransport httpTransport) throws IOException {

        final InputStream inputStream = Resources.getStream(this, "/services/google-drive/credentials.json");
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));

        final GoogleAuthorizationCodeFlow codeFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(servicePath.toFile()))
                .setAccessType("offline")
                .build();


        final LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                //.setLandingPages() // TODO...
                .build();


        return new AuthorizationCodeInstalledApp(codeFlow, receiver)
                .authorize("user");

    }


    private String findChunk(MergeChunk chunk) {


        try {

            return driveService.files()
                    .list()
                    .setQ("name = '" + chunk.getId() + "'")
                    .setPageSize(1)
                    .setSpaces("appDataFolder")
                    .execute()
                    .getFiles().get(0)
                    .getId();

        } catch (IOException e) {
            Application.log(getClass(), "WARNING! Exception %s in findChunk(): %s", e.getClass().getName(), e.getMessage());
        } catch (IndexOutOfBoundsException ignored) { }

        return null;

    }

    private String createChunk(MergeChunk chunk) throws DriveFullException {

        try {

            return driveService.files().create(
                    new File()
                            .setName(chunk.getId())
                            .setParents(Collections.singletonList("appDataFolder"))
                            .setDescription("Nefele Chunk")

            ).execute()
                    .getId();



        } catch (IOException e) {
            Application.log(getClass(), "WARNING! IOException %s in createChunk(): %s", e.getClass().getName(), e.getMessage());
            throw new DriveFullException();
        }

    }

}