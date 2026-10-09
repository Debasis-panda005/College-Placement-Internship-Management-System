package com.college.backend.entity;

import java.io.Serializable;

public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String salary;
    private String jobType; // "Full Time", "Part Time", "Internship"
    private String status;  // "Active", "Closed"

    public Job() {
    }

    public Job(String title, String company, String location, String description,
               String salary, String jobType, String status) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
    }

    public Job(Long id, String title, String company, String location, String description,
               String salary, String jobType, String status) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", salary='" + salary + '\'' +
                ", jobType='" + jobType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
