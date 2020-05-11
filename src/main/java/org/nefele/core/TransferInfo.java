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

import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public class TransferInfo {

    public final static int TRANSFER_TYPE_DOWNLOAD = 0;
    public final static int TRANSFER_TYPE_UPLOAD = 1;
    public final static int TRANSFER_TYPE_COMMAND = 2;
    public final static int TRANSFER_TYPE_DOWNLOAD_GROUP = 3;
    public final static int TRANSFER_TYPE_UPLOAD_GROUP = 4;

    public final static int TRANSFER_STATUS_READY = 0;
    public final static int TRANSFER_STATUS_RUNNING = 1;
    public final static int TRANSFER_STATUS_COMPLETED = 2;
    public final static int TRANSFER_STATUS_ERROR = 3;
    public final static int TRANSFER_STATUS_PAUSED = 4;
    public final static int TRANSFER_STATUS_CANCELED = 5;
    public final static int TRANSFER_STATUS_RESUME = 6;


    protected String name;
    protected int size;
    protected int progress;
    protected int type;
    protected int status;
    protected int speed;
    protected boolean parallel;
    protected Instant startTime;

    private int lastProgress = 0;


    public TransferInfo(String name, int size, int type, boolean parallel) {
        this.name = name;
        this.parallel = parallel;
        this.size = size;
        this.progress = 0;
        this.type = type;
        this.startTime = Instant.now();
        this.status = TRANSFER_STATUS_READY;
    }


    public synchronized Integer execute() {
        return getStatus();
    }


    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getProgress() {
        return progress;
    }

    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRemainingTime() {

        if(speed == 0)
            return -1;

        return (((size - progress) / speed)) + 1;

    }

    public boolean isParallel() {
        return parallel;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public final void updateSpeed() {

        speed += (int) ((progress - lastProgress) * (1000.0 / (double) TransferQueue.TRANSFER_QUEUE_INTERVAL));
        speed /= 2;

        lastProgress = progress;
    }


}
