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

import org.nefele.Application;
import org.nefele.fs.MergeChunk;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergePath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DownloadTransferInfo extends TransferInfo {

    private static final int DOWNLOAD_BLOCK_SIZE = 65536;


    public DownloadTransferInfo(MergePath path) {
        super(path, TRANSFER_TYPE_DOWNLOAD);
    }


    @Override
    public Integer execute() {

        setStatus(TransferInfo.TRANSFER_STATUS_RUNNING);


        // TODO: deep copy array
        for(MergeChunk chunk : getPath().getInode().getData().getChunks()) {

            if(getStatus() == TRANSFER_STATUS_CANCELED)
                break;

            if(getStatus() == TRANSFER_STATUS_PAUSED) {

                while (getStatus() != TRANSFER_STATUS_RESUME)
                    Thread.yield();

                setStatus(TRANSFER_STATUS_RUNNING);

            }


            if(!getFileSystem().getCache().isCached(chunk)) {

                try {

                    InputStream inputStream = chunk.getDrive().readChunk(chunk);
                    assert inputStream != null;

                    ByteBuffer byteBuffer = ByteBuffer.allocate(inputStream.available());


                    while (inputStream.available() > 0) {

                        byte[] bytes = new byte[Math.min(DOWNLOAD_BLOCK_SIZE, inputStream.available())];

                        if (inputStream.read(bytes) > 0)
                            byteBuffer.put(bytes);

                        setProgress(getProgress() + bytes.length);

                    }

                    byteBuffer.rewind();
                    getFileSystem().getCache().write(chunk, byteBuffer, 0);

                } catch (IOException e) {

                    // TODO: handle error


                    Application.log(getClass(), "WARNING! %s, something wrong, transfer canceled!", e.getClass().getName(), e.getMessage());

                    setStatus(TRANSFER_STATUS_ERROR);
                    return getStatus();

                }

            }

        }


        setStatus(TRANSFER_STATUS_COMPLETED);

        return getStatus();

    }


}
