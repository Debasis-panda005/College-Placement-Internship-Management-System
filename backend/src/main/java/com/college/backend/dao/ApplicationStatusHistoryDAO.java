package com.college.backend.dao;

import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationStatusHistoryDAO {

    // =====================================================
    // 1. ADD HISTORY (ENTITY)
    // =====================================================

    public boolean addHistory(ApplicationStatusHistory history) {
        if (history == null || history.getApplicationId() == null) {
            return false;
        }

        String sql = "INSERT INTO application_status_history (application_id, status, changed_at) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, history.getApplicationId());
            statement.setString(2, history.getStatus());

            LocalDateTime changedAt = history.getChangedAt() != null
                    ? history.getChangedAt()
                    : LocalDateTime.now();
            statement.setTimestamp(3, Timestamp.valueOf(changedAt));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // 2. ADD HISTORY (CONVENIENCE METHOD: APPLICATION ID & STATUS)
    // =====================================================

    public boolean addHistory(Long applicationId, String status) {
        return addHistory(new ApplicationStatusHistory(applicationId, status, LocalDateTime.now()));
    }

    // =====================================================
    // 3. ADD HISTORY (WITH EXPLICIT TIMESTAMP)
    // =====================================================

    public boolean addHistory(Long applicationId, String status, LocalDateTime changedAt) {
        return addHistory(new ApplicationStatusHistory(applicationId, status, changedAt));
    }

    // =====================================================
    // 4. ADD HISTORY WITH EXISTING CONNECTION (TRANSACTION SUPPORT)
    // =====================================================

    public boolean addHistory(Connection connection, ApplicationStatusHistory history) throws SQLException {
        if (connection == null || history == null || history.getApplicationId() == null) {
            return false;
        }

        String sql = "INSERT INTO application_status_history (application_id, status, changed_at) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, history.getApplicationId());
            statement.setString(2, history.getStatus());

            LocalDateTime changedAt = history.getChangedAt() != null
                    ? history.getChangedAt()
                    : LocalDateTime.now();
            statement.setTimestamp(3, Timestamp.valueOf(changedAt));

            return statement.executeUpdate() > 0;
        }
    }

    // =====================================================
    // 5. GET HISTORY BY APPLICATION ID
    // =====================================================

    public List<ApplicationStatusHistory> getHistoryByApplicationId(Long applicationId) {

        List<ApplicationStatusHistory> historyList = new ArrayList<>();

        if (applicationId == null) {
            return historyList;
        }

        String sql = "SELECT id, application_id, status, changed_at " +
                "FROM application_status_history " +
                "WHERE application_id = ? " +
                "ORDER BY changed_at ASC, id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    ApplicationStatusHistory history = new ApplicationStatusHistory();

                    history.setId(resultSet.getLong("id"));
                    history.setApplicationId(resultSet.getLong("application_id"));
                    history.setStatus(resultSet.getString("status"));

                    Timestamp timestamp = resultSet.getTimestamp("changed_at");
                    if (timestamp != null) {
                        history.setChangedAt(timestamp.toLocalDateTime());
                    }

                    historyList.add(history);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }
}
