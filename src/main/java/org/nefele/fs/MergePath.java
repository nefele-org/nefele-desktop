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
import org.nefele.utils.Tree;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MergePath implements Path {

    private final MergeFileSystem fileSystem;
    private final String path;
    private final String absolutePath;
    private final Tree<MergeNode> inode;
    private final BasicFileAttributeView attributeView;


    public MergePath(MergeFileSystem fileSystem, Tree<MergeNode> inode, String absolutePath, String path) {

        if(!requireNonNull(absolutePath).startsWith(MergeFileSystem.PATH_SEPARATOR))
            throw new IllegalArgumentException(absolutePath);


        this.fileSystem = fileSystem;
        this.path = path;
        this.absolutePath = absolutePath;
        this.inode = requireNonNull(inode);


        this.attributeView = new BasicFileAttributeView() {

            private final MergeNode inode = getInode().getData();

            @Override
            public String name() {
                return inode.getName();
            }

            @Override
            public BasicFileAttributes readAttributes() throws IOException {
                return new BasicFileAttributes() {

                    @Override
                    public FileTime lastModifiedTime() {
                        return FileTime.from(inode.getModifiedTime());
                    }

                    @Override
                    public FileTime lastAccessTime() {
                        return FileTime.from(inode.getAccessedTime());
                    }

                    @Override
                    public FileTime creationTime() {
                        return FileTime.from(inode.getCreatedTime());
                    }

                    @Override
                    public boolean isRegularFile() {
                        return !inode.getMime().equals("directory");
                    }

                    @Override
                    public boolean isDirectory() {
                        return inode.getMime().equals("directory");
                    }

                    @Override
                    public boolean isSymbolicLink() {
                        return false;
                    }

                    @Override
                    public boolean isOther() {
                        return false;
                    }

                    @Override
                    public long size() {
                        return inode.getSize();
                    }

                    @Override
                    public Object fileKey() {
                        return inode;
                    }
                };
            }

            @Override
            public void setTimes(FileTime mtime, FileTime atime, FileTime ctime) throws IOException {

                if (mtime != null)
                    inode.setModifiedTime(mtime.toInstant());

                if (atime != null)
                    inode.setAccessedTime(atime.toInstant());

                if (ctime != null)
                    inode.setCreatedTime(ctime.toInstant());

                inode.invalidate();

            }
        };


    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return path.startsWith(MergeFileSystem.PATH_SEPARATOR);
    }

    @Override
    public Path getRoot() {
        return fileSystem.getRootDirectories().iterator().next();
    }

    @Override
    public Path getFileName() {

        if(inode == null)
            throw new UnsupportedOperationException();

        return new MergePath(fileSystem, inode, absolutePath, inode.getData().getName());

    }

    @Override
    public Path getParent() {

        if(inode == null)
            throw new UnsupportedOperationException();

        if(inode.getParent() == null)
            return getRoot();
        
        

        var index = absolutePath.lastIndexOf(inode.getData().getName());

        if(index == 1)      // Parent is root?
            return getRoot();
        else
            return new MergePath(fileSystem, inode.getParent(), absolutePath.substring(0, index - 1), absolutePath.substring(0, index - 1));

    }

    @Override
    public int getNameCount() {
        return 1;
    }

    @Override
    public Path getName(int i) {

        if(i > 1)
            throw new IndexOutOfBoundsException();

        return getFileName();

    }

    public Tree<MergeNode> getInode() {
        return inode;
    }

    public BasicFileAttributeView getAttributeView() {
        return attributeView;
    }

    @Override
    public Path subpath(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(Path path) {

        if(!path.isAbsolute())
            return false;

        if(!isAbsolute())
            return false;

        return path.toString().startsWith(this.path);
    }

    @Override
    public boolean endsWith(Path path) {
        return path.toString().endsWith(this.path);
    }

    @Override
    public Path normalize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolve(Path path) {
        return get(absolutePath, path.toString());
    }

    @Override
    public Path resolve(String other) {
        return get(absolutePath, other);
    }

    @Override
    public Path relativize(Path path) {

        if(!(path instanceof MergePath))
            throw new IllegalArgumentException();


        MergePath mergePath = (MergePath) path;

        String relativePath = mergePath.absolutePath
                .replaceFirst(absolutePath, "")
                .replaceFirst("^.", "");


        return new MergePath(fileSystem, mergePath.getInode(), mergePath.absolutePath, relativePath);

    }

    @Override
    public URI toUri() {

        try {
            return new URI(fileSystem.provider().getScheme(), "", absolutePath, null, null);
        } catch (URISyntaxException ignored) { }

        throw new IllegalArgumentException(absolutePath);

    }

    @Override
    public Path toAbsolutePath() {
        return new MergePath(fileSystem, inode, absolutePath, absolutePath);
    }

    @Override
    public Path toRealPath(LinkOption... linkOptions) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File toFile() {
        return new MergeFile(this);
    }

    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {

        if(watchService instanceof MergeWatchService)
            return new MergeWatchKey((MergeWatchService) watchService, this);


        throw new IllegalArgumentException("watchService must be a instance of MergeWatchService!");

    }

    @Override
    public int compareTo(Path path) {

        if(!path.isAbsolute())
            return path.toString().charAt(0) - MergeFileSystem.PATH_SEPARATOR_CHAR;

        return this.path.compareTo(path.toString());

    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergePath paths = (MergePath) o;
        return getFileSystem().equals(paths.getFileSystem()) &&
                absolutePath.equals(paths.absolutePath);
    }


    public static Path get(String... strings) {

        String path = String
                .join(MergeFileSystem.PATH_SEPARATOR, strings)
                .replace(FileSystems.getDefault().getSeparator(), MergeFileSystem.PATH_SEPARATOR)
                .replace("//", MergeFileSystem.PATH_SEPARATOR)
                .trim();

        try {
            return Path.of(new URI("nefele", "", path, null));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException();
        }

    }



}
