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

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.Themeable;
import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class CloudHelperButton extends StackPane implements Initializable, Themeable {

    private final StringProperty name;
    private final StringProperty iconName;
    private final ObjectProperty<CloudHelperItem> item;

    @FXML private MaterialDesignIconView icon;
    @FXML private Label labelName;
    @FXML private Tooltip tooltip;

    public CloudHelperButton(CloudHelperItem item) {

        this.name = new SimpleStringProperty(requireNonNull(item.getName()));
        this.iconName = new SimpleStringProperty(requireNonNull(item.getIcon()));
        this.item = new SimpleObjectProperty<>(requireNonNull(item));

        Resources.getFXML(this, "/fxml/CloudHelperButton.fxml");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelName.textProperty().bind(getItem().nameProperty());
        icon.glyphNameProperty().bind(getItem().iconProperty());

        this.setOnMouseClicked(e ->{
            /* TODO... */
        });

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }

    public String getName() { return name.get(); }

    public StringProperty nameProperty() { return name; }

    public void setName(String name) { this.name.set(name); }

    public String getIconName() { return iconName.get(); }

    public StringProperty iconNameProperty() { return iconName; }

    public void setIconName(String iconName) { this.iconName.set(iconName); }

    public CloudHelperItem getItem() {
        return item.get();
    }

    public ObjectProperty<CloudHelperItem> itemProperty() {
        return item;
    }

    public void setItem(CloudHelperItem item) {
        this.item.set(item);
    }
}
