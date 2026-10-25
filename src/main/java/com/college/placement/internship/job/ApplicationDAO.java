
package com.college.placement.internship.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    // ADD APPLICATION
    public void addApplication(Application application) {

        String sql = "INSERT INTO applications "
                + "(job_id, student_name, student_email, resume) "
                + "VALUES (?, ?, ?, ?)";

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
        return getFilteredApplications("", "", "", "");
    }

    // FILTER APPLICATIONS
    public List<Application> getFilteredApplications(
            String search, String jobId, String status, String sort) {

        List<Application> applications = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM applications WHERE 1=1");

        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (student_name LIKE ? OR student_email LIKE ?)");
            String keyword = "%" + search.trim() + "%";
            parameters.add(keyword);
            parameters.add(keyword);
        }

        if (jobId != null && !jobId.trim().isEmpty()) {
            try {
                int parsedJobId = Integer.parseInt(jobId.trim());

                if (parsedJobId <= 0) {
                    return applications;
                }

                sql.append(" AND job_id = ?");
                parameters.add(parsedJobId);

            } catch (NumberFormatException e) {
                return applications;
            }
        }

        if (status != null && !status.trim().isEmpty()) {
            if (!isValidStatus(status)) {
                return applications;
            }

            sql.append(" AND status = ?");
            parameters.add(status);
        }

        if ("oldest".equals(sort)) {
            sql.append(" ORDER BY applied_date ASC");
        } else {
            sql.append(" ORDER BY applied_date DESC");
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                Object parameter = parameters.get(i);

                if (parameter instanceof Integer) {
                    statement.setInt(i + 1, (Integer) parameter);
                } else {
                    statement.setString(i + 1, parameter.toString());
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }

    // UPDATE APPLICATION STATUS
    public boolean updateApplicationStatus(
            int applicationId, String status) {

        if (!isValidStatus(status)) {
            return false;
        }

        String sql = "UPDATE applications SET status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, applicationId);

            return statement.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // VALIDATE STATUS
    private boolean isValidStatus(String status) {
        return "Applied".equals(status)
                || "Shortlisted".equals(status)
                || "Selected".equals(status)
                || "Rejected".equals(status);
    }
}
