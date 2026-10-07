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

        // Shortlist
        if ("shortlist".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);

                applicationDAO.shortlistApplication(id);
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Reject
        if ("reject".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);

                applicationDAO.rejectApplication(id);
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Select (Only allowed after interview is completed)
        if ("select".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);

                // Enforce business rule: Selection only after completed interview
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

        // Display all applications
        List<Application> applications =
                applicationDAO.getAllApplications();

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