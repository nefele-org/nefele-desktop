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
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.fs.MergeFileSystem;
import org.nefele.ui.Themeable;
import org.nefele.ui.controls.NefelePane;
import org.nefele.ui.dialog.BaseDialog;
import org.nefele.ui.dialog.Dialogs;

import java.net.URL;
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

        //Application.getInstance().getDrives().forEach(i -> cells.add(new StatsCell(i)));


        buttonSystemMemoryClean.setOnMouseClicked(e -> {

            Application.garbageCollect();
            updateSystemMemory();

        });

        buttonTemporaryClean.setOnMouseClicked(e -> {

            if(Dialogs.showWarningBox("STATS_CARD_PRIMARY_HEADER_2","STATS_CARD_DIALOG_TEMPORARY_CLEAN", BaseDialog.DIALOG_NO, BaseDialog.DIALOG_YES) == BaseDialog.DIALOG_YES)
                /* TODO... */;

        });

        Application.getInstance().runWorker(new Thread(this::updateWorker, "Stats.updateWorker()"), 0,5, TimeUnit.SECONDS);
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

    private void updateWorker() {

        if(isVisible()) {

            Platform.runLater(() -> {

                updateSystemMemory();

                spinnerStorage.setProgress(1);
                spinnerTemporaryFiles.setProgress(0);

                labelStoragePercentage.setText(String.format("%d%%", 0));
                labelStorageOccupied.setText(String.format("%d GB", 0));
                labelStorageFree.setText(String.format("%d GB", 0));

                labelTemporaryFilesPercentage.setText(String.format("%d%%", 0));
                labelTemporaryFilesOccupied.setText(String.format("%d GB", 0));
                labelTemporaryFilesFree.setText(String.format("%d GB", Application.getInstance().getConfig().getLong("app.cache.limit").orElse(0L) / 1024 / 1024 / 1024));

                labelCloudDrivePercentage.setText(String.format("%d%%", 0));
                labelCloudDriveSpace.setText(String.format("%d GB", 0));

                labelAvailablePercentage.setText(String.format("%d%%", 0));
                labelAvailableSpace.setText(String.format("%d GB", 0));

                labelTrashPercentage.setText(String.format("%d%%", 0));
                labelTrashSpace.setText(String.format("%d GB", 0));

                labelAllFilesNum.setText(String.format("%d", 0));
                labelAllFilesDim.setText(String.format("%d GB", 0));

                labelAllFoldersNum.setText(String.format("%d", 0));
                labelAllFoldersDim.setText(String.format("%d GB", 0));

                labelTrashNum.setText(String.format("%d", 0));
                labelTrashDim.setText(String.format("%d GB", 0));

                labelIncomingSharesNum.setText(String.format("%d", 0));
                labelIncomingSharesDim.setText(String.format("%d GB", 0));

                labelOutgoingSharesNum.setText(String.format("%d", 0));
                labelOutgoingSharesDim.setText(String.format("%d GB", 0));

            });

        }

    }
}
