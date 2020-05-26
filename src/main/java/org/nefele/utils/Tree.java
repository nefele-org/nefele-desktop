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

package org.nefele.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class Tree<T> {

    private final AtomicReference<T> data;
    private final AtomicReference<Tree<T>> parent;
    private final ArrayList<Tree<T>> children;

    public Tree() {
        this.data = new AtomicReference<>(null);
        this.parent = new AtomicReference<>(null);
        this.children = new ArrayList<>();
    }

    public Tree(T data) {
        this.data = new AtomicReference<>(data);
        this.parent = new AtomicReference<>(null);
        this.children = new ArrayList<>();
    }

    public Tree(Tree<T> parent, T data) {

        this.data = new AtomicReference<>(data);
        this.parent = new AtomicReference<>(parent);
        this.children = new ArrayList<>();

        if(parent != null)
            parent.add(this);

    }

    @SafeVarargs
    public final void add(Tree<T>... trees) {
        synchronized (children) {
            children.addAll(Arrays.asList(trees));
        }
    }

    public final Tree<T> add(T data) {
        return new Tree<>(this, data);
    }

    @SafeVarargs
    public final void remove(Tree<T>... trees) {
        synchronized (children) {
            children.removeAll(Arrays.asList(trees));
        }
    }

    public final void remove(int index) {
        synchronized (children) {
            children.remove(index);
        }
    }

    public final void remove(T data) {
        synchronized (children) {
           children.removeIf(i -> i.getData().equals(data));
        }
    }

    public final void removeIf(Predicate<? super Tree<T>> predicate) {
        synchronized (children) {
            children.removeIf(predicate);
        }
    }

    public final Tree<T> findIf(Predicate<? super Tree<T>> predicate) {

        synchronized (children) {

            return children
                    .stream()
                    .filter(predicate)
                    .findFirst()
                        .orElse(null);

        }

    }

    public T getData() {
        return data.get();
    }

    public Tree<T> getParent() {
        return parent.get();
    }

    public ArrayList<Tree<T>> getChildren() {
        return children;
    }

    public void setData(T data) {
        this.data.set(data);
    }

    public void setParent(Tree<T> parent) {
        this.parent.set(parent);
    }

}
