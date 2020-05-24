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

package org.nefele.fs;

import org.nefele.Application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class SynchronizedFolder {

    private Path root;
    private String cloudPath;
    private WatchService watchService;

    private ArrayList<Path> registered;


    public SynchronizedFolder(Path path, String cloudPath) throws IOException {

        this.root = path;
        this.registered = new ArrayList<>();

        if(cloudPath.equals("/"))
            this.cloudPath = "";
        else
            this.cloudPath = cloudPath;



        if(!Files.exists(path))
            throw new FileNotFoundException(path.toString());

        watchService = FileSystems.getDefault().newWatchService();



        for(Object e : Files.list(path).filter(Files::isDirectory).toArray())
            register((Path) e);

        register(path);


        Application.getInstance().runThread(new Thread(this::worker, "SynchronizedFolderWorker"));

    }

    public Path getPath() {
        return root;
    }

    private void worker() {

        do {

            WatchKey watchKey;

            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                Application.log(e.getClass(), e.getMessage());
                continue;
            }


            for(WatchEvent<?> e : watchKey.pollEvents()) {

                WatchEvent.Kind<?> kind = e.kind();


                //try {

                    Path watchable = (Path) watchKey.watchable();
                    Path resolved  = (Path) watchable.resolve((Path) e.context());

                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {

                        //if(Files.isDirectory(resolved))
                        //   org.nefele.fs.Files.createDirectory(resolve(watchable, resolved));
                        //else
                        //   org.nefele.fs.Files.createFile(resolve(watchable, resolved));


                    } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                        //org.nefele.fs.Files.delete(resolve(watchable, resolved));

                    } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                        Application.log(getClass(), String.format("ENTRY_MODIFY: %s", e.context().toString()));

                    }


                //} catch (IOException ioException) {
                //    Application.Log(e.getClass(), ioException.getMessage());
                //}

            }


            watchKey.reset();

            if(!watchKey.isValid())
                break;

        } while(Application.getInstance().isRunning());


        Application.log(getClass(), String.format("Unregistered path %s", root.toString()));

    }


    private String resolve(Path watchable, Path entry) throws IOException {

        Path resolved = watchable.resolve(entry);
        Path relative = root.relativize(resolved);

        if(Files.isDirectory(resolved))
            register(resolved);


        return new StringBuilder()
                .append(cloudPath)
                //.append(MergeFile.pathSeparator)
                .append(relative)
                .toString();

    }

    private void register(Path path) throws IOException {

        if(registered.contains(path))
            return;

        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        Application.log(getClass(), String.format("Registered in %s path %s", root, path));


    }
}
