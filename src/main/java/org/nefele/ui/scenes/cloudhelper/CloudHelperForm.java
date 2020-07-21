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

package org.nefele.ui.scenes.cloudhelper;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class CloudHelperForm extends StackPane implements Initializable, Themeable {

    private final StringProperty description;
    private final ObjectProperty<Parent> inputType;

    @FXML private StackPane inputPane;
    @FXML private Label labelTitle;
    @FXML private Label labelDescription;


    public CloudHelperForm(String description, Parent inputType) {

        this.description = new SimpleStringProperty(requireNonNull(description));
        this.inputType = new SimpleObjectProperty<>(requireNonNull(inputType));

        Resources.getFXML(this, "/fxml/scenes/cloudhelper/CloudHelperForm.fxml");

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        labelDescription.setUserData(getDescription());
        inputPane.getChildren().add(getInputType());


        Application.getInstance().getViews().add(this);
    }



    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }


    public Parent getInputType() {
        return inputType.get();
    }

    public ObjectProperty<Parent> inputTypeProperty() {
        return inputType;
    }

    public void setInputType(Parent inputType) {
        this.inputType.set(inputType);
    }
}
