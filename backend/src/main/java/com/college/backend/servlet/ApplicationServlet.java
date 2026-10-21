package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ApplicationStatusHistoryDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.CsrfUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({"/applications", "/shortlist"})
public class ApplicationServlet extends HttpServlet {

    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;
    private ApplicationStatusHistoryDAO statusHistoryDAO;
    private StudentDAO studentDAO;
    private JobDAO jobDAO;

    @Override
    public void init() {
        applicationDAO = new ApplicationDAO();
        interviewDAO = new InterviewDAO();
        statusHistoryDAO = new ApplicationStatusHistoryDAO();
        studentDAO = new StudentDAO();
        jobDAO = new JobDAO();
    }

    // Setters for test dependency injection
    public void setApplicationDAO(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    public void setInterviewDAO(InterviewDAO interviewDAO) {
        this.interviewDAO = interviewDAO;
    }

    public void setStatusHistoryDAO(ApplicationStatusHistoryDAO statusHistoryDAO) {
        this.statusHistoryDAO = statusHistoryDAO;
    }

    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public void setJobDAO(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    // ============================
    // GET
    // ============================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        CsrfUtil.getOrCreateToken(request.getSession());

        // 1. Enforce Authentication on all Application pages (Requirement 5.1.1)
        HttpSession session = request.getSession(false);
        Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
        String sessionUserRole = session != null ? (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE) : null;

        if (sessionUserId == null) {
            HttpSession newSession = request.getSession(true);
            CsrfUtil.getOrCreateToken(newSession);
            newSession.setAttribute("errorMessage", "You must log in to view applications.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isCoordinatorOrAdmin = User.ROLE_COORDINATOR.equals(sessionUserRole) || User.ROLE_ADMIN.equals(sessionUserRole);
        Student currentStudent = null;
        if (!isCoordinatorOrAdmin) {
            currentStudent = studentDAO.findByUserId(sessionUserId);
            if (currentStudent == null) {
                HttpSession currSession = request.getSession(false);
                if (currSession != null) {
                    currSession.setAttribute("errorMessage", "No student profile found for your account. Please complete your registration.");
                }
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;
            }
        }

        String servletPath = request.getServletPath();

        // Handle /shortlist route - display shortlisted applications (Requirement 5.1.1, 5.1.2, 5.1.3)
        if ("/shortlist".equals(servletPath)) {

            String errorMessage =
                    (String) request.getSession().getAttribute("errorMessage");
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                request.getSession().removeAttribute("errorMessage");
            }

            String successMessage =
                    (String) request.getSession().getAttribute("successMessage");
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                request.getSession().removeAttribute("successMessage");
            }

            List<Application> shortlistedApplications;
            if (isCoordinatorOrAdmin) {
                shortlistedApplications = applicationDAO.getShortlistedApplications();
            } else {
                List<Application> allShortlisted = applicationDAO.getShortlistedApplications();
                shortlistedApplications = new ArrayList<>();
                if (allShortlisted != null) {
                    for (Application app : allShortlisted) {
                        if (currentStudent.getId().equals(app.getStudentId())) {
                            shortlistedApplications.add(app);
                        }
                    }
                }
            }

            List<Interview> allInterviews =
                    interviewDAO.getAllInterviews();

            Map<Long, Interview> interviewMap = new HashMap<>();
            if (allInterviews != null) {
                for (Interview interview : allInterviews) {
                    if (interview.getApplicationId() != null) {
                        Long appId = interview.getApplicationId();
                        Interview existing = interviewMap.get(appId);
                        if (existing == null || (interview.getId() != null && (existing.getId() == null || interview.getId() > existing.getId()))) {
                            interviewMap.put(appId, interview);
                        }
                    }
                }
            }

            Map<Long, Boolean> rescheduledMap =
                    interviewDAO.getRescheduledApplicationsMap();

            request.setAttribute(
                    "applications",
                    shortlistedApplications
            );

            request.setAttribute(
                    "interviewMap",
                    interviewMap
            );

            request.setAttribute(
                    "rescheduledMap",
                    rescheduledMap
            );

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/shortlist.jsp"
            ).forward(request, response);

            return;
        }

        String action = request.getParameter("action");

        // Reject state-changing actions on GET with HTTP 405 Method Not Allowed
        if ("shortlist".equals(action) || "select".equals(action) || "reject".equals(action) || "delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method GET is not supported for action: " + action);
            return;
        }

        // View Application Details (Requirement 5.1.2, 5.1.5 IDOR Prevention)
        if ("details".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application != null) {

                        // Object ownership validation to prevent IDOR
                        if (!isCoordinatorOrAdmin && !currentStudent.getId().equals(application.getStudentId())) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access denied: You are not authorized to view this application.");
                            return;
                        }

                        Interview interview =
                                interviewDAO.getInterviewByApplicationId(id);

                        request.setAttribute(
                                "application",
                                application
                        );

                        request.setAttribute(
                                "interview",
                                interview
                        );

                        request.getRequestDispatcher(
                                "/WEB-INF/jsp/application-details.jsp"
                        ).forward(request, response);

                        return;

                    } else {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    }

                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }

        // View Application Status History Timeline (Requirement 5.1.2, 5.1.5 IDOR Prevention)
        if ("history".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application != null) {

                        // Object ownership validation to prevent IDOR
                        if (!isCoordinatorOrAdmin && !currentStudent.getId().equals(application.getStudentId())) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access denied: You are not authorized to view this application history.");
                            return;
                        }

                        List<ApplicationStatusHistory> statusHistory =
                                statusHistoryDAO.getHistoryByApplicationId(id);

                        request.setAttribute(
                                "application",
                                application
                        );

                        request.setAttribute(
                                "statusHistory",
                                statusHistory
                        );

                        request.getRequestDispatcher(
                                "/WEB-INF/jsp/application-history.jsp"
                        ).forward(request, response);

                        return;

                    } else {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    }

                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            } else {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Application ID is required"
                );
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
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

        String successMessage =
                (String) request.getSession().getAttribute("successMessage");

        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            request.getSession().removeAttribute("successMessage");
        }

        // Parse search and filter parameters
        String studentIdParam = request.getParameter("studentId");
        String jobIdParam = request.getParameter("jobId");
        String statusParam = request.getParameter("status");

        Long filterStudentId = null;
        if (isCoordinatorOrAdmin) {
            if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
                try {
                    filterStudentId = Long.parseLong(studentIdParam.trim());
                } catch (NumberFormatException e) {
                    // Ignore invalid number format gracefully
                }
            }
        } else {
            // Student role: strictly bound to own student record
            filterStudentId = currentStudent.getId();
        }

        Long filterJobId = null;
        if (jobIdParam != null && !jobIdParam.trim().isEmpty()) {
            try {
                filterJobId = Long.parseLong(jobIdParam.trim());
            } catch (NumberFormatException e) {
                // Ignore invalid number format gracefully
            }
        }

        String filterStatus = null;
        if (statusParam != null && !statusParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusParam.trim())) {
            filterStatus = statusParam.trim();
        }

        // Retrieve filtered or all applications (Requirement 5.1.2, 5.1.3)
        List<Application> applications;
        if (isCoordinatorOrAdmin) {
            if (filterStudentId != null || filterJobId != null || filterStatus != null) {
                applications = applicationDAO.searchApplications(filterStudentId, filterJobId, filterStatus);
            } else {
                applications = applicationDAO.getAllApplications();
            }
        } else {
            // Student: always retrieve applications scoped to own student ID
            applications = applicationDAO.searchApplications(filterStudentId, filterJobId, filterStatus);
        }

        List<Long> completedAppIds =
                interviewDAO.getCompletedApplicationIds();

        request.setAttribute(
                "applications",
                applications
        );

        request.setAttribute(
                "completedAppIds",
                completedAppIds
        );

        // Retain filter parameter values for JSP form inputs
        request.setAttribute("selectedStudentId", isCoordinatorOrAdmin ? (studentIdParam != null ? studentIdParam.trim() : "") : currentStudent.getId().toString());
        request.setAttribute("selectedJobId", jobIdParam != null ? jobIdParam.trim() : "");
        request.setAttribute("selectedStatus", statusParam != null ? statusParam.trim() : "ALL");

        // Retrieve application statistics for dashboard summary
        Map<String, Integer> applicationStats;
        if (isCoordinatorOrAdmin) {
            applicationStats = applicationDAO.getApplicationStatistics();
        } else {
            applicationStats = new HashMap<>();
            applicationStats.put("TOTAL", 0);
            applicationStats.put("APPLIED", 0);
            applicationStats.put("SHORTLISTED", 0);
            applicationStats.put("SELECTED", 0);
            applicationStats.put("REJECTED", 0);
            List<Application> allStudentApps = applicationDAO.searchApplications(currentStudent.getId(), null, null);
            if (allStudentApps != null) {
                for (Application app : allStudentApps) {
                    applicationStats.put("TOTAL", applicationStats.get("TOTAL") + 1);
                    String st = app.getStatus();
                    if (st != null && applicationStats.containsKey(st.toUpperCase())) {
                        applicationStats.put(st.toUpperCase(), applicationStats.get(st.toUpperCase()) + 1);
                    }
                }
            }
        }

        request.setAttribute(
                "applicationStats",
                applicationStats
        );

        request.getRequestDispatcher(
                "/WEB-INF/jsp/applications.jsp"
        ).forward(request, response);
    }


    // ============================
    // POST
    // ============================

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

        // Handle application creation (both "create" and "apply" actions)
        if ("create".equals(action) || "apply".equals(action)) {
            handleCreateApplication(request, response);
            return;
        }

        // Status transitions and deletions require COORDINATOR or ADMIN role (Requirement 5.1.4)
        HttpSession session = request.getSession(false);
        Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
        String sessionUserRole = session != null ? (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE) : null;

        boolean isCoordinatorOrAdmin = sessionUserId != null &&
                (User.ROLE_COORDINATOR.equals(sessionUserRole) || User.ROLE_ADMIN.equals(sessionUserRole));

        if ("shortlist".equals(action) || "reject".equals(action) || "select".equals(action) || "delete".equals(action)) {
            if (!isCoordinatorOrAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied: Only coordinators and administrators can perform this action.");
                return;
            }
        }

        // Shortlist (Allowed only from APPLIED)
        if ("shortlist".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application == null) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    } else if (!"APPLIED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot shortlist: Application is currently "
                                        + application.getStatus()
                                        + " (Only APPLIED applications can be shortlisted)!"
                        );
                    } else {
                        applicationDAO.shortlistApplication(id);
                    }
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

        }

        // Reject (Allowed only from APPLIED or SHORTLISTED)
        else if ("reject".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application == null) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    } else if ("SELECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot reject: Candidate has already been SELECTED!"
                        );
                    } else if ("REJECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application is already REJECTED!"
                        );
                    } else {
                        boolean rejected = applicationDAO.rejectApplication(id);
                        if (rejected) {
                            request.getSession().setAttribute(
                                    "successMessage",
                                    "Application rejected successfully."
                            );
                        } else {
                            request.getSession().setAttribute(
                                    "errorMessage",
                                    "Failed to reject application ID: " + id
                            );
                        }
                    }
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            String from = request.getParameter("from");
            String redirectTarget = "shortlist".equals(from) ? "/shortlist" : ("interviews".equals(from) ? "/interviews" : "/applications");

            response.sendRedirect(
                    request.getContextPath() + redirectTarget
            );

        }

        // Select (Allowed only from SHORTLISTED and interview must be COMPLETED)
        else if ("select".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application == null) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    } else if ("APPLIED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot select: Application must be SHORTLISTED and complete an interview first!"
                        );
                    } else if ("REJECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot select: Application is already REJECTED!"
                        );
                    } else if ("SELECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application is already SELECTED!"
                        );
                    } else if (!"SHORTLISTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot select: Application is currently " + application.getStatus() + "!"
                        );
                    } else {
                        // Application is SHORTLISTED: verify interview is COMPLETED
                        boolean completed =
                                interviewDAO.isInterviewCompletedForApplication(id);

                        if (completed) {
                            boolean selected = applicationDAO.selectApplication(id);
                            if (selected) {
                                request.getSession().setAttribute(
                                        "successMessage",
                                        "Application selected successfully."
                                );
                            } else {
                                request.getSession().setAttribute(
                                        "errorMessage",
                                        "Failed to select application ID: " + id
                                );
                            }
                        } else {
                            request.getSession().setAttribute(
                                    "errorMessage",
                                    "Candidate can only be SELECTED after the interview is COMPLETED!"
                            );
                        }
                    }
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            String from = request.getParameter("from");
            String redirectTarget = "shortlist".equals(from) ? "/shortlist" : ("interviews".equals(from) ? "/interviews" : "/applications");

            response.sendRedirect(
                    request.getContextPath() + redirectTarget
            );

        }

        // Delete
        else if ("delete".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    applicationDAO.deleteApplication(id);
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

        }

        // Default fallback
        else {
            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );
        }
    }

    /**
     * Processes student or coordinator application submissions with mandatory authentication,
     * server-side identity resolution, job eligibility validation, and duplicate prevention.
     */
    private void handleCreateApplication(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce Authentication (Requirement 4.1.A, 4.1.E)
        HttpSession session = request.getSession(false);
        Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
        String sessionUserRole = session != null ? (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE) : null;

        if (sessionUserId == null) {
            HttpSession newSession = request.getSession(true);
            CsrfUtil.getOrCreateToken(newSession);
            newSession.setAttribute("errorMessage", "You must log in to apply.");
            request.setAttribute("errorMessage", "You must log in to apply.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Resolve Applicant Student Identity (Requirement 4.1.B, 4.1.C, 4.1.D, 4.1.F, 4.1.G)
        Student applicantStudent = null;
        Long applicantStudentId = null;

        boolean isCoordinatorOrAdmin = User.ROLE_COORDINATOR.equals(sessionUserRole) || User.ROLE_ADMIN.equals(sessionUserRole);
        String submittedStudentIdParam = request.getParameter("studentId");

        if (isCoordinatorOrAdmin && submittedStudentIdParam != null && !submittedStudentIdParam.trim().isEmpty()) {
            // Coordinator/Admin administrative workflow: can register on behalf of a specific student ID
            try {
                applicantStudentId = Long.parseLong(submittedStudentIdParam.trim());
                applicantStudent = studentDAO.findById(applicantStudentId);
                if (applicantStudent == null) {
                    setApplicationErrorAndRedirect(request, response, "Student not found with ID: " + applicantStudentId);
                    return;
                }
            } catch (NumberFormatException e) {
                setApplicationErrorAndRedirect(request, response, "Invalid Student ID: " + submittedStudentIdParam);
                return;
            }
        } else {
            // Student self-application workflow:
            // Identity is strictly bound to the authenticated user ID in the session.
            // Any client-supplied 'studentId' is ignored to prevent identity spoofing / privilege escalation.
            applicantStudent = studentDAO.findByUserId(sessionUserId);
            if (applicantStudent == null) {
                setApplicationErrorAndRedirect(request, response, "No student profile found for your account. Please complete your registration.");
                return;
            }
            applicantStudentId = applicantStudent.getId();
        }

        // 3. Validate Job ID (Requirement 4.1.I, 4.2.G)
        String jobIdParam = request.getParameter("jobId");
        if (jobIdParam == null || jobIdParam.trim().isEmpty()) {
            setApplicationErrorAndRedirect(request, response, "Job ID is required.");
            return;
        }

        Long jobId;
        try {
            jobId = Long.parseLong(jobIdParam.trim());
        } catch (NumberFormatException e) {
            setApplicationErrorAndRedirect(request, response, "Job not found.");
            return;
        }

        // 4. Eligibility Check A: Job exists (Requirement 4.2.A)
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            setApplicationErrorAndRedirect(request, response, "Job not found.");
            return;
        }

        // 5. Eligibility Check B: Job is not closed (Requirement 4.2.B)
        if (job.isClosed()) {
            setApplicationErrorAndRedirect(request, response, "Applications are closed for this job.");
            return;
        }

        // 6. Eligibility Check C: Application deadline (Requirement 4.2.C)
        LocalDate today = LocalDate.now();
        if (job.isDeadlinePassed(today)) {
            setApplicationErrorAndRedirect(request, response, "The application deadline has passed.");
            return;
        }

        // 7. Eligibility Check D: Minimum CGPA (Requirement 4.2.D)
        if (!job.isCgpaEligible(applicantStudent.getCgpa())) {
            setApplicationErrorAndRedirect(request, response, "Your CGPA does not meet the minimum requirement.");
            return;
        }

        // 8. Eligibility Check E: Eligible Departments (Requirement 4.2.E)
        if (!job.isDepartmentEligible(applicantStudent.getDepartment())) {
            setApplicationErrorAndRedirect(request, response, "Your department is not eligible.");
            return;
        }

        // 9. Duplicate Check A: Application Pre-check (Requirement 4.3.A, 4.3.B)
        if (applicationDAO.hasAlreadyApplied(applicantStudentId, jobId)) {
            setApplicationErrorAndRedirect(request, response, "You have already applied for this job.");
            return;
        }

        // 10. Application Date & Persistence (Requirement 4.1.K, 4.1.L, 4.3.G, 4.3.I)
        LocalDate applicationDate = today;
        String appDateParam = request.getParameter("applicationDate");
        if (appDateParam != null && !appDateParam.trim().isEmpty()) {
            try {
                applicationDate = LocalDate.parse(appDateParam.trim());
            } catch (DateTimeParseException ignored) {
                applicationDate = today;
            }
        }

        Application application = new Application(applicantStudentId, jobId, applicationDate, "APPLIED");

        try {
            boolean created = applicationDAO.createApplication(application);
            if (created) {
                HttpSession currSession = request.getSession(false);
                if (currSession != null) {
                    currSession.setAttribute("successMessage", "Application submitted successfully!");
                }
                request.setAttribute("successMessage", "Application submitted successfully!");
                response.sendRedirect(request.getContextPath() + "/applications");
            } else {
                setApplicationErrorAndRedirect(request, response, "Application could not be created. Please try again later.");
            }
        } catch (DuplicateRecordException e) {
            // Handled database-level unique constraint violation (concurrent submission race condition)
            setApplicationErrorAndRedirect(request, response, "You have already applied for this job.");
        } catch (DAOException e) {
            setApplicationErrorAndRedirect(request, response, "A database error occurred while creating your application. Please try again later.");
        }
    }

    private void setApplicationErrorAndRedirect(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("errorMessage", errorMessage);
        }
        request.setAttribute("errorMessage", errorMessage);
        response.sendRedirect(request.getContextPath() + "/applications");
    }
}
