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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.nefele.Application;
import org.nefele.ApplicationService;
import org.nefele.ApplicationTask;

import java.nio.file.Files;
import java.util.concurrent.*;

public class TransferQueue implements ApplicationService {

    public final static int TRANSFER_QUEUE_INTERVAL = 1000;

    private final ObservableList<Pair<TransferInfo, Future<Integer>>> transferQueue;
    private final TransferExecutorService executorService;


    public TransferQueue() {

        this.transferQueue = FXCollections.observableArrayList();
        this.executorService = new TransferExecutorService(0, 2147483647, 365L, TimeUnit.DAYS, new SynchronousQueue<>());

        Application.getInstance().getServiceManager()
                .register(this, "TransferQueue", true, TRANSFER_QUEUE_INTERVAL, TRANSFER_QUEUE_INTERVAL, TimeUnit.MILLISECONDS);

        Application.getInstance().getServiceManager()
                .register(executorService, "TransferExecutorQueue", true, 500, 500, TimeUnit.MILLISECONDS);

    }



    public Future<Integer> enqueue(TransferInfo i) {

        Future<Integer> future;

        synchronized (transferQueue) {
            transferQueue.add(new Pair<>(i, (future = executorService.submit(i::execute))));
        }

        return future;

    }




    public int getCurrentTransfers() {
        return executorService.getCurrentThreadActiveCount();
    }

    public ObservableList<Pair<TransferInfo, Future<Integer>>> getTransferQueue() {
        return transferQueue;
    }



    @Override
    public void initialize() {

        int parallelMax = Application.getInstance().getConfig()
                .getInteger("core.transfers.parallel")
                .orElse(4);

        Application.log(this.getClass(), "Parallels transfer set to " + parallelMax);
        Application.log(this.getClass(), "Fixed-Rate set to " + TRANSFER_QUEUE_INTERVAL);

        this.executorService.setMaximumThreadActiveCount(parallelMax);

    }

    @Override
    public void update(ApplicationTask currentTask) {

        synchronized (transferQueue) {

            for (Pair<TransferInfo, Future<Integer>> i : transferQueue) {

                switch (i.getKey().getStatus()) {

                    case TransferInfo.TRANSFER_STATUS_COMPLETED:
                        //case TransferInfo.TRANSFER_STATUS_CANCELED:
                        //case TransferInfo.TRANSFER_STATUS_ERROR:

                        if (!i.getValue().isCancelled() || !i.getValue().isDone())
                            i.getValue().cancel(true);

                        break;

                    case TransferInfo.TRANSFER_STATUS_READY:
                    case TransferInfo.TRANSFER_STATUS_RUNNING:
                    case TransferInfo.TRANSFER_STATUS_RESUME:
                    case TransferInfo.TRANSFER_STATUS_PAUSED:
                        break;


                }

                Platform.runLater(() -> i.getKey().updateSpeed());

            }


            Platform.runLater(() ->
                    transferQueue.removeIf((j) -> j.getValue().isDone())
            );

        }

    }

    @Override
    public void close() {

    }

}
