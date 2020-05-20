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
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Drive {

    public static final Integer STATUS_UNKNOWN = 0;
    public static final Integer STATUS_CONNECTING = 1;
    public static final Integer STATUS_READY = 2;
    public static final Integer STATUS_DISCONNECTING = 3;
    public static final Integer STATUS_DISCONNECTED = 4;
    public static final Integer STATUS_ERROR = 100;

    public static final Integer ERROR_OK = 0;
    public static final Integer ERROR_EXPIRED = 1;
    public static final Integer ERROR_LOGIN_FAIL = 2;
    public static final Integer ERROR_UNREACHABLE = 3;
    public static final Integer ERROR_UNKNOWN = 100;



    private final ReadOnlyStringProperty id;
    private final ReadOnlyStringProperty service;
    private final LongProperty quota;
    private final LongProperty chunks;
    private final IntegerProperty status;
    private final IntegerProperty error;
    private boolean dirty;


    public Drive(String id, String service, long quota, long blocks) {

        this.id = new SimpleStringProperty(id);
        this.service = new SimpleStringProperty(service);

        this.quota = new SimpleLongProperty(quota);
        this.chunks = new SimpleLongProperty(blocks);
        this.status = new SimpleIntegerProperty(STATUS_UNKNOWN);
        this.error = new SimpleIntegerProperty(ERROR_OK);

        this.dirty = false;

    }


    public abstract OutputStream writeChunk(MergeChunk chunk);
    public abstract InputStream readChunk(MergeChunk chunk);
    public abstract Drive initialize();
    public abstract Drive exit();



    public void invalidate() {
        dirty = true;
    }

    protected void validate() {
        dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public String getId() {
        return id.get();
    }

    public ReadOnlyStringProperty idProperty() {
        return id;
    }

    public String getService() {
        return service.get();
    }

    public ReadOnlyStringProperty serviceProperty() {
        return service;
    }

    public long getQuota() {
        return quota.get();
    }

    public LongProperty quotaProperty() {
        return quota;
    }

    public void setQuota(long quota) {
        this.quota.set(quota);
    }

    public long getChunks() { return chunks.get(); }

    public LongProperty chunksProperty() {
        return chunks;
    }

    public void setChunks(long chunks) {
        this.chunks.set(chunks);
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

    public int getError() {
        return error.get();
    }

    public IntegerProperty errorProperty() {
        return error;
    }

    public void setError(int error) {
        this.error.set(error);
    }
}
