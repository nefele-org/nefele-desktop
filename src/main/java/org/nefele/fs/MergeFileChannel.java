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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;

public class MergeFileChannel extends FileChannel {

    private final MergePath path;
    private final MergeNode inode;
    private final MergeFileSystem fileSystem;
    private long position;

    public MergeFileChannel(MergePath path) {

        if(!(path.getFileSystem() instanceof MergeFileSystem))
            throw new IllegalArgumentException();

        this.path = path;
        this.inode = path.getInode().getData();
        this.fileSystem = (MergeFileSystem) path.getFileSystem();
        this.position = 0;

    }


    public MergePath getPath() {
        return path;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        int e = read(byteBuffer, position);
        position += e;
        return e;
    }

    @Override
    public long read(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        int e = write(byteBuffer, position);
        position += e;
        return e;
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public FileChannel position(long l) throws IOException {
        this.position = l;
        return this;
    }

    @Override
    public long size() throws IOException {
        return inode.getSize();
    }

    @Override
    public FileChannel truncate(long l) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void force(boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferTo(long l, long l1, WritableByteChannel writableByteChannel) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferFrom(ReadableByteChannel readableByteChannel, long l, long l1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(ByteBuffer byteBuffer, long l) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer byteBuffer, long position) throws IOException {

        long blocksize = MergeChunk.getSize();
        long initpos = position;


        if(blocksize < 8192L)
            throw new IllegalStateException();


        while(byteBuffer.hasRemaining()) {

            long block = position / blocksize;
            long offset = position % blocksize;
            long size = blocksize;

            if (byteBuffer.remaining() < size)
                size = byteBuffer.remaining();


            MergeChunk chunk;

            if (inode.getChunks().size() > block) {

                chunk = inode.getChunks()
                        .stream()
                        .filter(i -> i.getOffset() == block)
                        .findFirst()
                        .get();

            } else {

                chunk = fileSystem.getCache().alloc(inode, block);

                inode.setSize(inode.getSize() + size);
                inode.setAccessedTime(Instant.now());
                inode.setModifiedTime(Instant.now());
                inode.invalidate();

            }

            fileSystem.getCache()
                    .write(chunk, byteBuffer, offset);

            position += size;

        }



        return (int) (position - initpos);

    }


    @Override
    public MappedByteBuffer map(MapMode mapMode, long l, long l1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(long l, long l1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(long l, long l1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void implCloseChannel() throws IOException {

    }

}
