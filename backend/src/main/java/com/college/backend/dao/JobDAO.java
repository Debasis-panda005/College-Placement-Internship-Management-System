package com.college.backend.dao;

import com.college.backend.entity.Job;
import com.college.backend.exception.DAOException;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobDAO {

    /**
     * Obtains a database connection. Protected to allow test overrides.
     */
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // =====================================================
    // 1. ADD JOB
    // =====================================================

    public boolean addJob(Job job) {
        if (job == null) {
            return false;
        }

        if (job.getDuration() != null && !job.getDuration().trim().isEmpty()) {
            String sqlWithDuration = "INSERT INTO jobs (title, company, location, description, salary, duration, job_type, status, deadline, min_cgpa, eligible_departments) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sqlWithDuration, Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, job.getTitle());
                statement.setString(2, job.getCompany());
                statement.setString(3, job.getLocation());
                statement.setString(4, job.getDescription());
                statement.setString(5, job.getSalary());
                statement.setString(6, job.getDuration());
                statement.setString(7, job.getJobType());
                statement.setString(8, job.getStatus() != null ? job.getStatus() : "Active");
                if (job.getDeadline() != null) {
                    statement.setDate(9, Date.valueOf(job.getDeadline()));
                } else {
                    statement.setNull(9, Types.DATE);
                }
                statement.setBigDecimal(10, job.getMinCgpa());
                statement.setString(11, job.getEligibleDepartments());

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
                // If duration column is not present in schema, fallback to insert without duration
                if (e.getErrorCode() == 1054 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("duration"))) {
                    return addJobWithoutDuration(job);
                }
                e.printStackTrace();
                return false;
            }
        }

        return addJobWithoutDuration(job);
    }

    private boolean addJobWithoutDuration(Job job) {
        String sql = "INSERT INTO jobs (title, company, location, description, salary, job_type, status, deadline, min_cgpa, eligible_departments) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getDescription());
            statement.setString(5, job.getSalary());
            statement.setString(6, job.getJobType());
            statement.setString(7, job.getStatus() != null ? job.getStatus() : "Active");
            if (job.getDeadline() != null) {
                statement.setDate(8, Date.valueOf(job.getDeadline()));
            } else {
                statement.setNull(8, Types.DATE);
            }
            statement.setBigDecimal(9, job.getMinCgpa());
            statement.setString(10, job.getEligibleDepartments());

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
        String sql = "SELECT * FROM jobs ORDER BY id DESC";

        try (Connection connection = getConnection();
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

        String sql = "SELECT * FROM jobs WHERE id = ?";

        try (Connection connection = getConnection();
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

        if (job.getDuration() != null && !job.getDuration().trim().isEmpty()) {
            String sqlWithDuration = "UPDATE jobs SET title = ?, company = ?, location = ?, description = ?, " +
                    "salary = ?, duration = ?, job_type = ?, status = ?, deadline = ?, min_cgpa = ?, eligible_departments = ? WHERE id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sqlWithDuration)) {

                statement.setString(1, job.getTitle());
                statement.setString(2, job.getCompany());
                statement.setString(3, job.getLocation());
                statement.setString(4, job.getDescription());
                statement.setString(5, job.getSalary());
                statement.setString(6, job.getDuration());
                statement.setString(7, job.getJobType());
                statement.setString(8, job.getStatus() != null ? job.getStatus() : "Active");
                if (job.getDeadline() != null) {
                    statement.setDate(9, Date.valueOf(job.getDeadline()));
                } else {
                    statement.setNull(9, Types.DATE);
                }
                statement.setBigDecimal(10, job.getMinCgpa());
                statement.setString(11, job.getEligibleDepartments());
                statement.setLong(12, job.getId());

                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                // If duration column is not present in schema, fallback to update without duration
                if (e.getErrorCode() == 1054 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("duration"))) {
                    return updateJobWithoutDuration(job);
                }
                e.printStackTrace();
                return false;
            }
        }

        return updateJobWithoutDuration(job);
    }

    private boolean updateJobWithoutDuration(Job job) {
        String sql = "UPDATE jobs SET title = ?, company = ?, location = ?, description = ?, " +
                "salary = ?, job_type = ?, status = ?, deadline = ?, min_cgpa = ?, eligible_departments = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getDescription());
            statement.setString(5, job.getSalary());
            statement.setString(6, job.getJobType());
            statement.setString(7, job.getStatus() != null ? job.getStatus() : "Active");
            if (job.getDeadline() != null) {
                statement.setDate(8, Date.valueOf(job.getDeadline()));
            } else {
                statement.setNull(8, Types.DATE);
            }
            statement.setBigDecimal(9, job.getMinCgpa());
            statement.setString(10, job.getEligibleDepartments());
            statement.setLong(11, job.getId());

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

        try (Connection connection = getConnection();
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

        String sql = "SELECT * FROM jobs WHERE title LIKE ? OR company LIKE ? OR location LIKE ? ORDER BY id DESC";

        try (Connection connection = getConnection();
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

        String sql = "SELECT * FROM jobs WHERE job_type = ? ORDER BY id DESC";

        try (Connection connection = getConnection();
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

    public List<Job> getAllInternships() {
        return getJobsByType("Internship");
    }

    // =====================================================
    // 8. GET JOBS BY STATUS
    // =====================================================

    public List<Job> getJobsByStatus(String status) {
        List<Job> jobs = new ArrayList<>();
        if (status == null || status.trim().isEmpty()) {
            return getAllJobs();
        }

        String sql = "SELECT * FROM jobs WHERE status = ? ORDER BY id DESC";

        try (Connection connection = getConnection();
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
    // 8B. GET JOB STATISTICS
    // =====================================================

    /**
     * Retrieves aggregated statistics for jobs and internships.
     * Uses efficient database-level aggregation to compute counts in a single query.
     *
     * @return Map containing TOTAL, JOBS, INTERNSHIPS, ACTIVE, and CLOSED counts
     * @throws DAOException on database failure
     */
    public Map<String, Integer> getJobStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL", 0);
        stats.put("JOBS", 0);
        stats.put("INTERNSHIPS", 0);
        stats.put("ACTIVE", 0);
        stats.put("CLOSED", 0);

        String sql = "SELECT " +
                "COUNT(*) AS total_count, " +
                "COALESCE(SUM(CASE WHEN job_type != 'Internship' THEN 1 ELSE 0 END), 0) AS jobs_count, " +
                "COALESCE(SUM(CASE WHEN job_type = 'Internship' THEN 1 ELSE 0 END), 0) AS internships_count, " +
                "COALESCE(SUM(CASE WHEN status = 'Active' THEN 1 ELSE 0 END), 0) AS active_count, " +
                "COALESCE(SUM(CASE WHEN status = 'Closed' THEN 1 ELSE 0 END), 0) AS closed_count " +
                "FROM jobs";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                stats.put("TOTAL", resultSet.getInt("total_count"));
                stats.put("JOBS", resultSet.getInt("jobs_count"));
                stats.put("INTERNSHIPS", resultSet.getInt("internships_count"));
                stats.put("ACTIVE", resultSet.getInt("active_count"));
                stats.put("CLOSED", resultSet.getInt("closed_count"));
            }

        } catch (SQLException e) {
            throw new DAOException("Database error retrieving job statistics: " + e.getMessage(), e);
        }

        return stats;
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
        String sql = "SELECT * FROM jobs ORDER BY " + sortClause;

        try (Connection connection = getConnection();
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

        try {
            Date deadline = resultSet.getDate("deadline");
            if (deadline != null) {
                job.setDeadline(deadline.toLocalDate());
            }
        } catch (SQLException ignored) {
            // column not present in pre-migration schema
        }

        try {
            job.setMinCgpa(resultSet.getBigDecimal("min_cgpa"));
        } catch (SQLException ignored) {
            // column not present in pre-migration schema
        }

        try {
            job.setEligibleDepartments(resultSet.getString("eligible_departments"));
        } catch (SQLException ignored) {
            // column not present in pre-migration schema
        }

        try {
            job.setDuration(resultSet.getString("duration"));
        } catch (SQLException ignored) {
            // column not present in pre-migration schema
        }

        return job;
    }
}
