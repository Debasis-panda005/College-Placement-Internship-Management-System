
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

    private static final long serialVersionUID = 1L;

    private ApplicationDAO applicationDAO;

    @Override
    public void init() throws ServletException {
        applicationDAO = new ApplicationDAO();
    }

    // Submit an application or update its status
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("updateStatus".equals(action)) {
            String idValue = request.getParameter("id");
            String status = request.getParameter("status");

            if (idValue == null ||
                    !("Pending".equals(status)
                            || "Shortlisted".equals(status)
                            || "Selected".equals(status)
                            || "Rejected".equals(status))) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid application status or ID");
                return;
            }

            try {
                int applicationId = Integer.parseInt(idValue);

                if (applicationId <= 0) {
                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid application ID");
                    return;
                }

                applicationDAO.updateApplicationStatus(
                        applicationId, status);

                response.sendRedirect(
                        request.getContextPath()
                                + "/ApplicationServlet?message=statusUpdated");

            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid application ID");
            }

            return;
        }

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
                    request.getContextPath()
                            + "/JobServlet?message=applicationSubmitted");

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid job ID");
        }
    }

    // Display all applications
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<Application> applications =
                applicationDAO.getAllApplications();

        request.setAttribute("applications", applications);

        request.getRequestDispatcher("/applications.jsp")
                .forward(request, response);
    }
}
