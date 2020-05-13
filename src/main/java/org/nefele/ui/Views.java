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

package org.nefele.ui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.util.Pair;
import org.nefele.Application;
import org.nefele.Resources;

import java.util.ArrayList;

import static java.util.Objects.requireNonNull;



public final class Views {

    private final ArrayList<Pair<Parent, Runnable>> registeredViews;


    public Views() {
        registeredViews = new ArrayList<>();
    }


    private synchronized <T extends Parent> void updateView(T parent, Runnable runnable) {

        parent.setStyle(null);
        parent.setStyle(Application.getInstance().getTheme().getStyle());


        // translate()
        Application.getInstance().getLocale().translate(parent);

        // initializeInterface()
        Platform.runLater(runnable);

    }



    public synchronized void update() {

        registeredViews.forEach(i ->
                updateView(i.getKey(), i.getValue()));

    }



    public synchronized <T extends Parent & Themeable> void add(T themeable) {
        add(themeable, themeable::initializeInterface);
    }

    public synchronized <T extends Parent> void add(T parent, Runnable runnable) {

        if(registeredViews.add(new Pair<>(parent, runnable)))
            Application.log(getClass(), "Registered View %s", parent.getClass().getName());

        Resources.getCSS(parent, "/css/base.css");
        updateView(parent, runnable);

    }

    public synchronized void remove(Parent parent) {

        if(registeredViews.removeIf(i -> i.getKey().equals(parent)))
            Application.log(getClass(), "Unregistered View %s", parent.getClass().getName());

    }

}
