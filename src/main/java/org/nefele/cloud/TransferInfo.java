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

import javafx.beans.property.*;
import org.nefele.fs.MergeChunk;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergePath;

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
    private final ReadOnlyIntegerProperty type;
    private final LongProperty size;
    private final LongProperty progress;
    private final IntegerProperty status;
    private final IntegerProperty speed;
    private final ReadOnlyObjectProperty<MergePath> path;
    private final ReadOnlyObjectProperty<MergeFileSystem> fileSystem;

    private long lastProgress = 0;



    public TransferInfo(MergePath path, int type) {

        requireNonNull(path);

        if(!(path.getFileSystem() instanceof MergeFileSystem))
            throw new IllegalArgumentException();


        this.name = new SimpleStringProperty(path.getFileName().toString());
        this.type = new SimpleIntegerProperty(type);
        this.progress = new SimpleLongProperty(0L);
        this.status = new SimpleIntegerProperty(TRANSFER_STATUS_READY);
        this.speed = new SimpleIntegerProperty(0);
        this.path = new SimpleObjectProperty<>(path);
        this.fileSystem = new SimpleObjectProperty<>((MergeFileSystem) path.getFileSystem());

        this.size =  new SimpleLongProperty(
                path.getInode().getChunks()
                        .stream()
                        .mapToLong(MergeChunk::getSize)
                        .sum()
        );


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

    public LongProperty sizeProperty() {
        return size;
    }

    public void setSize(long size) {
        this.size.setValue(size);
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

    public MergePath getPath() {
        return path.get();
    }

    public ReadOnlyObjectProperty<MergePath> pathProperty() {
        return path;
    }

    public MergeFileSystem getFileSystem() {
        return fileSystem.get();
    }

    public ReadOnlyObjectProperty<MergeFileSystem> fileSystemProperty() {
        return fileSystem;
    }

    public final void updateSpeed() {
        setSpeed((int) (getProgress() - lastProgress));
        lastProgress = getProgress();
    }


}
