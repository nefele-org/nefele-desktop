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
import org.nefele.cloud.DriveService;
import org.nefele.core.Mime;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

import static java.util.Objects.requireNonNull;


public class MergeFileStore extends FileStore {

    public final MergeFileSystem fileSystem;

    public MergeFileStore(MergeFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public String name() {
        return "nefele";
    }

    @Override
    public String type() {
        return "mfs";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {

        return DriveService.getInstance().getDrives()
                .stream()
                .mapToLong(Drive::getQuota)
                .sum() * MergeChunk.getSize();

    }

    @Override
    public long getUsableSpace() throws IOException {

        return getTotalSpace() - DriveService.getInstance().getDrives()
                .stream()
                .mapToLong(Drive::getChunks)
                .sum() * MergeChunk.getSize();

    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return getUsableSpace();
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> aClass) {
        return false;
    }

    @Override
    public boolean supportsFileAttributeView(String s) {
        return false;
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void createDirectory(MergePath path, FileAttribute<?>... fileAttributes) throws IOException {

        MergeNode inode = path.getInode().getData();

        inode.setMime(Mime.FOLDER.getType());
        inode.invalidate();

        Application.log(getClass(), "Created directory %s", path.toString());

    }

    public void delete(MergePath path) throws IOException {

        if(!path.getInode().getChildren().isEmpty())
            throw new DirectoryNotEmptyException(path.toString());

        if(path.getInode().getParent() == null)
            throw new IOException("Can not delete root directory!");


        fileSystem.getStorage()
                .free(path.getInode().getData());

        path.getInode().getParent().remove(path.getInode());

        Application.log(getClass(), "Deleted %s", path.toString());

    }


    public void moveToTrash(MergePath path) throws IOException {

        if(!path.getInode().getChildren().isEmpty())
            throw new DirectoryNotEmptyException(path.toString());

        if(path.getInode().getParent() == null)
            throw new IOException("Can not move to trash root directory!");


        path.getInode().getData().setTrashed(true);
        path.getInode().getData().invalidate();

        Application.log(getClass(), "Moved to trash %s", path.toString());
    }
}
