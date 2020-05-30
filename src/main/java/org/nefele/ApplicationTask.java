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

package org.nefele;


import javafx.concurrent.Task;

public abstract class ApplicationTask extends Task<Void> {

    public void updateProgress(long max, long current) {
        super.updateProgress(max, current);
    }

    public void updateProgress(double max, double current) {
        super.updateProgress(max, current);
    }

    public void updateMessage(String message) {
        super.updateMessage(message);
    }

    public void updateTitle(String title) {
        super.updateTitle(title);
    }

    public void updateValue(Void value) {
        super.updateValue(value);
    }

    public void set(Void aVoid) {
        super.set(aVoid);
    }

    public void setException(Throwable t) {
        super.setException(t);
    }

    public boolean runAndReset() {
        return super.runAndReset();
    }

    protected abstract Void call() throws Exception;

}