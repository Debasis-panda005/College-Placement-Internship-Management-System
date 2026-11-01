package com.college.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "interview_rounds")
public class InterviewRound implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_RESCHEDULED = "RESCHEDULED";

    public static final String RESULT_PENDING = "PENDING";
    public static final String RESULT_PASSED = "PASSED";
    public static final String RESULT_FAILED = "FAILED";

    public static final String MODE_ONLINE = "ONLINE";
    public static final String MODE_OFFLINE = "OFFLINE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;
    private Integer roundNumber;
    private String roundName;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String mode;
    private String interviewerName;
    private String interviewerInfo;
    private String status;
    private String result;
    private String remarks;
    private String feedback;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterviewRound() {
        this.status = STATUS_SCHEDULED;
        this.result = RESULT_PENDING;
        this.mode = MODE_ONLINE;
    }

    public InterviewRound(Long applicationId, Integer roundNumber, String roundName,
                          LocalDate interviewDate, LocalTime interviewTime, String mode) {
        this.applicationId = applicationId;
        this.roundNumber = roundNumber;
        this.roundName = roundName;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.mode = mode != null ? mode.trim() : MODE_ONLINE;
        this.status = STATUS_SCHEDULED;
        this.result = RESULT_PENDING;
    }

    public InterviewRound(Long id, Long applicationId, Integer roundNumber, String roundName,
                          LocalDate interviewDate, LocalTime interviewTime, String mode,
                          String interviewerName, String interviewerInfo, String status,
                          String result, String remarks, String feedback) {
        this.id = id;
        this.applicationId = applicationId;
        this.roundNumber = roundNumber;
        this.roundName = roundName;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.mode = mode;
        this.interviewerName = interviewerName;
        this.interviewerInfo = interviewerInfo;
        this.status = status;
        this.result = result;
        this.remarks = remarks;
        this.feedback = feedback;
    }

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

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
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

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerInfo() {
        return interviewerInfo;
    }

    public void setInterviewerInfo(String interviewerInfo) {
        this.interviewerInfo = interviewerInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedInterviewDate() {
        if (interviewDate == null) {
            return "TBD";
        }
        return interviewDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getFormattedInterviewTime() {
        if (interviewTime == null) {
            return "TBD";
        }
        return interviewTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getStatusBadgeClass() {
        if (status == null) {
            return "badge-secondary";
        }
        switch (status.toUpperCase()) {
            case "COMPLETED":
                return "badge-completed";
            case "SCHEDULED":
                return "badge-scheduled";
            case "RESCHEDULED":
                return "badge-rescheduled";
            case "CANCELLED":
                return "badge-cancelled";
            default:
                return "badge-secondary";
        }
    }

    public String getResultBadgeClass() {
        if (result == null) {
            return "badge-pending";
        }
        switch (result.toUpperCase()) {
            case "PASSED":
                return "badge-passed";
            case "FAILED":
                return "badge-failed";
            case "PENDING":
            default:
                return "badge-pending";
        }
    }
}
