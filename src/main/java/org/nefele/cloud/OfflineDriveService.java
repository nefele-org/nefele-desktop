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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class OfflineDriveService implements CloudService {

    private long currentSpace;


    OfflineDriveService() {
        currentSpace = 0;
    }


    @Override
    public String getServiceName() {
        return "offline-drive-service";
    }

    @Override
    public InputStream readChunk(String id) {

        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(new File(String.valueOf(Paths.get("offline-drive", id))));
        } catch (FileNotFoundException e) {
            Application.log(e.getClass(), e.getMessage());
        }

        return inputStream;

    }

    @Override
    public OutputStream writeChunk(String id) {

        OutputStream outputStream = null;


        if(!Files.exists(Paths.get("offline-drive", id))) {
            try {
                Files.createFile(Paths.get("offline-drive", id));
            } catch (IOException e) {
                Application.log(e.getClass(), e.getLocalizedMessage());
            }
        }

        try {
            outputStream = new FileOutputStream(new File(String.valueOf(Paths.get("offline-drive", id))));
        } catch (FileNotFoundException e) {
            Application.log(e.getClass(), e.getLocalizedMessage());
        }

        return outputStream;

    }



    @Override
    public long getFreeSpace() {
        return Long.MAX_VALUE;
    }

}
