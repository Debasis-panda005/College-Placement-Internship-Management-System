package com.college.backend.servlet;

import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.util.CsrfUtil;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet handling user authentication and secure session initialization.
 * Provides login view on GET and validates credentials on POST.
 * Distinguishes internal students, external students, coordinators, and administrators.
 * Defends against session fixation, brute-force enumeration (generic errors),
 * CSRF attacks, and privilege escalation by binding roles strictly from DB records.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_EMAIL = "userEmail";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_ROLE = "userRole";

    private UserDAO userDAO;
    private ExternalStudentDAO externalStudentDAO;

    @Override
    public void init() {
        this.userDAO = new UserDAO();
        this.externalStudentDAO = new ExternalStudentDAO(this.userDAO);
    }

    // Setters for testing injection
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setExternalStudentDAO(ExternalStudentDAO externalStudentDAO) {
        this.externalStudentDAO = externalStudentDAO;
    }

    // =====================================================
    // GET: DISPLAY LOGIN FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure CSRF token is present in the session
        CsrfUtil.getOrCreateToken(request.getSession());

        // Extract flash messages from session
        String successMessage = (String) request.getSession().getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            request.getSession().removeAttribute("successMessage");
        }

        String errorMessage = (String) request.getSession().getAttribute("errorMessage");
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }

        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")
                .forward(request, response);
    }

    // =====================================================
    // POST: AUTHENTICATE USER & INITIALIZE SESSION
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce CSRF protection
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // 2. Extract credentials
        String email = trimToNull(request.getParameter("email"));
        String password = request.getParameter("password");

        // Preserve safe email input for redisplay upon error (NEVER preserve password)
        request.setAttribute("email", email);

        // 3. Validate required inputs
        if (email == null || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        // 4. Authenticate against database
        try {
            User user = userDAO.findByEmail(email);

            // Generic failure response prevents account enumeration
            if (user == null || !PasswordHashService.verifyPassword(password, user.getPasswordHash())) {
                request.setAttribute("errorMessage", "Invalid email or password.");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                return;
            }

            // 5. Account Type & Approval Status Validation
            String role = user.getRole();

            if (User.ROLE_STUDENT.equalsIgnoreCase(role)) {
                // Determine if this is an external student profile
                ExternalStudent extStudent = null;
                if (externalStudentDAO != null) {
                    extStudent = externalStudentDAO.findByUserId(user.getId());
                }

                if (extStudent != null) {
                    // EXTERNAL STUDENT: enforce verification status
                    boolean isPending = ExternalStudent.STATUS_PENDING.equalsIgnoreCase(extStudent.getVerificationStatus())
                            || User.STATUS_PENDING.equalsIgnoreCase(user.getStatus())
                            || User.STATUS_PENDING_APPROVAL.equalsIgnoreCase(user.getStatus());

                    boolean isRejected = ExternalStudent.STATUS_REJECTED.equalsIgnoreCase(extStudent.getVerificationStatus())
                            || User.STATUS_REJECTED.equalsIgnoreCase(user.getStatus());

                    if (isPending) {
                        request.setAttribute("errorMessage",
                                "Your student registration is pending verification. Please contact the administrator or check back once approved.");
                        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                        return;
                    }

                    if (isRejected) {
                        request.setAttribute("errorMessage",
                                "Your registration request has been rejected. Please contact the placement administrator.");
                        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                        return;
                    }

                    // Strict verification: Both external student profile and user status must be verified as APPROVED
                    if (!ExternalStudent.STATUS_APPROVED.equalsIgnoreCase(extStudent.getVerificationStatus())
                            || !User.STATUS_APPROVED.equalsIgnoreCase(user.getStatus())) {
                        LOGGER.log(Level.WARNING, "External student account {0} cannot be verified as approved (user status={1}, ext status={2})",
                                new Object[]{user.getEmail(), user.getStatus(), extStudent.getVerificationStatus()});
                        request.setAttribute("errorMessage",
                                "Your registration status cannot be verified as approved. Please contact the placement administrator.");
                        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                        return;
                    }

                    // Approved External Student -> establish session and redirect to /external/dashboard
                    HttpSession session = establishAuthenticatedSession(request, user);
                    session.setAttribute("studentType", "EXTERNAL");
                    session.setAttribute("externalStudentId", extStudent.getId());
                    response.sendRedirect(request.getContextPath() + "/external/dashboard");
                    return;

                } else {
                    // INTERNAL STUDENT: Centurion University student (never requires an external student profile)
                    boolean isPending = User.STATUS_PENDING_APPROVAL.equalsIgnoreCase(user.getStatus())
                            || User.STATUS_PENDING.equalsIgnoreCase(user.getStatus());

                    boolean isRejected = User.STATUS_REJECTED.equalsIgnoreCase(user.getStatus());

                    if (isPending) {
                        request.setAttribute("errorMessage",
                                "Your student registration is pending verification. Please contact the administrator or check back once approved.");
                        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                        return;
                    }

                    if (isRejected) {
                        request.setAttribute("errorMessage",
                                "Your registration request has been rejected. Please contact the placement administrator.");
                        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                        return;
                    }

                    // Strict verification: Internal student user status must be verified as APPROVED
                    if (!User.STATUS_APPROVED.equalsIgnoreCase(user.getStatus())) {
                        LOGGER.log(Level.WARNING, "Internal student account {0} cannot be verified as approved (user status={1})",
                                new Object[]{user.getEmail(), user.getStatus()});
                        request.setAttribute("errorMessage",
                                "Your registration status cannot be verified as approved. Please contact the placement administrator.");
                        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                        return;
                    }

                    // Approved Internal Student -> establish session and redirect to /jobs
                    HttpSession session = establishAuthenticatedSession(request, user);
                    session.setAttribute("studentType", "INTERNAL");
                    response.sendRedirect(request.getContextPath() + "/jobs");
                    return;
                }

            } else if (User.ROLE_COORDINATOR.equalsIgnoreCase(role)) {
                // COORDINATOR: enforce coordinator approval status (never requires a student profile)
                boolean isPending = User.STATUS_PENDING_APPROVAL.equalsIgnoreCase(user.getStatus())
                        || User.STATUS_PENDING.equalsIgnoreCase(user.getStatus());

                boolean isRejected = User.STATUS_REJECTED.equalsIgnoreCase(user.getStatus());

                if (isPending) {
                    request.setAttribute("errorMessage",
                            "Your coordinator registration request is pending administrator approval. Please contact the administrator or check back once approved.");
                    request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                    return;
                }

                if (isRejected) {
                    request.setAttribute("errorMessage",
                            "Your coordinator registration request has been rejected. Please contact the placement administrator.");
                    request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                    return;
                }

                // Strict verification: Coordinator status must be verified as APPROVED
                if (!User.STATUS_APPROVED.equalsIgnoreCase(user.getStatus())) {
                    LOGGER.log(Level.WARNING, "Coordinator account {0} cannot be verified as approved (user status={1})",
                            new Object[]{user.getEmail(), user.getStatus()});
                    request.setAttribute("errorMessage",
                            "Your coordinator registration request is pending administrator approval. Please contact the administrator or check back once approved.");
                    request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                    return;
                }

                // Approved Coordinator -> establish session and redirect to /jobs
                establishAuthenticatedSession(request, user);
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;

            } else {
                // ADMINISTRATOR or other authorized system roles (never requires a student profile)
                if (user.getStatus() != null && !User.STATUS_APPROVED.equalsIgnoreCase(user.getStatus())) {
                    LOGGER.log(Level.WARNING, "Administrative account {0} cannot be verified as approved (user status={1})",
                            new Object[]{user.getEmail(), user.getStatus()});
                    request.setAttribute("errorMessage",
                            "Your administrator account is not approved or active. Please contact the system administrator.");
                    request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                    return;
                }

                establishAuthenticatedSession(request, user);
                response.sendRedirect(request.getContextPath() + "/jobs");
                return;
            }

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Database error during login authentication for email: " + email, e);
            request.setAttribute("errorMessage", "A database error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during login authentication for email: " + email, e);
            request.setAttribute("errorMessage", "An unexpected error occurred during login. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }

    /**
     * Session Fixation Protection: Invalidates old session, establishes a fresh authenticated session,
     * generates a new CSRF token, and binds database-enforced identity attributes.
     */
    private HttpSession establishAuthenticatedSession(HttpServletRequest request, User user) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        CsrfUtil.getOrCreateToken(newSession);

        newSession.setAttribute(SESSION_USER_ID, user.getId());
        newSession.setAttribute(SESSION_USER_EMAIL, user.getEmail());
        newSession.setAttribute(SESSION_USER_NAME, user.getName());
        newSession.setAttribute(SESSION_USER_ROLE, user.getRole());

        return newSession;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
