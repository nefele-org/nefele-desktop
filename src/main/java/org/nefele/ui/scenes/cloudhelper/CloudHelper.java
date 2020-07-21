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


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;
import org.nefele.ui.base.NefeleContentPane;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;

import java.net.URL;
import java.util.ResourceBundle;

public class CloudHelper extends NefeleContentPane implements Initializable, Themeable {

    private final ObservableList<CloudHelperItem> helperButtons;

    @FXML private VBox contentPane;
    @FXML private VBox titleBox;


    public CloudHelper(){

        helperButtons = FXCollections.observableArrayList();
        Resources.getFXML(this, "/fxml/scenes/cloudhelper/CloudHelper.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        helperButtons.addListener((ListChangeListener<CloudHelperItem>) change -> {

            while (change.next()) {

                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(i ->
                            contentPane.getChildren().add(new CloudHelperButton(i) {{

                                accessProperty().addListener((v, o, n) -> {

                                    if (n) {

                                        JFXTextField name = new JFXTextField(i.getName());

                                        JFXSlider sliderQuota = new JFXSlider() {{

                                            setMin(0.0);
                                            setMax(getDrive().getMaxQuota() / 1024.0 / 1024.0);
                                            setValue((75.0 /100.0) * (getDrive().getMaxQuota() / 1024.0 / 1024.0));

                                        }};


                                        contentPane.getChildren().clear();

                                        titleBox.getChildren().clear();


                                        CloudHelper.this.getScene().getWindow().setWidth(600);
                                        CloudHelper.this.getScene().getWindow().setHeight(400);


                                        titleBox.getChildren().add(new Label() {{

                                            setText(Application.getInstance().getLocale().get("CLOUDHELPER_FORM_TITLE"));
                                            getStyleClass().add("text-h2");

                                        }});


                                        contentPane.getChildren().add(new CloudHelperForm("CLOUDHELPER_FORM_NAME", name));
                                        contentPane.getChildren().add(new CloudHelperForm("CLOUDHELPER_FORM_QUOTA", sliderQuota));


                                        contentPane.getChildren().add(new JFXButton() {{

                                            setText(Application.getInstance().getLocale().get("DIALOG_CONTINUE"));


                                            setOnMouseClicked(e -> {

                                                if (name.getText().trim().isBlank()) {

                                                    Dialogs.showWarningBox("DIALOG_TITLE_WARNING",
                                                            "CLOUDHELPER_FORM_DIALOG_EMPTY_NAME", BaseDialog.DIALOG_OK);


                                                } else if (Dialogs.showWarningBox("DIALOG_TITLE_WARNING", "CLOUDHELPER_FORM_DIALOG", BaseDialog.DIALOG_NO,
                                                        BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES) {

                                                    getDrive().setQuota((long) sliderQuota.getValue() * 1024L * 1024L);
                                                    getDrive().setDescription(name.getText().trim());
                                                    getDrive().invalidate();

                                                    Application.getInstance().getViews().update();
                                                    getScene().getWindow().hide();

                                                }
                                            });

                                        }});

                                    }

                                });
                            }})
                    );


                    if (change.wasRemoved()) {
                        change.getRemoved().forEach(
                                i -> contentPane.getChildren().removeIf(j -> {
                                            if (j instanceof CloudHelperButton)
                                                return ((CloudHelperButton) j).getItem().getService().equals(i.getService());

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

        if(getChildren().isEmpty())
            Application.panic(getClass(), "Drive Helper can not be empty!");

    }

    public ObservableList<CloudHelperItem> getHelperButtons() {
        return helperButtons;
    }
}
