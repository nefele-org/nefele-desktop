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

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.TimeUnit;

public class ApplicationFuture extends Service<Void> {




    private final ApplicationService module;
    private final long period;
    private final long delay;
    private final String serviceName;


    public ApplicationFuture(ApplicationService module, String serviceName, long delay, long period, TimeUnit timeUnit) {
        this.module = module;
        this.period = timeUnit.toMillis(delay);
        this.delay = timeUnit.toMillis(period);
        this.serviceName = serviceName;
    }



    public void initialize() {
        Application.log(getClass(), "Loading service %s", serviceName);
        module.initialize();
    }


    @Override
    protected Task<Void> createTask() {
        return new ApplicationTask() {

            {
                setOnCancelled(i -> {
                    Application.log(getClass(), "Unloading service %s", serviceName);
                    module.close();
                });
            }

            @Override
            protected Void call() throws Exception {

                Application.log(getClass(), "Running service %s", serviceName);


                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {

                    if(!isCancelled())
                        throw e;

                }


                do {

                    module.update(this);

                    try {
                        Thread.sleep(period);
                    } catch (InterruptedException e) {

                        if(!isCancelled())
                            throw e;

                        break;

                    }

                } while (Application.getInstance().isRunning() && !isCancelled());


                return null;

            }

        };
    }

    public long getPeriod() {
        return period;
    }

    public long getDelay() {
        return delay;
    }

    public String getServiceName() {
        return serviceName;
    }
}
