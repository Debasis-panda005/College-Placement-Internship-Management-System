package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ApplicationStatusHistoryDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.InterviewReportDAO;
import com.college.backend.dao.InterviewRoundDAO;
import com.college.backend.dao.InterviewRoundHistoryDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.InterviewRoundHistory;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.util.AuthUtil;
import com.college.backend.util.CsrfUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet({"/interview-rounds", "/interview-rounds/*"})
public class InterviewRoundServlet extends HttpServlet {

    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private UserDAO userDAO = new UserDAO();
    private JobDAO jobDAO = new JobDAO();
    private InterviewDAO interviewDAO = new InterviewDAO();
    private InterviewRoundDAO roundDAO = new InterviewRoundDAO();
    private InterviewRoundHistoryDAO roundHistoryDAO = new InterviewRoundHistoryDAO();
    private InterviewReportDAO reportDAO = new InterviewReportDAO();
    private ApplicationStatusHistoryDAO statusHistoryDAO = new ApplicationStatusHistoryDAO();

    @Override
    public void init() {
        this.applicationDAO = new ApplicationDAO();
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.jobDAO = new JobDAO();
        this.interviewDAO = new InterviewDAO();
        this.roundDAO = new InterviewRoundDAO();
        this.roundHistoryDAO = new InterviewRoundHistoryDAO();
        this.reportDAO = new InterviewReportDAO();
        this.statusHistoryDAO = new ApplicationStatusHistoryDAO();
    }

    // Setters for test dependency injection
    public void setApplicationDAO(ApplicationDAO applicationDAO) { this.applicationDAO = applicationDAO; }
    public void setStudentDAO(StudentDAO studentDAO) { this.studentDAO = studentDAO; }
    public void setUserDAO(UserDAO userDAO) { this.userDAO = userDAO; }
    public void setJobDAO(JobDAO jobDAO) { this.jobDAO = jobDAO; }
    public void setInterviewDAO(InterviewDAO interviewDAO) { this.interviewDAO = interviewDAO; }
    public void setRoundDAO(InterviewRoundDAO roundDAO) { this.roundDAO = roundDAO; }
    public void setRoundHistoryDAO(InterviewRoundHistoryDAO roundHistoryDAO) { this.roundHistoryDAO = roundHistoryDAO; }
    public void setReportDAO(InterviewReportDAO reportDAO) { this.reportDAO = reportDAO; }
    public void setStatusHistoryDAO(ApplicationStatusHistoryDAO statusHistoryDAO) { this.statusHistoryDAO = statusHistoryDAO; }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CsrfUtil.getOrCreateToken(request.getSession());

        // 1. Authentication check
        if (!AuthUtil.isAuthenticated(request)) {
            HttpSession newSession = request.getSession(true);
            CsrfUtil.getOrCreateToken(newSession);
            newSession.setAttribute("errorMessage", "You must log in to view interview rounds.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isStaff = AuthUtil.isCoordinatorOrAdmin(request);
        Long sessionUserId = AuthUtil.getSessionUserId(request);

        String appIdParam = request.getParameter("applicationId");
        if (appIdParam == null || appIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + (isStaff ? "/applications" : "/applications"));
            return;
        }

        Long applicationId;
        try {
            applicationId = Long.parseLong(appIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Invalid application identifier format.");
            response.sendRedirect(request.getContextPath() + "/applications");
            return;
        }

        Application application = applicationDAO.getApplicationById(applicationId);
        if (application == null) {
            request.getSession().setAttribute("errorMessage", "Application not found with ID: " + applicationId);
            response.sendRedirect(request.getContextPath() + "/applications");
            return;
        }

        // 2. Authorization check (IDOR Defense)
        Student currentStudent = null;
        if (!isStaff) {
            currentStudent = studentDAO.findByUserId(sessionUserId);
            if (currentStudent == null || !currentStudent.getId().equals(application.getStudentId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied: You are not authorized to view this application's interview rounds.");
                return;
            }
        }

        // Retrieve related data
        Student applicant = studentDAO.findById(application.getStudentId());
        User applicantUser = applicant != null ? userDAO.findById(applicant.getUserId()) : null;
        Job job = jobDAO.getJobById(application.getJobId());
        List<InterviewRound> rounds = roundDAO.getRoundsByApplicationId(applicationId);
        Map<String, Integer> stats = roundDAO.getRoundStatistics(applicationId);
        InterviewReport report = reportDAO.getReportByApplicationId(applicationId);

        int nextRoundNumber = roundDAO.getNextRoundNumber(applicationId);
        boolean hasActiveRound = roundDAO.hasActiveOrPendingRound(applicationId);
        boolean hasFailedRound = roundDAO.hasAnyFailedRound(applicationId);
        boolean allCompletedAndPassed = roundDAO.isAllRoundsCompletedAndPassed(applicationId);

        // Flash messages
        HttpSession session = request.getSession(false);
        if (session != null) {
            String err = (String) session.getAttribute("errorMessage");
            if (err != null) {
                request.setAttribute("errorMessage", err);
                session.removeAttribute("errorMessage");
            }
            String succ = (String) session.getAttribute("successMessage");
            if (succ != null) {
                request.setAttribute("successMessage", succ);
                session.removeAttribute("successMessage");
            }
        }

        request.setAttribute("application", application);
        request.setAttribute("applicant", applicant);
        request.setAttribute("applicantUser", applicantUser);
        request.setAttribute("job", job);
        request.setAttribute("rounds", rounds);
        request.setAttribute("stats", stats);
        request.setAttribute("report", report);
        request.setAttribute("nextRoundNumber", nextRoundNumber);
        request.setAttribute("hasActiveRound", hasActiveRound);
        request.setAttribute("hasFailedRound", hasFailedRound);
        request.setAttribute("allCompletedAndPassed", allCompletedAndPassed);
        request.setAttribute("isStaff", isStaff);
        request.setAttribute("today", LocalDate.now().toString());

        request.getRequestDispatcher("/WEB-INF/jsp/interview-rounds.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. CSRF Protection
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // 2. Authentication and Role authorization (Coordinator or Admin only for mutating interview rounds)
        if (!AuthUtil.isAuthenticated(request) || !AuthUtil.isCoordinatorOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: Only coordinators and administrators can manage interview rounds.");
            return;
        }

        String action = request.getParameter("action");
        String appIdParam = request.getParameter("applicationId");

        if (appIdParam == null || appIdParam.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Application ID is required.");
            response.sendRedirect(request.getContextPath() + "/applications");
            return;
        }

        Long applicationId;
        try {
            applicationId = Long.parseLong(appIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Invalid Application ID format.");
            response.sendRedirect(request.getContextPath() + "/applications");
            return;
        }

        Application application = applicationDAO.getApplicationById(applicationId);
        if (application == null) {
            request.getSession().setAttribute("errorMessage", "Application not found with ID: " + applicationId);
            response.sendRedirect(request.getContextPath() + "/applications");
            return;
        }

        String coordinatorName = AuthUtil.getSessionUserName(request);

        // ----------------------------------------------------
        // ACTION: ADD NEW ROUND
        // ----------------------------------------------------
        if ("addRound".equals(action)) {
            handleAddRound(request, response, application, coordinatorName);
        }
        // ----------------------------------------------------
        // ACTION: RESCHEDULE ROUND
        // ----------------------------------------------------
        else if ("rescheduleRound".equals(action)) {
            handleRescheduleRound(request, response, application, coordinatorName);
        }
        // ----------------------------------------------------
        // ACTION: COMPLETE ROUND & RECORD RESULT
        // ----------------------------------------------------
        else if ("completeRound".equals(action)) {
            handleCompleteRound(request, response, application, coordinatorName);
        }
        // ----------------------------------------------------
        // ACTION: CANCEL ROUND
        // ----------------------------------------------------
        else if ("cancelRound".equals(action)) {
            handleCancelRound(request, response, application, coordinatorName);
        }
        // ----------------------------------------------------
        // ACTION: RECORD FINAL SELECTION / REJECTION DECISION
        // ----------------------------------------------------
        else if ("recordFinalDecision".equals(action)) {
            handleRecordFinalDecision(request, response, application, coordinatorName);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown interview round action: " + action);
        }
    }

    private void handleAddRound(HttpServletRequest request, HttpServletResponse response,
                                Application application, String coordinatorName) throws IOException {

        Long appId = application.getId();

        // Check application state
        if ("SELECTED".equalsIgnoreCase(application.getStatus()) || "REJECTED".equalsIgnoreCase(application.getStatus())) {
            request.getSession().setAttribute("errorMessage",
                    "Cannot schedule interview rounds: Final determination is already " + application.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        if (roundDAO.hasAnyFailedRound(appId)) {
            request.getSession().setAttribute("errorMessage",
                    "Cannot schedule further rounds: Candidate has FAILED a previous interview round.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        if (roundDAO.hasActiveOrPendingRound(appId)) {
            request.getSession().setAttribute("errorMessage",
                    "An interview round is already scheduled. Please complete or cancel it before scheduling the next round.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        String roundName = request.getParameter("roundName");
        String customRoundName = request.getParameter("customRoundName");
        if ("OTHER".equalsIgnoreCase(roundName) && customRoundName != null && !customRoundName.trim().isEmpty()) {
            roundName = customRoundName.trim();
        }

        String roundNumParam = request.getParameter("roundNumber");
        String dateParam = request.getParameter("interviewDate");
        String timeParam = request.getParameter("interviewTime");
        String mode = request.getParameter("mode");
        String interviewerName = request.getParameter("interviewerName");
        String interviewerInfo = request.getParameter("interviewerInfo");

        if (roundName == null || roundName.trim().isEmpty() ||
                dateParam == null || dateParam.trim().isEmpty() ||
                timeParam == null || timeParam.trim().isEmpty() ||
                mode == null || mode.trim().isEmpty()) {

            request.getSession().setAttribute("errorMessage", "All required fields must be provided.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        int roundNumber;
        try {
            roundNumber = roundNumParam != null && !roundNumParam.trim().isEmpty() ?
                    Integer.parseInt(roundNumParam.trim()) : roundDAO.getNextRoundNumber(appId);
        } catch (NumberFormatException e) {
            roundNumber = roundDAO.getNextRoundNumber(appId);
        }

        // Enforce sequence: previous rounds must be passed
        if (roundNumber > 1 && !roundDAO.areAllPreviousRoundsPassed(appId, roundNumber)) {
            request.getSession().setAttribute("errorMessage",
                    "Cannot schedule Round " + roundNumber + ": Previous rounds must be completed and PASSED first.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        LocalDate interviewDate;
        LocalTime interviewTime;
        try {
            interviewDate = LocalDate.parse(dateParam.trim());
            interviewTime = LocalTime.parse(timeParam.trim());
        } catch (DateTimeParseException e) {
            request.getSession().setAttribute("errorMessage", "Invalid interview date or time format.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        if (interviewDate.isBefore(LocalDate.now())) {
            request.getSession().setAttribute("errorMessage", "Interview date cannot be in the past.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        InterviewRound round = new InterviewRound();
        round.setApplicationId(appId);
        round.setRoundNumber(roundNumber);
        round.setRoundName(roundName.trim());
        round.setInterviewDate(interviewDate);
        round.setInterviewTime(interviewTime);
        round.setMode(mode.trim().toUpperCase());
        round.setInterviewerName(interviewerName != null ? interviewerName.trim() : null);
        round.setInterviewerInfo(interviewerInfo != null ? interviewerInfo.trim() : null);
        round.setStatus(InterviewRound.STATUS_SCHEDULED);
        round.setResult(InterviewRound.RESULT_PENDING);

        boolean created = roundDAO.createRound(round, coordinatorName);
        if (created) {
            // Also maintain synchronicity with legacy interview schedule if Round 1
            if (roundNumber == 1) {
                try {
                    Interview legacy = new Interview(appId, interviewDate, interviewTime, mode.trim().toUpperCase(), "SCHEDULED");
                    interviewDAO.createInterview(legacy);
                } catch (Exception ignored) {}
            }
            request.getSession().setAttribute("successMessage",
                    "Round " + roundNumber + " (" + round.getRoundName() + ") scheduled successfully!");
        } else {
            request.getSession().setAttribute("errorMessage", "Failed to schedule interview round.");
        }

        response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
    }

    private void handleRescheduleRound(HttpServletRequest request, HttpServletResponse response,
                                       Application application, String coordinatorName) throws IOException {

        Long appId = application.getId();
        String roundIdParam = request.getParameter("roundId");
        String dateParam = request.getParameter("interviewDate");
        String timeParam = request.getParameter("interviewTime");
        String mode = request.getParameter("mode");
        String interviewerName = request.getParameter("interviewerName");
        String interviewerInfo = request.getParameter("interviewerInfo");
        String remarks = request.getParameter("remarks");

        if (roundIdParam == null || dateParam == null || timeParam == null) {
            request.getSession().setAttribute("errorMessage", "Missing required reschedule parameters.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        Long roundId;
        LocalDate newDate;
        LocalTime newTime;
        try {
            roundId = Long.parseLong(roundIdParam.trim());
            newDate = LocalDate.parse(dateParam.trim());
            newTime = LocalTime.parse(timeParam.trim());
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "Invalid date, time, or identifier format.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        if (newDate.isBefore(LocalDate.now())) {
            request.getSession().setAttribute("errorMessage", "Rescheduled date cannot be in the past.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        boolean updated = roundDAO.rescheduleRound(roundId, newDate, newTime, mode,
                interviewerName, interviewerInfo, remarks, coordinatorName);

        if (updated) {
            request.getSession().setAttribute("successMessage", "Interview round rescheduled successfully.");
        } else {
            request.getSession().setAttribute("errorMessage", "Failed to reschedule round. Round may not be in SCHEDULED status.");
        }

        response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
    }

    private void handleCompleteRound(HttpServletRequest request, HttpServletResponse response,
                                     Application application, String coordinatorName) throws IOException {

        Long appId = application.getId();
        String roundIdParam = request.getParameter("roundId");
        String result = request.getParameter("result");
        String remarks = request.getParameter("remarks");
        String feedback = request.getParameter("feedback");

        if (roundIdParam == null || result == null || result.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Round identifier and evaluation result (PASSED/FAILED) are required.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        Long roundId;
        try {
            roundId = Long.parseLong(roundIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Invalid round identifier format.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        String resUpper = result.trim().toUpperCase();
        if (!InterviewRound.RESULT_PASSED.equals(resUpper) && !InterviewRound.RESULT_FAILED.equals(resUpper)) {
            request.getSession().setAttribute("errorMessage", "Evaluation result must be either PASSED or FAILED.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        boolean completed = roundDAO.completeRound(roundId, resUpper, remarks, feedback, coordinatorName);

        if (completed) {
            // Also update legacy interview table if present
            try {
                Interview legacy = interviewDAO.getInterviewByApplicationId(appId);
                if (legacy != null && "SCHEDULED".equals(legacy.getStatus())) {
                    interviewDAO.completeInterview(legacy.getId());
                }
            } catch (Exception ignored) {}

            request.getSession().setAttribute("successMessage",
                    "Interview round completed. Result recorded: " + resUpper + ".");
        } else {
            request.getSession().setAttribute("errorMessage", "Failed to record round completion.");
        }

        response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
    }

    private void handleCancelRound(HttpServletRequest request, HttpServletResponse response,
                                   Application application, String coordinatorName) throws IOException {

        Long appId = application.getId();
        String roundIdParam = request.getParameter("roundId");
        String reason = request.getParameter("cancellationReason");

        if (roundIdParam == null || reason == null || reason.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Cancellation reason is required.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        Long roundId;
        try {
            roundId = Long.parseLong(roundIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Invalid round identifier format.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        boolean cancelled = roundDAO.cancelRound(roundId, reason.trim(), coordinatorName);

        if (cancelled) {
            request.getSession().setAttribute("successMessage", "Interview round cancelled.");
        } else {
            request.getSession().setAttribute("errorMessage", "Failed to cancel round. Round must be in SCHEDULED status.");
        }

        response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
    }

    private void handleRecordFinalDecision(HttpServletRequest request, HttpServletResponse response,
                                           Application application, String coordinatorName) throws IOException {

        Long appId = application.getId();
        String decision = request.getParameter("finalDecision");
        String coordinatorRemarks = request.getParameter("coordinatorRemarks");
        String overallFeedback = request.getParameter("overallFeedback");

        if (decision == null || decision.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Final decision (SELECTED or REJECTED) is required.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        String decUpper = decision.trim().toUpperCase();
        if (!"SELECTED".equals(decUpper) && !"REJECTED".equals(decUpper)) {
            request.getSession().setAttribute("errorMessage", "Final determination must be either SELECTED or REJECTED.");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        // Validate selection prerequisites
        if ("SELECTED".equals(decUpper)) {
            if (roundDAO.hasActiveOrPendingRound(appId)) {
                request.getSession().setAttribute("errorMessage",
                        "Cannot record final SELECTION: An interview round is currently pending completion.");
                response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
                return;
            }

            if (roundDAO.hasAnyFailedRound(appId)) {
                request.getSession().setAttribute("errorMessage",
                        "Cannot record final SELECTION: Candidate has FAILED one or more interview rounds.");
                response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
                return;
            }

            if (!roundDAO.isAllRoundsCompletedAndPassed(appId)) {
                request.getSession().setAttribute("errorMessage",
                        "Cannot record final SELECTION: At least one round must be configured, COMPLETED, and PASSED.");
                response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
                return;
            }
        }

        // Update application status
        boolean statusUpdated;
        if ("SELECTED".equals(decUpper)) {
            statusUpdated = applicationDAO.selectApplication(appId);
        } else {
            statusUpdated = applicationDAO.rejectApplication(appId);
        }

        if (!statusUpdated) {
            request.getSession().setAttribute("errorMessage", "Failed to update application status to " + decUpper + ".");
            response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
            return;
        }

        // Generate and persist InterviewReport record
        Map<String, Integer> stats = roundDAO.getRoundStatistics(appId);
        InterviewReport report = reportDAO.getReportByApplicationId(appId);
        if (report == null) {
            report = new InterviewReport();
            report.setApplicationId(appId);
            report.setReportUuid("REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        report.setFinalDecision(decUpper);
        report.setDecisionDate(LocalDateTime.now());
        report.setTotalRounds(stats.getOrDefault("TOTAL", 0));
        report.setCompletedRounds(stats.getOrDefault("COMPLETED", 0));
        report.setPassedRounds(stats.getOrDefault("PASSED", 0));
        report.setFailedRounds(stats.getOrDefault("FAILED", 0));
        report.setCancelledRounds(stats.getOrDefault("CANCELLED", 0));
        report.setCoordinatorRemarks(coordinatorRemarks != null ? coordinatorRemarks.trim() : "Final evaluation completed.");
        report.setOverallFeedback(overallFeedback != null ? overallFeedback.trim() : "Candidate status set to " + decUpper + ".");
        report.setGeneratedBy(coordinatorName);
        report.setGeneratedAt(LocalDateTime.now());

        reportDAO.createOrUpdateReport(report);

        // Add history
        statusHistoryDAO.addHistory(new ApplicationStatusHistory(
                appId,
                decUpper,
                LocalDateTime.now()
        ));

        request.getSession().setAttribute("successMessage",
                "Final decision (" + decUpper + ") recorded successfully! Official PDF evaluation report is now available.");
        response.sendRedirect(request.getContextPath() + "/interview-rounds?applicationId=" + appId);
    }
}
