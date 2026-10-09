package com.college.backend.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationStatusHistory {

    private Long id;
    private Long applicationId;
    private String status;
    private LocalDateTime changedAt;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // Default constructor
    public ApplicationStatusHistory() {
    }

    // Parameterized constructor without ID
    public ApplicationStatusHistory(Long applicationId,
                                  String status,
                                  LocalDateTime changedAt) {
        this.applicationId = applicationId;
        this.status = status;
        this.changedAt = changedAt;
    }

    // Parameterized constructor with ID
    public ApplicationStatusHistory(Long id,
                                  Long applicationId,
                                  String status,
                                  LocalDateTime changedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.status = status;
        this.changedAt = changedAt;
    }

    // Convenience constructor defaulting changedAt to current time
    public ApplicationStatusHistory(Long applicationId, String status) {
        this.applicationId = applicationId;
        this.status = status;
        this.changedAt = LocalDateTime.now();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    // Helper method for JSP formatting: e.g. "08 Oct 2026, 10:30 AM"
    public String getFormattedChangedAt() {
        if (changedAt == null) {
            return "";
        }
        return changedAt.format(FORMATTER);
    }

    // Helper method for UI status label (e.g. INTERVIEW SCHEDULED)
    public String getDisplayStatus() {
        if (status == null) {
            return "";
        }
        if ("INTERVIEW_SCHEDULED".equalsIgnoreCase(status)) {
            return "INTERVIEW SCHEDULED";
        }
        if ("INTERVIEW_RESCHEDULED".equalsIgnoreCase(status)) {
            return "INTERVIEW RESCHEDULED";
        }
        if ("INTERVIEW_COMPLETED".equalsIgnoreCase(status)) {
            return "INTERVIEW COMPLETED";
        }
        if ("INTERVIEW_CANCELLED".equalsIgnoreCase(status)) {
            return "INTERVIEW CANCELLED";
        }
        return status;
    }

    // Helper method for timeline status description
    public String getStatusDescription() {
        if (status == null) {
            return "";
        }
        switch (status.toUpperCase()) {
            case "APPLIED":
                return "Application created";
            case "SHORTLISTED":
                return "Candidate shortlisted";
            case "INTERVIEW_SCHEDULED":
            case "INTERVIEW SCHEDULED":
                return "Interview scheduled";
            case "INTERVIEW_RESCHEDULED":
            case "INTERVIEW RESCHEDULED":
                return "Interview rescheduled";
            case "INTERVIEW_COMPLETED":
            case "INTERVIEW COMPLETED":
                return "Interview completed";
            case "INTERVIEW_CANCELLED":
            case "INTERVIEW CANCELLED":
                return "Interview cancelled";
            case "SELECTED":
                return "Candidate selected";
            case "REJECTED":
                return "Candidate rejected";
            default:
                return status;
        }
    }

    // Helper method for CSS styling class
    public String getStatusClass() {
        if (status == null) {
            return "applied";
        }
        switch (status.toUpperCase()) {
            case "APPLIED":
                return "applied";
            case "SHORTLISTED":
                return "shortlisted";
            case "INTERVIEW_SCHEDULED":
            case "INTERVIEW SCHEDULED":
            case "INTERVIEW_RESCHEDULED":
            case "INTERVIEW RESCHEDULED":
            case "INTERVIEW_COMPLETED":
            case "INTERVIEW COMPLETED":
                return "interview";
            case "INTERVIEW_CANCELLED":
            case "INTERVIEW CANCELLED":
                return "cancelled";
            case "SELECTED":
                return "selected";
            case "REJECTED":
                return "rejected";
            default:
                return "applied";
        }
    }
}
