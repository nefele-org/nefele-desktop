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

import org.nefele.Application;
import org.nefele.Invalidatable;
import org.nefele.cloud.DriveProvider;

import java.util.Objects;

public class MergeChunk implements Invalidatable {

    private final String id;
    private final long offset;
    private final MergeNode inode;
    private final DriveProvider driveProvider;
    private final boolean compressed;
    private final boolean encrypted;
    private long size;
    private long revision;
    private boolean dirty;

    public MergeChunk(String id, long offset, MergeNode inode, DriveProvider driveProvider, long revision, long size, boolean compressed, boolean encrypted) {
        this.id = id;
        this.offset = offset;
        this.inode = inode;
        this.driveProvider = driveProvider;
        this.revision = revision;
        this.compressed = compressed;
        this.encrypted = encrypted;
        this.size = size;
        this.dirty = false;
    }


    public String getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    public MergeNode getInode() {
        return inode;
    }

    public DriveProvider getDriveProvider() {
        return driveProvider;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergeChunk chunk = (MergeChunk) o;
        return getId().equals(chunk.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }


    public static long getDefaultSize() {
        return Application.getInstance().getConfig().getLong("core.mfs.blocksize").orElse(65536L);
    }

}
