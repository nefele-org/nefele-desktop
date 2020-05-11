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

import java.nio.file.attribute.FileAttribute;

import static java.util.Objects.requireNonNull;


public class MergeFileSystemView {

    protected MergeFileStore fileStore;
    protected MergeDirectory workingDirectory;
    protected MergePath workingDirectoryPath;


    public MergeFileSystemView(MergeFileStore fileStore) {
        this.fileStore = requireNonNull(fileStore);
    }

    public MergeDirectory getWorkingDirectory() {
        return workingDirectory;
    }

    public MergePath getWorkingDirectoryPath() {
        return workingDirectoryPath;
    }

    public MergeDirent lookup(MergePath path) {
        return null;
    }

    public MergeDirectory createDirectory(MergePath path, FileAttribute<?>... attributes) {
        return null;
    }

    public MergeRegularFile createRegularFile(MergePath path, FileAttribute<?>... attributes) {
        return null;
    }


}
