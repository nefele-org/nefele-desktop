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

package org.nefele.ui.scenes.settings;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class SettingsAdvancedRecord extends StackPane implements Initializable, Themeable {

    private final StringProperty name;
    private final StringProperty value;

    @FXML private Label labelName;
    @FXML private JFXTextField textFieldValue;

    public SettingsAdvancedRecord(String name, String value ) {

        this.name = new SimpleStringProperty(requireNonNull(name));
        this.value = new SimpleStringProperty(requireNonNull(value));

        Resources.getFXML(this, "/fxml/SettingsAdvancedRecord.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelName.setText(getName());
        textFieldValue.setText(getValue());

        Application.getInstance().getViews().add(this);
    }


    public String getName() { return name.get(); }

    public StringProperty nameProperty() { return name; }

    public void setName(String name) { this.name.set(name); }

    public String getValue() { return value.get(); }

    public StringProperty valueProperty() { return value; }

    public void setValue(String value) { this.value.set(value); }

    public JFXTextField getTextFieldValue() {
        return textFieldValue;
    }
}
