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
import org.mortbay.util.ajax.JSON;
import org.nefele.Application;
import org.nefele.fs.MergeChunk;
import org.nefele.fs.MergePath;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Comparator;

public class DownloadTransferInfo extends TransferInfo {

    private static final int DOWNLOAD_BLOCK_SIZE = 65536;
    private static final int PREPARE_BLOCK_SIZE = 4194304;

    private final File localFile;


    public DownloadTransferInfo(MergePath path, File localFile) {
        super(path, TRANSFER_TYPE_DOWNLOAD);
        this.localFile = localFile;
    }


    @Override
    public Integer execute() {

        setStatus(TransferInfo.TRANSFER_STATUS_READY);
        setStatus(TransferInfo.TRANSFER_STATUS_RUNNING);

        Application.log(getClass(), "Started DownloadTransferInfo() for %s (size: %d)", getPath().toString(), getSize());



        for(MergeChunk chunk : getPath().getInode().getData().getChunks()) {

            if (getStatus() == TRANSFER_STATUS_PAUSED) {

                while (getStatus() == TRANSFER_STATUS_PAUSED)
                    Thread.yield();

                if(getStatus() == TRANSFER_STATUS_RESUME)
                    setStatus(TRANSFER_STATUS_RUNNING);

            }

            if (getStatus() == TRANSFER_STATUS_CANCELED)
                break;


            if(!getFileSystem().getStorage().isCached(chunk)) {


                while(true) {

                    try {

                        ByteBuffer byteBuffer = chunk.getDrive().readChunk(chunk, new TransferInfoCallback() {

                            @Override
                            public boolean isCanceled() {
                                return getStatus() == TRANSFER_STATUS_CANCELED;
                            }

                            @Override
                            public void updateProgress(int progress) {
                                Platform.runLater(() -> setProgress(getProgress() + progress));
                            }

                        });


                        getFileSystem().getStorage()
                                .write(chunk, byteBuffer, 0, false);

                        break;

                    } catch (TransferInfoTryAgainException e) {
                        Application.log(getClass(), "WARNING! %s for %s: %s", e.getClass().getName(), chunk.getId(), e.getMessage());

                    } catch (Exception e) {

                        Application.log(getClass(), "WARNING! %s, something wrong, transfer canceled for %s: %s", e.getClass().getName(), chunk.getId(), e.getMessage());

                        setStatus(TRANSFER_STATUS_ERROR);
                        return getStatus();

                    }

                }

            }

        }


        if (getStatus() == TRANSFER_STATUS_CANCELED)
            return getStatus();




        Application.log(getClass(), "Writing DownloadTransferInfo() for %s (size: %d) in %s", getPath().toString(), getSize(), localFile.getAbsolutePath());



        try {

            if (!localFile.exists())
                localFile.createNewFile();


            OutputStream outputStream = Files.newOutputStream(localFile.toPath());
            InputStream inputStream = Files.newInputStream(getPath());

            while (inputStream.available() > 0) {

                byte[] bytes = new byte[Math.min(PREPARE_BLOCK_SIZE, inputStream.available())];

                if(inputStream.read(bytes) > 0)
                    outputStream.write(bytes);

            }

            outputStream.close();
            inputStream.close();


        } catch (Exception e) {

            Application.log(getClass(), "WARNING! %s, something wrong, writing canceled for %s: %s", e.getClass().getName(), localFile.getAbsolutePath(), e.getMessage());

            setStatus(TRANSFER_STATUS_ERROR);
            return getStatus();

        }


        setStatus(TRANSFER_STATUS_COMPLETED);
        return getStatus();

    }


}
