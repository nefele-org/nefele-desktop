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
import javafx.scene.paint.Color;
import org.nefele.Application;
import org.nefele.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;


public class Theme {

    public static final String DEFAULT_THEME = "light";

    private final HashMap<String, String> map = new HashMap<>();
    private final String style;
    private final String styleName;



    public Theme(String styleName) {

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



        StringBuilder sb = new StringBuilder();

        sb.append("* {\n");

        for(String key : map.keySet())
            sb.append(String.format("%s: %s;\n", key, map.get(key)));

        sb.append("}\n");

        this.style = sb.toString();


        Application.log(getClass(), "Loaded theme %s", styleName);

    }




    public Color get(String style) {
        return Color.web(requireNonNull(map.get(style)));
    }

    public String getStyle() {
        return style;
    }

    public String getStyleName() {
        return styleName;
    }

    public String getLightMode() {
        return getStyleName().contains("dark") ? "dark" : "light";
    }
}
