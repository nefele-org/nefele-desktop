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

import javafx.beans.property.*;
import org.nefele.fs.Inode;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;

import static java.util.Objects.requireNonNull;

public abstract class TransferInfo {

    public final static int TRANSFER_TYPE_DOWNLOAD = 0;
    public final static int TRANSFER_TYPE_UPLOAD = 1;

    public final static int TRANSFER_STATUS_READY = 0;
    public final static int TRANSFER_STATUS_RUNNING = 1;
    public final static int TRANSFER_STATUS_COMPLETED = 2;
    public final static int TRANSFER_STATUS_ERROR = 3;
    public final static int TRANSFER_STATUS_PAUSED = 4;
    public final static int TRANSFER_STATUS_CANCELED = 5;
    public final static int TRANSFER_STATUS_RESUME = 6;


    private final ReadOnlyStringProperty name;
    private final ReadOnlyLongProperty size;
    private final ReadOnlyIntegerProperty type;
    private final LongProperty progress;
    private final IntegerProperty status;
    private final IntegerProperty speed;
    private final ReadOnlyObjectProperty<Inode> inode;

    private long lastProgress = 0;



    public TransferInfo(Inode inode, int type) {

        requireNonNull(inode);

        this.name = new SimpleStringProperty(inode.getName());
        this.size =  new SimpleLongProperty(inode.getSize());
        this.type = new SimpleIntegerProperty(type);
        this.progress = new SimpleLongProperty(0L);
        this.status = new SimpleIntegerProperty(TRANSFER_STATUS_READY);
        this.speed = new SimpleIntegerProperty(0);
        this.inode = new SimpleObjectProperty<>(inode);

    }

    public abstract Integer execute();


    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public long getSize() {
        return size.get();
    }

    public ReadOnlyLongProperty sizeProperty() {
        return size;
    }

    public int getType() {
        return type.get();
    }

    public ReadOnlyIntegerProperty typeProperty() {
        return type;
    }

    public long getProgress() {
        return progress.get();
    }

    public LongProperty progressProperty() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress.set(progress);
    }

    public int getStatus() {
        return status.get();
    }

    public IntegerProperty statusProperty() {
        return status;
    }

    public void setStatus(int status) {
        this.status.set(status);
    }

    public int getSpeed() {
        return speed.get();
    }

    public IntegerProperty speedProperty() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed.set(speed);
    }

    public Inode getInode() {
        return inode.get();
    }

    public ReadOnlyObjectProperty<Inode> inodeProperty() {
        return inode;
    }


    public final void updateSpeed() {
        setSpeed(getSpeed() + (int) (getProgress() - lastProgress));
        lastProgress = getProgress();
    }


}
