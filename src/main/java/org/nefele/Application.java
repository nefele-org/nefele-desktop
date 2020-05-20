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

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.nefele.cloud.DriveService;
import org.nefele.core.Mimes;
import org.nefele.core.Status;
import org.nefele.core.TransferQueue;
import org.nefele.ui.Theme;
import org.nefele.ui.Views;
import org.nefele.ui.controls.NefelePane;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.scenes.Home;
import org.nefele.ui.scenes.SplashScreen;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;


public final class Application extends javafx.application.Application implements AutoCloseable {

    public static Application instance = null;

    private final Database database;
    private final Config config;
    private final Locale locale;
    private final Status status;
    private final AtomicBoolean running;
    private final TransferQueue transferQueue;
    private final ArrayList<Service> services;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Views views;
    private final ObjectProperty<Theme> theme;

    private ScheduledFuture<?> serviceWorker = null;




    public Application() {

        instance = this;

        running = new AtomicBoolean(true);
        services = new ArrayList<>();

        database = new Database();
        config = new Config(database);
        locale = new Locale();
        status = new Status();
        transferQueue = new TransferQueue();

        views = new Views();
        theme = new SimpleObjectProperty<>(null);

        executorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newScheduledThreadPool(16);

    }



    @Override
    public void start(Stage stage) throws Exception {

        Application.log(Application.class, "Starting application");
        Application.log(Application.class, "Java: %s %s", System.getProperty("java.vendor"), System.getProperty("java.version"));
        Application.log(Application.class, "JavaFX: %s", System.getProperty("javafx.runtime.version"));
        Application.log(Application.class, "System: %s", System.getProperty("os.name"));

        Application.log(Application.class, "Heap: %d/%d MB",
                Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024);



        Resources.getFont(this, "/font/segoeui.ttf");
        Resources.getFont(this, "/font/segoeuib.ttf");
        Resources.getFont(this, "/font/segoeuii.ttf");


        addService(getLocale());
        addService(DriveService.getInstance());
        addService(Mimes.getInstance());




        Application.log(getClass(), "Initialize interface");
        Platform.setImplicitExit(false);


        stage.setScene(new Scene(new SplashScreen()));
        stage.getIcons().add(new Image(Resources.getURL(this, "/images/trayicon.png").toExternalForm()));
        stage.setWidth(400);
        stage.setHeight(250);
        stage.setTitle("Nefele");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.show();



        runThread(new Thread(() -> {


            Application.log(getClass(), "Initialize theme");

            setTheme(new Theme(getConfig().getString("app.ui.theme").orElse(Theme.DEFAULT_THEME)));



            Application.log(getClass(), "Initialize services");

            double max = services.size();
            double cur = 0.0;

            for(Service i : services) {

                cur += 1.0;
                getStatus().setLoadingProgress(cur / max);

                Application.log(getClass(), "Loading %s", i.getClass().getName());
                i.initialize(this);

            }


            serviceWorker = runWorker(new Thread(() -> {
                services.forEach(i -> {
                    i.synchronize(Application.this);
                });
            }, "Service::synchronize()"), 10, 10, TimeUnit.SECONDS);



            Platform.runLater(() -> {

                stage.setScene(new Scene(new NefelePane(new Home())));
                stage.setAlwaysOnTop(false);
                stage.setMinWidth(600);
                stage.setMinHeight(400);
                stage.setWidth(800);
                stage.setHeight(480);

            });

        }, "Application::Loading"));


    }

    @Override
    public void stop() throws Exception {

        Application.log(Application.class, "Preparing to exit in a friendly way...");


        if(serviceWorker != null)
            serviceWorker.cancel(true);

        for(Service i : services) {
            Application.log(getClass(), "Unloading %s", i.getClass().getName());
            i.exit(this);
        }

        running.set(false);


        try {

            Application.log(Application.class, "Doing a genocide");

            scheduledExecutorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.SECONDS);

            Application.log(Application.class, "Scared threads are fleeing");

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.SECONDS);


        } catch (InterruptedException e) {
            Application.panic(Application.class, e);
        }


        Application.log(Application.class, "Goodbye!");

        super.stop();
        System.exit(0);

    }

    @Override
    public void close() throws Exception {
        stop();
    }

    public void exit() {

        try {
            close();
        } catch (Exception e) {
            Application.panic(getClass(), e);
        }

    }


    public void runThread(Thread thread) {

        requireNonNull(thread);

        executorService.execute(thread);
        Application.log(Application.class, "Submit new Thread #%d (%s)", thread.getId(), thread.getName());

    }

    public ScheduledFuture<?> runWorker(Thread thread, int delay, int interval, TimeUnit unit) {

        requireNonNull(thread);

        Application.log(Application.class, "Submit new Worker #%d (%s)", thread.getId(), thread.getName());
        return scheduledExecutorService.scheduleAtFixedRate(thread, delay, interval, unit);

    }

    public <T extends Service> void addService(T e) {
        Application.log(getClass(), "Added service %s", e.getClass().getName());
        services.add(e);
    }


    public boolean isRunning() {
        return running.get();
    }

    public Views getViews() {
        return views;
    }

    public Database getDatabase() {
        return database;
    }

    public Config getConfig() {
        return config;
    }

    public Locale getLocale() {
        return locale;
    }

    public Status getStatus() {
        return status;
    }

    public TransferQueue getTransferQueue() {
        return transferQueue;
    }

    public Theme getTheme() {
        return theme.get();
    }

    public void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }


    public static Application getInstance() {
        return requireNonNull(instance);
    }

    public static void panic(Class<?> className, String message, Object... args) {

        Application.getInstance().runThread(new Thread(() -> {

            try {

                Player player = new Player(Resources.getStream(Application.getInstance(), "/audio/panic.mp3"));
                player.play();

            } catch (JavaLayerException ignored) { }

        }, "Put your hands up!"));



        log(className, "PANIC! " + message, args);

        Dialogs.showErrorBox(
                "DIALOG_PANIC_TITLE",
                "DIALOG_PANIC_MESSAGE"
        );


        System.exit(1);
    }

    public static void panic(Class<?> className, Exception e) {

        StringBuilder ss = new StringBuilder().append (
                        String.format("Exception in thread \"%s\" %s: %s\n",
                                Thread.currentThread().getName(),
                                e.getClass().getName(),
                                e.getMessage())
                    );


        for (StackTraceElement i : e.getStackTrace())
            ss.append(String.format("   at %s/%s.%s(%s:%d)\n", i.getModuleName(), i.getClassName(), i.getMethodName(), i.getFileName(), i.getLineNumber()));


        Application.panic(className, ss.toString());

    }

    public static void log(Class<?> className, String message, Object... args) {
        System.err.println(String.format("[%s] (%s) %s", Date.from(Instant.now()).toString(), className.getName(), String.format(message, args)));
    }


    public static void garbageCollect() {

        long m0 = Runtime.getRuntime().totalMemory();
        Runtime.getRuntime().gc();
        long m1 = Runtime.getRuntime().totalMemory();

        Application.log(Application.class, "Garbage collected, %d MB freed", (m0 - m1) / 1024 / 1024);

    }

}
