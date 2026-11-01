package com.college.backend.dao;

import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.InterviewRoundHistory;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterviewRoundDAO {

    private final InterviewRoundHistoryDAO historyDAO = new InterviewRoundHistoryDAO();

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // =====================================================
    // 1. CREATE ROUND
    // =====================================================
    public boolean createRound(InterviewRound round, String performedBy) {
        if (round == null || round.getApplicationId() == null) {
            return false;
        }

        String sql = "INSERT INTO interview_rounds " +
                "(application_id, round_number, round_name, interview_date, interview_time, mode, " +
                "interviewer_name, interviewer_info, status, result, remarks, feedback) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            Long generatedId = null;

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, round.getApplicationId());
                statement.setInt(2, round.getRoundNumber());
                statement.setString(3, round.getRoundName());
                statement.setDate(4, Date.valueOf(round.getInterviewDate()));
                statement.setTime(5, Time.valueOf(round.getInterviewTime()));
                statement.setString(6, round.getMode() != null ? round.getMode() : InterviewRound.MODE_ONLINE);
                statement.setString(7, round.getInterviewerName());
                statement.setString(8, round.getInterviewerInfo());
                statement.setString(9, round.getStatus() != null ? round.getStatus() : InterviewRound.STATUS_SCHEDULED);
                statement.setString(10, round.getResult() != null ? round.getResult() : InterviewRound.RESULT_PENDING);
                statement.setString(11, round.getRemarks());
                statement.setString(12, round.getFeedback());

                int affected = statement.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getLong(1);
                        round.setId(generatedId);
                    }
                }
            }

            if (generatedId == null) {
                generatedId = round.getId();
            }

            if (generatedId != null) {
                InterviewRoundHistory history = new InterviewRoundHistory(
                        generatedId,
                        round.getApplicationId(),
                        "CREATED",
                        null,
                        InterviewRound.STATUS_SCHEDULED,
                        null,
                        InterviewRound.RESULT_PENDING,
                        "Round " + round.getRoundNumber() + " (" + round.getRoundName() + ") scheduled",
                        performedBy != null ? performedBy : "COORDINATOR"
                );
                historyDAO.addHistory(connection, history);
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
    // 2. GET ROUNDS BY APPLICATION ID
    // =====================================================
    public List<InterviewRound> getRoundsByApplicationId(Long applicationId) {
        List<InterviewRound> rounds = new ArrayList<>();
        if (applicationId == null) {
            return rounds;
        }

        String sql = "SELECT * FROM interview_rounds WHERE application_id = ? ORDER BY round_number ASC, id ASC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rounds.add(mapResultSetToRound(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rounds;
    }

    // =====================================================
    // 3. GET ROUND BY ID
    // =====================================================
    public InterviewRound getRoundById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT * FROM interview_rounds WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRound(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =====================================================
    // 4. RESCHEDULE ROUND
    // =====================================================
    public boolean rescheduleRound(Long roundId, LocalDate newDate, LocalTime newTime,
                                   String newMode, String interviewerName, String interviewerInfo,
                                   String remarks, String performedBy) {
        if (roundId == null || newDate == null || newTime == null) {
            return false;
        }

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            InterviewRound existing = null;
            String selectSql = "SELECT * FROM interview_rounds WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
                stmt.setLong(1, roundId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        existing = mapResultSetToRound(rs);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            if (!InterviewRound.STATUS_SCHEDULED.equals(existing.getStatus()) &&
                    !InterviewRound.STATUS_RESCHEDULED.equals(existing.getStatus())) {
                connection.rollback();
                return false;
            }

            String updateSql = "UPDATE interview_rounds SET " +
                    "interview_date = ?, interview_time = ?, mode = ?, " +
                    "interviewer_name = ?, interviewer_info = ?, status = ?, remarks = ? " +
                    "WHERE id = ?";

            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setDate(1, Date.valueOf(newDate));
                updateStmt.setTime(2, Time.valueOf(newTime));
                updateStmt.setString(3, newMode != null ? newMode : existing.getMode());
                updateStmt.setString(4, interviewerName != null ? interviewerName : existing.getInterviewerName());
                updateStmt.setString(5, interviewerInfo != null ? interviewerInfo : existing.getInterviewerInfo());
                updateStmt.setString(6, InterviewRound.STATUS_RESCHEDULED);
                updateStmt.setString(7, remarks);
                updateStmt.setLong(8, roundId);

                int affected = updateStmt.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            String formattedOldDate = existing.getInterviewDate() != null ? existing.getInterviewDate().toString() : "TBD";
            String formattedOldTime = existing.getInterviewTime() != null ? existing.getInterviewTime().toString() : "TBD";
            String formattedNewDate = newDate != null ? newDate.toString() : "TBD";
            String formattedNewTime = newTime != null ? newTime.toString() : "TBD";

            String rescheduleDetail = "Original schedule: " + formattedOldDate + ", " + formattedOldTime +
                    ". Rescheduled to: " + formattedNewDate + ", " + formattedNewTime + "." +
                    (remarks != null && !remarks.trim().isEmpty() ? " Reason: " + remarks.trim() : "");

            InterviewRoundHistory history = new InterviewRoundHistory(
                    roundId,
                    existing.getApplicationId(),
                    "RESCHEDULED",
                    existing.getStatus(),
                    InterviewRound.STATUS_RESCHEDULED,
                    existing.getResult(),
                    existing.getResult(),
                    rescheduleDetail,
                    performedBy != null ? performedBy : "COORDINATOR"
            );
            historyDAO.addHistory(connection, history);

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
    // 5. COMPLETE ROUND AND RECORD RESULT
    // =====================================================
    public boolean completeRound(Long roundId, String result, String remarks,
                                 String feedback, String performedBy) {
        if (roundId == null || result == null) {
            return false;
        }

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            InterviewRound existing = null;
            String selectSql = "SELECT * FROM interview_rounds WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
                stmt.setLong(1, roundId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        existing = mapResultSetToRound(rs);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            if (!InterviewRound.STATUS_SCHEDULED.equals(existing.getStatus()) &&
                    !InterviewRound.STATUS_RESCHEDULED.equals(existing.getStatus())) {
                connection.rollback();
                return false;
            }

            String updateSql = "UPDATE interview_rounds SET " +
                    "status = ?, result = ?, remarks = ?, feedback = ? " +
                    "WHERE id = ?";

            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setString(1, InterviewRound.STATUS_COMPLETED);
                updateStmt.setString(2, result.trim().toUpperCase());
                updateStmt.setString(3, remarks);
                updateStmt.setString(4, feedback);
                updateStmt.setLong(5, roundId);

                int affected = updateStmt.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            InterviewRoundHistory history = new InterviewRoundHistory(
                    roundId,
                    existing.getApplicationId(),
                    "COMPLETED",
                    existing.getStatus(),
                    InterviewRound.STATUS_COMPLETED,
                    existing.getResult(),
                    result.trim().toUpperCase(),
                    "Round completed with result: " + result.trim().toUpperCase() + ". Feedback: " + (feedback != null ? feedback : "N/A"),
                    performedBy != null ? performedBy : "COORDINATOR"
            );
            historyDAO.addHistory(connection, history);

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
    // 6. CANCEL ROUND
    // =====================================================
    public boolean cancelRound(Long roundId, String reason, String performedBy) {
        if (roundId == null) {
            return false;
        }

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            InterviewRound existing = null;
            String selectSql = "SELECT * FROM interview_rounds WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
                stmt.setLong(1, roundId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        existing = mapResultSetToRound(rs);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            if (!InterviewRound.STATUS_SCHEDULED.equals(existing.getStatus()) &&
                    !InterviewRound.STATUS_RESCHEDULED.equals(existing.getStatus())) {
                connection.rollback();
                return false;
            }

            String updateSql = "UPDATE interview_rounds SET " +
                    "status = ?, cancellation_reason = ? " +
                    "WHERE id = ?";

            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setString(1, InterviewRound.STATUS_CANCELLED);
                updateStmt.setString(2, reason);
                updateStmt.setLong(3, roundId);

                int affected = updateStmt.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            InterviewRoundHistory history = new InterviewRoundHistory(
                    roundId,
                    existing.getApplicationId(),
                    "CANCELLED",
                    existing.getStatus(),
                    InterviewRound.STATUS_CANCELLED,
                    existing.getResult(),
                    existing.getResult(),
                    reason != null ? reason : "Round cancelled",
                    performedBy != null ? performedBy : "COORDINATOR"
            );
            historyDAO.addHistory(connection, history);

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
    // 7. GET NEXT ROUND NUMBER FOR APPLICATION
    // =====================================================
    public int getNextRoundNumber(Long applicationId) {
        if (applicationId == null) {
            return 1;
        }

        String sql = "SELECT COALESCE(MAX(round_number), 0) + 1 FROM interview_rounds WHERE application_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1;
    }

    // =====================================================
    // 8. VERIFY PREVIOUS ROUNDS ARE PASSED
    // =====================================================
    public boolean areAllPreviousRoundsPassed(Long applicationId, int roundNumber) {
        if (applicationId == null || roundNumber <= 1) {
            return true;
        }

        String sql = "SELECT COUNT(*) FROM interview_rounds " +
                "WHERE application_id = ? AND round_number < ? AND (status != 'COMPLETED' OR result != 'PASSED')";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);
            statement.setInt(2, roundNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // 9. CHECK IF ACTIVE ROUND EXISTS
    // =====================================================
    public boolean hasActiveOrPendingRound(Long applicationId) {
        if (applicationId == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM interview_rounds " +
                "WHERE application_id = ? AND status IN ('SCHEDULED', 'RESCHEDULED') AND result = 'PENDING'";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // 10. CHECK IF ANY ROUND HAS FAILED
    // =====================================================
    public boolean hasAnyFailedRound(Long applicationId) {
        if (applicationId == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM interview_rounds WHERE application_id = ? AND result = 'FAILED'";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // 11. CHECK IF ALL ROUNDS ARE COMPLETED & PASSED
    // =====================================================
    public boolean isAllRoundsCompletedAndPassed(Long applicationId) {
        if (applicationId == null) {
            return false;
        }

        String countSql = "SELECT COUNT(*) FROM interview_rounds WHERE application_id = ?";
        String pendingOrFailedSql = "SELECT COUNT(*) FROM interview_rounds " +
                "WHERE application_id = ? AND (status != 'COMPLETED' OR result != 'PASSED')";

        try (Connection connection = getConnection()) {
            int totalRounds = 0;
            try (PreparedStatement stmt = connection.prepareStatement(countSql)) {
                stmt.setLong(1, applicationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalRounds = rs.getInt(1);
                    }
                }
            }

            if (totalRounds == 0) {
                return false;
            }

            try (PreparedStatement stmt = connection.prepareStatement(pendingOrFailedSql)) {
                stmt.setLong(1, applicationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // 12. GET ROUND STATISTICS FOR APPLICATION
    // =====================================================
    public Map<String, Integer> getRoundStatistics(Long applicationId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL", 0);
        stats.put("SCHEDULED", 0);
        stats.put("COMPLETED", 0);
        stats.put("PASSED", 0);
        stats.put("FAILED", 0);
        stats.put("CANCELLED", 0);

        if (applicationId == null) {
            return stats;
        }

        String sql = "SELECT status, result, COUNT(*) AS cnt FROM interview_rounds WHERE application_id = ? GROUP BY status, result";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                int total = 0;
                while (rs.next()) {
                    String status = rs.getString("status");
                    String result = rs.getString("result");
                    int count = rs.getInt("cnt");

                    total += count;

                    if (InterviewRound.STATUS_SCHEDULED.equalsIgnoreCase(status) ||
                            InterviewRound.STATUS_RESCHEDULED.equalsIgnoreCase(status)) {
                        stats.put("SCHEDULED", stats.get("SCHEDULED") + count);
                    } else if (InterviewRound.STATUS_COMPLETED.equalsIgnoreCase(status)) {
                        stats.put("COMPLETED", stats.get("COMPLETED") + count);
                    } else if (InterviewRound.STATUS_CANCELLED.equalsIgnoreCase(status)) {
                        stats.put("CANCELLED", stats.get("CANCELLED") + count);
                    }

                    if (InterviewRound.RESULT_PASSED.equalsIgnoreCase(result)) {
                        stats.put("PASSED", stats.get("PASSED") + count);
                    } else if (InterviewRound.RESULT_FAILED.equalsIgnoreCase(result)) {
                        stats.put("FAILED", stats.get("FAILED") + count);
                    }
                }
                stats.put("TOTAL", total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    private InterviewRound mapResultSetToRound(ResultSet rs) throws SQLException {
        InterviewRound r = new InterviewRound();
        r.setId(rs.getLong("id"));
        r.setApplicationId(rs.getLong("application_id"));
        r.setRoundNumber(rs.getInt("round_number"));
        r.setRoundName(rs.getString("round_name"));

        Date date = rs.getDate("interview_date");
        if (date != null) {
            r.setInterviewDate(date.toLocalDate());
        }

        Time time = rs.getTime("interview_time");
        if (time != null) {
            r.setInterviewTime(time.toLocalTime());
        }

        r.setMode(rs.getString("mode"));
        r.setInterviewerName(rs.getString("interviewer_name"));
        r.setInterviewerInfo(rs.getString("interviewer_info"));
        r.setStatus(rs.getString("status"));
        r.setResult(rs.getString("result"));
        r.setRemarks(rs.getString("remarks"));
        r.setFeedback(rs.getString("feedback"));
        r.setCancellationReason(rs.getString("cancellation_reason"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            r.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            r.setUpdatedAt(updated.toLocalDateTime());
        }

        return r;
    }
}
