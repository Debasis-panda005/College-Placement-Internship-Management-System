package com.college.backend.dao;

import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDAO {

    private final ApplicationStatusHistoryDAO statusHistoryDAO = new ApplicationStatusHistoryDAO();

    // =====================================================
    // 1. CREATE APPLICATION
    // =====================================================

    public boolean createApplication(Application application) {

        String sql = "INSERT INTO applications " +
                "(student_id, job_id, application_date, status) " +
                "VALUES (?, ?, ?, ?)";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            Long generatedId = null;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                statement.setLong(1, application.getStudentId());
                statement.setLong(2, application.getJobId());
                statement.setDate(
                        3,
                        Date.valueOf(application.getApplicationDate())
                );
                statement.setString(4, application.getStatus());

                int affected = statement.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getLong(1);
                        application.setId(generatedId);
                    }
                }
            }

            if (generatedId == null) {
                generatedId = application.getId();
            }

            if (generatedId == null) {
                connection.rollback();
                return false;
            }

            String status = application.getStatus() != null ? application.getStatus() : "APPLIED";
            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(generatedId, status, LocalDateTime.now())
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

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "SHORTLISTED");
                statement.setLong(2, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(id, "SHORTLISTED", LocalDateTime.now())
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


    // =====================================================
    // 7. SELECT APPLICATION
    // =====================================================

    public boolean selectApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "SELECTED");
                statement.setLong(2, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(id, "SELECTED", LocalDateTime.now())
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


    // =====================================================
    // 8. REJECT APPLICATION
    // =====================================================

    public boolean rejectApplication(Long id) {

        String sql =
                "UPDATE applications " +
                        "SET status = ? " +
                        "WHERE id = ?";

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "REJECTED");
                statement.setLong(2, id);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            boolean historyAdded = statusHistoryDAO.addHistory(
                    connection,
                    new ApplicationStatusHistory(id, "REJECTED", LocalDateTime.now())
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


    // =====================================================
    // 10. CHECK DUPLICATE APPLICATION
    // =====================================================

    public boolean hasAlreadyApplied(Long studentId, Long jobId) {

        String sql = "SELECT COUNT(*) FROM applications WHERE student_id = ? AND job_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, studentId);
            statement.setLong(2, jobId);

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


    // =====================================================
    // 11. SEARCH AND FILTER APPLICATIONS
    // =====================================================

    public List<Application> searchApplications(Long studentId, Long jobId, String status) {

        List<Application> applications = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM applications WHERE 1=1");

        if (studentId != null) {
            sql.append(" AND student_id = ?");
        }

        if (jobId != null) {
            sql.append(" AND job_id = ?");
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append(" AND status = ?");
        }

        sql.append(" ORDER BY id");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;

            if (studentId != null) {
                statement.setLong(index++, studentId);
            }

            if (jobId != null) {
                statement.setLong(index++, jobId);
            }

            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                statement.setString(index++, status.trim());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Application application = new Application();
                    application.setId(resultSet.getLong("id"));
                    application.setStudentId(resultSet.getLong("student_id"));
                    application.setJobId(resultSet.getLong("job_id"));

                    Date date = resultSet.getDate("application_date");
                    if (date != null) {
                        application.setApplicationDate(date.toLocalDate());
                    }

                    application.setStatus(resultSet.getString("status"));
                    applications.add(application);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applications;
    }


    // =====================================================
    // 12. GET APPLICATION STATISTICS
    // =====================================================

    public Map<String, Integer> getApplicationStatistics() {

        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL", 0);
        stats.put("APPLIED", 0);
        stats.put("SHORTLISTED", 0);
        stats.put("SELECTED", 0);
        stats.put("REJECTED", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM applications GROUP BY status";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            int total = 0;

            while (resultSet.next()) {
                String status = resultSet.getString("status");
                int count = resultSet.getInt("cnt");

                if (status != null) {
                    stats.put(status.toUpperCase(), count);
                }

                total += count;
            }

            stats.put("TOTAL", total);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }
}
