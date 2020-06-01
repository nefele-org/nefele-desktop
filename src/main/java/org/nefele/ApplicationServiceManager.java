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

package org.nefele;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ApplicationServiceManager {

    private final ExecutorService executorService;
    private final AtomicBoolean booted;
    private final ArrayList<ApplicationFuture> registeredModules;

    public ApplicationServiceManager() {
        this.executorService = Executors.newCachedThreadPool();
        this.booted = new AtomicBoolean(false);
        this.registeredModules = new ArrayList<>();
    }


    public ApplicationFuture register(ApplicationService service, String serviceName, boolean restartable, int delay, int period, TimeUnit unit) {

        ApplicationFuture applicationFuture;

        registeredModules.add((applicationFuture = new ApplicationFuture(service, serviceName, delay, period, unit) {{
            setExecutor(executorService);

            setOnFailed((i) -> {
                if(restartable)
                    restart();
            });


        }}));


        if(booted.get()) {
            applicationFuture.initialize();
            applicationFuture.start();
        }

        return applicationFuture;

    }


    public void unregister(ApplicationFuture future) {
        future.cancel();
        registeredModules.remove(future);
    }

    public void boot(Consumer<Double> onLoadingProgress) {

        booted.set(true);


        var max = (double) registeredModules.size();
        var cur = 0.0;


        for(var i : registeredModules) {

            cur += 1.0;
            onLoadingProgress.accept(cur / max);

            try {
                i.initialize();
            } catch (Exception e) {
                Application.log(getClass(), e, "Failed to initialize: " + i.getServiceName());
            }

        }

        registeredModules.forEach(ApplicationFuture::start);

    }

    public void shutdown() {

        registeredModules
                .forEach(ApplicationFuture::close);

        executorService.shutdown();
        booted.set(false);

    }


}
