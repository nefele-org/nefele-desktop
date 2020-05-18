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

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.nefele.Application;
import org.nefele.utils.Hash;

import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MergeFileChannel extends FileChannel {

    private final MergePath path;
    private final Inode inode;
    private long position;

    public MergeFileChannel(MergePath path) {
        this.path = path;
        this.inode = path.getInode().getData();
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

        long blocksize = Chunk.getSize();
        long initpos = position;


        while(byteBuffer.hasRemaining()) {

            long block = position / blocksize;
            long size = blocksize;

            if(position % blocksize != 0)
                throw new IOException("position is not aligned");

            if(byteBuffer.remaining() < size)
                size = byteBuffer.remaining();


            Chunk chunk;

            if(inode.getChunks().size() > block)
                chunk = inode.getChunks().get((int) block);

            else {

                chunk = Chunk.alloc(inode.getId(), block);

                inode.getChunks().add(chunk);
                inode.setSize(inode.getSize() + size);
                inode.invalidate();

            }


            Application.getInstance().getCache()
                    .write(chunk, byteBuffer, (int) size);

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
