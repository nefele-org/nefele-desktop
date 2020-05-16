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
import javafx.scene.Parent;
import org.nefele.Application;
import org.nefele.utils.Tree;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Objects;

public class MergePath implements Path {

    private final MergeFileSystem fileSystem;
    private final String path;
    private final String absolutePath;
    private final Tree<Inode> inode;

    public MergePath(MergeFileSystem fileSystem, Tree<Inode> inode, String absolutePath, String path) {

        this.fileSystem = fileSystem;
        this.path = path;
        this.absolutePath = absolutePath;
        this.inode = inode;

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
         return new MergePath(fileSystem, inode, absolutePath, inode.getData().getName());
    }

    @Override
    public Path getParent() {
        throw new UnsupportedOperationException();
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

    public Tree<Inode> getInode() {
        return inode;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Path relativize(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI toUri() {

        try {
            return new URI(fileSystem.provider().getScheme(), "", absolutePath, null, null);
        } catch (URISyntaxException ignored) { }

        throw new IllegalArgumentException(path);

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
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
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
                path.equals(paths.path);
    }
}
