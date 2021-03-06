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

package org.nefele.cloud.providers;

import org.nefele.Application;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.TransferInfoAbortException;
import org.nefele.cloud.TransferInfoCallback;
import org.nefele.cloud.TransferInfoException;
import org.nefele.fs.MergeChunk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OfflineDriveProvider extends DriveProvider {

    public final static String SERVICE_ID = "offline-drive-service";
    public final static String SERVICE_DEFAULT_DESCRIPTION = "Offline Cloud";

    private final Path drivePath;
    private final Path servicePath;


    public OfflineDriveProvider(String id, String service, String description, long quota, long blocks) {
        super(id, service, description, quota, blocks);

        this.servicePath = Application.getInstance()
                .getDataPath().resolve(Paths.get("drive", SERVICE_ID, id));

        this.drivePath = Application.getInstance()
                .getDataPath().resolve(Paths.get("drive", SERVICE_ID, id, "storage"));

    }

    @Override
    public void writeChunk(MergeChunk chunk, InputStream inputStream, TransferInfoCallback callback) throws TransferInfoException {

        try (OutputStream outputStream = Files.newOutputStream(drivePath.resolve(chunk.getId()))) {

            while (inputStream.available() > 0) {

                if (callback.isCancelled())
                    break;


                var bytes = new byte[Math.min(65536, inputStream.available())];

                if (inputStream.read(bytes) > 0)
                    outputStream.write(bytes);

                callback.updateProgress(bytes.length);


                try {
                    Thread.sleep(10); // FIXME: used only for testing
                } catch (InterruptedException ignored) { }

            }


            Files.writeString(drivePath.resolve(chunk.getId() + ".rev"), Long.toUnsignedString(chunk.getRevision()));

        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }


    }

    @Override
    public ByteBuffer readChunk(MergeChunk chunk, TransferInfoCallback callback) throws TransferInfoException {

        try (FileInputStream inputStream = new FileInputStream(new File(drivePath.resolve(Paths.get(chunk.getId())).toString()))) {


            ByteBuffer byteBuffer = ByteBuffer
                    .allocateDirect(inputStream.available());

            while (inputStream.available() > 0) {

                if (callback.isCancelled())
                    break;


                var bytes = new byte[Math.min(65536, inputStream.available())];

                if (inputStream.read(bytes) > 0)
                    byteBuffer.put(bytes);

                callback.updateProgress(bytes.length);


                try {
                    Thread.sleep(10); // FIXME: used only for testing
                } catch (InterruptedException ignored) {
                }

            }


            return byteBuffer.flip();

        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }


    @Override
    public void removeChunk(MergeChunk chunk) throws TransferInfoException {

        try {

            Files.delete(drivePath
                    .resolve(Paths.get(chunk.getId())));

            Files.delete(drivePath
                    .resolve(Paths.get(chunk.getId() + ".rev")));

        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }


    @Override
    public int isChunkUpdated(MergeChunk chunk) throws TransferInfoException {

        try {

            long currentRev = chunk.getRevision();
            long updatedRev = Long.parseUnsignedLong(Files.readString(drivePath.resolve(chunk.getId() + ".rev")));

            return (int) (currentRev - updatedRev);

        } catch (Exception e) {
            throw new TransferInfoAbortException(e.getMessage());
        }

    }

    @Override
    public long getMaxQuota() {
        return 8589934592L;
    }


    @Override
    public DriveProvider initialize() {

        Application.log(getClass(), "Initializing %s %s", SERVICE_ID, getId());
        setStatus(STATUS_CONNECTING);


        try {

            if(Files.notExists(servicePath.getParent()))
                Files.createDirectory(servicePath.getParent());

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

        setStatus(STATUS_READY);
        return this;
    }

    @Override
    public DriveProvider exit() {
        return this;
    }

}
