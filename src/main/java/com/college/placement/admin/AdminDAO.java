
package com.college.placement.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.college.placement.internship.job.DBConnection;

public class AdminDAO {

    public int getTotalJobs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM jobs";
        return getCount(sql);
    }

    public int getTotalApplications() throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications";
        return getCount(sql);
    }

    private int getCount(String sql) throws SQLException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getConnection();
        } catch (Exception e) {
            throw new SQLException(
                "Unable to connect to the placement database.", e
            );
        }
    }
}
