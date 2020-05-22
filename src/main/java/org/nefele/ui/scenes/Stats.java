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
import com.jfoenix.controls.JFXSpinner;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.DriveService;
import org.nefele.core.TransferInfo;
import org.nefele.fs.MergeFileStore;
import org.nefele.fs.MergeFileSystem;
import org.nefele.ui.Themeable;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.utils.ExtraBindings;

import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Stats extends StackPane implements Initializable, Themeable {

    private final ObservableList<StatsDriveInfo> cells;

    @FXML private FlowPane flowPane;
    @FXML private ScrollPane scrollPane;
    
    @FXML private JFXSpinner spinnerStorage;
    @FXML private JFXSpinner spinnerTemporaryFiles;
    @FXML private JFXSpinner spinnerSystemMemory;

    @FXML private Label labelStoragePercentage;
    @FXML private Label labelStorageOccupied;
    @FXML private Label labelStorageFree;
    
    @FXML private Label labelTemporaryFilesPercentage;
    @FXML private Label labelTemporaryFilesOccupied;
    @FXML private Label labelTemporaryFilesFree;

    @FXML private Label labelSystemMemoryPercentage;
    @FXML private Label labelSystemMemoryOccupied;
    @FXML private Label labelSystemMemoryFree;

    @FXML private Label labelCloudDrivePercentage;
    @FXML private Label labelCloudDriveSpace;
    
    @FXML private Label labelAvailablePercentage;
    @FXML private Label labelAvailableSpace;
    
    @FXML private Label labelTrashPercentage;
    @FXML private Label labelTrashSpace;

    @FXML private Label labelAllFilesNum;
    @FXML private Label labelAllFilesDim;
    
    @FXML private Label labelAllFoldersNum;
    @FXML private Label labelAllFoldersDim;
    
    @FXML private Label labelTrashNum;
    @FXML private Label labelTrashDim;

    @FXML private Label labelIncomingSharesNum;
    @FXML private Label labelIncomingSharesDim;

    @FXML private Label labelOutgoingSharesNum;
    @FXML private Label labelOutgoingSharesDim;

    @FXML private JFXButton buttonSystemMemoryClean;
    @FXML private JFXButton buttonTemporaryClean;


    public Stats() {
        this.cells = FXCollections.observableArrayList();
        Resources.getFXML(this, "/fxml/Stats.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cells.addListener((ListChangeListener<? super StatsDriveInfo>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(flowPane.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(flowPane.getChildren()::add);

            }

        });


        DriveService.getInstance().getDrives().forEach(i -> cells.add(new StatsDriveInfo(i)));




        buttonSystemMemoryClean.setOnMouseClicked(e -> {

            Application.garbageCollect();
            updateSystemMemory();

        });


        buttonTemporaryClean.setOnMouseClicked(e -> {

            if(Dialogs.showWarningBox("STATS_CARD_PRIMARY_HEADER_2","STATS_CARD_DIALOG_TEMPORARY_CLEAN", BaseDialog.DIALOG_NO, BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES)
                /* TODO... */;

        });


        Application.getInstance().runWorker(new Thread(this::updateWorker, "Stats.updateWorker()"), 0,3, TimeUnit.SECONDS);
        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/stats-card.css");
    }



    private synchronized void updateSystemMemory() {

        spinnerSystemMemory.setProgress((double) Runtime.getRuntime().totalMemory() / (double) Runtime.getRuntime().maxMemory());

        labelSystemMemoryPercentage.setText(String.format("%d%%", (int) (spinnerSystemMemory.getProgress() * 100)));
        labelSystemMemoryOccupied.setText(String.format("%d MB", Runtime.getRuntime().totalMemory() / 1024 / 1024));
        labelSystemMemoryFree.setText(String.format("%d MB", (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) / 1024 / 1024));

    }

    private synchronized void updateStorage() {

        final FileSystem fileSystem = FileSystems.getFileSystem(URI.create("cloud:///"));
        final FileStore fileStore = fileSystem.getFileStores().iterator().next();


        spinnerStorage.progressProperty().bind(Bindings.createDoubleBinding(() ->
                        (fileStore.getTotalSpace() - fileStore.getUsableSpace()) / (double) fileStore.getTotalSpace()));

        labelStorageOccupied.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                (fileStore.getTotalSpace() - fileStore.getUsableSpace()), ""));

        labelStoragePercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                (int) (spinnerStorage.getProgress() * 100.0), spinnerStorage.progressProperty())
                    .asString("%d %%"));

        labelStorageFree.textProperty().bind(ExtraBindings.createSizeBinding(fileStore::getUsableSpace, ""));


    }

    private void updateWorker() {

        if(isVisible()) {

            Platform.runLater(() -> {

                updateSystemMemory();
                updateStorage();

                final MergeFileSystem fileSystem = (MergeFileSystem) FileSystems.getFileSystem(URI.create("cloud:///"));
                final MergeFileStore fileStore = fileSystem.getFileStore();



                spinnerTemporaryFiles.progressProperty().bind(Bindings.createDoubleBinding(() ->
                        (double) fileSystem.getStorage().getCurrentSize() / (double) Application.getInstance().getConfig().getLong("app.cache.limit").orElse(1L)
                ));


                labelTemporaryFilesPercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                        (int) (spinnerTemporaryFiles.getProgress() * 100.0), spinnerTemporaryFiles.progressProperty())
                        .asString("%d %%"));


                labelTemporaryFilesOccupied.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                                 fileSystem.getStorage().getCurrentSize(), ""));


                labelTemporaryFilesFree.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                        Application.getInstance().getConfig().getLong("app.cache.limit").orElse(1L) - fileSystem.getStorage().getCurrentSize(), ""));





                labelCloudDrivePercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                        (int) (spinnerStorage.getProgress() * 100.0), spinnerStorage.progressProperty())
                        .asString("%d %%"));

                labelCloudDriveSpace.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                        (fileStore.getTotalSpace() - fileStore.getUsableSpace()), ""));





                labelAvailablePercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                        (int) ((1.0 - spinnerStorage.getProgress()) * 100.0), spinnerStorage.progressProperty())
                        .asString("%d %%"));

                labelAvailableSpace.textProperty().bind(ExtraBindings.createSizeBinding(fileStore::getUsableSpace, ""));





                labelTrashPercentage.setText(String.format("%d%%", 0)); /* TODO... */
                labelTrashSpace.setText(String.format("%d GB", 0)); /* TODO... */




                labelAllFilesNum.textProperty().bind(Bindings.createStringBinding(() ->
                        String.valueOf(Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isRegularFile)
                                .count())));


                labelAllFilesDim.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                        Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isRegularFile)
                                .mapToLong(i -> i.toFile().length()).sum(), ""
                ));





                labelAllFoldersNum.textProperty().bind(Bindings.createStringBinding(() ->
                        String.valueOf(Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isDirectory)
                                .count() - 1 )));

                labelAllFoldersDim.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                        Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isDirectory)
                                .mapToLong(i -> i.toFile().length()).sum(), ""
                ));




                labelTrashNum.setText(String.format("%d", 0)); /* TODO... */
                labelTrashDim.setText(String.format("%d GB", 0)); /* TODO... */




                labelIncomingSharesNum.textProperty().bind(Bindings.size(
                        Application.getInstance().getTransferQueue().getTransferQueue().filtered(i -> i.getKey().getType() == TransferInfo.TRANSFER_TYPE_UPLOAD)
                ).asString());

                labelIncomingSharesDim.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                        Application.getInstance().getTransferQueue().getTransferQueue()
                            .stream()
                            .filter(i -> i.getKey().getType() == TransferInfo.TRANSFER_TYPE_UPLOAD)
                            .mapToLong(i -> i.getKey().getSize())
                            .sum(), ""
                ));



                labelOutgoingSharesNum.textProperty().bind(Bindings.size(
                        Application.getInstance().getTransferQueue().getTransferQueue().filtered(i -> i.getKey().getType() == TransferInfo.TRANSFER_TYPE_DOWNLOAD)
                ).asString());

                labelOutgoingSharesDim.textProperty().bind(ExtraBindings.createSizeBinding(() ->
                        Application.getInstance().getTransferQueue().getTransferQueue()
                            .stream()
                            .filter(i -> i.getKey().getType() == TransferInfo.TRANSFER_TYPE_DOWNLOAD)
                            .mapToLong(i -> i.getKey().getSize())
                            .sum(), ""
                ));

            });

        }

    }
}
