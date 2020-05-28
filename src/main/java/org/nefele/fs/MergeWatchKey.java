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

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;

public class MergeWatchKey implements WatchKey {

    private final MergePath path;
    private final MergeWatchService watchService;
    private final List<WatchEvent<?>> watchEvents;

    private boolean valid;


    public MergeWatchKey(MergeWatchService watchService, MergePath path) {

        this.path = requireNonNull(path);
        this.watchService = requireNonNull(watchService);
        this.watchEvents = new CopyOnWriteArrayList<>();
        this.valid = true;

        getWatchService().getWatchKeys().add(this);

    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public List<WatchEvent<?>> pollEvents() {
        return watchEvents;
    }

    @Override
    public boolean reset() {
        watchEvents.clear();
        return isValid();
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Watchable watchable() {
        return path;
    }

    public MergeWatchService getWatchService() {
        return watchService;
    }

    public List<WatchEvent<?>> getWatchEvents() {
        return watchEvents;
    }

    public void invalidate() {
        valid = false;
    }
}
