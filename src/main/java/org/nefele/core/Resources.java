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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import org.nefele.Application;
import org.nefele.Themeable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Objects;

import static java.util.Objects.requireNonNull;



public final class Resources {


    public static <T extends Parent & Themeable> T getFXML(T parent, String resource) {

        requireNonNull(parent);
        requireNonNull(resource);


        try {

            FXMLLoader fxmlLoader = new FXMLLoader();

            fxmlLoader.setLocation(getURL(parent, resource));
            fxmlLoader.setRoot(parent);
            fxmlLoader.setController(parent);
            fxmlLoader.load();


        } catch (IOException e) {
            Application.panic(Views.class, e);
        }


        //Application.log(Resources.class, "Loaded FXML %s", resource);
        return parent;

    }


    public static <T extends Parent & Themeable> T getFXML(T parent, Class<?> controller, String resource) {

        requireNonNull(parent);
        requireNonNull(controller);
        requireNonNull(resource);


        try {

            FXMLLoader fxmlLoader = new FXMLLoader();

            fxmlLoader.setLocation(getURL(parent, resource));
            fxmlLoader.setRoot(parent);
            fxmlLoader.setController(controller.getConstructor().newInstance());
            fxmlLoader.load();


        } catch (IOException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Application.panic(Views.class, e);
        }


        //Application.log(Resources.class, "Loaded FXML %s", resource);
        return parent;

    }


    public static <T extends Parent> void getCSS(T parent, String resource) {

        String css = getURL(parent, resource).toExternalForm();

        parent.getStylesheets().remove(css);
        parent.getStylesheets().add(css);

        //Application.log(Resources.class, "Loaded CSS %s", resource);

    }


    public static InputStream getStream(Object parent, String resource) {

        InputStream stream;
        if(Objects.isNull((stream = parent.getClass().getResourceAsStream(resource))))
            stream = parent.getClass().getClassLoader().getResourceAsStream(resource);

        requireNonNull(stream);

        return stream;
    }


    public static URL getURL(Object parent, String resource) {

        URL url;
        if(Objects.isNull((url = parent.getClass().getResource(resource))))
            url = parent.getClass().getClassLoader().getResource(resource);

        requireNonNull(url);

        return url;

    }

    public static void getFont(Object parent, String resource) {

        Font.loadFont(Resources.getURL(parent, resource).toExternalForm(), 12);

        Application.log(Resources.class, "Loaded Font %s", resource);
    }


}
