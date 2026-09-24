package com.college.placement.internship.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

    // ADD JOB
    public void addJob(Job job) {

        String sql = "INSERT INTO jobs " +
                "(title, company, location, description, salary, job_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getDescription());
            statement.setString(5, job.getSalary());
            statement.setString(6, job.getJobType());

            statement.executeUpdate();

            System.out.println("Job added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // VIEW ALL JOBS
    public List<Job> getAllJobs() {

        List<Job> jobs = new ArrayList<>();

        String sql = "SELECT * FROM jobs";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Job job = new Job();

                job.setId(resultSet.getInt("id"));
                job.setTitle(resultSet.getString("title"));
                job.setCompany(resultSet.getString("company"));
                job.setLocation(resultSet.getString("location"));
                job.setDescription(resultSet.getString("description"));
                job.setSalary(resultSet.getString("salary"));
                job.setJobType(resultSet.getString("job_type"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // DELETE JOB
    public void deleteJob(int id) {

        String sql = "DELETE FROM jobs WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

            System.out.println("Job deleted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // UPDATE JOB
    public void updateJob(Job job) {

        String sql = "UPDATE jobs SET title = ?, company = ?, location = ?, " +
                "description = ?, salary = ?, job_type = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getDescription());
            statement.setString(5, job.getSalary());
            statement.setString(6, job.getJobType());
            statement.setInt(7, job.getId());

            statement.executeUpdate();

            System.out.println("Job updated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}