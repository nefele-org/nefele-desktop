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

import javafx.beans.property.*;
import javafx.util.Pair;
import org.nefele.Application;
import org.nefele.ApplicationService;
import org.nefele.ApplicationTask;
import org.nefele.utils.PlatformUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

public final class Status implements ApplicationService {

    public static final String ICON_SUCCESS = "EMOTICON_HAPPY";
    public static final String ICON_ALERT = "ALERT";
    public static final String ICON_TRANSFERS = "ACCESS_POINT";
    public static final String ICON_NETWORK_DOWN = "SERVER_NETWORK_OFF";

    private final ReadOnlyStringProperty text;
    private final ReadOnlyStringProperty icon;
    private final StringProperty textImpl;
    private final StringProperty iconImpl;
    private final DoubleProperty loadingProgress;
    private final BooleanProperty networkConnected;


    private final HashSet<Pair<String, String>> messageQueue;
    private Pair<String, String> fallbackStatus;


    public Status() {

        this.textImpl = new SimpleStringProperty("");
        this.iconImpl = new SimpleStringProperty(ICON_SUCCESS);
        this.loadingProgress = new SimpleDoubleProperty(0.0);
        this.networkConnected = new SimpleBooleanProperty(true);

        this.text = new SimpleStringProperty() {{ bind(textImpl); }};
        this.icon = new SimpleStringProperty() {{ bind(iconImpl); }};

        this.messageQueue = new HashSet<>();

    }


    @Override
    public void initialize() {

        updateFallbackMessage(ICON_SUCCESS, "STATUS_READY");


        this.networkConnected.addListener((v, o, n) -> {

            if(!n)
                updateFallbackMessage(ICON_NETWORK_DOWN, "STATUS_NETWORK_DOWN");
            else
                updateFallbackMessage(ICON_SUCCESS, "STATUS_READY");

        });

    }

    @Override
    public void update(ApplicationTask currentTask) {


        if (!messageQueue.isEmpty()) {

            var iterator = messageQueue.iterator();
            var message = iterator.next();

            iterator.remove();


            PlatformUtils.runLaterAndWait(() -> {
                iconImpl.setValue(message.getKey());
                textImpl.setValue(message.getValue());
            });


        } else
            updateMessage(fallbackStatus.getKey(), fallbackStatus.getValue());



        try {

            final var url = new URL("https://www.wikipedia.org")
                    .openConnection();

            url.connect();
            url.getInputStream().close();

            networkConnected.setValue(true);

        } catch (MalformedURLException ignored) {
        } catch (IOException ignored) {
            networkConnected.setValue(false);
        }


    }

    public String getText() {
        return text.get();
    }

    public ReadOnlyStringProperty textProperty() {
        return text;
    }

    public String getIcon() {
        return icon.get();
    }

    public ReadOnlyStringProperty iconProperty() {
        return icon;
    }

    public double getLoadingProgress() {
        return loadingProgress.get();
    }

    public DoubleProperty loadingProgressProperty() {
        return loadingProgress;
    }

    public void setLoadingProgress(double loadingProgress) {
        this.loadingProgress.set(loadingProgress);
    }

    public boolean isNetworkConnected() {
        return networkConnected.get();
    }

    public BooleanProperty networkConnectedProperty() {
        return networkConnected;
    }

    public void updateMessage(String icon, String message, String... args) {
        messageQueue.add(new Pair<>(icon, Application.getInstance().getLocale().get(message) + " " + String.join(", ", args)));
    }

    public void updateFallbackMessage(String icon, String message) {
        fallbackStatus = new Pair<>(icon, message);
    }

}
