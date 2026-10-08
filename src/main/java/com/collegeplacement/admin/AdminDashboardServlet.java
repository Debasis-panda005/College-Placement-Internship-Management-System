package com.collegeplacement.admin;

import java.io.IOException;

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

        int totalJobs = adminDAO.getTotalJobs();
        int totalApplications = adminDAO.getTotalApplications();

        request.setAttribute("totalJobs", totalJobs);
        request.setAttribute("totalApplications", totalApplications);

        request.getRequestDispatcher("/admin/dashboard.jsp")
               .forward(request, response);
    }
}