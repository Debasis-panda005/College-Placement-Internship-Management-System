package com.college.backend.entity;

import java.time.LocalDate;

public class Application {

    private Long id;
    private Long studentId;
    private Long jobId;
    private LocalDate applicationDate;
    private String status;

    // Default constructor
    public Application() {
    }

    // Parameterized constructor
    public Application(Long studentId,
                       Long jobId,
                       LocalDate applicationDate,
                       String status) {

        this.studentId = studentId;
        this.jobId = jobId;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}