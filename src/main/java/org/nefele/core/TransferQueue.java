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

import javafx.util.Pair;
import org.nefele.Application;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class TransferQueue {

    public final static int TRANSFER_QUEUE_INTERVAL = 100;

    private final ArrayList<Pair<TransferInfo, Future<Integer>>> transferQueue;
    private final TransferExecutorService executorService;
    private TransferInfoListener newTransferListener;
    private TransferInfoListener updateTransferListener;


    public TransferQueue(int parallelMax) {

        Application.log(this.getClass(), "Parallels transfer set to " + parallelMax);
        Application.log(this.getClass(), "Fixed-Rate set to " + TRANSFER_QUEUE_INTERVAL);


        this.transferQueue = new ArrayList<>();
        this.executorService = new TransferExecutorService(0, 2147483647, 365L, TimeUnit.DAYS, new SynchronousQueue<Runnable>());
        this.executorService.setMaximumThreadActiveCount(parallelMax);

        Timer timerUpdate = new Timer("Transfer Queue", true);

        timerUpdate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTransferQueue();
            }
        }, 0, TRANSFER_QUEUE_INTERVAL);

    }

    public void addNewTransferListener(TransferInfoListener e) {
        this.newTransferListener = e;
    }

    public void addUpdateTransferListener(TransferInfoListener e) {
        this.updateTransferListener = e;
    }


    public Future<Integer> enqueue(TransferInfo i) {

        Future<Integer> future;

        if (!i.isParallel()) {
            future = executorService.submit(i::execute);
        }
        else {

            synchronized (transferQueue) {
                transferQueue.add(new Pair<>(i, (future = executorService.submit(i::execute))));
            }

            if (newTransferListener != null)
                newTransferListener.run(i);

        }

        return future;

    }


    private void updateTransferQueue() {

        synchronized (transferQueue) {

            for (Pair<TransferInfo, Future<Integer>> i : transferQueue) {

                switch (i.getKey().getStatus()) {

                    case TransferInfo.TRANSFER_STATUS_COMPLETED:
                    case TransferInfo.TRANSFER_STATUS_CANCELED:
                    case TransferInfo.TRANSFER_STATUS_ERROR:

                        if (!i.getValue().isCancelled() || !i.getValue().isDone())
                            i.getValue().cancel(true);

                        break;

                    case TransferInfo.TRANSFER_STATUS_READY:
                    case TransferInfo.TRANSFER_STATUS_RUNNING:
                    case TransferInfo.TRANSFER_STATUS_RESUME:
                    case TransferInfo.TRANSFER_STATUS_PAUSED:
                        break;


                }

                i.getKey().updateSpeed();

                if (updateTransferListener != null)
                    updateTransferListener.run(i.getKey());

            }


            transferQueue.removeIf((j) ->
                            (j.getKey().getStatus() == TransferInfo.TRANSFER_STATUS_COMPLETED) ||
                            (j.getKey().getStatus() == TransferInfo.TRANSFER_STATUS_CANCELED)  ||
                            (j.getKey().getStatus() == TransferInfo.TRANSFER_STATUS_ERROR));

        }

    }

    public int getCurrentTransfers() {
        return executorService.getCurrentThreadActiveCount();
    }

}
