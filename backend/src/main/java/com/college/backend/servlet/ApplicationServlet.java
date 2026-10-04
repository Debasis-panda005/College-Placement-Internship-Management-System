package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.entity.Application;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/applications")
public class ApplicationServlet extends HttpServlet {

    private ApplicationDAO applicationDAO;

    @Override
    public void init() {
        applicationDAO = new ApplicationDAO();
    }

    // ============================
    // GET
    // ============================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // Shortlist
        if ("shortlist".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);

                boolean success =
                        applicationDAO.shortlistApplication(id);

                System.out.println(
                        "Shortlist Application: " + success
                );
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Select
        if ("select".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                Long id = Long.parseLong(idParameter);

                boolean success =
                        applicationDAO.selectApplication(id);

                System.out.println(
                        "Select Application: " + success
                );
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

                boolean success =
                        applicationDAO.deleteApplication(id);

                System.out.println(
                        "Delete Application: " + success
                );
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }


        // Display all applications
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