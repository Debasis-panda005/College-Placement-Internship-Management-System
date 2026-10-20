
package com.collegeplacement.admin;

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
            request.setAttribute(
                    "totalJobs", adminDAO.getTotalJobs()
            );

            request.setAttribute(
                    "totalApplications",
                    adminDAO.getTotalApplications()
            );

        } catch (SQLException e) {
            getServletContext().log(
                    "Failed to load admin dashboard.", e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load dashboard statistics. "
                    + "Check the database connection."
            );
            return;
        }

        request.getRequestDispatcher("/admin/dashboard.jsp")
               .forward(request, response);
    }
}
