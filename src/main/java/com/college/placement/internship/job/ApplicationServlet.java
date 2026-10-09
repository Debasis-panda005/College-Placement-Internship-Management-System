package com.college.placement.internship.job;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/ApplicationServlet")
public class ApplicationServlet extends HttpServlet {

    private ApplicationDAO applicationDAO;

    @Override
    public void init() {
        applicationDAO = new ApplicationDAO();
    }

    // SUBMIT APPLICATION
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // UPDATE APPLICATION STATUS
        if ("updateStatus".equals(action)) {

            String idValue = request.getParameter("id");
            String status = request.getParameter("status");

            if (idValue != null &&
                    ("Pending".equals(status)
                            || "Shortlisted".equals(status)
                            || "Selected".equals(status)
                            || "Rejected".equals(status))) {

                try {
                    int applicationId = Integer.parseInt(idValue);

                    if (applicationId > 0) {
                        applicationDAO.updateApplicationStatus(
                                applicationId, status);
                    }

                } catch (NumberFormatException e) {
                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid application ID");
                    return;
                }

                response.sendRedirect(
                        "ApplicationServlet?message=statusUpdated");
                return;
            }

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid application status or ID");
            return;
        }

        // SUBMIT A NEW APPLICATION
        try {
            int jobId = Integer.parseInt(
                    request.getParameter("jobId"));

            String studentName = request.getParameter("studentName");
            String studentEmail = request.getParameter("studentEmail");
            String resume = request.getParameter("resume");

            Application application = new Application();
            application.setJobId(jobId);
            application.setStudentName(studentName);
            application.setStudentEmail(studentEmail);
            application.setResume(resume);

            applicationDAO.addApplication(application);

            response.sendRedirect(
                    "JobServlet?message=applicationSubmitted");

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid job ID");
        }
    }

    // VIEW ALL APPLICATIONS
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<Application> applications =
                applicationDAO.getAllApplications();

        request.setAttribute("applications", applications);

        request.getRequestDispatcher("applications.jsp")
                .forward(request, response);
    }
}