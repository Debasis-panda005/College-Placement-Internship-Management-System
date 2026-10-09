package com.college.backend.dao;

import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import com.college.backend.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAO {

    private final ApplicationStatusHistoryDAO statusHistoryDAO = new ApplicationStatusHistoryDAO();

    // 1. Create Interview
    public boolean createInterview(Interview interview) {

        String sql = "INSERT INTO interviews " +
                "(application_id, interview_date, interview_time, mode, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setLong(1, interview.getApplicationId());
                statement.setDate(2, Date.valueOf(interview.getInterviewDate()));
                statement.setTime(3, Time.valueOf(interview.getInterviewTime()));
                statement.setString(4, interview.getMode());
                statement.setString(5, interview.getStatus());

                int affected = statement.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(
                            interview.getApplicationId(),
                            "INTERVIEW_SCHEDULED",
                            LocalDateTime.now()
                    )
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

    // 2. Get All Interviews
    public List<Interview> getAllInterviews() {

        List<Interview> interviews = new ArrayList<>();

        String sql = "SELECT * FROM interviews";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Interview interview = new Interview();

                interview.setId(resultSet.getLong("id"));
                interview.setApplicationId(
                        resultSet.getLong("application_id"));

                Date date = resultSet.getDate("interview_date");
                if (date != null) {
                    interview.setInterviewDate(date.toLocalDate());
                }

                Time time = resultSet.getTime("interview_time");
                if (time != null) {
                    interview.setInterviewTime(time.toLocalTime());
                }

                interview.setMode(resultSet.getString("mode"));
                interview.setStatus(resultSet.getString("status"));

                interviews.add(interview);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return interviews;
    }

    // 3. Get Interview By ID
    public Interview getInterviewById(Long id) {

        String sql = "SELECT * FROM interviews WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Interview interview = new Interview();

                    interview.setId(resultSet.getLong("id"));
                    interview.setApplicationId(
                            resultSet.getLong("application_id"));

                    Date date = resultSet.getDate("interview_date");
                    if (date != null) {
                        interview.setInterviewDate(date.toLocalDate());
                    }

                    Time time = resultSet.getTime("interview_time");
                    if (time != null) {
                        interview.setInterviewTime(time.toLocalTime());
                    }

                    interview.setMode(resultSet.getString("mode"));
                    interview.setStatus(resultSet.getString("status"));

                    return interview;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 4. Update Interview (Reschedule with atomic history logging)
    public boolean updateInterview(Interview interview) {

        String sql = "UPDATE interviews SET " +
                "application_id = ?, " +
                "interview_date = ?, " +
                "interview_time = ?, " +
                "mode = ?, " +
                "status = ? " +
                "WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setLong(1, interview.getApplicationId());
                statement.setDate(
                        2,
                        Date.valueOf(interview.getInterviewDate())
                );
                statement.setTime(
                        3,
                        Time.valueOf(interview.getInterviewTime())
                );
                statement.setString(4, interview.getMode());
                statement.setString(5, interview.getStatus());
                statement.setLong(6, interview.getId());

                int affected = statement.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(
                            interview.getApplicationId(),
                            "INTERVIEW_RESCHEDULED",
                            LocalDateTime.now()
                    )
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

    // 5. Delete Interview
    public boolean deleteInterview(Long id) {

        String sql = "DELETE FROM interviews WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. Get Interview By Application ID
    public Interview getInterviewByApplicationId(Long applicationId) {

        String sql = "SELECT * FROM interviews WHERE application_id = ? ORDER BY id DESC LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Interview interview = new Interview();

                    interview.setId(resultSet.getLong("id"));
                    interview.setApplicationId(
                            resultSet.getLong("application_id"));

                    Date date = resultSet.getDate("interview_date");
                    if (date != null) {
                        interview.setInterviewDate(date.toLocalDate());
                    }

                    Time time = resultSet.getTime("interview_time");
                    if (time != null) {
                        interview.setInterviewTime(time.toLocalTime());
                    }

                    interview.setMode(resultSet.getString("mode"));
                    interview.setStatus(resultSet.getString("status"));

                    return interview;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 7. Check if Interview is Completed for an Application
    public boolean isInterviewCompletedForApplication(Long applicationId) {

        String sql = "SELECT COUNT(*) FROM interviews WHERE application_id = ? AND status = 'COMPLETED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 8. Check if an Interview is already scheduled for an Application
    public boolean hasActiveInterview(Long applicationId) {

        String sql = "SELECT COUNT(*) FROM interviews WHERE application_id = ? AND status IN ('SCHEDULED', 'COMPLETED')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 9. Get all Application IDs with Completed Interviews
    public List<Long> getCompletedApplicationIds() {

        List<Long> applicationIds = new ArrayList<>();

        String sql = "SELECT application_id FROM interviews WHERE status = 'COMPLETED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                applicationIds.add(resultSet.getLong("application_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applicationIds;
    }

    // 10. Complete Interview
    public boolean completeInterview(Long id) {

        Interview interview = getInterviewById(id);
        if (interview == null) {
            return false;
        }

        String sql = "UPDATE interviews SET status = 'COMPLETED' WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(
                            interview.getApplicationId(),
                            "INTERVIEW_COMPLETED",
                            LocalDateTime.now()
                    )
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

    // 11. Search and Filter Interviews
    public List<Interview> searchInterviews(Long applicationId,
                                            LocalDate interviewDate,
                                            String mode,
                                            String status) {

        List<Interview> interviews = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM interviews WHERE 1=1");

        if (applicationId != null) {
            sql.append(" AND application_id = ?");
        }

        if (interviewDate != null) {
            sql.append(" AND interview_date = ?");
        }

        if (mode != null && !mode.trim().isEmpty() && !"ALL".equalsIgnoreCase(mode.trim())) {
            sql.append(" AND mode = ?");
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append(" AND status = ?");
        }

        sql.append(" ORDER BY interview_date DESC, interview_time DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;

            if (applicationId != null) {
                statement.setLong(index++, applicationId);
            }

            if (interviewDate != null) {
                statement.setDate(index++, Date.valueOf(interviewDate));
            }

            if (mode != null && !mode.trim().isEmpty() && !"ALL".equalsIgnoreCase(mode.trim())) {
                statement.setString(index++, mode.trim());
            }

            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                statement.setString(index++, status.trim());
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Interview interview = new Interview();

                    interview.setId(resultSet.getLong("id"));
                    interview.setApplicationId(
                            resultSet.getLong("application_id")
                    );

                    Date date = resultSet.getDate("interview_date");
                    if (date != null) {
                        interview.setInterviewDate(date.toLocalDate());
                    }

                    Time time = resultSet.getTime("interview_time");
                    if (time != null) {
                        interview.setInterviewTime(time.toLocalTime());
                    }

                    interview.setMode(resultSet.getString("mode"));
                    interview.setStatus(resultSet.getString("status"));

                    interviews.add(interview);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return interviews;
    }

    // 12. Cancel Interview (Transactional with history logging)
    public boolean cancelInterview(Long id) {

        if (id == null) {
            return false;
        }

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            Long applicationId = null;
            String currentStatus = null;

            String selectSql = "SELECT application_id, status FROM interviews WHERE id = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setLong(1, id);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        applicationId = rs.getLong("application_id");
                        currentStatus = rs.getString("status");
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            if (!"SCHEDULED".equals(currentStatus)) {
                connection.rollback();
                return false;
            }

            String updateSql = "UPDATE interviews SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setLong(1, id);
                int affected = updateStmt.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(
                            applicationId,
                            "INTERVIEW_CANCELLED",
                            LocalDateTime.now()
                    )
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
}
