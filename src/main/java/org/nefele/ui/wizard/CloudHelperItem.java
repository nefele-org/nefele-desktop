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

package org.nefele.ui.wizard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static java.util.Objects.requireNonNull;

public class CloudHelperItem {

    private final StringProperty service;
    private final StringProperty name;
    private final StringProperty icon;
    private final StringProperty hint;
    private final BooleanProperty access;

    public CloudHelperItem(String service, String name, String icon, String hint) {

        this.service = new SimpleStringProperty(requireNonNull(service));
        this.name = new SimpleStringProperty(requireNonNull(name));
        this.icon = new SimpleStringProperty(requireNonNull(icon));
        this.hint = new SimpleStringProperty(requireNonNull(hint));
        this.access = new SimpleBooleanProperty(false);

    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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

    public String getService() {
        return service.get();
    }

    public StringProperty serviceProperty() {
        return service;
    }

    public boolean isAccess() { return access.get(); }

    public void setAccess(boolean access) { this.access.set(access); }

    public BooleanProperty accessProperty() { return access; }

    public void setService(String service) { this.service.set(service); }
}
