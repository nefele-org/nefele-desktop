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
import org.nefele.core.Mime;
import org.nefele.core.Mimes;
import org.nefele.utils.Tree;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

public class MergeFileTree {

    private final Tree<MergeNode> tree;
    private final MergeFileSystem fileSystem;

    public MergeFileTree(MergeFileSystem fileSystem) {

        this.tree = new Tree<>(
                new MergeNode(MergeFileSystem.ROOT, "directory", 0, Instant.now(), Instant.now(), Instant.now(), false, Instant.now(), "", "")
        );

        this.fileSystem = fileSystem;

        fetchNode(tree.getData().getId(), tree);
    }


    private void fetchNode(String parent, Tree<MergeNode> inodeTree) {

        fileSystem.getCache().getInodes()
                .values()
                .stream()
                .filter(i -> i.getParent().equals(parent))
                .forEach(inodeTree::add);

        inodeTree.getChildren()
                .stream()
                .filter(i -> i.getData().getMime().equals("directory"))
                .forEach(i -> fetchNode(i.getData().getId(), i));

    }


    public Tree<MergeNode> getTree() {
        return tree;
    }



    public Tree<MergeNode> resolve(String[] paths) {

        Tree<MergeNode> tree = getTree();

        for(int i = 0; i < paths.length; i++) {

            final String path = paths[i];

            if(path.isEmpty())
                continue;

            Tree<MergeNode> child = requireNonNull(tree).findIf (
                    j -> j.getData().getName().equals(path)
            );

            if(child == null) {

                if(i != paths.length - 1)
                    throw new IllegalStateException();

                child = new Tree<>(tree, fileSystem.getCache().alloc(tree.getData(), path, Mime.UNKNOWN.getType()));

            }

            tree = child;

        }


        return requireNonNull(tree);

    }

    public String toAbsolutePath(Tree<MergeNode> entry) {

        final ArrayList<String> names = new ArrayList<>();

        for(; entry != tree; entry = entry.getParent())
            names.add(entry.getData().getName());

        Collections.reverse(names);

        return MergeFileSystem.PATH_SEPARATOR + String.join(MergeFileSystem.PATH_SEPARATOR, names);
    }
}
