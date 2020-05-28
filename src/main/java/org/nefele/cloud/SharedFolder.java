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
import org.nefele.Service;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergePath;
import org.nefele.transfers.DownloadTransferInfo;
import org.nefele.transfers.TransferInfo;
import org.nefele.transfers.UploadTransferInfo;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public final class SharedFolder implements Service {

    private final Path localPath;
    private final MergePath cloudPath;
    private final ArrayList<String> uploadQueue;
    private final ArrayList<String> downloadQueue;

    public SharedFolder(Path localPath, MergePath cloudPath) {

        this.localPath = localPath;
        this.cloudPath = cloudPath;

        this.uploadQueue = new ArrayList<>();
        this.downloadQueue = new ArrayList<>();

        initialize(null);
    }


    @Override
    public void initialize(Application app) {

        try {

            final ArrayList<String> localFiles = new ArrayList<>();
            final ArrayList<String> cloudFiles = new ArrayList<>();

            Files.walk(localPath)
                    .filter(Predicate.not(localPath::equals))
                    .map(localPath::relativize)
                    .map(Path::toString)
                    .forEach(localFiles::add);

            Files.walk(cloudPath)
                    .filter(Predicate.not(cloudPath::equals))
                    .map(cloudPath::relativize)
                    .map(Path::toString)
                    .forEach(cloudFiles::add);

            localFiles
                    .stream()
                    .map(name -> name.replace(File.pathSeparator, MergeFileSystem.PATH_SEPARATOR))
                    .filter(Predicate.not(cloudFiles::contains))
                    .map(name -> name.replace(MergeFileSystem.PATH_SEPARATOR, File.pathSeparator))
                    .forEach(uploadQueue::add);

            cloudFiles
                    .stream()
                    .map(name -> name.replace(MergeFileSystem.PATH_SEPARATOR, File.pathSeparator))
                    .filter(Predicate.not(localFiles::contains))
                    .map(name -> name.replace(File.pathSeparator, MergeFileSystem.PATH_SEPARATOR))
                    .forEach(downloadQueue::add);


            Application.getInstance().addService(this);

        } catch (Exception e) {
            e.printStackTrace();
            Application.log(getClass(), e,"SharedFolder %s::%s", localPath, cloudPath);
        }

    }

    @Override @SuppressWarnings("unchecked")
    public void synchronize(Application app) {


        final ArrayList<String> currentUploadQueue = ((ArrayList<String>) uploadQueue.clone());
        final ArrayList<String> currentDownloadQueue = ((ArrayList<String>) downloadQueue.clone());

        uploadQueue.clear();
        downloadQueue.clear();



        currentUploadQueue.stream()
                .distinct()
                .forEach(q -> {

                    try {

                        if(Files.isDirectory(localPath.resolve(q)))
                            Files.createDirectory(cloudPath.resolve(q));


                        else {

                            if (Files.exists(cloudPath.resolve(q)))
                                Files.delete(cloudPath.resolve(q));

                            final Future<Integer> future = Application.getInstance().getTransferQueue().enqueue(
                                    new UploadTransferInfo((MergePath) cloudPath.resolve(q), localPath.resolve(q).toFile()));


                            Application.getInstance().runThread(new Thread(() -> {

                                try {

                                    int status = future.get();

                                    switch (status) {

                                        case TransferInfo.TRANSFER_STATUS_COMPLETED:
                                            break;

                                        case TransferInfo.TRANSFER_STATUS_ERROR:
                                        case TransferInfo.TRANSFER_STATUS_CANCELED:

                                            if (Files.exists(cloudPath.resolve(q)))
                                                Files.delete(cloudPath.resolve(q));

                                            uploadQueue.add(q);
                                            break;

                                        default:
                                            Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                            break;

                                    }


                                } catch (Exception e) {
                                    Application.log(getClass(), e,"SharedFolder::UploadQueueFuture::%s", q);
                                }


                            }, "SharedFolder::UploadQueue::" + q));

                        }

                    } catch (Exception e) {
                        Application.log(getClass(), e,"SharedFolder::UploadQueue::%s", q);
                    }

                });




        currentDownloadQueue.stream()
                .distinct()
                .forEach(q -> {


                    try {

                        if(Files.isDirectory(cloudPath.resolve(q)))
                            Files.createDirectory(localPath.resolve(q));


                        else {

                            if (Files.exists(localPath.resolve(q)))
                                Files.delete(localPath.resolve(q));


                            final Future<Integer> future = Application.getInstance().getTransferQueue().enqueue(
                                    new DownloadTransferInfo((MergePath) cloudPath.resolve(q), localPath.resolve(q).toFile()));

                            Application.getInstance().runThread(new Thread(() -> {

                                try {

                                    int status = future.get();

                                    switch (status) {

                                        case TransferInfo.TRANSFER_STATUS_COMPLETED:
                                            break;

                                        case TransferInfo.TRANSFER_STATUS_ERROR:
                                        case TransferInfo.TRANSFER_STATUS_CANCELED:

                                            if (Files.exists(localPath.resolve(q)))
                                                Files.delete(localPath.resolve(q));

                                            downloadQueue.add(q);
                                            break;

                                        default:
                                            Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                            break;

                                    }


                                } catch (Exception e) {
                                    Application.log(getClass(), e, "SharedFolder::DownloadQueueFuture::%s", q);
                                }


                            }, "SharedFolder::DownloadQueue::" + q));


                        }

                    } catch (Exception e) {
                        Application.log(getClass(), e, "SharedFolder::DownloadQueue::" + q);
                    }

                });


    }



    @Override
    public void exit(Application app) {

        if(uploadQueue.size() + downloadQueue.size() > 0)
            Application.log(getClass(), "WARNING! Ignoring %d uploads and %d downloads from %s:%s", uploadQueue.size(), downloadQueue.size(), localPath, cloudPath);

        uploadQueue.clear();
        downloadQueue.clear();

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
}
