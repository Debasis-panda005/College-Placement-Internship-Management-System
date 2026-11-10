package com.college.backend.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an external student applicant from an external college/university.
 * Linked to a User account with role 'STUDENT'.
 * Registration initially has verification status 'PENDING'.
 */
public class ExternalStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    public static final BigDecimal MIN_CGPA = BigDecimal.ZERO;
    public static final BigDecimal MAX_CGPA = new BigDecimal("10.00");
    public static final int MIN_GRADUATION_YEAR = 2000;
    public static final int MAX_GRADUATION_YEAR = 2100;
    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 12;

    private Long id;
    private Long userId;
    private String collegeName;
    private String collegeLocation;
    private String degree;
    private String department;
    private Integer currentSemester;
    private BigDecimal cgpa;
    private Integer graduationYear;
    private String phone;
    private String resumeUrl;
    private String verificationStatus = STATUS_PENDING;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long approvedBy;
    private LocalDateTime approvalDate;
    private String adminNotes;

    // Joined fields for display convenience
    private String applicantName;
    private String applicantEmail;
    private String approvedByName;

    public ExternalStudent() {
    }

    public ExternalStudent(Long userId, String collegeName, String collegeLocation, String degree,
                           String department, Integer currentSemester, BigDecimal cgpa,
                           Integer graduationYear, String phone, String resumeUrl) {
        this.userId = userId;
        setCollegeName(collegeName);
        setCollegeLocation(collegeLocation);
        setDegree(degree);
        setDepartment(department);
        setCurrentSemester(currentSemester);
        setCgpa(cgpa);
        setGraduationYear(graduationYear);
        this.phone = phone != null ? phone.trim() : null;
        this.resumeUrl = resumeUrl != null ? resumeUrl.trim() : null;
        this.verificationStatus = STATUS_PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public ExternalStudent(Long id, Long userId, String collegeName, String collegeLocation, String degree,
                           String department, Integer currentSemester, BigDecimal cgpa,
                           Integer graduationYear, String phone, String resumeUrl,
                           String verificationStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        setCollegeName(collegeName);
        setCollegeLocation(collegeLocation);
        setDegree(degree);
        setDepartment(department);
        setCurrentSemester(currentSemester);
        setCgpa(cgpa);
        setGraduationYear(graduationYear);
        this.phone = phone != null ? phone.trim() : null;
        this.resumeUrl = resumeUrl != null ? resumeUrl.trim() : null;
        this.verificationStatus = verificationStatus != null ? verificationStatus.trim().toUpperCase() : STATUS_PENDING;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Validates whether a given CGPA is within the allowed 0.00 to 10.00 scale.
     */
    public static boolean isValidCgpa(BigDecimal cgpa) {
        if (cgpa == null) {
            return false;
        }
        return cgpa.compareTo(MIN_CGPA) >= 0 && cgpa.compareTo(MAX_CGPA) <= 0;
    }

    /**
     * Validates whether a graduation year is within the reasonable 2000 to 2100 range.
     */
    public static boolean isValidGraduationYear(Integer year) {
        if (year == null) {
            return false;
        }
        return year >= MIN_GRADUATION_YEAR && year <= MAX_GRADUATION_YEAR;
    }

    /**
     * Validates whether current semester is within 1 to 12.
     */
    public static boolean isValidSemester(Integer semester) {
        if (semester == null) {
            return false;
        }
        return semester >= MIN_SEMESTER && semester <= MAX_SEMESTER;
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

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        if (collegeName == null || collegeName.trim().isEmpty()) {
            throw new IllegalArgumentException("College/University name cannot be empty.");
        }
        this.collegeName = collegeName.trim();
    }

    public String getCollegeLocation() {
        return collegeLocation;
    }

    public void setCollegeLocation(String collegeLocation) {
        if (collegeLocation == null || collegeLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("College location cannot be empty.");
        }
        this.collegeLocation = collegeLocation.trim();
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        if (degree == null || degree.trim().isEmpty()) {
            throw new IllegalArgumentException("Degree/Course cannot be empty.");
        }
        this.degree = degree.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
        this.department = department.trim();
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Integer currentSemester) {
        if (!isValidSemester(currentSemester)) {
            throw new IllegalArgumentException("Semester must be between 1 and 12.");
        }
        this.currentSemester = currentSemester;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        if (!isValidCgpa(cgpa)) {
            throw new IllegalArgumentException("CGPA must be between 0.00 and 10.00.");
        }
        this.cgpa = cgpa;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        if (!isValidGraduationYear(graduationYear)) {
            throw new IllegalArgumentException("Graduation year must be between 2000 and 2100.");
        }
        this.graduationYear = graduationYear;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl != null ? resumeUrl.trim() : null;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus != null ? verificationStatus.trim().toUpperCase() : STATUS_PENDING;
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

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    // Convenience alias getters for JSP EL and service consistency
    public String getName() {
        return applicantName;
    }

    public void setName(String name) {
        this.applicantName = name;
    }

    public String getFullName() {
        return applicantName;
    }

    public void setFullName(String fullName) {
        this.applicantName = fullName;
    }

    public String getEmail() {
        return applicantEmail;
    }

    public void setEmail(String email) {
        this.applicantEmail = email;
    }

    public String getMobileNumber() {
        return phone;
    }

    public void setMobileNumber(String mobileNumber) {
        this.phone = mobileNumber;
    }

    public String getStatus() {
        return verificationStatus;
    }

    public void setStatus(String status) {
        setVerificationStatus(status);
    }

    public String getResumePath() {
        return resumeUrl;
    }

    public void setResumePath(String resumePath) {
        this.resumeUrl = resumePath;
    }

    @Override
    public String toString() {
        return "ExternalStudent{" +
                "id=" + id +
                ", userId=" + userId +
                ", collegeName='" + collegeName + '\'' +
                ", collegeLocation='" + collegeLocation + '\'' +
                ", degree='" + degree + '\'' +
                ", department='" + department + '\'' +
                ", currentSemester=" + currentSemester +
                ", cgpa=" + cgpa +
                ", graduationYear=" + graduationYear +
                ", phone='" + phone + '\'' +
                ", resumeUrl='" + resumeUrl + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
