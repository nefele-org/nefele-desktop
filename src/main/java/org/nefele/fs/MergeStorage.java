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
import org.nefele.Service;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveFullException;
import org.nefele.cloud.DriveNotFoundException;
import org.nefele.cloud.DriveProviders;
import org.nefele.transfers.TransferInfoException;
import org.nefele.transfers.TransferInfoTryAgainException;
import org.nefele.utils.IdUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

;

public class MergeStorage implements Service {

    private final HashMap<String, MergeNode> inodes;
    private final HashMap<String, MergeChunk> chunks;
    private final ArrayList<MergeChunk> dustChunks;
    private final ArrayList<MergeNode> dustNodes;
    private final Path cachePath;


    public MergeStorage() {

        this.inodes = new HashMap<>();
        this.chunks = new HashMap<>();
        this.dustChunks = new ArrayList<>();
        this.dustNodes = new ArrayList<>();
        this.cachePath = Application.getInstance().getDataPath().resolve("cache");

        /* FIXME: Service registered too late */
        initialize();

    }


    public HashMap<String, MergeNode> getInodes() {
        return inodes;
    }

    public HashMap<String, MergeChunk> getChunks() {
        return chunks;
    }



    public void write(MergeChunk chunk, ByteBuffer byteBuffer, long offset, boolean raw) throws IOException {

        try {

            if(offset != 0L) {

//                fileChannel = (FileChannel) Files.newByteChannel(cachePath.resolve(Path.of(chunk.getId())),
//                        StandardOpenOption.WRITE);

                throw new IllegalStateException("deprecated");

            }

            if(chunk.isEncrypted()) {
                // TODO...
            }


            OutputStream outputStream;

            if(chunk.isCompressed() && raw) {
                outputStream = new DeflaterOutputStream(Files.newOutputStream(cachePath.resolve(chunk.getId()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                ));

            } else {
                outputStream = Files.newOutputStream(cachePath.resolve(chunk.getId()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

            }


            while(byteBuffer.hasRemaining()) {

                var bytes = new byte[Math.min(65536, byteBuffer.remaining())];

                byteBuffer.get(bytes);
                outputStream.write(bytes);

            }

            outputStream.flush();
            outputStream.close();


            chunk.setRevision(Instant.now().toEpochMilli());
            chunk.setSize(Files.size(cachePath.resolve(chunk.getId())));
            chunk.invalidate();

        } catch (IOException e) {
            Application.log(getClass(), e,"write()");
            throw e;
        }

    }

    public InputStream read(MergeChunk chunk, boolean raw) throws IOException {

        try {

            if(chunk.isEncrypted()) {
                 // TODO... decrypt()
            }

            if(chunk.isCompressed() && !raw)
                return new InflaterInputStream(Files.newInputStream(cachePath.resolve(chunk.getId())));

            return Files.newInputStream(cachePath.resolve(chunk.getId()));

        } catch (IOException e) {
            Application.log(getClass(), e, "read()");
            throw e;
        }

    }

    public void free(MergeChunk chunk) {

        try {

            getChunks().remove(chunk.getId());

            chunk.getInode().getChunks().remove(chunk);
            chunk.getInode().invalidate();


            while(true) {

                try {

                    chunk.getDriveProvider().removeChunk(chunk);
                    break;

                } catch (TransferInfoTryAgainException ignored) {
                } catch (TransferInfoException e) { break; }

            }


            chunk.getDriveProvider().setChunks(chunk.getDriveProvider().getChunks() - 1L);
            chunk.getDriveProvider().invalidate();

            if(isCached(chunk))
                Files.delete(cachePath.resolve(Path.of(chunk.getId())));


        } catch (IOException e) {
            Application.log(getClass(), e, "free()");
        } finally {
            dustChunks.add(chunk);
        }

    }

    public void free(MergeNode node) {

        try {

            getInodes().remove(node.getId());

            while (!node.getChunks().isEmpty())
                free(node.getChunks().get(0));


        } finally {
            dustNodes.add(node);
        }

    }




    public MergeChunk alloc(MergeNode node, long offset) throws IOException {

        try {

            DriveProvider driveProvider = DriveProviders.getInstance().nextAllocatable();

            MergeChunk chunk = new MergeChunk(
                    IdUtils.generateId(), offset, node, driveProvider, 0L, 0L,
                    Application.getInstance().getConfig().getBoolean("core.mfs.compressed").orElse(false),
                    Application.getInstance().getConfig().getBoolean("core.mfs.encrypted").orElse(false)
            );


            getChunks().put(chunk.getId(), chunk);
            node.getChunks().add(chunk);

            driveProvider.setChunks(driveProvider.getChunks() + 1L);
            driveProvider.invalidate();

            chunk.invalidate();
            return chunk;

        } catch (DriveFullException | DriveNotFoundException e) {
            Application.log(getClass(), e, "alloc()");
            throw e;
        }

    }


    public MergeNode alloc(MergeNode parent, String name, String mime) {

        MergeNode node = new MergeNode(
                name, mime, 0L, Instant.now(), Instant.now(), Instant.now(), IdUtils.generateId(), parent.getId(), false
        );


        node.invalidate();
        getInodes().put(node.getId(), node);

        return node;

    }


    public boolean isCached(MergeChunk chunk) {
        return Files.exists(cachePath.resolve(Path.of(chunk.getId())));
    }




    public long getCurrentCacheSize() {

        return getChunks().values()
                .stream()
                .filter(this::isCached)
                .mapToLong(MergeChunk::getSize)
                .sum();

    }

    public void cleanCache() {

        Application.log(getClass(), "Cleaning local cache directory");

        try {

            Files.list(cachePath).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) { }
            });

        } catch (IOException ignored) { }

    }


    @Override
    public void initialize() {

        try {

            if (Files.notExists(cachePath))
                Files.createDirectory(cachePath);

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }



        try {

            Application.getInstance().getDatabase().fetch(
                    "SELECT * FROM inodes", null,
                    r -> {

                        MergeNode node = new MergeNode (
                                r.getString("name"),
                                r.getString("mime"),
                                r.getLong("size"),
                                Instant.ofEpochSecond(r.getLong("ctime")),
                                Instant.ofEpochSecond(r.getLong("atime")),
                                Instant.ofEpochSecond(r.getLong("mtime")),
                                r.getString("id"),
                                r.getString("parent"),
                                true
                        );

                        getInodes().put(node.getId(), node);

                    }
            );


            Application.getInstance().getDatabase().fetch(
                    "SELECT * FROM chunks", null,
                    r -> {


                        try {

                            DriveProvider driveProvider = DriveProviders.getInstance().fromId(r.getString("drive"));
                            MergeNode inode = getInodes().get(r.getString("inode"));


                            if(inode == null)
                                throw new NoSuchFileException(r.getString("inode"));

                            MergeChunk chunk = new MergeChunk (
                                    r.getString("id"),
                                    r.getLong("offset"),
                                    inode,
                                    driveProvider,
                                    r.getLong("revision"),
                                    r.getLong("size"),
                                    r.getInt("compressed") != 0,
                                    r.getInt("encrypted") != 0
                            );

                            getChunks().put(chunk.getId(), chunk);

                        } catch (DriveNotFoundException | NoSuchFileException e) {
                            Application.log(getClass(), "WARNING! Chunk %s has been orphaned or invalid: %s", r.getString("id"), e.getClass().getName());
                        }

                    }
            );

        } catch (SQLException e) {
            Application.panic(getClass(), e);
        }



        getChunks().values().forEach(i ->
            i.getInode().getChunks().add(i));


    }

    @Override
    public void synchronize() {


        try {

            Application.getInstance().getDatabase().update (
                    "INSERT OR REPLACE INTO inodes (name, mime, size, ctime, atime, mtime, id, parent) values (?, ?, ?, ?, ?, ?, ?, ?)",

                    s -> {

                        for(var node : getInodes().values()) {

                            if(!node.isDirty())
                                continue;

                            if(!node.exists())
                                continue;

                            s.setString(1, node.getName());
                            s.setString(2, node.getMime());
                            s.setLong(3, node.getSize());
                            s.setLong(4, node.getCreatedTime().getEpochSecond());
                            s.setLong(5, node.getAccessedTime().getEpochSecond());
                            s.setLong(6, node.getModifiedTime().getEpochSecond());
                            s.setString(7, node.getId());
                            s.setString(8, node.getParent());
                            s.addBatch();

                            node.validate();

                        }

                    }, true
            );


            Application.getInstance().getDatabase().update (
                    "INSERT OR REPLACE INTO chunks (id, offset, inode, drive, revision, size, compressed, encrypted) values (?, ?, ?, ?, ?, ?, ?, ?)",

                    s -> {

                        for(var chunk : getChunks().values()) {

                            if(!chunk.isDirty())
                                continue;

                            s.setString(1, chunk.getId());
                            s.setLong(2, chunk.getOffset());
                            s.setString(3, chunk.getInode().getId());
                            s.setString(4, chunk.getDriveProvider().getId());
                            s.setLong(5, chunk.getRevision());
                            s.setLong(6, chunk.getSize());
                            s.setInt(7, chunk.isCompressed() ? 1 : 0);
                            s.setInt(8, chunk.isEncrypted() ? 1 : 0);
                            s.addBatch();

                            chunk.validate();

                        }

                    }, true
            );


            Application.getInstance().getDatabase().update (
                    "DELETE FROM inodes WHERE id = ?",

                    s -> {

                        for(var node : dustNodes) {

                            s.setString(1, node.getId());
                            s.addBatch();

                        }

                    }, true
            );


            Application.getInstance().getDatabase().update (
                    "DELETE FROM chunks WHERE id = ?",

                    s -> {

                        for(var chunk : dustChunks) {

                            s.setString(1, chunk.getId());
                            s.addBatch();

                        }

                    }, true
            );

        } catch (SQLException e) {
            Application.panic(getClass(), e);
        }

    }

    @Override
    public void exit() {

        synchronize();
        cleanCache();

    }

}
