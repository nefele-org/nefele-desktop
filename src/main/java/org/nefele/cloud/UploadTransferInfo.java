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

import javafx.application.Platform;
import org.nefele.Application;
import org.nefele.fs.MergeChunk;
import org.nefele.fs.MergePath;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;


public class UploadTransferInfo extends TransferInfo {


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
                writer.write(reader.readNBytes((int) MergeChunk.getDefaultSize()));

            writer.close();
            reader.close();


        } catch (Exception e) {

            Application.log(getClass(), e, "Something wrong, preparing canceled for %s", localFile.getAbsolutePath());

            setStatus(TRANSFER_STATUS_ERROR);
            return getStatus();

        }



        setStatus(TRANSFER_STATUS_RUNNING);
        Application.log(getClass(), "Started UploadTransferInfo() for %s (size: %d)", getPath().toString(), getSize());



        for (MergeChunk chunk : getPath().getInode().getChunks()) {

            if (getStatus() == TRANSFER_STATUS_PAUSED) {

                while (getStatus() == TRANSFER_STATUS_PAUSED)
                    Thread.yield();

                if(getStatus() == TRANSFER_STATUS_RESUME)
                    setStatus(TRANSFER_STATUS_RUNNING);

            }

            if (getStatus() == TRANSFER_STATUS_CANCELED)
                break;



            if (!getFileSystem().getStorage().isCached(chunk)) {

                Application.log(getClass(), "WARNING! chunk %s is not cached, something wrong!", chunk.getId());

                setStatus(TRANSFER_STATUS_ERROR);
                return getStatus();

            }


            while(true) {

                try(InputStream inputStream = getFileSystem().getStorage().read(chunk, true)) {

                    chunk.getDriveProvider().writeChunk(chunk, inputStream, new TransferInfoCallback() {

                        @Override
                        public boolean isCancelled() {
                            return getStatus() == TRANSFER_STATUS_CANCELED;
                        }

                        @Override
                        public void updateProgress(int progress) {
                            Platform.runLater(() -> setProgress(getProgress() + progress));
                        }

                    });

                    break;

                } catch (TransferInfoTryAgainException e) {
                    Application.log(getClass(), "WARNING! %s for %s: %s", e.getClass().getName(), chunk.getId(), e.getMessage());

                } catch (Exception e) {

                    Application.log(getClass(), e, "Something wrong, transfer canceled for %s", chunk.getId());

                    setStatus(TRANSFER_STATUS_ERROR);
                    return getStatus();

                }

            }

        }


        try {

            if (getStatus() == TRANSFER_STATUS_CANCELED)
                Files.delete(getPath());  /* FIXME: delete() destroy everything including your machine */

        } catch (Exception e) {
            Application.log(getClass(), e, "(ignored) could not delete %s", getPath().toString());
        }



        Application.log(getClass(), "Completed UploadTransferInfo() for %s (size: %d)", getPath().toString(), getSize());

        setStatus(TRANSFER_STATUS_COMPLETED);
        return getStatus();

    }


}
