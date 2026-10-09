package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.Interview;
import com.college.backend.util.CsrfUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@WebServlet("/interviews")
public class InterviewServlet extends HttpServlet {

    private InterviewDAO interviewDAO;
    private ApplicationDAO applicationDAO;

    @Override
    public void init() {
        interviewDAO = new InterviewDAO();
        applicationDAO = new ApplicationDAO();
    }

    // =====================================================
    // GET REQUEST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        CsrfUtil.getOrCreateToken(request.getSession());

        String action = request.getParameter("action");

        // Reject state-changing actions on GET with HTTP 405 Method Not Allowed
        if ("complete".equals(action) || "cancel".equals(action) || "delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method GET is not supported for action: " + action);
            return;
        }

        // -------------------------------------------------
        // EDIT INTERVIEW
        // -------------------------------------------------

        if ("edit".equals(action)) {

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID is required!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idParam.trim());
            } catch (NumberFormatException e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Interview ID format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Interview interview = interviewDAO.getInterviewById(id);

            if (interview == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID " + id + " does not exist!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (!"SCHEDULED".equals(interview.getStatus())) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Only SCHEDULED interviews can be edited or rescheduled."
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            // Check for error messages from session
            String errorMessage =
                    (String) request.getSession().getAttribute("errorMessage");

            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                request.getSession().removeAttribute("errorMessage");
            }

            request.setAttribute("interview", interview);
            request.setAttribute("today", LocalDate.now().toString());

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/interview-edit.jsp"
            ).forward(request, response);

        }

        // -------------------------------------------------
        // DEFAULT - SHOW ALL INTERVIEWS
        // -------------------------------------------------

        else {

            // Pre-fill applicationId if provided (e.g. from shortlist page)
            String appIdParameter = request.getParameter("applicationId");
            if (appIdParameter != null && !appIdParameter.isEmpty()) {
                try {
                    Long selectedAppId = Long.parseLong(appIdParameter);
                    request.setAttribute("selectedApplicationId", selectedAppId);
                } catch (NumberFormatException e) {
                    // ignore invalid format
                }
            }

            // Check for error messages from session
            String errorMessage =
                    (String) request.getSession().getAttribute("errorMessage");

            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                request.getSession().removeAttribute("errorMessage");
            }

            // Check for success messages from session
            String successMessage =
                    (String) request.getSession().getAttribute("successMessage");

            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                request.getSession().removeAttribute("successMessage");
            }

            // Retrieve interview statistics
            Map<String, Integer> interviewStats =
                    interviewDAO.getInterviewStatistics();

            request.setAttribute(
                    "interviewStats",
                    interviewStats
            );

            Map<Long, Boolean> rescheduledMap =
                    interviewDAO.getRescheduledApplicationsMap();

            request.setAttribute(
                    "rescheduledMap",
                    rescheduledMap
            );

            // Parse search and filter parameters
            String filterAppIdParam = request.getParameter("filterAppId");
            String filterDateParam = request.getParameter("filterDate");
            String filterModeParam = request.getParameter("filterMode");
            String filterStatusParam = request.getParameter("filterStatus");

            Long filterAppId = null;
            if (filterAppIdParam != null && !filterAppIdParam.trim().isEmpty()) {
                try {
                    filterAppId = Long.parseLong(filterAppIdParam.trim());
                } catch (NumberFormatException e) {
                    // ignore invalid format gracefully
                }
            }

            LocalDate filterDate = null;
            if (filterDateParam != null && !filterDateParam.trim().isEmpty()) {
                try {
                    filterDate = LocalDate.parse(filterDateParam.trim());
                } catch (DateTimeParseException e) {
                    // ignore invalid format gracefully
                }
            }

            String filterMode = null;
            if (filterModeParam != null && !filterModeParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(filterModeParam.trim())) {
                filterMode = filterModeParam.trim();
            }

            String filterStatus = null;
            if (filterStatusParam != null && !filterStatusParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(filterStatusParam.trim())) {
                filterStatus = filterStatusParam.trim();
            }

            List<Interview> interviews;
            if (filterAppId != null || filterDate != null || filterMode != null || filterStatus != null) {
                interviews = interviewDAO.searchInterviews(filterAppId, filterDate, filterMode, filterStatus);
            } else {
                interviews = interviewDAO.getAllInterviews();
            }

            request.setAttribute(
                    "interviews",
                    interviews
            );
            request.setAttribute("today", LocalDate.now().toString());

            // Retain filter parameter values for JSP form inputs
            request.setAttribute("selectedFilterAppId", filterAppIdParam != null ? filterAppIdParam.trim() : "");
            request.setAttribute("selectedFilterDate", filterDateParam != null ? filterDateParam.trim() : "");
            request.setAttribute("selectedFilterMode", filterModeParam != null ? filterModeParam.trim() : "ALL");
            request.setAttribute("selectedFilterStatus", filterStatusParam != null ? filterStatusParam.trim() : "ALL");

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/interviews.jsp"
            ).forward(request, response);
        }
    }


    // =====================================================
    // POST REQUEST
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Validate CSRF Token
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        String action = request.getParameter("action");

        // -------------------------------------------------
        // CREATE INTERVIEW
        // -------------------------------------------------

        if ("create".equals(action)) {

            String appIdParam = request.getParameter("applicationId");
            if (appIdParam == null || appIdParam.trim().isEmpty()) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Application ID is required!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Long applicationId;
            try {
                applicationId = Long.parseLong(appIdParam.trim());
            } catch (NumberFormatException e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Application ID format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            // Enforce business rule: Interview should only be scheduled for a SHORTLISTED application
            Application application =
                    applicationDAO.getApplicationById(applicationId);

            if (application == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Application ID " + applicationId + " does not exist!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (!"SHORTLISTED".equals(application.getStatus())) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview can only be scheduled for a SHORTLISTED application (Current status: "
                                + application.getStatus() + ")!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (interviewDAO.hasActiveInterview(applicationId)) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "An interview is already scheduled for Application ID " + applicationId + "!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            String dateParam = request.getParameter("interviewDate");
            String timeParam = request.getParameter("interviewTime");
            String mode = request.getParameter("mode");

            if (dateParam == null || dateParam.trim().isEmpty() ||
                    timeParam == null || timeParam.trim().isEmpty() ||
                    mode == null || mode.trim().isEmpty()) {

                request.getSession().setAttribute(
                        "errorMessage",
                        "All fields are required to schedule an interview!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?applicationId=" + applicationId
                );
                return;
            }

            LocalDate interviewDate;
            LocalTime interviewTime;
            try {
                interviewDate = LocalDate.parse(dateParam.trim());
                interviewTime = LocalTime.parse(timeParam.trim());
            } catch (Exception e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid date or time format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?applicationId=" + applicationId
                );
                return;
            }

            String validationError =
                    validateInterviewDateTime(interviewDate, interviewTime);

            if (validationError != null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        validationError
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?applicationId=" + applicationId
                );
                return;
            }

            Interview interview =
                    new Interview(
                            applicationId,
                            interviewDate,
                            interviewTime,
                            mode.trim(),
                            "SCHEDULED"
                    );

            interviewDAO.createInterview(interview);

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
        }

        // -------------------------------------------------
        // UPDATE INTERVIEW
        // -------------------------------------------------

        else if ("update".equals(action)) {

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID is required!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idParam.trim());
            } catch (NumberFormatException e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Interview ID format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Interview existingInterview = interviewDAO.getInterviewById(id);
            if (existingInterview == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID " + id + " does not exist!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (!"SCHEDULED".equals(existingInterview.getStatus())) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Only SCHEDULED interviews can be edited or rescheduled."
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            String dateParam = request.getParameter("interviewDate");
            String timeParam = request.getParameter("interviewTime");
            String mode = request.getParameter("mode");

            if (dateParam == null || dateParam.trim().isEmpty() ||
                    timeParam == null || timeParam.trim().isEmpty() ||
                    mode == null || mode.trim().isEmpty()) {

                request.getSession().setAttribute(
                        "errorMessage",
                        "All fields are required to update an interview!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?action=edit&id=" + id
                );
                return;
            }

            LocalDate interviewDate;
            LocalTime interviewTime;
            try {
                interviewDate = LocalDate.parse(dateParam.trim());
                interviewTime = LocalTime.parse(timeParam.trim());
            } catch (Exception e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid date or time format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?action=edit&id=" + id
                );
                return;
            }

            String validationError =
                    validateInterviewDateTime(interviewDate, interviewTime);

            if (validationError != null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        validationError
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?action=edit&id=" + id
                );
                return;
            }

            // Preserve interview ID, application ID, and status
            existingInterview.setInterviewDate(interviewDate);
            existingInterview.setInterviewTime(interviewTime);
            existingInterview.setMode(mode.trim());

            boolean updated = interviewDAO.updateInterview(existingInterview);
            if (!updated) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Failed to update interview ID " + id + "!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews?action=edit&id=" + id
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
        }

        // -------------------------------------------------
        // COMPLETE INTERVIEW
        // -------------------------------------------------

        else if ("complete".equals(action)) {

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID is required!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idParam.trim());
            } catch (NumberFormatException e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Interview ID format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (id <= 0) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Interview ID!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Interview interview = interviewDAO.getInterviewById(id);

            if (interview == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID " + id + " does not exist!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (!"SCHEDULED".equals(interview.getStatus())) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Only SCHEDULED interviews can be completed."
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            boolean completed = interviewDAO.completeInterview(id);
            if (completed) {
                request.getSession().setAttribute(
                        "successMessage",
                        "Interview completed successfully."
                );
            } else {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Failed to complete the interview!"
                );
            }

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
            return;
        }

        // -------------------------------------------------
        // CANCEL INTERVIEW
        // -------------------------------------------------

        else if ("cancel".equals(action)) {

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID is required!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idParam.trim());
            } catch (NumberFormatException e) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Interview ID format!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (id <= 0) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Invalid Interview ID!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            Interview interview = interviewDAO.getInterviewById(id);

            if (interview == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview ID " + id + " does not exist!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (!"SCHEDULED".equals(interview.getStatus())) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Only SCHEDULED interviews can be cancelled."
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            boolean cancelled = interviewDAO.cancelInterview(id);
            if (cancelled) {
                request.getSession().setAttribute(
                        "successMessage",
                        "Interview cancelled successfully."
                );
            } else {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Failed to cancel the interview!"
                );
            }

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
            return;
        }

        // -------------------------------------------------
        // DELETE INTERVIEW
        // -------------------------------------------------

        else if ("delete".equals(action)) {

            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    Long id = Long.parseLong(idParam.trim());
                    interviewDAO.deleteInterview(id);
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Interview ID format!"
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
            return;
        }

        // -------------------------------------------------
        // DEFAULT FALLBACK
        // -------------------------------------------------

        else {
            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
        }
    }

    // =====================================================
    // DATE / TIME VALIDATION HELPER
    // =====================================================

    private String validateInterviewDateTime(
            LocalDate interviewDate,
            LocalTime interviewTime) {

        if (interviewDate == null || interviewTime == null) {
            return "Interview date and time are required!";
        }

        LocalDate today = LocalDate.now();

        if (interviewDate.isBefore(today)) {
            return "Interview date cannot be in the past!";
        }

        if (interviewDate.isEqual(today)) {
            LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
            if (interviewTime.isBefore(now)) {
                return "Interview time cannot be in the past for today's date!";
            }
        }

        return null;
    }
}
