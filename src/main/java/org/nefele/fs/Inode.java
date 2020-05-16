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

import javafx.beans.property.BooleanProperty;
import org.nefele.Application;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;

public class Inode {

    private final Long id;
    private String name;
    private String mime;
    private Long size;
    private Instant createdTime;
    private Instant accessedTime;
    private Instant modifiedTime;
    private Boolean trashed;
    private Instant deleteTime;
    private Long parent;


    public Inode(String name, String mime, Long size, Long createdTime, Long accessedTime, Long modifiedTime, Boolean trashed, Long deleteTime, Long id, Long parent) {
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
    }

    public Inode(String name, String mime, Long id, Long parent) {
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

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Long getId() {
        return id;
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
}
