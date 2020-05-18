package org.nefele.ui.dialog;

import javafx.scene.image.Image;
import org.nefele.Resources;

import static java.util.Objects.requireNonNull;

public class InfoDialog extends BaseDialog {
    public InfoDialog(String title, String message) {
        super(title, message);
        setIcon(new Image(Resources.getURL(this, "/images/dialog_info.png").toExternalForm()));
    }

}