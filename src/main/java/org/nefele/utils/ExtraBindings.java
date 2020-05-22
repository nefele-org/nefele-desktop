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

package org.nefele.utils;

import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import java.util.concurrent.Callable;


public final class ExtraBindings {

    public static <T extends Number> StringBinding createSizeBinding(Callable<T> func, String suffix, Observable... dependencies) {

        return javafx.beans.binding.Bindings.createStringBinding(() -> {

            final long value = func.call().longValue();


            if(value < 1024L)
                return String.format("%d bytes%s", value, suffix);

            else if(value < (1024L * 1024L))
                return String.format("%d kB%s", value / 1024L, suffix);

            else if(value < (1024L * 1024L * 1024L))
                return String.format("%d MB%s", value / 1024L / 1024L, suffix);

            else if(value < (1024L * 1024L * 1024L * 1024L))
                return String.format("%d GB%s", value / 1024L / 1024L / 1024L, suffix);

            else
                return String.format("%d TB%s", value / 1024L / 1024L / 1024L / 1024L, suffix);



        }, dependencies);

    }


    public static <T extends Number> StringBinding createTimeBinding(Callable<T> func, Observable... dependencies) {

        return javafx.beans.binding.Bindings.createStringBinding(() -> {

            final int value = func.call().intValue();


            if(value == 0)
                return "∞";

            else if(value < 60)
                return String.format("%d s", value);

            else if(value < (60 * 60))
                return String.format("%d m", value / 60);

            else
                return String.format("%d h", value / 60 / 60);


        }, dependencies);

    }


    public static <T extends Number> StringBinding createPercentageBinding(Callable<T> func, Observable... dependencies) {

        return javafx.beans.binding.Bindings.createStringBinding(() ->
            String.format("%d %%", func.call().intValue())
        , dependencies);

    }

}
