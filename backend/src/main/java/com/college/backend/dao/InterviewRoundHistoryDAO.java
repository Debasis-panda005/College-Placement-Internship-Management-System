package com.college.backend.dao;

import com.college.backend.entity.InterviewRoundHistory;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterviewRoundHistoryDAO {

    public boolean addHistory(Connection connection, InterviewRoundHistory history) throws SQLException {
        if (history == null) {
            return false;
        }

        String sql = "INSERT INTO interview_round_history " +
                "(round_id, application_id, action, action_date, previous_status, new_status, " +
                "previous_result, new_result, remarks, performed_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, history.getRoundId());
            statement.setLong(2, history.getApplicationId());
            statement.setString(3, history.getAction());
            statement.setTimestamp(4, history.getActionDate() != null ?
                    Timestamp.valueOf(history.getActionDate()) : Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(5, history.getPreviousStatus());
            statement.setString(6, history.getNewStatus());
            statement.setString(7, history.getPreviousResult());
            statement.setString(8, history.getNewResult());
            statement.setString(9, history.getRemarks());
            statement.setString(10, history.getPerformedBy());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean addHistory(InterviewRoundHistory history) {
        if (history == null) {
            return false;
        }

        try (Connection connection = DBConnection.getConnection()) {
            return addHistory(connection, history);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InterviewRoundHistory> getHistoryByRoundId(Long roundId) {
        List<InterviewRoundHistory> list = new ArrayList<>();
        if (roundId == null) {
            return list;
        }

        String sql = "SELECT * FROM interview_round_history WHERE round_id = ? ORDER BY action_date ASC, id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, roundId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<InterviewRoundHistory> getHistoryByApplicationId(Long applicationId) {
        List<InterviewRoundHistory> list = new ArrayList<>();
        if (applicationId == null) {
            return list;
        }

        String sql = "SELECT * FROM interview_round_history WHERE application_id = ? ORDER BY action_date ASC, id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private InterviewRoundHistory mapResultSetToHistory(ResultSet rs) throws SQLException {
        InterviewRoundHistory h = new InterviewRoundHistory();
        h.setId(rs.getLong("id"));
        h.setRoundId(rs.getLong("round_id"));
        h.setApplicationId(rs.getLong("application_id"));
        h.setAction(rs.getString("action"));

        Timestamp ts = rs.getTimestamp("action_date");
        if (ts != null) {
            h.setActionDate(ts.toLocalDateTime());
        }

        h.setPreviousStatus(rs.getString("previous_status"));
        h.setNewStatus(rs.getString("new_status"));
        h.setPreviousResult(rs.getString("previous_result"));
        h.setNewResult(rs.getString("new_result"));
        h.setRemarks(rs.getString("remarks"));
        h.setPerformedBy(rs.getString("performed_by"));
        return h;
    }
}
