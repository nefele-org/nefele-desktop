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

import com.jfoenix.controls.JFXComboBox;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import org.nefele.Application;
import org.nefele.Resources;

import java.net.URL;
import java.util.ResourceBundle;

public class WizardPage1 extends WizardPage {

    @FXML private JFXComboBox<String> comboBoxLaunguage;

    public WizardPage1(Parent wizardRoot) {

        super(wizardRoot);

        Resources.getFXML(this, "/fxml/wizard/WizardPage1.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        comboBoxLaunguage.getItems()
                .addAll(Application.getInstance().getLocale().list());

        comboBoxLaunguage.setValue(Application.getInstance().getLocale().getLanguage());


        comboBoxLaunguage.valueProperty().addListener((v, o, n) -> {

            Application.getInstance().getLocale().setLanguage(n);
            Application.getInstance().getViews().update();

            Application.getInstance().runThread(new Thread(() -> {

                Application.getInstance().getConfig().setString("app.ui.locale", n);
                Application.getInstance().getConfig().update(null);

            }, "updateSettings()::app.ui.locale"));

        });

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        setChecked(true);
    }

}
