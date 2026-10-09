package com.college.backend.servlet;

import com.college.backend.dao.JobDAO;
import com.college.backend.entity.Job;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet({"/jobs", "/jobs/*"})
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
            request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                    .forward(request, response);
            return;
        }

        // 2. EDIT JOB FORM
        if ("edit".equals(action)) {
            Long id = parseId(request.getParameter("id"));
            if (id == null) {
                request.getSession().setAttribute("errorMessage", "Invalid job ID provided.");
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;
            }

            Job job = jobDAO.getJobById(id);
            if (job == null) {
                request.getSession().setAttribute("errorMessage", "Job not found with ID: " + id);
                response.sendRedirect(request.getContextPath() + "/jobs");
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
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;
            }

            Job job = jobDAO.getJobById(id);
            if (job == null) {
                request.getSession().setAttribute("errorMessage", "Job not found with ID: " + id);
                response.sendRedirect(request.getContextPath() + "/jobs");
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

        // 1. CREATE JOB
        if ("create".equals(action)) {
            String title = trimToNull(request.getParameter("title"));
            String company = trimToNull(request.getParameter("company"));
            String location = trimToNull(request.getParameter("location"));
            String description = trimToNull(request.getParameter("description"));
            String salary = trimToNull(request.getParameter("salary"));
            String jobType = trimToNull(request.getParameter("jobType"));
            String status = trimToNull(request.getParameter("status"));

            if (title == null || company == null || location == null || jobType == null) {
                request.setAttribute("errorMessage", "Title, Company, Location, and Job Type are required.");
                request.setAttribute("job", new Job(title, company, location, description, salary, jobType, status));
                request.getRequestDispatcher("/WEB-INF/jsp/job-add.jsp")
                        .forward(request, response);
                return;
            }

            Job job = new Job(title, company, location, description, salary, jobType,
                    status != null ? status : "Active");

            boolean created = jobDAO.addJob(job);
            if (created) {
                request.getSession().setAttribute("successMessage", "Job created successfully.");
                response.sendRedirect(request.getContextPath() + "/jobs");
            } else {
                request.setAttribute("errorMessage", "Failed to create job. Please check system logs.");
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
                request.getSession().setAttribute("errorMessage", "Invalid job ID provided for update.");
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;
            }

            String title = trimToNull(request.getParameter("title"));
            String company = trimToNull(request.getParameter("company"));
            String location = trimToNull(request.getParameter("location"));
            String description = trimToNull(request.getParameter("description"));
            String salary = trimToNull(request.getParameter("salary"));
            String jobType = trimToNull(request.getParameter("jobType"));
            String status = trimToNull(request.getParameter("status"));

            if (title == null || company == null || location == null || jobType == null) {
                Job job = new Job(id, title, company, location, description, salary, jobType, status);
                request.setAttribute("errorMessage", "Title, Company, Location, and Job Type are required.");
                request.setAttribute("job", job);
                request.getRequestDispatcher("/WEB-INF/jsp/job-edit.jsp")
                        .forward(request, response);
                return;
            }

            Job job = new Job(id, title, company, location, description, salary, jobType,
                    status != null ? status : "Active");

            boolean updated = jobDAO.updateJob(job);
            if (updated) {
                request.getSession().setAttribute("successMessage", "Job updated successfully.");
                response.sendRedirect(request.getContextPath() + "/jobs");
            } else {
                request.setAttribute("errorMessage", "Failed to update job with ID: " + id);
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
                request.getSession().setAttribute("errorMessage", "Invalid job ID provided for deletion.");
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;
            }

            boolean deleted = jobDAO.deleteJob(id);
            if (deleted) {
                request.getSession().setAttribute("successMessage", "Job deleted successfully.");
            } else {
                request.getSession().setAttribute("errorMessage", "Failed to delete job with ID: " + id);
            }
            response.sendRedirect(request.getContextPath() + "/jobs");
            return;
        }

        // Unknown action
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
    }

    // =====================================================
    // HELPERS
    // =====================================================

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
