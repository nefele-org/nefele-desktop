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
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.nefele.*;
import org.nefele.cloud.*;
import org.nefele.fs.MergePath;
import org.nefele.ui.controls.FileBrowser;
import org.nefele.ui.controls.FileBrowserItem;
import org.nefele.ui.controls.FileBrowserItemFactory;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.ui.dialog.InputDialog;
import org.nefele.ui.dialog.InputDialogResult;
import org.nefele.utils.FilenameUtils;
import org.nefele.utils.PlatformUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

        try {

            textFieldPath.setFocusTraversable(false);

            buttonForward.setOnMouseClicked(e ->
                    fileBrowser.browseHistory(1));

            buttonBack.setOnMouseClicked(e ->
                    fileBrowser.browseHistory(-1));

            buttonRefresh.setOnMouseClicked(e ->
                    fileBrowser.update());

            buttonHome.setOnMouseClicked(e ->
                    fileBrowser.setCurrentPath(Path.of(URI.create("nefele:///"))));

            buttonSync.setOnMouseClicked(e -> {
                Platform.runLater(() -> {

                    try {

//                    if(Dialogs.showInfoBox("ARCHIVE_DIALOG_REQUEST_SYNC",
//                            BaseDialog.DIALOG_NO, BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES)
                        /*TODO...*/

                    } catch (Exception exception) {

                        Dialogs.showErrorBox("ARCHIVE_ERROR_DIALOG_REQUEST_SYNC");

                    }

                });
            });

            buttonUpFile.setOnMouseClicked(e -> {

                var fileChooser = new FileChooser();
                var file = fileChooser.showOpenDialog(getScene().getWindow());


                if (file == null)
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FILE_NOTSELECTED");

                else if (!file.isFile())
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FILE_INVALID");

                else {

                    Application.getInstance().runThread(new Thread(() -> {

                        try {

                            final Path path = MergePath.get(fileBrowser.getCurrentPath().toString(), file.getName());

                            var status = Application.getInstance().getTransferQueue().enqueue(
                                    new UploadTransferInfo((MergePath) path, file)).get();


                            switch (status) {

                                case TransferInfo.TRANSFER_STATUS_COMPLETED:

                                    //Application.getInstance().setStatus(...)
                                    break;

                                case TransferInfo.TRANSFER_STATUS_ERROR:
                                case TransferInfo.TRANSFER_STATUS_CANCELED:

                                    if (Files.exists(path))
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

                InputDialogResult result = Dialogs.showInputBox("INPUTDIALOG_TITLE", BaseDialog.DIALOG_OK);

                if (result.getButton() == BaseDialog.DIALOG_OK) {

                    final String filename = result.getText().trim();

                    if (filename.isBlank()) {
                        Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_EMPTY");

                    } else if (FilenameUtils.isFilenameInvalid(filename)) {
                        Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID_NAME");

                    } else {

                        Application.getInstance().runThread(new Thread(() -> {

                            try {

                                Files.createDirectory(
                                        MergePath.get(fileBrowser.getCurrentPath().toString(), filename));

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

                var fileChooser = new DirectoryChooser();
                var file = fileChooser.showDialog(getScene().getWindow());


                if (file == null)
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_NOTSELECTED");

                else if (!file.isDirectory())
                    Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID");

                else {

                    Application.getInstance().runThread(new Thread(() -> {

                        try {

                            Files.walk(file.toPath()).forEach(path -> {

                                final Path cloudPath = MergePath.get(fileBrowser.getCurrentPath().toString(), file.toPath().getParent().relativize(path).toString());

                                try {

                                    if (Files.isDirectory(path))
                                        Files.createDirectory(cloudPath);

                                    else {

                                        PlatformUtils.runLaterAndWait(() -> {

                                            final Future<Integer> future = Application.getInstance().getTransferQueue().enqueue(
                                                    new UploadTransferInfo((MergePath) cloudPath, path.toFile()));


                                            Application.getInstance().runThread(new Thread(() -> {

                                                try {

                                                    var status = future.get();

                                                    switch (status) {

                                                        case TransferInfo.TRANSFER_STATUS_COMPLETED:

                                                            //Application.getInstance().setStatus(... // TODO: set Status Bar
                                                            break;


                                                        case TransferInfo.TRANSFER_STATUS_CANCELED:
                                                        case TransferInfo.TRANSFER_STATUS_ERROR:

                                                            if (Files.exists(cloudPath))
                                                                Files.delete(cloudPath);

                                                            break;


                                                        default:
                                                            Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                                            break;

                                                    }

                                                } catch (InterruptedException | ExecutionException | IOException ignored) {
                                                }

                                            }, "Future::" + cloudPath.toUri()));

                                        });


                                    }

                                } catch (IOException ignored) {
                                }

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


                private final MenuItem folderShareMenuItem = new MenuItem("") {{
                    setOnAction(e -> {

                        MergePath path = (MergePath) MergePath.get(
                                fileBrowser.getCurrentPath().toString(),
                                fileBrowser.getSelectedItem().getText());


                        var fileChooser = new DirectoryChooser();
                        var file = fileChooser.showDialog(getScene().getWindow());

                        if (file == null)
                            Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_NOTSELECTED");

                        else if (!file.isDirectory())
                            Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID");

                        else {

                            SharedFolders.getInstance()
                                    .addSharedFolderService(new SharedFolder(file.toPath(), path));

                            fileBrowser.update();

                            Application.log(Archive.this.getClass(), "Added new SharedFolder %s:%s", file.getAbsolutePath(), path);

                        }

                    });
                }};

                private final MenuItem folderUnshareMenuItem = new MenuItem("") {{
                    setOnAction(e -> {

                        MergePath path = (MergePath) MergePath.get(
                                fileBrowser.getCurrentPath().toString(),
                                fileBrowser.getSelectedItem().getText());

                        SharedFolders.getInstance()
                                .removeSharedFolderServiceByPath(path);

                        fileBrowser.update();

                        Application.log(Archive.this.getClass(), "Removed SharedFolder %s", path);

                    });
                }};


                private final MenuItem folderOpenMenuItem = new MenuItem("") {{
                    setOnAction(e ->
                            fileBrowser.browse(MergePath.get(
                                    fileBrowser.getCurrentPath().toString(),
                                    fileBrowser.getSelectedItem().getText()
                            ))
                    );
                }};


                private final MenuItem renameMenuItem = new MenuItem("") {{
                    setOnAction(e -> {

                        InputDialogResult result = Dialogs.showInputBox("ARCHIVE_DIALOG_RENAME", fileBrowser.getSelectedItem().getText(),
                                InputDialog.DIALOG_OK,
                                InputDialog.DIALOG_ABORT);


                        if (result.getButton() == InputDialog.DIALOG_OK) {

                            final String filename = result.getText().trim();

                            if (filename.isBlank()) {
                                Dialogs.showErrorBox("ARCHIVE_DIALOG_RENAME_EMPTY");

                            } else if (FilenameUtils.isFilenameInvalid(filename)) {
                                Dialogs.showErrorBox("ARCHIVE_DIALOG_RENAME_INVALID_NAME");

                            } else {

                                Application.getInstance().runThread(new Thread(() -> {

                                    try {

                                        Path oldName = MergePath.get(fileBrowser.getCurrentPath().toString(), fileBrowser.getSelectedItem().getText());
                                        Path newName = MergePath.get(fileBrowser.getCurrentPath().toString(), filename);

                                        Files.move(oldName, newName);


                                    } catch (IOException io) {
                                        Platform.runLater(() -> Dialogs.showErrorBox("ARCHIVE_DIALOG_RENAME_FAIL"));
                                    } finally {
                                        Platform.runLater(fileBrowser::update);
                                    }

                                }, "Archive::rename()"));

                            }

                        }

                    });
                }};


                private final MenuItem deleteMenuItem = new MenuItem("") {{
                    setOnAction(e ->

                        fileBrowser.getSelectedItems().forEach(i ->

                            Application.getInstance().runThread(new Thread(() -> {

                                try {

                                    Path path = MergePath.get(
                                            fileBrowser.getCurrentPath().toString(),
                                            i.getText()
                                    );

                                    if (Files.isDirectory(path))
                                        if (Files.list(path).count() > 0)
                                            throw new DirectoryNotEmptyException(path.toString());

                                    Files.delete(path);

                                } catch (DirectoryNotEmptyException io) {
                                    Platform.runLater(() -> Dialogs.showErrorBox("ERROR_DIRECTORY_NOT_EMPTY"));
                                } catch (IOException io) {
                                    Platform.runLater(() -> Dialogs.showErrorBox("ERROR_FILE_DELETE"));
                                } finally {
                                    Platform.runLater(fileBrowser::update);
                                }

                            }, "Archive::delete()"))

                        )

                    );
                }};


                private final MenuItem fileDownloadMenuItem = new MenuItem("") {{
                    setOnAction(e -> {

                        var fileChooser = new DirectoryChooser();
                        var file = fileChooser.showDialog(getScene().getWindow());

                        if (file == null)
                            Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_NOTSELECTED");

                        else if (!file.isDirectory())
                            Dialogs.showErrorBox("ARCHIVE_DIALOG_FOLDER_INVALID");

                        else {


                            final int getSelectedItemCount = fileBrowser.getSelectedItems().size();

                            fileBrowser.getSelectedItems().forEach(i -> {

                                if (i.getMime().equals(Mime.FOLDER))
                                    return;


                                final Path cloudPath = MergePath.get(fileBrowser.getCurrentPath().toString(), i.getText());
                                final Path localPath = Paths.get(file.getAbsolutePath(), cloudPath.getFileName().toString());


                                PlatformUtils.runLaterAndWait(() -> {

                                    final var future = Application.getInstance().getTransferQueue().enqueue(
                                            new DownloadTransferInfo((MergePath) cloudPath, localPath.toFile()));


                                    Application.getInstance().runThread(new Thread(() -> {

                                        try {

                                            var status = future.get();

                                            switch (status) {

                                                case TransferInfo.TRANSFER_STATUS_COMPLETED:

                                                    if (getSelectedItemCount == 1) {

                                                        if (Desktop.isDesktopSupported())
                                                            Desktop.getDesktop().open(localPath.toFile());

                                                    }

                                                    //Application.getInstance().setStatus(... // TODO: set Status Bar
                                                    break;


                                                case TransferInfo.TRANSFER_STATUS_CANCELED:
                                                case TransferInfo.TRANSFER_STATUS_ERROR:

                                                    if (Files.exists(localPath))
                                                        Files.delete(localPath);

                                                    break;


                                                default:
                                                    Application.log(getClass(), "WARNING! Invalid TransferInfo.TRANSFER_STATUS_*: %d", status);
                                                    break;

                                            }

                                        } catch (InterruptedException | ExecutionException | IOException ignored) {
                                        }

                                    }, "Future::" + cloudPath.toUri()));


                                });


                            });

                        }

                    });
                }};


                private final List<MenuItem> folderMenuItems = new ArrayList<>() {{
                    add(folderOpenMenuItem);
                    add(renameMenuItem);
                    add(deleteMenuItem);
                }};


                private final List<MenuItem> fileMenuItems = new ArrayList<>() {{
                    add(fileDownloadMenuItem);
                    add(renameMenuItem);
                    add(deleteMenuItem);
                }};


                @Override
                public List<FileBrowserItem> call(Path path) {

                    final ArrayList<FileBrowserItem> items = new ArrayList<>();

                    folderOpenMenuItem.setText(Application.getInstance().getLocale().get("CONTEXT_MENU_OPEN"));
                    folderShareMenuItem.setText(Application.getInstance().getLocale().get("CONTEXT_MENU_SHARE"));
                    folderUnshareMenuItem.setText(Application.getInstance().getLocale().get("CONTEXT_MENU_UNSHARE"));
                    fileDownloadMenuItem.setText(Application.getInstance().getLocale().get("CONTEXT_MENU_DOWNLOAD"));
                    renameMenuItem.setText(Application.getInstance().getLocale().get("CONTEXT_MENU_RENAME"));
                    deleteMenuItem.setText(Application.getInstance().getLocale().get("CONTEXT_MENU_DELETE"));


                    try {


                        Files.list(path)
                                .filter(Files::exists)
                                .filter(Files::isDirectory).forEach(i ->
                                items.add(new FileBrowserItem(Mime.FOLDER, i.getFileName().toString()) {{

                                    if (!SharedFolders.getInstance().isShared((MergePath) i))
                                        setMenuItems(Stream
                                                .concat(folderMenuItems.stream(), Stream.of(folderShareMenuItem))
                                                .collect(Collectors.toList()));

                                    else
                                        setMenuItems(Stream
                                                .concat(folderMenuItems.stream(), Stream.of(folderUnshareMenuItem))
                                                .collect(Collectors.toList()));

                                }})
                        );

                        Files.list(path)
                                .filter(Files::exists)
                                .filter(p -> !Files.isDirectory(p)).forEach(i -> {

                                    String filename = i.getFileName().toString();
                                    Mime mime = Mimes.getInstance().getByExtension(filename);

                                    items.add(new FileBrowserItem(mime, filename) {{
                                        setMenuItems(fileMenuItems);
                                    }});
                                }
                        );

                    } catch (IOException ignored) {
                    }


                    return items;

                }

            });


            fileBrowser.setCurrentPath(Path.of(URI.create("nefele:///")));
            fileBrowser.update();

            Application.getInstance().getViews().add(this);

        } catch (Exception e) {
            Application.panic(getClass(), e);
        }

    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/filebrowser-header.css");

        fileBrowser.update();
    }

}
