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

package org.nefele.ui.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;

import static java.util.Objects.requireNonNull;

public final class DockerItem {

    private final ObjectProperty<Parent> reference;
    private final StringProperty icon;
    private final StringProperty hint;

    public DockerItem(Parent parent, String icon, String hint) {
        this.reference = new SimpleObjectProperty<>(requireNonNull(parent));
        this.icon = new SimpleStringProperty(requireNonNull(icon));
        this.hint = new SimpleStringProperty(requireNonNull(hint));
    }


    public Parent getReference() {
        return reference.get();
    }

    public ObjectProperty<Parent> referenceProperty() {
        return reference;
    }

    public void setReference(Parent reference) {
        this.reference.set(reference);
    }

    public String getIcon() {
        return icon.get();
    }

    public StringProperty iconProperty() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon.set(icon);
    }

    public String getHint() {
        return hint.get();
    }

    public StringProperty hintProperty() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint.set(hint);
    }
}
