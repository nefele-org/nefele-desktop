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

package org.nefele.ui.scenes.stats;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
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
import org.nefele.Themeable;
import org.nefele.cloud.DriveProvider;
import org.nefele.cloud.DriveProviders;
import org.nefele.cloud.TransferInfo;
import org.nefele.core.Resources;
import org.nefele.fs.MergeFileStore;
import org.nefele.fs.MergeFileSystem;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;
import org.nefele.utils.BindingsUtils;
import org.nefele.utils.PlatformUtils;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
    @FXML private Label labelStorageTotal;
    
    @FXML private Label labelTemporaryFilesPercentage;
    @FXML private Label labelTemporaryFilesOccupied;
    @FXML private Label labelTemporaryFilesTotal;

    @FXML private Label labelSystemMemoryPercentage;
    @FXML private Label labelSystemMemoryOccupied;
    @FXML private Label labelSystemMemoryFree;

    @FXML private Label labelCloudDrivePercentage;
    @FXML private Label labelCloudDriveSpace;
    
    @FXML private Label labelAvailablePercentage;
    @FXML private Label labelAvailableSpace;

    @FXML private Label labelAllFilesNum;
    @FXML private Label labelAllFilesDim;
    
    @FXML private Label labelAllFoldersNum;
    @FXML private Label labelAllFoldersDim;

    @FXML private Label labelIncomingSharesNum;
    @FXML private Label labelIncomingSharesDim;

    @FXML private Label labelOutgoingSharesNum;
    @FXML private Label labelOutgoingSharesDim;

    @FXML private JFXButton buttonSystemMemoryClean;
    @FXML private JFXButton buttonTemporaryClean;


    public Stats() {
        this.cells = FXCollections.observableArrayList();
        Resources.getFXML(this, "/fxml/scenes/stats/Stats.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        DriveProviders.getInstance().getDriveProviders().addListener((ListChangeListener<? super DriveProvider>) change -> {

            while (change.next()) {

                if (change.wasRemoved())
                    change.getRemoved().forEach(i -> flowPane.getChildren().removeIf(j ->
                            (j instanceof StatsDriveInfo && ((StatsDriveInfo) j).getDriveProvider().getId().equals(i.getId()))));

                if (change.wasAdded())
                    change.getAddedSubList().forEach(i -> flowPane.getChildren().add(new StatsDriveInfo(i)));

            }

        });

        cells.addListener((ListChangeListener<? super StatsDriveInfo>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(flowPane.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(flowPane.getChildren()::add);

            }

        });

        DriveProviders.getInstance().getDriveProviders().forEach(
                i -> cells.add(new StatsDriveInfo(i)));

        buttonSystemMemoryClean.setOnMouseClicked(e -> {

            Application.garbageCollect();
            updateSystemMemory();

        });


        buttonTemporaryClean.setOnMouseClicked(e -> {

            if(Dialogs.showWarningBox("STATS_CARD_PRIMARY_HEADER_2","STATS_CARD_DIALOG_TEMPORARY_CLEAN", BaseDialog.DIALOG_NO, BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES)
                ((MergeFileSystem) FileSystems.getFileSystem(URI.create("nefele:///"))).getStorage().cleanCache();

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

        final FileSystem fileSystem = FileSystems.getFileSystem(URI.create("nefele:///"));
        final FileStore fileStore = fileSystem.getFileStores().iterator().next();


        spinnerStorage.progressProperty().bind(Bindings.createDoubleBinding(() ->
                        (fileStore.getTotalSpace() - fileStore.getUsableSpace()) / (double) fileStore.getTotalSpace()));

        labelStorageOccupied.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                (fileStore.getTotalSpace() - fileStore.getUsableSpace()), ""));

        labelStoragePercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                (int) (spinnerStorage.getProgress() * 100.0), spinnerStorage.progressProperty())
                    .asString("%d %%"));

        labelStorageTotal.textProperty().bind(BindingsUtils.createSizeBinding(fileStore::getTotalSpace, ""));


    }

    private void updateWorker() {

        if(isVisible()) {

            PlatformUtils.runLaterAndWait(() -> {

                updateSystemMemory();
                updateStorage();

                final MergeFileSystem fileSystem = (MergeFileSystem) FileSystems.getFileSystem(URI.create("nefele:///"));
                final MergeFileStore fileStore = fileSystem.getFileStore();



                spinnerTemporaryFiles.progressProperty().bind(Bindings.createDoubleBinding(() ->
                        (double) fileSystem.getStorage().getCurrentCacheSize() / (double) Application.getInstance().getConfig().getLong("core.mfs.cache.limit").orElse(1L)
                ));


                labelTemporaryFilesPercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                        (int) (spinnerTemporaryFiles.getProgress() * 100.0), spinnerTemporaryFiles.progressProperty())
                        .asString("%d %%"));


                labelTemporaryFilesOccupied.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                                 fileSystem.getStorage().getCurrentCacheSize(), ""));


                labelTemporaryFilesTotal.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                        Application.getInstance().getConfig().getLong("core.mfs.cache.limit").orElse(1L), ""));





                labelCloudDrivePercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                        (int) (spinnerStorage.getProgress() * 100.0), spinnerStorage.progressProperty())
                        .asString("%d %%"));

                labelCloudDriveSpace.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                        (fileStore.getTotalSpace() - fileStore.getUsableSpace()), ""));





                labelAvailablePercentage.textProperty().bind(Bindings.createIntegerBinding(() ->
                        (int) (((1.0 - spinnerStorage.getProgress()) * 100.0) + 0.5), spinnerStorage.progressProperty())
                        .asString("%d %%"));

                labelAvailableSpace.textProperty().bind(BindingsUtils.createSizeBinding(fileStore::getUsableSpace, ""));




                labelAllFilesNum.textProperty().bind(Bindings.createStringBinding(() ->
                        String.valueOf(Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isRegularFile)
                                .count())));


                labelAllFilesDim.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                        Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isRegularFile)
                                .mapToLong(i -> i.toFile().length())
                                .sum(), ""
                ));





                labelAllFoldersNum.textProperty().bind(Bindings.createStringBinding(() ->
                        String.valueOf(Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isDirectory)
                                .count() - 1 )));

                labelAllFoldersDim.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                        Files.walk(fileSystem.getPath(MergeFileSystem.ROOT))
                                .filter(Files::isDirectory)
                                .mapToLong(i -> i.toFile().length())
                                .sum(), ""
                ));




                labelIncomingSharesNum.textProperty().bind(Bindings.size(
                        Application.getInstance().getTransferQueue().getTransferQueue().filtered(i ->
                                i.getKey().getType() == TransferInfo.TRANSFER_TYPE_UPLOAD)).asString());


                labelIncomingSharesDim.textProperty().bind(BindingsUtils.createSizeBinding(() ->
                        Application.getInstance().getTransferQueue().getTransferQueue()
                            .stream()
                            .filter(i -> i.getKey().getType() == TransferInfo.TRANSFER_TYPE_UPLOAD)
                            .mapToLong(i -> i.getKey().getSize())
                            .sum(), ""
                ));



                labelOutgoingSharesNum.textProperty().bind(Bindings.size(
                        Application.getInstance().getTransferQueue().getTransferQueue().filtered(i ->
                                i.getKey().getType() == TransferInfo.TRANSFER_TYPE_DOWNLOAD)).asString());


                labelOutgoingSharesDim.textProperty().bind(BindingsUtils.createSizeBinding(() ->
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
