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

package org.nefele.fs;

import org.nefele.Invalidatable;

import java.time.Instant;
import java.util.ArrayList;

public class MergeNode implements Invalidatable {

    private String name;
    private String mime;
    private long size;
    private Instant createdTime;
    private Instant accessedTime;
    private Instant modifiedTime;
    private boolean trashed;
    private Instant deletedTime;
    private String parent;
    private boolean dirty;

    private final String id;
    private final ArrayList<MergeChunk> chunks;


    public MergeNode(String name, String mime, long size, Instant createdTime, Instant accessedTime, Instant modifiedTime, boolean trashed, Instant deletedTime, String id, String parent) {

        this.name = name;
        this.mime = mime;
        this.size = size;
        this.createdTime = createdTime;
        this.accessedTime = accessedTime;
        this.modifiedTime = modifiedTime;
        this.trashed = trashed;
        this.deletedTime = deletedTime;
        this.id = id;
        this.parent = parent;
        this.dirty = false;

        this.chunks = new ArrayList<>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getAccessedTime() {
        return accessedTime;
    }

    public void setAccessedTime(Instant accessedTime) {
        this.accessedTime = accessedTime;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Instant modifiedTiem) {
        this.modifiedTime = modifiedTiem;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public Instant getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Instant deletedTime) {
        this.deletedTime = deletedTime;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public ArrayList<MergeChunk> getChunks() {
        return chunks;
    }

    @Override
    public void invalidate() {
        dirty = true;
    }

    @Override
    public void validate() {
        dirty = false;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }
}
