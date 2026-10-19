
package com.college.placement.internship.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InternshipDAO {

    public void addInternship(Internship internship) {
        String sql = "INSERT INTO internships " +
                "(title, company, location, description, stipend, duration, " +
                "eligibility, application_deadline, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getCompany());
            ps.setString(3, internship.getLocation());
            ps.setString(4, internship.getDescription());
            ps.setString(5, internship.getStipend());
            ps.setString(6, internship.getDuration());
            ps.setString(7, internship.getEligibility());
            ps.setDate(8, internship.getApplicationDeadline());
            ps.setString(9, internship.getStatus());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Could not add internship", e);
        }
    }

    public List<Internship> getAllInternships() {
        List<Internship> internships = new ArrayList<>();
        String sql = "SELECT * FROM internships ORDER BY id DESC";

        try (Connection con = DBConnection.getConnection();
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
                internship.setEligibility(rs.getString("eligibility"));
                internship.setApplicationDeadline(
                        rs.getDate("application_deadline"));
                internship.setStatus(rs.getString("status"));

                internships.add(internship);
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not load internships", e);
        }

        return internships;
    }

    public Internship getInternshipById(int id) {
        String sql = "SELECT * FROM internships WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Internship internship = new Internship();

                    internship.setId(rs.getInt("id"));
                    internship.setTitle(rs.getString("title"));
                    internship.setCompany(rs.getString("company"));
                    internship.setLocation(rs.getString("location"));
                    internship.setDescription(rs.getString("description"));
                    internship.setStipend(rs.getString("stipend"));
                    internship.setDuration(rs.getString("duration"));
                    internship.setEligibility(rs.getString("eligibility"));
                    internship.setApplicationDeadline(
                            rs.getDate("application_deadline"));
                    internship.setStatus(rs.getString("status"));

                    return internship;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not find internship", e);
        }

        return null;
    }

    public void updateInternship(Internship internship) {
        String sql = "UPDATE internships SET title=?, company=?, location=?, " +
                "description=?, stipend=?, duration=?, eligibility=?, " +
                "application_deadline=?, status=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getCompany());
            ps.setString(3, internship.getLocation());
            ps.setString(4, internship.getDescription());
            ps.setString(5, internship.getStipend());
            ps.setString(6, internship.getDuration());
            ps.setString(7, internship.getEligibility());
            ps.setDate(8, internship.getApplicationDeadline());
            ps.setString(9, internship.getStatus());
            ps.setInt(10, internship.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Could not update internship", e);
        }
    }

    public void deleteInternship(int id) {
        String sql = "DELETE FROM internships WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Could not delete internship", e);
        }
    }
}
