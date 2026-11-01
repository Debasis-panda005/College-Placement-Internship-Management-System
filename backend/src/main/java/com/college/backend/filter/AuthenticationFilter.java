package com.college.backend.filter;

import com.college.backend.util.AuthUtil;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter that centralizes authentication verification for protected application routes.
 * Reuses AuthUtil without introducing a secondary authentication mechanism.
 * Allows unauthenticated access to public pages and static assets, while redirecting
 * unauthorized visitors to the login page with contextual flash messages.
 */
public class AuthenticationFilter implements Filter {

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

        String path = getNormalizedPath(httpRequest);

        // 1. Allow unauthenticated access to public resources and static assets
        if (isPublicResource(httpRequest, path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Verify authentication via existing AuthUtil session inspection
        if (AuthUtil.isAuthenticated(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 3. User is unauthenticated attempting to access a protected route
        // Guard against redirect loops
        if ("/login".equalsIgnoreCase(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Initialize session and CSRF token for the flash message
        HttpSession session = httpRequest.getSession(true);
        CsrfUtil.getOrCreateToken(session);

        // Set contextual error flash message consistent with existing servlets
        if (path.startsWith("/admin")) {
            session.setAttribute("errorMessage", "You must log in to view the admin dashboard.");
        } else if (path.startsWith("/applications")) {
            session.setAttribute("errorMessage", "You must log in to view applications.");
        } else if (path.startsWith("/jobs") && "new".equalsIgnoreCase(httpRequest.getParameter("action"))) {
            session.setAttribute("errorMessage", "You must log in to add jobs.");
        } else if (path.startsWith("/jobs") && "edit".equalsIgnoreCase(httpRequest.getParameter("action"))) {
            session.setAttribute("errorMessage", "You must log in to edit jobs.");
        } else {
            session.setAttribute("errorMessage", "You must log in to access this page.");
        }

        httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
    }

    /**
     * Determines whether an incoming request targets a publicly accessible resource.
     *
     * @param request the HTTP servlet request
     * @param path    the context-relative request path
     * @return true if the resource is public, false if it requires authentication
     */
    public boolean isPublicResource(HttpServletRequest request, String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return true;
        }

        // Static files and asset directories
        if (path.startsWith("/assets/") || path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/images/") || path.equals("/favicon.ico")) {
            return true;
        }

        // Common static file extensions
        String lower = path.toLowerCase();
        if (lower.endsWith(".css") || lower.endsWith(".js") || lower.endsWith(".png") ||
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".svg") ||
            lower.endsWith(".gif") || lower.endsWith(".ico") || lower.endsWith(".woff") ||
            lower.endsWith(".woff2") || lower.endsWith(".ttf") || lower.endsWith(".map")) {
            return true;
        }

        // Public portal pages
        if (path.equals("/home") || path.equals("/index") || path.equals("/index.jsp")) {
            return true;
        }

        // Authentication & registration endpoints
        if (path.equals("/login") || path.equals("/register")) {
            return true;
        }

        // Logout is always permitted to clear session
        if (path.equals("/logout")) {
            return true;
        }

        // Public browsing of job and internship listings (GET requests only)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if (path.equals("/jobs") || path.startsWith("/jobs/") ||
                path.equals("/internships") || path.startsWith("/internships/")) {
                String action = request.getParameter("action");
                // "new" (add job) and "edit" (edit job) require coordinator/admin login
                if ("new".equalsIgnoreCase(action) || "edit".equalsIgnoreCase(action)) {
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Normalizes the context-relative path by stripping path parameters and query strings.
     */
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
