package com.college.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing an institutional Placement Coordinator profile.
 * Bound 1-to-1 to a User account with role COORDINATOR.
 * Tracks institutional employee details and administrative approval lifecycle.
 */
public class Coordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private Long id;
    private Long userId;
    private String employeeId;
    private String department;
    private String designation;
    private String phone;
    private String status = STATUS_PENDING_APPROVAL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long approvedBy;
    private LocalDateTime approvalDate;
    private String adminNotes;

    // Display fields joined from users table
    private String coordinatorName;
    private String institutionalEmail;
    private String approvedByName;

    public Coordinator() {
    }

    public Coordinator(Long userId, String employeeId, String department, String designation, String phone) {
        this.userId = userId;
        this.employeeId = employeeId != null ? employeeId.trim() : null;
        this.department = department != null ? department.trim() : null;
        this.designation = designation != null ? designation.trim() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.status = STATUS_PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
    }

    public Coordinator(Long id, Long userId, String employeeId, String department, String designation,
                       String phone, String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                       Long approvedBy, LocalDateTime approvalDate, String adminNotes) {
        this.id = id;
        this.userId = userId;
        this.employeeId = employeeId != null ? employeeId.trim() : null;
        this.department = department != null ? department.trim() : null;
        this.designation = designation != null ? designation.trim() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.status = status != null ? status.trim().toUpperCase() : STATUS_PENDING_APPROVAL;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate;
        this.adminNotes = adminNotes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId != null ? employeeId.trim() : null;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : null;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation != null ? designation.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim().toUpperCase() : STATUS_PENDING_APPROVAL;
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

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getCoordinatorName() {
        return coordinatorName;
    }

    public void setCoordinatorName(String coordinatorName) {
        this.coordinatorName = coordinatorName;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equalsIgnoreCase(this.status);
    }

    public boolean isPendingApproval() {
        return STATUS_PENDING_APPROVAL.equalsIgnoreCase(this.status);
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "Coordinator{" +
                "id=" + id +
                ", userId=" + userId +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
