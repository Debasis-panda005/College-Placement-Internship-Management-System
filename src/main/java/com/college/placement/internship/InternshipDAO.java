
package com.college.placement.internship;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.college.placement.internship.job.DBConnection;

public class InternshipDAO {

    // Safely obtain the database connection
    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getConnection();
        } catch (Exception e) {
            throw new SQLException(
                "Unable to connect to the placement database.", e
            );
        }
    }

    // Retrieve all internships
    public List<Internship> getAllInternships() throws SQLException {
        List<Internship> internships = new ArrayList<>();

        String sql = "SELECT id, title, company, location, description, "
                   + "stipend, duration, status FROM internships "
                   + "ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Internship internship = new Internship();

                internship.setId(rs.getInt("id"));
                internship.setTitle(rs.getString("title"));
                internship.setCompany(rs.getString("company"));
                internship.setLocation(rs.getString("location"));
                internship.setDescription(rs.getString("description"));
                internship.setStipend(rs.getString("stipend"));
                internship.setDuration(rs.getString("duration"));
                internship.setStatus(rs.getString("status"));

                internships.add(internship);
            }
        }

        return internships;
    }

    // Add a new internship
    public void addInternship(Internship internship) throws SQLException {
        String sql = "INSERT INTO internships "
                   + "(title, company, location, description, stipend, "
                   + "duration, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getCompany());
            ps.setString(3, internship.getLocation());
            ps.setString(4, internship.getDescription());
            ps.setString(5, internship.getStipend());
            ps.setString(6, internship.getDuration());
            ps.setString(7, internship.getStatus());

            ps.executeUpdate();
        }
    }

    // Delete an internship using its ID
    public boolean deleteInternship(int id) throws SQLException {
        String sql = "DELETE FROM internships WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
