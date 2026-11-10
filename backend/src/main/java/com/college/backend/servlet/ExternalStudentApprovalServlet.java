package com.college.backend.servlet;

import com.college.backend.dao.ExternalStudentAuditLogDAO;
import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.ExternalStudentAuditLog;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.util.AuthUtil;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Administrative controller for verifying, approving, and rejecting external student registrations.
 * Strictly restricts approval mutations to administrators (or authorized coordinators).
 * Maintains audit trail and transactional state transitions.
 */
@WebServlet({"/admin/external-students", "/admin/external-students/*"})
public class ExternalStudentApprovalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO;
    private ExternalStudentDAO externalStudentDAO;
    private ExternalStudentAuditLogDAO auditLogDAO;

    // Configurable coordinator approval permission (default: false, only ADMIN can approve/reject)
    private static volatile boolean coordinatorApprovalAllowed = false;

    @Override
    public void init() {
        this.userDAO = new UserDAO();
        this.externalStudentDAO = new ExternalStudentDAO(this.userDAO);
        this.auditLogDAO = new ExternalStudentAuditLogDAO();
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setExternalStudentDAO(ExternalStudentDAO externalStudentDAO) {
        this.externalStudentDAO = externalStudentDAO;
    }

    public void setAuditLogDAO(ExternalStudentAuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    public static void setCoordinatorApprovalAllowed(boolean allowed) {
        coordinatorApprovalAllowed = allowed;
    }

    public static boolean isCoordinatorApprovalAllowed() {
        return coordinatorApprovalAllowed;
    }

    // =====================================================
    // GET: VIEW EXTERNAL STUDENTS / DETAILS
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!AuthUtil.isAuthenticated(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = AuthUtil.getSessionUserRole(request);
        if (!User.ROLE_ADMIN.equals(role) && !User.ROLE_COORDINATOR.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrative privileges required.");
            return;
        }

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if ("details".equalsIgnoreCase(action) && idParam != null) {
            try {
                Long studentId = Long.parseLong(idParam.trim());
                ExternalStudent student = externalStudentDAO.findById(studentId);
                if (student == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "External student profile not found.");
                    return;
                }

                List<ExternalStudentAuditLog> auditLogs = auditLogDAO.getAuditLogsForStudent(studentId);
                request.setAttribute("student", student);
                request.setAttribute("auditLogs", auditLogs);
                request.getRequestDispatcher("/WEB-INF/jsp/admin-external-student-details.jsp").forward(request, response);
                return;
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid external student ID.");
                return;
            }
        }

        // Default: redirect to the admin dashboard external student section
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    // =====================================================
    // POST: APPROVE OR REJECT EXTERNAL STUDENT
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Session authentication check
        if (!AuthUtil.isAuthenticated(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        // 2. Role authorization check
        String role = AuthUtil.getSessionUserRole(request);
        boolean canApprove = User.ROLE_ADMIN.equals(role) || (User.ROLE_COORDINATOR.equals(role) && coordinatorApprovalAllowed);

        if (!canApprove) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied: Only administrators have authority to approve or reject external student registrations.");
            return;
        }

        // 3. CSRF token validation
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // 4. Parameter extraction
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");
        String adminNotes = request.getParameter("adminNotes");

        if (idParam == null || idParam.trim().isEmpty()) {
            setFlashAndRedirect(request, response, "External student ID is required.", false);
            return;
        }

        Long externalStudentId;
        try {
            externalStudentId = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            setFlashAndRedirect(request, response, "Invalid student ID format.", false);
            return;
        }

        Long adminUserId = AuthUtil.getSessionUserId(request);

        // 5. Load and inspect current student record
        ExternalStudent student;
        try {
            student = externalStudentDAO.findById(externalStudentId);
        } catch (DAOException e) {
            setFlashAndRedirect(request, response, "Database error retrieving external student record.", false);
            return;
        }

        if (student == null) {
            setFlashAndRedirect(request, response, "External student registration not found.", false);
            return;
        }

        // Idempotency: verify current status is PENDING
        if (!ExternalStudent.STATUS_PENDING.equalsIgnoreCase(student.getVerificationStatus())) {
            setFlashAndRedirect(request, response,
                    "This registration request has already been processed with status: " + student.getVerificationStatus(),
                    false);
            return;
        }

        // 6. Execute requested status transition
        if ("approve".equalsIgnoreCase(action)) {
            try {
                boolean updated = externalStudentDAO.updateVerificationStatus(
                        externalStudentId,
                        ExternalStudent.STATUS_APPROVED,
                        adminUserId,
                        adminNotes != null ? adminNotes.trim() : "Approved by administrator"
                );

                if (updated) {
                    setFlashAndRedirect(request, response,
                            "External student registration for '" + student.getApplicantName() + "' has been approved successfully.",
                            true);
                } else {
                    setFlashAndRedirect(request, response, "Failed to update external student status.", false);
                }
            } catch (DAOException e) {
                setFlashAndRedirect(request, response, "Database error processing approval: " + e.getMessage(), false);
            }

        } else if ("reject".equalsIgnoreCase(action)) {
            if (adminNotes == null || adminNotes.trim().isEmpty()) {
                setFlashAndRedirect(request, response, "A rejection reason is required to reject an external registration.", false);
                return;
            }

            try {
                boolean updated = externalStudentDAO.updateVerificationStatus(
                        externalStudentId,
                        ExternalStudent.STATUS_REJECTED,
                        adminUserId,
                        adminNotes.trim()
                );

                if (updated) {
                    setFlashAndRedirect(request, response,
                            "External student registration for '" + student.getApplicantName() + "' has been rejected.",
                            true);
                } else {
                    setFlashAndRedirect(request, response, "Failed to update external student status.", false);
                }
            } catch (DAOException e) {
                setFlashAndRedirect(request, response, "Database error processing rejection: " + e.getMessage(), false);
            }

        } else {
            setFlashAndRedirect(request, response, "Unrecognized action: " + action, false);
        }
    }

    private void setFlashAndRedirect(HttpServletRequest request, HttpServletResponse response, String message, boolean success)
            throws IOException {
        HttpSession session = request.getSession(true);
        if (success) {
            session.setAttribute("successMessage", message);
        } else {
            session.setAttribute("errorMessage", message);
        }

        String redirectUrl = request.getContextPath() + "/admin/dashboard#external-students";
        response.sendRedirect(redirectUrl);
    }
}
