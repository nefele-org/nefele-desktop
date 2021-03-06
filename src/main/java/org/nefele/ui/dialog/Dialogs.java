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

import javafx.application.Platform;
import org.nefele.Application;

public final class Dialogs {

    private static <T extends BaseDialog> int showMessageBoxImpl(BaseDialog baseDialog, int... buttons) {

        if(!Platform.isFxApplicationThread())
            throw new IllegalStateException("Wrong thread for MessageBox!");


        for(var i : buttons)
            baseDialog.getButtons().add(i);

        baseDialog.showAndWait();


        return baseDialog.getDialogResult();

    }


    public static int showMessageBox(BaseDialog baseDialog, int... buttons) {
        return showMessageBoxImpl(baseDialog, buttons);
    }

    public static int showMessageBox(String title, String message, int... buttons) {
        return showMessageBoxImpl(
                new BaseDialog(title, message), buttons.length > 0 ? buttons : new int[] { BaseDialog.DIALOG_OK });
    }

    public static int showInfoBox(String title, String message, int... buttons) {
        return showMessageBoxImpl(
                new InfoDialog(title, message), buttons.length > 0 ? buttons : new int[] { BaseDialog.DIALOG_OK });
    }

    public static int showErrorBox(String title, String message, int... buttons) {
        return showMessageBoxImpl(
                new ErrorDialog(title, message), buttons.length > 0 ? buttons : new int[] { BaseDialog.DIALOG_OK });
    }

    public static int showWarningBox(String title, String message, int... buttons) {
        return showMessageBoxImpl(
                new WarningDialog(title, message), buttons.length > 0 ? buttons : new int[] { BaseDialog.DIALOG_OK });
    }


    public static InputDialogResult showInputBox(String title, int... buttons) {
        return showInputBox(title, new String[0], buttons);
    }

    public static InputDialogResult showInputBox(String title, String prompt, int... buttons) {
        return showInputBox(title, new String[] { prompt }, buttons);
    }

    public static InputDialogResult showInputBox(String title, String[] prompts, int... buttons) {

        InputDialog inputDialog = new InputDialog(title, prompts);

        for(var i : buttons)
            inputDialog.getButtons().add(i);

        inputDialog.showAndWait();
        return inputDialog.getDialogResult();

    }




    public static int showMessageBox(String message) {
        return showMessageBox(Application.getInstance().getLocale().get("DIALOG_TITLE_MESSAGE"), message);
    }

    public static int showInfoBox(String message) {
        return showInfoBox(Application.getInstance().getLocale().get("DIALOG_TITLE_INFO"), message);
    }

    public static int showErrorBox(String message) {
        return showErrorBox(Application.getInstance().getLocale().get("DIALOG_TITLE_ERROR"), message);
    }

    public static int showWarningBox(String message) {
        return showWarningBox(Application.getInstance().getLocale().get("DIALOG_TITLE_ERROR"), message);
    }


}