package com.college.backend.util;

import com.college.backend.entity.User;
import com.college.backend.servlet.LoginServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Reusable utility class for session-based role and authentication checks.
 * Uses the authenticated session attributes established by LoginServlet.
 * Never trusts request parameters or client-supplied headers for authorization.
 */
public final class AuthUtil {

    private AuthUtil() {
        // Prevent instantiation
    }

    /**
     * Extracts the authenticated user's ID from the active session.
     *
     * @param request the HTTP request
     * @return the user ID, or null if unauthenticated
     */
    public static Long getSessionUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute(LoginServlet.SESSION_USER_ID);
    }

    /**
     * Extracts the authenticated user's role from the active session.
     *
     * @param request the HTTP request
     * @return the user role string, or null if unauthenticated
     */
    public static String getSessionUserRole(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE);
    }

    /**
     * Checks if the request comes from an authenticated user.
     *
     * @param request the HTTP request
     * @return true if an authenticated session exists with a valid user ID
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return getSessionUserId(request) != null;
    }

    /**
     * Checks if the authenticated user has the STUDENT role.
     *
     * @param request the HTTP request
     * @return true if authenticated as a student
     */
    public static boolean isStudent(HttpServletRequest request) {
        return isAuthenticated(request) && User.ROLE_STUDENT.equals(getSessionUserRole(request));
    }

    /**
     * Checks if the authenticated user has the COORDINATOR role.
     *
     * @param request the HTTP request
     * @return true if authenticated as a coordinator
     */
    public static boolean isCoordinator(HttpServletRequest request) {
        return isAuthenticated(request) && User.ROLE_COORDINATOR.equals(getSessionUserRole(request));
    }

    /**
     * Checks if the authenticated user has the ADMIN role.
     *
     * @param request the HTTP request
     * @return true if authenticated as an administrator
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return isAuthenticated(request) && User.ROLE_ADMIN.equals(getSessionUserRole(request));
    }

    /**
     * Checks if the authenticated user has either the COORDINATOR or ADMIN role.
     *
     * @param request the HTTP request
     * @return true if authenticated as coordinator or admin
     */
    public static boolean isCoordinatorOrAdmin(HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return false;
        }
        String role = getSessionUserRole(request);
        return User.ROLE_COORDINATOR.equals(role) || User.ROLE_ADMIN.equals(role);
    }

    /**
     * Checks if the authenticated user has any of the specified roles.
     *
     * @param request the HTTP request
     * @param allowedRoles the roles permitted
     * @return true if authenticated with one of the allowed roles
     */
    public static boolean hasAnyRole(HttpServletRequest request, String... allowedRoles) {
        if (!isAuthenticated(request) || allowedRoles == null) {
            return false;
        }
        String currentRole = getSessionUserRole(request);
        if (currentRole == null) {
            return false;
        }
        for (String role : allowedRoles) {
            if (currentRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
