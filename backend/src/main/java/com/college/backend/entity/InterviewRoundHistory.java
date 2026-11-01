package com.college.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_round_history")
public class InterviewRoundHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roundId;
    private Long applicationId;
    private String action;
    private LocalDateTime actionDate;
    private String previousStatus;
    private String newStatus;
    private String previousResult;
    private String newResult;
    private String remarks;
    private String performedBy;

    public InterviewRoundHistory() {
        this.actionDate = LocalDateTime.now();
    }

    public InterviewRoundHistory(Long roundId, Long applicationId, String action,
                                 String previousStatus, String newStatus,
                                 String previousResult, String newResult,
                                 String remarks, String performedBy) {
        this.roundId = roundId;
        this.applicationId = applicationId;
        this.action = action;
        this.actionDate = LocalDateTime.now();
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.previousResult = previousResult;
        this.newResult = newResult;
        this.remarks = remarks;
        this.performedBy = performedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getPreviousResult() {
        return previousResult;
    }

    public void setPreviousResult(String previousResult) {
        this.previousResult = previousResult;
    }

    public String getNewResult() {
        return newResult;
    }

    public void setNewResult(String newResult) {
        this.newResult = newResult;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getFormattedActionDate() {
        if (actionDate == null) {
            return "";
        }
        return actionDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public boolean isRescheduled() {
        return "RESCHEDULED".equalsIgnoreCase(action);
    }
}
