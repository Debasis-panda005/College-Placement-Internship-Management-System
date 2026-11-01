package com.college.backend.dao;

import com.college.backend.entity.InterviewReport;
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

public class InterviewReportDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public boolean createOrUpdateReport(InterviewReport report) {
        if (report == null || report.getApplicationId() == null || report.getReportUuid() == null) {
            return false;
        }

        String sql = "INSERT INTO interview_reports " +
                "(application_id, report_uuid, final_decision, decision_date, total_rounds, " +
                "completed_rounds, passed_rounds, failed_rounds, cancelled_rounds, " +
                "coordinator_remarks, overall_feedback, generated_by, generated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "final_decision = VALUES(final_decision), " +
                "decision_date = VALUES(decision_date), " +
                "total_rounds = VALUES(total_rounds), " +
                "completed_rounds = VALUES(completed_rounds), " +
                "passed_rounds = VALUES(passed_rounds), " +
                "failed_rounds = VALUES(failed_rounds), " +
                "cancelled_rounds = VALUES(cancelled_rounds), " +
                "coordinator_remarks = VALUES(coordinator_remarks), " +
                "overall_feedback = VALUES(overall_feedback), " +
                "generated_by = VALUES(generated_by), " +
                "generated_at = VALUES(generated_at)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, report.getApplicationId());
            statement.setString(2, report.getReportUuid());
            statement.setString(3, report.getFinalDecision());
            statement.setTimestamp(4, report.getDecisionDate() != null ?
                    Timestamp.valueOf(report.getDecisionDate()) : Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(5, report.getTotalRounds() != null ? report.getTotalRounds() : 0);
            statement.setInt(6, report.getCompletedRounds() != null ? report.getCompletedRounds() : 0);
            statement.setInt(7, report.getPassedRounds() != null ? report.getPassedRounds() : 0);
            statement.setInt(8, report.getFailedRounds() != null ? report.getFailedRounds() : 0);
            statement.setInt(9, report.getCancelledRounds() != null ? report.getCancelledRounds() : 0);
            statement.setString(10, report.getCoordinatorRemarks());
            statement.setString(11, report.getOverallFeedback());
            statement.setString(12, report.getGeneratedBy());
            statement.setTimestamp(13, report.getGeneratedAt() != null ?
                    Timestamp.valueOf(report.getGeneratedAt()) : Timestamp.valueOf(LocalDateTime.now()));

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        report.setId(rs.getLong(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public InterviewReport getReportByApplicationId(Long applicationId) {
        if (applicationId == null) {
            return null;
        }

        String sql = "SELECT * FROM interview_reports WHERE application_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public InterviewReport getReportByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM interview_reports WHERE report_uuid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, uuid.trim());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<InterviewReport> getAllReports() {
        List<InterviewReport> list = new ArrayList<>();
        String sql = "SELECT * FROM interview_reports ORDER BY decision_date DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private InterviewReport mapResultSetToReport(ResultSet rs) throws SQLException {
        InterviewReport rep = new InterviewReport();
        rep.setId(rs.getLong("id"));
        rep.setApplicationId(rs.getLong("application_id"));
        rep.setReportUuid(rs.getString("report_uuid"));
        rep.setFinalDecision(rs.getString("final_decision"));

        Timestamp decDate = rs.getTimestamp("decision_date");
        if (decDate != null) {
            rep.setDecisionDate(decDate.toLocalDateTime());
        }

        rep.setTotalRounds(rs.getInt("total_rounds"));
        rep.setCompletedRounds(rs.getInt("completed_rounds"));
        rep.setPassedRounds(rs.getInt("passed_rounds"));
        rep.setFailedRounds(rs.getInt("failed_rounds"));
        rep.setCancelledRounds(rs.getInt("cancelled_rounds"));
        rep.setCoordinatorRemarks(rs.getString("coordinator_remarks"));
        rep.setOverallFeedback(rs.getString("overall_feedback"));
        rep.setGeneratedBy(rs.getString("generated_by"));

        Timestamp genDate = rs.getTimestamp("generated_at");
        if (genDate != null) {
            rep.setGeneratedAt(genDate.toLocalDateTime());
        }

        return rep;
    }
}
