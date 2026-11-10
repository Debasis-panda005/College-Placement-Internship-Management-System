package com.college.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for external student approval/rejection lifecycle events.
 */
public class ExternalStudentAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_STATUS_CHANGE = "STATUS_CHANGE";

    private Long id;
    private Long externalStudentId;
    private String action;
    private Long performedBy;
    private LocalDateTime actionTimestamp;
    private String previousStatus;
    private String newStatus;
    private String remarks;

    // Joined fields
    private String performedByName;
    private String studentName;

    public ExternalStudentAuditLog() {
    }

    public ExternalStudentAuditLog(Long externalStudentId, String action, Long performedBy,
                                   String previousStatus, String newStatus, String remarks) {
        this.externalStudentId = externalStudentId;
        this.action = action;
        this.performedBy = performedBy;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.actionTimestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExternalStudentId() {
        return externalStudentId;
    }

    public void setExternalStudentId(Long externalStudentId) {
        this.externalStudentId = externalStudentId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Long performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
