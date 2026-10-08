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

    // SUBMIT APPLICATION
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        int jobId = Integer.parseInt(
                request.getParameter("jobId")
        );

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
                "JobServlet?message=applicationSubmitted"
        );
    }


    // VIEW ALL APPLICATIONS
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<Application> applications =
                applicationDAO.getAllApplications();

        request.setAttribute(
                "applications",
                applications
        );

        request.getRequestDispatcher(
                "applications.jsp"
        ).forward(request, response);
    }
}
