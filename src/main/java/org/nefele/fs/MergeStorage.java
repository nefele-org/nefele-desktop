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
import org.nefele.ApplicationService;
import org.nefele.ApplicationTask;
import org.nefele.cloud.*;
import org.nefele.utils.CryptoUtils;
import org.nefele.utils.IdUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class MergeStorage implements ApplicationService {


    private final HashSet<MergeNode> inodes;
    private final HashSet<MergeChunk> chunks;
    private final HashSet<MergeChunk> dustChunks;
    private final HashSet<MergeNode> dustNodes;
    private final Path cachePath;


    public MergeStorage() {

        this.inodes = new HashSet<>();
        this.chunks = new HashSet<>();
        this.dustChunks = new HashSet<>();
        this.dustNodes = new HashSet<>();
        this.cachePath = Application.getInstance().getDataPath().resolve("cache");

    }


    public HashSet<MergeNode> getInodes() {
        synchronized (inodes) {
            return inodes;
        }
    }

    public HashSet<MergeChunk> getChunks() {
        synchronized (chunks) {
            return chunks;
        }
    }

    public Stream<MergeNode> getInodeStream() {
        synchronized (inodes) {
            return Set.copyOf(inodes).stream();
        }
    }

    public Stream<MergeChunk> getChunkStream() {
        synchronized (chunks) {
            return Set.copyOf(chunks).stream();
        }
    }



    public void write(MergeChunk chunk, ByteBuffer byteBuffer, long offset, boolean raw) throws IOException {

        try {

            if(offset != 0L) {

//                fileChannel = (FileChannel) Files.newByteChannel(cachePath.resolve(Path.of(chunk.getId())),
//                        StandardOpenOption.WRITE);

                throw new IllegalStateException("deprecated");

            }

            if(chunk.isEncrypted() && raw)
                byteBuffer = CryptoUtils.encrypt(byteBuffer);



            OutputStream outputStream;

            if(chunk.isCompressed() && raw) {
                outputStream = new DeflaterOutputStream(Files.newOutputStream(cachePath.resolve(chunk.getId()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                ), new Deflater(Application.getInstance().getConfig()
                        .getInteger("core.mfs.compression.level")
                        .orElse(Deflater.DEFAULT_COMPRESSION), true));

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

            InputStream inputStream = Files.newInputStream(cachePath.resolve(chunk.getId()));

            if(chunk.isCompressed() && !raw)
                inputStream = new InflaterInputStream(inputStream, new Inflater(true));

            if(chunk.isEncrypted() && !raw)
                inputStream = CryptoUtils.decrypt(inputStream);

            return inputStream;

        } catch (IOException e) {
            Application.log(getClass(), e, "read()");
            throw e;
        }

    }

    public void free(MergeChunk chunk) {

        try {

            getChunks().remove(chunk);

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

    public void free(MergeNode node, boolean clearData) {

        try {

            getInodes().remove(node);

            if(clearData) {
                while (!node.getChunks().isEmpty())
                    free(node.getChunks().get(0));
            }

        } finally {
            dustNodes.add(node);
        }

    }


    public void create(MergeNode node) {
        getInodes().add(node);
    }



    public MergeChunk alloc(MergeNode node, long offset) throws IOException {

        try {

            DriveProvider driveProvider = DriveProviders.getInstance().nextAllocatable();

            MergeChunk chunk = new MergeChunk(
                    IdUtils.generateId(), offset, node, driveProvider, 0L, 0L,
                    Application.getInstance().getConfig().getBoolean("core.mfs.compression.enable").orElse(false),
                    Application.getInstance().getConfig().getBoolean("core.mfs.encryption.enable").orElse(false)
            );


            getChunks().add(chunk);
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



    public boolean isCached(MergeChunk chunk) {
        return Files.exists(cachePath.resolve(Path.of(chunk.getId())));
    }




    public long getCurrentCacheSize() {

        return getChunkStream()
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

                        MergeNode node = new MergeNode(
                                r.getString("name"),
                                r.getString("mime"),
                                r.getLong("size"),
                                Instant.ofEpochSecond(r.getLong("ctime")),
                                Instant.ofEpochSecond(r.getLong("atime")),
                                Instant.ofEpochSecond(r.getLong("mtime")),
                                r.getString("id"),
                                r.getString("parent")
                        );


                        getInodes().add(node);

                    }
            );


            Application.getInstance().getDatabase().fetch(
                    "SELECT * FROM chunks", null,
                    r -> {


                        try {


                            DriveProvider driveProvider = DriveProviders.getInstance().fromId(r.getString("drive"));


                            final var inodeId = r.getString("inode");

                            MergeNode inode = getInodeStream()
                                    .filter(i -> i.getId().equals(inodeId))
                                    .findFirst()
                                    .orElse(null);

                            if (inode == null)
                                throw new NoSuchFileException(inodeId);


                            MergeChunk chunk = new MergeChunk(
                                    r.getString("id"),
                                    r.getLong("offset"),
                                    inode,
                                    driveProvider,
                                    r.getLong("revision"),
                                    r.getLong("size"),
                                    r.getInt("compressed") != 0,
                                    r.getInt("encrypted") != 0
                            );

                            getChunks().add(chunk);

                        } catch (DriveNotFoundException | NoSuchFileException e) {
                            Application.log(getClass(), "WARNING! Chunk %s has been orphaned or invalid: %s", r.getString("id"), e.getClass().getName());
                        }

                    }
            );

        } catch (SQLException e) {
            Application.panic(getClass(), e);
        }


        getChunkStream().forEach(i ->
                i.getInode().getChunks().add(i));

        getInodes().removeAll(
                getInodeStream()
                        .filter(i -> i.getSize() > 0)
                        .filter(i -> i.getChunks().isEmpty())
                        .peek(i -> Application.log(getClass(), "WARNING! Inode %s has been corrupted or invalid", i.getId()))
                        .collect(Collectors.toList())
        );

    }

    @Override
    public void update(ApplicationTask currentTask) {


        try {

            Application.getInstance().getDatabase().update (
                    "INSERT OR REPLACE INTO inodes (name, mime, size, ctime, atime, mtime, id, parent) values (?, ?, ?, ?, ?, ?, ?, ?)",

                    s -> {

                        for(var node : Set.copyOf(getInodes())) {

                            if(!node.isDirty())
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

                        for(var chunk : Set.copyOf(getChunks())) {

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

                        for(var node : Set.copyOf(dustNodes)) {

                            s.setString(1, node.getId());
                            s.addBatch();

                        }

                    }, true
            );


            Application.getInstance().getDatabase().update (
                    "DELETE FROM chunks WHERE id = ?",

                    s -> {

                        for(var chunk : Set.copyOf(dustChunks)) {

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
    public void close() {

        update(null);
        cleanCache();

    }

}
