package com.college.backend.dao;

import com.college.backend.entity.Job;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

    // =====================================================
    // 1. ADD JOB
    // =====================================================

    public boolean addJob(Job job) {
        if (job == null) {
            return false;
        }

        String sql = "INSERT INTO jobs (title, company, location, description, salary, job_type, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getDescription());
            statement.setString(5, job.getSalary());
            statement.setString(6, job.getJobType());
            statement.setString(7, job.getStatus() != null ? job.getStatus() : "Active");

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        job.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // 2. GET ALL JOBS
    // =====================================================

    public List<Job> getAllJobs() {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT id, title, company, location, description, salary, job_type, status " +
                "FROM jobs ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                jobs.add(mapResultSetToJob(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // =====================================================
    // 3. GET JOB BY ID
    // =====================================================

    public Job getJobById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT id, title, company, location, description, salary, job_type, status " +
                "FROM jobs WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToJob(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =====================================================
    // 4. UPDATE JOB
    // =====================================================

    public boolean updateJob(Job job) {
        if (job == null || job.getId() == null) {
            return false;
        }

        String sql = "UPDATE jobs SET title = ?, company = ?, location = ?, description = ?, " +
                "salary = ?, job_type = ?, status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getDescription());
            statement.setString(5, job.getSalary());
            statement.setString(6, job.getJobType());
            statement.setString(7, job.getStatus() != null ? job.getStatus() : "Active");
            statement.setLong(8, job.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // 5. DELETE JOB
    // =====================================================

    public boolean deleteJob(Long id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM jobs WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // 6. SEARCH JOBS
    // =====================================================

    public List<Job> searchJobs(String keyword) {
        List<Job> jobs = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllJobs();
        }

        String sql = "SELECT id, title, company, location, description, salary, job_type, status " +
                "FROM jobs WHERE title LIKE ? OR company LIKE ? OR location LIKE ? ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    jobs.add(mapResultSetToJob(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // =====================================================
    // 7. GET JOBS BY TYPE
    // =====================================================

    public List<Job> getJobsByType(String jobType) {
        List<Job> jobs = new ArrayList<>();
        if (jobType == null || jobType.trim().isEmpty()) {
            return getAllJobs();
        }

        String sql = "SELECT id, title, company, location, description, salary, job_type, status " +
                "FROM jobs WHERE job_type = ? ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, jobType.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    jobs.add(mapResultSetToJob(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // =====================================================
    // 8. GET JOBS BY STATUS
    // =====================================================

    public List<Job> getJobsByStatus(String status) {
        List<Job> jobs = new ArrayList<>();
        if (status == null || status.trim().isEmpty()) {
            return getAllJobs();
        }

        String sql = "SELECT id, title, company, location, description, salary, job_type, status " +
                "FROM jobs WHERE status = ? ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    jobs.add(mapResultSetToJob(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // =====================================================
    // 9. GET JOBS SORTED BY (WHITELIST VALIDATION)
    // =====================================================

    public static final String SALARY_SORT_CLAUSE =
            "CASE " +
            "WHEN UPPER(salary) LIKE '%LPA%' " +
            "AND SUBSTRING_INDEX(TRIM(REPLACE(REPLACE(UPPER(salary), 'LPA', ' LPA'), ',', '')), ' ', 1) REGEXP '^[0-9]+([.][0-9]+)?$' " +
            "THEN CAST(SUBSTRING_INDEX(TRIM(REPLACE(REPLACE(UPPER(salary), 'LPA', ' LPA'), ',', '')), ' ', 1) AS DECIMAL(15,2)) * 100000 " +
            "WHEN (UPPER(salary) LIKE '%MONTH%' OR UPPER(salary) LIKE '%/M%') " +
            "AND SUBSTRING_INDEX(TRIM(REPLACE(REPLACE(salary, ',', ''), '/', ' ')), ' ', 1) REGEXP '^[0-9]+([.][0-9]+)?$' " +
            "THEN CAST(SUBSTRING_INDEX(TRIM(REPLACE(REPLACE(salary, ',', ''), '/', ' ')), ' ', 1) AS DECIMAL(15,2)) * 12 " +
            "WHEN SUBSTRING_INDEX(TRIM(REPLACE(salary, ',', '')), ' ', 1) REGEXP '^[0-9]+([.][0-9]+)?$' " +
            "THEN CAST(SUBSTRING_INDEX(TRIM(REPLACE(salary, ',', '')), ' ', 1) AS DECIMAL(15,2)) " +
            "ELSE 0 END DESC, id DESC";

    public static String resolveSortOrder(String column) {
        if (column == null) {
            return "id DESC";
        }
        String normalized = column.trim().toLowerCase();
        switch (normalized) {
            case "title":
                return "title ASC";
            case "company":
                return "company ASC";
            case "jobtype":
            case "job_type":
                return "job_type ASC";
            case "location":
                return "location ASC";
            case "salary":
                return SALARY_SORT_CLAUSE;
            default:
                return "id DESC";
        }
    }

    public static double parseSalaryToNumericAnnual(String salary) {
        if (salary == null || salary.trim().isEmpty()) {
            return 0.0;
        }
        String upper = salary.trim().toUpperCase();
        String cleaned = upper.replace(",", "");

        if (cleaned.contains("LPA")) {
            String numPart = cleaned.replace("LPA", " LPA").trim();
            String firstToken = numPart.split("\\s+")[0];
            try {
                return Double.parseDouble(firstToken) * 100000.0;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        if (cleaned.contains("MONTH") || cleaned.contains("/M")) {
            String numPart = cleaned.replace("/", " ").replace("MONTH", " ").replace("PER", " ").trim();
            String firstToken = numPart.split("\\s+")[0];
            try {
                return Double.parseDouble(firstToken) * 12.0;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        String firstToken = cleaned.split("\\s+")[0];
        try {
            return Double.parseDouble(firstToken);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public List<Job> getJobsSortedBy(String column) {
        List<Job> jobs = new ArrayList<>();
        String sortClause = resolveSortOrder(column);
        String sql = "SELECT id, title, company, location, description, salary, job_type, status " +
                "FROM jobs ORDER BY " + sortClause;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                jobs.add(mapResultSetToJob(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // =====================================================
    // HELPER: MAP RESULT SET
    // =====================================================

    private Job mapResultSetToJob(ResultSet resultSet) throws SQLException {
        Job job = new Job();
        job.setId(resultSet.getLong("id"));
        job.setTitle(resultSet.getString("title"));
        job.setCompany(resultSet.getString("company"));
        job.setLocation(resultSet.getString("location"));
        job.setDescription(resultSet.getString("description"));
        job.setSalary(resultSet.getString("salary"));
        job.setJobType(resultSet.getString("job_type"));
        job.setStatus(resultSet.getString("status"));
        return job;
    }
}
