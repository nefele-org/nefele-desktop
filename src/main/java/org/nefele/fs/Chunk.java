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
import org.nefele.cloud.Drive;
import org.nefele.cloud.Drives;

import java.sql.SQLException;
import java.util.UUID;

public class Chunk {

    private static final long DEFAULT_CHUNK_SIZE = 4096L;

    private final String id;
    private final Drive drive;
    private final Long offset;
    private String hash;
    private boolean cached;
    private boolean dirty;


    public Chunk(String id, Long offset, Drive drive, String hash) {
        this.id = id;
        this.drive = drive;
        this.offset = offset;
        this.hash = hash;
        this.cached = Application.getInstance().getCache().exist(id);
    }


    public String getId() {
        return id;
    }

    public Drive getDrive() {
        return drive;
    }

    public Long getOffset() {
        return offset;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean isCached() {
        return cached;
    }

    public void setCached(Boolean cached) {
        this.cached = cached;
    }


    public void invalidate() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }



    public static void free(Chunk chunk) {

        try {

            Application.getInstance().getDatabase().query ("DELETE FROM chunks WHERE id = ?",
                    s -> s.setString(1, chunk.getId()),
                    null
            );

        } catch (SQLException e) {
            Application.panic(Inode.class, e);
        }


        if(chunk.isCached())
            Application.getInstance().getCache().free(chunk);


    }

    public static Chunk alloc(String inode, Long offset) {


        Drive drive = Drives.nextAllocatable();
        String id = UUID.randomUUID().toString();

//        try {

//            Application.getInstance().getDatabase().query (
//
//                    "INSERT INTO chunks (id, offset, inode, drive, hash) VALUES (?, ?, ?, ?, ?)",
//                    s -> {
//
//                        s.setString(1, id);
//                        s.setLong(2, offset);
//                        s.setString(3, inode);
//                        s.setString(4, drive.getId());
//                        s.setString(5, "");
//
//                    },
//                    null
//            );

            drive.setChunks(drive.getChunks() + 1L);
            drive.invalidate();

            return new Chunk(id, offset, drive, "");

//        } catch (SQLException e) {
//            Application.panic(Chunk.class, e);
//        }

//        throw new IllegalStateException();
//
    }

    public static long getSize() {
        return Application.getInstance().getConfig()
                .getLong("core.mfs.blocksize")
                .orElse(Chunk.DEFAULT_CHUNK_SIZE);
    }

}
