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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;
import org.nefele.ui.base.NefeleContentPane;
import org.nefele.ui.base.NefelePane;
import org.nefele.ui.controls.docker.Docker;
import org.nefele.ui.controls.docker.DockerItem;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.scenes.archive.Archive;
import org.nefele.ui.scenes.drivemanager.DriveManager;
import org.nefele.ui.scenes.settings.Settings;
import org.nefele.ui.scenes.stats.Stats;
import org.nefele.ui.scenes.transferviewer.TransferViewer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;


public class Home extends NefeleContentPane implements Initializable, Themeable {

    @FXML private Docker dockerPane;
    @FXML private AnchorPane contentPane;


    public Home() {
        Resources.getFXML(this, "/fxml/scenes/Home.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dockerPane.getParents().add(new DockerItem(new Archive(), "ARCHIVE", "DOCKER_BUTTON_HINT_ARCHIVE"));
        dockerPane.getParents().add(new DockerItem(new TransferViewer(), "CLOUD_SYNC", "DOCKER_BUTTON_HINT_TRANSFERVIEWER"));
        dockerPane.getParents().add(new DockerItem(new Stats(), "CHART_BAR", "DOCKER_BUTTON_HINT_STATS"));
        dockerPane.getParents().add(new DockerItem(new DriveManager(), "LAYERS", "DOCKER_BUTTON_HINT_DRIVEMANAGER"));
        dockerPane.getParents().add(new DockerItem(new Settings(), "SETTINGS", "DOCKER_BUTTON_HINT_SETTINGS"));
        dockerPane.getParents().add(new DockerItem(new StackPane(), "EXIT_TO_APP", "DOCKER_BUTTON_HINT_EXIT"));

        dockerPane.setContentPane(contentPane);

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

        ((NefelePane) getScene().getRoot()).setOnClosing(() -> {

            int e = Dialogs.showInfoBox("HOME_DIALOG_TITLE","HOME_DIALOG_DESCRIPTION",
                    BaseDialog.DIALOG_ABORT, BaseDialog.DIALOG_MINIMIZE, BaseDialog.DIALOG_EXIT);

            switch (e) {

                case BaseDialog.DIALOG_ABORT:
                case BaseDialog.DIALOG_CLOSED:
                    break;

                case BaseDialog.DIALOG_EXIT:
                    Application.getInstance().exit();
                    break;

                case BaseDialog.DIALOG_MINIMIZE:

                    if(!SystemTray.isSupported()) {
                        Dialogs.showErrorBox("SYSTEM_TRAY_ERROR");
                        return false;
                    }

                    Image trayImage = null;
                    try {
                        trayImage = ImageIO.read(Resources.getURL(this, "/images/trayicon.png"));
                    } catch (IOException ioException) {
                        Application.panic(getClass(), ioException);
                    }


                    PopupMenu popupMenu = new PopupMenu();
                    
                    popupMenu.add(new MenuItem(Application.getInstance().getLocale().get("SYSTEM_TRAY_SHOW")) {{
                        addActionListener(e -> Platform.runLater(() -> ((Stage) getScene().getWindow()).show()));
                    }});

                    popupMenu.add(new MenuItem(Application.getInstance().getLocale().get("SYSTEM_TRAY_EXIT")) {{
                        addActionListener(e -> Platform.runLater(() -> Application.getInstance().exit()));
                    }});

                    popupMenu.setName("Nefele");
                    popupMenu.setLabel("Nefele");


                    try {

                        SystemTray.getSystemTray().add(new TrayIcon(requireNonNull(trayImage), "Nefele", popupMenu) {{

                            setImageAutoSize(true);

                            addActionListener(e -> Platform.runLater(() -> {

                                ((Stage) getScene().getWindow()).show();
                                SystemTray.getSystemTray().remove(this);

                            }));

                        }});

                    } catch (AWTException awtException) {
                        Dialogs.showErrorBox("SYSTEM_TRAY_ERROR");
                        return false;
                    }

                    return true;


            }

            return false;

        });


    }
}
