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

import com.google.common.collect.ImmutableSet;
import org.nefele.Application;
import org.nefele.Mime;
import org.nefele.Mimes;
import org.nefele.utils.IdUtils;

import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MergeFileTree {

    private final MergeFileSystem fileSystem;
    private final MergeNode rootNode;

    public MergeFileTree(MergeFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.rootNode = new MergeNode(MergeFileSystem.ROOT, "directory", 0, Instant.now(), Instant.now(), Instant.now(), "", "");
    }


    public MergeNode resolve(String[] paths) throws MergeFileSystemException {

        MergeNode walker = rootNode;
        String parent = "";
        int index = 0;

        for(var path : paths) {

            index++;

            if(path.isEmpty())
                continue;


            final var currentParent = parent;

            walker = fileSystem.getStorage().getInodes()
                    .stream()
                    .filter(i -> i.getName().equals(path) && i.getParent().equals(currentParent))
                    .findFirst()
                    .orElse(null);


            if(walker == null) {

                if(index == paths.length)
                    walker = new MergeNode(path, Mime.UNKNOWN.getType(), 0, Instant.now(), Instant.now(), Instant.now(), IdUtils.generateId(), currentParent);
                else
                    throw new MergeFileSystemException("resolve failed: " + String.join(MergeFileSystem.PATH_SEPARATOR, paths));

            }

            parent = walker.getId();

        }


        return requireNonNull(walker, "BUG! Walker cannot be null, impossible!");

    }

    public MergeNode resolve(String id) throws MergeFileSystemException {

        if(id.isEmpty())
            return null;

        var result = fileSystem.getStorage().getInodes()
                .stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElse(null);

        if(result == null)
            throw new MergeFileSystemException("resolve failed: " + id);

        return result;

    }

    public String toAbsolutePath(MergeNode entry) throws MergeFileSystemException {

        var paths = new ArrayList<String>() {{
            add(entry.getName());
        }};

        for(var parent = resolve(entry.getParent());
                parent != null;
                parent = resolve(parent.getParent())) {

            paths.add(parent.getName());
        }

        Collections.reverse(paths);

        return MergeFileSystem.ROOT + String.join(MergeFileSystem.PATH_SEPARATOR, paths);

    }

    public Set<MergeNode> listChildren(MergeNode inode) {

        return fileSystem.getStorage().getInodes()
                .stream()
                .filter(i -> i.getParent().equals(inode.getId()))
                .collect(Collectors.toUnmodifiableSet());

    }
}
