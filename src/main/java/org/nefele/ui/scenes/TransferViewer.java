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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.TransferInfo;
import org.nefele.ui.Themeable;

import java.net.URL;
import java.util.ResourceBundle;

public class TransferViewer extends StackPane implements Initializable, Themeable {

    private final ObservableList<TransferInfo> transfers;

    @FXML private ScrollPane transferPane;
    @FXML private VBox cellPane;


    public TransferViewer() {

        transfers = FXCollections.observableArrayList();

        Resources.getFXML(this, "/fxml/TransferViewer.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        getTransfers().addListener((ListChangeListener<? super TransferInfo>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    /* TODO... */;

                if(change.wasAdded())
                    /* TODO... */;

            }

        });

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "css/transferviewer-cell.css");

        cellPane.getChildren().add(new TransferViewerCell(null)); /* TODO: Remove */

    }

    public ObservableList<TransferInfo> getTransfers() {
        return transfers;
    }
}
