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

import java.io.IOException;
import java.io.InputStream;

public class DownloadTransferInfo extends TransferInfo {

    InputStream inputStream;

    public DownloadTransferInfo(String name, int size, InputStream inputStream) {
        super(name, size, TRANSFER_TYPE_DOWNLOAD, true);

        this.inputStream = inputStream;

    }


    @Override
    public synchronized Integer execute() {

        while(getStatus() == TRANSFER_STATUS_READY)
            Thread.yield();

        do {

            try {

                byte[] buffer = inputStream.readNBytes(65536);

                if(buffer.length == 0)
                    setStatus(TRANSFER_STATUS_COMPLETED);

                setProgress(getProgress() + buffer.length);

            } catch (IOException e) {
                setStatus(TRANSFER_STATUS_ERROR);
            }

        } while(getStatus() == TRANSFER_STATUS_RUNNING);


        return super.execute();
    }

}
