
package com.college.placement.company;

import com.college.placement.internship.job.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompanyDAO {

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getConnection();
        } catch (Exception e) {
            throw new SQLException("Unable to connect to the database.", e);
        }
    }

    public List<Company> getAllCompanies() throws SQLException {
        List<Company> companies = new ArrayList<>();

        String sql = "SELECT id, name, email, phone, location, website "
                   + "FROM companies ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                companies.add(new Company(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("location"),
                    rs.getString("website")
                ));
            }
        }

        return companies;
    }

    public void addCompany(Company company) throws SQLException {
        String sql = "INSERT INTO companies "
                   + "(name, email, phone, location, website) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, company.getName());
            ps.setString(2, company.getEmail());
            ps.setString(3, company.getPhone());
            ps.setString(4, company.getLocation());
            ps.setString(5, company.getWebsite());

            ps.executeUpdate();
        }
    }

    public Company getCompanyById(int id) throws SQLException {
        String sql = "SELECT id, name, email, phone, location, website "
                   + "FROM companies WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Company(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("location"),
                        rs.getString("website")
                    );
                }
            }
        }

        return null;
    }

    public boolean updateCompany(Company company) throws SQLException {
        String sql = "UPDATE companies SET name = ?, email = ?, "
                   + "phone = ?, location = ?, website = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, company.getName());
            ps.setString(2, company.getEmail());
            ps.setString(3, company.getPhone());
            ps.setString(4, company.getLocation());
            ps.setString(5, company.getWebsite());
            ps.setInt(6, company.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCompany(int id) throws SQLException {
        String sql = "DELETE FROM companies WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
