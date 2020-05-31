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
import java.nio.file.*;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MergeWatchService implements WatchService {

    private final BlockingQueue<WatchKey> watchKeys;
    private final HashSet<MergeWatchKey> registeredWatchKeys;

    public MergeWatchService() {
        this.watchKeys = new LinkedBlockingQueue<>();
        this.registeredWatchKeys = new HashSet<>();
    }



    @Override
    public void close() throws IOException {
        watchKeys.clear();
    }

    @Override
    public WatchKey poll() {
        return watchKeys.poll();
    }

    @Override
    public WatchKey poll(long l, TimeUnit timeUnit) throws InterruptedException {
        return watchKeys.poll(l, timeUnit);
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return watchKeys.take();
    }



    public WatchKey register(Watchable watchable, Iterable<? extends WatchEvent.Kind<?>> eventKinds) throws ClosedWatchServiceException {

        if(!(watchable instanceof MergePath))
            throw new IllegalArgumentException("watchable must be a MergePath!");


        final var watchKey = new MergeWatchKey(this, (MergePath) watchable, eventKinds);
        registeredWatchKeys.add(watchKey);

        return watchKey;

    }


    public void post(WatchEvent.Kind<Path> eventKind, Path context) {

        registeredWatchKeys.forEach(watchKey -> {

            if(!watchKey.subscribesTo(eventKind))
                return;

            if(!watchKey.watchable().equals(context.getParent()))
                return;


            watchKey.post(new MergeWatchEvent<>(eventKind, 1, ((Path) watchKey.watchable()).relativize(context)));

        });

    }

    public void enqueue(WatchKey watchKey) {
        watchKeys.offer(watchKey);
    }






}
