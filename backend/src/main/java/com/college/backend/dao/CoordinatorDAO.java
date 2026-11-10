package com.college.backend.dao;

import com.college.backend.entity.Coordinator;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Placement Coordinator profiles and registration requests.
 * Manages atomic coordinator self-registration, unique employee ID verification,
 * and secure administrative approval workflows.
 */
public class CoordinatorDAO {

    private final UserDAO userDAO;

    public CoordinatorDAO() {
        this.userDAO = new UserDAO();
    }

    public CoordinatorDAO(UserDAO userDAO) {
        this.userDAO = userDAO != null ? userDAO : new UserDAO();
    }

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /**
     * Finds a coordinator record by primary key ID.
     */
    public Coordinator findById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT c.id, c.user_id, c.employee_id, c.department, c.designation, c.phone, c.status, " +
                "c.created_at, c.updated_at, c.approved_by, c.approval_date, c.admin_notes, " +
                "u.name AS coordinator_name, u.email AS institutional_email, " +
                "a.name AS approved_by_name " +
                "FROM coordinators c " +
                "JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN users a ON c.approved_by = a.id " +
                "WHERE c.id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCoordinator(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding coordinator by ID " + id + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds a coordinator record by associated User ID.
     */
    public Coordinator findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        String sql = "SELECT c.id, c.user_id, c.employee_id, c.department, c.designation, c.phone, c.status, " +
                "c.created_at, c.updated_at, c.approved_by, c.approval_date, c.admin_notes, " +
                "u.name AS coordinator_name, u.email AS institutional_email, " +
                "a.name AS approved_by_name " +
                "FROM coordinators c " +
                "JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN users a ON c.approved_by = a.id " +
                "WHERE c.user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCoordinator(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding coordinator by User ID " + userId + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds a coordinator record by unique Employee ID.
     */
    public Coordinator findByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return null;
        }
        String normalized = employeeId.trim();

        String sql = "SELECT c.id, c.user_id, c.employee_id, c.department, c.designation, c.phone, c.status, " +
                "c.created_at, c.updated_at, c.approved_by, c.approval_date, c.admin_notes, " +
                "u.name AS coordinator_name, u.email AS institutional_email, " +
                "a.name AS approved_by_name " +
                "FROM coordinators c " +
                "JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN users a ON c.approved_by = a.id " +
                "WHERE c.employee_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalized);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCoordinator(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding coordinator by Employee ID '" + normalized + "': " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Checks if an Employee ID is already registered.
     */
    public boolean existsByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        String normalized = employeeId.trim();

        String sql = "SELECT COUNT(*) FROM coordinators WHERE employee_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalized);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error checking Employee ID '" + normalized + "': " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Checks if an Employee ID is already registered within an existing transaction connection.
     */
    public boolean existsByEmployeeId(Connection connection, String employeeId) throws SQLException {
        if (connection == null || employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        String normalized = employeeId.trim();

        String sql = "SELECT COUNT(*) FROM coordinators WHERE employee_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Creates a coordinator profile using a standalone connection.
     */
    public boolean createCoordinator(Coordinator coordinator) {
        validateCoordinatorFields(coordinator);

        try (Connection connection = getConnection()) {
            return createCoordinator(connection, coordinator);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate entry creating coordinator profile: " + e.getMessage(), e);
            }
            throw new DAOException("Database error creating coordinator profile: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a coordinator profile using an active transaction connection.
     */
    public boolean createCoordinator(Connection connection, Coordinator coordinator) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null.");
        }
        validateCoordinatorFields(coordinator);

        // Invariant check: associated user must exist and have role COORDINATOR
        String checkUserSql = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
            checkStmt.setLong(1, coordinator.getUserId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Associated user with ID " + coordinator.getUserId() + " does not exist.");
                }
                String role = rs.getString("role");
                if (!User.ROLE_COORDINATOR.equalsIgnoreCase(role)) {
                    throw new IllegalArgumentException("Associated user ID " + coordinator.getUserId()
                            + " has role '" + role + "'. Coordinator profiles can only be associated with users having role COORDINATOR.");
                }
            }
        }

        // Check if profile already exists for this user_id
        String checkExistingSql = "SELECT COUNT(*) FROM coordinators WHERE user_id = ?";
        try (PreparedStatement checkExistingStmt = connection.prepareStatement(checkExistingSql)) {
            checkExistingStmt.setLong(1, coordinator.getUserId());
            try (ResultSet rs = checkExistingStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateRecordException("Coordinator profile already exists for User ID " + coordinator.getUserId());
                }
            }
        }

        // Check if employee ID is already used
        if (existsByEmployeeId(connection, coordinator.getEmployeeId())) {
            throw new DuplicateRecordException("A coordinator profile already exists with Employee ID '" + coordinator.getEmployeeId() + "'.");
        }

        String sql = "INSERT INTO coordinators (user_id, employee_id, department, designation, phone, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, coordinator.getUserId());
            statement.setString(2, coordinator.getEmployeeId().trim());
            statement.setString(3, coordinator.getDepartment().trim());
            statement.setString(4, coordinator.getDesignation().trim());
            statement.setString(5, coordinator.getPhone() != null ? coordinator.getPhone().trim() : null);

            String status = coordinator.getStatus() != null ? coordinator.getStatus().trim().toUpperCase() : Coordinator.STATUS_PENDING_APPROVAL;
            statement.setString(6, status);

            LocalDateTime created = coordinator.getCreatedAt() != null ? coordinator.getCreatedAt() : LocalDateTime.now();
            statement.setTimestamp(7, Timestamp.valueOf(created));
            statement.setTimestamp(8, Timestamp.valueOf(created));

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        coordinator.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate entry creating coordinator profile: " + e.getMessage(), e);
            }
            throw e;
        }

        return false;
    }

    /**
     * Registers a new coordinator atomically using a single JDBC transaction.
     * Enforces:
     * - Role strictly set to User.ROLE_COORDINATOR.
     * - Status strictly set to PENDING_APPROVAL on both User and Coordinator.
     * - Rollback on any failure, preventing orphan accounts.
     *
     * @param user        user account
     * @param coordinator coordinator profile
     * @return true if registration request was created
     */
    public boolean registerCoordinator(User user, Coordinator coordinator) {
        if (user == null || coordinator == null) {
            throw new IllegalArgumentException("User and Coordinator entities cannot be null.");
        }

        user.setRole(User.ROLE_COORDINATOR);
        user.setStatus(User.STATUS_PENDING_APPROVAL);
        coordinator.setStatus(Coordinator.STATUS_PENDING_APPROVAL);

        Connection connection = null;
        try {
            connection = getConnection();
            return registerCoordinator(connection, user, coordinator);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate record detected: " + e.getMessage(), e);
            }
            throw new DAOException("Database error during coordinator registration: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Transactional worker for coordinator registration.
     */
    public boolean registerCoordinator(Connection connection, User user, Coordinator coordinator) throws SQLException {
        if (connection == null || user == null || coordinator == null) {
            throw new IllegalArgumentException("Connection, User, and Coordinator entities cannot be null.");
        }

        user.setRole(User.ROLE_COORDINATOR);
        user.setStatus(User.STATUS_PENDING_APPROVAL);
        coordinator.setStatus(Coordinator.STATUS_PENDING_APPROVAL);

        boolean initialAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            // Check for duplicate email
            if (userDAO.existsByEmail(connection, user.getEmail())) {
                throw new DuplicateRecordException("An account with institutional email '" + user.getEmail() + "' already exists.");
            }

            // Check for duplicate employee ID
            if (existsByEmployeeId(connection, coordinator.getEmployeeId())) {
                throw new DuplicateRecordException("A coordinator profile with Employee ID '" + coordinator.getEmployeeId() + "' already exists.");
            }

            // Insert user
            boolean userCreated = userDAO.createUser(connection, user);
            if (!userCreated || user.getId() == null) {
                throw new SQLException("Failed to create user record for coordinator.");
            }

            // Link user ID and insert coordinator
            coordinator.setUserId(user.getId());
            boolean coordCreated = createCoordinator(connection, coordinator);
            if (!coordCreated) {
                throw new SQLException("Failed to create coordinator profile.");
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                // Log and propagate primary exception
            }
            if (e instanceof DuplicateRecordException) {
                throw (DuplicateRecordException) e;
            }
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Transaction failure during coordinator registration: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(initialAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Retrieves all coordinator registration requests ordered by pending status first, then newest first.
     */
    public List<Coordinator> getAllRegistrationRequests() {
        List<Coordinator> list = new ArrayList<>();

        String sql = "SELECT c.id, c.user_id, c.employee_id, c.department, c.designation, c.phone, c.status, " +
                "c.created_at, c.updated_at, c.approved_by, c.approval_date, c.admin_notes, " +
                "u.name AS coordinator_name, u.email AS institutional_email, " +
                "a.name AS approved_by_name " +
                "FROM coordinators c " +
                "JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN users a ON c.approved_by = a.id " +
                "ORDER BY " +
                "CASE c.status " +
                "  WHEN 'PENDING_APPROVAL' THEN 1 " +
                "  WHEN 'APPROVED' THEN 2 " +
                "  WHEN 'REJECTED' THEN 3 " +
                "  ELSE 4 " +
                "END, " +
                "c.created_at DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCoordinator(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Database error retrieving coordinator registration requests: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Atomically updates a coordinator's approval status in both coordinators and users tables.
     * Records admin auditor ID, timestamp, and optional notes.
     *
     * @param coordinatorId       coordinator primary key ID
     * @param newStatus           target status (APPROVED or REJECTED)
     * @param approvedByAdminId   admin User ID performing the action
     * @param adminNotes          audit notes or rejection reason
     * @return true if updated successfully
     */
    public boolean updateStatus(Long coordinatorId, String newStatus, Long approvedByAdminId, String adminNotes) {
        if (coordinatorId == null) {
            throw new IllegalArgumentException("Coordinator ID cannot be null.");
        }
        if (newStatus == null || (!Coordinator.STATUS_APPROVED.equalsIgnoreCase(newStatus)
                && !Coordinator.STATUS_REJECTED.equalsIgnoreCase(newStatus)
                && !Coordinator.STATUS_PENDING_APPROVAL.equalsIgnoreCase(newStatus))) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        String normalizedStatus = newStatus.trim().toUpperCase();

        try (Connection connection = getConnection()) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                // Find associated user_id
                Long userId = null;
                String findUserSql = "SELECT user_id FROM coordinators WHERE id = ?";
                try (PreparedStatement findStmt = connection.prepareStatement(findUserSql)) {
                    findStmt.setLong(1, coordinatorId);
                    try (ResultSet rs = findStmt.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getLong("user_id");
                        }
                    }
                }

                if (userId == null) {
                    connection.rollback();
                    return false;
                }

                // Update coordinators table
                String updateCoordSql = "UPDATE coordinators SET status = ?, approved_by = ?, approval_date = ?, admin_notes = ? WHERE id = ?";
                try (PreparedStatement updateCoordStmt = connection.prepareStatement(updateCoordSql)) {
                    updateCoordStmt.setString(1, normalizedStatus);
                    if (approvedByAdminId != null) {
                        updateCoordStmt.setLong(2, approvedByAdminId);
                    } else {
                        updateCoordStmt.setNull(2, java.sql.Types.BIGINT);
                    }
                    updateCoordStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    updateCoordStmt.setString(4, adminNotes != null ? adminNotes.trim() : null);
                    updateCoordStmt.setLong(5, coordinatorId);

                    int affected = updateCoordStmt.executeUpdate();
                    if (affected <= 0) {
                        connection.rollback();
                        return false;
                    }
                }

                // Synchronize users table status
                String updateUserSql = "UPDATE users SET status = ? WHERE id = ?";
                try (PreparedStatement updateUserStmt = connection.prepareStatement(updateUserSql)) {
                    updateUserStmt.setString(1, normalizedStatus);
                    updateUserStmt.setLong(2, userId);
                    updateUserStmt.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(initialAutoCommit);
            }

        } catch (SQLException e) {
            throw new DAOException("Database error updating coordinator approval status: " + e.getMessage(), e);
        }
    }

    /**
     * Counts pending registration requests.
     */
    public int countPendingRequests() {
        String sql = "SELECT COUNT(*) FROM coordinators WHERE status = 'PENDING_APPROVAL'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Database error counting pending coordinator requests: " + e.getMessage(), e);
        }
        return 0;
    }

    private void validateCoordinatorFields(Coordinator coordinator) {
        if (coordinator == null) {
            throw new IllegalArgumentException("Coordinator profile cannot be null.");
        }
        if (coordinator.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for coordinator profile.");
        }
        if (coordinator.getEmployeeId() == null || coordinator.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required for coordinator profile.");
        }
        if (coordinator.getDepartment() == null || coordinator.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required for coordinator profile.");
        }
        if (coordinator.getDesignation() == null || coordinator.getDesignation().trim().isEmpty()) {
            throw new IllegalArgumentException("Designation is required for coordinator profile.");
        }
    }

    private Coordinator mapResultSetToCoordinator(ResultSet rs) throws SQLException {
        Coordinator coordinator = new Coordinator();
        coordinator.setId(rs.getLong("id"));
        coordinator.setUserId(rs.getLong("user_id"));
        coordinator.setEmployeeId(rs.getString("employee_id"));
        coordinator.setDepartment(rs.getString("department"));
        coordinator.setDesignation(rs.getString("designation"));
        coordinator.setPhone(rs.getString("phone"));
        coordinator.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            coordinator.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            coordinator.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        long approvedBy = rs.getLong("approved_by");
        if (!rs.wasNull()) {
            coordinator.setApprovedBy(approvedBy);
        }

        Timestamp approvalDate = rs.getTimestamp("approval_date");
        if (approvalDate != null) {
            coordinator.setApprovalDate(approvalDate.toLocalDateTime());
        }

        coordinator.setAdminNotes(rs.getString("admin_notes"));

        // Joined fields if available
        try {
            coordinator.setCoordinatorName(rs.getString("coordinator_name"));
        } catch (SQLException ignored) {
        }
        try {
            coordinator.setInstitutionalEmail(rs.getString("institutional_email"));
        } catch (SQLException ignored) {
        }
        try {
            coordinator.setApprovedByName(rs.getString("approved_by_name"));
        } catch (SQLException ignored) {
        }

        return coordinator;
    }
}
