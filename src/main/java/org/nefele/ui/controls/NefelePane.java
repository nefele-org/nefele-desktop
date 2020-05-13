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

package org.nefele.ui.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.ui.Theme;
import org.nefele.ui.Themeable;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;



public class NefelePane extends StackPane implements Initializable, Themeable {

    public static final int MODAL_WINDOW = 0;
    public static final int MODAL_DIALOG = 1;
    public static final int MODAL_UNDECORATED = 2;


    private final NefeleContentPane contentPane;
    private final IntegerProperty modal;
    private final BooleanProperty showDarkMode;
    private final BooleanProperty showLogo;
    private final BooleanProperty showStatusBar;
    private final BooleanProperty resizable;
    private final ObjectProperty<NefelePaneClosingOperation> onClosing;

    @FXML private JFXToggleButton toggleDarkMode;
    @FXML private JFXButton controlMinimize;
    @FXML private JFXButton controlMaximize;
    @FXML private JFXButton controlExit;
    @FXML private ImageView iconControlMinimize;
    @FXML private ImageView iconControlMaximize;
    @FXML private ImageView iconControlExit;
    @FXML private FontAwesomeIconView iconDarkMode;
    @FXML private BorderPane headerPane;
    @FXML private StackPane contentPaneContainer;
    @FXML private Pane awesomeLogo;
    @FXML private MaterialDesignIconView resizeHandle;
    @FXML private MaterialDesignIconView statusIcon;
    @FXML private Label statusText;
    @FXML private HBox statusBar;
    @FXML private Tooltip tooltipDarkMode;

    private double mouseDragX = 0;
    private double mouseDragY = 0;
    private boolean mouseDragging = false;
    private boolean mouseResizing = false;




    public NefelePane(NefeleContentPane contentPane) {

        this.contentPane = requireNonNull(contentPane);

        this.modal = new SimpleIntegerProperty(-1);
        this.showDarkMode = new SimpleBooleanProperty(true);
        this.showLogo = new SimpleBooleanProperty(true);
        this.showStatusBar = new SimpleBooleanProperty(true);
        this.resizable = new SimpleBooleanProperty(true);
        this.onClosing = new SimpleObjectProperty<>(null);

        Resources.getFXML(this, "/fxml/base/NefelePane.fxml");

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        toggleDarkMode.setOnMouseClicked(e -> {

            String currentStyle = Application.getInstance().getTheme().getStyleName();

            if(currentStyle.contains("dark"))
                Application.getInstance().setTheme(new Theme(currentStyle.replace("dark", "light")));
            else
                Application.getInstance().setTheme(new Theme(currentStyle.replace("light", "dark")));


            Application.getInstance().runThread(new Thread(() -> {
                Application.getInstance().getConfig().set("app.ui.theme", Application.getInstance().getTheme().getStyleName());
                Application.getInstance().getConfig().update();
            }, "toggleDarkMode()::app.ui.theme"));

            Application.getInstance().getViews().update();

        });

        Application.getInstance().themeProperty().addListener((v, o, n) ->
            toggleDarkMode.setSelected(requireNonNull(n).getStyleName().contains("dark")));


        controlMinimize.setOnMouseClicked(e -> {
            ((Stage) getScene().getWindow()).setIconified(true);
        });

        controlExit.setOnMouseClicked(e -> {

            if(getOnClosing() == null)
                getScene().getWindow().hide();

            else if(getOnClosing().closing())
                getScene().getWindow().hide();

        });

        controlMaximize.setOnMouseClicked(e -> {
            toggleMaximize();
        });



        headerPane.setOnMousePressed(e -> {

            mouseDragX = e.getX();
            mouseDragY = e.getY();
            mouseDragging = true;

        });


        headerPane.setOnMouseClicked(e -> {

            if(e.getClickCount() > 1)
                toggleMaximize();

        });


        headerPane.setOnMouseDragged(e -> {

            Stage stage = (Stage) getScene().getWindow();

            stage.setX(e.getScreenX() - mouseDragX);
            stage.setY(e.getScreenY() - mouseDragY);
            stage.setOpacity(0.8f);

        });


        headerPane.setOnMouseReleased(e -> {

            mouseDragX = 0;
            mouseDragY = 0;
            mouseDragging = false;

            getScene().getWindow().setOpacity(1f);

        });



        modalProperty().addListener((v, o, n) -> {

            switch(n.intValue()) {

                case MODAL_WINDOW:
                    controlMinimize.setVisible(true);
                    controlMaximize.setVisible(true);
                    controlExit.setVisible(true);
                    break;

                case MODAL_DIALOG:
                    controlMinimize.setVisible(false);
                    controlMaximize.setVisible(false);
                    controlExit.setVisible(true);
                    break;

                case MODAL_UNDECORATED:
                    controlMinimize.setVisible(false);
                    controlMaximize.setVisible(false);
                    controlExit.setVisible(false);
                    break;

                default:
                    throw new UnsupportedOperationException();

            }

        });


        modalProperty().setValue(MODAL_WINDOW);

        toggleDarkMode.setSelected(Application.getInstance().getTheme().getStyleName().contains("dark"));
        toggleDarkMode.setFocusTraversable(false);

        toggleDarkMode.visibleProperty().bind(showLogoProperty());
        iconDarkMode.visibleProperty().bind(showDarkModeProperty());
        awesomeLogo.visibleProperty().bind(showLogoProperty());
        statusBar.visibleProperty().bind(showStatusBarProperty());
        resizeHandle.visibleProperty().bind(resizableProperty());

        statusText.textProperty().bind(Application.getInstance().getStatus().textProperty());
        statusIcon.glyphNameProperty().bind(Application.getInstance().getStatus().iconProperty());

        contentPaneContainer.getChildren().add(contentPane);

        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() {


        Resources.getCSS(this, "/css/window-header-icon.css");
        Resources.getCSS(this, "/css/window-header-control-box.css");
        Resources.getCSS(this, "/css/window-pane.css");
        Resources.getCSS(this, "/css/status-bar.css");


        resizeHandle.setOnMouseReleased(e -> {
            setCursor(Cursor.DEFAULT);
            mouseResizing = false;
        });

        resizeHandle.setOnMouseExited(e -> {
            if(!mouseResizing)
                setCursor(Cursor.DEFAULT);
        });

        resizeHandle.setOnMouseDragged(e -> {

            setCursor(Cursor.SE_RESIZE);

            if(!mouseDragging) {

                Stage stage = (Stage) getScene().getWindow();

                stage.setWidth(e.getSceneX());
                stage.setHeight(e.getSceneY());


                if(stage.getWidth() < stage.getMinWidth())
                    stage.setWidth(stage.getMinWidth());

                if(stage.getHeight() < stage.getMinHeight())
                    stage.setHeight(stage.getMinHeight());

                if(stage.getWidth() > Screen.getPrimary().getBounds().getWidth())
                    stage.setWidth(Screen.getPrimary().getBounds().getWidth());

                if(stage.getHeight() > Screen.getPrimary().getBounds().getHeight())
                    stage.setHeight(Screen.getPrimary().getBounds().getHeight());


                mouseResizing = true;

            }

        });



        if(!Objects.isNull(getScene().getWindow()))
            resizeHandle.visibleProperty().bind(Bindings.not(((Stage) getScene().getWindow()).maximizedProperty()));

        resizeHandle.setOnMouseEntered(e -> setCursor(Cursor.SE_RESIZE));
        resizeHandle.setPickOnBounds(true);

    }


    private void toggleMaximize() {

        Stage stage = (Stage) getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());

        // FIXME: test MacOSX
        if (System.getProperty("os.name").contains("Windows")) {
            if (stage.isMaximized())
                stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
        }

    }



    public int getModal() {
        return modal.get();
    }

    public IntegerProperty modalProperty() {
        return modal;
    }

    public void setModal(int modal) {
        this.modal.set(modal);
    }

    public boolean isShowDarkMode() {
        return showDarkMode.get();
    }

    public BooleanProperty showDarkModeProperty() {
        return showDarkMode;
    }

    public void setShowDarkMode(boolean showDarkMode) {
        this.showDarkMode.set(showDarkMode);
    }

    public boolean isShowLogo() {
        return showLogo.get();
    }

    public BooleanProperty showLogoProperty() {
        return showLogo;
    }

    public void setShowLogo(boolean showLogo) {
        this.showLogo.set(showLogo);
    }

    public NefeleContentPane getContentPane() {
        return contentPane;
    }

    public boolean isResizable() {
        return resizable.get();
    }

    public BooleanProperty resizableProperty() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable.set(resizable);
    }

    public boolean isShowStatusBar() {
        return showStatusBar.get();
    }

    public BooleanProperty showStatusBarProperty() {
        return showStatusBar;
    }

    public void setShowStatusBar(boolean showStatusBar) {
        this.showStatusBar.set(showStatusBar);
    }

    public NefelePaneClosingOperation getOnClosing() {
        return onClosing.get();
    }

    public ObjectProperty<NefelePaneClosingOperation> onClosingProperty() {
        return onClosing;
    }

    public void setOnClosing(NefelePaneClosingOperation onClosing) {
        this.onClosing.set(onClosing);
    }
}
