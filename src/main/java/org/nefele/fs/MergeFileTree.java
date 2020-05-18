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
import org.nefele.utils.Tree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

public class MergeFileTree {

    private final Tree<Inode> tree;

    public MergeFileTree() {

        tree = new Tree<>(
                new Inode(MergeFileSystem.PATH_SEPARATOR, "directory", "", "")
        );

        fetchNode(tree.getData().getId(), tree);
    }


    private void fetchNode(String parent, Tree<Inode> inodeTree) {


        try {
            Application.getInstance().getDatabase().query("SELECT * FROM inodes WHERE parent = ?",
                    s -> s.setString(1, parent),
                    r -> {
                            inodeTree.add(new Inode(
                                    r.getString(1),
                                    r.getString(2),
                                    r.getLong(3),
                                    r.getLong(4),
                                    r.getLong(5),
                                    r.getLong(6),
                                    r.getBoolean(7),
                                    r.getLong(8),
                                    r.getString(9),
                                    r.getString(10)
                            ));

                    });

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        inodeTree.getChildren()
                .stream()
                .filter(i -> i.getData().getMime().equals("directory"))
                .forEach(i -> fetchNode(i.getData().getId(), i));



    }

    public Tree<Inode> getTree() {
        return tree;
    }



    public Tree<Inode> resolve(String[] paths) {

        Tree<Inode> tree = getTree();

        for(int i = 0; i < paths.length; i++) {

            final String path = paths[i];

            if(path.isEmpty())
                continue;

            Tree<Inode> child = requireNonNull(tree).findIf (
                    j -> j.getData().getName().equals(path)
            );

            if(child == null) {

                if(i != paths.length - 1)
                    throw new IllegalStateException();

                try {
                    child = new Tree<>(tree, Inode.alloc(path, tree.getData().getId()));
                } catch (IOException e) {
                    throw new IllegalArgumentException();
                }

            }

            tree = child;

        }


        return requireNonNull(tree);

    }

    public String toAbsolutePath(Tree<Inode> entry) {

        final ArrayList<String> names = new ArrayList<>();

        for(; entry != tree; entry = entry.getParent())
            names.add(entry.getData().getName());

        Collections.reverse(names);

        return MergeFileSystem.PATH_SEPARATOR + String.join(MergeFileSystem.PATH_SEPARATOR, names);
    }
}
