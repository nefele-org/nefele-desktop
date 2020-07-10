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

import com.jfoenix.controls.JFXSpinner;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.cloud.DriveProvider;
import org.nefele.core.Resources;
import org.nefele.utils.BindingsUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class StatsDriveInfo extends StackPane implements Initializable, Themeable {

    @FXML private JFXSpinner spinner;
    @FXML private Label labelDriveName;
    @FXML private Label labelStatus;
    @FXML private Label labelTotalSpace;
    @FXML private Label labelTotalQuota;

    private final DriveProvider driveProvider;

    public StatsDriveInfo(DriveProvider driveProvider) {
        
        this.driveProvider = driveProvider;

        Resources.getFXML(this, "/fxml/StatsDriveInfo.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        labelStatus.textProperty().bind(Bindings.createStringBinding(() -> {

            switch (getDriveProvider().getStatus()) {

                case DriveProvider.STATUS_CONNECTING:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_CONNECTING"));

                case DriveProvider.STATUS_READY:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_READY"));

                case DriveProvider.STATUS_DISCONNECTING:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_DISCONNECTING"));

                case DriveProvider.STATUS_DISCONNECTED:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_DISCONNECTED"));

                case DriveProvider.STATUS_ERROR:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_ERROR"));

                case DriveProvider.STATUS_DISABLED:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_DISABLED"));

                case DriveProvider.STATUS_UNKNOWN:
                default:
                    return (Application.getInstance().getLocale().get("STATS_STATUS_UNKNOWN"));


            }

        }, getDriveProvider().statusProperty()));


        labelDriveName.textProperty().bind(getDriveProvider().descriptionProperty());


        labelTotalQuota.textProperty().bind(
                BindingsUtils.createSizeBinding(() -> getDriveProvider().getQuota(), "", getDriveProvider().quotaProperty(), getDriveProvider().chunksProperty()));

        labelTotalSpace.textProperty().bind(
                BindingsUtils.createSizeBinding(() -> getDriveProvider().getMaxQuota(), ""));

        spinner.progressProperty().bind(Bindings.createDoubleBinding (
                () -> (double) getDriveProvider().getUsedSpace() / (double) getDriveProvider().getQuota(), getDriveProvider().chunksProperty(), getDriveProvider().quotaProperty()));

    }




    public void update() {

        spinner.setProgress((double) getDriveProvider().getUsedSpace() / (double) getDriveProvider().getQuota());

    }

    public DriveProvider getDriveProvider() {
        return driveProvider;
    }

}
