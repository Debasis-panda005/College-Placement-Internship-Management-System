package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.entity.Application;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet({"/applications", "/shortlist"})
public class ApplicationServlet extends HttpServlet {

    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;

    @Override
    public void init() {
        applicationDAO = new ApplicationDAO();
        interviewDAO = new InterviewDAO();
    }

    // ============================
    // GET
    // ============================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        // Handle /shortlist route - display ONLY shortlisted applications
        if ("/shortlist".equals(servletPath)) {

            List<Application> shortlistedApplications =
                    applicationDAO.getShortlistedApplications();

            request.setAttribute(
                    "applications",
                    shortlistedApplications
            );

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/shortlist.jsp"
            ).forward(request, response);

            return;
        }

        String action = request.getParameter("action");

        // Shortlist (Allowed only from APPLIED)
        if ("shortlist".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);
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
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Reject (Allowed only from APPLIED or SHORTLISTED)
        if ("reject".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);
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
                    applicationDAO.rejectApplication(id);
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Select (Allowed only from SHORTLISTED and interview must be COMPLETED)
        if ("select".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);
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
                        applicationDAO.selectApplication(id);
                    } else {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Candidate can only be SELECTED after the interview is COMPLETED!"
                        );
                    }
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Delete
        if ("delete".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);

                applicationDAO.deleteApplication(id);
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

        // Parse search and filter parameters
        String studentIdParam = request.getParameter("studentId");
        String jobIdParam = request.getParameter("jobId");
        String statusParam = request.getParameter("status");

        Long filterStudentId = null;
        if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
            try {
                filterStudentId = Long.parseLong(studentIdParam.trim());
            } catch (NumberFormatException e) {
                // Ignore invalid number format gracefully
            }
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

        // Retrieve filtered or all applications
        List<Application> applications;
        if (filterStudentId != null || filterJobId != null || filterStatus != null) {
            applications = applicationDAO.searchApplications(filterStudentId, filterJobId, filterStatus);
        } else {
            applications = applicationDAO.getAllApplications();
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
        request.setAttribute("selectedStudentId", studentIdParam != null ? studentIdParam.trim() : "");
        request.setAttribute("selectedJobId", jobIdParam != null ? jobIdParam.trim() : "");
        request.setAttribute("selectedStatus", statusParam != null ? statusParam.trim() : "ALL");

        // Retrieve application statistics for dashboard summary
        Map<String, Integer> applicationStats =
                applicationDAO.getApplicationStatistics();

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

        String action = request.getParameter("action");

        if ("create".equals(action)) {

            try {

                Long studentId =
                        Long.parseLong(
                                request.getParameter("studentId")
                        );

                Long jobId =
                        Long.parseLong(
                                request.getParameter("jobId")
                        );

                // Duplicate application prevention
                if (applicationDAO.hasAlreadyApplied(studentId, jobId)) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Student ID " + studentId + " has already applied for Job ID " + jobId + "!"
                    );
                    response.sendRedirect(
                            request.getContextPath() + "/applications"
                    );
                    return;
                }

                LocalDate applicationDate =
                        LocalDate.parse(
                                request.getParameter("applicationDate")
                        );

                Application application =
                        new Application(
                                studentId,
                                jobId,
                                applicationDate,
                                "APPLIED"
                        );

                boolean created =
                        applicationDAO.createApplication(application);

                System.out.println(
                        "Create Application: " + created
                );

                if (created) {

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/applications"
                    );

                } else {

                    request.setAttribute(
                            "errorMessage",
                            "Application could not be created. Check the database and server console."
                    );

                    List<Application> applications =
                            applicationDAO.getAllApplications();

                    request.setAttribute(
                            "applications",
                            applications
                    );

                    request.getRequestDispatcher(
                            "/WEB-INF/jsp/applications.jsp"
                    ).forward(request, response);
                }

            } catch (Exception e) {

                e.printStackTrace();

                request.setAttribute(
                        "errorMessage",
                        "Error creating application: "
                                + e.getMessage()
                );

                List<Application> applications =
                        applicationDAO.getAllApplications();

                request.setAttribute(
                        "applications",
                        applications
                );

                request.getRequestDispatcher(
                        "/WEB-INF/jsp/applications.jsp"
                ).forward(request, response);
            }
        }
    }
}
