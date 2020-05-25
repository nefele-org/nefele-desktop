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

import com.dropbox.core.*;
import com.dropbox.core.json.JsonReader;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.TransferInfoCallback;
import org.nefele.fs.MergeChunk;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.dialog.InputDialog;
import org.nefele.ui.dialog.InputDialogResult;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DropboxDriveProvider extends Drive {

    public final static String SERVICE_ID = "dropbox-drive-service";
    public final static String SERVICE_DEFAULT_DESCRIPTION = "Dropbox";

    private final Path servicePath;
    private final Path accessToken;


    public DropboxDriveProvider(String id, String service, String description, long quota, long blocks) {
        super(id, service, description, quota, blocks);


        servicePath = Application.getInstance().getDataPath().resolve(
                Paths.get("drive", SERVICE_ID, getId()));

        accessToken = servicePath.resolve("accessToken");

    }

    @Override
    public void writeChunk(MergeChunk chunk, InputStream inputStream, TransferInfoCallback callback) throws IOException {

    }

    @Override
    public ByteBuffer readChunk(MergeChunk chunk, TransferInfoCallback callback) throws IOException {
        return null;
    }

    @Override
    public void removeChunk(MergeChunk chunk) throws IOException {

    }

    @Override
    public long getMaxQuota() {
        return 0;
    }

    @Override
    public Drive initialize() {

        Application.log(getClass(), "Intializing %s %s", SERVICE_ID, getId());
        setStatus(STATUS_CONNECTING);


        if(Files.notExists(accessToken)) {

            try {

                if(!Desktop.isDesktopSupported())
                    throw new UnsupportedOperationException("Desktop is not supported");



                DbxAppInfo appInfo = DbxAppInfo.Reader.readFromFile(
                        Resources.getURL(this, "/services/dropbox/credentials.json").toExternalForm());


                DbxRequestConfig requestConfig = new DbxRequestConfig("nefele-desktop");
                DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
                DbxWebAuth.Request request = DbxWebAuth.newRequestBuilder()
                        .withNoRedirect()
                        .build();



                String URL = webAuth.authorize(request);
                String accessCode = null;

                InputDialogResult result;

                do {

                    Application.log(getClass(), "Opening Login Page: %s", URL);

                    Desktop.getDesktop()
                            .browse(URI.create(URL));


                    result = Dialogs.showInputBox(
                            "Inserisci il codice di autorizzazione che hai ricevuto da Dropbox",
                            BaseDialog.DIALOG_RETRY, BaseDialog.DIALOG_CONTINUE);


                } while (result.getButton() == BaseDialog.DIALOG_RETRY);




                if (result.getButton() != BaseDialog.DIALOG_CONTINUE)
                    throw new IllegalStateException("Connection attempt with dropbox canceled by user");

                accessCode = result.getText();




                DbxAuthFinish authFinish = webAuth.finishFromCode(accessCode);

                Application.log(getClass(), "Authorization complete for %s %s!", SERVICE_ID, getId());
                Application.log(getClass(), " - User ID: %s", authFinish.getUserId());
                Application.log(getClass(), " - Account ID: %s", authFinish.getAccountId());
                Application.log(getClass(), " - Access Token: %s", authFinish.getAccessToken());


                DbxAuthInfo.Writer.writeToFile(
                        new DbxAuthInfo(authFinish.getAccessToken(), appInfo.getHost()), accessToken.toFile());



            } catch (JsonReader.FileLoadException | UnsupportedOperationException | IllegalStateException | IOException | DbxException e) {

                Application.log(getClass(), "ERROR! Exception %s for %s %s: %s", e.getClass().getName(), SERVICE_ID, getId(), e.getMessage());

                setStatus(STATUS_ERROR);
                setError(ERROR_LOGIN_FAIL);

                return this;

            }


        }







        return this;
    }

    @Override
    public Drive exit() {
        return this;
    }

}
