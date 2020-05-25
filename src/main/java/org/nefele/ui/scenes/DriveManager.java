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

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.Drive;
import org.nefele.cloud.Drives;
import org.nefele.ui.Themeable;
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


        Drives.getInstance().getDrives().addListener((ListChangeListener<? super Drive>) change -> {

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


        Drives.getInstance().getDrives().forEach(
                i -> getDriveManagerBoxes().add(new DriveManagerBox(i)));


        buttonAdd.setOnMouseClicked(e ->{
            Platform.runLater(() -> {

                ((Stage)getScene().getWindow()).setScene(new Scene(new NefelePane(new Wizard())));
                ((Stage)getScene().getWindow()).setMinWidth(600);
                ((Stage)getScene().getWindow()).setMinHeight(400);
                getScene().getWindow().setWidth(800);
                getScene().getWindow().setHeight(480);

            });
        });

        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() { }

    public ObservableList<DriveManagerBox> getDriveManagerBoxes() {
        return driveManagerBoxes;
    }
}
