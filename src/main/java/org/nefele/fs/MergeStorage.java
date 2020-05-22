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
import org.nefele.cloud.Drive;
import org.nefele.cloud.DriveFullException;
import org.nefele.cloud.DriveNotFoundException;
import org.nefele.cloud.Drives;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

;

public class MergeStorage implements Service {

    private final HashMap<String, MergeNode> inodes;
    private final HashMap<String, MergeChunk> chunks;
    private final ArrayList<MergeChunk> dustChunks;
    private final ArrayList<MergeNode> dustNodes;
    private final Path path;


    public MergeStorage() {

        this.inodes = new HashMap<>();
        this.chunks = new HashMap<>();
        this.dustChunks = new ArrayList<>();
        this.dustNodes = new ArrayList<>();
        this.path = Paths.get(System.getProperty("user.home"), ".nefele", "cache");

        /* FIXME: Service registered too late */
        initialize(null);

    }


    public HashMap<String, MergeNode> getInodes() {
        return inodes;
    }

    public HashMap<String, MergeChunk> getChunks() {
        return chunks;
    }



    public void write(MergeChunk chunk, ByteBuffer byteBuffer, long offset) throws IOException {

        try {

            FileChannel fileChannel;

            if(offset == 0L) {

                fileChannel = (FileChannel) Files.newByteChannel(path.resolve(Path.of(chunk.getId())),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);

            } else {

                fileChannel = (FileChannel) Files.newByteChannel(path.resolve(Path.of(chunk.getId())),
                        StandardOpenOption.WRITE);
            }

            fileChannel.write(byteBuffer, offset);
            fileChannel.close();


            chunk.setHash(String.format("%d", System.nanoTime()));
            chunk.invalidate();

        } catch (IOException e) {
            Application.log(getClass(), "WARNING! write() %s: %s", e.getClass().getName(), e.getMessage());
        }

    }

    public InputStream read(MergeChunk chunk) throws IOException {

        try {

            return Files.newInputStream(path.resolve(Path.of(chunk.getId())));

        } catch (IOException e) {
            Application.log(getClass(), "WARNING! read() %s: %s", e.getClass().getName(), e.getMessage());
            throw new IOException(e.getMessage());
        }

    }

    public void free(MergeChunk chunk) {

        try {

            getChunks().remove(chunk.getId());

            chunk.getInode().getChunks().remove(chunk);
            chunk.getInode().invalidate();

            chunk.getDrive().removeChunk(chunk);
            chunk.getDrive().setChunks(chunk.getDrive().getChunks() - 1L);
            chunk.getDrive().invalidate();

            if(isCached(chunk))
                Files.delete(path.resolve(Path.of(chunk.getId())));


        } catch (IOException e) {
            Application.log(getClass(), "WARNING! free() %s: %s", e.getClass().getName(), e.getMessage());
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




    public MergeChunk alloc(MergeNode node, long offset) {

        try {

            Drive drive = Drives.getInstance().nextAllocatable();
            MergeChunk chunk = new MergeChunk(generateId(), offset, node, drive, "");

            getChunks().put(chunk.getId(), chunk);
            node.getChunks().add(chunk);

            drive.setChunks(drive.getChunks() + 1L);
            drive.invalidate();

            chunk.invalidate();
            return chunk;

        } catch (DriveFullException e) {
            Application.log(getClass(), "WARNING! alloc() %s: %s", e.getClass().getName(), e.getMessage());
            throw new DriveFullException();
        }

    }


    public MergeNode alloc(MergeNode parent, String name, String mime) {

        MergeNode node = new MergeNode(
                name, mime, 0L, Instant.now(), Instant.now(), Instant.now(), generateId(), parent.getId()
        );

        node.invalidate();
        getInodes().put(node.getId(), node);

        return node;

    }


    public boolean isCached(MergeChunk chunk) {
        return Files.exists(path.resolve(Path.of(chunk.getId())));
    }


    public long getCurrentSize() {

        return getChunks().values()
                .stream()
                .filter(this::isCached)
                .count() * MergeChunk.getSize();

    }


    @Override
    public void initialize(Application app) {

        try {

            if (Files.notExists(path))
                Files.createDirectory(path);

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
                                r.getString("parent")
                        );

                        getInodes().put(node.getId(), node);

                    }
            );


            Application.getInstance().getDatabase().fetch(
                    "SELECT * FROM chunks", null,
                    r -> {


                        try {

                            Drive drive = Drives.getInstance().fromId(r.getString("drive"));
                            MergeNode inode = getInodes().get(r.getString("inode"));


                            if(inode == null)
                                throw new FileNotFoundException();

                            MergeChunk chunk = new MergeChunk(
                                    r.getString("id"),
                                    r.getLong("offset"),
                                    inode,
                                    drive,
                                    r.getString("hash")
                            );

                            getChunks().put(chunk.getId(), chunk);

                        } catch (DriveNotFoundException | FileNotFoundException e) {
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
    public void synchronize(Application app) {


        try {

            Application.getInstance().getDatabase().update (
                    "INSERT OR REPLACE INTO inodes (name, mime, size, ctime, atime, mtime, id, parent) values (?, ?, ?, ?, ?, ?, ?, ?)",

                    s -> {

                        for(MergeNode node : getInodes().values()) {

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
                    "INSERT OR REPLACE INTO chunks (id, offset, inode, drive, hash) values (?, ?, ?, ?, ?)",

                    s -> {

                        for(MergeChunk chunk : getChunks().values()) {

                            if(!chunk.isDirty())
                                continue;

                            s.setString(1, chunk.getId());
                            s.setLong(2, chunk.getOffset());
                            s.setString(3, chunk.getInode().getId());
                            s.setString(4, chunk.getDrive().getId());
                            s.setString(5, chunk.getHash());
                            s.addBatch();

                            chunk.validate();

                        }

                    }, true
            );


            Application.getInstance().getDatabase().update (
                    "DELETE FROM inodes WHERE id = ?",

                    s -> {

                        for(MergeNode node : dustNodes) {

                            s.setString(1, node.getId());
                            s.addBatch();

                        }

                    }, true
            );


            Application.getInstance().getDatabase().update (
                    "DELETE FROM chunks WHERE id = ?",

                    s -> {

                        for(MergeChunk chunk : dustChunks) {

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
    public void exit(Application app) {

        synchronize(app);


        try {

            Files.list(path).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) { }
            });

        } catch (IOException ignored) { }

    }



    public static String generateId() {
        return UUID.randomUUID().toString();
    }

}
