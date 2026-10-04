package com.college.backend.entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Interview {

    private Long id;
    private Long applicationId;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String mode;
    private String status;

    // Default constructor
    public Interview() {
    }

    // Parameterized constructor
    public Interview(Long applicationId,
                     LocalDate interviewDate,
                     LocalTime interviewTime,
                     String mode,
                     String status) {

        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.mode = mode;
        this.status = status;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public LocalTime getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(LocalTime interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}