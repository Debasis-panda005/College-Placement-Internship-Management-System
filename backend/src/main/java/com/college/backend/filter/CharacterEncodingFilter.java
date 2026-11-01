package com.college.backend.filter;

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
 * Filter that establishes UTF-8 character encoding for all incoming HTTP requests
 * and outgoing responses. Must run first in the filter chain before request parameters
 * or form bodies are parsed.
 */
public class CharacterEncodingFilter implements Filter {

    public static final String DEFAULT_ENCODING = "UTF-8";

    private String encoding = DEFAULT_ENCODING;
    private boolean forceEncoding = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig != null) {
            String enc = filterConfig.getInitParameter("encoding");
            if (enc != null && !enc.trim().isEmpty()) {
                this.encoding = enc.trim();
            }
            String force = filterConfig.getInitParameter("forceEncoding");
            if (force != null && !force.trim().isEmpty()) {
                this.forceEncoding = Boolean.parseBoolean(force.trim());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            if (httpRequest.getCharacterEncoding() == null || forceEncoding) {
                httpRequest.setCharacterEncoding(encoding);
            }
        }

        if (response instanceof HttpServletResponse httpResponse) {
            if (!httpResponse.isCommitted()) {
                httpResponse.setCharacterEncoding(encoding);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isForceEncoding() {
        return forceEncoding;
    }

    public void setForceEncoding(boolean forceEncoding) {
        this.forceEncoding = forceEncoding;
    }
}
