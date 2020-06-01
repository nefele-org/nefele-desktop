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

package org.nefele.ui.controls.filebrowser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;
import org.nefele.core.Mime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class FileBrowserItem {

    private final ObjectProperty<Mime> mime;
    private final StringProperty text;
    private List<MenuItem> menuItems;

    public FileBrowserItem(Mime mime, String text) {
        this.mime = new SimpleObjectProperty<>(requireNonNull(mime));
        this.text = new SimpleStringProperty(requireNonNull(text));
        this.menuItems = new ArrayList<>();
    }

    public FileBrowserItem(Mime mime, String text, boolean shared) {
        this.mime = new SimpleObjectProperty<>(requireNonNull(mime));
        this.text = new SimpleStringProperty(requireNonNull(text));
        this.menuItems = new ArrayList<>();
    }



    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public Mime getMime() {
        return mime.get();
    }

    public ObjectProperty<Mime> mimeProperty() {
        return mime;
    }

    public void setMime(Mime mime) {
        this.mime.set(mime);
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBrowserItem that = (FileBrowserItem) o;
        return getMime().equals(that.getMime()) &&
                getText().equals(that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMime(), getText());
    }
}
