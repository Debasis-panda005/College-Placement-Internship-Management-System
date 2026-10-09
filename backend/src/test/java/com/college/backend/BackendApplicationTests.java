package com.college.backend;

import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BackendApplicationTests {

    // =====================================================
    // 1. APPLICATION ENTITY UNIT TESTS
    // =====================================================

    @Test
    @DisplayName("Application entity constructor and getters/setters verify correctly")
    void testApplicationEntity() {
        LocalDate now = LocalDate.of(2026, 10, 9);
        Application application = new Application(101L, 201L, now, "APPLIED");

        assertNull(application.getId());
        assertEquals(101L, application.getStudentId());
        assertEquals(201L, application.getJobId());
        assertEquals(now, application.getApplicationDate());
        assertEquals("APPLIED", application.getStatus());

        application.setId(1L);
        application.setStatus("SHORTLISTED");
        assertEquals(1L, application.getId());
        assertEquals("SHORTLISTED", application.getStatus());
    }

    // =====================================================
    // 2. INTERVIEW ENTITY UNIT TESTS
    // =====================================================

    @Test
    @DisplayName("Interview entity constructor and getters/setters verify correctly")
    void testInterviewEntity() {
        LocalDate interviewDate = LocalDate.of(2026, 10, 15);
        LocalTime interviewTime = LocalTime.of(10, 30);
        Interview interview = new Interview(1L, interviewDate, interviewTime, "ONLINE", "SCHEDULED");

        assertNull(interview.getId());
        assertEquals(1L, interview.getApplicationId());
        assertEquals(interviewDate, interview.getInterviewDate());
        assertEquals(interviewTime, interview.getInterviewTime());
        assertEquals("ONLINE", interview.getMode());
        assertEquals("SCHEDULED", interview.getStatus());

        interview.setId(5L);
        interview.setStatus("COMPLETED");
        interview.setMode("OFFLINE");
        assertEquals(5L, interview.getId());
        assertEquals("COMPLETED", interview.getStatus());
        assertEquals("OFFLINE", interview.getMode());
    }

    // =====================================================
    // 3. APPLICATION STATUS HISTORY DISPLAY STATUS TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getDisplayStatus transforms interview events properly")
    void testApplicationStatusHistoryDisplayStatus() {
        ApplicationStatusHistory scheduled = new ApplicationStatusHistory(1L, "INTERVIEW_SCHEDULED");
        assertEquals("INTERVIEW SCHEDULED", scheduled.getDisplayStatus());

        ApplicationStatusHistory rescheduled = new ApplicationStatusHistory(1L, "INTERVIEW_RESCHEDULED");
        assertEquals("INTERVIEW RESCHEDULED", rescheduled.getDisplayStatus());

        ApplicationStatusHistory completed = new ApplicationStatusHistory(1L, "INTERVIEW_COMPLETED");
        assertEquals("INTERVIEW COMPLETED", completed.getDisplayStatus());

        ApplicationStatusHistory cancelled = new ApplicationStatusHistory(1L, "INTERVIEW_CANCELLED");
        assertEquals("INTERVIEW CANCELLED", cancelled.getDisplayStatus());

        ApplicationStatusHistory custom = new ApplicationStatusHistory(1L, "SHORTLISTED");
        assertEquals("SHORTLISTED", custom.getDisplayStatus());

        ApplicationStatusHistory nullStatus = new ApplicationStatusHistory(1L, null);
        assertEquals("", nullStatus.getDisplayStatus());
    }

    // =====================================================
    // 4. APPLICATION STATUS HISTORY TIMELINE DESCRIPTION TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getStatusDescription returns user-friendly descriptions")
    void testApplicationStatusHistoryStatusDescription() {
        ApplicationStatusHistory history = new ApplicationStatusHistory();

        history.setStatus("APPLIED");
        assertEquals("Application created", history.getStatusDescription());

        history.setStatus("SHORTLISTED");
        assertEquals("Candidate shortlisted", history.getStatusDescription());

        history.setStatus("INTERVIEW_SCHEDULED");
        assertEquals("Interview scheduled", history.getStatusDescription());

        history.setStatus("INTERVIEW_RESCHEDULED");
        assertEquals("Interview rescheduled", history.getStatusDescription());

        history.setStatus("INTERVIEW_COMPLETED");
        assertEquals("Interview completed", history.getStatusDescription());

        history.setStatus("INTERVIEW_CANCELLED");
        assertEquals("Interview cancelled", history.getStatusDescription());

        history.setStatus("SELECTED");
        assertEquals("Candidate selected", history.getStatusDescription());

        history.setStatus("REJECTED");
        assertEquals("Candidate rejected", history.getStatusDescription());

        history.setStatus(null);
        assertEquals("", history.getStatusDescription());
    }

    // =====================================================
    // 5. APPLICATION STATUS HISTORY CSS CLASS TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getStatusClass maps to correct CSS badge classes")
    void testApplicationStatusHistoryStatusClass() {
        ApplicationStatusHistory history = new ApplicationStatusHistory();

        history.setStatus("APPLIED");
        assertEquals("applied", history.getStatusClass());

        history.setStatus("SHORTLISTED");
        assertEquals("shortlisted", history.getStatusClass());

        history.setStatus("INTERVIEW_SCHEDULED");
        assertEquals("interview", history.getStatusClass());

        history.setStatus("INTERVIEW_RESCHEDULED");
        assertEquals("interview", history.getStatusClass());

        history.setStatus("INTERVIEW_COMPLETED");
        assertEquals("interview", history.getStatusClass());

        history.setStatus("INTERVIEW_CANCELLED");
        assertEquals("cancelled", history.getStatusClass());

        history.setStatus("SELECTED");
        assertEquals("selected", history.getStatusClass());

        history.setStatus("REJECTED");
        assertEquals("rejected", history.getStatusClass());

        history.setStatus(null);
        assertEquals("applied", history.getStatusClass());
    }

    // =====================================================
    // 6. APPLICATION STATUS HISTORY TIMESTAMP FORMATTING TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getFormattedChangedAt formats date and time correctly")
    void testApplicationStatusHistoryFormattedChangedAt() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 9, 14, 30);
        ApplicationStatusHistory history = new ApplicationStatusHistory(1L, "APPLIED", time);

        assertEquals("09 oct 2026, 02:30 pm", history.getFormattedChangedAt().toLowerCase());

        history.setChangedAt(null);
        assertEquals("", history.getFormattedChangedAt());
    }

    // =====================================================
    // 7. CSRF TOKEN GENERATION TESTS
    // =====================================================

    @Test
    @DisplayName("CsrfUtil generateToken generates non-empty, URL-safe, unique tokens")
    void testCsrfGenerateToken() {
        String token1 = CsrfUtil.generateToken();
        String token2 = CsrfUtil.generateToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertFalse(token1.trim().isEmpty());
        assertFalse(token2.trim().isEmpty());
        assertEquals(43, token1.length(), "32-byte Base64 URL token without padding should be 43 chars");
        assertNotEquals(token1, token2, "Successive tokens should be unique due to cryptographic randomness");
        assertTrue(token1.matches("^[A-Za-z0-9_-]+$"), "Token should be URL-safe");
    }

    // =====================================================
    // 8. CSRF TOKEN SESSION CACHING TESTS
    // =====================================================

    @Test
    @DisplayName("CsrfUtil getOrCreateToken caches and returns consistent token in session")
    void testCsrfGetOrCreateToken() {
        assertNull(CsrfUtil.getOrCreateToken(null));
        assertNull(CsrfUtil.getToken(null));

        Map<String, Object> sessionAttributes = new HashMap<>();
        HttpSession session = createMockSession(sessionAttributes);

        assertNull(CsrfUtil.getToken(session));

        String token = CsrfUtil.getOrCreateToken(session);
        assertNotNull(token);
        assertEquals(token, sessionAttributes.get(CsrfUtil.CSRF_TOKEN_SESSION_ATTR));
        assertEquals(token, CsrfUtil.getToken(session));

        // Subsequent call must return the exact same cached token
        String secondToken = CsrfUtil.getOrCreateToken(session);
        assertEquals(token, secondToken);
    }

    // =====================================================
    // 9. CSRF TOKEN VALIDATION TESTS
    // =====================================================

    @Test
    @DisplayName("CsrfUtil isValid accurately validates matching and rejecting invalid tokens")
    void testCsrfValidation() {
        assertFalse(CsrfUtil.isValid(null));

        Map<String, Object> sessionAttributes = new HashMap<>();
        HttpSession session = createMockSession(sessionAttributes);

        // No token in session yet
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "test-token");
        HttpServletRequest requestWithoutSessionToken = createMockRequest(session, requestParams);
        assertFalse(CsrfUtil.isValid(requestWithoutSessionToken));

        // Populate session token
        String validToken = CsrfUtil.getOrCreateToken(session);

        // Matching token -> true
        requestParams.put(CsrfUtil.CSRF_TOKEN_PARAM, validToken);
        HttpServletRequest validRequest = createMockRequest(session, requestParams);
        assertTrue(CsrfUtil.isValid(validRequest));

        // Mismatched token -> false
        requestParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "invalid-tampered-token");
        HttpServletRequest tamperedRequest = createMockRequest(session, requestParams);
        assertFalse(CsrfUtil.isValid(tamperedRequest));

        // Missing request token -> false
        requestParams.remove(CsrfUtil.CSRF_TOKEN_PARAM);
        HttpServletRequest missingTokenRequest = createMockRequest(session, requestParams);
        assertFalse(CsrfUtil.isValid(missingTokenRequest));

        // Empty request token -> false
        requestParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "   ");
        HttpServletRequest emptyTokenRequest = createMockRequest(session, requestParams);
        assertFalse(CsrfUtil.isValid(emptyTokenRequest));

        // Request with null session -> false
        HttpServletRequest requestWithNullSession = createMockRequest(null, requestParams);
        assertFalse(CsrfUtil.isValid(requestWithNullSession));
    }

    // =====================================================
    // TEST HELPERS
    // =====================================================

    private HttpSession createMockSession(Map<String, Object> attributes) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes.remove(args[0]);
                    }
                    return null;
                }
        );
    }

    private HttpServletRequest createMockRequest(HttpSession session, Map<String, String> params) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getSession".equals(name)) {
                        return session;
                    } else if ("getParameter".equals(name)) {
                        return params != null ? params.get(args[0]) : null;
                    }
                    return null;
                }
        );
    }
}
