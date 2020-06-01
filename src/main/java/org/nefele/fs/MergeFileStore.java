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
import org.nefele.core.Mime;
import org.nefele.core.Mimes;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveProviders;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.stream.Collectors;


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

        return DriveProviders.getInstance().getDriveProviders()
                .stream()
                .mapToLong(DriveProvider::getQuota)
                .sum();

    }

    @Override
    public long getUsableSpace() throws IOException {

        return getTotalSpace() - fileSystem.getStorage().getInodeStream()
                .flatMap(i -> i.getChunks().stream())
                .collect(Collectors.toList())
                .stream()
                .mapToLong(MergeChunk::getSize)
                .sum();

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


    public void createFile(MergePath path, FileAttribute<?>... fileAttributes) throws IOException {


        MergeNode inode = path.getInode();

        inode.setMime(Mimes.getInstance().getByExtension(inode.getName()).getType());
        inode.invalidate();


        fileSystem.getStorage()
                .create(inode);

        fileSystem.getWatchService()
                .post(StandardWatchEventKinds.ENTRY_CREATE, path);

        Application.log(getClass(), "Created file %s", path.toString());

    }

    public void createDirectory(MergePath path, FileAttribute<?>... fileAttributes) throws IOException {



        MergeNode inode = path.getInode();

        inode.setMime(Mime.FOLDER.getType());
        inode.invalidate();


        fileSystem.getStorage().getInodes()
                .add(inode);

        fileSystem.getWatchService()
                .post(StandardWatchEventKinds.ENTRY_CREATE, path);

        Application.log(getClass(), "Created directory %s", path.toString());

    }

    public void delete(MergePath path) throws IOException {

        for(MergeNode child : fileSystem.getFileTree().listChildren(path.getInode()))
            delete((MergePath) path.resolve(child.getName()));


        fileSystem.getStorage()
                .free(path.getInode());

        fileSystem.getWatchService()
                .post(StandardWatchEventKinds.ENTRY_DELETE, path);


        Application.log(getClass(), "Deleted %s", path.toString());

    }


    public void move(MergePath oldPath, MergePath newPath) throws IOException {

        if(Files.exists(newPath))
            throw new FileAlreadyExistsException(newPath.toString());


        String newParent = oldPath.getInode().getParent();
        String newName = newPath.getInode().getName();


        newPath.getInode().setName(newName);
        newPath.getInode().setParent(newParent);
        newPath.getInode().invalidate();


        fileSystem.getStorage().getInodes()
                .remove(oldPath.getInode());

        fileSystem.getStorage().getInodes()
                .add(newPath.getInode());


        fileSystem.getWatchService().post(StandardWatchEventKinds.ENTRY_DELETE, oldPath);
        fileSystem.getWatchService().post(StandardWatchEventKinds.ENTRY_CREATE, newPath);

        Application.log(getClass(), "Moved %s to %s", oldPath.toString(), newPath.toString());

    }

    public void checkAccess(MergePath path) throws NoSuchFileException {

        if(!fileSystem.getStorage().getInodes().contains(path.getInode()))
            throw new NoSuchFileException(path.toString());

    }
}
