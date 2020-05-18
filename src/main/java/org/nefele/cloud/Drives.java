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

import org.nefele.Application;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class Drives {

    public static Drive fromId(String id) {

        return Application.getInstance().getDrives()
                .stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseGet(() -> {

                    final AtomicReference<String> service = new AtomicReference<>();
                    final AtomicLong quota = new AtomicLong();
                    final AtomicLong blocks = new AtomicLong();

                    try {

                        Application.getInstance().getDatabase().query (
                                "SELECT * FROM drives WHERE id = ?",
                                s -> s.setString(1, id),
                                r -> {
                                    service.set(r.getString(2));
                                    quota.set(r.getLong(3));
                                    blocks.set(r.getLong(4));
                                }
                        );

                    } catch (SQLException e) {
                        Application.panic(Drive.class, e);
                    }



                    switch (service.get()) {

                        case OfflineDriveService.SERVICE_ID:
                            return new OfflineDriveService(id, service.get())
                                    .initialize();

                        default:
                            throw new DriveNotFoundException(String.format("Service %s not found", service.get()));

                    }

                });

    }


    public static Drive nextAllocatable() {

        if(Application.getInstance().getDrives().stream().mapToLong(i -> i.getQuota() - i.getChunks()).sum() == 0)
            throw new DriveFullException();

        return Application.getInstance().getDrives()
                .stream()
                .max((a, b) -> Long.compare(a.getQuota() - a.getChunks(), b.getQuota() - b.getChunks()))
                .get();

    }

}
