package com.college.backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility for establishing JDBC connections to the application database.
 * Credentials and URL are retrieved safely from environment variables or system properties.
 * Plaintext passwords are never hardcoded.
 */
public class DBConnection {

    public static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/college_placement?useSSL=false&serverTimezone=UTC";
    public static final String DEFAULT_USER = "root";

    public static String getUrl() {
        String sysProp = System.getProperty("db.url");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        return DEFAULT_URL;
    }

    public static String getUser() {
        String sysProp = System.getProperty("db.user");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) {
            return envUser.trim();
        }
        return DEFAULT_USER;
    }

    public static String getPassword() {
        String sysProp = System.getProperty("db.password");
        if (sysProp != null) {
            return sysProp;
        }
        return System.getenv("DB_PASSWORD");
    }

    // Get database connection
    public static Connection getConnection() throws SQLException {
        String url = getUrl();
        String user = getUser();
        String password = getPassword();

        if (password == null || password.trim().isEmpty()) {
            throw new SQLException(
                    "Database password is not configured. Please set the DB_PASSWORD environment variable or db.password system property."
            );
        }

        try {
            // Explicitly load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }

        return DriverManager.getConnection(url, user, password);
    }
}
