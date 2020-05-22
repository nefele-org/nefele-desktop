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

package org.nefele.ui.scenes;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.Themeable;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class SettingsDriveRecord extends StackPane implements Initializable, Themeable {

    @FXML private Label labelName;
    @FXML private JFXSlider sliderChunk;
    @FXML private JFXCheckBox checkBoxState;
    @FXML private MaterialDesignIconView buttonDelete;

    private final StringProperty name;
    private final BooleanProperty state;
    private final IntegerProperty value;

    public SettingsDriveRecord(String name, boolean state, Integer value ) {

        this.name = new SimpleStringProperty(requireNonNull(name));
        this.state = new SimpleBooleanProperty(requireNonNull(state));
        this.value = new SimpleIntegerProperty(requireNonNull(value));

        Resources.getFXML(this, "/fxml/SettingsDriveRecord.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public boolean isActive() {
        return state.get();
    }

    public BooleanProperty stateProperty() {
        return state;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setValue(boolean state) {
        this.state.set(state);
    }

    public int getValue() {
        return value.get();
    }

    public IntegerProperty valueProperty() {
        return value;
    }

    public void setValue(int value) {
        this.value.set(value);
    }
}
