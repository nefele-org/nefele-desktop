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

package org.nefele.ui.scenes.transferviewer;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.cloud.TransferInfo;
import org.nefele.core.Resources;
import org.nefele.utils.PlatformUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

public class TransferViewer extends StackPane implements Initializable, Themeable {


    @FXML private ScrollPane transferPane;
    @FXML private VBox cellPane;
    @FXML private Label labelInfo;


    public TransferViewer() {
        Resources.getFXML(this, "/fxml/scenes/transferviewer/TransferViewer.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Application.getInstance()
                .getTransferQueue()
                .getTransferQueue().addListener((ListChangeListener<? super Pair<TransferInfo, Future<Integer>>>) change -> {


                    while (change.next()) {

                        if (change.wasRemoved())
                            PlatformUtils.runLaterAndWait(() -> change.getRemoved().forEach(i -> cellPane.getChildren().removeIf(j -> ((TransferViewerCell) j).getTransferInfo() == i.getKey())));

                        if (change.wasAdded())
                            PlatformUtils.runLaterAndWait(() -> change.getAddedSubList().forEach(i -> cellPane.getChildren().add(new TransferViewerCell(i.getKey()))));

                    }

        });



        labelInfo.visibleProperty().bind(
                Bindings.when(Bindings.isEmpty(cellPane.getChildren()))
                    .then(true)
                    .otherwise(false)
        );

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/transferviewer-cell.css");
    }

}
