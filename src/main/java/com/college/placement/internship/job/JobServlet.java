package com.college.placement.internship.job;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/JobServlet")
public class JobServlet extends HttpServlet {

    private JobDAO jobDAO;

    @Override
    public void init() {
        jobDAO = new JobDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String idParameter = request.getParameter("id");

        String title = request.getParameter("title");
        String company = request.getParameter("company");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        String salary = request.getParameter("salary");
        String jobType = request.getParameter("jobType");

        Job job = new Job();

        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setDescription(description);
        job.setSalary(salary);
        job.setJobType(jobType);

        // If ID exists, update existing job
        if (idParameter != null && !idParameter.isEmpty()) {

            int id = Integer.parseInt(idParameter);

            job.setId(id);

            jobDAO.updateJob(job);

        } else {

            // Otherwise, add a new job
            jobDAO.addJob(job);
        }

        response.sendRedirect("JobServlet");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        // DELETE JOB
        if ("delete".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                int id = Integer.parseInt(idParameter);

                jobDAO.deleteJob(id);
            }

            response.sendRedirect("JobServlet");
            return;
        }
        // EDIT JOB
        if ("edit".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                int id = Integer.parseInt(idParameter);

                Job job = jobDAO.getJobById(id);

                request.setAttribute("job", job);

                request.getRequestDispatcher("edit-job.jsp")
                        .forward(request, response);

                return;
            }
        }

        // VIEW ALL JOBS
        request.setAttribute("jobs", jobDAO.getAllJobs());

        request.getRequestDispatcher("jobs.jsp")
                .forward(request, response);
    }
}