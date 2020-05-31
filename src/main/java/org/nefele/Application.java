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
import org.nefele.cloud.DriveProviders;
import org.nefele.cloud.SharedFolders;
import org.nefele.cloud.TransferQueue;
import org.nefele.ui.SplashScreen;
import org.nefele.ui.controls.NefelePane;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.scenes.Home;
import org.nefele.utils.CryptoUtils;
import org.nefele.utils.PlatformUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
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
    private final ApplicationServiceManager serviceManager;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Views views;
    private final ObjectProperty<Theme> theme;
    private final Path dataPath;
    private final ApplicationLogger applicationLogger;

    private Stage primaryStage;




    public Application() {

        instance = this;

        dataPath = Paths.get(System.getProperty("user.home"), ".nefele");
        running = new AtomicBoolean(true);

        applicationLogger = new ApplicationLogger();
        serviceManager = new ApplicationServiceManager();
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

        primaryStage = stage;

        Application.log(Application.class, "Starting application");
        Application.log(Application.class, "Java: %s %s", System.getProperty("java.vendor"), System.getProperty("java.version"));
        Application.log(Application.class, "JavaFX: %s", System.getProperty("javafx.runtime.version"));
        Application.log(Application.class, "System: %s", System.getProperty("os.name"));


        Application.log(Application.class, "Heap: %d/%d MB",
                Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024);



        Resources.getFont(this, "/font/segoeui.ttf");
        Resources.getFont(this, "/font/segoeuib.ttf");
        Resources.getFont(this, "/font/segoeuii.ttf");


        CryptoUtils.initialize();

        getServiceManager().register(getConfig(), "Config",
                true, 30, 30, TimeUnit.SECONDS);

        getServiceManager().register(getLocale(), "Locale",
                true, 30, 30, TimeUnit.SECONDS);

        getServiceManager().register(Mimes.getInstance(), "Mimes",
                true, 365, 365, TimeUnit.DAYS);

        getServiceManager().register(DriveProviders.getInstance(), "DriveProviders",
                true, 10, 10, TimeUnit.SECONDS);

        getServiceManager().register(SharedFolders.getInstance(), "SharedFolders",
                true, 3, 3, TimeUnit.SECONDS);




        Application.log(getClass(), "Initialize interface");
        Platform.setImplicitExit(false);


        stage.setScene(new Scene(new SplashScreen()));
        stage.getIcons().add(new Image(Resources.getURL(this, "/images/trayicon.png").toExternalForm()));
        stage.setWidth(400);
        stage.setHeight(250);
        stage.setTitle("Nefele");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.centerOnScreen();
        stage.setAlwaysOnTop(false);
        stage.show();



        runThread(new Thread(() -> {


            Application.log(getClass(), "Initialize theme");

            setTheme(new Theme(getConfig()
                    .getString("app.ui.theme")
                    .orElse(Theme.DEFAULT_THEME)
            ));



            Application.log(getClass(), "Initialize services");

            serviceManager
                    .boot(status::setLoadingProgress);




            PlatformUtils.runLaterAndWait(() -> {

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

        if(getPrimaryStage() != null)
            getPrimaryStage().hide();


        new Thread(() -> {

            Application.log(Application.class, "Preparing to exit in a friendly way...");
            running.set(false);


            PlatformUtils
                    .runLaterAndWait(serviceManager::shutdown);


            try {

                Thread.sleep(5000); // FIXME: Find a better way to wait service manager


                Application.log(Application.class, "Doing a genocide");

                scheduledExecutorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.SECONDS);

                Application.log(Application.class, "Scared threads are fleeing");

                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.SECONDS);

            } catch (InterruptedException ignored ) { }


            Application.log(Application.class, "Goodbye!");

            getApplicationLogger().close();
            System.exit(0);

        }, "Application::BackgroundUnloading").start();


        super.stop();
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



    public ApplicationServiceManager getServiceManager() {
        return serviceManager;
    }

    public boolean isRunning() {
        return running.get();
    }

    public Path getDataPath() {
        return dataPath;
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

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ApplicationLogger getApplicationLogger() {
        return applicationLogger;
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

        log(className, "PANIC! " + message, args);


        Application.getInstance().runThread(new Thread(() -> {

            try {

                Player player = new Player(Resources.getStream(Application.getInstance(), "/audio/panic.mp3"));
                player.play();

            } catch (JavaLayerException ignored) { }

        }, "Put your hands up!"));



        PlatformUtils.runLaterAndWait(() ->
            Dialogs.showErrorBox(
                    "DIALOG_PANIC_TITLE",
                    "DIALOG_PANIC_MESSAGE")
        );


        System.exit(1);

    }

    public static void panic(Class<?> className, Exception e) {

        Application.log(className, e);
        Application.panic(className, "Aborting...");

    }


    public static void log(Class<?> className, Exception e, String message, Object...args) {

        StringBuilder ss = new StringBuilder().append (
                String.format("!!! Exception in thread \"%s\" %s: %s: %s\n",
                        Thread.currentThread().getName(),
                        e.getClass().getName(),
                        String.format(message, args),
                        e.getMessage())
        );


        for (StackTraceElement i : e.getStackTrace())
            ss.append(String.format("   at %s/%s.%s(%s:%d)\n", i.getModuleName(), i.getClassName(), i.getMethodName(), i.getFileName(), i.getLineNumber()));

        Application.log(className, ss.toString());

    }

    public static void log(Class<?> className, Exception e) {
        Application.log(className, e, "");
    }

    public static void log(Class<?> className, String message, Object... args) {

        var logMessage = String.format(
            "[%s] (%s) %s", Date.from(Instant.now()).toString(), className.getName(), String.format(message, args)
        );


        if(Application.getInstance() != null)
            if(Application.getInstance().getApplicationLogger() != null)
                Application.getInstance().getApplicationLogger().writeLog(logMessage);

        System.err.println(logMessage);

    }


    public static void garbageCollect() {

        long m0 = Runtime.getRuntime().totalMemory();
        Runtime.getRuntime().gc();
        long m1 = Runtime.getRuntime().totalMemory();

        Application.log(Application.class, "Garbage collected, %d MB freed", (m0 - m1) / 1024 / 1024);

    }



}
