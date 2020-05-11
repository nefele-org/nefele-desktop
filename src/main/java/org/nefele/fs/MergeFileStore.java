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

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

import static java.util.Objects.requireNonNull;


public class MergeFileStore extends FileStore {

    protected final MergeFileSystemState state;

    public MergeFileStore(MergeFileSystemState state) {
        this.state = requireNonNull(state);
    }


    @Override
    public String name() {
        return "MergeFileSystem";
    }

    @Override
    public String type() {
        return "MergeFileSystem";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return state.getTotalSpace();
    }

    @Override
    public long getUsableSpace() throws IOException {
        return state.getUsableSpace();
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
        return null;
    }

    @Override
    public Object getAttribute(String s) throws IOException {
        return null;
    }
}
