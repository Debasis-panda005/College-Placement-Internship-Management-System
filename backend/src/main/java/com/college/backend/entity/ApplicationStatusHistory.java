package com.college.backend.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationStatusHistory {

    private Long id;
    private Long applicationId;
    private String status;
    private LocalDateTime changedAt;
    private String toStatus;
    private String fromStatus;
    private String comments;
    private String changedBy;

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

    public String getToStatus() {
        return toStatus != null ? toStatus : getDisplayStatus();
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getComments() {
        return comments != null ? comments : getStatusDescription();
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
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

    // Helper method for timeline title (e.g. Application Submitted, Final Selection)
    public String getTimelineEventTitle() {
        if (status == null) {
            return "Status Update";
        }
        switch (status.toUpperCase()) {
            case "APPLIED":
                return "Application Submitted";
            case "SHORTLISTED":
                return "Application Shortlisted";
            case "INTERVIEW_SCHEDULED":
            case "INTERVIEW SCHEDULED":
                return "Interview Scheduled";
            case "INTERVIEW_RESCHEDULED":
            case "INTERVIEW RESCHEDULED":
                return "Interview Rescheduled";
            case "INTERVIEW_COMPLETED":
            case "INTERVIEW COMPLETED":
                return "Interview Completed";
            case "INTERVIEW_CANCELLED":
            case "INTERVIEW CANCELLED":
                return "Interview Cancelled";
            case "SELECTED":
                return "Final Selection";
            case "REJECTED":
                return "Final Rejection";
            default:
                return getDisplayStatus();
        }
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

    // Helper method for timeline event icons
    public String getIconClass() {
        if (status == null) {
            return "fa-solid fa-circle-info";
        }
        switch (status.toUpperCase()) {
            case "APPLIED":
                return "fa-solid fa-file-signature";
            case "SHORTLISTED":
                return "fa-solid fa-star";
            case "INTERVIEW_SCHEDULED":
            case "INTERVIEW SCHEDULED":
                return "fa-solid fa-calendar-check";
            case "INTERVIEW_RESCHEDULED":
            case "INTERVIEW RESCHEDULED":
                return "fa-solid fa-clock-rotate-left";
            case "INTERVIEW_COMPLETED":
            case "INTERVIEW COMPLETED":
                return "fa-solid fa-clipboard-check";
            case "INTERVIEW_CANCELLED":
            case "INTERVIEW CANCELLED":
                return "fa-solid fa-calendar-xmark";
            case "SELECTED":
                return "fa-solid fa-trophy";
            case "REJECTED":
                return "fa-solid fa-circle-xmark";
            default:
                return "fa-solid fa-circle-dot";
        }
    }

    // Helper method for timeline icon badge background color
    public String getBadgeBgColor() {
        if (status == null) {
            return "#1B365D";
        }
        switch (status.toUpperCase()) {
            case "APPLIED":
                return "#1B365D";
            case "SHORTLISTED":
                return "#F2A900";
            case "INTERVIEW_SCHEDULED":
            case "INTERVIEW SCHEDULED":
                return "#0284C7";
            case "INTERVIEW_RESCHEDULED":
            case "INTERVIEW RESCHEDULED":
                return "#E87722";
            case "INTERVIEW_COMPLETED":
            case "INTERVIEW COMPLETED":
                return "#059669";
            case "INTERVIEW_CANCELLED":
            case "INTERVIEW CANCELLED":
                return "#EF4444";
            case "SELECTED":
                return "#10B981";
            case "REJECTED":
                return "#DC2626";
            default:
                return "#1B365D";
        }
    }
}
