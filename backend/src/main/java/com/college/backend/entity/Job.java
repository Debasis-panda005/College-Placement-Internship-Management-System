package com.college.backend.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String salary;
    private String duration;
    private String jobType; // "Full Time", "Part Time", "Internship"
    private String status;  // "Active", "Closed"
    private LocalDate deadline;
    private BigDecimal minCgpa;
    private String eligibleDepartments;

    public Job() {
    }

    public Job(String title, String company, String location, String description,
               String salary, String jobType, String status) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
    }

    public Job(String title, String company, String location, String description,
               String salary, String duration, String jobType, String status) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.duration = duration;
        this.jobType = jobType;
        this.status = status;
    }

    public Job(Long id, String title, String company, String location, String description,
               String salary, String jobType, String status) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
    }

    public Job(Long id, String title, String company, String location, String description,
               String salary, String duration, String jobType, String status) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.duration = duration;
        this.jobType = jobType;
        this.status = status;
    }

    public Job(Long id, String title, String company, String location, String description,
               String salary, String jobType, String status,
               LocalDate deadline, BigDecimal minCgpa, String eligibleDepartments) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
        this.deadline = deadline;
        this.minCgpa = minCgpa;
        this.eligibleDepartments = eligibleDepartments;
    }

    public Job(String title, String company, String location, String description,
               String salary, String jobType, String status,
               LocalDate deadline, BigDecimal minCgpa, String eligibleDepartments) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
        this.deadline = deadline;
        this.minCgpa = minCgpa;
        this.eligibleDepartments = eligibleDepartments;
    }

    public Job(Long id, String title, String company, String location, String description,
               String salary, String duration, String jobType, String status,
               LocalDate deadline, BigDecimal minCgpa, String eligibleDepartments) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.duration = duration;
        this.jobType = jobType;
        this.status = status;
        this.deadline = deadline;
        this.minCgpa = minCgpa;
        this.eligibleDepartments = eligibleDepartments;
    }

    public Job(String title, String company, String location, String description,
               String salary, String duration, String jobType, String status,
               LocalDate deadline, BigDecimal minCgpa, String eligibleDepartments) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.duration = duration;
        this.jobType = jobType;
        this.status = status;
        this.deadline = deadline;
        this.minCgpa = minCgpa;
        this.eligibleDepartments = eligibleDepartments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isInternship() {
        if (this.jobType == null) {
            return false;
        }
        String lower = this.jobType.trim().toLowerCase();
        return lower.contains("intern");
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
        this.minCgpa = minCgpa;
    }

    public String getEligibleDepartments() {
        return eligibleDepartments;
    }

    public void setEligibleDepartments(String eligibleDepartments) {
        this.eligibleDepartments = eligibleDepartments;
    }

    /**
     * Checks if the job is closed for applications.
     *
     * @return true if status is 'Closed' (case-insensitive), false otherwise
     */
    public boolean isClosed() {
        if (status == null) {
            return false;
        }
        String trimmed = status.trim();
        return "Closed".equalsIgnoreCase(trimmed) || "Inactive".equalsIgnoreCase(trimmed);
    }

    /**
     * Checks if the job is active and open for applications.
     *
     * @return true if status is 'Active' or not explicitly closed
     */
    public boolean isOpen() {
        return !isClosed();
    }

    /**
     * Checks whether the application deadline has passed as of the given date.
     *
     * @param currentDate the reference date (e.g. LocalDate.now())
     * @return true if deadline is specified and currentDate is after deadline
     */
    public boolean isDeadlinePassed(LocalDate currentDate) {
        if (deadline == null || currentDate == null) {
            return false;
        }
        return currentDate.isAfter(deadline);
    }

    /**
     * Checks whether a student's CGPA satisfies the minimum CGPA requirement.
     *
     * @param studentCgpa student's cumulative grade point average
     * @return true if no minimum CGPA is required or student meets/exceeds requirement
     */
    public boolean isCgpaEligible(BigDecimal studentCgpa) {
        if (minCgpa == null) {
            return true;
        }
        if (studentCgpa == null) {
            return false;
        }
        return studentCgpa.compareTo(minCgpa) >= 0;
    }

    /**
     * Checks whether a student's department is eligible for this job.
     * Supports comma- or semicolon-separated department lists (e.g., "Computer Science, IT").
     * If no eligible departments are specified or set to "ALL", all departments are eligible.
     *
     * @param studentDepartment the student's department
     * @return true if eligible
     */
    public boolean isDepartmentEligible(String studentDepartment) {
        if (eligibleDepartments == null || eligibleDepartments.trim().isEmpty() ||
                "ALL".equalsIgnoreCase(eligibleDepartments.trim())) {
            return true;
        }
        if (studentDepartment == null || studentDepartment.trim().isEmpty()) {
            return false;
        }
        String trimmedStudentDept = studentDepartment.trim();
        String[] allowed = eligibleDepartments.split("[,;]+");
        for (String d : allowed) {
            if (d.trim().equalsIgnoreCase(trimmedStudentDept)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", salary='" + salary + '\'' +
                ", duration='" + duration + '\'' +
                ", jobType='" + jobType + '\'' +
                ", status='" + status + '\'' +
                ", deadline=" + deadline +
                ", minCgpa=" + minCgpa +
                ", eligibleDepartments='" + eligibleDepartments + '\'' +
                '}';
    }
}
