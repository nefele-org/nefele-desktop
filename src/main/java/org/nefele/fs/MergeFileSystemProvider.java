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

import org.nefele.utils.Tree;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class MergeFileSystemProvider extends FileSystemProvider {

    private final MergeFileSystem fileSystem;

    public MergeFileSystemProvider() {
        this.fileSystem = new MergeFileSystem(this);
    }

    @Override
    public String getScheme() {
        return "cloud";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> map) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem getFileSystem(URI uri) {

        if(uri.getScheme().equals(getScheme()))
            return fileSystem;

        throw new IllegalArgumentException();

    }

    @Override
    public Path getPath(URI uri) {
        return fileSystem.getPath(uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {

        if(!(path instanceof MergePath))
            throw new IllegalArgumentException();


        final Tree<Inode> inode = ((MergePath) path).getInode();

        if(inode == null)
            return null;


        return new DirectoryStream<Path>() {

            @Override
            public Iterator<Path> iterator() {

                return new Iterator<Path>() {

                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return (index < inode.getChildren().size());
                    }

                    @Override
                    public Path next() {

                        Tree<Inode> entry = inode.getChildren().get(index++);

                        return new MergePath(fileSystem, entry, fileSystem.getFileTree().toAbsolutePath(entry), entry.getData().getName());

                    }

                };

            }

            @Override
            public void close() throws IOException {

            }

        };

    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path1) throws IOException {
        return path.toAbsolutePath().equals(path1.toAbsolutePath());
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {

        if(!(path instanceof MergePath))
            throw new IllegalArgumentException();

        if(!(path.getFileSystem() instanceof MergeFileSystem))
            throw new IllegalArgumentException();

        return ((MergeFileSystem) path.getFileSystem()).getFileStore();

    }

    @Override
    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {
        throw new UnsupportedOperationException();
    }
}
