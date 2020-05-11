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

import java.time.Instant;

public abstract class MergeFile {

    protected final int id;
    protected Instant creationTime;
    protected Instant lastAccessTime;
    protected Instant lastModifiedTime;

    protected MergeFile(int id) {
        this.id = id;
        this.creationTime = Instant.now();
        this.lastAccessTime = Instant.now();
        this.lastModifiedTime = Instant.now();
    }

    public int getId() {
        return id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getLastAccessTime() {
        return lastAccessTime;
    }

    public Instant getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long size() {
        return 0;
    }

    public final boolean isDirectory() {
        //return this instanceof Directory;
        return false;
    }

    private final boolean isRegularFile() {
        //return this instanceof RegularFile;
        return true;
    }


    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public void setLastAccessTime(Instant lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public void setLastModifiedTime(Instant lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
}
