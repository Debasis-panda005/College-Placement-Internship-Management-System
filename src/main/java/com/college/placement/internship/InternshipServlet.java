
package com.college.placement.internship;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/internships")
public class InternshipServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private InternshipDAO dao;

    @Override
    public void init() throws ServletException {
        dao = new InternshipDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("internships", dao.getAllInternships());

            request.getRequestDispatcher("/admin/internships.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log("Unable to load internships.", e);
            throw new ServletException("Unable to load internships.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {

                String title = clean(request.getParameter("title"));
                String company = clean(request.getParameter("company"));

                if (title == null || company == null) {
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Title and company are required."
                    );
                    return;
                }

                String status = clean(request.getParameter("status"));

                if (status == null) {
                    status = "Active";
                }

                if (!"Active".equals(status)
                        && !"Inactive".equals(status)) {
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid internship status."
                    );
                    return;
                }

                Internship internship = new Internship();

                internship.setTitle(title);
                internship.setCompany(company);
                internship.setLocation(
                    clean(request.getParameter("location"))
                );
                internship.setDescription(
                    clean(request.getParameter("description"))
                );
                internship.setStipend(
                    clean(request.getParameter("stipend"))
                );
                internship.setDuration(
                    clean(request.getParameter("duration"))
                );
                internship.setStatus(status);

                dao.addInternship(internship);

            } else if ("delete".equals(action)) {

                int id;

                try {
                    id = Integer.parseInt(request.getParameter("id"));
                } catch (NumberFormatException e) {
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid internship ID."
                    );
                    return;
                }

                if (id <= 0 || !dao.deleteInternship(id)) {
                    response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Internship not found."
                    );
                    return;
                }

            } else {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action."
                );
                return;
            }

            response.sendRedirect(
                request.getContextPath() + "/internships"
            );

        } catch (SQLException e) {
            getServletContext().log("Unable to update internships.", e);
            throw new ServletException(
                "Unable to save or delete the internship.", e
            );
        }
    }

    private String clean(String value) {
        return value == null || value.trim().isEmpty()
                ? null : value.trim();
    }
}
