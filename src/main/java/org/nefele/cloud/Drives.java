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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nefele.Application;
import org.nefele.Service;
import org.sqlite.SQLiteErrorCode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;



public final class Drives implements Service {

    private final static Drives instance = new Drives();

    public static Drives getInstance() {
        return instance;
    }



    private final ObservableList<Drive> drives;
    private final ArrayList<Drive> dustDrives;

    private Drives() {
        drives = FXCollections.observableArrayList();
        dustDrives = new ArrayList<>();
    }





    public Drive fromId(String id) throws DriveNotFoundException {

        return getDrives()
                .stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseGet(() -> {

                    final AtomicReference<String> service = new AtomicReference<>();
                    final AtomicReference<String> description = new AtomicReference<>();
                    final AtomicLong quota = new AtomicLong();
                    final AtomicLong blocks = new AtomicLong();

                    try {

                        Application.getInstance().getDatabase().fetch (
                                "SELECT * FROM drives WHERE id = ?",
                                s -> s.setString(1, id),
                                r -> {
                                    service.set(r.getString(2));
                                    quota.set(r.getLong(3));
                                    blocks.set(r.getLong(4));
                                    description.set(r.getString(5));
                                }
                        );

                    } catch (SQLException e) {

                        if(e.getErrorCode() == SQLiteErrorCode.SQLITE_NOTFOUND.code)
                            throw new IllegalStateException();

                        Application.panic(Drive.class, e);

                    }



                    switch (service.get()) {

                        case OfflineDriveProvider.SERVICE_ID:
                            return new OfflineDriveProvider(id, service.get(), description.get(), quota.get(), blocks.get())
                                    .initialize();

                        case GoogleDriveProvider.SERVICE_ID:
                            return new GoogleDriveProvider(id, service.get(), description.get(), quota.get(), blocks.get())
                                    .initialize();

                        case DropboxDriveProvider.SERVICE_ID:
                            return new DropboxDriveProvider(id, service.get(), description.get(), quota.get(), blocks.get())
                                    .initialize();

                        default:
                            throw new IllegalStateException();

                    }

                });

    }


    public Drive nextAllocatable() throws DriveFullException, DriveNotFoundException {

        if (getDrives()
                .stream()
                .noneMatch(i -> i.getStatus() == Drive.STATUS_READY))
            throw new DriveNotFoundException("No drive is ready or available");

        if(getDrives()
                .stream()
                .filter(i -> i.getStatus() == Drive.STATUS_READY)
                .mapToLong(i -> i.getQuota() - i.getChunks()).sum() == 0)
            throw new DriveFullException();

        return getDrives()
                .stream()
                .filter(i -> i.getStatus() == Drive.STATUS_READY)
                .max(Comparator.comparingLong(a -> a.getQuota() - a.getChunks()))
                .get();

    }

    public void remove(Drive drive) throws DriveNotEmptyException {

        if(drive.getChunks() > 0)
            throw new DriveNotEmptyException();

        if(drives.remove(drive))
            dustDrives.add(drive);

    }

    public ObservableList<Drive> getDrives() {
        return drives;
    }


    @Override
    public void initialize(Application app) {

        try {

            final ArrayList<String> ids = new ArrayList<>();

            app.getDatabase().fetch(
                    "SELECT id FROM drives",
                    null, r -> ids.add(r.getString(1)));

            for(String id : ids)
                getDrives().add(fromId(id));


        } catch (SQLException | DriveNotFoundException e) {
            Application.panic(Drive.class, e);
        }

    }

    @Override
    public void synchronize(Application app) {

        try {

            Application.getInstance().getDatabase().update(
                    "INSERT OR REPLACE INTO drives (id, service, description, quota, chunks) VALUES (?, ?, ?, ?, ?)",
                    s -> {
                        drives
                                .stream()
                                .filter(Drive::isDirty)
                                .forEach(i -> {

                                    try {

                                        s.setString(1, i.getId());
                                        s.setString(2, i.getService());
                                        s.setString(3, i.getDescription());
                                        s.setLong(4, i.getQuota());
                                        s.setLong(5, i.getChunks());
                                        s.addBatch();

                                        i.validate();

                                    } catch (SQLException e) {
                                        Application.panic(getClass(), e);
                                    }

                                });
                    }, true
            );


            Application.getInstance().getDatabase().update(
                    "DELETE FROM drives WHERE id = ?",
                    s -> {
                        dustDrives
                                .forEach(i -> {

                                    try {

                                        s.setString(1, i.getId());
                                        s.addBatch();

                                        i.validate();

                                    } catch (SQLException e) {
                                        Application.panic(getClass(), e);
                                    }

                                });
                    }, true
            );

        } catch (SQLException e) {
            Application.panic(getClass(), e);
        }


    }

    @Override
    public void exit(Application app) {
        synchronize(app);
    }

}
