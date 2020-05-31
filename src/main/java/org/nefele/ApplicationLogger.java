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


import java.io.IOException;
import java.time.Instant;
import java.util.logging.*;

public final class ApplicationLogger {

    private final Logger logger;
    private final FileHandler fileHandler;

    public ApplicationLogger() {

        try {

            logger = Logger.getLogger(ApplicationLogger.class.getName());
            logger.setUseParentHandlers(false);


            fileHandler = new FileHandler(Application.getInstance().getDataPath()
                    .resolve("logs")
                    .resolve(Instant.now().getEpochSecond() + ".log").toString()
            );

            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord logRecord) {
                    return String.format("(%s) - %s%s",
                            logRecord.getThreadID(),
                            logRecord.getMessage(),
                            System.lineSeparator()
                    );
                }
            });

            fileHandler.setLevel(Level.ALL);


            logger.addHandler(fileHandler);

        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

    }


    public void writeLog(String message) {
        logger.info(message);
    }

    public void close() {
        fileHandler.close();
    }

}
