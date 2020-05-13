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

package org.nefele;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.beans.DesignMode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class Locale {


    public static final String DEFAULT_LOCALE = "English";
    private final HashMap<String, String> map = new HashMap<>();


    @SuppressWarnings("unchecked")
    public void setLanguage(String id) {

        InputStream inputStream = Resources.getStream(this, "/lang/" + id + ".json");

        try {

            map.clear();
            map.putAll(new ObjectMapper().readValue(inputStream, map.getClass()));

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }


        Application.log(getClass(), "Loaded locale " + id);

    }


    public String get(String id) {
        return map.getOrDefault(id, id);
    }


    public <T> void translate(Object parent) {

        if(parent instanceof Pane) {

            ((Pane) parent).getChildren()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(this::translate);

        }

        else if(parent instanceof Label) {

            Label i = (Label) parent;

            String translation;
            if (!(translation = Application.getInstance().getLocale().get(i.getText())).equals(i.getText()))
                i.setText(translation);

        }

        else if(parent instanceof Text) {

            Text i = (Text) parent;

            String translation;
            if (!(translation = Application.getInstance().getLocale().get(i.getText())).equals(i.getText()))
                i.setText(translation);

        }

        else if(parent instanceof Control) {

            Tooltip i = ((Control) parent).getTooltip();

            if(Objects.nonNull(i)) {

                String translation;
                if (!(translation = Application.getInstance().getLocale().get(i.getText())).equals(i.getText()))
                    i.setText(translation);

            }

            if(parent instanceof ScrollPane)
                translate(((ScrollPane) parent).getContent());

        }


    }


}
