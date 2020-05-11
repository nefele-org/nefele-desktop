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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.nefele.Resources;
import org.nefele.cloud.Drive;
import org.nefele.ui.Themeable;
import java.net.URL;
import java.util.ResourceBundle;

public class StatsCell extends BorderPane implements Initializable, Themeable {

    private final ObjectProperty<Drive> drive;

    @FXML private PieChart chart;
    @FXML private Label labelNameStatus;
    @FXML private Label labelSpace;

    public StatsCell(Drive drive) {

        this.drive = new SimpleObjectProperty<>(drive);

        Resources.getFXML(this, "/fxml/StatsCell.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        chart.setData(FXCollections.observableArrayList(
                new PieChart.Data("MAX", getDrive().getQuota()),
                new PieChart.Data("CUR", getDrive().getBlocks())
        ));

        labelNameStatus.textProperty().bind(getDrive().serviceProperty());
        labelSpace.textProperty().bind(getDrive().quotaProperty().asString());

    }

    @Override
    public void initializeInterface() {

    }

    public Drive getDrive() {
        return drive.get();
    }

    public ObjectProperty<Drive> driveProperty() {
        return drive;
    }

}
