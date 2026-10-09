package com.college.placement.internship.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    // ADD APPLICATION
    public void addApplication(Application application) {

        String sql = "INSERT INTO applications " +
                "(job_id, student_name, student_email, resume) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, application.getJobId());
            statement.setString(2, application.getStudentName());
            statement.setString(3, application.getStudentEmail());
            statement.setString(4, application.getResume());

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ALL APPLICATIONS
    public List<Application> getAllApplications() {

        List<Application> applications = new ArrayList<>();

        String sql = "SELECT * FROM applications ORDER BY applied_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Application application = new Application();

                application.setId(resultSet.getInt("id"));
                application.setJobId(resultSet.getInt("job_id"));
                application.setStudentName(
                        resultSet.getString("student_name"));
                application.setStudentEmail(
                        resultSet.getString("student_email"));
                application.setResume(
                        resultSet.getString("resume"));
                application.setAppliedDate(
                        resultSet.getString("applied_date"));
                application.setStatus(
                        resultSet.getString("status"));

                applications.add(application);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }

    // UPDATE APPLICATION STATUS
    public void updateApplicationStatus(int applicationId, String status) {

        String sql = "UPDATE applications SET status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, applicationId);

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}