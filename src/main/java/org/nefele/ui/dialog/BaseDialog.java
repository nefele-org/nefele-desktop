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

package org.nefele.ui.dialog;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nefele.Application;
import org.nefele.Themeable;
import org.nefele.core.Resources;
import org.nefele.ui.base.NefeleContentPane;
import org.nefele.ui.base.NefelePane;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class BaseDialog extends Stage {

    public static final int DIALOG_CLOSED = 0;
    public static final int DIALOG_OK = 1;
    public static final int DIALOG_ABORT = 2;
    public static final int DIALOG_YES = 3;
    public static final int DIALOG_NO = 4;
    public static final int DIALOG_CONTINUE = 5;
    public static final int DIALOG_EXIT = 6;
    public static final int DIALOG_MINIMIZE = 7;
    public static final int DIALOG_RETRY = 8;


    class MessagePane extends NefeleContentPane implements Themeable, Initializable {

        @FXML private HBox buttonHBox;
        @FXML private Label labelTitle;
        @FXML private Text textMessage;
        @FXML private MaterialDesignIconView ivIcon;
        @FXML private ScrollPane scrollMessage;

        public MessagePane() {
            Resources.getFXML(this, "/fxml/dialog/BaseDialog.fxml");
        }


        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {

            labelTitle.setUserData(getTitle());
            labelTitle.setText(getTitle());

            textMessage.setUserData(getMessage());
            textMessage.setText(getMessage());

            if(getIcon() != null)
                ivIcon.setGlyphName(getIcon());

            Application.getInstance().getViews().add(this);

        }

        @Override
        public void initializeInterface() {

            buttonHBox.getChildren().clear();


            getButtons().forEach(i -> {

                JFXButton button = null;

                switch(i) {

                    case DIALOG_OK:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_OK"));
                        break;

                    case DIALOG_ABORT:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_ABORT"));
                        break;

                    case DIALOG_YES:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_YES"));
                        break;

                    case DIALOG_NO:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_NO"));
                        break;

                    case DIALOG_CONTINUE:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_CONTINUE"));
                        break;

                    case DIALOG_EXIT:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_EXIT"));
                        break;

                    case DIALOG_MINIMIZE:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_MINIMIZE"));
                        break;

                    case DIALOG_RETRY:
                        button = new JFXButton(Application.getInstance().getLocale().get("DIALOG_RETRY"));
                        break;

                    default:
                        Application.panic(getClass(), "Invalid DIALOG_*: %d", i);
                        break;

                }


                requireNonNull(button);

                button.setMinWidth(68);
                button.setMinHeight(25);
                button.setOnMouseClicked(e -> exitWithDialogResult(i));

                textMessage.wrappingWidthProperty().bind(scrollMessage.widthProperty());

                buttonHBox.getChildren().add(button);

            });


            ((NefelePane) getScene().getRoot()).setOnClosing(() -> {
                exitWithDialogResult(DIALOG_CLOSED);
                return true;
            });

        }



    }




    private final ArrayList<Integer> buttons;
    private String icon;
    private int dialogResult;
    private String message;


    protected BaseDialog(String title, String message) {

        dialogResult = DIALOG_CLOSED;
        buttons = new ArrayList<>();
        icon = null;

        requireNonNull(title);
        requireNonNull(message);

        setWidth(350);
        setHeight(250);
        setMinWidth(320);
        setMinHeight(230);
        setTitle(title);
        setMessage(message);

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);

    }


    public ArrayList<Integer> getButtons() {
        return buttons;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getDialogResult() {
        return dialogResult;
    }




    protected String getIcon() {
        return icon;
    }

    protected void setIcon(String icon) {
        this.icon = icon;
    }


    protected void exitWithDialogResult(int dialogResult) {
        this.dialogResult = dialogResult;
        this.close();
    }


    @Override
    public void showAndWait() {

        NefelePane nefelePane = new NefelePane(new MessagePane());
        nefelePane.setModal(NefelePane.MODAL_DIALOG);
        nefelePane.setShowDarkMode(false);
        nefelePane.setShowLogo(false);
        nefelePane.setShowStatusBar(false);

        setScene(new Scene(nefelePane));

        super.showAndWait();
    }


    @Override
    public void close() {

        Application.getInstance().getViews().remove(
                getScene().getRoot());

        super.close();
    }

}