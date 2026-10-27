package com.college.backend.servlet;

import com.college.backend.dao.JobDAO;
import com.college.backend.entity.Job;
import com.college.backend.entity.User;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/jobs", "/jobs/*", "/internships", "/internships/*"})
public class JobServlet extends HttpServlet {

    private JobDAO jobDAO;

    @Override
    public void init() {
        jobDAO = new JobDAO();
    }

    // Setter for testing injection
    public void setJobDAO(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    // =====================================================
    // GET: VIEW, SEARCH, FILTER, SORT, FORM NAVIGATION
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        boolean isInternships = isInternshipsEndpoint(request);
        request.setAttribute("isInternshipsEndpoint", isInternships);

        CsrfUtil.getOrCreateToken(request.getSession());

        String action = request.getParameter("action");

        // Reject state-changing actions on GET with HTTP 405 Method Not Allowed
        if ("create".equals(action) || "update".equals(action) || "delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method GET is not supported for state-changing action: " + action);
            return;
        }

        // Pull flash messages from session
        String successMessage = (String) request.getSession().getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            request.getSession().removeAttribute("successMessage");
        }

        String errorMessage = (String) request.getSession().getAttribute("errorMessage");
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }

        // 1. ADD JOB FORM
        if ("new".equals(action)) {
            HttpSession session = request.getSession(false);
            Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
            if (sessionUserId == null) {
                HttpSession newSession = request.getSession(true);
                CsrfUtil.getOrCreateToken(newSession);
                newSession.setAttribute("errorMessage", "You must log in to add jobs.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (!isCoordinatorOrAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Only coordinators and administrators can create jobs.");
                return;
            }
            String reqType = request.getParameter("type");
            if (isInternships || "Internship".equalsIgnoreCase(reqType)) {
                Job job = new Job();
                job.setJobType("Internship");
                request.setAttribute("job", job);
            }
            request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                    .forward(request, response);
            return;
        }

        // 2. EDIT JOB FORM
        if ("edit".equals(action)) {
            HttpSession session = request.getSession(false);
            Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
            if (sessionUserId == null) {
                HttpSession newSession = request.getSession(true);
                CsrfUtil.getOrCreateToken(newSession);
                newSession.setAttribute("errorMessage", "You must log in to edit jobs.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (!isCoordinatorOrAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Only coordinators and administrators can edit jobs.");
                return;
            }
            Long id = parseId(request.getParameter("id"));
            if (id == null) {
                request.getSession().setAttribute("errorMessage", "Invalid job ID provided.");
                response.sendRedirect(request.getContextPath() + (isInternships ? "/internships" : "/jobs"));
                return;
            }

            Job job = jobDAO.getJobById(id);
            if (job == null) {
                request.getSession().setAttribute("errorMessage", "Job not found with ID: " + id);
                response.sendRedirect(request.getContextPath() + (isInternships ? "/internships" : "/jobs"));
                return;
            }

            request.setAttribute("job", job);
            request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                    .forward(request, response);
            return;
        }

        // 3. VIEW JOB DETAILS
        if ("view".equals(action)) {
            Long id = parseId(request.getParameter("id"));
            if (id == null) {
                request.getSession().setAttribute("errorMessage", "Invalid job ID provided.");
                response.sendRedirect(request.getContextPath() + (isInternships ? "/internships" : "/jobs"));
                return;
            }

            Job job = jobDAO.getJobById(id);
            if (job == null) {
                request.getSession().setAttribute("errorMessage", "Job not found with ID: " + id);
                response.sendRedirect(request.getContextPath() + (isInternships ? "/internships" : "/jobs"));
                return;
            }

            request.setAttribute("job", job);
            request.getRequestDispatcher("/WEB-INF/jsp/job-details.jsp")
                    .forward(request, response);
            return;
        }

        // 4. LIST JOBS (SEARCH, FILTER, SORT, ALL)
        String search = request.getParameter("search");
        String jobType = request.getParameter("jobType");
        String status = request.getParameter("status");
        String sortBy = request.getParameter("sortBy");

        List<Job> jobs;

        if (isInternships) {
            request.setAttribute("selectedJobType", "Internship");
            if (search != null && !search.trim().isEmpty()) {
                jobs = jobDAO.searchJobs(search.trim());
                request.setAttribute("searchKeyword", search.trim());
            } else if (status != null && !status.trim().isEmpty()) {
                jobs = jobDAO.getJobsByStatus(status.trim());
                request.setAttribute("selectedStatus", status.trim());
            } else if (sortBy != null && !sortBy.trim().isEmpty()) {
                jobs = jobDAO.getJobsSortedBy(sortBy.trim());
                request.setAttribute("selectedSortBy", sortBy.trim());
            } else {
                jobs = jobDAO.getAllInternships();
            }

            if (search != null || status != null || sortBy != null) {
                List<Job> filtered = new ArrayList<>();
                for (Job j : jobs) {
                    if (j.isInternship()) {
                        filtered.add(j);
                    }
                }
                jobs = filtered;
            }
        } else {
            if (search != null && !search.trim().isEmpty()) {
                jobs = jobDAO.searchJobs(search.trim());
                request.setAttribute("searchKeyword", search.trim());
            } else if (jobType != null && !jobType.trim().isEmpty()) {
                jobs = jobDAO.getJobsByType(jobType.trim());
                request.setAttribute("selectedJobType", jobType.trim());
            } else if (status != null && !status.trim().isEmpty()) {
                jobs = jobDAO.getJobsByStatus(status.trim());
                request.setAttribute("selectedStatus", status.trim());
            } else if (sortBy != null && !sortBy.trim().isEmpty()) {
                jobs = jobDAO.getJobsSortedBy(sortBy.trim());
                request.setAttribute("selectedSortBy", sortBy.trim());
            } else {
                jobs = jobDAO.getAllJobs();
            }
        }

        request.setAttribute("jobs", jobs);
        request.getRequestDispatcher("/WEB-INF/jsp/jobs.jsp")
                .forward(request, response);
    }

    // =====================================================
    // POST: CREATE, UPDATE, DELETE (WITH CSRF PROTECTION)
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Validate CSRF token
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        String action = request.getParameter("action");

        // Enforce coordinator/admin authorization for mutating job actions
        if ("create".equals(action) || "update".equals(action) || "delete".equals(action)) {
            if (!isCoordinatorOrAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Only coordinators and administrators can manage jobs.");
                return;
            }
        }

        boolean isInternships = isInternshipsEndpoint(request)
                || "internships".equalsIgnoreCase(request.getParameter("source"));
        String redirectTarget = isInternships
                ? (request.getContextPath() + "/internships")
                : (request.getContextPath() + "/jobs");

        // 1. CREATE JOB
        if ("create".equals(action)) {
            String title = trimToNull(request.getParameter("title"));
            String company = trimToNull(request.getParameter("company"));
            String location = trimToNull(request.getParameter("location"));
            String description = trimToNull(request.getParameter("description"));
            String salary = trimToNull(request.getParameter("salary"));
            String duration = trimToNull(request.getParameter("duration"));
            String jobType = trimToNull(request.getParameter("jobType"));
            String status = trimToNull(request.getParameter("status"));
            String deadlineParam = trimToNull(request.getParameter("deadline"));
            String minCgpaParam = trimToNull(request.getParameter("minCgpa"));
            String eligibleDepartments = trimToNull(request.getParameter("eligibleDepartments"));

            LocalDate deadline = null;
            if (deadlineParam != null) {
                try {
                    deadline = LocalDate.parse(deadlineParam);
                } catch (DateTimeParseException e) {
                    Job job = new Job(title, company, location, description, salary, duration, jobType,
                            status != null ? status : "Active", null, null, eligibleDepartments);
                    request.setAttribute("errorMessage", "Invalid Application Deadline format. Please use YYYY-MM-DD.");
                    request.setAttribute("job", job);
                    request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                            .forward(request, response);
                    return;
                }
            }

            BigDecimal minCgpa = null;
            if (minCgpaParam != null) {
                try {
                    minCgpa = new BigDecimal(minCgpaParam);
                    if (minCgpa.compareTo(BigDecimal.ZERO) < 0 || minCgpa.compareTo(new BigDecimal("10.00")) > 0) {
                        Job job = new Job(title, company, location, description, salary, duration, jobType,
                                status != null ? status : "Active", deadline, null, eligibleDepartments);
                        request.setAttribute("errorMessage", "Minimum CGPA must be a valid number between 0.00 and 10.00.");
                        request.setAttribute("job", job);
                        request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                                .forward(request, response);
                        return;
                    }
                } catch (NumberFormatException e) {
                    Job job = new Job(title, company, location, description, salary, duration, jobType,
                            status != null ? status : "Active", deadline, null, eligibleDepartments);
                    request.setAttribute("errorMessage", "Minimum CGPA must be a valid number between 0.00 and 10.00.");
                    request.setAttribute("job", job);
                    request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                            .forward(request, response);
                    return;
                }
            }

            if (title == null || company == null || location == null || jobType == null) {
                request.setAttribute("errorMessage", "Title, Company, Location, and Job Type are required.");
                request.setAttribute("job", new Job(title, company, location, description, salary, duration, jobType,
                        status != null ? status : "Active", deadline, minCgpa, eligibleDepartments));
                request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                        .forward(request, response);
                return;
            }

            Job job = new Job(title, company, location, description, salary, duration, jobType,
                    status != null ? status : "Active", deadline, minCgpa, eligibleDepartments);

            boolean created = jobDAO.addJob(job);
            if (created) {
                String successMsg = isInternships ? "Internship created successfully." : "Job created successfully.";
                request.getSession().setAttribute("successMessage", successMsg);
                response.sendRedirect(redirectTarget);
            } else {
                request.setAttribute("errorMessage", "Failed to create " + (isInternships ? "internship" : "job") + ". Please check system logs.");
                request.setAttribute("job", job);
                request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                        .forward(request, response);
            }
            return;
        }

        // 2. UPDATE JOB
        if ("update".equals(action)) {
            Long id = parseId(request.getParameter("id"));
            if (id == null) {
                request.getSession().setAttribute("errorMessage", "Invalid " + (isInternships ? "internship" : "job") + " ID provided for update.");
                response.sendRedirect(redirectTarget);
                return;
            }

            String title = trimToNull(request.getParameter("title"));
            String company = trimToNull(request.getParameter("company"));
            String location = trimToNull(request.getParameter("location"));
            String description = trimToNull(request.getParameter("description"));
            String salary = trimToNull(request.getParameter("salary"));
            String duration = trimToNull(request.getParameter("duration"));
            String jobType = trimToNull(request.getParameter("jobType"));
            String status = trimToNull(request.getParameter("status"));
            String deadlineParam = trimToNull(request.getParameter("deadline"));
            String minCgpaParam = trimToNull(request.getParameter("minCgpa"));
            String eligibleDepartments = trimToNull(request.getParameter("eligibleDepartments"));

            LocalDate deadline = null;
            if (deadlineParam != null) {
                try {
                    deadline = LocalDate.parse(deadlineParam);
                } catch (DateTimeParseException e) {
                    Job job = new Job(id, title, company, location, description, salary, duration, jobType,
                            status != null ? status : "Active", null, null, eligibleDepartments);
                    request.setAttribute("errorMessage", "Invalid Application Deadline format. Please use YYYY-MM-DD.");
                    request.setAttribute("job", job);
                    request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                            .forward(request, response);
                    return;
                }
            }

            BigDecimal minCgpa = null;
            if (minCgpaParam != null) {
                try {
                    minCgpa = new BigDecimal(minCgpaParam);
                    if (minCgpa.compareTo(BigDecimal.ZERO) < 0 || minCgpa.compareTo(new BigDecimal("10.00")) > 0) {
                        Job job = new Job(id, title, company, location, description, salary, duration, jobType,
                                status != null ? status : "Active", deadline, null, eligibleDepartments);
                        request.setAttribute("errorMessage", "Minimum CGPA must be a valid number between 0.00 and 10.00.");
                        request.setAttribute("job", job);
                        request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                                .forward(request, response);
                        return;
                    }
                } catch (NumberFormatException e) {
                    Job job = new Job(id, title, company, location, description, salary, duration, jobType,
                            status != null ? status : "Active", deadline, null, eligibleDepartments);
                    request.setAttribute("errorMessage", "Minimum CGPA must be a valid number between 0.00 and 10.00.");
                    request.setAttribute("job", job);
                    request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                            .forward(request, response);
                    return;
                }
            }

            if (title == null || company == null || location == null || jobType == null) {
                Job job = new Job(id, title, company, location, description, salary, duration, jobType,
                        status != null ? status : "Active", deadline, minCgpa, eligibleDepartments);
                request.setAttribute("errorMessage", "Title, Company, Location, and Job Type are required.");
                request.setAttribute("job", job);
                request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                        .forward(request, response);
                return;
            }

            Job job = new Job(id, title, company, location, description, salary, duration, jobType,
                    status != null ? status : "Active", deadline, minCgpa, eligibleDepartments);

            boolean updated = jobDAO.updateJob(job);
            if (updated) {
                String successMsg = isInternships ? "Internship updated successfully." : "Job updated successfully.";
                request.getSession().setAttribute("successMessage", successMsg);
                response.sendRedirect(redirectTarget);
            } else {
                request.setAttribute("errorMessage", "Failed to update " + (isInternships ? "internship" : "job") + " with ID: " + id);
                request.setAttribute("job", job);
                request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                        .forward(request, response);
            }
            return;
        }

        // 3. DELETE JOB
        if ("delete".equals(action)) {
            Long id = parseId(request.getParameter("id"));
            if (id == null) {
                request.getSession().setAttribute("errorMessage", "Invalid " + (isInternships ? "internship" : "job") + " ID provided for deletion.");
                response.sendRedirect(redirectTarget);
                return;
            }

            boolean deleted = jobDAO.deleteJob(id);
            if (deleted) {
                request.getSession().setAttribute("successMessage", isInternships ? "Internship deleted successfully." : "Job deleted successfully.");
            } else {
                request.getSession().setAttribute("errorMessage", "Failed to delete " + (isInternships ? "internship" : "job") + " with ID: " + id);
            }
            response.sendRedirect(redirectTarget);
            return;
        }

        // Unknown action
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private boolean isInternshipsEndpoint(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String servletPath = request.getServletPath();
        return servletPath != null && servletPath.startsWith("/internships");
    }

    private boolean isCoordinatorOrAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Long userId = (Long) session.getAttribute(LoginServlet.SESSION_USER_ID);
        String role = (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE);
        return userId != null && (User.ROLE_COORDINATOR.equals(role) || User.ROLE_ADMIN.equals(role));
    }

    private Long parseId(String idParam) {
        if (idParam == null || idParam.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
