package com.college.backend.dao;

import com.college.backend.entity.Application;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    // =====================================================
    // 1. CREATE APPLICATION
    // =====================================================

    public boolean createApplication(Application application) {

        String sql = "INSERT INTO applications " +
                "(student_id, job_id, application_date, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, application.getStudentId());
            statement.setLong(2, application.getJobId());
            statement.setDate(
                    3,
                    Date.valueOf(application.getApplicationDate())
            );
            statement.setString(4, application.getStatus());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 2. GET ALL APPLICATIONS
    // =====================================================

    public List<Application> getAllApplications() {

        List<Application> applications = new ArrayList<>();

        String sql = "SELECT * FROM applications ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Application application = new Application();

                application.setId(
                        resultSet.getLong("id")
                );

                application.setStudentId(
                        resultSet.getLong("student_id")
                );

                application.setJobId(
                        resultSet.getLong("job_id")
                );

                Date date =
                        resultSet.getDate("application_date");

                if (date != null) {
                    application.setApplicationDate(
                            date.toLocalDate()
                    );
                }

                application.setStatus(
                        resultSet.getString("status")
                );

                applications.add(application);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applications;
    }


    // =====================================================
    // 3. GET APPLICATION BY ID
    // =====================================================

    public Application getApplicationById(Long id) {

        String sql =
                "SELECT * FROM applications WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    Application application =
                            new Application();

                    application.setId(
                            resultSet.getLong("id")
                    );

                    application.setStudentId(
                            resultSet.getLong("student_id")
                    );

                    application.setJobId(
                            resultSet.getLong("job_id")
                    );

                    Date date =
                            resultSet.getDate("application_date");

                    if (date != null) {
                        application.setApplicationDate(
                                date.toLocalDate()
                        );
                    }

                    application.setStatus(
                            resultSet.getString("status")
                    );

                    return application;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // 4. UPDATE APPLICATION
    // =====================================================

    public boolean updateApplication(Application application) {

        String sql =
                "UPDATE applications SET " +
                        "student_id = ?, " +
                        "job_id = ?, " +
                        "application_date = ?, " +
                        "status = ? " +
                        "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    application.getStudentId()
            );

            statement.setLong(
                    2,
                    application.getJobId()
            );

            statement.setDate(
                    3,
                    Date.valueOf(
                            application.getApplicationDate()
                    )
            );

            statement.setString(
                    4,
                    application.getStatus()
            );

            statement.setLong(
                    5,
                    application.getId()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 5. DELETE APPLICATION
    // =====================================================

    public boolean deleteApplication(Long id) {

        String sql =
                "DELETE FROM applications WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 6. SHORTLIST APPLICATION
    // =====================================================

    public boolean shortlistApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "SHORTLISTED");
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 7. SELECT APPLICATION
    // =====================================================

    public boolean selectApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "SELECTED");
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 8. REJECT APPLICATION
    // =====================================================

    public boolean rejectApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "REJECTED");
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 9. GET SHORTLISTED APPLICATIONS
    // =====================================================

    public List<Application> getShortlistedApplications() {

        List<Application> applications = new ArrayList<>();

        String sql = "SELECT * FROM applications WHERE status = 'SHORTLISTED' ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Application application = new Application();

                application.setId(
                        resultSet.getLong("id")
                );

                application.setStudentId(
                        resultSet.getLong("student_id")
                );

                application.setJobId(
                        resultSet.getLong("job_id")
                );

                Date date =
                        resultSet.getDate("application_date");

                if (date != null) {
                    application.setApplicationDate(
                            date.toLocalDate()
                    );
                }

                application.setStatus(
                        resultSet.getString("status")
                );

                applications.add(application);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applications;
    }
}