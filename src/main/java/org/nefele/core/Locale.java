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

package org.nefele.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.nefele.Application;
import org.nefele.ApplicationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Locale implements ApplicationService {


    public static final String DEFAULT_LOCALE = "English";
    private final HashMap<String, String> map = new HashMap<>();
    private final StringProperty language;


    public Locale() {
        this.language = new SimpleStringProperty(null);
    }


    @SuppressWarnings("unchecked")
    public void setLanguage(String id) {

        InputStream inputStream = Resources.getStream(this, "/lang/" + id + ".json");

        try {

            map.clear();
            map.putAll(new ObjectMapper().readValue(inputStream, map.getClass()));

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }

        language.set(id);
        Application.log(getClass(), "Loaded locale " + id);

    }

    public String getLanguage() {
        return language.get();
    }

    public StringProperty languageProperty() {
        return language;
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

        else if(parent instanceof Text) {

            Text i = (Text) parent;

            if(i.getUserData() != null) {

                String translation;
                if (!(translation = Application.getInstance().getLocale().get((String) i.getUserData())).equals(i.getUserData()))
                    i.setText(translation);

            }

        }

        else if(parent instanceof TableColumnBase) {

            TableColumnBase<?, ?> i = (TableColumnBase<?, ?>) parent;

            if(i.getUserData() != null) {

                String translation;
                if (!(translation = Application.getInstance().getLocale().get((String) i.getUserData())).equals(i.getUserData()))
                    i.setText(translation);

            }

        }

        else if(parent instanceof Control) {

            Tooltip j = ((Control) parent).getTooltip();

            if(Objects.nonNull(j)) {

                if(j.getUserData() != null) {

                    String translation;
                    if (!(translation = Application.getInstance().getLocale().get((String) j.getUserData())).equals(j.getUserData()))
                        j.setText(translation);

                }

            }


            if(parent instanceof Labeled) {

                Labeled i = (Labeled) parent;

                if(i.getUserData() != null) {

                    String translation;
                    if (!(translation = Application.getInstance().getLocale().get((String) i.getUserData())).equals(i.getUserData()))
                        i.setText(translation);

                }

            }

            else if(parent instanceof TreeTableView) {
                ((TreeTableView<?>) parent).getColumns()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(this::translate);
            }

            else if(parent instanceof ScrollPane)
                translate(((ScrollPane) parent).getContent());



        }


    }


    public ArrayList<String> list() {

        ArrayList<String> r = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.getStream(this, "/lang")));

        try {

            String res;
            while ((res = reader.readLine()) != null)
                r.add(res.substring(0, res.lastIndexOf(".")));

            reader.close();

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }

        return r;

    }

    @Override
    public void initialize() {

        Application.getInstance()
                .getLocale()
                .setLanguage(Application.getInstance()
                        .getConfig()
                        .getString("app.ui.locale")
                            .orElse(Locale.DEFAULT_LOCALE));

    }


}
