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

package org.nefele.ui.wizard;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.controls.NefelePane;

import java.net.URL;
import java.util.ResourceBundle;

public class WizardPage3 extends WizardPage {

    @FXML private JFXButton buttonAddCloud;

    public WizardPage3() {
        Resources.getFXML(this, "/fxml/wizard/WizardPage3.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonAddCloud.setOnMouseClicked(e ->{
            Platform.runLater(() -> {

                Stage s = new Stage();
                NefelePane nefelePane = new NefelePane(new CloudHelper());
                nefelePane.setModal(NefelePane.MODAL_UNDECORATED);
                nefelePane.setShowDarkMode(false);
                nefelePane.setShowLogo(true);
                nefelePane.setShowStatusBar(false);
                nefelePane.setResizable(false);
                s.setScene(new Scene(nefelePane));
                s.show();

            });
        });

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }
}
