package org.nefele.ui.dialog;

public class InfoDialog extends BaseDialog {
    public InfoDialog(String title, String message) {
        super(title, message);
        setIcon("INFORMATION");
    }

}