package com.college.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_reports")
public class InterviewReport implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DECISION_SELECTED = "SELECTED";
    public static final String DECISION_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;
    private String reportUuid;
    private String finalDecision;
    private LocalDateTime decisionDate;
    private Integer totalRounds;
    private Integer completedRounds;
    private Integer passedRounds;
    private Integer failedRounds;
    private Integer cancelledRounds;
    private String coordinatorRemarks;
    private String overallFeedback;
    private String generatedBy;
    private LocalDateTime generatedAt;

    public InterviewReport() {
        this.decisionDate = LocalDateTime.now();
        this.generatedAt = LocalDateTime.now();
        this.totalRounds = 0;
        this.completedRounds = 0;
        this.passedRounds = 0;
        this.failedRounds = 0;
        this.cancelledRounds = 0;
    }

    public InterviewReport(Long applicationId, String reportUuid, String finalDecision,
                           Integer totalRounds, Integer completedRounds, Integer passedRounds,
                           Integer failedRounds, Integer cancelledRounds,
                           String coordinatorRemarks, String overallFeedback, String generatedBy) {
        this.applicationId = applicationId;
        this.reportUuid = reportUuid;
        this.finalDecision = finalDecision;
        this.decisionDate = LocalDateTime.now();
        this.totalRounds = totalRounds != null ? totalRounds : 0;
        this.completedRounds = completedRounds != null ? completedRounds : 0;
        this.passedRounds = passedRounds != null ? passedRounds : 0;
        this.failedRounds = failedRounds != null ? failedRounds : 0;
        this.cancelledRounds = cancelledRounds != null ? cancelledRounds : 0;
        this.coordinatorRemarks = coordinatorRemarks;
        this.overallFeedback = overallFeedback;
        this.generatedBy = generatedBy;
        this.generatedAt = LocalDateTime.now();
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

    public String getReportUuid() {
        return reportUuid;
    }

    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }

    public Integer getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }

    public Integer getCompletedRounds() {
        return completedRounds;
    }

    public void setCompletedRounds(Integer completedRounds) {
        this.completedRounds = completedRounds;
    }

    public Integer getPassedRounds() {
        return passedRounds;
    }

    public void setPassedRounds(Integer passedRounds) {
        this.passedRounds = passedRounds;
    }

    public Integer getFailedRounds() {
        return failedRounds;
    }

    public void setFailedRounds(Integer failedRounds) {
        this.failedRounds = failedRounds;
    }

    public Integer getCancelledRounds() {
        return cancelledRounds;
    }

    public void setCancelledRounds(Integer cancelledRounds) {
        this.cancelledRounds = cancelledRounds;
    }

    public String getCoordinatorRemarks() {
        return coordinatorRemarks;
    }

    public void setCoordinatorRemarks(String coordinatorRemarks) {
        this.coordinatorRemarks = coordinatorRemarks;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
