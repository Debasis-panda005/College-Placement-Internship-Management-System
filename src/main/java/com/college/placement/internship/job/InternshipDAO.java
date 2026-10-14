
package com.college.placement.internship.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InternshipDAO {

    // ADD INTERNSHIP
    public boolean addInternship(Internship internship) {
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

            if (internship.getApplicationDeadline() != null) {
                ps.setDate(8, internship.getApplicationDeadline());
            } else {
                ps.setNull(8, java.sql.Types.DATE);
            }

            ps.setString(9, internship.getStatus() == null
                    ? "Active" : internship.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // VIEW ALL INTERNSHIPS
    public List<Internship> getAllInternships() {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT * FROM internships ORDER BY id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapInternship(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // GET INTERNSHIP BY ID
    public Internship getInternshipById(int id) {
        String sql = "SELECT * FROM internships WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapInternship(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE INTERNSHIP
    public boolean updateInternship(Internship internship) {
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

            if (internship.getApplicationDeadline() != null) {
                ps.setDate(8, internship.getApplicationDeadline());
            } else {
                ps.setNull(8, java.sql.Types.DATE);
            }

            ps.setString(9, internship.getStatus());
            ps.setInt(10, internship.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // DELETE INTERNSHIP
    public boolean deleteInternship(int id) {
        String sql = "DELETE FROM internships WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // MAP DATABASE ROW TO JAVA OBJECT
    private Internship mapInternship(ResultSet rs)
            throws java.sql.SQLException {

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
