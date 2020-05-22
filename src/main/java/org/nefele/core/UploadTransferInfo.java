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

package org.nefele.core;

import javafx.application.Platform;
import org.nefele.Application;
import org.nefele.cloud.DriveFullException;
import org.nefele.fs.MergeChunk;
import org.nefele.fs.MergePath;
import org.nefele.ui.dialog.Dialogs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;


public class UploadTransferInfo extends TransferInfo {

    private static final int UPLOAD_BLOCK_SIZE = 65536;
    private static final int PREPARE_BLOCK_SIZE = 4194304;

    private final File localFile;



    public UploadTransferInfo(MergePath path, File localFile) {
        super(path, TRANSFER_TYPE_UPLOAD);
        this.localFile = localFile;
    }


    @Override
    public Integer execute() {

        setSize(localFile.length());
        setStatus(TRANSFER_STATUS_READY);


        Application.log(getClass(), "Prepare UploadTransferInfo() for %s (size: %d)", getPath().toString(), getSize());

        try {

            Files.createFile(getPath());

            OutputStream writer = Files.newOutputStream(getPath());
            InputStream reader = Files.newInputStream(localFile.toPath());

            while (reader.available() > 0)
                writer.write(reader.readNBytes(PREPARE_BLOCK_SIZE));

            writer.close();
            reader.close();


        } catch (DriveFullException | IOException io) {

            setStatus(TRANSFER_STATUS_ERROR);
            Application.log(getClass(), "WARNING! %s when uploading file %s: %s", io.getClass().getName(), localFile.toString(), io.getMessage());


            if(io instanceof DriveFullException)
                Platform.runLater(() -> Dialogs.showErrorBox("DRIVE_FULL_EXCEPTION"));
            else
                Platform.runLater(() -> Dialogs.showErrorBox("FILE_UPLOAD_ERROR"));

            return getStatus();

        }



        setStatus(TRANSFER_STATUS_RUNNING);
        Application.log(getClass(), "Started UploadTransferInfo() for %s (size: %d)", getPath().toString(), getSize());


        // TODO: deep copy array
        for(MergeChunk chunk : getPath().getInode().getData().getChunks()) {

            if(getStatus() == TRANSFER_STATUS_CANCELED)
                break;

            if(getStatus() == TRANSFER_STATUS_PAUSED) {

                while (getStatus() != TRANSFER_STATUS_RESUME)
                    Thread.yield();

                setStatus(TRANSFER_STATUS_RUNNING);

            }


            if(!getFileSystem().getStorage().isCached(chunk)) {

                Application.log(getClass(), "WARNING! chunk %s is not cached, something wrong!", chunk.getId());

                setStatus(TRANSFER_STATUS_ERROR);
                return getStatus();

            }

            try {

                OutputStream outputStream = chunk.getDrive().writeChunk(chunk);
                InputStream inputStream = getFileSystem().getStorage().read(chunk);


                while(inputStream.available() > 0) {

                    if(getStatus() == TRANSFER_STATUS_CANCELED)
                        break;


                    byte[] bytes = new byte[Math.min(UPLOAD_BLOCK_SIZE, inputStream.available())];

                    if(inputStream.read(bytes) > 0)
                        outputStream.write(bytes);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) { }


                    Platform.runLater(() -> setProgress(getProgress() + bytes.length));

                }

                outputStream.close();
                inputStream.close();


            } catch (IOException e) {

                // TODO: handle error


                Application.log(getClass(), "WARNING! %s, something wrong, transfer canceled!", e.getClass().getName(), e.getMessage());

                setStatus(TRANSFER_STATUS_ERROR);
                return getStatus();

            }

        }


        try {

            if (getStatus() == TRANSFER_STATUS_CANCELED)
                Files.delete(getPath());  /* FIXME: delete() destroy everything including your machine */

        } catch (IOException e) {
            Application.log(getClass(), "WARNING! could not delete %s: %s %s", getPath().toString(), e.getClass().getName(), e.getMessage());
        }


        setStatus(TRANSFER_STATUS_COMPLETED);
        return getStatus();

    }


}
