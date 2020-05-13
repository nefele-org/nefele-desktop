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

import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.Theme;
import org.nefele.ui.Themeable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

public class Settings extends StackPane implements Initializable, Themeable {

    @FXML private VBox contentPane;
    @FXML private JFXButton buttonAdvancedSettings;
    @FXML private BorderPane boxAdvancedSettings;
    @FXML private VBox headerAdvancedSettings;
    @FXML private VBox contentAdvancedSettings;


    private final ObservableList<SettingsRecord> records;
    private final ObservableList<SettingsAdvancedRecord> advancedRecord;

    public Settings() {

        this.records = FXCollections.observableArrayList();
        this.advancedRecord = FXCollections.observableArrayList();

        Resources.getFXML(this, "/fxml/Settings.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        buttonAdvancedSettings.setOnMouseClicked(e -> {

            if(boxAdvancedSettings.getChildren().size() > 0)
                boxAdvancedSettings.getChildren().clear();
            else
                boxAdvancedSettings.getChildren().add(headerAdvancedSettings);

            boxAdvancedSettings.requestLayout();

        });



        getRecords().addListener((ListChangeListener<? super SettingsRecord>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(contentPane.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(contentPane.getChildren()::add);

            }

        });

        getAdvancedRecords().addListener((ListChangeListener<? super SettingsAdvancedRecord>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(contentAdvancedSettings.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(contentAdvancedSettings.getChildren()::add);

            }

        });


        getRecords().add(new SettingsRecord("app.ui.locale", "SETTINGS_LOCALE", "SETTINGS_LOCALE_DESCRIPTION",
                new JFXComboBox<String>() {{

                    BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.getStream(this, "/lang")));

                    try {

                        String res;
                        while ((res = reader.readLine()) != null)
                            getItems().add(res.substring(0, res.lastIndexOf(".")));

                        reader.close();
                        
                    } catch (IOException e) {
                        Application.panic(getClass(), e);
                    }

                    setValue(Application.getInstance().getLocale().getLanguage());


                    valueProperty().addListener((v, o, n) -> {
                        
                        Application.getInstance().getLocale().setLanguage(n);
                        Application.getInstance().getViews().update();
                        
                        Application.getInstance().runThread(new Thread(() -> {
                    
                            Application.getInstance().getConfig().setString("app.ui.locale", n);
                            Application.getInstance().getConfig().update();

                        }, "updateSettings()::app.ui.locale"));
                        
                    });

                }}
            ));


        getRecords().add(new SettingsRecord("app.ui.theme", "SETTINGS_THEME", "SETTINGS_THEME_DESCRIPTION",
                new JFXComboBox<String>() {{

                    BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.getStream(this, "/theme")));

                    try {

                        String res;
                        while ((res = reader.readLine()) != null)
                            getItems().add(res.substring(0, res.lastIndexOf(".")));

                        reader.close();

                    } catch (IOException e) {
                        Application.panic(getClass(), e);
                    }

                    setValue(Application.getInstance().getTheme().getStyleName());


                    valueProperty().addListener((v, o, n) -> {

                        if(!Application.getInstance().getTheme().getStyleName().equals(n)) {

                            Application.getInstance().setTheme(new Theme(n));
                            Application.getInstance().getViews().update();

                            Application.getInstance().runThread(new Thread(() -> {

                                Application.getInstance().getConfig().setString("app.ui.theme", n);
                                Application.getInstance().getConfig().update();

                            }, "updateSettings()::app.ui.theme"));

                        }

                    });

                    Application.getInstance().themeProperty().addListener((v, o, n) ->
                        setValue(Application.getInstance().getTheme().getStyleName()));


                }}
        ));


        getRecords().add(new SettingsRecord("app.ui.startup", "SETTINGS_STARTUP", "SETTINGS_STARTUP_DESCRIPTION",
                new JFXToggleButton() {{

                    this.selectedProperty().addListener((v, o, n) -> {

                        Application.getInstance().runThread(new Thread(() -> {

                            Application.getInstance().getConfig().setBoolean("app.ui.startup", n);
                            Application.getInstance().getConfig().update();

                        }, "updateSettings()::app.ui.startup"));

                    });

                    this.setSelected(Application.getInstance().getConfig().getBoolean("app.ui.startup").orElse(false));
                    this.setSize(8.0);
                    this.setMinHeight(12);
                    this.setMaxHeight(12);

                }}
        ));


        getRecords().add(new SettingsRecord("core.mfs.shared", "SETTINGS_SHARED", "SETTINGS_SHARED_DESCRIPTION",
                new JFXToggleButton() {{

                    this.selectedProperty().addListener((v, o, n) -> {

                        Application.getInstance().runThread(new Thread(() -> {

                            Application.getInstance().getConfig().setBoolean("core.mfs.shared", n);
                            Application.getInstance().getConfig().update();

                        }, "updateSettings()::core.mfs.shared"));

                    });

                    this.setSelected(Application.getInstance().getConfig().getBoolean("core.mfs.shared").orElse(false));
                    this.setSize(8.0);
                    this.setMinHeight(12);
                    this.setMaxHeight(12);

                }}
        ));



        getRecords().add(new SettingsRecord("core.transfers.parallel", "SETTINGS_TRANSFERS", "SETTINGS_TRANSFERS_DESCRIPTION",
                new JFXSlider(1.0, 32.0, Application.getInstance().getConfig().getInteger("core.transfers.parallel").orElse(4).doubleValue()) {{

                    this.valueChangingProperty().addListener((v, o, n) -> {

                        if(!n) {

                            Application.getInstance().runThread(new Thread(() -> {

                                Application.getInstance().getConfig().setInteger("core.transfers.parallel", ((Double) getValue()).intValue());
                                Application.getInstance().getConfig().update();

                            }, "updateSettings()::core.transfers.parallel"));

                        }
                    });

                }}
        ));

        Application.getInstance().getConfig().list().forEach((k, v) ->
            getAdvancedRecords().add(new SettingsAdvancedRecord(k, (String) v)));


        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/settings-base.css");
    }

    public ObservableList<SettingsRecord> getRecords() {
        return records;
    }

    public ObservableList<SettingsAdvancedRecord> getAdvancedRecords() {
        return advancedRecord;
    }
}
