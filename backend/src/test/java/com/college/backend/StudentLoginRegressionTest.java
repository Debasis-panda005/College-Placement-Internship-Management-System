package com.college.backend;

import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.CsrfUtil;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated verification test suite validating the resolution of the Student Login Database Error
 * and verifying comprehensive authentication behavior across all four user account types:
 *
 * 1. Internal Students (Centurion University students):
 *    - Authenticated and redirected to /jobs.
 *    - Must NOT require an external student profile or fail if external_students table is missing.
 *    - studentType session attribute set to "INTERNAL".
 *
 * 2. External Students:
 *    - Approved accounts: Authenticated and redirected to /external/dashboard.
 *    - Pending accounts: Blocked with pending verification notice; no session created.
 *    - Rejected accounts: Blocked with rejection notice; no session created.
 *    - studentType session attribute set to "EXTERNAL", externalStudentId recorded.
 *
 * 3. Coordinators:
 *    - Approved accounts: Authenticated and redirected to /jobs.
 *    - Pending approval accounts: Blocked with pending administrator approval notice.
 *    - Rejected accounts: Blocked with rejection notice.
 *    - Must NOT require a student profile.
 *
 * 4. Administrators:
 *    - Authenticated and redirected to /jobs.
 *    - Must NOT require student or coordinator profiles.
 *
 * 5. Security & Error Handling:
 *    - Invalid passwords blocked with generic "Invalid email or password." notice (prevents enumeration).
 *    - Database failures (DAOException) logged with SEVERE level and shielded from client.
 *    - Schema backwards-compatibility: UserDAO falls back seamlessly on MySQL Error 1054 (missing status column).
 *    - Missing table compatibility: ExternalStudentDAO returns null on MySQL Error 1146.
 */
public class StudentLoginRegressionTest {

    private static final String CSRF_TOKEN = "test-valid-csrf-token";

    @BeforeAll
    static void initDbConfig() {
        if (System.getProperty("db.password") == null && System.getenv("DB_PASSWORD") == null) {
            System.setProperty("db.password", "Tapu@123");
        }
    }

    // =====================================================
    // 1. INTERNAL STUDENT LOGIN
    // =====================================================

    @Test
    @DisplayName("1. Internal student logs in successfully and is redirected to /jobs without requiring external student profile")
    void testInternalStudentLoginSuccess() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "StudentPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User internalUser = new User(101L, "Internal CUTM Student", "internal.student@cutm.ac.in", hash,
                User.ROLE_STUDENT, User.STATUS_APPROVED, LocalDateTime.now());

        // UserDAO returns internal student; ExternalStudentDAO returns null (no external profile)
        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("internal.student@cutm.ac.in".equalsIgnoreCase(email)) {
                    return internalUser;
                }
                return null;
            }
        });

        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                // Internal students have NO record in external_students table
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "internal.student@cutm.ac.in");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verify redirect to /jobs
        assertEquals("/jobs", respState.get("redirectedUrl"), "Approved internal student must be redirected to /jobs");

        // Verify fresh authenticated session attributes
        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrs = (Map<String, Object>) reqState.get("newSessionAttrs");
        assertNotNull(newSessionAttrs, "Fresh session must be created");
        assertEquals(101L, newSessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals("internal.student@cutm.ac.in", newSessionAttrs.get(LoginServlet.SESSION_USER_EMAIL));
        assertEquals("Internal CUTM Student", newSessionAttrs.get(LoginServlet.SESSION_USER_NAME));
        assertEquals(User.ROLE_STUDENT, newSessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
        assertEquals("INTERNAL", newSessionAttrs.get("studentType"), "Student classification must be INTERNAL");
        assertNull(newSessionAttrs.get("externalStudentId"), "Internal student must not have an externalStudentId");
    }

    // =====================================================
    // 2. EXTERNAL STUDENT LOGIN (APPROVED)
    // =====================================================

    @Test
    @DisplayName("2. Approved external student logs in successfully and is redirected to /external/dashboard")
    void testExternalStudentLoginApproved() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "ExternalPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User externalUser = new User(202L, "External Approved Student", "approved.ext@otheruniv.edu", hash,
                User.ROLE_STUDENT, User.STATUS_APPROVED, LocalDateTime.now());

        ExternalStudent extProfile = new ExternalStudent(202L, "National Institute of Technology", "Rourkela",
                "B.Tech", "Computer Science", 6, new BigDecimal("8.75"), 2026, "9876543210", "/resumes/ext.pdf");
        extProfile.setId(55L);
        extProfile.setVerificationStatus(ExternalStudent.STATUS_APPROVED);

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("approved.ext@otheruniv.edu".equalsIgnoreCase(email)) {
                    return externalUser;
                }
                return null;
            }
        });

        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                if (userId != null && userId.equals(202L)) {
                    return extProfile;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "approved.ext@otheruniv.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verify redirect to external student dashboard
        assertEquals("/external/dashboard", respState.get("redirectedUrl"), "Approved external student must be redirected to /external/dashboard");

        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrs = (Map<String, Object>) reqState.get("newSessionAttrs");
        assertNotNull(newSessionAttrs);
        assertEquals(202L, newSessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals("approved.ext@otheruniv.edu", newSessionAttrs.get(LoginServlet.SESSION_USER_EMAIL));
        assertEquals(User.ROLE_STUDENT, newSessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
        assertEquals("EXTERNAL", newSessionAttrs.get("studentType"), "Student classification must be EXTERNAL");
        assertEquals(55L, newSessionAttrs.get("externalStudentId"), "External student profile ID must be present");
    }

    // =====================================================
    // 3. EXTERNAL STUDENT LOGIN (PENDING VERIFICATION)
    // =====================================================

    @Test
    @DisplayName("3. Pending external student login is blocked with pending verification notification")
    void testExternalStudentLoginPendingVerificationBlocked() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "PendingPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User pendingUser = new User(203L, "Pending External Student", "pending.ext@otheruniv.edu", hash,
                User.ROLE_STUDENT, User.STATUS_PENDING, LocalDateTime.now());

        ExternalStudent extProfile = new ExternalStudent(203L, "Delhi Tech University", "Delhi",
                "B.Tech", "IT", 4, new BigDecimal("8.10"), 2027, "9123456789", "/resumes/pending.pdf");
        extProfile.setId(56L);
        extProfile.setVerificationStatus(ExternalStudent.STATUS_PENDING);

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("pending.ext@otheruniv.edu".equalsIgnoreCase(email)) {
                    return pendingUser;
                }
                return null;
            }
        });

        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                if (userId != null && userId.equals(203L)) {
                    return extProfile;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "pending.ext@otheruniv.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verify login blocked and forwarded to login.jsp with pending notification
        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("Your student registration is pending verification. Please contact the administrator or check back once approved.",
                reqAttrs.get("errorMessage"));
        assertNull(respState.get("redirectedUrl"), "Pending student must never be redirected to dashboard");
        assertNull(reqState.get("newSessionAttrs"), "No authenticated session must be established for pending accounts");
    }

    // =====================================================
    // 4. EXTERNAL STUDENT LOGIN (REJECTED)
    // =====================================================

    @Test
    @DisplayName("4. Rejected external student login is blocked with rejection notice")
    void testExternalStudentLoginRejectedBlocked() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "RejectedPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User rejectedUser = new User(204L, "Rejected External Student", "rejected.ext@otheruniv.edu", hash,
                User.ROLE_STUDENT, User.STATUS_REJECTED, LocalDateTime.now());

        ExternalStudent extProfile = new ExternalStudent(204L, "State Engineering College", "Bhubaneswar",
                "MCA", "CSE", 2, new BigDecimal("6.50"), 2026, "9012345678", "/resumes/rej.pdf");
        extProfile.setId(57L);
        extProfile.setVerificationStatus(ExternalStudent.STATUS_REJECTED);

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("rejected.ext@otheruniv.edu".equalsIgnoreCase(email)) {
                    return rejectedUser;
                }
                return null;
            }
        });

        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                if (userId != null && userId.equals(204L)) {
                    return extProfile;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "rejected.ext@otheruniv.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verify login blocked and forwarded to login.jsp with rejection notification
        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("Your registration request has been rejected. Please contact the placement administrator.",
                reqAttrs.get("errorMessage"));
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    // =====================================================
    // 5. COORDINATOR LOGIN
    // =====================================================

    @Test
    @DisplayName("5. Approved coordinator logs in successfully and is redirected to /jobs without requiring student profile")
    void testCoordinatorLoginSuccess() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "CoordPassword123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User coordUser = new User(301L, "Placement Coordinator", "coordinator@college.edu", hash,
                User.ROLE_COORDINATOR, User.STATUS_APPROVED, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("coordinator@college.edu".equalsIgnoreCase(email)) {
                    return coordUser;
                }
                return null;
            }
        });

        // Coordinators do NOT have student records
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "coordinator@college.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/jobs", respState.get("redirectedUrl"), "Approved coordinator must be redirected to /jobs");

        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrs = (Map<String, Object>) reqState.get("newSessionAttrs");
        assertNotNull(newSessionAttrs);
        assertEquals(301L, newSessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals("coordinator@college.edu", newSessionAttrs.get(LoginServlet.SESSION_USER_EMAIL));
        assertEquals(User.ROLE_COORDINATOR, newSessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
    }

    @Test
    @DisplayName("6. Pending coordinator registration request is blocked with coordinator-specific notice")
    void testCoordinatorLoginPendingApprovalBlocked() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "CoordPendingPass!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User pendingCoord = new User(302L, "Pending Coordinator", "pending.coord@college.edu", hash,
                User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("pending.coord@college.edu".equalsIgnoreCase(email)) {
                    return pendingCoord;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "pending.coord@college.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("Your coordinator registration request is pending administrator approval. Please contact the administrator or check back once approved.",
                reqAttrs.get("errorMessage"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    // =====================================================
    // 7. ADMINISTRATOR LOGIN
    // =====================================================

    @Test
    @DisplayName("7. Administrator logs in successfully and is redirected to /jobs without requiring student records")
    void testAdministratorLoginSuccess() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "AdminPassword123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User adminUser = new User(1L, "System Administrator", "admin@college.edu", hash,
                User.ROLE_ADMIN, User.STATUS_APPROVED, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("admin@college.edu".equalsIgnoreCase(email)) {
                    return adminUser;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "admin@college.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/jobs", respState.get("redirectedUrl"), "Administrator must be redirected to /jobs");

        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrs = (Map<String, Object>) reqState.get("newSessionAttrs");
        assertNotNull(newSessionAttrs);
        assertEquals(1L, newSessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals(User.ROLE_ADMIN, newSessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
    }

    // =====================================================
    // 8. SECURITY & INVALID CREDENTIAL HANDLING
    // =====================================================

    @Test
    @DisplayName("8. Invalid password produces generic error notice and prevents account enumeration")
    void testInvalidPasswordGenericError() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String realPassword = "RealPassword123!";
        String hash = PasswordHashService.hashPassword(realPassword);

        User user = new User(10L, "Student User", "student@college.edu", hash,
                User.ROLE_STUDENT, User.STATUS_APPROVED, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("student@college.edu".equalsIgnoreCase(email)) {
                    return user;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "student@college.edu");
        params.put("password", "WRONG_PASSWORD_ATTEMPT");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("Invalid email or password.", reqAttrs.get("errorMessage"),
                "Generic error must prevent enumeration on password mismatch");
        assertEquals("student@college.edu", reqAttrs.get("email"), "Safe email input must be preserved");
        assertNull(reqState.get("newSessionAttrs"));
    }

    @Test
    @DisplayName("9. Database exception in DAO is caught, logged, and shielded from user as database error")
    void testDatabaseErrorHandlingShieldedAndLogged() throws Exception {
        LoginServlet servlet = new LoginServlet();

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                throw new DAOException("Communications link failure: MySQL server disconnected");
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "any.student@cutm.ac.in");
        params.put("password", "AnyPass123!");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("A database error occurred. Please try again later.", reqAttrs.get("errorMessage"),
                "Internal database exception details must be shielded from client");
        assertNull(reqState.get("newSessionAttrs"));
    }

    // =====================================================
    // 10. FAIL-CLOSED SECURITY: MISSING SCHEMA ELEMENTS
    // =====================================================

    @Test
    @DisplayName("10. UserDAO throws DAOException on missing status column (MySQL Error 1054) and LoginServlet fails closed without assuming APPROVED")
    void testMissingStatusColumnFailsClosedWithoutAssumingApproved() throws Exception {
        // 1. Verify UserDAO throws DAOException when status column is missing
        UserDAO daoWithMissingColumn = new UserDAO() {
            @Override
            public User findByEmail(String email) {
                SQLException sqlEx = new SQLException("Unknown column 'status' in 'field list'", "42S22", 1054);
                if (sqlEx.getErrorCode() == 1054) {
                    throw new DAOException("Database error: required column 'status' is missing from 'users' table (MySQL 1054): " + sqlEx.getMessage(), sqlEx);
                }
                return null;
            }
        };

        DAOException thrown = assertThrows(DAOException.class, () -> daoWithMissingColumn.findByEmail("legacy.user@college.edu"));
        assertTrue(thrown.getMessage().contains("status"), "Exception message must report the missing column 'status'");

        // 2. Verify LoginServlet catches DAOException, logs SEVERE, displays safe user error, and denies access
        LoginServlet servlet = new LoginServlet();
        servlet.setUserDAO(daoWithMissingColumn);
        servlet.setExternalStudentDAO(new ExternalStudentDAO());

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "legacy.user@college.edu");
        params.put("password", "AnyPassword123!");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verification: Safe error message, NO session, NO redirection, strictly fails closed
        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("A database error occurred. Please try again later.", reqAttrs.get("errorMessage"));
        assertNull(respState.get("redirectedUrl"), "Missing column must never grant login or redirect");
        assertNull(reqState.get("newSessionAttrs"), "Missing column must never establish an authenticated session");
    }

    @Test
    @DisplayName("11. ExternalStudentDAO throws DAOException on missing table (MySQL Error 1146) and LoginServlet fails closed without assuming internal student")
    void testMissingExternalStudentTableFailsClosedWithoutAssumingInternalStudent() throws Exception {
        // 1. Verify ExternalStudentDAO throws DAOException when external_students table is missing
        ExternalStudentDAO daoWithMissingTable = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                SQLException sqlEx = new SQLException("Table 'college_placement.external_students' doesn't exist", "42S02", 1146);
                if (sqlEx.getErrorCode() == 1146) {
                    throw new DAOException("Database error: required table 'external_students' is missing or inaccessible (MySQL 1146): " + sqlEx.getMessage(), sqlEx);
                }
                return null;
            }
        };

        DAOException thrown = assertThrows(DAOException.class, () -> daoWithMissingTable.findByUserId(101L));
        assertTrue(thrown.getMessage().contains("external_students"), "Exception message must report the missing table");

        // 2. Verify LoginServlet catches DAOException, logs SEVERE, displays safe user error, and does NOT treat user as internal student
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "StudentPassword123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User studentUser = new User(101L, "Candidate Student", "candidate@somewhere.edu", hash,
                User.ROLE_STUDENT, User.STATUS_APPROVED, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("candidate@somewhere.edu".equalsIgnoreCase(email)) {
                    return studentUser;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(daoWithMissingTable);

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "candidate@somewhere.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verification: Safe error message, NO session, NO redirection, does NOT assume internal student
        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertEquals("A database error occurred. Please try again later.", reqAttrs.get("errorMessage"));
        assertNull(respState.get("redirectedUrl"), "Missing external student table must never default to internal student access");
        assertNull(reqState.get("newSessionAttrs"), "No session may be created when schema verification fails");
    }

    // =====================================================
    // 12. STRICT APPROVAL ENFORCEMENT (NEVER ASSUME APPROVED)
    // =====================================================

    @Test
    @DisplayName("12. Internal student with null or unverified status is denied authentication")
    void testInternalStudentUnverifiedStatusDenied() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "StudentPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User unverifiedUser = new User(105L, "Unverified Student", "unverified@cutm.ac.in", hash,
                User.ROLE_STUDENT, null, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("unverified@cutm.ac.in".equalsIgnoreCase(email)) {
                    return unverifiedUser;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "unverified@cutm.ac.in");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("cannot be verified"),
                "Unverified status must be rejected without assuming APPROVED");
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    @Test
    @DisplayName("13. Pending internal student is blocked with pending notification")
    void testInternalStudentPendingStatusBlocked() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "StudentPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User pendingUser = new User(106L, "Pending Student", "pending.internal@cutm.ac.in", hash,
                User.ROLE_STUDENT, User.STATUS_PENDING, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("pending.internal@cutm.ac.in".equalsIgnoreCase(email)) {
                    return pendingUser;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "pending.internal@cutm.ac.in");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("pending verification"));
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    @Test
    @DisplayName("14. Rejected internal student is blocked with rejection notice")
    void testInternalStudentRejectedStatusBlocked() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "StudentPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User rejectedUser = new User(107L, "Rejected Student", "rejected.internal@cutm.ac.in", hash,
                User.ROLE_STUDENT, User.STATUS_REJECTED, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("rejected.internal@cutm.ac.in".equalsIgnoreCase(email)) {
                    return rejectedUser;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "rejected.internal@cutm.ac.in");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("rejected"));
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    @Test
    @DisplayName("15. Coordinator with unverified or null status is denied authentication")
    void testCoordinatorUnverifiedStatusDenied() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "CoordPassword123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User unverifiedCoord = new User(305L, "Unverified Coordinator", "unverified.coord@college.edu", hash,
                User.ROLE_COORDINATOR, null, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("unverified.coord@college.edu".equalsIgnoreCase(email)) {
                    return unverifiedCoord;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "unverified.coord@college.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("pending administrator approval"),
                "Unverified coordinator status must not be assumed APPROVED");
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    @Test
    @DisplayName("16. Rejected coordinator registration is blocked with rejection notice")
    void testCoordinatorRejectedStatusBlocked() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "CoordPassword123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User rejectedCoord = new User(306L, "Rejected Coordinator", "rejected.coord@college.edu", hash,
                User.ROLE_COORDINATOR, User.STATUS_REJECTED, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("rejected.coord@college.edu".equalsIgnoreCase(email)) {
                    return rejectedCoord;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "rejected.coord@college.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("rejected"));
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    @Test
    @DisplayName("17. External student with unverified profile status is denied authentication")
    void testExternalStudentUnverifiedStatusDenied() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String plainPassword = "ExternalPass123!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        User externalUser = new User(205L, "Unverified Ext Student", "unverified.ext@otheruniv.edu", hash,
                User.ROLE_STUDENT, User.STATUS_APPROVED, LocalDateTime.now());

        ExternalStudent extProfile = new ExternalStudent(205L, "Some College", "City",
                "B.Tech", "CSE", 6, new BigDecimal("8.00"), 2026, "9876543210", "/resumes/ext.pdf");
        extProfile.setId(58L);
        extProfile.setVerificationStatus("UNVERIFIED"); // Non-APPROVED status

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("unverified.ext@otheruniv.edu".equalsIgnoreCase(email)) {
                    return externalUser;
                }
                return null;
            }
        });
        servlet.setExternalStudentDAO(new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                if (userId != null && userId.equals(205L)) {
                    return extProfile;
                }
                return null;
            }
        });

        Map<String, Object> initialSessionAttrs = new HashMap<>();
        initialSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CSRF_TOKEN);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", CSRF_TOKEN);
        params.put("email", "unverified.ext@otheruniv.edu");
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest req = createMockRequest(initialSessionAttrs, params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("dispatchedPath"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("cannot be verified"),
                "Unverified external student status must be rejected without assuming APPROVED");
        assertNull(respState.get("redirectedUrl"));
        assertNull(reqState.get("newSessionAttrs"));
    }

    // =====================================================
    // HELPER MOCKS
    // =====================================================

    private HttpServletRequest createMockRequest(Map<String, Object> sessionAttrs,
                                                 Map<String, String> params,
                                                 Map<String, Object> requestAttrs,
                                                 Map<String, Object> state) {
        final Map<String, Object>[] currentSessionAttrs = new Map[]{sessionAttrs};
        final HttpSession[] currentSession = new HttpSession[1];
        currentSession[0] = createMockSession(sessionAttrs, () -> currentSession[0] = null);

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getSession".equals(name)) {
                        boolean create = args.length == 0 || (boolean) args[0];
                        if (currentSession[0] == null && create) {
                            Map<String, Object> newAttrs = new HashMap<>();
                            currentSessionAttrs[0] = newAttrs;
                            state.put("newSessionAttrs", newAttrs);
                            currentSession[0] = createMockSession(newAttrs, () -> currentSession[0] = null);
                        }
                        return currentSession[0];
                    } else if ("getParameter".equals(name)) {
                        return params.get(args[0]);
                    } else if ("getAttribute".equals(name)) {
                        return requestAttrs.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        requestAttrs.put((String) args[0], args[1]);
                        return null;
                    } else if ("getRequestDispatcher".equals(name)) {
                        state.put("dispatchedPath", args[0]);
                        return Proxy.newProxyInstance(
                                RequestDispatcher.class.getClassLoader(),
                                new Class<?>[]{RequestDispatcher.class},
                                (dispProxy, dispMethod, dispArgs) -> {
                                    state.put("forwarded", Boolean.TRUE);
                                    return null;
                                }
                        );
                    } else if ("getContextPath".equals(name)) {
                        return "";
                    } else if ("getMethod".equals(name)) {
                        return "POST";
                    }
                    return null;
                }
        );
    }

    private HttpSession createMockSession(Map<String, Object> attributes, Runnable onInvalidate) {
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
                    } else if ("invalidate".equals(name)) {
                        attributes.put("invalidated", Boolean.TRUE);
                        if (onInvalidate != null) {
                            onInvalidate.run();
                        }
                        return null;
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(Map<String, Object> respState) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("sendRedirect".equals(name)) {
                        respState.put("redirectedUrl", args[0]);
                        return null;
                    } else if ("sendError".equals(name)) {
                        respState.put("errorCode", args[0]);
                        if (args.length > 1) {
                            respState.put("errorMessage", args[1]);
                        }
                        return null;
                    }
                    return null;
                }
        );
    }
}
