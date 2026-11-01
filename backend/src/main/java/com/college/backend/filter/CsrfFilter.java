package com.college.backend.filter;

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
 * Filter that establishes centralized Cross-Site Request Forgery (CSRF) protection.
 * Reuses CsrfUtil to ensure tokens are provisioned on safe methods and verified
 * via constant-time comparison on mutating methods (POST, PUT, DELETE, PATCH).
 */
public class CsrfFilter implements Filter {

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

        String method = httpRequest.getMethod();

        // 1. Safe HTTP methods: seed or ensure CSRF token in session
        if (isSafeMethod(method)) {
            HttpSession session = httpRequest.getSession(true);
            CsrfUtil.getOrCreateToken(session);
            chain.doFilter(request, response);
            return;
        }

        // 2. State-changing HTTP methods: validate CSRF token
        if (!CsrfUtil.isValid(httpRequest)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Checks if the HTTP method is considered safe/idempotent and read-only.
     */
    public boolean isSafeMethod(String method) {
        if (method == null) {
            return false;
        }
        return "GET".equalsIgnoreCase(method) ||
               "HEAD".equalsIgnoreCase(method) ||
               "OPTIONS".equalsIgnoreCase(method) ||
               "TRACE".equalsIgnoreCase(method);
    }

    @Override
    public void destroy() {
        // No resources to release
    }
}
