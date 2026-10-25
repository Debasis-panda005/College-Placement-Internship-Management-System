
package com.college.placement.internship.job;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/JobServlet")
public class JobServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private JobDAO jobDAO;

    @Override
    public void init() throws ServletException {
        jobDAO = new JobDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idParameter = request.getParameter("id");
        String title = request.getParameter("title");
        String company = request.getParameter("company");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        String salary = request.getParameter("salary");
        String jobType = request.getParameter("jobType");
        String status = request.getParameter("status");

        Job job = new Job();

        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setDescription(description);
        job.setSalary(salary);
        job.setJobType(jobType);
        job.setStatus(status);

        try {
            if (idParameter != null && !idParameter.trim().isEmpty()) {
                int id = Integer.parseInt(idParameter);
                job.setId(id);

                jobDAO.updateJob(job);

                response.sendRedirect(
                    request.getContextPath()
                    + "/JobServlet?message=updated"
                );
            } else {
                jobDAO.addJob(job);

                response.sendRedirect(
                    request.getContextPath()
                    + "/JobServlet?message=added"
                );
            }
        } catch (NumberFormatException e) {
            throw new ServletException("Invalid job ID.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String search = request.getParameter("search");
        String jobType = request.getParameter("jobType");
        String status = request.getParameter("status");
        String sortBy = request.getParameter("sortBy");
        String message = request.getParameter("message");

        String idParameter = request.getParameter("id");

        // DELETE JOB
        if ("delete".equalsIgnoreCase(action)) {
            try {
                if (idParameter != null
                        && !idParameter.trim().isEmpty()) {

                    int id = Integer.parseInt(idParameter);
                    jobDAO.deleteJob(id);
                }

                response.sendRedirect(
                    request.getContextPath()
                    + "/JobServlet?message=deleted"
                );
                return;

            } catch (NumberFormatException e) {
                throw new ServletException("Invalid job ID.", e);
            }
        }

        // EDIT JOB
        if ("edit".equalsIgnoreCase(action)) {
            try {
                if (idParameter != null
                        && !idParameter.trim().isEmpty()) {

                    int id = Integer.parseInt(idParameter);
                    Job job = jobDAO.getJobById(id);

                    if (job == null) {
                        response.sendError(
                            HttpServletResponse.SC_NOT_FOUND,
                            "Job not found."
                        );
                        return;
                    }

                    request.setAttribute("job", job);

                    request.getRequestDispatcher("/edit-job.jsp")
                           .forward(request, response);
                    return;
                }

            } catch (NumberFormatException e) {
                throw new ServletException("Invalid job ID.", e);
            }
        }

        // APPLY FOR JOB
        if ("apply".equalsIgnoreCase(action)) {
            try {
                if (idParameter != null
                        && !idParameter.trim().isEmpty()) {

                    int id = Integer.parseInt(idParameter);
                    Job job = jobDAO.getJobById(id);

                    if (job == null) {
                        response.sendError(
                            HttpServletResponse.SC_NOT_FOUND,
                            "Job not found."
                        );
                        return;
                    }

                    request.setAttribute("job", job);

                    request.getRequestDispatcher("/apply-job.jsp")
                           .forward(request, response);
                    return;
                }

            } catch (NumberFormatException e) {
                throw new ServletException("Invalid job ID.", e);
            }
        }

        // VIEW JOB DETAILS
        if ("view".equalsIgnoreCase(action)) {
            try {
                if (idParameter != null
                        && !idParameter.trim().isEmpty()) {

                    int id = Integer.parseInt(idParameter);
                    Job job = jobDAO.getJobById(id);

                    if (job == null) {
                        response.sendError(
                            HttpServletResponse.SC_NOT_FOUND,
                            "Job not found."
                        );
                        return;
                    }

                    request.setAttribute("job", job);

                    request.getRequestDispatcher("/job-details.jsp")
                           .forward(request, response);
                    return;
                }

            } catch (NumberFormatException e) {
                throw new ServletException("Invalid job ID.", e);
            }
        }

        // RETRIEVE JOBS
        List<Job> jobs;

        try {
            if (search != null && !search.trim().isEmpty()) {

                jobs = jobDAO.searchJobs(search.trim());

            } else if (jobType != null
                    && !jobType.trim().isEmpty()) {

                jobs = jobDAO.getJobsByType(jobType);

            } else if (status != null
                    && !status.trim().isEmpty()) {

                jobs = jobDAO.getJobsByStatus(status);

            } else if (sortBy != null
                    && !sortBy.trim().isEmpty()) {

                jobs = jobDAO.getJobsSortedBy(sortBy);

            } else {

                // Default: retrieve every job
                jobs = jobDAO.getAllJobs();

                // DEBUG OUTPUT IN ECLIPSE CONSOLE
                System.out.println("========== JOB DEBUG ==========");
                System.out.println(
                    "Context path: " + request.getContextPath()
                );
                System.out.println(
                    "Request URI: " + request.getRequestURI()
                );
                System.out.println(
                    "Jobs retrieved: "
                    + (jobs == null ? "NULL" : jobs.size())
                );

                if (jobs != null) {
                    for (Job currentJob : jobs) {
                        System.out.println(
                            "Job ID: " + currentJob.getId()
                            + ", Title: " + currentJob.getTitle()
                            + ", Company: " + currentJob.getCompany()
                        );
                    }
                }

                System.out.println("===============================");
            }

            if (jobs == null) {
                jobs = new java.util.ArrayList<Job>();
            }

            request.setAttribute("jobs", jobs);
            request.setAttribute("message", message);

            request.getRequestDispatcher("/jobs.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();

            throw new ServletException(
                "Unable to retrieve or display jobs. "
                + "Check the Eclipse Console for the database error.",
                e
            );
        }
    }
}
