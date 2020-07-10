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

package org.nefele.ui.controls.docker;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class DockerButton extends JFXButton implements Initializable, Themeable {

    private final BooleanProperty selected;
    private final ObjectProperty<DockerItem> item;

    @FXML private MaterialDesignIconView icon;
    @FXML private Tooltip tooltip;


    public DockerButton(DockerItem item) {

        this.selected = new SimpleBooleanProperty(this, "selected", false) {
            @Override
            protected void invalidated() {
                pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), get());
            }
        };

        this.item = new SimpleObjectProperty<>(requireNonNull(item));

        Resources.getFXML(this, "/fxml/controls/DockerButton.fxml");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        icon.glyphNameProperty().bind(getItem().iconProperty());

        if(getItem().getIcon().equals("EXIT_TO_APP"))
            icon.getStyleClass().add("docker-button-exit");


        Application.getInstance().getViews().add(this);
    }


    @Override
    public void initializeInterface() {
        tooltip.setText(Application.getInstance().getLocale().get(getItem().getHint()));
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

    public DockerItem getItem() {
        return item.get();
    }

    public ObjectProperty<DockerItem> itemProperty() {
        return item;
    }

    public void setItem(DockerItem item) {
        this.item.set(item);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DockerButton that = (DockerButton) o;
        return getItem().getReference().equals(that.getItem().getReference());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem().getReference());
    }

}
