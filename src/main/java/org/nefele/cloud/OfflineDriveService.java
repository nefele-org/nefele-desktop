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

import javafx.scene.Parent;
import org.nefele.Application;
import org.nefele.fs.Chunk;
import org.nefele.fs.MergeFileSystem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class OfflineDriveService extends Drive {

    public final static String SERVICE_ID = "offline-drive-service";

    private final Path offlinePath;


    public OfflineDriveService(String id, String service) {
        super(id, service);

        this.offlinePath = Paths.get("offline-cloud-service", id);

    }

    @Override
    OutputStream writeChunk(Chunk chunk) {

        try {
            return new FileOutputStream(new File(offlinePath.resolve(Paths.get(chunk.getId())).toString()));
        } catch (FileNotFoundException e) {
            Application.panic(getClass(), e);
        }

        throw new IllegalStateException();

    }

    @Override
    InputStream readChunk(Chunk chunk) {

        try {
            return new FileInputStream(new File(offlinePath.resolve(Paths.get(chunk.getId())).toString()));
        } catch (FileNotFoundException e) {
            Application.panic(getClass(), e);
        }

        throw new IllegalStateException();

    }

    @Override
    Drive initialize() {

        Application.log(getClass(), "Initializing %s", SERVICE_ID);


        final Path path = offlinePath;

        if(Files.notExists(path)) {

            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                Application.panic(getClass(), e);
            }

        }


        try {

            setChunks(Files.walk(path).count());

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }



        return this;
    }

    @Override
    Drive exit() {
        return this;
    }

}
