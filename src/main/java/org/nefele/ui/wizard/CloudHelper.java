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


import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.Themeable;
import org.nefele.ui.controls.NefeleContentPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CloudHelper extends NefeleContentPane implements Initializable, Themeable {

    private final ObservableList<CloudHelperItem> helperButtons;

    @FXML private VBox contentPane;


    public CloudHelper(){

        helperButtons = FXCollections.observableArrayList();
        Resources.getFXML(this, "/fxml/wizard/CloudHelper.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        helperButtons.addListener((ListChangeListener<CloudHelperItem>) change -> {

                while (change.next()) {

                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(i ->
                                getChildren().add(new CloudHelperButton(i) {{
                                    /* TODO... */
                                }}));


                    if (change.wasRemoved()) {
                        change.getRemoved().forEach(
                                i -> getChildren().removeIf(j -> {
                                            if (j instanceof CloudHelperButton)
                                                return ((CloudHelperButton) j).getItem().getReference().equals(i.getReference());

                                            return false;
                                        }
                                )
                        );
                    }

                }

            }
        });

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }

    public ObservableList<CloudHelperItem> getHelperButtons() {
        return helperButtons;
    }
}
