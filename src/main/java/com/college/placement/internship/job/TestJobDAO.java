package com.college.placement.internship.job;

public class TestJobDAO {

    public static void main(String[] args) {

        JobDAO jobDAO = new JobDAO();

        // Update job with ID 1
        Job job = new Job();

        job.setId(1);
        job.setTitle("Senior Java Developer");
        job.setCompany("TCS");
        job.setLocation("Bhubaneswar");
        job.setDescription("Java development and backend application work");
        job.setSalary("7 LPA");
        job.setJobType("Full Time");

        jobDAO.updateJob(job);
    }
}