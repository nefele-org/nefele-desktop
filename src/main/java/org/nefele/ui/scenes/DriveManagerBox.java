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

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.Drive;
import org.nefele.cloud.DriveNotEmptyException;
import org.nefele.cloud.Drives;
import org.nefele.ui.Themeable;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;



public class DriveManagerBox extends StackPane implements Initializable, Themeable {
    
    private final ReadOnlyObjectProperty<Drive> drive;

    @FXML private Label labelName;
    @FXML private JFXToggleButton toggleState;
    @FXML private MaterialDesignIconView buttonDelete;
    @FXML private JFXSlider sliderChunks;

    public DriveManagerBox(Drive drive) {

        this.drive = new SimpleObjectProperty<>(requireNonNull(drive));

        Resources.getFXML(this, "/fxml/DriveManagerBox.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelName.textProperty().bind(getDrive().descriptionProperty());


        toggleState.setSelected(getDrive().getStatus() == Drive.STATUS_READY);

        toggleState.selectedProperty().addListener((v, o, n) -> {

            switch (getDrive().getStatus()) {

                case Drive.STATUS_READY:
                case Drive.STATUS_DISABLED:

                    getDrive().setStatus(n ? Drive.STATUS_READY : Drive.STATUS_DISABLED);
                    getDrive().invalidate();
                    break;

                default:
                    toggleState.setSelected(false);
                    break;

            }

        });


        buttonDelete.setOnMouseClicked(e -> {

            Platform.runLater(() -> {

                try {

                    if (Dialogs.showInfoBox("SETTINGS_DRIVE_DIALOG_TITLE", "SETTINGS_DRIVE_DIALOG_DESCRIPTION",
                            BaseDialog.DIALOG_NO, BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES)
                        Drives.getInstance().remove(getDrive());

                } catch (DriveNotEmptyException empty) {
                    Dialogs.showErrorBox("SETTINGS_DRIVE_ERROR_DIALOG_DESCRIPTION");
                }


            });

        });


        sliderChunks.minProperty().bind(getDrive().chunksProperty());
        sliderChunks.setMax(getDrive().getMaxQuota());
        sliderChunks.setValue(getDrive().getQuota());

        sliderChunks.valueChangingProperty().addListener((v, o, n) -> {

            if (!n) {

                getDrive().setQuota(((Double) sliderChunks.getValue()).longValue());
                getDrive().invalidate();

            }
        });


        sliderChunks.disableProperty().bind(
                Bindings
                        .when(getDrive().statusProperty().isNotEqualTo(Drive.STATUS_READY)
                                .and(getDrive().statusProperty().isNotEqualTo(Drive.STATUS_DISABLED)))
                        .then(true)
                        .otherwise(false)
        );

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/drivemanager-box.css");
    }

    public Drive getDrive() {
        return drive.get();
    }

    public ReadOnlyObjectProperty<Drive> driveProperty() {
        return drive;
    }
}