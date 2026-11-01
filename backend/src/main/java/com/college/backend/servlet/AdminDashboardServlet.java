package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.exception.DAOException;
import com.college.backend.util.AuthUtil;
import com.college.backend.util.CsrfUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller servlet for the administrative dashboard.
 * Aggregates real-time metrics across registered students, jobs, internships,
 * applications, and interview schedules.
 * Restricted strictly to authenticated users with COORDINATOR or ADMIN role.
 * Only HTTP GET requests are supported; all modifying HTTP methods return HTTP 405.
 */
@WebServlet({"/admin/dashboard", "/admin"})
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardServlet.class.getName());

    private StudentDAO studentDAO;
    private JobDAO jobDAO;
    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;

    @Override
    public void init() {
        this.studentDAO = new StudentDAO();
        this.jobDAO = new JobDAO();
        this.applicationDAO = new ApplicationDAO();
        this.interviewDAO = new InterviewDAO();
    }

    // Setters for testing dependency injection
    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public void setJobDAO(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    public void setApplicationDAO(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    public void setInterviewDAO(InterviewDAO interviewDAO) {
        this.interviewDAO = interviewDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure CSRF token is present in the user's session
        CsrfUtil.getOrCreateToken(request.getSession());

        // 1. Enforce Authentication
        if (!AuthUtil.isAuthenticated(request)) {
            HttpSession session = request.getSession(true);
            CsrfUtil.getOrCreateToken(session);
            session.setAttribute("errorMessage", "You must log in to view the admin dashboard.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Enforce Role Authorization (Students receive 403 Forbidden, only COORDINATOR or ADMIN permitted)
        if (AuthUtil.isStudent(request) || !AuthUtil.isCoordinatorOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator or Coordinator privileges required.");
            return;
        }

        // 3. Process Flash Messages from Session
        HttpSession session = request.getSession(false);
        if (session != null) {
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
        }

        // 4. Retrieve Aggregated Dashboard Metrics
        try {
            int totalStudents = studentDAO.getTotalStudents();
            Map<String, Integer> jobStats = jobDAO.getJobStatistics();
            Map<String, Integer> appStats = applicationDAO.getApplicationStatistics();
            Map<String, Integer> interviewStats = interviewDAO.getInterviewStatistics();

            int totalJobs = jobStats != null ? jobStats.getOrDefault("JOBS", 0) : 0;
            int totalInternships = jobStats != null ? jobStats.getOrDefault("INTERNSHIPS", 0) : 0;
            int activeOpportunities = jobStats != null ? jobStats.getOrDefault("ACTIVE", 0) : 0;

            int totalApplications = appStats != null ? appStats.getOrDefault("TOTAL", 0) : 0;
            int shortlistedApplications = appStats != null ? appStats.getOrDefault("SHORTLISTED", 0) : 0;
            int selectedApplications = appStats != null ? appStats.getOrDefault("SELECTED", 0) : 0;

            int scheduledInterviews = interviewStats != null ? interviewStats.getOrDefault("SCHEDULED", 0) : 0;

            // Consolidated statistics map for JSP EL expressions (e.g. ${stats.totalStudents})
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", totalStudents);
            stats.put("totalJobs", totalJobs);
            stats.put("totalInternships", totalInternships);
            stats.put("activeJobs", activeOpportunities);
            stats.put("activeOpportunities", activeOpportunities);
            stats.put("totalApplications", totalApplications);
            stats.put("shortlistedApplications", shortlistedApplications);
            stats.put("selectedApplications", selectedApplications);
            stats.put("scheduledInterviews", scheduledInterviews);

            request.setAttribute("stats", stats);

            // Also expose individual request attributes for backwards compatibility
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("totalJobs", totalJobs);
            request.setAttribute("totalInternships", totalInternships);
            request.setAttribute("activeOpportunities", activeOpportunities);
            request.setAttribute("activeJobs", activeOpportunities);
            request.setAttribute("totalApplications", totalApplications);
            request.setAttribute("shortlistedApplications", shortlistedApplications);
            request.setAttribute("selectedApplications", selectedApplications);
            request.setAttribute("scheduledInterviews", scheduledInterviews);

        } catch (DAOException e) {
            // Log internally and present user-safe error without exposing database details or credentials
            LOGGER.log(Level.SEVERE, "Database error retrieving live dashboard metrics", e);
            request.setAttribute("errorMessage", "Unable to load complete dashboard metrics due to a database error. Please try again later.");
            request.setAttribute("databaseError", Boolean.TRUE);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", 0);
            stats.put("totalJobs", 0);
            stats.put("totalInternships", 0);
            stats.put("activeJobs", 0);
            stats.put("activeOpportunities", 0);
            stats.put("totalApplications", 0);
            stats.put("shortlistedApplications", 0);
            stats.put("selectedApplications", 0);
            stats.put("scheduledInterviews", 0);
            request.setAttribute("stats", stats);

            request.setAttribute("totalStudents", 0);
            request.setAttribute("totalJobs", 0);
            request.setAttribute("totalInternships", 0);
            request.setAttribute("activeOpportunities", 0);
            request.setAttribute("activeJobs", 0);
            request.setAttribute("totalApplications", 0);
            request.setAttribute("shortlistedApplications", 0);
            request.setAttribute("selectedApplications", 0);
            request.setAttribute("scheduledInterviews", 0);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/admin-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method POST is not supported for this endpoint.");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method PUT is not supported for this endpoint.");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method DELETE is not supported for this endpoint.");
    }
}
