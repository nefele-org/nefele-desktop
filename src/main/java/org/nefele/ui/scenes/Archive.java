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
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
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
import org.nefele.cloud.DriveNotEmptyException;
import org.nefele.cloud.Drives;
import org.nefele.core.*;
import org.nefele.fs.MergePath;
import org.nefele.ui.Themeable;
import org.nefele.ui.controls.FileBrowser;
import org.nefele.ui.controls.FileBrowserItem;
import org.nefele.ui.controls.FileBrowserItemFactory;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.utils.ExtraPlatform;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class Archive extends StackPane implements Initializable, Themeable {

    @FXML private JFXTextField textFieldPath;
    @FXML private JFXButton buttonForward;
    @FXML private JFXButton buttonBack;
    @FXML private JFXButton buttonRefresh;
    @FXML private JFXButton buttonHome;
    @FXML private JFXButton buttonSync;
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

        buttonRefresh.setOnMouseClicked(e -> {
            fileBrowser.update();
        });

        buttonHome.setOnMouseClicked(e -> {
            fileBrowser.setCurrentPath(Path.of(URI.create("nefele:///")));
        });

        buttonSync.setOnMouseClicked(e -> {
            Platform.runLater(() ->{

                try{

//                    if(Dialogs.showInfoBox("ARCHIVE_DIALOG_REQUEST_SYNC",
//                            BaseDialog.DIALOG_NO, BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES)
                            /*TODO...*/

                } catch(Exception exception) {

                    Dialogs.showErrorBox("ARCHIVE_ERROR_DIALOG_REQUEST_SYNC");

                }

            });
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

                        final Path path = MergePath.get("nefele", fileBrowser.getCurrentPath().toString(), file.getName());

                        int status = Application.getInstance().getTransferQueue().enqueue(
                                new UploadTransferInfo((MergePath) path, file)).get();


                        switch (status) {

                            case TransferInfo.TRANSFER_STATUS_COMPLETED:

                                //Application.getInstance().setStatus(...)
                                break;

                            case TransferInfo.TRANSFER_STATUS_ERROR:
                            case TransferInfo.TRANSFER_STATUS_CANCELED:

                                if(Files.exists(path))
                                    Files.delete(path);

                                break;

                            default:
                                Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                break;

                        }

                    } catch (InterruptedException | ExecutionException | CancellationException | IOException ignored) {
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
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID_NAME");

                } else {

                    Application.getInstance().runThread(new Thread(() -> {

                        try {

                            Files.createDirectory(
                                    MergePath.get("nefele", fileBrowser.getCurrentPath().toString(), p.getKey()));

                        } catch (IOException io) {
                            Platform.runLater(() -> Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_CREATE_FAIL"));
                        } finally {
                            Platform.runLater(fileBrowser::update);
                        }

                    }, "Archive::addFolder()"));

                }

            }

        });




        buttonUpFolder.setOnMouseClicked(e -> {

            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(getScene().getWindow());


            if(file == null)
                Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_NOTSELECTED");

            else if(!file.isDirectory())
                Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID");

            else {

                Application.getInstance().runThread(new Thread(() -> {

                    try {

                        Files.walk(file.toPath()).forEach(path -> {

                            final Path cloudPath = MergePath.get("nefele", fileBrowser.getCurrentPath().toString(), file.toPath().getParent().relativize(path).toString());

                            try {

                                if (Files.isDirectory(path))
                                    Files.createDirectory(cloudPath);

                                else {

                                    ExtraPlatform.runLaterAndWait(() -> {

                                        final Future<Integer> future = Application.getInstance().getTransferQueue().enqueue(
                                                new UploadTransferInfo((MergePath) cloudPath, path.toFile()));


                                        Application.getInstance().runThread(new Thread(() -> {

                                            try {

                                                int status = future.get();
                                                switch (status) {

                                                    case TransferInfo.TRANSFER_STATUS_COMPLETED:

                                                        //Application.getInstance().setStatus(... // TODO: set Status Bar
                                                        break;


                                                    case TransferInfo.TRANSFER_STATUS_CANCELED:
                                                    case TransferInfo.TRANSFER_STATUS_ERROR:

                                                        if(Files.exists(cloudPath))
                                                            Files.delete(cloudPath);

                                                        break;


                                                    default:
                                                        Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                                        break;

                                                }

                                            } catch (InterruptedException | ExecutionException | IOException ignored) { }

                                        }, "Future::" + cloudPath.toUri()));

                                    });


                                }

                            } catch (IOException ignored) { }

                        });


                    } catch (IOException ignored) {
                        Platform.runLater(() -> Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_UPLOAD_FAIL"));
                    } finally {
                        Platform.runLater(fileBrowser::update);
                    }

                }, "Archive::uploadFolder()"));

            }

        });


        fileBrowser.currentPathProperty().addListener(
                (v, o, n) -> textFieldPath.setText(n.toUri().toString()));


        fileBrowser.setItemFactory(new FileBrowserItemFactory() {

            private final FileSystem fileSystem = FileSystems.getFileSystem(URI.create("nefele:///"));

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
                    setOnAction(e -> {

                        DirectoryChooser fileChooser = new DirectoryChooser();
                        File file = fileChooser.showDialog(getScene().getWindow());

                        if(file == null)
                            Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_NOTSELECTED");

                        else if(!file.isDirectory())
                            Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID");

                        else {


                            final int getSelectedItemCount = fileBrowser.getSelectedItems().size();

                            fileBrowser.getSelectedItems().forEach(i -> {

                                if(i.getMime().equals(Mime.FOLDER))
                                    return;


                                final Path cloudPath = MergePath.get("nefele", fileBrowser.getCurrentPath().toString(), i.getText());
                                final Path localPath = Paths.get(file.getAbsolutePath(), cloudPath.getFileName().toString());


                                ExtraPlatform.runLaterAndWait(() -> {

                                    final Future<Integer> future = Application.getInstance().getTransferQueue().enqueue(
                                            new DownloadTransferInfo((MergePath) cloudPath, localPath.toFile()));


                                    Application.getInstance().runThread(new Thread(() -> {

                                        try {

                                            int status = future.get();
                                            switch (status) {

                                                case TransferInfo.TRANSFER_STATUS_COMPLETED:

                                                    if(getSelectedItemCount == 1) {

                                                        if(Desktop.isDesktopSupported())
                                                            Desktop.getDesktop().open(localPath.toFile());

                                                    }

                                                    //Application.getInstance().setStatus(... // TODO: set Status Bar
                                                    break;


                                                case TransferInfo.TRANSFER_STATUS_CANCELED:
                                                case TransferInfo.TRANSFER_STATUS_ERROR:

                                                    if(Files.exists(localPath))
                                                        Files.delete(localPath);

                                                    break;


                                                default:
                                                    Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                                    break;

                                            }

                                        } catch (InterruptedException | ExecutionException | IOException ignored) { }

                                    }, "Future::" + cloudPath.toUri()));


                                });


                            });

                        }

                    });
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


                    Files.list(path)
                            .filter(Files::isDirectory).forEach(i ->
                                items.add(new FileBrowserItem(Mime.FOLDER, i.getFileName().toString()) {{
                                    setMenuItems(folderMenuItems);
                                }})
                    );

                    Files.list(path)
                            .filter(p -> !Files.isDirectory(p)).forEach(i -> {

                                String filename = i.getFileName().toString();
                                Mime mime = Mimes.getInstance().getByExtension(filename);

                                items.add(new FileBrowserItem(mime, filename) {{
                                    setMenuItems(fileMenuItems);
                                }});
                            }
                    );

                } catch (IOException ignored) { }


                return items;

            }

        });



        fileBrowser.setCurrentPath(Path.of(URI.create("nefele:///")));
        fileBrowser.update();

        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/filebrowser-header.css");
    }
}
