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

package org.nefele.core;

import org.nefele.Application;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class TransferExecutorService extends ThreadPoolExecutor {


    protected int maximumThreadActiveCount = 4;
    protected ArrayDeque<RunnableFuture<?>> pendingTask = new ArrayDeque<>();
    protected ArrayDeque<RunnableFuture<?>> runningTask = new ArrayDeque<>();


    public TransferExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

        Application.addOnInitHandler(this::initalize);
        Application.addOnExitHandler(this::exit);

    }

    public void initalize() {

        Application.getInstance().runWorker(new Thread(
                this::updatePool, "TransferExecutorService::updatePool()"), 0, 250, TimeUnit.MILLISECONDS);

    }

    public void exit() {
        shutdown();
    }



    @Override
    public <T> Future<T> submit(Callable<T> task) {

        if(task == null)
            throw new NullPointerException();


        RunnableFuture<T> runnableFuture = this.newTaskFor(task);

        if(runningTask.size() < maximumThreadActiveCount) {
            startThread(runnableFuture);
        } else
            pendingTask.add(runnableFuture);

        return runnableFuture;
    }


    public void setMaximumThreadActiveCount(int maximumThreadActiveCount) {
        this.maximumThreadActiveCount = maximumThreadActiveCount;
    }

    public int getCurrentThreadActiveCount() {
        return runningTask.size();
    }


    protected void startThread(RunnableFuture<?> runnableFuture) {

        runningTask.add(runnableFuture);
        execute(runnableFuture);

    }

    protected void updatePool() {

        runningTask.removeIf(RunnableFuture::isDone);

        while(!pendingTask.isEmpty() && (runningTask.size() < maximumThreadActiveCount))
            startThread(pendingTask.pop());

    }
}
