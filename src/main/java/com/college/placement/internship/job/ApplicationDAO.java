package com.college.placement.internship.job;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
}