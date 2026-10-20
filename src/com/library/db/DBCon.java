package com.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//JDBC connection
public final class DBCon {
    private static final String URL = "jdbc:mysql://localhost:3306/library_management";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private DBCon() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
