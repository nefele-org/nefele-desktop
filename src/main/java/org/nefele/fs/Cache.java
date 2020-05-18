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
import org.nefele.utils.Hash;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public final class Cache {

    private final static String CACHE_PATH = "cache";

    private final Map<String, File> cache;
    private long currentSize;


    public Cache() {

        cache = Collections.synchronizedMap(new HashMap<>());
        currentSize = 0L;

        Application.addOnInitHandler(this::initialize);

    }


    public void initialize() {

        if (Files.notExists(Paths.get(CACHE_PATH))) {

            if (!new File(CACHE_PATH).mkdir())
                Application.panic(getClass(), "Could not create cache directory!");

        }

        try {

            currentSize = Files.walk(Paths.get(CACHE_PATH))
                               .map(Path::toFile)
                               .mapToLong(File::length)
                               .sum();


        } catch (IOException e) {
            Application.panic(getClass(), "Could not calculate cache size: %s", e.getMessage());
        }

    }


    public void write(Chunk chunk, ByteBuffer byteBuffer, int size) throws IOException {

        byte[] bytes = new byte[size];
        byteBuffer.get(bytes);


        Path path = Paths.get(CACHE_PATH, chunk.getId());

        if(Files.exists(path))
            Files.delete(path);

        Files.write(path, bytes);


        chunk.setCached(true);
        chunk.setHash(Hash.fromFile(path.toFile()));
        chunk.invalidate();

        cache.put(chunk.getId(), path.toFile());

    }

    public void free(Chunk chunk) {
        
        if(cache.containsKey(chunk.getId())) {

            cache.remove(chunk.getId());

            try {

                if (chunk.isCached()) {

                    Path path = Paths.get(CACHE_PATH, chunk.getId());

                    currentSize -= path.toFile().length();
                    Files.delete(path);

                    chunk.setCached(false);

                }

            } catch (IOException ignored) { }

        }

    }

    public boolean verify(Chunk chunk) {
        return true;
    }

    public boolean exist(String id) {
        return cache.containsKey(id);
    }

    public Long getCurrentSize() {
        return currentSize;
    }


    public InputStream read(Chunk chunk) throws FileNotFoundException {

        if(exist(chunk.getId()))
            return new FileInputStream(Paths.get(CACHE_PATH, chunk.getId()).toString());

        throw new FileNotFoundException();

    }
}

