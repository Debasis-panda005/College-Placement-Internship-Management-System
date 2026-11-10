package com.college.backend.dao;

import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDAO {

    private final ApplicationStatusHistoryDAO statusHistoryDAO = new ApplicationStatusHistoryDAO();

    /**
     * Obtains a database connection. Protected to allow test overrides.
     */
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // =====================================================
    // 1. CREATE APPLICATION
    // =====================================================

    public boolean createApplication(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null.");
        }

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            Long generatedId = null;

            // Try insert with applicant_type and external_student_id
            String sqlWithExternal = "INSERT INTO applications " +
                    "(student_id, job_id, application_date, status, applicant_type, external_student_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            boolean insertSuccess = false;
            try (PreparedStatement statement =
                         connection.prepareStatement(sqlWithExternal, Statement.RETURN_GENERATED_KEYS)) {

                Long studentId = application.getStudentId() != null
                        ? application.getStudentId()
                        : application.getExternalStudentId();
                statement.setLong(1, studentId != null ? studentId : 0L);
                statement.setLong(2, application.getJobId());
                statement.setDate(3, Date.valueOf(application.getApplicationDate()));
                statement.setString(4, application.getStatus());
                statement.setString(5, application.getApplicantType());
                if (application.getExternalStudentId() != null) {
                    statement.setLong(6, application.getExternalStudentId());
                } else {
                    statement.setNull(6, java.sql.Types.BIGINT);
                }

                int affected = statement.executeUpdate();
                if (affected > 0) {
                    insertSuccess = true;
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedId = generatedKeys.getLong(1);
                            application.setId(generatedId);
                        }
                    }
                }
            } catch (SQLException e) {
                // If applicant_type or external_student_id column doesn't exist, fallback to classic insert
                if (e.getErrorCode() == 1054 || (e.getMessage() != null &&
                        (e.getMessage().toLowerCase().contains("applicant_type") || e.getMessage().toLowerCase().contains("external_student_id")))) {
                    String fallbackSql = "INSERT INTO applications (student_id, job_id, application_date, status) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement fbStmt = connection.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
                        Long studentId = application.getStudentId() != null
                                ? application.getStudentId()
                                : application.getExternalStudentId();
                        fbStmt.setLong(1, studentId != null ? studentId : 0L);
                        fbStmt.setLong(2, application.getJobId());
                        fbStmt.setDate(3, Date.valueOf(application.getApplicationDate()));
                        fbStmt.setString(4, application.getStatus());

                        int affected = fbStmt.executeUpdate();
                        if (affected > 0) {
                            insertSuccess = true;
                            try (ResultSet generatedKeys = fbStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    generatedId = generatedKeys.getLong(1);
                                    application.setId(generatedId);
                                }
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }

            if (!insertSuccess || generatedId == null) {
                connection.rollback();
                return false;
            }

            String status = application.getStatus() != null ? application.getStatus() : "APPLIED";
            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(generatedId, status, LocalDateTime.now())
            );

            if (!historyAdded) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Application already exists for candidate and job ID " + application.getJobId() + ".", e);
            }
            throw new DAOException("Database error occurred while creating application.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    // =====================================================
    // 2. GET ALL APPLICATIONS
    // =====================================================

    public List<Application> getAllApplications() {

        List<Application> applications = new ArrayList<>();

        String sql = "SELECT * FROM applications ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applications;
    }


    // =====================================================
    // 3. GET APPLICATION BY ID
    // =====================================================

    public Application getApplicationById(Long id) {
        if (id == null) {
            return null;
        }

        String sql =
                "SELECT * FROM applications WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapResultSetToApplication(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // 4. UPDATE APPLICATION
    // =====================================================

    public boolean updateApplication(Application application) {
        if (application == null || application.getId() == null) {
            return false;
        }

        String sql =
                "UPDATE applications SET " +
                        "student_id = ?, " +
                        "job_id = ?, " +
                        "application_date = ?, " +
                        "status = ?, " +
                        "applicant_type = ?, " +
                        "external_student_id = ? " +
                        "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            Long sId = application.getStudentId() != null ? application.getStudentId() : application.getExternalStudentId();
            if (sId != null) {
                statement.setLong(1, sId);
            } else {
                statement.setNull(1, java.sql.Types.BIGINT);
            }
            statement.setLong(2, application.getJobId());
            statement.setDate(3, Date.valueOf(application.getApplicationDate()));
            statement.setString(4, application.getStatus());
            statement.setString(5, application.getApplicantType());
            if (application.getExternalStudentId() != null) {
                statement.setLong(6, application.getExternalStudentId());
            } else {
                statement.setNull(6, java.sql.Types.BIGINT);
            }
            statement.setLong(7, application.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            // Fallback for pre-migration schema
            String fallbackSql =
                    "UPDATE applications SET " +
                            "student_id = ?, " +
                            "job_id = ?, " +
                            "application_date = ?, " +
                            "status = ? " +
                            "WHERE id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(fallbackSql)) {
                statement.setLong(1, application.getStudentId() != null ? application.getStudentId() : 0L);
                statement.setLong(2, application.getJobId());
                statement.setDate(3, Date.valueOf(application.getApplicationDate()));
                statement.setString(4, application.getStatus());
                statement.setLong(5, application.getId());
                return statement.executeUpdate() > 0;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }


    // =====================================================
    // 5. DELETE APPLICATION
    // =====================================================

    public boolean deleteApplication(Long id) {

        String sql =
                "DELETE FROM applications WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 6. SHORTLIST APPLICATION
    // =====================================================

    public boolean shortlistApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "SHORTLISTED");
                statement.setLong(2, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(id, "SHORTLISTED", LocalDateTime.now())
            );

            if (!historyAdded) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    // =====================================================
    // 7. SELECT APPLICATION
    // =====================================================

    public boolean selectApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "SELECTED");
                statement.setLong(2, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(id, "SELECTED", LocalDateTime.now())
            );

            if (!historyAdded) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    // =====================================================
    // 8. REJECT APPLICATION
    // =====================================================

    public boolean rejectApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "REJECTED");
                statement.setLong(2, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(id, "REJECTED", LocalDateTime.now())
            );

            if (!historyAdded) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    // =====================================================
    // 9. GET SHORTLISTED APPLICATIONS
    // =====================================================

    public List<Application> getShortlistedApplications() {

        List<Application> applications = new ArrayList<>();

        String sql = "SELECT * FROM applications WHERE status = 'SHORTLISTED' ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applications;
    }


    // =====================================================
    // 10. CHECK DUPLICATE APPLICATION
    // =====================================================

    public boolean hasAlreadyApplied(Long studentId, Long jobId) {
        if (studentId == null || jobId == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM applications WHERE (student_id = ? OR external_student_id = ?) AND job_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, studentId);
            statement.setLong(2, studentId);
            statement.setLong(3, jobId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            // Fallback for pre-migration schema
            String fallbackSql = "SELECT COUNT(*) FROM applications WHERE student_id = ? AND job_id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(fallbackSql)) {
                statement.setLong(1, studentId);
                statement.setLong(2, jobId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            } catch (SQLException ex) {
                throw new DAOException("Database error checking existing application: " + ex.getMessage(), ex);
            }
        }

        return false;
    }


    // =====================================================
    // 11. SEARCH AND FILTER APPLICATIONS
    // =====================================================

    public List<Application> searchApplications(Long studentId, Long jobId, String status) {

        List<Application> applications = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM applications WHERE 1=1");

        if (studentId != null) {
            sql.append(" AND (student_id = ? OR external_student_id = ?)");
        }

        if (jobId != null) {
            sql.append(" AND job_id = ?");
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append(" AND status = ?");
        }

        sql.append(" ORDER BY id");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;

            if (studentId != null) {
                statement.setLong(index++, studentId);
                statement.setLong(index++, studentId);
            }

            if (jobId != null) {
                statement.setLong(index++, jobId);
            }

            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                statement.setString(index++, status.trim());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    applications.add(mapResultSetToApplication(resultSet));
                }
            }

        } catch (SQLException e) {
            if (studentId != null && (e.getErrorCode() == 1054 || (e.getMessage() != null && e.getMessage().contains("external_student_id")))) {
                return searchApplicationsLegacy(studentId, jobId, status);
            }
            e.printStackTrace();
        }

        return applications;
    }

    private List<Application> searchApplicationsLegacy(Long studentId, Long jobId, String status) {
        List<Application> applications = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM applications WHERE 1=1");
        if (studentId != null) {
            sql.append(" AND student_id = ?");
        }
        if (jobId != null) {
            sql.append(" AND job_id = ?");
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append(" AND status = ?");
        }
        sql.append(" ORDER BY id");
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (studentId != null) {
                statement.setLong(index++, studentId);
            }
            if (jobId != null) {
                statement.setLong(index++, jobId);
            }
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                statement.setString(index++, status.trim());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    applications.add(mapResultSetToApplication(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }


    // =====================================================
    // 12. GET APPLICATION STATISTICS
    // =====================================================

    public Map<String, Integer> getApplicationStatistics() {

        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL", 0);
        stats.put("APPLIED", 0);
        stats.put("SHORTLISTED", 0);
        stats.put("SELECTED", 0);
        stats.put("REJECTED", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM applications GROUP BY status";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            int total = 0;

            while (resultSet.next()) {
                String status = resultSet.getString("status");
                int count = resultSet.getInt("cnt");

                if (status != null) {
                    stats.put(status.toUpperCase(), count);
                }

                total += count;
            }

            stats.put("TOTAL", total);

        } catch (SQLException e) {
            throw new DAOException("Database error retrieving application statistics: " + e.getMessage(), e);
        }

        return stats;
    }

    // =====================================================
    // 13. EXTERNAL STUDENT APPLICATION HELPERS
    // =====================================================

    public List<Application> getApplicationsByExternalStudent(Long externalStudentId) {
        if (externalStudentId == null) {
            return new ArrayList<>();
        }
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE external_student_id = ? OR (student_id = ? AND applicant_type = 'EXTERNAL') ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, externalStudentId);
            stmt.setLong(2, externalStudentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        } catch (SQLException e) {
            // Fallback for pre-migration schema
            String fallbackSql = "SELECT * FROM applications WHERE student_id = ? ORDER BY id DESC";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(fallbackSql)) {
                stmt.setLong(1, externalStudentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        applications.add(mapResultSetToApplication(rs));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return applications;
    }

    public boolean hasAlreadyAppliedExternal(Long externalStudentId, Long jobId) {
        if (externalStudentId == null || jobId == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM applications WHERE (external_student_id = ? OR (student_id = ? AND applicant_type = 'EXTERNAL')) AND job_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, externalStudentId);
            stmt.setLong(2, externalStudentId);
            stmt.setLong(3, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            // Fallback for pre-migration schema
            String fallbackSql = "SELECT COUNT(*) FROM applications WHERE student_id = ? AND job_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(fallbackSql)) {
                stmt.setLong(1, externalStudentId);
                stmt.setLong(2, jobId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getLong("id"));

        long studentId = rs.getLong("student_id");
        if (!rs.wasNull()) {
            app.setStudentId(studentId);
        }

        app.setJobId(rs.getLong("job_id"));
        Date date = rs.getDate("application_date");
        if (date != null) {
            app.setApplicationDate(date.toLocalDate());
        }
        app.setStatus(rs.getString("status"));

        String appType = null;
        try {
            appType = rs.getString("applicant_type");
        } catch (SQLException ignored) {
        }

        Long extId = null;
        try {
            long val = rs.getLong("external_student_id");
            if (!rs.wasNull()) {
                extId = val;
                app.setExternalStudentId(extId);
            }
        } catch (SQLException ignored) {
        }

        if (extId != null) {
            app.setApplicantType(Application.APPLICANT_EXTERNAL);
            if (app.getStudentId() == null) {
                app.setStudentId(extId);
            }
        } else if (appType != null && !appType.trim().isEmpty()) {
            app.setApplicantType(appType.trim().toUpperCase());
            if (Application.APPLICANT_EXTERNAL.equalsIgnoreCase(appType) && app.getStudentId() != null) {
                app.setExternalStudentId(app.getStudentId());
            }
        } else {
            app.setApplicantType(Application.APPLICANT_INTERNAL);
        }

        return app;
    }
}
