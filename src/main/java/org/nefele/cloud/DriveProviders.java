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
import org.nefele.utils.IdUtils;
import org.sqlite.SQLiteErrorCode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;



public final class DriveProviders implements Service {

    private final static DriveProviders instance = new DriveProviders();

    public static DriveProviders getInstance() {
        return instance;
    }




    private final ObservableList<DriveProvider> driveProviders;
    private final ArrayList<DriveProvider> dustDriveProviders;

    private DriveProviders() {
        driveProviders = FXCollections.observableArrayList();
        dustDriveProviders = new ArrayList<>();
    }





    public DriveProvider fromId(String id) throws DriveNotFoundException {

        return getDriveProviders()
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

                        Application.panic(DriveProvider.class, e);

                    }



                    try {

                        return add(id, service.get(), description.get(), quota.get(), blocks.get());

                    } catch (DriveNotFoundException e) {
                        Application.panic(DriveProvider.class, e);
                    }


                    throw new IllegalStateException();

                });

    }


    public DriveProvider add(String id, String service, String description, long quota, long blocks) throws DriveNotFoundException {

        DriveProvider provider;

        switch (service) {

            case OfflineDriveProvider.SERVICE_ID:
                provider = new OfflineDriveProvider(id, service, description, quota, blocks)
                        .initialize();
                break;

            case GoogleDriveProvider.SERVICE_ID:
                provider = new GoogleDriveProvider(id, service, description, quota, blocks)
                        .initialize();
                break;

            case DropboxDriveProvider.SERVICE_ID:
                provider = new DropboxDriveProvider(id, service, description, quota, blocks)
                        .initialize();
                break;

            default:
                throw new DriveNotFoundException(service);

        }

        getDriveProviders()
                .add(provider);

        provider.invalidate();
        return provider;

    }


    public DriveProvider add(String service) throws DriveNotFoundException {

        String id = IdUtils.generateId();

        switch (service) {

            case OfflineDriveProvider.SERVICE_ID:
                return add(id, service, OfflineDriveProvider.SERVICE_DEFAULT_DESCRIPTION, 0, 0);

            case GoogleDriveProvider.SERVICE_ID:
                return add(id, service, GoogleDriveProvider.SERVICE_DEFAULT_DESCRIPTION, 0, 0);

            case DropboxDriveProvider.SERVICE_ID:
                return add(id, service, DropboxDriveProvider.SERVICE_DEFAULT_DESCRIPTION, 0, 0);

            default:
                throw new DriveNotFoundException(service);

        }

    }



    public DriveProvider nextAllocatable() throws DriveFullException, DriveNotFoundException {

        if (getDriveProviders()
                .stream()
                .noneMatch(i -> i.getStatus() == DriveProvider.STATUS_READY))
            throw new DriveNotFoundException("No drives are ready or available");

        if(getDriveProviders()
                .stream()
                .filter(i -> i.getStatus() == DriveProvider.STATUS_READY)
                .mapToLong(i -> i.getQuota() - i.getUsedSpace()).sum() <= 0L)
            throw new DriveFullException("Current active drives are full");

        return getDriveProviders()
                .stream()
                .filter(i -> i.getStatus() == DriveProvider.STATUS_READY)
                .max(Comparator.comparingLong(a -> a.getQuota() - a.getUsedSpace()))
                .get();

    }

    public void remove(DriveProvider driveProvider) throws DriveNotEmptyException {

        if(driveProvider.getChunks() > 0)
            throw new DriveNotEmptyException();

        if(driveProviders.remove(driveProvider))
            dustDriveProviders.add(driveProvider);

    }

    public ObservableList<DriveProvider> getDriveProviders() {
        return driveProviders;
    }


    @Override
    public void initialize() {

        try {

            final ArrayList<String> ids = new ArrayList<>();

            Application.getInstance().getDatabase().fetch(
                    "SELECT id FROM drives",
                    null, r -> ids.add(r.getString(1)));

            for(String id : ids)
                fromId(id);


        } catch (SQLException | DriveNotFoundException e) {
            Application.panic(DriveProvider.class, e);
        }

    }

    @Override
    public void synchronize() {

        try {

            Application.getInstance().getDatabase().update(
                    "INSERT OR REPLACE INTO drives (id, service, description, quota, chunks) VALUES (?, ?, ?, ?, ?)",
                    s -> {
                        driveProviders
                                .stream()
                                .filter(DriveProvider::isDirty)
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
                        dustDriveProviders
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
    public void exit() {
        synchronize();
    }


}
