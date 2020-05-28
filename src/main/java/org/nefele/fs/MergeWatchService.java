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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MergeWatchService implements WatchService {

    private final MergeFileSystem fileSystem;
    private final ArrayList<MergeWatchKey> watchKeys;
    private boolean closed;


    public MergeWatchService(MergeFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.watchKeys = new ArrayList<>();
        this.closed = false;
    }

    @Override
    public void close() throws IOException {

        watchKeys.forEach(MergeWatchKey::invalidate);
        watchKeys.clear();

        closed = true;

    }

    @Override
    public WatchKey poll() {

        if(isClosed())
            throw new ClosedWatchServiceException();


        return watchKeys.stream()
                .filter(i -> !i.getWatchEvents().isEmpty())
                .findFirst()
                .orElse(null);

    }

    @Override
    public WatchKey poll(long l, TimeUnit timeUnit) throws InterruptedException {

        if(isClosed())
            throw new ClosedWatchServiceException();

        throw new UnsupportedOperationException();

    }

    @Override
    public WatchKey take() throws InterruptedException {

        if(isClosed())
            throw new ClosedWatchServiceException();


        /* FIXME: Can be better than this... */

        WatchKey key;
        while((key = poll()) == null)
            Thread.yield();

        return key;

    }



    public MergeFileSystem getFileSystem() {
        return fileSystem;
    }

    public ArrayList<MergeWatchKey> getWatchKeys() {
        return watchKeys;
    }

    public boolean isClosed() {
        return closed;
    }

}
