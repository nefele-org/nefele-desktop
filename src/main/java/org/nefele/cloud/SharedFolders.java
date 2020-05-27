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

package org.nefele.cloud;

import org.nefele.Application;
import org.nefele.Service;
import org.nefele.fs.MergePath;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

public final class SharedFolders implements Service {

    private final static SharedFolders instance = new SharedFolders();

    public static SharedFolders getInstance() {
        return instance;
    }



    private final HashSet<SharedFolder> sharedFolders;

    public SharedFolders() {
        sharedFolders = new HashSet<>();
    }

    @Override
    public void initialize(Application app) {

    }

    @Override
    public void synchronize(Application app) {

    }

    @Override
    public void exit(Application app) {

    }


    public HashSet<SharedFolder> getSharedFolders() {
        return sharedFolders;
    }

    public boolean isShared(MergePath path) {


        return getSharedFolders()
                .stream()
                .peek(i -> Application.log(getClass(), "SHARED FOLDER: %s == %s: %s", i.getCloudPath(), path, i.getCloudPath().equals(path)))
                .anyMatch(i -> i.getCloudPath().equals(path));

    }
}
