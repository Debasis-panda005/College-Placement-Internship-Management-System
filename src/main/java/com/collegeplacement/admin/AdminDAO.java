package com.collegeplacement.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDAO {

    private static final String URL =
            "jdbc:mysql://localhost:3306/placementdb";

    private static final String USER = "root";

    private static final String PASSWORD = "Ankit@6463";

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        return java.sql.DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }

    public int getTotalJobs() {

        String sql = "SELECT COUNT(*) FROM jobs";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getTotalApplications() {

        String sql = "SELECT COUNT(*) FROM applications";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}