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
import org.nefele.fs.MergeChunk;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OfflineDriveService extends Drive {

    public final static String SERVICE_ID = "offline-drive-service";

    private final Path drivePath;
    private final Path servicePath;


    public OfflineDriveService(String id, String service, long quota, long blocks) {
        super(id, service, quota, blocks);

        this.servicePath = Paths.get(System.getProperty("user.home"), ".nefele", SERVICE_ID);
        this.drivePath = Paths.get(System.getProperty("user.home"), ".nefele", SERVICE_ID, id);

    }

    @Override
    public OutputStream writeChunk(MergeChunk chunk) {

        try {
            return new FileOutputStream(new File(drivePath.resolve(Paths.get(chunk.getId())).toString()));
        } catch (FileNotFoundException e) {
            Application.panic(getClass(), e);
        }

        throw new IllegalStateException();

    }

    @Override
    public InputStream readChunk(MergeChunk chunk) {

        try {
            return new FileInputStream(new File(drivePath.resolve(Paths.get(chunk.getId())).toString()));
        } catch (FileNotFoundException e) {
            Application.panic(getClass(), e);
        }

        throw new IllegalStateException();

    }

    @Override
    public void removeChunk(MergeChunk chunk) {

        try {
            Files.delete(drivePath.resolve(Paths.get(chunk.getId())));
        } catch (IOException ignored) { }

    }

    @Override
    public Drive initialize() {

        Application.log(getClass(), "Initializing %s %s", SERVICE_ID, getId());



        try {

            if(Files.notExists(servicePath))
                Files.createDirectory(servicePath);

            if(Files.notExists(drivePath))
                Files.createDirectory(drivePath);

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }


        try {

            setChunks(Files.list(drivePath).count());

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }

        return this;
    }

    @Override
    public Drive exit() {
        return this;
    }

}
