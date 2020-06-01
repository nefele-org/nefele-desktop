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

import java.sql.SQLException;
import java.util.ArrayList;

public final class Mimes implements ApplicationService {

    private final static Mimes instance = new Mimes();

    public static Mimes getInstance() {
        return instance;
    }



    private final ArrayList<Mime> mimes;

    private Mimes() {
        mimes = new ArrayList<>();
    }




    public ArrayList<Mime> getMimes() {
        return mimes;
    }

    public Mime getByExtension(String filename) {

        if(filename.contains("."))
            filename = filename.substring(filename.lastIndexOf("."));
        else
            filename = "*";


        final String extension = filename;

        return getMimes()
                .stream()
                .filter(i -> i.getExtension().equals(extension))
                .findFirst()
                    .orElse(Mime.UNKNOWN);

    }

    public Mime getByType(String type) {

        return getMimes()
                .stream()
                .filter(i -> i.getType().equals(type))
                .findFirst()
                .orElse(Mime.UNKNOWN);

    }



    @Override
    public void initialize() {

        try {

            Application.getInstance().getDatabase().fetch (
                    "SELECT * FROM mime",
                    null,
                    r ->

                        getMimes().add(
                                new Mime(
                                        r.getString(1),
                                        r.getString(2),
                                        r.getString(3),
                                        r.getString(4))
                        )


            );

        } catch (SQLException e) {
            Application.panic(Mimes.class, e);
        }

        Application.log(Mimes.class, "Loaded %d mimes", getMimes().size());


    }

}
