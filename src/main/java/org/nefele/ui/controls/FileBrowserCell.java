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

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import org.nefele.Resources;
import org.nefele.ui.Themeable;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class FileBrowserCell extends BorderPane implements Initializable, Themeable {

    @FXML private MaterialDesignIconView icon;
    @FXML private Label label;
    @FXML private Tooltip tooltip;

    private final FileBrowserItem item;
    private final BooleanProperty selected;


    public FileBrowserCell(FileBrowserItem item) {

        this.item = item;
        this.selected = new SimpleBooleanProperty(false) {
            @Override
            protected void invalidated() {
                pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), get());
            }
        };

        Resources.getFXML(this, "/fxml/controls/FileBrowserCell.fxml");
    }

    @Override
    public void initializeInterface() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        icon.glyphNameProperty().bind(item.getMime().iconProperty());
        tooltip.textProperty().bind(item.textProperty().concat(" (").concat(item.getMime().descriptionProperty()).concat(")"));
        label.textProperty().bind(item.textProperty());

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBrowserCell that = (FileBrowserCell) o;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }

    @Override
    public String toString() {
        return item.getText();
    }


    public FileBrowserItem getItem() {
        return item;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

}
