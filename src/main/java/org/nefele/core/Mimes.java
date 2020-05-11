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

package org.nefele.core;

import org.nefele.Application;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class Mimes {

    public static Optional<Mime> getByExtension(String extension) {

        AtomicReference<Optional<Mime>> result = new AtomicReference<>(Optional.empty());

        try {

            Application.getInstance().getDatabase().query(
                    "SELECT * FROM mime WHERE extension = ?",
                    s -> s.setString(1, extension),
                    r -> {

                        result.set(Optional.of(new Mime(
                                r.getString(1),
                                r.getString(2),
                                r.getString(3),
                                r.getString(4)
                        )));

                    }
            );

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());
        }


        return result.get();

    }

    public static Optional<Mime> getByType(String type) {

        AtomicReference<Optional<Mime>> result = new AtomicReference<>();

        try {

            Application.getInstance().getDatabase().query(
                    "SELECT * FROM mime WHERE type = ?",
                    s -> s.setString(1, type),
                    r -> {

                        result.set(Optional.of(new Mime(
                                r.getString(1),
                                r.getString(2),
                                r.getString(3),
                                r.getString(4)
                        )));

                    }
            );

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());
        }


        return result.get();

    }

}
