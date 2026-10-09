package com.college.backend;

import com.college.backend.dao.JobDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import com.college.backend.entity.Job;
import com.college.backend.servlet.JobServlet;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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
    // 10. JOB ENTITY UNIT TESTS
    // =====================================================

    @Test
    @DisplayName("Job entity constructor and getters/setters verify correctly with Long ID")
    void testJobEntity() {
        Job job = new Job("Software Engineer", "Acme Corp", "Bangalore",
                "Backend role", "12 LPA", "Full Time", "Active");

        assertNull(job.getId());
        assertEquals("Software Engineer", job.getTitle());
        assertEquals("Acme Corp", job.getCompany());
        assertEquals("Bangalore", job.getLocation());
        assertEquals("Backend role", job.getDescription());
        assertEquals("12 LPA", job.getSalary());
        assertEquals("Full Time", job.getJobType());
        assertEquals("Active", job.getStatus());

        job.setId(100L);
        job.setStatus("Closed");
        job.setSalary("14 LPA");
        assertEquals(100L, job.getId());
        assertEquals("Closed", job.getStatus());
        assertEquals("14 LPA", job.getSalary());

        Job jobWithId = new Job(200L, "Data Analyst Intern", "Beta Labs", "Remote",
                "Analytics", "25,000", "Internship", "Active");
        assertEquals(200L, jobWithId.getId());
        assertEquals("Internship", jobWithId.getJobType());
        assertTrue(jobWithId.toString().contains("Beta Labs"));
    }

    // =====================================================
    // 11. JOB DAO SORT WHITELIST TESTS
    // =====================================================

    @Test
    @DisplayName("JobDAO resolveSortOrder safely whitelists allowed columns and rejects malicious input")
    void testJobDAOSortWhitelist() {
        assertEquals("title ASC", JobDAO.resolveSortOrder("title"));
        assertEquals("company ASC", JobDAO.resolveSortOrder("company"));
        assertEquals("job_type ASC", JobDAO.resolveSortOrder("jobType"));
        assertEquals("job_type ASC", JobDAO.resolveSortOrder("job_type"));
        assertEquals("location ASC", JobDAO.resolveSortOrder("location"));
        assertEquals(JobDAO.SALARY_SORT_CLAUSE, JobDAO.resolveSortOrder("salary"));

        // Defaults to id DESC on null or unknown/malicious input
        assertEquals("id DESC", JobDAO.resolveSortOrder(null));
        assertEquals("id DESC", JobDAO.resolveSortOrder(""));
        assertEquals("id DESC", JobDAO.resolveSortOrder("unknown_column"));
        assertEquals("id DESC", JobDAO.resolveSortOrder("id; DROP TABLE jobs;--"));
    }

    // =====================================================
    // 12. JOB SERVLET STATE-CHANGING GET REJECTION TESTS
    // =====================================================

    @Test
    @DisplayName("JobServlet rejects state-changing actions on GET with HTTP 405 Method Not Allowed")
    void testJobServletRejectsStateChangingGetRequests() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doGetMethod = JobServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        String[] stateChangingActions = {"create", "update", "delete"};

        for (String action : stateChangingActions) {
            Map<String, String> params = new HashMap<>();
            params.put("action", action);

            Map<String, Object> sessionAttrs = new HashMap<>();
            HttpSession session = createMockSession(sessionAttrs);
            HttpServletRequest request = createMockRequest(session, params);

            Map<String, Object> responseState = new HashMap<>();
            HttpServletResponse response = createMockResponse(responseState);

            doGetMethod.invoke(servlet, request, response);

            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, responseState.get("errorCode"),
                    "Action '" + action + "' on GET must be rejected with HTTP 405");
        }
    }

    // =====================================================
    // 13. JOB SERVLET CSRF VALIDATION ON POST TESTS
    // =====================================================

    @Test
    @DisplayName("JobServlet enforces CSRF token validation on POST requests and rejects missing/invalid tokens")
    void testJobServletCsrfValidationOnPost() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        // 1. Missing CSRF token
        Map<String, String> missingParams = new HashMap<>();
        missingParams.put("action", "delete");
        missingParams.put("id", "10");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-secret-token");
        HttpSession session = createMockSession(sessionAttrs);

        HttpServletRequest missingTokenRequest = createMockRequest(session, missingParams);
        Map<String, Object> missingTokenResponseState = new HashMap<>();
        HttpServletResponse missingTokenResponse = createMockResponse(missingTokenResponseState);

        doPostMethod.invoke(servlet, missingTokenRequest, missingTokenResponse);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, missingTokenResponseState.get("errorCode"),
                "POST without CSRF token must be rejected with HTTP 403 Forbidden");

        // 2. Tampered / invalid CSRF token
        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("action", "delete");
        invalidParams.put("id", "10");
        invalidParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "tampered-token");

        HttpServletRequest invalidTokenRequest = createMockRequest(session, invalidParams);
        Map<String, Object> invalidTokenResponseState = new HashMap<>();
        HttpServletResponse invalidTokenResponse = createMockResponse(invalidTokenResponseState);

        doPostMethod.invoke(servlet, invalidTokenRequest, invalidTokenResponse);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, invalidTokenResponseState.get("errorCode"),
                "POST with invalid CSRF token must be rejected with HTTP 403 Forbidden");
    }

    // =====================================================
    // 14. JOB APPLICATION LINKAGE UNIT TESTS
    // =====================================================

    @Test
    @DisplayName("Application correctly references Job Long ID and preserves workflow integrity")
    void testJobApplicationLinkage() {
        Job job = new Job(55L, "Cloud Engineer", "CloudCorp", "Hyderabad",
                "AWS/GCP role", "10 LPA", "Full Time", "Active");

        Long studentId = 1001L;
        LocalDate applicationDate = LocalDate.of(2026, 10, 9);
        Application application = new Application(studentId, job.getId(), applicationDate, "APPLIED");

        assertEquals(studentId, application.getStudentId());
        assertEquals(job.getId(), application.getJobId());
        assertEquals(55L, application.getJobId());
        assertEquals("APPLIED", application.getStatus());
    }

    // =====================================================
    // 15. SALARY NUMERIC NORMALIZATION TESTS
    // =====================================================

    @Test
    @DisplayName("Salary values normalize numerically to annual amount across formats and descriptions")
    void testSalaryNumericNormalization() {
        assertEquals(15000.0, JobDAO.parseSalaryToNumericAnnual("15000"), 0.001);
        assertEquals(50000.0, JobDAO.parseSalaryToNumericAnnual("50000"), 0.001);
        assertEquals(150000.0, JobDAO.parseSalaryToNumericAnnual("150000"), 0.001);
        assertEquals(50000.0, JobDAO.parseSalaryToNumericAnnual("50,000"), 0.001);
        assertEquals(850000.0, JobDAO.parseSalaryToNumericAnnual("8.5 LPA"), 0.001);
        assertEquals(1200000.0, JobDAO.parseSalaryToNumericAnnual("12 LPA"), 0.001);
        assertEquals(1400000.0, JobDAO.parseSalaryToNumericAnnual("14 LPA"), 0.001);
        assertEquals(600000.0, JobDAO.parseSalaryToNumericAnnual("50000 / month"), 0.001);
        assertEquals(300000.0, JobDAO.parseSalaryToNumericAnnual("25000 / month"), 0.001);

        // Non-numeric descriptions normalize to 0.0 (sorted last)
        assertEquals(0.0, JobDAO.parseSalaryToNumericAnnual("Competitive"), 0.001);
        assertEquals(0.0, JobDAO.parseSalaryToNumericAnnual("Negotiable"), 0.001);
        assertEquals(0.0, JobDAO.parseSalaryToNumericAnnual("Unpaid"), 0.001);
        assertEquals(0.0, JobDAO.parseSalaryToNumericAnnual(null), 0.001);
        assertEquals(0.0, JobDAO.parseSalaryToNumericAnnual(""), 0.001);

        // Verify mathematical descending order across all required test values
        double sal14Lpa = JobDAO.parseSalaryToNumericAnnual("14 LPA");
        double sal12Lpa = JobDAO.parseSalaryToNumericAnnual("12 LPA");
        double sal85Lpa = JobDAO.parseSalaryToNumericAnnual("8.5 LPA");
        double sal50kMonth = JobDAO.parseSalaryToNumericAnnual("50000 / month");
        double sal25kMonth = JobDAO.parseSalaryToNumericAnnual("25000 / month");
        double sal150k = JobDAO.parseSalaryToNumericAnnual("150000");
        double sal50kWithComma = JobDAO.parseSalaryToNumericAnnual("50,000");
        double sal50k = JobDAO.parseSalaryToNumericAnnual("50000");
        double sal15k = JobDAO.parseSalaryToNumericAnnual("15000");
        double salCompetitive = JobDAO.parseSalaryToNumericAnnual("Competitive");

        assertTrue(sal14Lpa > sal12Lpa);
        assertTrue(sal12Lpa > sal85Lpa);
        assertTrue(sal85Lpa > sal50kMonth);
        assertTrue(sal50kMonth > sal25kMonth);
        assertTrue(sal25kMonth > sal150k);
        assertTrue(sal150k > sal50k);
        assertEquals(sal50kWithComma, sal50k);
        assertTrue(sal50k > sal15k);
        assertTrue(sal15k > salCompetitive);
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

    private HttpServletResponse createMockResponse(Map<String, Object> state) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("sendError".equals(name)) {
                        state.put("errorCode", args[0]);
                        if (args.length > 1) {
                            state.put("errorMessage", args[1]);
                        }
                        return null;
                    } else if ("sendRedirect".equals(name)) {
                        state.put("redirectUrl", args[0]);
                        return null;
                    }
                    return null;
                }
        );
    }
}
