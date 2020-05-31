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

import org.sqlite.SQLiteErrorCode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class Database {

    private final String CONNECTION_URL;

    public Database() {

        CONNECTION_URL = String.format("jdbc:sqlite:%s",
                Application.getInstance().getDataPath().resolve("nefele.db"));

    }


    private Connection connect() {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException e) {

            // TODO: Handle exception
            Application.panic(getClass(), e);

        }

        return connection;

    }



    public void fetch(String sql, DatabasePrepare onPrepare, DatabaseResult onResult) throws SQLException {

        try {

            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setFetchSize(5000);


                if (onPrepare != null)
                    onPrepare.run(stmt);


                if (onResult != null) {

                    ResultSet rset = stmt.executeQuery();

                    while (rset.next())
                        onResult.run(rset);

                    rset.close();

                }

            }


        } catch (SQLException e) {

            if(e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code)
                fetch(sql, onPrepare, onResult);

            else if(e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code)
                Application.panic(Database.class, "Database is gone, rest in peace :( (%s)", e.getMessage());

            else
                throw e;
        }


    }


    public int update(String sql, DatabasePrepare onPrepare, boolean batch) throws SQLException {

        try {

            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                if (onPrepare != null)
                    onPrepare.run(stmt);

                if(batch)
                    stmt.executeBatch();
                else
                    return stmt.executeUpdate();

                return 0;

            }

        } catch (SQLException e) {

            if(e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code)
                return update(sql, onPrepare, batch);

            else if(e.getErrorCode() == SQLiteErrorCode.SQLITE_LOCKED.code)
                return update(sql, onPrepare, batch);

            else if(e.getMessage().equals("database is locked"))    // SQLState: null, ErrorCode: 0...
                return update(sql, onPrepare, batch);

            else if(e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code)
                Application.panic(Database.class, "Database is gone, rest in peace :( [%s(%d)] %s", e.getSQLState(), e.getErrorCode(), e.getMessage());

            else {
                Application.log(Database.class, e, "[%s(%d)]", e.getSQLState(), e.getErrorCode());
                throw e;
            }

        }


        throw new IllegalStateException();

    }



}