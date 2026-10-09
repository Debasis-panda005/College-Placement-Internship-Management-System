package com.college.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating, storing, and validating CSRF tokens
 * for session-based CSRF protection.
 */
public final class CsrfUtil {

    public static final String CSRF_TOKEN_SESSION_ATTR = "csrfToken";
    public static final String CSRF_TOKEN_PARAM = "csrfToken";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    private CsrfUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Generates a cryptographically secure, URL-safe random token.
     *
     * @return a Base64 URL-encoded token without padding
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Retrieves the CSRF token from the session, or generates and stores a new one if absent.
     *
     * @param session the HTTP session
     * @return the CSRF token string, or null if session is null
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (token == null || token.trim().isEmpty()) {
            token = generateToken();
            session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Retrieves the CSRF token from the session without generating a new one.
     *
     * @param session the HTTP session
     * @return the CSRF token string, or null if absent or session is null
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
    }

    /**
     * Validates that the request parameter matches the CSRF token stored in the session.
     * Uses constant-time comparison via MessageDigest.isEqual to prevent timing attacks.
     *
     * @param request the HTTP servlet request
     * @return true if token is present and valid, false otherwise
     */
    public static boolean isValid(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }
        String requestToken = request.getParameter(CSRF_TOKEN_PARAM);
        if (requestToken == null || requestToken.trim().isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(
                sessionToken.getBytes(StandardCharsets.UTF_8),
                requestToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
