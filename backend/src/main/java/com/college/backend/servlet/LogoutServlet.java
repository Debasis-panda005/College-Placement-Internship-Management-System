package com.college.backend.servlet;

import com.college.backend.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet handling user logout and secure session invalidation.
 * Destroys authentication credentials stored in the HTTP session and redirects to login.
 * Enforces CSRF token validation on POST requests to prevent logout CSRF attacks.
 * Rejects GET requests with HTTP 405 Method Not Allowed to prevent state changes via unsafe HTTP methods.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Rejects GET requests to prevent logout CSRF and accidental session termination by crawlers.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "HTTP method GET is not supported for logout. Use POST.");
    }

    /**
     * Authenticates CSRF token and invalidates active session.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Enforce CSRF protection on state-changing POST logout
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        processLogout(request, response);
    }

    /**
     * Terminates the current authenticated session, clears attributes, and redirects to login.
     */
    private void processLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Establish a clean session for flash notification
        HttpSession newSession = request.getSession(true);
        CsrfUtil.getOrCreateToken(newSession);
        newSession.setAttribute("successMessage", "You have been logged out successfully.");

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
