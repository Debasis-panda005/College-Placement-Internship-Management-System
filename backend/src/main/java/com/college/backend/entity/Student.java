package com.college.backend.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity representing a student profile linked to a User account with role 'STUDENT'.
 * Contains academic, contact, and placement profile details.
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final BigDecimal MIN_CGPA = BigDecimal.ZERO;
    public static final BigDecimal MAX_CGPA = new BigDecimal("10.00");
    public static final int MIN_GRADUATION_YEAR = 2000;
    public static final int MAX_GRADUATION_YEAR = 2100;

    private Long id;
    private Long userId;
    private String rollNumber;
    private String department;
    private BigDecimal cgpa;
    private Integer graduationYear;
    private String phone;
    private String skills;
    private String resumeUrl;

    public Student() {
    }

    public Student(Long userId, String rollNumber, String department, BigDecimal cgpa,
                   Integer graduationYear, String phone, String skills, String resumeUrl) {
        this.userId = userId;
        setRollNumber(rollNumber);
        setDepartment(department);
        setCgpa(cgpa);
        setGraduationYear(graduationYear);
        this.phone = phone != null ? phone.trim() : null;
        this.skills = skills != null ? skills.trim() : null;
        this.resumeUrl = resumeUrl != null ? resumeUrl.trim() : null;
    }

    public Student(Long id, Long userId, String rollNumber, String department, BigDecimal cgpa,
                   Integer graduationYear, String phone, String skills, String resumeUrl) {
        this.id = id;
        this.userId = userId;
        setRollNumber(rollNumber);
        setDepartment(department);
        setCgpa(cgpa);
        setGraduationYear(graduationYear);
        this.phone = phone != null ? phone.trim() : null;
        this.skills = skills != null ? skills.trim() : null;
        this.resumeUrl = resumeUrl != null ? resumeUrl.trim() : null;
    }

    /**
     * Validates whether a given CGPA is within the allowed 0.00 to 10.00 scale.
     *
     * @param cgpa the CGPA to validate
     * @return true if valid or null, false if out of range
     */
    public static boolean isValidCgpa(BigDecimal cgpa) {
        if (cgpa == null) {
            return false;
        }
        return cgpa.compareTo(MIN_CGPA) >= 0 && cgpa.compareTo(MAX_CGPA) <= 0;
    }

    /**
     * Validates whether a given graduation year is within valid college bounds.
     *
     * @param year the graduation year to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGraduationYear(Integer year) {
        if (year == null) {
            return false;
        }
        return year >= MIN_GRADUATION_YEAR && year <= MAX_GRADUATION_YEAR;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber != null ? rollNumber.trim().toUpperCase() : null;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : null;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        if (cgpa != null && !isValidCgpa(cgpa)) {
            throw new IllegalArgumentException("CGPA must be between " + MIN_CGPA + " and " + MAX_CGPA + ". Provided: " + cgpa);
        }
        this.cgpa = cgpa;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        if (graduationYear != null && !isValidGraduationYear(graduationYear)) {
            throw new IllegalArgumentException("Graduation year must be between "
                    + MIN_GRADUATION_YEAR + " and " + MAX_GRADUATION_YEAR + ". Provided: " + graduationYear);
        }
        this.graduationYear = graduationYear;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills != null ? skills.trim() : null;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl != null ? resumeUrl.trim() : null;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", userId=" + userId +
                ", rollNumber='" + rollNumber + '\'' +
                ", department='" + department + '\'' +
                ", cgpa=" + cgpa +
                ", graduationYear=" + graduationYear +
                ", phone='" + phone + '\'' +
                ", skills='" + skills + '\'' +
                ", resumeUrl='" + resumeUrl + '\'' +
                '}';
    }
}
