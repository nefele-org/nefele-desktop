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
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.Themeable;
import org.nefele.cloud.DriveNotFoundException;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveProviders;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.dialog.InputDialogResult;

import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class CloudHelperButton extends JFXButton implements Initializable, Themeable {

    private final StringProperty name;
    private final StringProperty iconName;
    private final StringProperty service;
    private final BooleanProperty access;

    private final ObjectProperty<DriveProvider> drive;
    private final ObjectProperty<CloudHelperItem> item;

    @FXML private MaterialDesignIconView icon;
    @FXML private Tooltip tooltip;
    @FXML private Label labelName;

    public CloudHelperButton(CloudHelperItem item) {

        this.service = new SimpleStringProperty(requireNonNull(item.getService()));
        this.name = new SimpleStringProperty(requireNonNull(item.getName()));
        this.iconName = new SimpleStringProperty(requireNonNull(item.getIcon()));
        this.access = new SimpleBooleanProperty(item.isAccess());
        this.item = new SimpleObjectProperty<>(requireNonNull(item));
        this.drive = new SimpleObjectProperty<>();

        Resources.getFXML(this, "/fxml/wizard/CloudHelperButton.fxml");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelName.textProperty().bind(getItem().nameProperty());
        icon.glyphNameProperty().bind(getItem().iconProperty());

        this.setOnMouseClicked(e -> {

            try {

                DriveProvider driveProvider = DriveProviders.getInstance().add(getService());

                while(driveProvider.getStatus() != DriveProvider.STATUS_READY) {

                    if(Dialogs.showErrorBox(
                            "DIALOG_TITLE_ERROR",
                            "CLOUDHELPER_DIALOG_ERROR_LOGIN" ,
                            BaseDialog.DIALOG_RETRY, BaseDialog.DIALOG_ABORT) == BaseDialog.DIALOG_RETRY)
                        driveProvider.initialize();

                    else
                        return;

                }


                if(driveProvider.getStatus() == DriveProvider.STATUS_READY){

                    setDrive(driveProvider);
                    access.setValue(true);

                }


            } catch (DriveNotFoundException driveNotFoundException) {

                Application.panic(getClass(), "Drive not found is impossible!!");

            }

        });

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/cloud-helper-text.css");
        tooltip.setUserData(getItem().getHint());
    }

    public String getName() { return name.get(); }

    public StringProperty nameProperty() { return name; }

    public void setName(String name) { this.name.set(name); }

    public String getIconName() { return iconName.get(); }

    public StringProperty iconNameProperty() { return iconName; }

    public void setIconName(String iconName) { this.iconName.set(iconName); }

    public CloudHelperItem getItem() { return item.get(); }

    public ObjectProperty<CloudHelperItem> itemProperty() { return item; }

    public void setItem(CloudHelperItem item) { this.item.set(item); }

    public String getService() { return service.get(); }

    public StringProperty serviceProperty() { return service; }

    public boolean isAccess() { return access.get(); }

    public BooleanProperty accessProperty() { return access; }

    public DriveProvider getDrive() {
        return drive.get();
    }

    public ObjectProperty<DriveProvider> driveProperty() {
        return drive;
    }

    public void setDrive(DriveProvider drive) {
        this.drive.set(drive);
    }
}
