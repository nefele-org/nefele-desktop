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

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


public class MergeFileSystem extends FileSystem {

    public final static String PATH_SEPARATOR = "/";
    public final static Character PATH_SEPARATOR_CHAR = '/';

    private final FileSystemProvider provider;
    private final MergeFileStore fileStore;
    private final MergeFileTree fileTree;


    public MergeFileSystem(FileSystemProvider provider) {

        this.provider = provider;
        this.fileStore = new MergeFileStore(this);
        this.fileTree = new MergeFileTree();

        Application.getInstance().runWorker(
                new Thread(this::synchronizeWorker, "MergeFileSystem::SynchronizeWorker()"), 3,
                Application.getInstance().getConfig().getInteger("core.mfs.sync").orElse(5), TimeUnit.SECONDS);

        Application.getInstance().addOnExitHandler(this::synchronizeWorker);

    }


    private synchronized void synchronizeWorker() {
        updateInode(getFileTree().getTree());
    }


    private void updateInode(Tree<Inode> tree) {

        Inode inode = tree.getData();

        if(inode.isDirty()) {

            try {
                Inode.put(inode);
            } catch (IOException e) {
                Application.panic(getClass(), e);
            }

            inode.setDirty(false);
        }

        tree.getChildren().forEach(this::updateInode);

    }


    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return MergeFileSystem.PATH_SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singleton(getPath(MergeFileSystem.PATH_SEPARATOR));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return Collections.singleton(fileStore);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    @Override
    public Path getPath(String s, String... strings) {

        if(!s.startsWith(MergeFileSystem.PATH_SEPARATOR))
            throw new IllegalArgumentException("Path must be absolute!");

        return new MergePath(this, fileTree.resolve(s.split(MergeFileSystem.PATH_SEPARATOR)), s, s);

    }

    @Override
    public PathMatcher getPathMatcher(String s) {

        if(!s.contains(":"))
            throw new IllegalArgumentException();

        if(!s.startsWith("regex:"))
            throw new UnsupportedOperationException();


        Pattern pattern = Pattern.compile(s.substring(s.indexOf(":") + 1));

        return path ->
                pattern.matcher(path.toString()).matches();

    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    public MergeFileStore getFileStore() {
        return fileStore;
    }

    public MergeFileTree getFileTree() {
        return fileTree;
    }

}
