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

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.nefele.Application;
import org.nefele.Resources;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class WizardPage2 extends WizardPage {

    @FXML private JFXCheckBox checkBoxTerms;
    @FXML private ScrollPane scrollTerms;
    @FXML private Text textTerms;
    @FXML private Pane paneTextTerms;
    @FXML private Text textMessage;

    public WizardPage2(Parent wizardRoot) {

        super(wizardRoot);

        Resources.getFXML(this, "/fxml/wizard/WizardPage2.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        checkedProperty().bind(checkBoxTerms.selectedProperty());
        textTerms.wrappingWidthProperty().bind(paneTextTerms.widthProperty());

        textMessage.setText("" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse elementum ipsum non libero feugiat bibendum. Cras odio risus, molestie nec placerat a, varius nec felis. Duis fringilla eleifend elementum. Donec gravida erat ac urna porta, at viverra urna tristique. Donec ultrices tincidunt risus, eu vulputate lectus luctus in. Sed volutpat euismod tellus, at volutpat tortor ultricies ac. Aenean quis diam ullamcorper, pellentesque eros vel, vulputate odio. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Etiam vitae metus purus.\n" +
                "\n" +
                "Suspendisse vehicula nibh vitae suscipit sagittis. Pellentesque porttitor nunc in velit congue aliquet sit amet nec est. Fusce mi turpis, pharetra nec felis a, auctor imperdiet lorem. Maecenas accumsan sapien et metus fermentum egestas. Phasellus suscipit et leo vitae convallis. Fusce nec lobortis erat. In molestie sed lorem ut vulputate.\n" +
                "\n" +
                "Morbi sem nulla, cursus in enim at, accumsan ultrices ipsum. Sed facilisis urna vel porttitor viverra. Nam pretium urna a urna aliquam dapibus. Pellentesque elementum aliquam leo ut volutpat. Morbi suscipit tortor nisl, vitae tempor mi laoreet non. Ut maximus felis porttitor condimentum finibus. Nullam sit amet est odio. Proin turpis sapien, bibendum a feugiat vel, lacinia ac nulla.\n" +
                "\n" +
                "Sed id lacinia tortor, id consequat ipsum. Quisque luctus nisi ut egestas lacinia. Vestibulum pulvinar feugiat leo. Pellentesque turpis magna, laoreet eget semper at, viverra at risus. Etiam molestie nunc orci, sed tempus leo auctor eget. Nam sed porta orci. Maecenas aliquet mauris quis magna fermentum vulputate. Vivamus in metus et quam efficitur dapibus. Mauris et fringilla odio. Integer sodales vitae tortor eget tincidunt. Suspendisse viverra ornare odio, vel euismod mi accumsan eget. Suspendisse ac nunc lacus. Nullam molestie sem non dignissim pulvinar. Vestibulum sit amet purus porttitor, mollis urna nec, vehicula velit. Curabitur vel lorem tempor, porttitor quam vitae, pharetra massa. Aenean luctus tellus eu nunc porta, non ultrices orci pulvinar.");


        textMessage.wrappingWidthProperty().bind(scrollTerms.widthProperty());
        Application.getInstance().getViews().add(this);
    }

    @Override
    public void initializeInterface() {

    }

}
