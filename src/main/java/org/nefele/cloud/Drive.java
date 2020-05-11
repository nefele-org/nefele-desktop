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

package org.nefele.cloud;

import javafx.beans.property.*;
import org.nefele.Application;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;


public class Drive {

    public static final Integer DEFAULT_BLOCK_SIZE = 4096;

    private final ReadOnlyIntegerProperty id;
    private final ReadOnlyStringProperty service;
    private final LongProperty quota;
    private final LongProperty blocks;


    public static Drive fromId(int id) throws SQLException {

        final AtomicReference<String> service = new AtomicReference<>();
        final AtomicLong quota = new AtomicLong();
        final AtomicLong blocks = new AtomicLong();

        Application.getInstance().getDatabase().query (
                "SELECT * FROM drives WHERE id = ?",
                s -> s.setInt(1, id),
                r -> {
                    service.set(r.getString(2));
                    quota.set(r.getLong(3));
                    blocks.set(r.getLong(4));
                }
        );

        requireNonNull(service.get(), String.format("id %d not found on database", id));

        return new Drive(id, service.get(), quota.get(), blocks.get());
    }

    public Drive(int id, String service, long quota, long blocks) {
        this.id = new SimpleIntegerProperty(id);
        this.service = new SimpleStringProperty(service);
        this.quota = new SimpleLongProperty(quota);
        this.blocks = new SimpleLongProperty(blocks);
    }


    public int getId() {
        return id.get();
    }

    public ReadOnlyIntegerProperty idProperty() {
        return id;
    }

    public String getService() {
        return service.get();
    }

    public ReadOnlyStringProperty serviceProperty() {
        return service;
    }

    public long getQuota() {
        return quota.get();
    }

    public LongProperty quotaProperty() {
        return quota;
    }

    public void setQuota(long quota) {
        this.quota.set(quota);
    }

    public long getBlocks() {
        return blocks.get();
    }

    public LongProperty blocksProperty() {
        return blocks;
    }

    public void setBlocks(long blocks) {
        this.blocks.set(blocks);
    }
}
