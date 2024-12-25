package com.promineotech.projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.promineotech.projects.exception.DbException;

public class DbConnection {
    // Database connection details
    private static final String HOST = "localhost";
    private static final String PASSWORD = "your_password_here";
    private static final int PORT = 3306;
    private static final String SCHEMA = "projects";
    private static final String USER = "projects";
    

    public static Connection getConnection() {
        String uri = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", HOST, PORT, SCHEMA, USER, PASSWORD);

        try {
            // Attempt to establish a connection
            Connection connection = DriverManager.getConnection(uri);
            System.out.println("Connection established successfully for '" + SCHEMA + "'.");
            return connection;
        } catch (SQLException e) {
            // Handle connection failure
            System.out.println("Connection failed: " + uri);
            throw new DbException("Connection failed: \" + uri");
        }
    }
}