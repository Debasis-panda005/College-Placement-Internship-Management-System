
package com.college.placement.internship.job;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;

@WebServlet("/internships")
public class InternshipServlet extends HttpServlet {

    private final InternshipDAO internshipDAO = new InternshipDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("new".equals(action)) {
            request.getRequestDispatcher("/add-internship.jsp")
                    .forward(request, response);
            return;
        }

        if ("edit".equals(action)) {
            showEditForm(request, response);
            return;
        }

        if ("view".equals(action)) {
            showDetails(request, response);
            return;
        }

        if ("delete".equals(action)) {
            deleteInternship(request, response);
            return;
        }

        request.setAttribute("internships",
                internshipDAO.getAllInternships());

        request.getRequestDispatcher("/internships.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            Internship internship = readInternship(request);

            if ("update".equals(action)) {
                internship.setId(Integer.parseInt(
                        request.getParameter("id")));

                if (internshipDAO.updateInternship(internship)) {
                    response.sendRedirect(request.getContextPath()
                            + "/internships?message=updated");
                } else {
                    response.sendRedirect(request.getContextPath()
                            + "/internships?error=update");
                }

            } else {
                if (internshipDAO.addInternship(internship)) {
                    response.sendRedirect(request.getContextPath()
                            + "/internships?message=added");
                } else {
                    response.sendRedirect(request.getContextPath()
                            + "/internships?error=add");
                }
            }

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid internship details or date.");
        }
    }

    private Internship readInternship(HttpServletRequest request) {
        Internship internship = new Internship();

        internship.setTitle(request.getParameter("title"));
        internship.setCompany(request.getParameter("company"));
        internship.setLocation(request.getParameter("location"));
        internship.setDescription(request.getParameter("description"));
        internship.setStipend(request.getParameter("stipend"));
        internship.setDuration(request.getParameter("duration"));
        internship.setEligibility(request.getParameter("eligibility"));

        String deadline = request.getParameter("applicationDeadline");
        if (deadline != null && !deadline.isBlank()) {
            internship.setApplicationDeadline(Date.valueOf(deadline));
        }

        String status = request.getParameter("status");
        internship.setStatus(
                status == null || status.isBlank() ? "Active" : status);

        return internship;
    }

    private void showEditForm(HttpServletRequest request,
                              HttpServletResponse response)
            throws ServletException, IOException {

        Internship internship = getInternshipFromRequest(request);

        if (internship == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Internship not found.");
            return;
        }

        request.setAttribute("internship", internship);
        request.getRequestDispatcher("/edit-internship.jsp")
                .forward(request, response);
    }

    private void showDetails(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {

        Internship internship = getInternshipFromRequest(request);

        if (internship == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Internship not found.");
            return;
        }

        request.setAttribute("internship", internship);
        request.getRequestDispatcher("/internship-details.jsp")
                .forward(request, response);
    }

    private void deleteInternship(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            if (internshipDAO.deleteInternship(id)) {
                response.sendRedirect(request.getContextPath()
                        + "/internships?message=deleted");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/internships?error=delete");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid internship ID.");
        }
    }

    private Internship getInternshipFromRequest(
            HttpServletRequest request) {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            return internshipDAO.getInternshipById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
