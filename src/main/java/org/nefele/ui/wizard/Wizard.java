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

import com.jfoenix.controls.JFXButton;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.Themeable;
import org.nefele.ui.controls.NefeleContentPane;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Wizard extends NefeleContentPane implements Initializable, Themeable {

    @FXML private AnchorPane wizardViewer;
    @FXML private JFXButton buttonForward;

    private final ArrayList<WizardPage> parents;
    private final ObjectProperty<WizardPage> currentPage;
    private int currentIndex;


    public Wizard(){

        parents = new ArrayList<>();
        currentPage = new SimpleObjectProperty<>(null);
        currentIndex = 0;

        Resources.getFXML(this, "/fxml/wizard/Wizard.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        parents.add(new WizardPage1());
        parents.add(new WizardPage2());
        parents.add(new WizardPage3());
        parents.add(new WizardPage4());


        currentPage.addListener((v, o, n) ->{

            if(o != null) {

                buttonForward.disableProperty().unbind();
                wizardViewer.getChildren().remove(o);

            }

            buttonForward.disableProperty().bind(n.checkedProperty().not());
            wizardViewer.getChildren().add(n);

        });


        buttonForward.setOnAction(e -> {

            if(++currentIndex < parents.size())
                currentPage.setValue(parents.get(currentIndex));

        });


        currentPage.setValue(parents.get(currentIndex));

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }

    public ArrayList<WizardPage> getParents() {
        return parents;
    }

}
