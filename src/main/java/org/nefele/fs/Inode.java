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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Instant;

public class Inode {

    private String path;
    private String mime;
    private InputStream inputStream;
    private Instant ctime;
    private Instant atime;
    private Instant mtime;


    public Inode() {

        path = "";
        mime = "text/plain";
        inputStream = null;

        ctime = Instant.now();
        atime = Instant.now();
        mtime = Instant.now();

    }


    public Inode setPath(String path) {
        this.path = path;
        return this;
    }

    public Inode setMime(String mime) {
        this.mime = mime;
        return this;
    }

    public Inode setData(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public Inode setCreatedTime(Instant instant) {
        this.ctime = instant;
        return this;
    }

    public Inode setAccessedTime(Instant instant) {
        this.atime = instant;
        return this;
    }

    public Inode setModifiedTime(Instant instant) {
        this.mtime = instant;
        return this;
    }


    public Inode create() throws IOException {

        if(path == null)
            throw new NullPointerException("path cannot be null");

        try {

            Application.getInstance().getDatabase().query(
                    "INSERT INTO inodes (path, mime, ctime, atime, mtime) values (?, ?, ?, ?, ?)",
                    s -> {
                        s.setString(1, path);
                        s.setString(2, mime);
                        s.setLong(3, ctime.getEpochSecond());
                        s.setLong(4, atime.getEpochSecond());
                        s.setLong(5, mtime.getEpochSecond());
                    },
                    null
            );

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());

            throw new IOException(e.getMessage());
        }

        return this;

    }


    public Inode remove() throws IOException {

        if(path == null)
            throw new NullPointerException("path cannot be null");

        try {

            Application.getInstance().getDatabase().query(
                    "DELETE FROM inodes WHERE path = ? AND mime = ?",
                    s -> {
                        s.setString(1, path);
                        s.setString(2, mime);
                    },
                    null
            );

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());

            throw new IOException(e.getMessage());
        }

        return this;

    }

}
