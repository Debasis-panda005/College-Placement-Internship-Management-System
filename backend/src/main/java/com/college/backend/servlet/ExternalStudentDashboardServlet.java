package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.Interview;
import com.college.backend.entity.Job;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the dedicated External Student Dashboard.
 * Displays profile overview, real-time recruitment metrics from MySQL,
 * and quick-action access to eligible jobs, internships, applications, and interviews.
 */
@WebServlet("/external/dashboard")
public class ExternalStudentDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO;
    private ExternalStudentDAO externalStudentDAO;
    private JobDAO jobDAO;
    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;

    @Override
    public void init() {
        this.userDAO = new UserDAO();
        this.externalStudentDAO = new ExternalStudentDAO(this.userDAO);
        this.jobDAO = new JobDAO();
        this.applicationDAO = new ApplicationDAO();
        this.interviewDAO = new InterviewDAO();
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setExternalStudentDAO(ExternalStudentDAO externalStudentDAO) {
        this.externalStudentDAO = externalStudentDAO;
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

    // =====================================================
    // GET: RENDER DASHBOARD
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce session authentication
        if (!AuthUtil.isAuthenticated(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = AuthUtil.getSessionUserId(request);
        String role = AuthUtil.getSessionUserRole(request);

        if (!User.ROLE_STUDENT.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Student account required.");
            return;
        }

        // 2. Load and verify external student profile
        ExternalStudent student = externalStudentDAO.findByUserId(userId);
        if (student == null) {
            // Not an external student - redirect internal students to regular jobs dashboard
            response.sendRedirect(request.getContextPath() + "/jobs");
            return;
        }

        // 3. Verification check: pending and rejected accounts cannot access dashboard
        if (ExternalStudent.STATUS_PENDING.equalsIgnoreCase(student.getVerificationStatus())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            request.setAttribute("errorMessage",
                    "Your student registration is pending verification. Please contact the administrator or check back once approved.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        if (ExternalStudent.STATUS_REJECTED.equalsIgnoreCase(student.getVerificationStatus())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            request.setAttribute("errorMessage",
                    "Your registration request has been rejected. Please contact the placement administrator.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        // Ensure session attributes for external student are present
        HttpSession currentSession = request.getSession(false);
        if (currentSession != null) {
            currentSession.setAttribute("studentType", "EXTERNAL");
            currentSession.setAttribute("externalStudentId", student.getId());
        }

        // 4. Compute Live Recruitment Statistics from MySQL
        Map<String, Integer> stats = computeDashboardStats(student);

        // 5. Check flash messages
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

        CsrfUtil.getOrCreateToken(request.getSession());

        request.setAttribute("student", student);
        request.setAttribute("stats", stats);
        request.setAttribute("metrics", stats);
        request.getRequestDispatcher("/WEB-INF/jsp/external-dashboard.jsp").forward(request, response);
    }

    // =====================================================
    // POST: UPDATE PERMITTED PROFILE FIELDS
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!AuthUtil.isAuthenticated(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        Long userId = AuthUtil.getSessionUserId(request);
        ExternalStudent student = externalStudentDAO.findByUserId(userId);
        if (student == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found.");
            return;
        }

        String phone = request.getParameter("phone");
        String currentSemesterStr = request.getParameter("currentSemester");

        boolean modified = false;

        if (phone != null && !phone.trim().isEmpty()) {
            student.setPhone(phone.trim());
            modified = true;
        }

        if (currentSemesterStr != null && !currentSemesterStr.trim().isEmpty()) {
            try {
                int semester = Integer.parseInt(currentSemesterStr.trim());
                if (ExternalStudent.isValidSemester(semester)) {
                    student.setCurrentSemester(semester);
                    modified = true;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (modified) {
            try {
                updatePermittedFieldsInDb(student);
                request.getSession().setAttribute("successMessage", "Profile details updated successfully.");
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            }
        }

        response.sendRedirect(request.getContextPath() + "/external/dashboard");
    }

    private Map<String, Integer> computeDashboardStats(ExternalStudent student) {
        Map<String, Integer> stats = new HashMap<>();
        int availableInternships = 0;
        int availableJobs = 0;
        int totalApplications = 0;
        int shortlistedApplications = 0;
        int upcomingInterviews = 0;
        int selectedApplications = 0;

        // 1. Available jobs and internships matching external audience
        try {
            List<Job> allActive = jobDAO.getActiveJobs();
            if (allActive != null) {
                LocalDate today = LocalDate.now();
                for (Job job : allActive) {
                    if (job.isEligibleForExternal()) {
                        if (job.getDeadline() != null && job.isDeadlinePassed(today)) {
                            continue;
                        }
                        if (job.isInternship()) {
                            availableInternships++;
                        } else {
                            availableJobs++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Student applications breakdown
        try {
            List<Application> allApps = applicationDAO.getAllApplications();
            if (allApps != null && student != null) {
                for (Application app : allApps) {
                    boolean isOwned = (student.getId().equals(app.getExternalStudentId()))
                            || (student.getId().equals(app.getStudentId()) && "EXTERNAL".equalsIgnoreCase(app.getApplicantType()));

                    if (isOwned) {
                        totalApplications++;
                        if ("SHORTLISTED".equalsIgnoreCase(app.getStatus())) {
                            shortlistedApplications++;
                        } else if ("SELECTED".equalsIgnoreCase(app.getStatus())) {
                            selectedApplications++;
                        }

                        // Check upcoming interviews for this application
                        try {
                            Interview inv = interviewDAO.getInterviewByApplicationId(app.getId());
                            if (inv != null) {
                                if ("SCHEDULED".equalsIgnoreCase(inv.getStatus()) || "RESCHEDULED".equalsIgnoreCase(inv.getStatus())) {
                                    upcomingInterviews++;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        stats.put("availableInternships", availableInternships);
        stats.put("availableJobs", availableJobs);
        stats.put("totalApplications", totalApplications);
        stats.put("shortlistedApplications", shortlistedApplications);
        stats.put("upcomingInterviews", upcomingInterviews);
        stats.put("selectedApplications", selectedApplications);

        return stats;
    }

    private void updatePermittedFieldsInDb(ExternalStudent student) throws SQLException {
        externalStudentDAO.updatePermittedProfile(student.getId(), student.getPhone(), student.getCurrentSemester());
    }
}
