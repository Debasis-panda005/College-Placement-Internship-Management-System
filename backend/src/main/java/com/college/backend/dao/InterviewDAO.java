package com.college.backend.dao;

import com.college.backend.entity.Interview;
import com.college.backend.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAO {

    // 1. Create Interview
    public boolean createInterview(Interview interview) {

        String sql = "INSERT INTO interviews " +
                "(application_id, interview_date, interview_time, mode, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, interview.getApplicationId());
            statement.setDate(2, Date.valueOf(interview.getInterviewDate()));
            statement.setTime(3, Time.valueOf(interview.getInterviewTime()));
            statement.setString(4, interview.getMode());
            statement.setString(5, interview.getStatus());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    // 4. Update Interview
    public boolean updateInterview(Interview interview) {

        String sql = "UPDATE interviews SET " +
                "application_id = ?, " +
                "interview_date = ?, " +
                "interview_time = ?, " +
                "mode = ?, " +
                "status = ? " +
                "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

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

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

        String sql = "SELECT * FROM interviews WHERE application_id = ?";

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

        String sql = "SELECT COUNT(*) FROM interviews WHERE application_id = ?";

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
}