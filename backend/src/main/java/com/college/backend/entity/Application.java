package com.college.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long jobId;
    private LocalDate applicationDate;
    private String status;
    private String applicantType = APPLICANT_INTERNAL;
    private Long externalStudentId;

    public static final String APPLICANT_INTERNAL = "INTERNAL";
    public static final String APPLICANT_EXTERNAL = "EXTERNAL";
    public static final String APPLICANT_TYPE_INTERNAL = APPLICANT_INTERNAL;
    public static final String APPLICANT_TYPE_EXTERNAL = APPLICANT_EXTERNAL;


    public Application() {
    }

    public Application(Long studentId, Long jobId,
                       LocalDate applicationDate, String status) {
        this.studentId = studentId;
        this.jobId = jobId;
        this.applicationDate = applicationDate;
        this.status = status;
        this.applicantType = APPLICANT_INTERNAL;
    }

    public Application(Long studentId, Long jobId,
                       LocalDate applicationDate, String status,
                       String applicantType, Long externalStudentId) {
        this.studentId = studentId;
        this.jobId = jobId;
        this.applicationDate = applicationDate;
        this.status = status;
        this.externalStudentId = externalStudentId;
        if (externalStudentId != null) {
            this.applicantType = APPLICANT_EXTERNAL;
        } else {
            this.applicantType = applicantType != null ? applicantType : APPLICANT_INTERNAL;
        }
    }

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

    public String getApplicantType() {
        if (applicantType != null && !applicantType.trim().isEmpty()) {
            return applicantType.trim().toUpperCase();
        }
        return externalStudentId != null ? APPLICANT_EXTERNAL : APPLICANT_INTERNAL;
    }

    public void setApplicantType(String applicantType) {
        if (applicantType == null || applicantType.trim().isEmpty()) {
            this.applicantType = externalStudentId != null ? APPLICANT_EXTERNAL : APPLICANT_INTERNAL;
        } else {
            this.applicantType = applicantType.trim().toUpperCase();
        }
    }

    public Long getExternalStudentId() {
        return externalStudentId;
    }

    public void setExternalStudentId(Long externalStudentId) {
        this.externalStudentId = externalStudentId;
        if (externalStudentId != null && (this.applicantType == null || APPLICANT_INTERNAL.equalsIgnoreCase(this.applicantType))) {
            this.applicantType = APPLICANT_EXTERNAL;
        }
    }

    public boolean isExternal() {
        return APPLICANT_EXTERNAL.equalsIgnoreCase(getApplicantType()) || externalStudentId != null;
    }
}
