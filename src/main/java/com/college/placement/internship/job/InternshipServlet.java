
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

    private InternshipDAO internshipDAO;

    @Override
    public void init() {
        internshipDAO = new InternshipDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("new".equals(action)) {
                request.getRequestDispatcher("/add-internship.jsp")
                        .forward(request, response);
                return;
            }

            if ("edit".equals(action) || "view".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Internship internship = internshipDAO.getInternshipById(id);

                if (internship == null) {
                    response.sendError(404, "Internship not found");
                    return;
                }

                request.setAttribute("internship", internship);

                String page = "edit".equals(action)
                        ? "/edit-internship.jsp"
                        : "/internship-details.jsp";

                request.getRequestDispatcher(page).forward(request, response);
                return;
            }

            if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                internshipDAO.deleteInternship(id);

                response.sendRedirect(request.getContextPath()
                        + "/internships?message=deleted");
                return;
            }

            request.setAttribute("internships",
                    internshipDAO.getAllInternships());
            request.setAttribute("message", request.getParameter("message"));

            request.getRequestDispatcher("/internships.jsp")
                    .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid internship ID");
        } catch (RuntimeException e) {
            throw new ServletException("Unable to load internships", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            String action = request.getParameter("action");
            Internship internship = new Internship();

            if ("update".equals(action)) {
                internship.setId(Integer.parseInt(request.getParameter("id")));
            }

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

            if ("update".equals(action)) {
                internshipDAO.updateInternship(internship);
                response.sendRedirect(request.getContextPath()
                        + "/internships?message=updated");
            } else {
                internshipDAO.addInternship(internship);
                response.sendRedirect(request.getContextPath()
                        + "/internships?message=added");
            }

        } catch (IllegalArgumentException e) {
            response.sendError(400,
                    "Invalid internship ID or deadline. Use YYYY-MM-DD.");
        } catch (RuntimeException e) {
            throw new ServletException("Unable to save internship", e);
        }
    }
}
