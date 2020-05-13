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
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class Config {

    protected Database database;
    protected HashMap<String, Object> cache;

    public Config(Database database) {
        this.database = database;
        this.cache = new HashMap<>();
    }


    public Optional<Object> get(String name) {

        Object cached;
        if((cached = cache.get(name)) != null)
            return Optional.of(cached);


        AtomicReference<Optional<Object>> result = new AtomicReference<>();

        try {

            database.query("SELECT value FROM config WHERE name = ?",
                    s -> s.setString(1, name),
                    r -> result.set(Optional.of(r.getObject(1)))
            );

            if(result.get().isPresent())
                cache.put(name, result.get().get());

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());
        }

        return result.get();

    }


    public void set(String name, Object value) {

        cache.put(name, value);
        update();

    }


    public HashMap<String, Object> list() {

        try {

            database.query("SELECT name, value FROM config",
                    null,
                    r -> {

                        if(!cache.containsKey(r.getString(1)))
                            cache.put(r.getString(1), r.getObject(2));

                    }
            );

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());
        }


        return cache;

    }


    public void update() {

        try {

            for(String key : cache.keySet()) {

                if(get(key).isPresent()) {
                    database.query("UPDATE config SET value = ? WHERE name = ?",
                            s -> {
                                s.setString(1, (String) get(key).get());
                                s.setString(2, key);
                            },
                            null
                    );
                }

            }

        } catch (SQLException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());
        }

    }


    public Optional<String> getString(String name) {
        return get(name).map(Object::toString);
    }

    public Optional<Integer> getInteger(String name) {
        return get(name).map(o -> Integer.parseInt(o.toString()));
    }

    public Optional<Long> getLong(String name) {
        return get(name).map(o -> Long.parseLong(o.toString()));
    }


    public Optional<Boolean> getBoolean(String name) {
        return get(name).map(o -> Boolean.parseBoolean(o.toString()));
    }


    public void setString(String name, String value) {
        set(name, value);
    }

    public void setInteger(String name, Integer value) {
        set(name, String.valueOf(value));
    }

    public void SetBoolean(String name, Boolean value) {
        set(name, value ? "1" : "0");
    }

}
