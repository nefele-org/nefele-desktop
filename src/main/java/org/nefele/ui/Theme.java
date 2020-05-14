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

package org.nefele.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import org.nefele.Application;
import org.nefele.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;


public class Theme {

    public static final String DEFAULT_THEME = "light";

    private final HashMap<String, String> map = new HashMap<>();
    private final String styleName;
    private String style;
    private String fontFamily;
    private Integer fontSize;



    public Theme(String styleName) {

        this.style = null;
        this.styleName = requireNonNull(styleName);


        InputStream inputStream;

        try {

            inputStream = Resources.getStream(this, "/theme/" + styleName + ".json");

        } catch (RuntimeException e) {

            Application.log(e.getClass(), "Failed to load theme %s, fallback %s", styleName, DEFAULT_THEME);
            inputStream = Resources.getStream(this, "/theme/" + DEFAULT_THEME + ".json");

        }


        try {

            map.clear();
            map.putAll(new ObjectMapper().readValue(inputStream, map.getClass()));

        } catch (IOException e) {
            Application.panic(getClass(), e);
        }


        setFontFamily(Application.getInstance().getConfig().getString("app.ui.font-family").orElse("Segoe UI"));
        setFontSize(Application.getInstance().getConfig().getInteger("app.ui.font-size").orElse(13));

        update();

        Application.log(getClass(), "Loaded theme %s", styleName);

    }




    public Color get(String style) {
        return Color.web(requireNonNull(map.get(style)));
    }

    public String getLightMode() {
        return getStyleName().contains("dark") ? "dark" : "light";
    }


    public String getStyle() {
        return style;
    }

    public String getStyleName() {
        return styleName;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public ArrayList<String> list() {

        ArrayList<String> r = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.getStream(this, "/theme")));

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


    public void update() {

        StringBuilder sb = new StringBuilder();

        sb.append("* {\n");

        for(String key : map.keySet())
            sb.append(String.format("%s: %s;\n", key, map.get(key)));

        sb.append(String.format("-fx-font-family: \"%s\";\n", getFontFamily()));
        sb.append(String.format("-fx-font-size: %dpx;\n", getFontSize()));
        sb.append("}\n");

        this.style = sb.toString();

    }
}
