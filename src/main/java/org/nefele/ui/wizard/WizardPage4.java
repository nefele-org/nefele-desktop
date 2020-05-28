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

import com.jfoenix.controls.JFXToggleButton;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.scenes.SettingsRecord;

import java.net.URL;
import java.util.ResourceBundle;

public class WizardPage4 extends WizardPage {

    @FXML private VBox contentDefaultSettings;

    private final ObservableList<SettingsRecord> defaultRecords;

    public WizardPage4(Parent wizardRoot) {

        super(wizardRoot);

        this.defaultRecords = FXCollections.observableArrayList();
        Resources.getFXML(this, "/fxml/wizard/WizardPage4.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        getDefaultRecords().addListener((ListChangeListener<? super SettingsRecord>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(contentDefaultSettings.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(contentDefaultSettings.getChildren()::add);

            }

        });

        getDefaultRecords().add(new SettingsRecord("app.ui.startup", "SETTINGS_STARTUP", "SETTINGS_STARTUP_DESCRIPTION",
                new JFXToggleButton() {{

                    this.setSelected(Application.getInstance().getConfig().getBoolean("app.ui.startup").orElse(false));
                    this.setSize(8.0);


                    this.selectedProperty().addListener((v, o, n) -> {

                        Application.getInstance().runThread(new Thread(() -> {

                            Application.getInstance().getConfig().setBoolean("app.ui.startup", n);
                            Application.getInstance().getConfig().update();

                        }, "updateSettings()::app.ui.startup"));

                    });

                }}
        ));


        getDefaultRecords().add(new SettingsRecord("core.mfs.compressed", "SETTINGS_COMPRESSED", "SETTINGS_COMPRESSED_DESCRIPTION",
                new JFXToggleButton() {{

                    this.setSelected(Application.getInstance().getConfig().getBoolean("core.mfs.compressed").orElse(false));
                    this.setSize(8.0);


                    this.selectedProperty().addListener((v, o, n) -> {

                        Application.getInstance().runThread(new Thread(() -> {

                            Application.getInstance().getConfig().setBoolean("core.mfs.compressed", n);
                            Application.getInstance().getConfig().update();

                        }, "updateSettings()::core.mfs.compressed"));

                    });

                }}
        ));


        getDefaultRecords().add(new SettingsRecord("core.mfs.encrypted", "SETTINGS_ENCRYPTED", "SETTINGS_ENCRYPTED_DESCRIPTION",
                new JFXToggleButton() {{

                    this.setSelected(Application.getInstance().getConfig().getBoolean("core.mfs.encrypted").orElse(false));
                    this.setSize(8.0);


                    this.selectedProperty().addListener((v, o, n) -> {

                        Application.getInstance().runThread(new Thread(() -> {

                            Application.getInstance().getConfig().setBoolean("core.mfs.encrypted", n);
                            Application.getInstance().getConfig().update();

                        }, "updateSettings()::core.mfs.encrypted"));

                    });

                }}
        ));

        checkedProperty().setValue(true);

        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }

    public ObservableList<SettingsRecord> getDefaultRecords() {
        return defaultRecords;
    }
}
