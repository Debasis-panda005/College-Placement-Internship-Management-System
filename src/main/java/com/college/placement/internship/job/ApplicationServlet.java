
package com.college.placement.internship.job;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/ApplicationServlet")
public class ApplicationServlet extends HttpServlet {

    private ApplicationDAO applicationDAO;

    @Override
    public void init() {
        applicationDAO = new ApplicationDAO();
    }

    // SUBMIT APPLICATION OR UPDATE STATUS
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("updateStatus".equals(action)) {
            try {
                int applicationId = Integer.parseInt(
                        request.getParameter("applicationId"));

                String status = request.getParameter("status");

                boolean updated =
                        applicationDAO.updateApplicationStatus(
                                applicationId, status);

                String message = updated
                        ? "statusUpdated"
                        : "statusUpdateFailed";

                response.sendRedirect(
                        request.getContextPath()
                        + "/ApplicationServlet?message=" + message);

            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid application ID.");
            }

            return;
        }

        // EXISTING APPLICATION SUBMISSION
        try {
            int jobId = Integer.parseInt(
                    request.getParameter("jobId"));

            String studentName =
                    request.getParameter("studentName");
            String studentEmail =
                    request.getParameter("studentEmail");
            String resume =
                    request.getParameter("resume");

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
                    "Invalid job ID.");
        }
    }

    // VIEW AND FILTER APPLICATIONS
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        String jobId = request.getParameter("jobId");
        String status = request.getParameter("status");
        String sort = request.getParameter("sort");

        if (search == null) search = "";
        if (jobId == null) jobId = "";
        if (status == null) status = "";
        if (sort == null) sort = "newest";

        List<Application> applications =
                applicationDAO.getFilteredApplications(
                        search, jobId, status, sort);

        request.setAttribute("applications", applications);
        request.setAttribute("filterSearch", search);
        request.setAttribute("filterJobId", jobId);
        request.setAttribute("filterStatus", status);
        request.setAttribute("filterSort", sort);

        request.getRequestDispatcher("/applications.jsp")
                .forward(request, response);
    }
}
