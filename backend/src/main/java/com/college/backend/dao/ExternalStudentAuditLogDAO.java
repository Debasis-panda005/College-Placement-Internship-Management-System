package com.college.backend.dao;

import com.college.backend.entity.ExternalStudentAuditLog;
import com.college.backend.exception.DAOException;
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
 * Data Access Object for audit history of external student approvals, rejections, and status changes.
 */
public class ExternalStudentAuditLogDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /**
     * Records an audit log entry within an active database transaction.
     */
    public boolean insertAuditLog(Connection connection, ExternalStudentAuditLog auditLog) throws SQLException {
        if (connection == null || auditLog == null) {
            return false;
        }

        String sql = "INSERT INTO external_student_audit_logs (external_student_id, action, performed_by, " +
                "action_timestamp, previous_status, new_status, remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, auditLog.getExternalStudentId());
            statement.setString(2, auditLog.getAction());
            statement.setLong(3, auditLog.getPerformedBy());

            LocalDateTime ts = auditLog.getActionTimestamp() != null ? auditLog.getActionTimestamp() : LocalDateTime.now();
            statement.setTimestamp(4, Timestamp.valueOf(ts));
            statement.setString(5, auditLog.getPreviousStatus());
            statement.setString(6, auditLog.getNewStatus());
            statement.setString(7, auditLog.getRemarks());

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        auditLog.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            // Error code 1146 means Table doesn't exist (prior to manual migration)
            if (e.getErrorCode() == 1146) {
                return true; // Gracefully continue if audit table hasn't been migrated yet
            }
            throw e;
        }

        return false;
    }

    /**
     * Standalone insertion of audit log.
     */
    public boolean insertAuditLog(ExternalStudentAuditLog auditLog) {
        if (auditLog == null) {
            return false;
        }

        try (Connection connection = getConnection()) {
            return insertAuditLog(connection, auditLog);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                return true;
            }
            throw new DAOException("Database error recording external student audit log: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves audit trail for a specific external student.
     */
    public List<ExternalStudentAuditLog> getAuditLogsForStudent(Long externalStudentId) {
        List<ExternalStudentAuditLog> logs = new ArrayList<>();
        if (externalStudentId == null) {
            return logs;
        }

        String sql = "SELECT al.id, al.external_student_id, al.action, al.performed_by, al.action_timestamp, " +
                "al.previous_status, al.new_status, al.remarks, u.name AS performed_by_name " +
                "FROM external_student_audit_logs al " +
                "LEFT JOIN users u ON al.performed_by = u.id " +
                "WHERE al.external_student_id = ? " +
                "ORDER BY al.action_timestamp DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, externalStudentId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ExternalStudentAuditLog log = new ExternalStudentAuditLog();
                    log.setId(rs.getLong("id"));
                    log.setExternalStudentId(rs.getLong("external_student_id"));
                    log.setAction(rs.getString("action"));
                    log.setPerformedBy(rs.getLong("performed_by"));

                    Timestamp ts = rs.getTimestamp("action_timestamp");
                    if (ts != null) {
                        log.setActionTimestamp(ts.toLocalDateTime());
                    }

                    log.setPreviousStatus(rs.getString("previous_status"));
                    log.setNewStatus(rs.getString("new_status"));
                    log.setRemarks(rs.getString("remarks"));
                    log.setPerformedByName(rs.getString("performed_by_name"));

                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                return logs; // Table not yet created
            }
            throw new DAOException("Database error retrieving audit logs: " + e.getMessage(), e);
        }

        return logs;
    }
}
