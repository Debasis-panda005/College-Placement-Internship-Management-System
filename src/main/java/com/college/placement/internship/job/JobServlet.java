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

    // ADD OR UPDATE JOB
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

            // Send update success message
            response.sendRedirect("JobServlet?message=updated");

        } else {

            // Otherwise, add a new job
            jobDAO.addJob(job);

            // Send add success message
            response.sendRedirect("JobServlet?message=added");
        }
    }

    // VIEW, SEARCH, FILTER, SORT, EDIT OR DELETE JOB
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String search = request.getParameter("search");
        String jobType = request.getParameter("jobType");
        String sortBy = request.getParameter("sortBy");
        String message = request.getParameter("message");

        // DELETE JOB
        if ("delete".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                int id = Integer.parseInt(idParameter);

                jobDAO.deleteJob(id);
            }

            // Send delete success message
            response.sendRedirect("JobServlet?message=deleted");
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

        // VIEW JOB DETAILS
        if ("view".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.isEmpty()) {

                int id = Integer.parseInt(idParameter);

                Job job = jobDAO.getJobById(id);

                request.setAttribute("job", job);

                request.getRequestDispatcher("job-details.jsp")
                        .forward(request, response);

                return;
            }
        }

        // SEARCH JOBS
        if (search != null && !search.trim().isEmpty()) {

            request.setAttribute(
                    "jobs",
                    jobDAO.searchJobs(search.trim())
            );

            // FILTER JOBS BY TYPE
        } else if (jobType != null && !jobType.trim().isEmpty()) {

            request.setAttribute(
                    "jobs",
                    jobDAO.getJobsByType(jobType)
            );

            // SORT JOBS
        } else if (sortBy != null && !sortBy.trim().isEmpty()) {

            request.setAttribute(
                    "jobs",
                    jobDAO.getJobsSortedBy(sortBy)
            );

            // VIEW ALL JOBS
        } else {

            request.setAttribute(
                    "jobs",
                    jobDAO.getAllJobs()
            );
        }

        // Send message to JSP
        request.setAttribute("message", message);

        // SHOW JOBS PAGE
        request.getRequestDispatcher("jobs.jsp")
                .forward(request, response);
    }
}