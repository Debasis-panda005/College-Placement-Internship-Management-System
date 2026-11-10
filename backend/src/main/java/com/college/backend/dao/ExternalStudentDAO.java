package com.college.backend.dao;

import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.ExternalStudentAuditLog;
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
 * Data Access Object for External Student profiles and registration applications.
 * Manages atomic self-registration, institutional email verification,
 * and database operations for candidates from external universities.
 */
public class ExternalStudentDAO {

    private final UserDAO userDAO;
    private final ExternalStudentAuditLogDAO auditLogDAO;

    public ExternalStudentDAO() {
        this.userDAO = new UserDAO();
        this.auditLogDAO = new ExternalStudentAuditLogDAO();
    }

    public ExternalStudentDAO(UserDAO userDAO) {
        this.userDAO = userDAO != null ? userDAO : new UserDAO();
        this.auditLogDAO = new ExternalStudentAuditLogDAO();
    }

    public ExternalStudentDAO(UserDAO userDAO, ExternalStudentAuditLogDAO auditLogDAO) {
        this.userDAO = userDAO != null ? userDAO : new UserDAO();
        this.auditLogDAO = auditLogDAO != null ? auditLogDAO : new ExternalStudentAuditLogDAO();
    }

    public ExternalStudentAuditLogDAO getAuditLogDAO() {
        return auditLogDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /**
     * Finds an external student profile by its primary key ID.
     */
    public ExternalStudent findById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT es.id, es.user_id, es.college_name, es.college_location, es.degree, " +
                "es.department, es.current_semester, es.cgpa, es.graduation_year, es.phone, " +
                "es.resume_url, es.verification_status, es.created_at, es.updated_at, " +
                "es.approved_by, es.approval_date, es.admin_notes, " +
                "u.name AS applicant_name, u.email AS applicant_email, " +
                "a.name AS approved_by_name " +
                "FROM external_students es " +
                "JOIN users u ON es.user_id = u.id " +
                "LEFT JOIN users a ON es.approved_by = a.id " +
                "WHERE es.id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExternalStudent(rs);
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                throw new DAOException("Database error: required table 'external_students' is missing or inaccessible (MySQL 1146): " + e.getMessage(), e);
            }
            throw new DAOException("Database error finding external student by ID " + id + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds an external student profile by primary ID, falling back to User ID.
     */
    public ExternalStudent findByIdOrUserId(Long id) {
        if (id == null) {
            return null;
        }
        ExternalStudent es = findById(id);
        if (es != null) {
            return es;
        }
        return findByUserId(id);
    }

    /**
     * Finds an external student profile by the associated User ID.
     */
    public ExternalStudent findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        String sql = "SELECT es.id, es.user_id, es.college_name, es.college_location, es.degree, " +
                "es.department, es.current_semester, es.cgpa, es.graduation_year, es.phone, " +
                "es.resume_url, es.verification_status, es.created_at, es.updated_at, " +
                "es.approved_by, es.approval_date, es.admin_notes, " +
                "u.name AS applicant_name, u.email AS applicant_email, " +
                "a.name AS approved_by_name " +
                "FROM external_students es " +
                "JOIN users u ON es.user_id = u.id " +
                "LEFT JOIN users a ON es.approved_by = a.id " +
                "WHERE es.user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExternalStudent(rs);
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                throw new DAOException("Database error: required table 'external_students' is missing or inaccessible (MySQL 1146): " + e.getMessage(), e);
            }
            throw new DAOException("Database error finding external student by User ID " + userId + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds an external student profile by email.
     */
    public ExternalStudent findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String normalizedEmail = User.normalizeEmail(email);

        String sql = "SELECT es.id, es.user_id, es.college_name, es.college_location, es.degree, " +
                "es.department, es.current_semester, es.cgpa, es.graduation_year, es.phone, " +
                "es.resume_url, es.verification_status, es.created_at, es.updated_at, " +
                "es.approved_by, es.approval_date, es.admin_notes, " +
                "u.name AS applicant_name, u.email AS applicant_email, " +
                "a.name AS approved_by_name " +
                "FROM external_students es " +
                "JOIN users u ON es.user_id = u.id " +
                "LEFT JOIN users a ON es.approved_by = a.id " +
                "WHERE LOWER(TRIM(u.email)) = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedEmail);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExternalStudent(rs);
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                throw new DAOException("Database error: required table 'external_students' is missing or inaccessible (MySQL 1146): " + e.getMessage(), e);
            }
            throw new DAOException("Database error finding external student by email '" + normalizedEmail + "': " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Checks whether an external student profile exists for a given User ID.
     */
    public boolean existsByUserId(Long userId) {
        if (userId == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM external_students WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                throw new DAOException("Database error: required table 'external_students' is missing or inaccessible (MySQL 1146): " + e.getMessage(), e);
            }
            throw new DAOException("Database error checking external student profile for User ID " + userId + ": " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Creates an external student profile using an active transaction connection.
     */
    public boolean createExternalStudent(Connection connection, ExternalStudent student) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null.");
        }
        validateStudentFields(student);

        // Invariant check: associated user must exist and have role STUDENT
        String checkUserSql = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
            checkStmt.setLong(1, student.getUserId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Associated user with ID " + student.getUserId() + " does not exist.");
                }
                String role = rs.getString("role");
                if (!User.ROLE_STUDENT.equalsIgnoreCase(role)) {
                    throw new IllegalArgumentException("Associated user ID " + student.getUserId()
                            + " has role '" + role + "'. External student profiles can only be associated with users having role STUDENT.");
                }
            }
        }

        // Check if profile already exists for this user_id
        String checkExistingSql = "SELECT COUNT(*) FROM external_students WHERE user_id = ?";
        try (PreparedStatement checkExistingStmt = connection.prepareStatement(checkExistingSql)) {
            checkExistingStmt.setLong(1, student.getUserId());
            try (ResultSet rs = checkExistingStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateRecordException("An external student profile already exists for User ID " + student.getUserId());
                }
            }
        }

        String sql = "INSERT INTO external_students (user_id, college_name, college_location, degree, department, " +
                "current_semester, cgpa, graduation_year, phone, resume_url, verification_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, student.getUserId());
            statement.setString(2, student.getCollegeName().trim());
            statement.setString(3, student.getCollegeLocation().trim());
            statement.setString(4, student.getDegree().trim());
            statement.setString(5, student.getDepartment().trim());
            statement.setInt(6, student.getCurrentSemester());
            statement.setBigDecimal(7, student.getCgpa());
            statement.setInt(8, student.getGraduationYear());
            statement.setString(9, student.getPhone() != null ? student.getPhone().trim() : null);
            statement.setString(10, student.getResumeUrl() != null ? student.getResumeUrl().trim() : null);

            String status = student.getVerificationStatus() != null
                    ? student.getVerificationStatus().trim().toUpperCase()
                    : ExternalStudent.STATUS_PENDING;
            statement.setString(11, status);

            LocalDateTime created = student.getCreatedAt() != null ? student.getCreatedAt() : LocalDateTime.now();
            statement.setTimestamp(12, Timestamp.valueOf(created));
            statement.setTimestamp(13, Timestamp.valueOf(created));

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate entry creating external student profile: " + e.getMessage(), e);
            }
            throw e;
        }

        return false;
    }

    /**
     * Registers a new external student atomically using a single JDBC transaction.
     * Enforces:
     * - Role strictly set to User.ROLE_STUDENT.
     * - Status strictly set to User.STATUS_PENDING on User and ExternalStudent.STATUS_PENDING on profile.
     * - Rollback on any failure, preventing orphan accounts.
     *
     * @param user    user account
     * @param student external student profile
     * @return true if registration succeeded
     */
    public boolean registerExternalStudent(User user, ExternalStudent student) {
        if (user == null || student == null) {
            throw new IllegalArgumentException("User and ExternalStudent entities cannot be null.");
        }

        user.setRole(User.ROLE_STUDENT);
        user.setStatus(User.STATUS_PENDING);
        student.setVerificationStatus(ExternalStudent.STATUS_PENDING);

        Connection connection = null;
        try {
            connection = getConnection();
            return registerExternalStudent(connection, user, student);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate record detected: " + e.getMessage(), e);
            }
            throw new DAOException("Database error during external student registration: " + e.getMessage(), e);
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
     * Transactional worker for external student registration.
     */
    public boolean registerExternalStudent(Connection connection, User user, ExternalStudent student) throws SQLException {
        if (connection == null || user == null || student == null) {
            throw new IllegalArgumentException("Connection, User, and ExternalStudent entities cannot be null.");
        }

        // Security enforcement: force role to STUDENT and status to PENDING
        user.setRole(User.ROLE_STUDENT);
        user.setStatus(User.STATUS_PENDING);
        student.setVerificationStatus(ExternalStudent.STATUS_PENDING);

        boolean initialAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            // Check for duplicate email across users
            if (userDAO.existsByEmail(connection, user.getEmail())) {
                throw new DuplicateRecordException("An account with email '" + user.getEmail() + "' already exists.");
            }

            // Insert user
            boolean userCreated = userDAO.createUser(connection, user);
            if (!userCreated || user.getId() == null) {
                throw new SQLException("Failed to create user record for external student.");
            }

            // Link user ID and insert external student record
            student.setUserId(user.getId());
            boolean studentCreated = createExternalStudent(connection, student);
            if (!studentCreated) {
                throw new SQLException("Failed to create external student profile.");
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                // Ignore rollback exception to propagate original
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
            throw new SQLException("Transaction failure during external student registration: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(initialAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Retrieves all external student profiles.
     */
    public List<ExternalStudent> getAllExternalStudents() {
        List<ExternalStudent> list = new ArrayList<>();

        String sql = "SELECT es.id, es.user_id, es.college_name, es.college_location, es.degree, " +
                "es.department, es.current_semester, es.cgpa, es.graduation_year, es.phone, " +
                "es.resume_url, es.verification_status, es.created_at, es.updated_at, " +
                "es.approved_by, es.approval_date, es.admin_notes, " +
                "u.name AS applicant_name, u.email AS applicant_email, " +
                "a.name AS approved_by_name " +
                "FROM external_students es " +
                "JOIN users u ON es.user_id = u.id " +
                "LEFT JOIN users a ON es.approved_by = a.id " +
                "ORDER BY " +
                "CASE es.verification_status " +
                "  WHEN 'PENDING' THEN 1 " +
                "  WHEN 'APPROVED' THEN 2 " +
                "  WHEN 'REJECTED' THEN 3 " +
                "  ELSE 4 " +
                "END, " +
                "es.created_at DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToExternalStudent(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Database error retrieving external student records: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Counts pending registration requests.
     */
    public int countPendingRequests() {
        String sql = "SELECT COUNT(*) FROM external_students WHERE verification_status = 'PENDING'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                throw new DAOException("Database error: required table 'external_students' is missing or inaccessible (MySQL 1146): " + e.getMessage(), e);
            }
            throw new DAOException("Database error counting pending external student requests: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Updates verification status for an external student.
     */
    public boolean updateVerificationStatus(Long externalStudentId, String newStatus, Long approvedByAdminId, String adminNotes) {
        if (externalStudentId == null) {
            throw new IllegalArgumentException("External student ID cannot be null.");
        }
        if (newStatus == null || (!ExternalStudent.STATUS_APPROVED.equalsIgnoreCase(newStatus)
                && !ExternalStudent.STATUS_REJECTED.equalsIgnoreCase(newStatus)
                && !ExternalStudent.STATUS_PENDING.equalsIgnoreCase(newStatus))) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        String normalizedStatus = newStatus.trim().toUpperCase();

        try (Connection connection = getConnection()) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                String previousStatus = null;
                Long userId = null;
                String findUserSql = "SELECT user_id, verification_status FROM external_students WHERE id = ?";
                try (PreparedStatement findStmt = connection.prepareStatement(findUserSql)) {
                    findStmt.setLong(1, externalStudentId);
                    try (ResultSet rs = findStmt.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getLong("user_id");
                            previousStatus = rs.getString("verification_status");
                        }
                    }
                }

                if (userId == null) {
                    connection.rollback();
                    return false;
                }

                String updateExtSql = "UPDATE external_students SET verification_status = ?, approved_by = ?, approval_date = ?, admin_notes = ? WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateExtSql)) {
                    updateStmt.setString(1, normalizedStatus);
                    if (approvedByAdminId != null) {
                        updateStmt.setLong(2, approvedByAdminId);
                    } else {
                        updateStmt.setNull(2, java.sql.Types.BIGINT);
                    }
                    updateStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    updateStmt.setString(4, adminNotes != null ? adminNotes.trim() : null);
                    updateStmt.setLong(5, externalStudentId);

                    int affected = updateStmt.executeUpdate();
                    if (affected <= 0) {
                        connection.rollback();
                        return false;
                    }
                }

                // Update users table status
                String updateUserSql = "UPDATE users SET status = ? WHERE id = ?";
                try (PreparedStatement updateUserStmt = connection.prepareStatement(updateUserSql)) {
                    updateUserStmt.setString(1, normalizedStatus);
                    updateUserStmt.setLong(2, userId);
                    updateUserStmt.executeUpdate();
                }

                // Record in audit log
                if (auditLogDAO != null && approvedByAdminId != null) {
                    String action = ExternalStudent.STATUS_APPROVED.equalsIgnoreCase(normalizedStatus)
                            ? ExternalStudentAuditLog.ACTION_APPROVE
                            : (ExternalStudent.STATUS_REJECTED.equalsIgnoreCase(normalizedStatus)
                            ? ExternalStudentAuditLog.ACTION_REJECT
                            : ExternalStudentAuditLog.ACTION_STATUS_CHANGE);

                    ExternalStudentAuditLog auditEntry = new ExternalStudentAuditLog(
                            externalStudentId, action, approvedByAdminId, previousStatus, normalizedStatus, adminNotes);
                    auditLogDAO.insertAuditLog(connection, auditEntry);
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
            throw new DAOException("Database error updating external student verification status: " + e.getMessage(), e);
        }
    }

    /**
     * Searches and filters external students with full multi-criteria support.
     */
    public List<ExternalStudent> searchAndFilterExternalStudents(String query, String status, String degree, String collegeName) {
        List<ExternalStudent> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT es.id, es.user_id, es.college_name, es.college_location, es.degree, " +
                "es.department, es.current_semester, es.cgpa, es.graduation_year, es.phone, " +
                "es.resume_url, es.verification_status, es.created_at, es.updated_at, " +
                "es.approved_by, es.approval_date, es.admin_notes, " +
                "u.name AS applicant_name, u.email AS applicant_email, " +
                "a.name AS approved_by_name " +
                "FROM external_students es " +
                "JOIN users u ON es.user_id = u.id " +
                "LEFT JOIN users a ON es.approved_by = a.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append(" AND es.verification_status = ?");
            params.add(status.trim().toUpperCase());
        }

        if (degree != null && !degree.trim().isEmpty() && !"ALL".equalsIgnoreCase(degree.trim())) {
            sql.append(" AND LOWER(es.degree) LIKE ?");
            params.add("%" + degree.trim().toLowerCase() + "%");
        }

        if (collegeName != null && !collegeName.trim().isEmpty() && !"ALL".equalsIgnoreCase(collegeName.trim())) {
            sql.append(" AND LOWER(es.college_name) LIKE ?");
            params.add("%" + collegeName.trim().toLowerCase() + "%");
        }

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(es.college_name) LIKE ? OR LOWER(es.department) LIKE ?)");
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append(" ORDER BY ");
        sql.append("CASE es.verification_status ");
        sql.append("  WHEN 'PENDING' THEN 1 ");
        sql.append("  WHEN 'APPROVED' THEN 2 ");
        sql.append("  WHEN 'REJECTED' THEN 3 ");
        sql.append("  ELSE 4 ");
        sql.append("END, es.created_at DESC");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExternalStudent(rs));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error searching external students: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Computes centralized recruitment metrics for External Student Management Dashboard.
     */
    public java.util.Map<String, Integer> getExternalStudentMetrics() {
        java.util.Map<String, Integer> metrics = new java.util.HashMap<>();
        metrics.put("totalStudents", 0);
        metrics.put("pendingVerification", 0);
        metrics.put("approvedStudents", 0);
        metrics.put("rejectedStudents", 0);
        metrics.put("totalApplications", 0);
        metrics.put("shortlistedApplications", 0);
        metrics.put("interviewedApplications", 0);
        metrics.put("selectedApplications", 0);

        // 1. External student status breakdown
        String studentStatusSql = "SELECT verification_status, COUNT(*) AS cnt FROM external_students GROUP BY verification_status";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(studentStatusSql);
             ResultSet rs = statement.executeQuery()) {

            int total = 0;
            while (rs.next()) {
                String status = rs.getString("verification_status");
                int count = rs.getInt("cnt");
                total += count;
                if ("PENDING".equalsIgnoreCase(status)) {
                    metrics.put("pendingVerification", count);
                } else if ("APPROVED".equalsIgnoreCase(status)) {
                    metrics.put("approvedStudents", count);
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    metrics.put("rejectedStudents", count);
                }
            }
            metrics.put("totalStudents", total);
        } catch (SQLException e) {
            // Gracefully handle or log
        }

        // 2. External student application metrics (if applications table supports external applicants)
        String appStatusSql = "SELECT status, COUNT(*) AS cnt FROM applications " +
                "WHERE (applicant_type = 'EXTERNAL' OR external_student_id IS NOT NULL) " +
                "GROUP BY status";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(appStatusSql);
             ResultSet rs = statement.executeQuery()) {

            int totalApps = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("cnt");
                totalApps += count;
                if ("SHORTLISTED".equalsIgnoreCase(status)) {
                    metrics.put("shortlistedApplications", metrics.get("shortlistedApplications") + count);
                } else if ("SELECTED".equalsIgnoreCase(status)) {
                    metrics.put("selectedApplications", count);
                }
            }
            metrics.put("totalApplications", totalApps);
        } catch (SQLException ignored) {
            // Column may not yet be present prior to manual migration
        }

        // 3. Interviewed external candidates count
        String interviewedSql = "SELECT COUNT(DISTINCT a.id) FROM applications a " +
                "JOIN interviews i ON a.id = i.application_id " +
                "WHERE (a.applicant_type = 'EXTERNAL' OR a.external_student_id IS NOT NULL)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(interviewedSql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                metrics.put("interviewedApplications", rs.getInt(1));
            }
        } catch (SQLException ignored) {
        }

        return metrics;
    }

    private void validateStudentFields(ExternalStudent student) {
        if (student == null) {
            throw new IllegalArgumentException("External student profile cannot be null.");
        }
        if (student.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for external student profile.");
        }
        if (student.getCollegeName() == null || student.getCollegeName().trim().isEmpty()) {
            throw new IllegalArgumentException("College name is required.");
        }
        if (student.getCollegeLocation() == null || student.getCollegeLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("College location is required.");
        }
        if (student.getDegree() == null || student.getDegree().trim().isEmpty()) {
            throw new IllegalArgumentException("Degree is required.");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (student.getCurrentSemester() == null || !ExternalStudent.isValidSemester(student.getCurrentSemester())) {
            throw new IllegalArgumentException("Current semester must be between 1 and 12.");
        }
        if (student.getCgpa() == null || !ExternalStudent.isValidCgpa(student.getCgpa())) {
            throw new IllegalArgumentException("CGPA must be between 0.00 and 10.00.");
        }
        if (student.getGraduationYear() == null || !ExternalStudent.isValidGraduationYear(student.getGraduationYear())) {
            throw new IllegalArgumentException("Graduation year must be between 2000 and 2100.");
        }
        if (student.getPhone() == null || student.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile phone number is required.");
        }
    }

    private ExternalStudent mapResultSetToExternalStudent(ResultSet rs) throws SQLException {
        ExternalStudent student = new ExternalStudent();
        student.setId(rs.getLong("id"));
        student.setUserId(rs.getLong("user_id"));
        student.setCollegeName(rs.getString("college_name"));
        student.setCollegeLocation(rs.getString("college_location"));
        student.setDegree(rs.getString("degree"));
        student.setDepartment(rs.getString("department"));
        student.setCurrentSemester(rs.getInt("current_semester"));
        student.setCgpa(rs.getBigDecimal("cgpa"));
        student.setGraduationYear(rs.getInt("graduation_year"));
        student.setPhone(rs.getString("phone"));
        student.setResumeUrl(rs.getString("resume_url"));
        student.setVerificationStatus(rs.getString("verification_status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            student.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            student.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        long approvedBy = rs.getLong("approved_by");
        if (!rs.wasNull()) {
            student.setApprovedBy(approvedBy);
        }

        Timestamp approvalDate = rs.getTimestamp("approval_date");
        if (approvalDate != null) {
            student.setApprovalDate(approvalDate.toLocalDateTime());
        }

        student.setAdminNotes(rs.getString("admin_notes"));

        try {
            student.setApplicantName(rs.getString("applicant_name"));
        } catch (SQLException ignored) {
        }
        try {
            student.setApplicantEmail(rs.getString("applicant_email"));
        } catch (SQLException ignored) {
        }
        try {
            student.setApprovedByName(rs.getString("approved_by_name"));
        } catch (SQLException ignored) {
        }

        return student;
    }

    /**
     * Updates editable profile fields (phone and currentSemester) for an external student.
     * Institutional fields like university, degree, CGPA, graduation year, and resume are immutable.
     */
    public boolean updatePermittedProfile(Long id, String phone, int currentSemester) {
        if (id == null) {
            return false;
        }
        String sql = "UPDATE external_students SET phone = ?, current_semester = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone != null ? phone.trim() : null);
            stmt.setInt(2, currentSemester);
            stmt.setLong(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating external student profile: " + e.getMessage(), e);
        }
    }

    /**
     * Alias returning all external students, maintaining naming convention compatibility.
     */
    public List<ExternalStudent> findAll() {
        return getAllExternalStudents();
    }

    /**
     * Alias method for searching and filtering external students.
     */
    public List<ExternalStudent> searchExternalStudents(String query, String status, String degree, String collegeName) {
        return searchAndFilterExternalStudents(query, status, degree, collegeName);
    }

    /**
     * Retrieves aggregated dashboard metrics keyed with uppercase category codes.
     */
    public java.util.Map<String, Integer> getDashboardMetrics() {
        java.util.Map<String, Integer> raw = getExternalStudentMetrics();
        java.util.Map<String, Integer> metrics = new java.util.HashMap<>();
        metrics.put("TOTAL", raw.getOrDefault("totalStudents", 0));
        metrics.put("PENDING", raw.getOrDefault("pendingVerification", 0));
        metrics.put("APPROVED", raw.getOrDefault("approvedStudents", 0));
        metrics.put("REJECTED", raw.getOrDefault("rejectedStudents", 0));
        metrics.put("APPLICATIONS", raw.getOrDefault("totalApplications", 0));
        metrics.put("SHORTLISTED", raw.getOrDefault("shortlistedApplications", 0));
        metrics.put("INTERVIEWED", raw.getOrDefault("interviewedApplications", 0));
        metrics.put("SELECTED", raw.getOrDefault("selectedApplications", 0));
        return metrics;
    }
}

