
package com.college.placement.admin;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private AdminDAO adminDAO;

    @Override
    public void init() throws ServletException {
        adminDAO = new AdminDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int totalJobs = adminDAO.getTotalJobs();
            int totalApplications =
                    adminDAO.getTotalApplications();

            request.setAttribute("totalJobs", totalJobs);
            request.setAttribute(
                "totalApplications", totalApplications
            );

            request.getRequestDispatcher("/admin/dashboard.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log(
                "Failed to load admin dashboard statistics.", e
            );

            throw new ServletException(
                "Unable to load dashboard statistics. "
                + "Check the database connection and server log.",
                e
            );
        }
    }
}
