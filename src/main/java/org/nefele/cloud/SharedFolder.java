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

import org.nefele.Application;
import org.nefele.ApplicationService;
import org.nefele.ApplicationTask;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergePath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Predicate;



public final class SharedFolder implements ApplicationService {

    private final static int HOST_LOCAL = 0;
    private final static int HOST_CLOUD = 1;


    class SharedEntry {

        private final int host;
        private final Path source;
        private final Path target;

        public SharedEntry(int host, Path source) {

            this.host = host;
            this.source = source;

            if(host == HOST_LOCAL)
                target = getLocalPath().resolve(getCloudPath().relativize(source).toString());
            else
                target = getCloudPath().resolve(getLocalPath().relativize(source).toString());

        }

        public int getHost() {
            return host;
        }

        public Path getSource() {
            return source;
        }

        public Path getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SharedEntry that = (SharedEntry) o;
            return getHost() == that.getHost() &&
                    getSource().equals(that.getSource());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHost(), getSource());
        }

        @Override
        public String toString() {
            return String.format("<%s:%s>", source, target);
        }
    }


    private final Path localPath;
    private final MergePath cloudPath;
    private final HashSet<SharedEntry> createQueue;
    private final HashSet<SharedEntry> deleteQueue;
    private final HashSet<SharedEntry> updateQueue;
    private final HashSet<Path> ignoreQueue;

    private final HashSet<WatchKey> watchKeys;
    private final WatchService localWatchService;
    private final WatchService cloudWatchService;



    public SharedFolder(Path localPath, MergePath cloudPath) {

        this.localPath = localPath;
        this.cloudPath = cloudPath;

        this.createQueue = new HashSet<>();
        this.deleteQueue = new HashSet<>();
        this.updateQueue = new HashSet<>();
        this.ignoreQueue = new HashSet<>();

        this.watchKeys = new HashSet<>();


        try {

            this.localWatchService = FileSystems.getDefault().newWatchService();
            this.cloudWatchService = FileSystems.getFileSystem(URI.create("nefele:///")).newWatchService();

        } catch (IOException e) {
            Application.panic(getClass(), e);
            throw new IllegalStateException();
        }

    }



    @Override
    public void initialize() {

        registerWatchable(this.localPath, this.cloudPath);

        Application.getInstance().runThread(new Thread(() -> watchServiceWorker(localWatchService), "SharedFolder::watchService::" + this.localPath));
        Application.getInstance().runThread(new Thread(() -> watchServiceWorker(cloudWatchService), "SharedFolder::watchService::" + this.cloudPath));

    }


    @Override @SuppressWarnings("unchecked")
    public void update(ApplicationTask currentTask) {

        final var currentCreateQueue = (HashSet<SharedEntry>) createQueue.clone();
        final var currentDeleteQueue = (HashSet<SharedEntry>) deleteQueue.clone();
        final var currentUpdateQueue = (HashSet<SharedEntry>) updateQueue.clone();

        synchronized(createQueue) { createQueue.clear(); }
        synchronized(deleteQueue) { deleteQueue.clear(); }
        synchronized(updateQueue) { updateQueue.clear(); }


        currentCreateQueue
                .forEach(q -> {

                    if(currentDeleteQueue.stream().anyMatch(i -> i.getSource().equals(q.getSource())))
                        return;


                    ignoreQueue.add(q.getTarget());

                    try {

                        if(Files.notExists(q.getTarget())) {

                            if (Files.isDirectory(q.getSource())) {

                                Files.createDirectory(q.getTarget());


                                if(q.getSource() instanceof MergePath)
                                    registerWatchable(q.getTarget(), q.getSource());
                                else
                                    registerWatchable(q.getSource(), q.getTarget());


                            } else
                                currentUpdateQueue.add(q);

                        }


                    } catch (Exception e) {
                        Application.log(getClass(), e, "SharedFolder::createQueue::%s", q);
                        createQueue.add(q);
                    }

                    ignoreQueue.remove(q.getTarget());

                });



        currentUpdateQueue
                .forEach(q -> {

                    try {

                        if (currentDeleteQueue.stream().anyMatch(i -> i.getSource().equals(q.getSource())))
                            return;

                        if (Files.isDirectory(q.getSource()))
                            return;


                        ignoreQueue.add(q.getTarget());


                        if(Files.exists(q.getTarget()))
                            Files.delete(q.getTarget());



                        final Future<Integer> future;

                        if (q.getHost() == HOST_CLOUD)
                            future = Application.getInstance().getTransferQueue().enqueue(
                                        new UploadTransferInfo((MergePath) q.getTarget(), q.getSource().toFile()));
                        else
                            future = Application.getInstance().getTransferQueue().enqueue(
                                        new DownloadTransferInfo((MergePath) q.getSource(), q.getTarget().toFile()));


                        Application.getInstance().runThread(new Thread(() -> {

                            try {

                                var status = future.get();

                                switch (status) {

                                    case TransferInfo.TRANSFER_STATUS_COMPLETED:
                                        break;

                                    case TransferInfo.TRANSFER_STATUS_ERROR:
                                    case TransferInfo.TRANSFER_STATUS_CANCELED:

                                        if (Files.exists(q.getTarget()))
                                            Files.delete(q.getTarget());

                                        updateQueue.add(q);
                                        break;

                                    default:
                                        Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                        break;

                                }


                            } catch (Exception e) {
                                Application.log(getClass(), e, "SharedFolder::updateQueueFuture::%s", q);
                            }


                            ignoreQueue.remove(q.getTarget());

                        }, "SharedFolder::TransferQueue::" + q));


                    } catch (Exception e) {
                        Application.log(getClass(), e, "SharedFolder::updateQueue::%s", q);
                        updateQueue.add(q);
                    }

                });


        currentDeleteQueue
                .forEach(q -> {

                    ignoreQueue.add(q.getTarget());

                    try {

                        if(Files.exists(q.getTarget()))
                            Files.delete(q.getTarget());

                    } catch (Exception e) {
                        Application.log(getClass(), e, "SharedFolder::deleteQueue::%s", q);
                        deleteQueue.add(q);
                    }

                    ignoreQueue.remove(q.getTarget());

                });




    }



    public void watchServiceWorker(WatchService watchService) {

        do {

            try {


                WatchKey watchKey = watchService.take();


                if(!watchKey.isValid())
                    continue;


                if (watchKeys.contains(watchKey)) {

                    watchKey.pollEvents().forEach(e -> {

                        try {

                            Application.log(getClass(), "Received WatchKey %s in %s %s from %s", e.kind(), e.context(), this, watchService);


                            Path source = ((Path) watchKey.watchable()).resolve(e.context().toString());

                            if(ignoreQueue.contains(source))
                                return;



                            Path relativeSource;

                            if(source instanceof MergePath)
                                relativeSource = cloudPath.relativize(source);
                            else
                                relativeSource = localPath.relativize(source);



                            if (e.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {

                                Application.log(getClass(), "Created entry %s in %s", relativeSource, this);


                                synchronized (createQueue) {

                                    if(source instanceof MergePath)
                                        createQueue.add(new SharedEntry(HOST_LOCAL, source));
                                    else
                                        createQueue.add(new SharedEntry(HOST_CLOUD, source));

                                }


                            } else if (e.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {

                                Application.log(getClass(), "Deleted entry %s in %s", relativeSource, this);

                                synchronized (deleteQueue) {

                                    if(source instanceof MergePath)
                                        deleteQueue.add(new SharedEntry(HOST_LOCAL, source));
                                    else
                                        deleteQueue.add(new SharedEntry(HOST_CLOUD, source));

                                }


                            } else if (e.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {

                                Application.log(getClass(), "Updated entry %s in %s", relativeSource, this);

                                if(source instanceof MergePath)
                                    updateQueue.add(new SharedEntry(HOST_LOCAL, source));
                                else
                                    updateQueue.add(new SharedEntry(HOST_CLOUD, source));

                            }


                        } catch (Exception eV) {
                            Application.log(getClass(), eV, "watchKey.pollEvents() of SharedFolder %s", this);
                        }

                    });

                }


                watchKey.reset();

            } catch (InterruptedException | ClosedWatchServiceException ignored) {
                break;
            }

        } while (Application.getInstance().isRunning());

    }


    public void registerWatchable(Path localPath, Path cloudPath) {

        try {

            final ArrayList<String> localFiles = new ArrayList<>();
            final ArrayList<String> cloudFiles = new ArrayList<>();

            Files.list(localPath)
                    .map(localPath::relativize)
                    .map(Path::toString)
                    .forEach(localFiles::add);

            Files.list(cloudPath)
                    .map(cloudPath::relativize)
                    .map(Path::toString)
                    .forEach(cloudFiles::add);

            localFiles
                    .stream()
                    .map(name -> name.replace(File.pathSeparator, MergeFileSystem.PATH_SEPARATOR))
                    .filter(Predicate.not(cloudFiles::contains))
                    .map(name -> name.replace(MergeFileSystem.PATH_SEPARATOR, File.pathSeparator))
                    .forEach(i -> createQueue.add(new SharedEntry(HOST_CLOUD, localPath.resolve(i))));

            cloudFiles
                    .stream()
                    .map(name -> name.replace(MergeFileSystem.PATH_SEPARATOR, File.pathSeparator))
                    .filter(Predicate.not(localFiles::contains))
                    .map(name -> name.replace(File.pathSeparator, MergeFileSystem.PATH_SEPARATOR))
                    .forEach(i -> createQueue.add(new SharedEntry(HOST_LOCAL, cloudPath.resolve(i))));





            final WatchEvent.Kind<?>[] standardWatchEventsKind = new WatchEvent.Kind[] {
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
            };


            Files.walk(localPath)
                    .filter(Files::isDirectory)
                    .forEach(path -> {

                        try {

                            watchKeys.add(path.register(localWatchService,
                                    standardWatchEventsKind
                            ));

                        } catch (IOException e) {
                            Application.log(getClass(), e, "watchKeys.add()");
                        }

                    });


            Files.walk(cloudPath)
                    .filter(Files::isDirectory)
                    .forEach(path -> {

                        try {

                            watchKeys.add(path.register(cloudWatchService,
                                    standardWatchEventsKind
                            ));

                        } catch (IOException e) {
                            Application.log(getClass(), e, "watchKeys.add()");
                        }

                    });


        } catch (Exception e) {
            Application.log(getClass(), e,"SharedFolder %s", this);
        }

    }


    @Override
    public void close() {

        if(createQueue.size() + deleteQueue.size() + updateQueue.size() > 0)
            Application.log(getClass(), "WARNING! Ignoring %d create(), %d delete(), %d update() from %s", createQueue.size(), deleteQueue.size(), updateQueue.size(), this);

        try {
            cloudWatchService.close();
            localWatchService.close();
        } catch (IOException ignored) { }

        watchKeys.clear();

        createQueue.clear();
        deleteQueue.clear();
        updateQueue.clear();

    }


    public Path getLocalPath() {
        return localPath;
    }

    public MergePath getCloudPath() {
        return cloudPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedFolder that = (SharedFolder) o;
        return getLocalPath().equals(that.getLocalPath()) &&
                getCloudPath().equals(that.getCloudPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocalPath(), getCloudPath());
    }

    @Override
    public String toString() {
        return String.format("<%s::%s>", cloudPath, localPath);
    }
}
