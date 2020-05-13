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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.Mime;
import org.nefele.core.Mimes;
import org.nefele.ui.Themeable;
import org.nefele.ui.controls.FileBrowser;
import org.nefele.ui.controls.FileBrowserItem;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Trash extends StackPane implements Initializable, Themeable {

    @FXML private JFXTextField textFieldPath;
    @FXML private JFXButton buttonForward;
    @FXML private JFXButton buttonBack;
    @FXML private JFXButton buttonHome;
    @FXML private JFXButton buttonEmpty;
    @FXML private FileBrowser fileBrowser;


    public Trash() {
        Resources.getFXML(this, "/fxml/Trash.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        buttonForward.setOnMouseClicked(e -> {
            fileBrowser.browseHistory(1);
        });

        buttonBack.setOnMouseClicked(e -> {
            fileBrowser.browseHistory(-1);
        });


        buttonHome.setOnMouseClicked(e -> {
            fileBrowser.setCurrentPath("/");
        });

        buttonEmpty.setOnMouseClicked(e -> {
            /* TODO ... */
        });




        textFieldPath.textProperty().bind(fileBrowser.currentPathProperty());
        textFieldPath.setFocusTraversable(false);


        fileBrowser.setItemFactory(path -> {

            Application.log(fileBrowser.getClass(), "List %s", path);

            ArrayList<FileBrowserItem> items = new ArrayList<>();
            ArrayList<MenuItem> menuItems = new ArrayList<>();

            menuItems.add(new MenuItem("Blabla"));

            try {

                Files.list(Paths.get(path)).filter(Files::isDirectory).forEach(i ->
                        items.add(new FileBrowserItem(Mime.FOLDER, i.getFileName().toString())));

                Files.list(Paths.get(path)).filter(p -> !Files.isDirectory(p)).forEach(i -> {

                    Mime mime = Mime.UNKNOWN;
                    String filename = i.getFileName().toString();

                    if(filename.contains("."))
                        mime = Mimes.getByExtension(filename.substring(filename.lastIndexOf(".")))
                                .orElse(Mimes.getByExtension("*")
                                        .orElse(Mime.UNKNOWN));

                    items.add(new FileBrowserItem(mime, filename));

                });

            } catch (IOException ignored) { }

            items.forEach(i -> i.setMenuItems(menuItems));
            return items;

        });

        fileBrowser.update();
        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/filebrowser-header.css");
    }
}
