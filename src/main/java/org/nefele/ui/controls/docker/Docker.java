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

package org.nefele.ui.controls.docker;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;
import org.nefele.ui.base.NefelePane;

import javax.imageio.ImageIO;
import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class Docker extends VBox implements Initializable, Themeable {

    private final ObjectProperty<DockerButton> selectedButton;
    private final ObjectProperty<Pane> contentPane;
    private final ObservableList<DockerItem> parents;


    public Docker() {

        selectedButton = new SimpleObjectProperty<>(null);
        contentPane = new SimpleObjectProperty<>(null);
        parents = FXCollections.observableArrayList();

        Resources.getFXML(this, "/fxml/controls/docker/Docker.fxml");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        parents.addListener((ListChangeListener<DockerItem>) change -> {

            while(change.next()) {

                if(change.wasAdded()) {
                    change.getAddedSubList().forEach(i ->
                        getChildren().add(new DockerButton(i) {{

                            if(i.getIcon().equals("EXIT_TO_APP"))
                                setOnMouseClicked(e -> ((NefelePane) getScene().getRoot()).close());
                            else
                                setOnMouseClicked(e -> selectedButtonProperty().set(this));

                        }})
                    );
                }


                if(change.wasRemoved()) {
                    change.getRemoved().forEach(
                            i -> getChildren().removeIf(j -> {
                                if (j instanceof DockerButton)
                                    return ((DockerButton) j).getItem().getReference().equals(i.getReference());

                                return false;
                            }
                        )
                    );
                }

            }

        });

        selectedButton.addListener((v, o, n) -> {

            requireNonNull(getContentPane());
            requireNonNull(n);


            if (o != null) {
                o.selectedProperty().setValue(false);
                o.getItem().getReference().setVisible(false);
                getContentPane().getChildren().remove(o.getItem().getReference());
            }


            n.selectedProperty().setValue(true);
            n.getItem().getReference().setOpacity(0);
            n.getItem().getReference().setVisible(true);

            getContentPane().getChildren().add(n.getItem().getReference());


            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200),
                    new KeyValue(n.getItem().getReference().opacityProperty(), 1, Interpolator.EASE_BOTH)));

            timeline.play();



        });





        Platform.runLater(() -> {

            if(getChildren().isEmpty())
                Application.panic(getClass(), "Docker can not be empty!");

            setSelectedButton((DockerButton) getChildren().get(2));

        });

        Application.getInstance().getViews().add(this);
    }


    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/docker-button.css");
    }



    public DockerButton getSelectedButton() {
        return selectedButton.get();
    }

    public ObjectProperty<DockerButton> selectedButtonProperty() {
        return selectedButton;
    }

    public void setSelectedButton(DockerButton selectedButton) {
        this.selectedButton.set(selectedButton);
    }

    public Pane getContentPane() {
        return contentPane.get();
    }

    public ObjectProperty<Pane> contentPaneProperty() {
        return contentPane;
    }

    public void setContentPane(Pane contentPane) {
        this.contentPane.set(contentPane);
    }

    public ObservableList<DockerItem> getParents() {
        return parents;
    }


}
