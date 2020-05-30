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

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Service;
import javafx.concurrent.Worker;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class MergeWatchKey implements WatchKey {


    private final MergePath watchable;
    private final MergeWatchService watchService;
    private final ImmutableSet<WatchEvent.Kind<?>> subscribedEvents;

    private final AtomicBoolean signalled;
    private final AtomicBoolean valid;
    private final AtomicInteger overflow;

    private final BlockingQueue<WatchEvent<?>> eventQueue;



    public MergeWatchKey(MergeWatchService watchService, MergePath watchable, Iterable<? extends WatchEvent.Kind<?>> eventTypes) {

        this.watchable = requireNonNull(watchable);
        this.watchService = requireNonNull(watchService);
        this.subscribedEvents = ImmutableSet.copyOf(eventTypes);

        this.signalled = new AtomicBoolean(false);
        this.valid = new AtomicBoolean(true);
        this.overflow = new AtomicInteger(0);

        this.eventQueue = new ArrayBlockingQueue<>(128);

    }




    public boolean subscribesTo(WatchEvent.Kind<?> watchEvent) {
        return subscribedEvents.contains(watchEvent);
    }


    public void post(WatchEvent<?> watchEvent) {

        if(!eventQueue.offer(watchEvent))
            overflow.incrementAndGet();

        signal();

    }


    public void signal() {

        if(signalled.compareAndSet(false, true))
            watchService.enqueue(this);

    }





    @Override
    public boolean isValid() {
        return valid.get();
    }


    @Override
    public List<WatchEvent<?>> pollEvents() {

        List<WatchEvent<?>> events = new ArrayList<>(eventQueue.size());
        eventQueue.drainTo(events);

        if(overflow.get() > 0)
            events.add(new MergeWatchEvent<>(StandardWatchEventKinds.OVERFLOW, overflow.getAndSet(0), null));

        return Collections.unmodifiableList(events);

    }


    @Override
    public boolean reset() {

        if(isValid() && signalled.compareAndSet(true, false)) {
            if(!eventQueue.isEmpty())
                signal();
        }

        return isValid();

    }


    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Watchable watchable() {
        return watchable;
    }


}
