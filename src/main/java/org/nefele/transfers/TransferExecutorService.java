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

package org.nefele.transfers;

import org.nefele.Application;
import org.nefele.Service;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.*;

public class TransferExecutorService extends ThreadPoolExecutor implements Service {


    private int maximumThreadActiveCount = 4;
    private ArrayDeque<RunnableFuture<?>> pendingTask = new ArrayDeque<>();
    private ArrayDeque<RunnableFuture<?>> runningTask = new ArrayDeque<>();


    public TransferExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

        Application.getInstance().addService(this);

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

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);


        if(r instanceof Future<?> && Objects.isNull(t)) {

            Future<?> future = (Future<?>) r;

            if(future.isDone()) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    t = e.getCause();
                } catch (CancellationException e) {
                    t = e;
                }
            }

        }

        if(t != null) {
            Application.log(getClass(), (Exception) t, "TransferExecutorService::afterExecute()");
        }
    }

    public void setMaximumThreadActiveCount(int maximumThreadActiveCount) {
        this.maximumThreadActiveCount = maximumThreadActiveCount;
    }

    public int getCurrentThreadActiveCount() {
        return runningTask.size();
    }


    private void startThread(RunnableFuture<?> runnableFuture) {

        runningTask.add(runnableFuture);
        execute(runnableFuture);

    }

    private void updatePool() {

        runningTask.removeIf(RunnableFuture::isDone);

        while(!pendingTask.isEmpty() && (runningTask.size() < maximumThreadActiveCount))
            startThread(pendingTask.pop());

    }

    @Override
    public void initialize() {

        Application.getInstance().runWorker(new Thread(
                this::updatePool, "TransferExecutorService::updatePool()"), 0, 250, TimeUnit.MILLISECONDS);

    }

    @Override
    public void synchronize() {
        updatePool();
    }

    @Override
    public void exit() {
        shutdown();
    }


}
