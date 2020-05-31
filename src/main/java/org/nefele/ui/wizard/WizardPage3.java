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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveProviders;
import org.nefele.cloud.providers.DropboxDriveProvider;
import org.nefele.cloud.providers.GoogleDriveProvider;
import org.nefele.cloud.providers.OfflineDriveProvider;
import org.nefele.ui.controls.NefelePane;

import java.net.URL;
import java.util.ResourceBundle;

public class WizardPage3 extends WizardPage {

    @FXML private JFXButton buttonAddCloud;

    public WizardPage3(Parent wizardRoot) {

        super(wizardRoot);

        Resources.getFXML(this, "/fxml/wizard/WizardPage3.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonAddCloud.setOnMouseClicked(e ->
            Platform.runLater(() -> {


                NefelePane nefelePane = new NefelePane(new CloudHelper() {{


                    getHelperButtons().add(new CloudHelperItem(
                            OfflineDriveProvider.SERVICE_ID,
                            OfflineDriveProvider.SERVICE_DEFAULT_DESCRIPTION, "LAYERS", "DRIVE_OFFLINE_HINT"));

                    getHelperButtons().add(new CloudHelperItem(
                            GoogleDriveProvider.SERVICE_ID,
                            GoogleDriveProvider.SERVICE_DEFAULT_DESCRIPTION, "GOOGLE_DRIVE", "DRIVE_GOOGLE_DRIVE_HINT"));

                    getHelperButtons().add(new CloudHelperItem(
                            DropboxDriveProvider.SERVICE_ID,
                            DropboxDriveProvider.SERVICE_DEFAULT_DESCRIPTION, "DROPBOX", "DRIVE_DROPBOX_HINT"));


                }});


                nefelePane.setPrefWidth(400);
                nefelePane.setModal(NefelePane.MODAL_DIALOG);
                nefelePane.setShowDarkMode(false);
                nefelePane.setShowLogo(true);
                nefelePane.setShowStatusBar(false);
                nefelePane.setResizable(false);

                Stage stage = new Stage();
                Scene scene = new Scene(nefelePane);

                stage.setScene(scene);
                stage.setTitle("Nefele");
                stage.getIcons().add(new Image(Resources.getURL(this, "/images/trayicon.png").toExternalForm()));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNDECORATED);

                stage.showAndWait();

                if(DriveProviders.getInstance().getDriveProviders().stream().anyMatch(i -> i.getStatus() == DriveProvider.STATUS_READY)) {

                    checkedProperty().setValue(true);
                    ((Wizard) super.getWizardRoot()).getButtonForward().fire();

                }

            })
        );


        Application.getInstance().getViews().add(this);
    }

}
