package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewReportDAO;
import com.college.backend.dao.InterviewRoundDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.service.InterviewPdfService;
import com.college.backend.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet({"/interview-report", "/interview-report/view", "/interview-report/download"})
public class InterviewPdfServlet extends HttpServlet {

    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private UserDAO userDAO = new UserDAO();
    private JobDAO jobDAO = new JobDAO();
    private InterviewRoundDAO roundDAO = new InterviewRoundDAO();
    private InterviewReportDAO reportDAO = new InterviewReportDAO();
    private InterviewPdfService pdfService = new InterviewPdfService();

    @Override
    public void init() {
        this.applicationDAO = new ApplicationDAO();
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.jobDAO = new JobDAO();
        this.roundDAO = new InterviewRoundDAO();
        this.reportDAO = new InterviewReportDAO();
        this.pdfService = new InterviewPdfService();
    }

    // Setters for test dependency injection
    public void setApplicationDAO(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setJobDAO(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    public void setRoundDAO(InterviewRoundDAO roundDAO) {
        this.roundDAO = roundDAO;
    }

    public void setReportDAO(InterviewReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public void setPdfService(InterviewPdfService pdfService) {
        this.pdfService = pdfService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Authentication check
        if (!AuthUtil.isAuthenticated(request)) {
            request.getSession(true).setAttribute("errorMessage", "You must log in to access interview reports.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isStaff = AuthUtil.isCoordinatorOrAdmin(request);
        Long sessionUserId = AuthUtil.getSessionUserId(request);

        // 2. Parse Application ID
        String appIdParam = request.getParameter("applicationId");
        String uuidParam = request.getParameter("uuid");

        Application application = null;
        InterviewReport report = null;

        if (uuidParam != null && !uuidParam.trim().isEmpty()) {
            report = reportDAO.getReportByUuid(uuidParam.trim());
            if (report != null) {
                application = applicationDAO.getApplicationById(report.getApplicationId());
            }
        }

        if (application == null && appIdParam != null && !appIdParam.trim().isEmpty()) {
            try {
                Long appId = Long.parseLong(appIdParam.trim());
                application = applicationDAO.getApplicationById(appId);
                if (application != null) {
                    report = reportDAO.getReportByApplicationId(appId);
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "Invalid application identifier format.");
                response.sendRedirect(request.getContextPath() + "/applications");
                return;
            }
        }

        if (application == null) {
            request.getSession().setAttribute("errorMessage", "Interview report not found. Application record does not exist.");
            response.sendRedirect(request.getContextPath() + "/applications");
            return;
        }

        // 3. Authorization check (IDOR Defense)
        Student currentStudent = null;
        if (!isStaff) {
            currentStudent = studentDAO.findByUserId(sessionUserId);
            if (currentStudent == null || !currentStudent.getId().equals(application.getStudentId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied: You are only authorized to access your own interview reports.");
                return;
            }
        }

        // 4. Decision check - report only generated after final selection or rejection
        String status = application.getStatus();
        if (!"SELECTED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            request.getSession().setAttribute("errorMessage",
                    "Interview report is not available yet. Final determination has not been recorded (Current status: "
                            + status + ").");
            String redirectUrl = isStaff ?
                    request.getContextPath() + "/interview-rounds?applicationId=" + application.getId() :
                    request.getContextPath() + "/applications?action=details&id=" + application.getId();
            response.sendRedirect(redirectUrl);
            return;
        }

        // 5. Gather related entities
        Student applicant = studentDAO.findById(application.getStudentId());
        User applicantUser = applicant != null ? userDAO.findById(applicant.getUserId()) : null;
        Job job = jobDAO.getJobById(application.getJobId());
        List<InterviewRound> rounds = roundDAO.getRoundsByApplicationId(application.getId());

        // Ensure report metadata is consistent
        if (report == null) {
            report = new InterviewReport();
            report.setApplicationId(application.getId());
            report.setReportUuid(UUID.randomUUID().toString());
            report.setFinalDecision(status.toUpperCase());
            report.setDecisionDate(java.time.LocalDateTime.now());
            report.setGeneratedBy(isStaff ? AuthUtil.getSessionUserName(request) : "Head - Placement Cell");

            Map<String, Integer> stats = roundDAO.getRoundStatistics(application.getId());
            report.setTotalRounds(stats.getOrDefault("TOTAL", rounds.size()));
            report.setCompletedRounds(stats.getOrDefault("COMPLETED", 0));
            report.setPassedRounds(stats.getOrDefault("PASSED", 0));
            report.setFailedRounds(stats.getOrDefault("FAILED", 0));
            report.setCancelledRounds(stats.getOrDefault("CANCELLED", 0));
            report.setCoordinatorRemarks("Candidate successfully processed through institutional evaluation.");
            report.setOverallFeedback("Candidate final status recorded as " + status.toUpperCase() + ".");

            reportDAO.createOrUpdateReport(report);
        }

        // 6. Generate PDF document bytes
        byte[] pdfBytes;
        try {
            pdfBytes = pdfService.generateInterviewReportPdf(report, application, applicant, applicantUser, job, rounds);
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Error generating PDF report: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/applications?action=details&id=" + application.getId());
            return;
        }

        // 7. Configure HTTP headers
        String servletPath = request.getServletPath();
        String actionParam = request.getParameter("action");
        boolean isDownload = "download".equalsIgnoreCase(actionParam) ||
                (servletPath != null && servletPath.contains("download"));

        String safeRoll = applicant != null && applicant.getRollNumber() != null ?
                applicant.getRollNumber().replaceAll("[^a-zA-Z0-9_-]", "") : "Applicant";
        String safeCompany = job != null && job.getCompany() != null ?
                job.getCompany().replaceAll("[^a-zA-Z0-9_-]", "") : "Company";
        String filename = "Interview_Report_" + safeRoll + "_" + safeCompany + ".pdf";

        response.setContentType("application/pdf");
        response.setContentLength(pdfBytes.length);
        response.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        String disposition = isDownload ? "attachment" : "inline";
        response.setHeader("Content-Disposition", disposition + "; filename=\"" + filename + "\"");

        try (OutputStream os = response.getOutputStream()) {
            os.write(pdfBytes);
            os.flush();
        }
    }
}
