package com.college.backend;

import com.college.backend.entity.User;
import com.college.backend.filter.AuthenticationFilter;
import com.college.backend.filter.CharacterEncodingFilter;
import com.college.backend.filter.CsrfFilter;
import com.college.backend.filter.RoleAuthorizationFilter;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 7 Automated Test Suite:
 * Verifies centralized Servlet Filters for Character Encoding (UTF-8),
 * Authentication, Role-Based Authorization (RBAC), and CSRF token protection.
 */
public class Phase7ServletFilterTests {

    // =========================================================================
    // 1. CHARACTER ENCODING FILTER TESTS
    // =========================================================================

    @Test
    @DisplayName("CharacterEncodingFilter sets UTF-8 on request when character encoding is null")
    void testCharacterEncodingFilterSetsRequestEncodingWhenNull() throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.init(null);

        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(null, "GET", "/jobs", "/college", new HashMap<>(), new HashMap<>(), requestState);
        HttpServletResponse response = createMockResponse(new HashMap<>());

        final boolean[] chainCalled = new boolean[]{false};
        FilterChain chain = createMockFilterChain(chainCalled);

        filter.doFilter(request, response, chain);

        assertTrue(chainCalled[0], "Filter chain must be invoked downstream");
        assertEquals("UTF-8", requestState.get("requestEncoding"), "Request encoding must be set to UTF-8");
    }

    @Test
    @DisplayName("CharacterEncodingFilter forces UTF-8 on request and response before commitment")
    void testCharacterEncodingFilterForcesEncodingOnRequestAndResponse() throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        Map<String, String> initParams = new HashMap<>();
        initParams.put("encoding", "UTF-8");
        initParams.put("forceEncoding", "true");
        filter.init(createMockFilterConfig(initParams));

        Map<String, Object> reqState = new HashMap<>();
        reqState.put("requestEncoding", "ISO-8859-1");
        HttpServletRequest request = createMockRequest(null, "POST", "/jobs", "/college", new HashMap<>(), new HashMap<>(), reqState);

        Map<String, Object> respState = new HashMap<>();
        respState.put("isCommitted", false);
        HttpServletResponse response = createMockResponse(respState);

        final boolean[] chainCalled = new boolean[]{false};
        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertTrue(chainCalled[0]);
        assertEquals("UTF-8", reqState.get("requestEncoding"), "Request encoding must be overwritten to UTF-8 when forceEncoding=true");
        assertEquals("UTF-8", respState.get("responseEncoding"), "Response encoding must be set to UTF-8");
    }

    @Test
    @DisplayName("CharacterEncodingFilter skips response encoding if response is already committed")
    void testCharacterEncodingFilterSkipsResponseEncodingWhenCommitted() throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.init(null);

        Map<String, Object> respState = new HashMap<>();
        respState.put("isCommitted", true);
        respState.put("responseEncoding", "ISO-8859-1");
        HttpServletResponse response = createMockResponse(respState);

        HttpServletRequest request = createMockRequest(null, "GET", "/home", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
        final boolean[] chainCalled = new boolean[]{false};

        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertTrue(chainCalled[0]);
        assertEquals("ISO-8859-1", respState.get("responseEncoding"), "Committed response encoding must not be modified");
    }

    // =========================================================================
    // 2. AUTHENTICATION FILTER TESTS
    // =========================================================================

    @Test
    @DisplayName("AuthenticationFilter allows unauthenticated access to public landing pages and static assets")
    void testAuthenticationFilterAllowsPublicPagesAndAssets() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.init(null);

        String[] publicPaths = new String[]{
                "/", "/home", "/index", "/index.jsp",
                "/login", "/register",
                "/assets/css/style.css", "/assets/js/main.js", "/assets/images/logo.png",
                "/assets/recruiters/google.svg", "/favicon.ico"
        };

        for (String path : publicPaths) {
            Map<String, Object> respState = new HashMap<>();
            HttpServletRequest request = createMockRequest(null, "GET", path, "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
            HttpServletResponse response = createMockResponse(respState);

            final boolean[] chainCalled = new boolean[]{false};
            filter.doFilter(request, response, createMockFilterChain(chainCalled));

            assertTrue(chainCalled[0], "Public path '" + path + "' must be allowed through without authentication");
            assertNull(respState.get("redirectUrl"), "Public path must not trigger a redirect");
        }
    }

    @Test
    @DisplayName("AuthenticationFilter allows unauthenticated users to browse jobs and internships on GET")
    void testAuthenticationFilterAllowsPublicJobBrowsing() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.init(null);

        String[] publicJobPaths = new String[]{"/jobs", "/jobs/view", "/internships", "/internships/details"};

        for (String path : publicJobPaths) {
            Map<String, Object> respState = new HashMap<>();
            HttpServletRequest request = createMockRequest(null, "GET", path, "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
            HttpServletResponse response = createMockResponse(respState);

            final boolean[] chainCalled = new boolean[]{false};
            filter.doFilter(request, response, createMockFilterChain(chainCalled));

            assertTrue(chainCalled[0], "GET on '" + path + "' must pass through without authentication");
            assertNull(respState.get("redirectUrl"));
        }
    }

    @Test
    @DisplayName("AuthenticationFilter intercepts unauthenticated attempts to access job creation or edit forms")
    void testAuthenticationFilterBlocksJobAddOrEditWhenUnauthenticated() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.init(null);

        // Case A: /jobs?action=new
        Map<String, String> newParams = new HashMap<>();
        newParams.put("action", "new");
        Map<String, Object> respStateA = new HashMap<>();
        Map<String, Object> reqStateA = new HashMap<>();
        HttpServletRequest reqA = createMockRequest(null, "GET", "/jobs", "/college", newParams, new HashMap<>(), reqStateA);
        HttpServletResponse respA = createMockResponse(respStateA);

        final boolean[] chainCalledA = new boolean[]{false};
        filter.doFilter(reqA, respA, createMockFilterChain(chainCalledA));

        assertFalse(chainCalledA[0], "Unauthenticated user must not reach add job form");
        assertEquals("/college/login", respStateA.get("redirectUrl"));
        Map<String, Object> sessionA = (Map<String, Object>) reqStateA.get("newSessionAttrs");
        assertNotNull(sessionA);
        assertEquals("You must log in to add jobs.", sessionA.get("errorMessage"));

        // Case B: /jobs?action=edit&id=10
        Map<String, String> editParams = new HashMap<>();
        editParams.put("action", "edit");
        editParams.put("id", "10");
        Map<String, Object> respStateB = new HashMap<>();
        Map<String, Object> reqStateB = new HashMap<>();
        HttpServletRequest reqB = createMockRequest(null, "GET", "/jobs", "/college", editParams, new HashMap<>(), reqStateB);
        HttpServletResponse respB = createMockResponse(respStateB);

        final boolean[] chainCalledB = new boolean[]{false};
        filter.doFilter(reqB, respB, createMockFilterChain(chainCalledB));

        assertFalse(chainCalledB[0]);
        assertEquals("/college/login", respStateB.get("redirectUrl"));
        Map<String, Object> sessionB = (Map<String, Object>) reqStateB.get("newSessionAttrs");
        assertNotNull(sessionB);
        assertEquals("You must log in to edit jobs.", sessionB.get("errorMessage"));
    }

    @Test
    @DisplayName("AuthenticationFilter redirects unauthenticated requests for protected routes to /login with flash message")
    void testAuthenticationFilterRedirectsProtectedRoutesToLogin() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.init(null);

        String[] protectedPaths = new String[]{
                "/admin/dashboard", "/admin",
                "/applications", "/applications/shortlist", "/shortlist",
                "/interviews", "/interview-rounds",
                "/interview-report", "/interview-report/view"
        };

        for (String path : protectedPaths) {
            Map<String, Object> respState = new HashMap<>();
            Map<String, Object> reqState = new HashMap<>();
            HttpServletRequest request = createMockRequest(null, "GET", path, "/college", new HashMap<>(), new HashMap<>(), reqState);
            HttpServletResponse response = createMockResponse(respState);

            final boolean[] chainCalled = new boolean[]{false};
            filter.doFilter(request, response, createMockFilterChain(chainCalled));

            assertFalse(chainCalled[0], "Unauthenticated request to '" + path + "' must be intercepted");
            assertEquals("/college/login", respState.get("redirectUrl"), "Must redirect to /login");
            Map<String, Object> sessionAttrs = (Map<String, Object>) reqState.get("newSessionAttrs");
            assertNotNull(sessionAttrs, "A session with flash error message must be provisioned");
            assertNotNull(sessionAttrs.get("errorMessage"), "Flash error message must be set");
        }
    }

    @Test
    @DisplayName("AuthenticationFilter allows authenticated users through to protected pages")
    void testAuthenticationFilterAllowsAuthenticatedUserThrough() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 42L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        String[] protectedPaths = new String[]{"/admin/dashboard", "/applications", "/interviews", "/interview-rounds"};

        for (String path : protectedPaths) {
            Map<String, Object> respState = new HashMap<>();
            HttpServletRequest request = createMockRequest(session, "GET", path, "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
            HttpServletResponse response = createMockResponse(respState);

            final boolean[] chainCalled = new boolean[]{false};
            filter.doFilter(request, response, createMockFilterChain(chainCalled));

            assertTrue(chainCalled[0], "Authenticated user must pass through for '" + path + "'");
            assertNull(respState.get("redirectUrl"));
        }
    }

    @Test
    @DisplayName("AuthenticationFilter prevents redirect loops when accessing /login")
    void testAuthenticationFilterPreventsRedirectLoopOnLogin() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.init(null);

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest request = createMockRequest(null, "GET", "/login", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
        HttpServletResponse response = createMockResponse(respState);

        final boolean[] chainCalled = new boolean[]{false};
        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertTrue(chainCalled[0], "Unauthenticated /login must proceed without redirect");
        assertNull(respState.get("redirectUrl"), "No redirect loop allowed on /login");
    }

    // =========================================================================
    // 3. ROLE AUTHORIZATION FILTER TESTS (RBAC)
    // =========================================================================

    @Test
    @DisplayName("RoleAuthorizationFilter blocks student access to admin dashboard with 403 Forbidden")
    void testRoleAuthorizationFilterBlocksStudentFromAdminDashboard() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "GET", "/admin/dashboard", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
        HttpServletResponse response = createMockResponse(respState);

        final boolean[] chainCalled = new boolean[]{false};
        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertFalse(chainCalled[0], "Student must be blocked from admin dashboard");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"));
    }

    @Test
    @DisplayName("RoleAuthorizationFilter permits coordinator and administrator to access admin dashboard")
    void testRoleAuthorizationFilterAllowsCoordinatorAndAdminToAdminDashboard() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();
        filter.init(null);

        String[] allowedRoles = new String[]{User.ROLE_COORDINATOR, User.ROLE_ADMIN};

        for (String role : allowedRoles) {
            Map<String, Object> sessionAttrs = new HashMap<>();
            sessionAttrs.put(LoginServlet.SESSION_USER_ID, 202L);
            sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, role);
            HttpSession session = createMockSession(sessionAttrs);

            Map<String, Object> respState = new HashMap<>();
            HttpServletRequest request = createMockRequest(session, "GET", "/admin/dashboard", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
            HttpServletResponse response = createMockResponse(respState);

            final boolean[] chainCalled = new boolean[]{false};
            filter.doFilter(request, response, createMockFilterChain(chainCalled));

            assertTrue(chainCalled[0], "Role " + role + " must be permitted on /admin/dashboard");
            assertNull(respState.get("errorCode"));
        }
    }

    @Test
    @DisplayName("RoleAuthorizationFilter blocks student from mutating job postings or opening add job form")
    void testRoleAuthorizationFilterBlocksStudentFromMutatingJobs() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        // Case A: GET /jobs?action=new
        Map<String, String> newParams = new HashMap<>();
        newParams.put("action", "new");
        Map<String, Object> respStateA = new HashMap<>();
        HttpServletRequest reqA = createMockRequest(session, "GET", "/jobs", "/college", newParams, new HashMap<>(), new HashMap<>());
        HttpServletResponse respA = createMockResponse(respStateA);

        final boolean[] chainCalledA = new boolean[]{false};
        filter.doFilter(reqA, respA, createMockFilterChain(chainCalledA));

        assertFalse(chainCalledA[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateA.get("errorCode"));

        // Case B: POST /jobs
        Map<String, Object> respStateB = new HashMap<>();
        HttpServletRequest reqB = createMockRequest(session, "POST", "/jobs", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
        HttpServletResponse respB = createMockResponse(respStateB);

        final boolean[] chainCalledB = new boolean[]{false};
        filter.doFilter(reqB, respB, createMockFilterChain(chainCalledB));

        assertFalse(chainCalledB[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateB.get("errorCode"));
    }

    @Test
    @DisplayName("RoleAuthorizationFilter blocks student from managing interview rounds with 403 Forbidden")
    void testRoleAuthorizationFilterBlocksStudentFromManagingInterviewRounds() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> postParams = new HashMap<>();
        postParams.put("action", "complete");
        postParams.put("roundId", "5");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "POST", "/interview-rounds", "/college", postParams, new HashMap<>(), new HashMap<>());
        HttpServletResponse response = createMockResponse(respState);

        final boolean[] chainCalled = new boolean[]{false};
        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertFalse(chainCalled[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"));
    }

    @Test
    @DisplayName("RoleAuthorizationFilter allows student to submit application but blocks coordinator")
    void testRoleAuthorizationFilterStudentApplicationSubmissionCheck() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();
        filter.init(null);

        Map<String, String> applyParams = new HashMap<>();
        applyParams.put("action", "apply");
        applyParams.put("jobId", "10");

        // 1. Student applying -> allowed
        Map<String, Object> studentSession = new HashMap<>();
        studentSession.put(LoginServlet.SESSION_USER_ID, 101L);
        studentSession.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);

        Map<String, Object> respStateStudent = new HashMap<>();
        HttpServletRequest reqStudent = createMockRequest(createMockSession(studentSession), "POST", "/applications", "/college", applyParams, new HashMap<>(), new HashMap<>());
        final boolean[] chainCalledStudent = new boolean[]{false};
        filter.doFilter(reqStudent, createMockResponse(respStateStudent), createMockFilterChain(chainCalledStudent));

        assertTrue(chainCalledStudent[0], "Student must be permitted to submit job application");
        assertNull(respStateStudent.get("errorCode"));

        // 2. Coordinator attempting to submit application -> blocked
        Map<String, Object> coordSession = new HashMap<>();
        coordSession.put(LoginServlet.SESSION_USER_ID, 202L);
        coordSession.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);

        Map<String, Object> respStateCoord = new HashMap<>();
        HttpServletRequest reqCoord = createMockRequest(createMockSession(coordSession), "POST", "/applications", "/college", applyParams, new HashMap<>(), new HashMap<>());
        final boolean[] chainCalledCoord = new boolean[]{false};
        filter.doFilter(reqCoord, createMockResponse(respStateCoord), createMockFilterChain(chainCalledCoord));

        assertFalse(chainCalledCoord[0], "Coordinator must not submit student job applications");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateCoord.get("errorCode"));
    }

    @Test
    @DisplayName("RoleAuthorizationFilter blocks users with unrecognized roles with 403 Forbidden")
    void testRoleAuthorizationFilterBlocksUnrecognizedRoles() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 999L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, "UNKNOWN_ATTACKER_ROLE");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "GET", "/applications", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
        HttpServletResponse response = createMockResponse(respState);

        final boolean[] chainCalled = new boolean[]{false};
        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertFalse(chainCalled[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"));
    }

    // =========================================================================
    // 4. CSRF PROTECTION FILTER TESTS
    // =========================================================================

    @Test
    @DisplayName("CsrfFilter seeds CSRF token on safe HTTP methods (GET, HEAD, OPTIONS) and allows request")
    void testCsrfFilterSeedsTokenOnSafeMethods() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        filter.init(null);

        String[] safeMethods = new String[]{"GET", "HEAD", "OPTIONS"};

        for (String method : safeMethods) {
            Map<String, Object> sessionAttrs = new HashMap<>();
            HttpSession session = createMockSession(sessionAttrs);

            Map<String, Object> respState = new HashMap<>();
            HttpServletRequest request = createMockRequest(session, method, "/login", "/college", new HashMap<>(), new HashMap<>(), new HashMap<>());
            HttpServletResponse response = createMockResponse(respState);

            final boolean[] chainCalled = new boolean[]{false};
            filter.doFilter(request, response, createMockFilterChain(chainCalled));

            assertTrue(chainCalled[0], method + " must pass through filter chain");
            assertNotNull(sessionAttrs.get(CsrfUtil.CSRF_TOKEN_SESSION_ATTR), "CSRF token must be seeded in session on " + method);
        }
    }

    @Test
    @DisplayName("CsrfFilter permits POST requests with valid matching CSRF token")
    void testCsrfFilterPermitsPostWithValidToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "secure-random-token-xyz");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> postParams = new HashMap<>();
        postParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "secure-random-token-xyz");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "POST", "/login", "/college", postParams, new HashMap<>(), new HashMap<>());
        HttpServletResponse response = createMockResponse(respState);

        final boolean[] chainCalled = new boolean[]{false};
        filter.doFilter(request, response, createMockFilterChain(chainCalled));

        assertTrue(chainCalled[0], "POST with valid CSRF token must pass through");
        assertNull(respState.get("errorCode"));
    }

    @Test
    @DisplayName("CsrfFilter rejects POST requests with missing, invalid or tampered CSRF token with 403 Forbidden")
    void testCsrfFilterRejectsPostWithInvalidOrMissingToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        filter.init(null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "legitimate-session-token");
        HttpSession session = createMockSession(sessionAttrs);

        // Case 1: Missing CSRF token parameter
        Map<String, String> noTokenParams = new HashMap<>();
        Map<String, Object> respState1 = new HashMap<>();
        HttpServletRequest req1 = createMockRequest(session, "POST", "/login", "/college", noTokenParams, new HashMap<>(), new HashMap<>());
        final boolean[] chainCalled1 = new boolean[]{false};
        filter.doFilter(req1, createMockResponse(respState1), createMockFilterChain(chainCalled1));

        assertFalse(chainCalled1[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState1.get("errorCode"));

        // Case 2: Tampered/mismatched CSRF token parameter
        Map<String, String> badTokenParams = new HashMap<>();
        badTokenParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "forged-attacker-token");
        Map<String, Object> respState2 = new HashMap<>();
        HttpServletRequest req2 = createMockRequest(session, "POST", "/jobs", "/college", badTokenParams, new HashMap<>(), new HashMap<>());
        final boolean[] chainCalled2 = new boolean[]{false};
        filter.doFilter(req2, createMockResponse(respState2), createMockFilterChain(chainCalled2));

        assertFalse(chainCalled2[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState2.get("errorCode"));

        // Case 3: Empty CSRF token parameter
        Map<String, String> emptyTokenParams = new HashMap<>();
        emptyTokenParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "   ");
        Map<String, Object> respState3 = new HashMap<>();
        HttpServletRequest req3 = createMockRequest(session, "POST", "/register", "/college", emptyTokenParams, new HashMap<>(), new HashMap<>());
        final boolean[] chainCalled3 = new boolean[]{false};
        filter.doFilter(req3, createMockResponse(respState3), createMockFilterChain(chainCalled3));

        assertFalse(chainCalled3[0]);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState3.get("errorCode"));
    }

    // =========================================================================
    // 5. WEB.XML FILTER REGISTRATION AND ORDER VERIFICATION
    // =========================================================================

    @Test
    @DisplayName("web.xml defines and maps all four filters in the correct sequential pipeline order")
    void testWebXmlFilterRegistrationAndExecutionOrder() throws IOException {
        File webXmlFile = new File("src/main/webapp/WEB-INF/web.xml");
        if (!webXmlFile.exists()) {
            webXmlFile = new File("backend/src/main/webapp/WEB-INF/web.xml");
        }
        assertTrue(webXmlFile.exists(), "web.xml must exist");

        String content = Files.readString(webXmlFile.toPath());

        // Verify filter declarations
        assertTrue(content.contains("<filter-name>CharacterEncodingFilter</filter-name>"));
        assertTrue(content.contains("<filter-class>com.college.backend.filter.CharacterEncodingFilter</filter-class>"));

        assertTrue(content.contains("<filter-name>AuthenticationFilter</filter-name>"));
        assertTrue(content.contains("<filter-class>com.college.backend.filter.AuthenticationFilter</filter-class>"));

        assertTrue(content.contains("<filter-name>RoleAuthorizationFilter</filter-name>"));
        assertTrue(content.contains("<filter-class>com.college.backend.filter.RoleAuthorizationFilter</filter-class>"));

        assertTrue(content.contains("<filter-name>CsrfFilter</filter-name>"));
        assertTrue(content.contains("<filter-class>com.college.backend.filter.CsrfFilter</filter-class>"));

        // Verify sequential mapping order
        int posEnc = content.indexOf("<filter-name>CharacterEncodingFilter</filter-name>");
        int posAuth = content.indexOf("<filter-name>AuthenticationFilter</filter-name>");
        int posRole = content.indexOf("<filter-name>RoleAuthorizationFilter</filter-name>");
        int posCsrf = content.indexOf("<filter-name>CsrfFilter</filter-name>");

        assertTrue(posEnc < posAuth, "CharacterEncodingFilter must precede AuthenticationFilter");
        assertTrue(posAuth < posRole, "AuthenticationFilter must precede RoleAuthorizationFilter");
        assertTrue(posRole < posCsrf, "RoleAuthorizationFilter must precede CsrfFilter");
    }

    // =========================================================================
    // TEST PROXY FIXTURES AND HELPERS
    // =========================================================================

    private HttpServletRequest createMockRequest(HttpSession session,
                                                 String method,
                                                 String uri,
                                                 String contextPath,
                                                 Map<String, String> params,
                                                 Map<String, Object> attributes,
                                                 Map<String, Object> state) {
        final HttpSession[] sessionHolder = new HttpSession[]{session};

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, invokedMethod, args) -> {
                    String name = invokedMethod.getName();
                    if ("getMethod".equals(name)) {
                        return method != null ? method : "GET";
                    } else if ("getRequestURI".equals(name)) {
                        return contextPath + uri;
                    } else if ("getContextPath".equals(name)) {
                        return contextPath;
                    } else if ("getServletPath".equals(name)) {
                        return uri;
                    } else if ("getParameter".equals(name)) {
                        return params != null ? params.get(args[0]) : null;
                    } else if ("getParameterMap".equals(name)) {
                        Map<String, String[]> map = new HashMap<>();
                        if (params != null) {
                            for (Map.Entry<String, String> e : params.entrySet()) {
                                map.put(e.getKey(), new String[]{e.getValue()});
                            }
                        }
                        return map;
                    } else if ("setAttribute".equals(name)) {
                        if (attributes != null) {
                            attributes.put((String) args[0], args[1]);
                        }
                        return null;
                    } else if ("getAttribute".equals(name)) {
                        return attributes != null ? attributes.get(args[0]) : null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes != null ? attributes.remove(args[0]) : null;
                    } else if ("getCharacterEncoding".equals(name)) {
                        return state != null ? state.get("requestEncoding") : null;
                    } else if ("setCharacterEncoding".equals(name)) {
                        if (state != null) {
                            state.put("requestEncoding", args[0]);
                        }
                        return null;
                    } else if ("getSession".equals(name)) {
                        boolean create = true;
                        if (args != null && args.length == 1) {
                            create = (Boolean) args[0];
                        }
                        if (sessionHolder[0] == null) {
                            if (create) {
                                Map<String, Object> newSessionAttrs = new HashMap<>();
                                if (state != null) {
                                    state.put("newSessionAttrs", newSessionAttrs);
                                }
                                sessionHolder[0] = createMockSession(newSessionAttrs);
                                return sessionHolder[0];
                            }
                            return null;
                        }
                        return sessionHolder[0];
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
                    } else if ("setCharacterEncoding".equals(name)) {
                        state.put("responseEncoding", args[0]);
                        return null;
                    } else if ("getCharacterEncoding".equals(name)) {
                        return state.getOrDefault("responseEncoding", "UTF-8");
                    } else if ("isCommitted".equals(name)) {
                        return state.getOrDefault("isCommitted", false);
                    }
                    return null;
                }
        );
    }

    private HttpSession createMockSession(Map<String, Object> attributes) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAttribute".equals(name)) {
                        return attributes != null ? attributes.get(args[0]) : null;
                    } else if ("setAttribute".equals(name)) {
                        if (attributes != null) {
                            attributes.put((String) args[0], args[1]);
                        }
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes != null ? attributes.remove(args[0]) : null;
                    } else if ("invalidate".equals(name)) {
                        if (attributes != null) {
                            attributes.clear();
                        }
                        return null;
                    }
                    return null;
                }
        );
    }

    private FilterChain createMockFilterChain(final boolean[] chainCalled) {
        return new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                chainCalled[0] = true;
            }
        };
    }

    private FilterConfig createMockFilterConfig(Map<String, String> initParams) {
        return (FilterConfig) Proxy.newProxyInstance(
                FilterConfig.class.getClassLoader(),
                new Class<?>[]{FilterConfig.class},
                (proxy, method, args) -> {
                    if ("getInitParameter".equals(method.getName())) {
                        return initParams != null ? initParams.get(args[0]) : null;
                    }
                    return null;
                }
        );
    }
}
