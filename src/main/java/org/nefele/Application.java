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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.apache.http.impl.cookie.AbstractCookieAttributeHandler;
import org.nefele.cloud.Drive;
import org.nefele.core.Status;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergeFileSystemProvider;
import org.nefele.fs.MergeFileTree;
import org.nefele.ui.Theme;
import org.nefele.ui.Views;
import org.nefele.ui.controls.NefelePane;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.scenes.Home;

import java.awt.*;
import java.net.URI;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;


public final class Application extends javafx.application.Application implements AutoCloseable {

    public static Application instance = null;


    private final Database database;
    private final Config config;
    private final Locale locale;
    private final Status status;
    private final AtomicBoolean running;
    private final ArrayList<Drive> drives;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Views views;

    private final ObjectProperty<Theme> theme;


    public Application() {

        running = new AtomicBoolean(true);

        database = new Database();
        config = new Config(database);
        locale = new Locale();
        status = new Status();
        drives = new ArrayList<>();
        views = new Views();
        theme = new SimpleObjectProperty<>(null);
        executorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newScheduledThreadPool(16);

    }



    @Override
    public void start(Stage stage) throws Exception {

        instance = this;

        Application.log(Application.class, "Starting application");
        Application.log(Application.class, "Java: %s %s", System.getProperty("java.vendor"), System.getProperty("java.version"));
        Application.log(Application.class, "JavaFX: %s", System.getProperty("javafx.runtime.version"));
        Application.log(Application.class, "System: %s", System.getProperty("os.name"));

        Application.log(Application.class, "Heap: %d/%d MB",
                Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024);



        Resources.getFont(this, "/font/segoeui.ttf");
        Resources.getFont(this, "/font/segoeuib.ttf");
        Resources.getFont(this, "/font/segoeuii.ttf");


        setTheme(new Theme(config.getString("app.ui.theme").orElse(Theme.DEFAULT_THEME)));
        locale.setLanguage(config.getString("app.ui.locale").orElse(Locale.DEFAULT_LOCALE));


        try {

            final ArrayList<Integer> ids = new ArrayList<>();

            database.query("SELECT id FROM drives",
                    null, r -> ids.add(r.getInt(1)));

            for(int id : ids)
                drives.add(Drive.fromId(id));


        } catch (SQLException e) {
            Application.panic(getClass(), e);
        }

        //transferQueue = new TransferQueue(config.getInteger("core.transfers.parallel").orElse(4));

        MergeFileSystemProvider provider = new MergeFileSystemProvider();

        FileSystem fileSystem = FileSystems.getFileSystem(URI.create("cloud:///"));

        Path path = fileSystem.getPath("/");

        Files.list(path).filter(Files::isDirectory).forEach(System.out::println);


        System.out.println("path.toString() = " + path.toString());
        System.out.println("path.isAbsolute() = " + path.isAbsolute());
        System.out.println("path.getFileName().toString() = " + path.getFileName().toString());
        System.out.println("path.toUri() = " + path.toUri());
        System.out.println("path.getRoot().toString() = " + path.getRoot().toString());


        Platform.setImplicitExit(false);

        stage.setScene(new Scene(new NefelePane(new Home())));
        stage.getIcons().add(new Image(Resources.getURL(this, "/images/trayicon.png").toExternalForm()));
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setWidth(1280);
        stage.setWidth(720);
        stage.setTitle("Nefele");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();


    }

    @Override
    public void stop() throws Exception {

        Application.log(Application.class, "Preparing to exit...");

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

    public void runWorker(Thread thread, int delay, int interval, TimeUnit unit) {

        requireNonNull(thread);

        scheduledExecutorService.scheduleAtFixedRate(thread, delay, interval, unit);
        Application.log(Application.class, "Submit new Worker #%d (%s)", thread.getId(), thread.getName());

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

//    public TransferQueue getTransferQueue() {
//        return transferQueue;
//    }

    public ArrayList<Drive> getDrives() {
        return drives;
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
        System.err.println(String.format("[%s] ", className.getName()) + String.format(message, args));
    }


    public static void garbageCollect() {

        long m0 = Runtime.getRuntime().totalMemory();
        Runtime.getRuntime().gc();
        long m1 = Runtime.getRuntime().totalMemory();

        Application.log(Application.class, "Garbage collected, %d MB freed", (m0 - m1) / 1024 / 1024);

    }

}
