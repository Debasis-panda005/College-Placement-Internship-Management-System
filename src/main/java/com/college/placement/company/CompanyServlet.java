
package com.college.placement.company;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/companies")
public class CompanyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private CompanyDAO companyDAO;

    @Override
    public void init() {
        companyDAO = new CompanyDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            String action = request.getParameter("action");

            if ("edit".equals(action)) {
                int id = parseId(request.getParameter("id"));

                if (id <= 0) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid company ID.");
                    return;
                }

                Company company = companyDAO.getCompanyById(id);

                if (company == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Company not found.");
                    return;
                }

                request.setAttribute("editCompany", company);
            }

            List<Company> companies = companyDAO.getAllCompanies();
            request.setAttribute("companies", companies);

            request.getRequestDispatcher("/companies.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log("Unable to load companies.", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load company records. Check the database connection."
            );
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid company ID.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("delete".equals(action)) {
                int id = parseId(request.getParameter("id"));

                if (id <= 0) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid company ID.");
                    return;
                }

                companyDAO.deleteCompany(id);

            } else if ("save".equals(action)) {
                String name = trim(request.getParameter("name"));
                String email = trim(request.getParameter("email"));
                String phone = trim(request.getParameter("phone"));
                String location = trim(request.getParameter("location"));
                String website = trim(request.getParameter("website"));
                String idValue = trim(request.getParameter("id"));

                if (name.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Company name is required.");
                    return;
                }

                if (name.length() > 150
                        || email.length() > 150
                        || phone.length() > 30
                        || location.length() > 150
                        || website.length() > 255) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "One or more fields exceed the allowed length.");
                    return;
                }

                Company company = new Company();
                company.setName(name);
                company.setEmail(emptyToNull(email));
                company.setPhone(emptyToNull(phone));
                company.setLocation(emptyToNull(location));
                company.setWebsite(emptyToNull(website));

                if (idValue.isEmpty()) {
                    companyDAO.addCompany(company);
                } else {
                    int id = parseId(idValue);

                    if (id <= 0) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                "Invalid company ID.");
                        return;
                    }

                    company.setId(id);

                    if (!companyDAO.updateCompany(company)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                "Company not found.");
                        return;
                    }
                }

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Unknown company action.");
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/companies?success=1"
            );

        } catch (SQLException e) {
            getServletContext().log("Unable to save company changes.", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save company changes. Check the server logs."
            );
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid company ID.");
        }
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID.", e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
