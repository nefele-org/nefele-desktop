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

import org.nefele.Service;
import org.nefele.fs.MergePath;

import java.util.HashSet;


public final class SharedFolders implements Service {

    private final static SharedFolders instance = new SharedFolders();

    public static SharedFolders getInstance() {
        return instance;
    }



    private final HashSet<SharedFolder> sharedFolders;

    private SharedFolders() {
        sharedFolders = new HashSet<>();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void synchronize() {

        synchronized (sharedFolders) {
            sharedFolders.forEach(SharedFolder::synchronize);
        }

    }

    @Override
    public void exit() {
        synchronized (sharedFolders) {
            sharedFolders.forEach(SharedFolder::exit);
        }
    }




    public <T extends SharedFolder & Service> void addSharedFolderService(T service) {
        synchronized (sharedFolders) {
            sharedFolders.add(service);
        }
    }

    public <T extends SharedFolder & Service> void removeSharedFolderServiceByPath(MergePath path) {
        synchronized(sharedFolders) {
            sharedFolders.stream()
                    .filter(i -> i.getCloudPath().equals(path))
                    .peek(SharedFolder::exit)
                    .findFirst()
                    .ifPresent(sharedFolders::remove);

        }
    }


    public boolean isShared(MergePath path) {

        synchronized (sharedFolders) {
            return sharedFolders
                    .stream()
                    .anyMatch(i -> i.getCloudPath().equals(path));
        }

    }

}
