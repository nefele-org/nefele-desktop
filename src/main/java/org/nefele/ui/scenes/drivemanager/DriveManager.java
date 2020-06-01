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
import javafx.application.Platform;
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
import org.nefele.core.Resources;
import org.nefele.Themeable;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveProviders;
import org.nefele.ui.controls.NefelePane;
import org.nefele.ui.wizard.Wizard;

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


        buttonAdd.setOnMouseClicked(e ->
            Platform.runLater(() -> {

                Stage s = new Stage();


                NefelePane nefelePane = new NefelePane(new Wizard());
                nefelePane.setModal(NefelePane.MODAL_UNDECORATED);
                nefelePane.setShowDarkMode(false);
                nefelePane.setShowLogo(true);
                nefelePane.setShowStatusBar(false);

                s.setHeight(420);
                s.setWidth(580);
                s.setScene(new Scene(nefelePane));
                s.setTitle("Nefele Wizard");
                s.getIcons().add(new Image(Resources.getURL(this, "/images/trayicon.png").toExternalForm()));
                s.initModality(Modality.APPLICATION_MODAL);
                s.initStyle(StageStyle.UNDECORATED);
                s.show();

                nefelePane.setResizable(false);
            })
        );

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
