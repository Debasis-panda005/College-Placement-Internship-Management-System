package com.college.backend.dao;

import com.college.backend.config.DBConnection;
import com.college.backend.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check registered email", e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash, role "
                + "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));

                return user;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find user by email", e);
        }
    }

    public User create(User user) {
        String sql = "INSERT INTO users "
                + "(name, email, password_hash, role) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());

            // Store a BCrypt hash, never the raw password.
            String hashedPassword = BCrypt.hashpw(
                    user.getPassword(),
                    BCrypt.gensalt()
            );

            ps.setString(3, hashedPassword);

            String role = user.getRole();
            ps.setString(
                    4,
                    role == null || role.isBlank() ? "STUDENT" : role
            );

            int rowsInserted = ps.executeUpdate();

            if (rowsInserted != 1) {
                throw new SQLException("User registration did not insert a row.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                } else {
                    throw new SQLException(
                            "User was inserted, but no generated ID was returned.");
                }
            }

            user.setPassword(hashedPassword);
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }
}