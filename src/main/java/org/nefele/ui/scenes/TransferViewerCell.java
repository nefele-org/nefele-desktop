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

import com.jfoenix.controls.JFXProgressBar;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.TransferInfo;
import org.nefele.ui.Themeable;

import java.net.URL;
import java.util.ResourceBundle;


public class TransferViewerCell extends StackPane implements Initializable, Themeable {

    private final ObjectProperty<TransferInfo> transferInfo;

    @FXML private FontAwesomeIconView iconOperation;
    @FXML private MaterialDesignIconView iconTransfer;
    @FXML private MaterialDesignIconView iconHistory;
    @FXML private MaterialDesignIconView buttonPauseResume;
    @FXML private FontAwesomeIconView buttonClose;
    @FXML private JFXProgressBar progressStatus;
    @FXML private Label labelTime;
    @FXML private Label labelSpeed;
    @FXML private Label labelFileName;

    public TransferViewerCell(TransferInfo transferInfo) {

        this.transferInfo = new SimpleObjectProperty<>(transferInfo);

        Resources.getFXML(this, "fxml/TransferViewerCell.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        buttonClose.setOnMouseClicked( e -> {
            getTransferInfo().setStatus(TransferInfo.TRANSFER_STATUS_CANCELED);
        });

        buttonPauseResume.setOnMouseClicked( e -> {

            getTransferInfo().setStatus(
                    getTransferInfo().getStatus() == TransferInfo.TRANSFER_STATUS_RUNNING
                        ? TransferInfo.TRANSFER_STATUS_PAUSED
                        : TransferInfo.TRANSFER_STATUS_RUNNING
            );

            buttonPauseResume.setGlyphName(
                    getTransferInfo().getStatus() == TransferInfo.TRANSFER_STATUS_RUNNING
                            ? "PLAY"
                            : "PAUSE"
            );

        });


        Application.getInstance().getViews().add(this);
    }


    @Override
    public void initializeInterface() {

    }

    public TransferInfo getTransferInfo() {
        return transferInfo.get();
    }

    public ObjectProperty<TransferInfo> transferInfoProperty() {
        return transferInfo;
    }

    public void setTransferInfo(TransferInfo transferInfo) {
        this.transferInfo.set(transferInfo);
    }


    private void setInfo(){

        progressStatus.setProgress((double)getTransferInfo().getProgress()/(double)getTransferInfo().getSize());

        labelFileName.setText(getTransferInfo().getName());

        labelSpeed.setText( getTransferInfo().getSpeed() / 1024 < 1024 ? String.format("%d kB/s", getTransferInfo().getSpeed() / 1024)
                                                                       : String.format("%d Mb/s", getTransferInfo().getSpeed() / 1024 / 1024));

        if(getTransferInfo().getRemainingTime() == -1)
            labelTime.setText("∞");
        else if(getTransferInfo().getRemainingTime() < 60)
            labelTime.setText(String.format("%d s", getTransferInfo().getRemainingTime()));
        else
            labelTime.setText(String.format("%d s", getTransferInfo().getRemainingTime() / 60 + 1));

    }

}
