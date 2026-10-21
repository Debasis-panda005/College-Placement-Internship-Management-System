package com.college.backend.servlet;

import com.college.backend.dao.UserDAO;
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

/**
 * Servlet handling user authentication and secure session initialization.
 * Provides login view on GET and validates credentials on POST.
 * Defends against session fixation, brute-force enumeration (generic errors),
 * CSRF attacks, and privilege escalation by binding roles strictly from DB records.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_EMAIL = "userEmail";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_ROLE = "userRole";

    private UserDAO userDAO;

    @Override
    public void init() {
        this.userDAO = new UserDAO();
    }

    // Setter for testing injection
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
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

            // 5. Session Fixation Protection: Invalidate old session and create a fresh one
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);

            // Generate a fresh CSRF token for the authenticated session
            CsrfUtil.getOrCreateToken(newSession);

            // 6. Store authenticated identity and database-enforced role in session
            newSession.setAttribute(SESSION_USER_ID, user.getId());
            newSession.setAttribute(SESSION_USER_EMAIL, user.getEmail());
            newSession.setAttribute(SESSION_USER_NAME, user.getName());
            newSession.setAttribute(SESSION_USER_ROLE, user.getRole());

            // 7. Post/Redirect/Get pattern to jobs dashboard
            response.sendRedirect(request.getContextPath() + "/jobs");

        } catch (DAOException e) {
            // Shield internal database details
            request.setAttribute("errorMessage", "A database error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "An unexpected error occurred during login. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
