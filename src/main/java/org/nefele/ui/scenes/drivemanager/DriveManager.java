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

package org.nefele.ui.scenes.drivemanager;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveProviders;
import org.nefele.cloud.providers.DropboxDriveProvider;
import org.nefele.cloud.providers.GoogleDriveProvider;
import org.nefele.cloud.providers.OfflineDriveProvider;
import org.nefele.core.Resources;
import org.nefele.ui.controls.NefelePane;
import org.nefele.ui.scenes.cloudhelper.CloudHelper;
import org.nefele.ui.scenes.cloudhelper.CloudHelperItem;

import java.net.URL;
import java.util.ResourceBundle;

public class DriveManager extends StackPane implements Initializable, Themeable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox contentPane;
    @FXML private JFXButton buttonAdd;

    private final ObservableList<DriveManagerBox> driveManagerBoxes;

    public DriveManager(){

        this.driveManagerBoxes = FXCollections.observableArrayList();
        Resources.getFXML(this, "/fxml/DriveManager.fxml");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        DriveProviders.getInstance().getDriveProviders().addListener((ListChangeListener<? super DriveProvider>) change -> {

            while (change.next()) {

                if (change.wasRemoved())
                    change.getRemoved().forEach(i -> contentPane.getChildren().removeIf(j -> (((DriveManagerBox) j).getDrive().getId().equals(i.getId()))));

                if (change.wasAdded())
                    change.getAddedSubList().forEach(i -> contentPane.getChildren().add(new DriveManagerBox(i)));

            }

        });


        driveManagerBoxes.addListener((ListChangeListener<? super DriveManagerBox>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(contentPane.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(contentPane.getChildren()::add);

            }

        });


        DriveProviders.getInstance().getDriveProviders().forEach(
                i -> getDriveManagerBoxes().add(new DriveManagerBox(i)));


        buttonAdd.setOnMouseClicked(e -> {

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


            nefelePane.setPrefWidth(600);
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
            stage.setWidth(600);
            stage.setHeight(400);

            stage.showAndWait();

        });

        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/docker-button.css");
    }

    public ObservableList<DriveManagerBox> getDriveManagerBoxes() {
        return driveManagerBoxes;
    }
}
