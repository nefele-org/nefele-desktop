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

import com.jfoenix.controls.JFXSpinner;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.cloud.Drive;
import org.nefele.fs.MergeChunk;
import org.nefele.ui.Themeable;
import org.nefele.utils.ExtraBindings;

import java.net.URL;
import java.util.ResourceBundle;

public class StatsDriveInfo extends StackPane implements Initializable, Themeable {

    @FXML private JFXSpinner spinner;
    @FXML private Label labelDriveName;
    @FXML private Label labelSpaceAvailable;
    @FXML private Label labelStatus;
    
    private final Drive drive;

    public StatsDriveInfo(Drive drive) {
        
        this.drive = drive;

        Resources.getFXML(this, "/fxml/StatsDriveInfo.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelStatus.textProperty().bind(Bindings.createStringBinding(() -> {

            switch (getDrive().getStatus()) {

                case Drive.STATUS_CONNECTING:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_CONNECTING"));

                case Drive.STATUS_READY:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_READY"));

                case Drive.STATUS_DISCONNECTING:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_DISCONNECTING"));

                case Drive.STATUS_DISCONNECTED:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_DISCONNECTED"));

                case Drive.STATUS_ERROR:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_ERROR"));

                case Drive.STATUS_DISABLED:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_DISABLED"));

                case Drive.STATUS_UNKNOWN:
                default:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_UNKNOWN"));


            }

        }, getDrive().statusProperty()));


        labelDriveName.textProperty().bind(getDrive().descriptionProperty());


        labelSpaceAvailable.textProperty().bind(
                ExtraBindings.createSizeBinding(() -> (getDrive().getQuota() - getDrive().getChunks()) * MergeChunk.getSize(), "" , getDrive().quotaProperty(), getDrive().chunksProperty()));

        spinner.progressProperty().bind(Bindings.createDoubleBinding (
                () -> (double) getDrive().getChunks() / (double) getDrive().getQuota(), getDrive().chunksProperty(), getDrive().quotaProperty()));

    }



    @Override
    public void initializeInterface() {

    }


    public void update() {

        spinner.setProgress((double) getDrive().getChunks() / (double) getDrive().getQuota());

    }

    public Drive getDrive() {
        return drive;
    }

}
