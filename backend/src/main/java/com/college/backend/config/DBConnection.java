package com.college.backend.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "application.properties was not found");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || username == null || password == null
                || url.isBlank() || username.isBlank() || password.isBlank()) {
            throw new SQLException(
                    "Database configuration is missing. Check db.url, "
                            + "db.username and the DB_PASSWORD environment variable.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver could not be loaded.", e);
        }

        return DriverManager.getConnection(url, username, password);
    }
}