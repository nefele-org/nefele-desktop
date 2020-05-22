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

import com.fasterxml.jackson.databind.type.PlaceholderForType;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.Mime;
import org.nefele.core.Mimes;
import org.nefele.core.UploadTransferInfo;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergePath;
import org.nefele.ui.Themeable;
import org.nefele.ui.controls.FileBrowser;
import org.nefele.ui.controls.FileBrowserItem;
import org.nefele.ui.controls.FileBrowserItemFactory;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;

import java.io.*;
import java.lang.management.PlatformManagedObject;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class Archive extends StackPane implements Initializable, Themeable {

    @FXML private JFXTextField textFieldPath;
    @FXML private JFXButton buttonForward;
    @FXML private JFXButton buttonBack;
    @FXML private JFXButton buttonHome;
    @FXML private JFXButton buttonUpFile;
    @FXML private JFXButton buttonAddFolder;
    @FXML private JFXButton buttonUpFolder;
    @FXML private FileBrowser fileBrowser;


    public Archive() {
        Resources.getFXML(this, "/fxml/Archive.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        textFieldPath.setFocusTraversable(false);

        buttonForward.setOnMouseClicked(e -> {
            fileBrowser.browseHistory(1);
        });

        buttonBack.setOnMouseClicked(e -> {
            fileBrowser.browseHistory(-1);
        });


        buttonHome.setOnMouseClicked(e -> {
            fileBrowser.setCurrentPath(Path.of(URI.create("cloud:///")));
        });


        buttonUpFile.setOnMouseClicked(e -> {

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(getScene().getWindow());


            if(file == null)
                Dialogs.showErrorBox("ARCHIVE_DIALOG_FILE_NOTSELECTED");

            else if(!file.isFile())
                Dialogs.showErrorBox("ARCHIVE_DIALOG_FILE_INVALID");

            else {

                Application.getInstance().runThread(new Thread(() -> {

                    try {

                        Path path = MergePath.get("cloud", fileBrowser.getCurrentPath().toString(), file.getName());

                        Application.getInstance().getTransferQueue().enqueue(
                                new UploadTransferInfo((MergePath) path, file)).get();

                    } catch (InterruptedException | ExecutionException | CancellationException ignored) {
                        // ignored...
                    } finally {
                        Platform.runLater(fileBrowser::update);
                    }

                }, "Archive::uploadFile()"));


            }

        });


        buttonAddFolder.setOnMouseClicked(e -> {

            Pair<String, Integer> p = Dialogs.showInputBox("INPUTDIALOG_TITLE", BaseDialog.DIALOG_OK);

            if(p.getValue() == BaseDialog.DIALOG_OK) {

                if(p.getKey().trim().isEmpty()) {
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_EMPTY");

                }else if(new File(p.getKey().trim()).isFile()) {
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID_1");

                } else {

                    try {

                        Files.createDirectory(
                                MergePath.get("cloud", fileBrowser.getCurrentPath().toString(), p.getKey()));

                        fileBrowser.update();

                    } catch (IOException io) {
                        Dialogs.showErrorBox(io.getLocalizedMessage());
                    }

                }

            }

        });




        buttonUpFolder.setOnMouseClicked(e -> {

            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(getScene().getWindow());


            if(file == null)
                Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_NOTSELECTED");

            else if(!file.isDirectory())
                Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID_2");

            else {

                try {

                    Files.createDirectory(
                            MergePath.get("cloud", fileBrowser.getCurrentPath().toString(), file.getName()));

                    fileBrowser.update();

                } catch (IOException io) {
                    Dialogs.showErrorBox(io.getLocalizedMessage());
                }

            }

        });


        fileBrowser.currentPathProperty().addListener(
                (v, o, n) -> textFieldPath.setText(n.toUri().toString()));


        fileBrowser.setItemFactory(new FileBrowserItemFactory() {

            private final FileSystem fileSystem = FileSystems.getFileSystem(URI.create("cloud:///"));

            private final ArrayList<MenuItem> folderMenuItems = new ArrayList<>() {{

                add(new MenuItem(Application.getInstance().getLocale().get("CONTEXT_MENU_OPEN")) {{
                    setOnAction(e ->
                        fileBrowser.browse(MergePath.get(
                                fileBrowser.getCurrentPath().toUri().getScheme(),
                                fileBrowser.getCurrentPath().toString(),
                                fileBrowser.getSelectedItem().getText()
                        ))
                    );
                }});

                add(new MenuItem(Application.getInstance().getLocale().get("CONTEXT_MENU_DELETE")) {{
                    setOnAction(e -> {

                        fileBrowser.getSelectedItems().forEach(i -> {

                            try {
                                Files.delete(MergePath.get(
                                        fileBrowser.getCurrentPath().toUri().getScheme(),
                                        fileBrowser.getCurrentPath().toString(),
                                        i.getText()
                                ));

                            } catch (DirectoryNotEmptyException io) {
                                Platform.runLater(() -> Dialogs.showErrorBox("ERROR_DIRECTORY_NOT_EMPTY"));
                            } catch (IOException io) {
                                Platform.runLater(() -> Dialogs.showErrorBox("ERROR_FILE_DELETE"));
                            }

                        });

                        fileBrowser.update();

                    });
                }});

            }};

            private final ArrayList<MenuItem> fileMenuItems = new ArrayList<>() {{

                add(new MenuItem(Application.getInstance().getLocale().get("CONTEXT_MENU_DOWNLOAD")) {{
                    setOnAction(Event::consume);
                }});

                add(new MenuItem(Application.getInstance().getLocale().get("CONTEXT_MENU_DELETE")) {{
                    setOnAction(e -> {

                        fileBrowser.getSelectedItems().forEach(i -> {

                            try {
                                Files.delete(MergePath.get(
                                        fileBrowser.getCurrentPath().toUri().getScheme(),
                                        fileBrowser.getCurrentPath().toString(),
                                        i.getText()
                                ));

                            } catch (DirectoryNotEmptyException io) {
                                Platform.runLater(() -> Dialogs.showErrorBox("ERROR_DIRECTORY_NOT_EMPTY"));
                            } catch (IOException io) {
                                Platform.runLater(() -> Dialogs.showErrorBox("ERROR_FILE_DELETE"));
                            }

                        });

                        fileBrowser.update();

                    });
                }});

            }};



            @Override
            public List<FileBrowserItem> call(Path path) {

                final ArrayList<FileBrowserItem> items = new ArrayList<>();


                try {


                    Files.list(path).filter(Files::isDirectory).forEach(i ->
                            items.add(new FileBrowserItem(Mime.FOLDER, i.getFileName().toString()) {{
                                setMenuItems(folderMenuItems);
                            }})
                    );

                    Files.list(path).filter(p -> !Files.isDirectory(p)).forEach(i -> {

                        String filename = i.getFileName().toString();
                        Mime mime = Mimes.getInstance().getByExtension(filename);

                        items.add(new FileBrowserItem(mime, filename) {{
                            setMenuItems(fileMenuItems);
                        }});

                    });

                } catch (IOException ignored) { }


                return items;

            }

        });



        fileBrowser.setCurrentPath(Path.of(URI.create("cloud:///")));
        fileBrowser.update();

        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/filebrowser-header.css");
    }
}
