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
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.Drive;
import org.nefele.ui.Themeable;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;



public class SettingsDriveRecord extends StackPane implements Initializable, Themeable {
    
    private final ReadOnlyObjectProperty<Drive> drive;

    @FXML private Label labelName;
    @FXML private JFXToggleButton toggleState;
    @FXML private MaterialDesignIconView buttonDelete;
    @FXML private JFXSlider sliderChunks;

    public SettingsDriveRecord(Drive drive) {

        this.drive = new SimpleObjectProperty<>(requireNonNull(drive));

        Resources.getFXML(this, "/fxml/SettingsDriveRecord.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelName.textProperty().bind(getDrive().descriptionProperty());


        toggleState.setSelected(getDrive().getStatus() == Drive.STATUS_READY);

        toggleState.selectedProperty().addListener((v, o, n) -> {
            getDrive().setStatus(n ? Drive.STATUS_READY : Drive.STATUS_DISABLED);
            getDrive().invalidate();
        });


        buttonDelete.setOnMouseClicked(e -> {
            /* TODO...*/
        });

        sliderChunks.setMin(1.0);
        sliderChunks.setMax(getDrive().getMaxQuota());
        sliderChunks.setValue(getDrive().getQuota());

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/transferviewer-cell.css");
    }

    public Drive getDrive() {
        return drive.get();
    }

    public ReadOnlyObjectProperty<Drive> driveProperty() {
        return drive;
    }
}
