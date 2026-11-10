package com.college.backend.servlet;

import com.college.backend.dao.CoordinatorDAO;
import com.college.backend.entity.Coordinator;
import com.college.backend.exception.DAOException;
import com.college.backend.util.AuthUtil;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Administrative controller for approving or rejecting coordinator registration requests.
 * Restricted strictly to authenticated users with ADMIN role.
 * Unauthorized users (including Students and non-admin Coordinators) are rejected with HTTP 403 Forbidden.
 */
@WebServlet("/admin/coordinators")
public class CoordinatorApprovalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private CoordinatorDAO coordinatorDAO;

    @Override
    public void init() {
        this.coordinatorDAO = new CoordinatorDAO();
    }

    public void setCoordinatorDAO(CoordinatorDAO coordinatorDAO) {
        this.coordinatorDAO = coordinatorDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Enforce authentication
        if (!AuthUtil.isAuthenticated(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Strictly enforce ADMIN role for coordinator approval management
        if (!AuthUtil.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied: Only administrators can view and manage coordinator approval workflows.");
            return;
        }

        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

        if ("details".equalsIgnoreCase(action) && idStr != null) {
            try {
                Long id = Long.parseLong(idStr);
                Coordinator coordinator = coordinatorDAO.findById(id);
                if (coordinator != null) {
                    request.setAttribute("coordinator", coordinator);
                } else {
                    request.setAttribute("errorMessage", "Coordinator request not found.");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "Invalid coordinator ID.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce CSRF protection
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // 2. Enforce Authentication
        if (!AuthUtil.isAuthenticated(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 3. Strictly enforce ADMIN authorization
        if (!AuthUtil.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied: Only administrators can approve or reject coordinator registration requests.");
            return;
        }

        // 4. Extract parameters
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");
        String adminNotes = request.getParameter("adminNotes");
        Long adminUserId = AuthUtil.getSessionUserId(request);

        if (idStr == null || idStr.trim().isEmpty() || action == null || action.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Missing request parameters for coordinator approval.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        Long coordinatorId;
        try {
            coordinatorId = Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Invalid coordinator ID format.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // 5. Execute Approval or Rejection
        try {
            if ("approve".equalsIgnoreCase(action.trim())) {
                boolean updated = coordinatorDAO.updateStatus(coordinatorId, Coordinator.STATUS_APPROVED, adminUserId, adminNotes);
                if (updated) {
                    request.getSession().setAttribute("successMessage",
                            "Coordinator registration request approved successfully. The coordinator account is now active.");
                } else {
                    request.getSession().setAttribute("errorMessage", "Coordinator request could not be found or updated.");
                }
            } else if ("reject".equalsIgnoreCase(action.trim())) {
                boolean updated = coordinatorDAO.updateStatus(coordinatorId, Coordinator.STATUS_REJECTED, adminUserId, adminNotes);
                if (updated) {
                    request.getSession().setAttribute("successMessage",
                            "Coordinator registration request rejected.");
                } else {
                    request.getSession().setAttribute("errorMessage", "Coordinator request could not be found or updated.");
                }
            } else {
                request.getSession().setAttribute("errorMessage", "Unsupported action: " + action);
            }
        } catch (DAOException e) {
            request.getSession().setAttribute("errorMessage", "A database error occurred while updating coordinator status.");
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}
