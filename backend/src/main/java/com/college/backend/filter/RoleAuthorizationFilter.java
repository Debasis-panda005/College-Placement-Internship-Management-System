package com.college.backend.filter;

import com.college.backend.entity.User;
import com.college.backend.util.AuthUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter that enforces role-based access control (RBAC) across protected endpoints.
 * Distinguishes privileges among STUDENT, COORDINATOR, and ADMIN roles.
 * Blocks unauthorized role operations with HTTP 403 Forbidden.
 */
public class RoleAuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No specific initialization required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) ||
            !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // Only enforce role checks on authenticated sessions
        if (!AuthUtil.isAuthenticated(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String role = AuthUtil.getSessionUserRole(httpRequest);
        if (role == null || (!User.ROLE_STUDENT.equals(role) &&
                             !User.ROLE_COORDINATOR.equals(role) &&
                             !User.ROLE_ADMIN.equals(role))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Unrecognized user role.");
            return;
        }

        String path = getNormalizedPath(httpRequest);
        String method = httpRequest.getMethod();
        String action = httpRequest.getParameter("action");

        // 1. ADMIN / COORDINATOR ONLY RESTRICTIONS
        if (path.startsWith("/admin")) {
            if (User.ROLE_STUDENT.equals(role)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access Denied: Administrator or Coordinator privileges required.");
                return;
            }
        }

        // Job Management (creation, editing, deleting, toggling status)
        if (path.equals("/jobs") || path.startsWith("/jobs/") ||
            path.equals("/internships") || path.startsWith("/internships/")) {
            boolean isMutating = !"GET".equalsIgnoreCase(method);
            boolean isFormView = "new".equalsIgnoreCase(action) || "edit".equalsIgnoreCase(action);
            if (isMutating || isFormView) {
                if (User.ROLE_STUDENT.equals(role)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Access Denied: Only coordinators and administrators can manage jobs.");
                    return;
                }
            }
        }

        // Interview Management (scheduling, updates, cancellations via POST)
        if (path.equals("/interviews") || path.startsWith("/interviews/")) {
            if (!"GET".equalsIgnoreCase(method)) {
                if (User.ROLE_STUDENT.equals(role)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Access Denied: Only coordinators and administrators can schedule or manage interviews.");
                    return;
                }
            }
        }

        // Dynamic Interview Rounds Management (creating, updating, completing, rescheduling, decisions via POST)
        if (path.equals("/interview-rounds") || path.startsWith("/interview-rounds/")) {
            if (!"GET".equalsIgnoreCase(method)) {
                if (User.ROLE_STUDENT.equals(role)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Access Denied: Only coordinators and administrators can manage interview rounds.");
                    return;
                }
            }
        }

        // Candidate Shortlisting and Status Decisions
        if (path.equals("/applications") || path.startsWith("/applications/") || path.equals("/shortlist")) {
            if (!"GET".equalsIgnoreCase(method)) {
                boolean isCoordinatorAction = "shortlist".equalsIgnoreCase(action) ||
                                              "select".equalsIgnoreCase(action) ||
                                              "reject".equalsIgnoreCase(action) ||
                                              "delete".equalsIgnoreCase(action) ||
                                              "status".equalsIgnoreCase(action);
                if (isCoordinatorAction) {
                    if (User.ROLE_STUDENT.equals(role)) {
                        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Access Denied: Only coordinators and administrators can perform candidate decisions.");
                        return;
                    }
                }

                // 2. STUDENT ONLY RESTRICTIONS: Application submission
                boolean isApplyAction = "apply".equalsIgnoreCase(action) || (action == null && httpRequest.getParameter("jobId") != null);
                if (isApplyAction) {
                    if (!User.ROLE_STUDENT.equals(role)) {
                        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Access Denied: Only students can submit job applications.");
                        return;
                    }
                }
            }
        }

        // All checks passed; proceed with filter chain
        chain.doFilter(request, response);
    }

    public String getNormalizedPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        int semicolonIndex = path.indexOf(';');
        if (semicolonIndex != -1) {
            path = path.substring(0, semicolonIndex);
        }
        int questionIndex = path.indexOf('?');
        if (questionIndex != -1) {
            path = path.substring(0, questionIndex);
        }
        return path;
    }

    @Override
    public void destroy() {
        // No resources to release
    }
}
