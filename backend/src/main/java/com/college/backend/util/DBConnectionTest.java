package com.college.backend.util;

import java.sql.Connection;

public class DBConnectionTest {

    public static void main(String[] args) {

        try {
            Connection connection = DBConnection.getConnection();

            if (connection != null) {
                System.out.println("Database connected successfully!");
                connection.close();
            }

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}