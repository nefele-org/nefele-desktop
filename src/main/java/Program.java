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

import org.nefele.Application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Program {

//    static {
//        System.setProperty("java.library.path", "build/libs/hello/shared"); // FIXME
//        System.loadLibrary("hello");
//    }


    public static void main(String[] args) {

        try {


            if(System.getProperty("user.home").isEmpty())
                throw new IllegalStateException("USER_HOME is not set");


            Path dataPath = Paths.get(System.getProperty("user.home"), ".nefele");

            if(Files.notExists(dataPath))
                Files.createDirectory(dataPath);

            if (Files.notExists(dataPath.resolve(Paths.get("cache"))))
                Files.createDirectory(dataPath.resolve(Paths.get("cache")));

            if(Files.notExists(dataPath.resolve(Paths.get("nefele.db"))))
                Files.write(dataPath.resolve(Paths.get("nefele.db")), Program.class.getResourceAsStream("/setup/nefele.db").readAllBytes());


        } catch(IOException e) {
            System.err.println(String.format("Fatal error: %s %s", e.getClass().getName(), e.getMessage()));
            e.printStackTrace();
        }

        javafx.application.Application.launch(Application.class, args);
    }

}
