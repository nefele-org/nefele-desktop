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

import java.sql.*;

public class Database {

    protected final static String CONNECTION_URL = "jdbc:sqlite:nefele.db";


    protected Connection connect() {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException e) {

            // TODO: Handle exception
            Application.panic(getClass(), e);

        }

        return connection;

    }

    

    public int query(String sql, DatabasePrepare onPrepare, DatabaseResult onResult) throws SQLException {

        try(Connection conn = connect();
            PreparedStatement  stmt = conn.prepareStatement(sql)) {

            if(onPrepare != null)
                onPrepare.run(stmt);



            if(onResult != null) {

                ResultSet rset = stmt.executeQuery();

                while(rset.next())
                    onResult.run(rset);

                rset.close();

            } else
                return stmt.executeUpdate();

        }

        return 0;

    }

}
