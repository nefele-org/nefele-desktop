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
import org.nefele.core.Mime;
import org.sqlite.SQLiteErrorCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Inode {

    private final ArrayList<Chunk> chunks;
    private final String id;

    private String name;
    private String mime;
    private Long size;
    private Instant createdTime;
    private Instant accessedTime;
    private Instant modifiedTime;
    private Boolean trashed;
    private Instant deleteTime;
    private String parent;
    private Boolean dirty;


    public Inode(String name, String mime, Long size, Long createdTime, Long accessedTime, Long modifiedTime, Boolean trashed, Long deleteTime, String id, String parent) {
        this.chunks = new ArrayList<>();
        this.name = name;
        this.mime = mime;
        this.size = size;
        this.createdTime = Instant.ofEpochSecond(createdTime);
        this.accessedTime = Instant.ofEpochSecond(accessedTime);
        this.modifiedTime = Instant.ofEpochSecond(modifiedTime);
        this.trashed = trashed;
        this.deleteTime = Instant.ofEpochSecond(deleteTime);
        this.id = id;
        this.parent = parent;
        this.dirty = false;
    }

    public Inode(String name, String mime, String id, String parent) {
        this.chunks = new ArrayList<>();
        this.name = name;
        this.mime = mime;
        this.id = id;
        this.parent = parent;
        this.size = 0L;
        this.createdTime = Instant.now();
        this.accessedTime = Instant.now();
        this.modifiedTime = Instant.now();
        this.trashed = false;
        this.deleteTime = Instant.now();
        this.dirty = false;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
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

    public void setModifiedTime(Instant modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Boolean getTrashed() {
        return trashed;
    }

    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }

    public Instant getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Instant deleteTime) {
        this.deleteTime = deleteTime;
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

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    @Override
    public String toString() {
        return "Inode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mime='" + mime + '\'' +
                ", size=" + size +
                ", createdTime=" + createdTime +
                ", accessedTime=" + accessedTime +
                ", modifiedTime=" + modifiedTime +
                ", trashed=" + trashed +
                ", deleteTime=" + deleteTime +
                ", parent=" + parent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inode inode = (Inode) o;
        return getId().equals(inode.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void invalidate() {
        setDirty(true);
    }



    public static Inode alloc(String name, String parent) throws IOException {

        try {

            Application.getInstance().getDatabase().query (

                    "INSERT INTO inodes (name, mime, size, ctime, atime, mtime, trash, dtime, parent, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    s -> {

                        s.setString(1, name);
                        s.setString(2, Mime.UNKNOWN.getType());
                        s.setLong(3, 0L);
                        s.setLong(4, Instant.now().getEpochSecond());
                        s.setLong(5, Instant.now().getEpochSecond());
                        s.setLong(6, Instant.now().getEpochSecond());
                        s.setInt(7, 0);
                        s.setLong(8, Instant.now().getEpochSecond());
                        s.setString(9, parent);
                        s.setString(10, UUID.randomUUID().toString());

                    },
                    null
            );

        } catch (SQLException e) {
            throw new IOException();
        }


        return get(name, parent);

    }


    public static void put(Inode inode) throws IOException {

        try {

            Application.getInstance().getDatabase().query (

                    "UPDATE inodes SET name = ?, mime = ?, size = ?, ctime = ?, atime = ?, mtime = ?, trash = ?, dtime = ?, parent = ? WHERE id = ?",
                    s -> {

                        s.setString(1, inode.getName());
                        s.setString(2, inode.getMime());
                        s.setLong(3, inode.getSize());
                        s.setLong(4, inode.getCreatedTime().getEpochSecond());
                        s.setLong(5, inode.getAccessedTime().getEpochSecond());
                        s.setLong(6, inode.getModifiedTime().getEpochSecond());
                        s.setInt(7, inode.getTrashed() ? 1 : 0);
                        s.setLong(8, inode.getDeleteTime().getEpochSecond());
                        s.setString(9, inode.getParent());
                        s.setString(10, inode.getId());

                    },
                    null
            );

        } catch (SQLException e) {

            if(e.getErrorCode() == SQLiteErrorCode.SQLITE_NOTFOUND.code)
                throw new FileNotFoundException(inode.getName());

            Application.panic(Inode.class, e);

        }

    }

    public static Inode get(String name, String parent) throws IOException {

        try {

            final AtomicReference<Inode> inode = new AtomicReference<>(null);

            Application.getInstance().getDatabase().query(
                    "SELECT * FROM inodes LEFT JOIN chunks ON chunks.inode = inodes.id WHERE inodes.parent = ? AND inodes.name = ?",
                    s -> {
                        s.setString(1, parent);
                        s.setString(2, name);
                    },
                    r -> {

                        if(r.isFirst()) {

                            inode.set(new Inode(
                                    r.getString(1),
                                    r.getString(2),
                                    r.getLong(3),
                                    r.getLong(4),
                                    r.getLong(5),
                                    r.getLong(6),
                                    r.getBoolean(7),
                                    r.getLong(8),
                                    r.getString(9),
                                    r.getString(10)
                            ));

                        }


                        if(r.getLong(11) > 0) {

                            inode.get().getChunks().add(new Chunk(
                                    r.getString(11),
                                    r.getLong(12),
                                    Drives.fromId(r.getString(14)),
                                    r.getString(15)
                            ));

                        }

                    }
            );

            return inode.get();

        } catch (SQLException e) {

            if(e.getErrorCode() == SQLiteErrorCode.SQLITE_NOTFOUND.code)
                throw new FileNotFoundException(name);

            Application.panic(Inode.class, e);

        }

        throw new IllegalStateException();
    }

    public static void free(Inode inode) throws IOException {

        try {

            Application.getInstance().getDatabase().query ("DELETE FROM inodes WHERE id = ?",
                    s -> s.setString(1, inode.getId()),
                    null
            );

        } catch (SQLException e) {

            if(e.getErrorCode() == SQLiteErrorCode.SQLITE_NOTFOUND.code)
                throw new FileNotFoundException(inode.getName());

            Application.panic(Inode.class, e);

        }


        inode.getChunks().forEach(Chunk::free);

    }

    public static String getId(String name, String parent) {
        return UUID.randomUUID().toString();
    }

}
