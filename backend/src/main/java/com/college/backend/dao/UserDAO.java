package com.college.backend.dao;

import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.DBConnection;
import com.college.backend.util.PasswordHashService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Data Access Object for User entities.
 * Handles database operations for user accounts using direct JDBC and PreparedStatements.
 * Distinguishes invalid input (IllegalArgumentException), duplicate records (DuplicateRecordException),
 * database failure (DAOException), and missing records (returns null).
 */
public class UserDAO {

    /**
     * Finds a user by their primary key ID.
     *
     * @param id the user ID
     * @return the User object if found, null if record does not exist or id is null
     * @throws DAOException on database connectivity or query failure
     */
    public User findById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT id, name, email, password_hash, role, created_at FROM users WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding user by ID " + id + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds a user by their unique email address.
     * Automatically normalizes the email before querying.
     *
     * @param email candidate email string
     * @return the User object if found, null if not found or email is blank
     * @throws DAOException on database connectivity or query failure
     */
    public User findByEmail(String email) {
        String normalizedEmail = User.normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return null;
        }

        String sql = "SELECT id, name, email, password_hash, role, created_at FROM users WHERE email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedEmail);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding user by email '" + normalizedEmail + "': " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Checks whether an account with the specified email already exists.
     * Automatically normalizes the email before querying.
     *
     * @param email candidate email string
     * @return true if an account exists, false otherwise
     * @throws DAOException on database connectivity or query failure
     */
    public boolean existsByEmail(String email) {
        String normalizedEmail = User.normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedEmail);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error checking user email '" + normalizedEmail + "': " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Inserts a new user record.
     * Enforces input validation (rejecting plaintext passwords, invalid roles, blank emails),
     * duplicate record detection (throwing DuplicateRecordException), and translates
     * SQLExceptions to DAOException.
     *
     * @param user the User to create
     * @return true if successfully inserted
     * @throws IllegalArgumentException on invalid input (null user, blank email, invalid role, non-BCrypt hash)
     * @throws DuplicateRecordException if the email already exists in the database
     * @throws DAOException on database connectivity or query failure
     */
    public boolean createUser(User user) {
        validateUser(user);

        try (Connection connection = DBConnection.getConnection()) {
            return createUser(connection, user);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("User with email '" + user.getEmail() + "' already exists.", e);
            }
            throw new DAOException("Database error creating user: " + e.getMessage(), e);
        }
    }

    /**
     * Validates user entity fields before database operations.
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User entity cannot be null.");
        }

        String normalizedEmail = User.normalizeEmail(user.getEmail());
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty.");
        }
        user.setEmail(normalizedEmail);

        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new IllegalArgumentException("User password hash cannot be null or empty.");
        }

        // Validate BCrypt hash format to reject plaintext passwords accidentally supplied
        if (!PasswordHashService.isBCryptHash(user.getPasswordHash())) {
            throw new IllegalArgumentException("Password hash must be a valid BCrypt hash. Plaintext passwords or malformed hashes are rejected.");
        }

        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = User.ROLE_STUDENT;
            user.setRole(role);
        } else if (!User.isValidRole(role)) {
            throw new IllegalArgumentException("Unsupported user role: " + role);
        }
    }

    /**
     * Inserts a new user using an existing Connection.
     * Enables transactional atomicity when creating User and Student records together.
     *
     * @param connection active database connection
     * @param user the User to create
     * @return true if successfully inserted
     * @throws IllegalArgumentException on invalid input
     * @throws DuplicateRecordException if the email already exists
     * @throws SQLException on database error (permits caller transaction rollback)
     */
    public boolean createUser(Connection connection, User user) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null.");
        }
        validateUser(user);

        // Pre-check for duplicate email within the active connection
        if (existsByEmail(connection, user.getEmail())) {
            throw new DuplicateRecordException("User with email '" + user.getEmail() + "' already exists.");
        }

        String sql = "INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());

            LocalDateTime created = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
            statement.setTimestamp(5, Timestamp.valueOf(created));

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("User with email '" + user.getEmail() + "' already exists.", e);
            }
            throw e;
        }

        return false;
    }

    /**
     * Registers a new student user, strictly enforcing role = STUDENT.
     * Prevents privilege escalation through public self-registration forms.
     *
     * @param user user account details
     * @return true if successfully registered
     * @throws IllegalArgumentException on invalid input
     * @throws DuplicateRecordException if email already exists
     * @throws DAOException on database failure
     */
    public boolean registerStudentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User entity cannot be null.");
        }
        user.setRole(User.ROLE_STUDENT);
        return createUser(user);
    }

    /**
     * Checks email existence within an existing transaction connection.
     */
    public boolean existsByEmail(Connection connection, String email) throws SQLException {
        String normalizedEmail = User.normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedEmail);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a User entity.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        return user;
    }
}
