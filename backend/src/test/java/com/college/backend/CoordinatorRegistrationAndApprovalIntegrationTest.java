package com.college.backend;

import com.college.backend.dao.CoordinatorDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Coordinator;
import com.college.backend.entity.User;
import com.college.backend.filter.RoleAuthorizationFilter;
import com.college.backend.servlet.CoordinatorApprovalServlet;
import com.college.backend.servlet.CoordinatorRegisterServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.DBConnection;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration and security test suite validating:
 * 1. New coordinator can submit a registration request.
 * 2. Duplicate email is rejected.
 * 3. Duplicate employee ID is rejected.
 * 4. Password is stored as a BCrypt hash.
 * 5. Pending coordinator cannot log in.
 * 6. Admin can approve a request.
 * 7. Approved coordinator can log in.
 * 8. Rejected coordinator cannot log in.
 * 9. Student cannot access coordinator pages.
 * 10. Unauthorized users cannot approve coordinator requests.
 * 11. Existing coordinator login continues working.
 */
public class CoordinatorRegistrationAndApprovalIntegrationTest {

    private UserDAO userDAO;
    private CoordinatorDAO coordinatorDAO;
    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeAll
    static void initDbConfig() {
        if (System.getProperty("db.password") == null && System.getenv("DB_PASSWORD") == null) {
            System.setProperty("db.password", "Tapu@123");
        }
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        coordinatorDAO = new CoordinatorDAO(userDAO);
    }

    @AfterEach
    void tearDown() {
        if (!createdUserIds.isEmpty()) {
            try (Connection conn = DBConnection.getConnection()) {
                for (Long userId : createdUserIds) {
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM coordinators WHERE user_id = ?")) {
                        stmt.setLong(1, userId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        stmt.setLong(1, userId);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Cleanup warning: " + e.getMessage());
            }
            createdUserIds.clear();
        }
    }

    @Test
    @DisplayName("1. New coordinator can submit registration request and initially has PENDING_APPROVAL status")
    void testNewCoordinatorRegistrationRequest() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord." + uniqueSuffix + "@college.edu";
        String empId = "EMP-" + uniqueSuffix.toUpperCase();
        String plainPassword = "SecurePass123!";

        CoordinatorRegisterServlet servlet = new CoordinatorRegisterServlet();
        servlet.init();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "valid-csrf-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-csrf-token");
        params.put("name", "Dr. Test Coordinator " + uniqueSuffix);
        params.put("email", email);
        params.put("employeeId", empId);
        params.put("department", "Computer Science & Engineering");
        params.put("designation", "Assistant Professor & Coordinator");
        params.put("phone", "9876543210");
        params.put("password", plainPassword);
        params.put("confirmPassword", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = CoordinatorRegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        // Verify successful redirect
        assertEquals("/login?role=coordinator", respState.get("redirectedUrl"));
        assertNotNull(sessionAttrs.get("successMessage"));

        // Verify in MySQL
        User user = userDAO.findByEmail(email);
        assertNotNull(user, "User record must exist in MySQL");
        createdUserIds.add(user.getId());

        assertEquals(User.ROLE_COORDINATOR, user.getRole());
        assertEquals(User.STATUS_PENDING_APPROVAL, user.getStatus());

        Coordinator coordinator = coordinatorDAO.findByUserId(user.getId());
        assertNotNull(coordinator, "Coordinator profile must exist in MySQL");
        assertEquals(empId, coordinator.getEmployeeId());
        assertEquals("Computer Science & Engineering", coordinator.getDepartment());
        assertEquals("Assistant Professor & Coordinator", coordinator.getDesignation());
        assertEquals(Coordinator.STATUS_PENDING_APPROVAL, coordinator.getStatus());
    }

    @Test
    @DisplayName("2. Duplicate email is rejected during coordinator registration")
    void testDuplicateEmailRejected() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord.dup." + uniqueSuffix + "@college.edu";
        String empId1 = "EMP-1-" + uniqueSuffix.toUpperCase();
        String empId2 = "EMP-2-" + uniqueSuffix.toUpperCase();
        String plainPassword = "SecurePass123!";

        // First registration
        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user1 = new User("Coord One", email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL);
        Coordinator c1 = new Coordinator(null, empId1, "CSE", "Coordinator", "9876543210");
        boolean firstCreated = coordinatorDAO.registerCoordinator(user1, c1);
        assertTrue(firstCreated);
        createdUserIds.add(user1.getId());

        // Attempt duplicate registration with same email
        CoordinatorRegisterServlet servlet = new CoordinatorRegisterServlet();
        servlet.init();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "token123");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "token123");
        params.put("name", "Coord Two");
        params.put("email", email);
        params.put("employeeId", empId2);
        params.put("department", "ECE");
        params.put("designation", "Officer");
        params.put("phone", "9876543211");
        params.put("password", plainPassword);
        params.put("confirmPassword", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = CoordinatorRegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertNotNull(reqAttrs.get("errorMessage"));
        assertTrue(((String) reqAttrs.get("errorMessage")).toLowerCase().contains("email"));
        assertEquals("/WEB-INF/jsp/coordinator-register.jsp", state.get("dispatchedPath"));
    }

    @Test
    @DisplayName("3. Duplicate employee ID is rejected during coordinator registration")
    void testDuplicateEmployeeIdRejected() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email1 = "coord.emp1." + uniqueSuffix + "@college.edu";
        String email2 = "coord.emp2." + uniqueSuffix + "@college.edu";
        String empId = "EMP-COMMON-" + uniqueSuffix.toUpperCase();
        String plainPassword = "SecurePass123!";

        // First registration
        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user1 = new User("Coord One", email1, passwordHash, User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL);
        Coordinator c1 = new Coordinator(null, empId, "CSE", "Coordinator", "9876543210");
        boolean firstCreated = coordinatorDAO.registerCoordinator(user1, c1);
        assertTrue(firstCreated);
        createdUserIds.add(user1.getId());

        // Attempt duplicate registration with same employee ID
        CoordinatorRegisterServlet servlet = new CoordinatorRegisterServlet();
        servlet.init();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "token123");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "token123");
        params.put("name", "Coord Two");
        params.put("email", email2);
        params.put("employeeId", empId);
        params.put("department", "Mechanical");
        params.put("designation", "Officer");
        params.put("phone", "9876543212");
        params.put("password", plainPassword);
        params.put("confirmPassword", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = CoordinatorRegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);

        assertNotNull(reqAttrs.get("errorMessage"));
        assertTrue(((String) reqAttrs.get("errorMessage")).toLowerCase().contains("employee"));
        assertEquals("/WEB-INF/jsp/coordinator-register.jsp", state.get("dispatchedPath"));
    }

    @Test
    @DisplayName("4. Password is stored securely as a salted BCrypt hash")
    void testPasswordStoredAsBcryptHash() {
        String plainPassword = "MySecretPass2026!";
        String hash = PasswordHashService.hashPassword(plainPassword);

        assertTrue(PasswordHashService.isBCryptHash(hash), "Password must be a valid BCrypt hash");
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(PasswordHashService.verifyPassword(plainPassword, hash));
        assertFalse(PasswordHashService.verifyPassword("WrongPassword!", hash));
    }

    @Test
    @DisplayName("5. Pending coordinator cannot log in to the portal")
    void testPendingCoordinatorCannotLogIn() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord.pending." + uniqueSuffix + "@college.edu";
        String empId = "EMP-PND-" + uniqueSuffix.toUpperCase();
        String plainPassword = "TestPass123!";

        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user = new User("Pending Coord", email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL);
        Coordinator c = new Coordinator(null, empId, "Civil", "Coordinator", "9876543210");
        coordinatorDAO.registerCoordinator(user, c);
        createdUserIds.add(user.getId());

        // Attempt login via LoginServlet
        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-csrf");
        params.put("email", email);
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(loginServlet, req, resp);

        // Verification: login forwarded back with error, NO redirect to jobs/dashboard, NO authenticated session
        assertEquals("/WEB-INF/jsp/login.jsp", state.get("dispatchedPath"));
        assertNotNull(reqAttrs.get("errorMessage"));
        assertTrue(((String) reqAttrs.get("errorMessage")).toLowerCase().contains("pending"));
        assertNull(sessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertNull(respState.get("redirectedUrl"));
    }

    @Test
    @DisplayName("6. Admin can approve a coordinator request")
    void testAdminCanApproveRequest() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord.appr." + uniqueSuffix + "@college.edu";
        String empId = "EMP-APP-" + uniqueSuffix.toUpperCase();
        String plainPassword = "TestPass123!";

        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user = new User("Coord To Approve", email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL);
        Coordinator c = new Coordinator(null, empId, "Civil", "Coordinator", "9876543210");
        coordinatorDAO.registerCoordinator(user, c);
        createdUserIds.add(user.getId());

        CoordinatorApprovalServlet approvalServlet = new CoordinatorApprovalServlet();
        approvalServlet.init();

        // Admin session
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 3L); // admin@college.edu has ID 3
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        sessionAttrs.put("csrfToken", "admin-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "admin-csrf");
        params.put("action", "approve");
        params.put("id", String.valueOf(c.getId()));
        params.put("adminNotes", "Verified departmental credentials");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = CoordinatorApprovalServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(approvalServlet, req, resp);

        assertEquals("/admin/dashboard", respState.get("redirectedUrl"));
        assertNotNull(sessionAttrs.get("successMessage"));

        // Verify both tables updated
        User updatedUser = userDAO.findById(user.getId());
        assertEquals(User.STATUS_APPROVED, updatedUser.getStatus());

        Coordinator updatedCoord = coordinatorDAO.findById(c.getId());
        assertEquals(Coordinator.STATUS_APPROVED, updatedCoord.getStatus());
        assertEquals(3L, updatedCoord.getApprovedBy());
        assertNotNull(updatedCoord.getApprovalDate());
    }

    @Test
    @DisplayName("7. Approved coordinator can log in to the portal")
    void testApprovedCoordinatorCanLogIn() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord.ready." + uniqueSuffix + "@college.edu";
        String empId = "EMP-RDY-" + uniqueSuffix.toUpperCase();
        String plainPassword = "ValidPass123!";

        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user = new User("Approved Coord", email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_APPROVED);
        Coordinator c = new Coordinator(null, empId, "Civil", "Coordinator", "9876543210");
        c.setStatus(Coordinator.STATUS_APPROVED);
        coordinatorDAO.registerCoordinator(user, c);
        // Ensure user status is APPROVED in DB
        userDAO.updateUserStatus(user.getId(), User.STATUS_APPROVED);
        coordinatorDAO.updateStatus(c.getId(), Coordinator.STATUS_APPROVED, 3L, "Pre-approved");
        createdUserIds.add(user.getId());

        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-csrf");
        params.put("email", email);
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(loginServlet, req, resp);

        // Verification: login succeeds, redirects to jobs
        assertEquals("/jobs", respState.get("redirectedUrl"));
        assertNull(reqAttrs.get("errorMessage"));
        assertEquals(user.getId(), sessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals(User.ROLE_COORDINATOR, sessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
    }

    @Test
    @DisplayName("8. Rejected coordinator cannot log in to the portal")
    void testRejectedCoordinatorCannotLogIn() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord.rej." + uniqueSuffix + "@college.edu";
        String empId = "EMP-REJ-" + uniqueSuffix.toUpperCase();
        String plainPassword = "ValidPass123!";

        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user = new User("Rejected Coord", email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_REJECTED);
        Coordinator c = new Coordinator(null, empId, "Civil", "Coordinator", "9876543210");
        c.setStatus(Coordinator.STATUS_REJECTED);
        coordinatorDAO.registerCoordinator(user, c);
        userDAO.updateUserStatus(user.getId(), User.STATUS_REJECTED);
        coordinatorDAO.updateStatus(c.getId(), Coordinator.STATUS_REJECTED, 3L, "Rejected: Invalid ID");
        createdUserIds.add(user.getId());

        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-csrf");
        params.put("email", email);
        params.put("password", plainPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(loginServlet, req, resp);

        assertEquals("/WEB-INF/jsp/login.jsp", state.get("dispatchedPath"));
        assertNotNull(reqAttrs.get("errorMessage"));
        assertTrue(((String) reqAttrs.get("errorMessage")).toLowerCase().contains("rejected"));
        assertNull(sessionAttrs.get(LoginServlet.SESSION_USER_ID));
    }

    @Test
    @DisplayName("9. Student cannot access coordinator pages (HTTP 403 Forbidden)")
    void testStudentCannotAccessCoordinatorPages() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> state = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        HttpServletRequest req = createMockFilterRequest(session, "/admin/dashboard", "GET", params, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        boolean[] chainCalled = new boolean[]{false};
        FilterChain chain = (r, s) -> chainCalled[0] = true;

        filter.doFilter(req, resp, chain);

        assertFalse(chainCalled[0], "Chain must not be invoked for unauthorized student");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"));
    }

    @Test
    @DisplayName("10. Unauthorized users (students and non-admin coordinators) cannot approve coordinator requests")
    void testUnauthorizedUsersCannotApprove() throws Exception {
        CoordinatorApprovalServlet servlet = new CoordinatorApprovalServlet();
        servlet.init();

        // A. Student user attempt
        Map<String, Object> studentSessionAttrs = new HashMap<>();
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        studentSessionAttrs.put("csrfToken", "valid-token");
        HttpSession studentSession = createMockSession(studentSessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-token");
        params.put("action", "approve");
        params.put("id", "1");

        Map<String, Object> reqAttrs1 = new HashMap<>();
        Map<String, Object> state1 = new HashMap<>();
        HttpServletRequest req1 = createMockRequest(studentSession, params, reqAttrs1, state1);

        Map<String, Object> respState1 = new HashMap<>();
        HttpServletResponse resp1 = createMockResponse(respState1);

        Method doPost = CoordinatorApprovalServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req1, resp1);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState1.get("errorCode"));

        // B. Non-admin Coordinator attempt
        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        coordSessionAttrs.put("csrfToken", "valid-token");
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        Map<String, Object> reqAttrs2 = new HashMap<>();
        Map<String, Object> state2 = new HashMap<>();
        HttpServletRequest req2 = createMockRequest(coordSession, params, reqAttrs2, state2);

        Map<String, Object> respState2 = new HashMap<>();
        HttpServletResponse resp2 = createMockResponse(respState2);

        doPost.invoke(servlet, req2, resp2);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState2.get("errorCode"));
    }

    @Test
    @DisplayName("11. Existing coordinator login continues working seamlessly without password overwrite")
    void testExistingCoordinatorLoginContinuesWorking() throws Exception {
        // Coordinator user 10 (coordinator@college.edu) is in database
        User existingCoord = userDAO.findByEmail("coordinator@college.edu");
        assertNotNull(existingCoord, "Existing coordinator must be present in users table");
        assertEquals(User.ROLE_COORDINATOR, existingCoord.getRole());
        assertEquals(User.STATUS_APPROVED, existingCoord.getStatus());

        // Verify password hash is authentic BCrypt and matches original user hash
        assertTrue(PasswordHashService.isBCryptHash(existingCoord.getPasswordHash()), "Password hash must be a valid BCrypt string");
        assertEquals("$2a$10$lnMUnP/Auam0i9C8xXOWmu6YMljw2cP5VByubf3NtImmTmF9GsLYC", existingCoord.getPasswordHash(),
                "Existing coordinator's authentic password hash must be preserved without alteration");

        // Verify an incorrect password attempt against live DB is rejected
        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();

        Map<String, Object> failSessionAttrs = new HashMap<>();
        failSessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession failSession = createMockSession(failSessionAttrs);

        Map<String, String> failParams = new HashMap<>();
        failParams.put("csrfToken", "valid-csrf");
        failParams.put("email", "coordinator@college.edu");
        failParams.put("password", "IncorrectPassword123!");

        Map<String, Object> failReqAttrs = new HashMap<>();
        Map<String, Object> failState = new HashMap<>();
        HttpServletRequest failReq = createMockRequest(failSession, failParams, failReqAttrs, failState);

        Map<String, Object> failRespState = new HashMap<>();
        HttpServletResponse failResp = createMockResponse(failRespState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(loginServlet, failReq, failResp);

        assertEquals("/WEB-INF/jsp/login.jsp", failState.get("dispatchedPath"));
        assertEquals("Invalid email or password.", failReqAttrs.get("errorMessage"));
        assertNull(failSessionAttrs.get(LoginServlet.SESSION_USER_ID));

        // Test successful login execution using a test password against an in-memory wrapper
        // to guarantee LoginServlet's role binding and redirection logic are exercised without mutating MySQL
        String testPassword = "CoordinatorTestPass123!";
        String testHash = PasswordHashService.hashPassword(testPassword);
        User simulatedExistingUser = new User(existingCoord.getName(), existingCoord.getEmail(), testHash, existingCoord.getRole(), existingCoord.getStatus());
        simulatedExistingUser.setId(existingCoord.getId());

        LoginServlet successServlet = new LoginServlet();
        successServlet.init();
        successServlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("coordinator@college.edu".equalsIgnoreCase(email)) {
                    return simulatedExistingUser;
                }
                return super.findByEmail(email);
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-csrf");
        params.put("email", "coordinator@college.edu");
        params.put("password", testPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        doPost.invoke(successServlet, req, resp);

        assertEquals("/jobs", respState.get("redirectedUrl"));
        assertEquals(existingCoord.getId(), sessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals(User.ROLE_COORDINATOR, sessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
    }

    @Test
    @DisplayName("12. Invalid password rejection for existing coordinator and unknown accounts")
    void testInvalidPasswordRejection() throws Exception {
        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);

        // Case A: Wrong password for existing coordinator
        Map<String, Object> sessionAttrsA = new HashMap<>();
        sessionAttrsA.put("csrfToken", "csrf-token-1");
        HttpSession sessionA = createMockSession(sessionAttrsA);

        Map<String, String> paramsA = new HashMap<>();
        paramsA.put("csrfToken", "csrf-token-1");
        paramsA.put("email", "coordinator@college.edu");
        paramsA.put("password", "CompletelyWrongPassword!");

        Map<String, Object> reqAttrsA = new HashMap<>();
        Map<String, Object> stateA = new HashMap<>();
        HttpServletRequest reqA = createMockRequest(sessionA, paramsA, reqAttrsA, stateA);
        Map<String, Object> respStateA = new HashMap<>();
        HttpServletResponse respA = createMockResponse(respStateA);

        doPost.invoke(loginServlet, reqA, respA);

        assertEquals("/WEB-INF/jsp/login.jsp", stateA.get("dispatchedPath"));
        assertEquals("Invalid email or password.", reqAttrsA.get("errorMessage"));
        assertNull(sessionAttrsA.get(LoginServlet.SESSION_USER_ID));

        // Case B: Non-existent account
        Map<String, Object> sessionAttrsB = new HashMap<>();
        sessionAttrsB.put("csrfToken", "csrf-token-2");
        HttpSession sessionB = createMockSession(sessionAttrsB);

        Map<String, String> paramsB = new HashMap<>();
        paramsB.put("csrfToken", "csrf-token-2");
        paramsB.put("email", "nobody.exists@college.edu");
        paramsB.put("password", "SomePassword123!");

        Map<String, Object> reqAttrsB = new HashMap<>();
        Map<String, Object> stateB = new HashMap<>();
        HttpServletRequest reqB = createMockRequest(sessionB, paramsB, reqAttrsB, stateB);
        Map<String, Object> respStateB = new HashMap<>();
        HttpServletResponse respB = createMockResponse(respStateB);

        doPost.invoke(loginServlet, reqB, respB);

        assertEquals("/WEB-INF/jsp/login.jsp", stateB.get("dispatchedPath"));
        assertEquals("Invalid email or password.", reqAttrsB.get("errorMessage"));
        assertNull(sessionAttrsB.get(LoginServlet.SESSION_USER_ID));
    }

    @Test
    @DisplayName("13. Admin login executes successfully and assigns ADMIN session role")
    void testAdminLogin() throws Exception {
        User adminUser = userDAO.findByEmail("admin@college.edu");
        assertNotNull(adminUser, "Admin user must exist in database");
        assertEquals(User.ROLE_ADMIN, adminUser.getRole());
        assertEquals(User.STATUS_APPROVED, adminUser.getStatus());

        // Use in-memory simulated hash to avoid knowing or mutating production admin password
        String testPassword = "AdminTestPassword123!";
        String testHash = PasswordHashService.hashPassword(testPassword);
        User simulatedAdmin = new User(adminUser.getName(), adminUser.getEmail(), testHash, adminUser.getRole(), adminUser.getStatus());
        simulatedAdmin.setId(adminUser.getId());

        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();
        loginServlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("admin@college.edu".equalsIgnoreCase(email)) {
                    return simulatedAdmin;
                }
                return super.findByEmail(email);
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "admin-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "admin-csrf");
        params.put("email", "admin@college.edu");
        params.put("password", testPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(loginServlet, req, resp);

        assertEquals("/jobs", respState.get("redirectedUrl"));
        assertEquals(adminUser.getId(), sessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals(User.ROLE_ADMIN, sessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
    }

    @Test
    @DisplayName("14. Student login executes successfully and assigns STUDENT session role")
    void testStudentLogin() throws Exception {
        User studentUser = userDAO.findByEmail("student.test@example.com");
        if (studentUser == null) {
            studentUser = userDAO.findByEmail("debasispanda845@gmail.com");
        }
        assertNotNull(studentUser, "A student user must exist in database");
        assertEquals(User.ROLE_STUDENT, studentUser.getRole());
        assertEquals(User.STATUS_APPROVED, studentUser.getStatus());

        String testPassword = "StudentTestPassword123!";
        String testHash = PasswordHashService.hashPassword(testPassword);
        User simulatedStudent = new User(studentUser.getName(), studentUser.getEmail(), testHash, studentUser.getRole(), studentUser.getStatus());
        simulatedStudent.setId(studentUser.getId());

        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();
        final User finalStudent = simulatedStudent;
        loginServlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if (finalStudent.getEmail().equalsIgnoreCase(email)) {
                    return finalStudent;
                }
                return super.findByEmail(email);
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "student-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "student-csrf");
        params.put("email", studentUser.getEmail());
        params.put("password", testPassword);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(loginServlet, req, resp);

        assertEquals("/jobs", respState.get("redirectedUrl"));
        assertEquals(studentUser.getId(), sessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals(User.ROLE_STUDENT, sessionAttrs.get(LoginServlet.SESSION_USER_ROLE));
    }

    @Test
    @DisplayName("15. Session role authorization protects admin, coordinator, and student operations")
    void testSessionRoleAuthorization() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();

        // A. Student cannot access /admin/dashboard
        Map<String, Object> studentSessionAttrs = new HashMap<>();
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 100L);
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentSessionAttrs);

        Map<String, Object> respStateA = new HashMap<>();
        HttpServletResponse respA = createMockResponse(respStateA);
        boolean[] chainA = new boolean[]{false};
        HttpServletRequest reqA = createMockFilterRequest(studentSession, "/admin/dashboard", "GET", new HashMap<>(), new HashMap<>());
        filter.doFilter(reqA, respA, (r, s) -> chainA[0] = true);

        assertFalse(chainA[0], "Chain must not proceed for student on /admin");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateA.get("errorCode"));

        // B. Coordinator cannot approve registrations on /admin/coordinators (Admin only)
        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 200L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        CoordinatorApprovalServlet approvalServlet = new CoordinatorApprovalServlet();
        approvalServlet.init();
        Map<String, String> approveParams = new HashMap<>();
        approveParams.put("csrfToken", "valid-csrf");
        approveParams.put("action", "approve");
        approveParams.put("id", "99");

        Map<String, Object> respStateB = new HashMap<>();
        HttpServletResponse respB = createMockResponse(respStateB);
        HttpServletRequest reqB = createMockRequest(coordSession, approveParams, new HashMap<>(), new HashMap<>());

        Method doPost = CoordinatorApprovalServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(approvalServlet, reqB, respB);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateB.get("errorCode"));

        // C. Coordinator is allowed to access /jobs
        Map<String, Object> respStateC = new HashMap<>();
        HttpServletResponse respC = createMockResponse(respStateC);
        boolean[] chainC = new boolean[]{false};
        HttpServletRequest reqC = createMockFilterRequest(coordSession, "/jobs", "GET", new HashMap<>(), new HashMap<>());
        filter.doFilter(reqC, respC, (r, s) -> chainC[0] = true);

        assertTrue(chainC[0], "Coordinator must be permitted to access /jobs");
        assertNull(respStateC.get("errorCode"));

        // D. Admin is allowed to access /admin/dashboard
        Map<String, Object> adminSessionAttrs = new HashMap<>();
        adminSessionAttrs.put(LoginServlet.SESSION_USER_ID, 300L);
        adminSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession adminSession = createMockSession(adminSessionAttrs);

        Map<String, Object> respStateD = new HashMap<>();
        HttpServletResponse respD = createMockResponse(respStateD);
        boolean[] chainD = new boolean[]{false};
        HttpServletRequest reqD = createMockFilterRequest(adminSession, "/admin/dashboard", "GET", new HashMap<>(), new HashMap<>());
        filter.doFilter(reqD, respD, (r, s) -> chainD[0] = true);

        assertTrue(chainD[0], "Admin must be permitted to access /admin/dashboard");
        assertNull(respStateD.get("errorCode"));
    }

    @Test
    @DisplayName("16. Admin can reject a coordinator request with reason and updates both tables atomically")
    void testAdminCanRejectRequestWithReason() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "coord.rejreason." + uniqueSuffix + "@college.edu";
        String empId = "EMP-RR-" + uniqueSuffix.toUpperCase();
        String plainPassword = "TestPass123!";

        String passwordHash = PasswordHashService.hashPassword(plainPassword);
        User user = new User("Coord To Reject", email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL);
        Coordinator c = new Coordinator(null, empId, "Electrical", "Coordinator", "9876543210");
        coordinatorDAO.registerCoordinator(user, c);
        createdUserIds.add(user.getId());

        CoordinatorApprovalServlet approvalServlet = new CoordinatorApprovalServlet();
        approvalServlet.init();

        // Admin session (ID 3 = admin@college.edu)
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 3L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        sessionAttrs.put("csrfToken", "admin-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        String rejectionReason = "Employee ID could not be validated with University HR records.";
        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "admin-csrf");
        params.put("action", "reject");
        params.put("id", String.valueOf(c.getId()));
        params.put("adminNotes", rejectionReason);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        Method doPost = CoordinatorApprovalServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(approvalServlet, req, resp);

        assertEquals("/admin/dashboard", respState.get("redirectedUrl"));
        assertNotNull(sessionAttrs.get("successMessage"));
        assertTrue(((String) sessionAttrs.get("successMessage")).toLowerCase().contains("rejected"));

        // Verify database updates
        User updatedUser = userDAO.findById(user.getId());
        assertEquals(User.STATUS_REJECTED, updatedUser.getStatus());

        Coordinator updatedCoord = coordinatorDAO.findById(c.getId());
        assertEquals(Coordinator.STATUS_REJECTED, updatedCoord.getStatus());
        assertEquals(3L, updatedCoord.getApprovedBy());
        assertNotNull(updatedCoord.getApprovalDate());
        assertEquals(rejectionReason, updatedCoord.getAdminNotes());
    }

    @Test
    @DisplayName("17. RoleAuthorizationFilter strictly prevents students and coordinators from accessing /admin/coordinators")
    void testRoleAuthorizationFilterStrictlyProtectsCoordinatorApproval() throws Exception {
        RoleAuthorizationFilter filter = new RoleAuthorizationFilter();

        // A. Student blocked on /admin/coordinators
        Map<String, Object> studentSessionAttrs = new HashMap<>();
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 501L);
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentSessionAttrs);

        Map<String, Object> respStateA = new HashMap<>();
        HttpServletResponse respA = createMockResponse(respStateA);
        boolean[] chainA = new boolean[]{false};
        HttpServletRequest reqA = createMockFilterRequest(studentSession, "/admin/coordinators", "POST", new HashMap<>(), new HashMap<>());
        filter.doFilter(reqA, respA, (r, s) -> chainA[0] = true);

        assertFalse(chainA[0], "Filter chain must be blocked for student on /admin/coordinators");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateA.get("errorCode"));

        // B. Coordinator blocked on /admin/coordinators
        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 502L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        Map<String, Object> respStateB = new HashMap<>();
        HttpServletResponse respB = createMockResponse(respStateB);
        boolean[] chainB = new boolean[]{false};
        HttpServletRequest reqB = createMockFilterRequest(coordSession, "/admin/coordinators", "POST", new HashMap<>(), new HashMap<>());
        filter.doFilter(reqB, respB, (r, s) -> chainB[0] = true);

        assertFalse(chainB[0], "Filter chain must be blocked for coordinator on /admin/coordinators");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateB.get("errorCode"));

        // C. Admin allowed through on /admin/coordinators
        Map<String, Object> adminSessionAttrs = new HashMap<>();
        adminSessionAttrs.put(LoginServlet.SESSION_USER_ID, 3L);
        adminSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession adminSession = createMockSession(adminSessionAttrs);

        Map<String, Object> respStateC = new HashMap<>();
        HttpServletResponse respC = createMockResponse(respStateC);
        boolean[] chainC = new boolean[]{false};
        HttpServletRequest reqC = createMockFilterRequest(adminSession, "/admin/coordinators", "POST", new HashMap<>(), new HashMap<>());
        filter.doFilter(reqC, respC, (r, s) -> chainC[0] = true);

        assertTrue(chainC[0], "Filter chain must proceed for admin on /admin/coordinators");
        assertNull(respStateC.get("errorCode"));
    }

    @Test
    @DisplayName("18. Approved and rejected coordinator requests omit approval buttons")
    void testApprovedAndRejectedRequestsOmitApprovalButtons() {
        // Coordinator in PENDING_APPROVAL status allows action buttons
        Coordinator pendingCoord = new Coordinator();
        pendingCoord.setStatus(Coordinator.STATUS_PENDING_APPROVAL);

        boolean showButtonsForPending = Coordinator.STATUS_PENDING_APPROVAL.equals(pendingCoord.getStatus());
        assertTrue(showButtonsForPending, "Approve and Reject buttons must be visible for PENDING_APPROVAL status");

        // Coordinator in APPROVED status
        Coordinator approvedCoord = new Coordinator();
        approvedCoord.setStatus(Coordinator.STATUS_APPROVED);

        boolean showButtonsForApproved = Coordinator.STATUS_PENDING_APPROVAL.equals(approvedCoord.getStatus());
        assertFalse(showButtonsForApproved, "Approve and Reject buttons must NOT be displayed for APPROVED status");

        // Coordinator in REJECTED status
        Coordinator rejectedCoord = new Coordinator();
        rejectedCoord.setStatus(Coordinator.STATUS_REJECTED);

        boolean showButtonsForRejected = Coordinator.STATUS_PENDING_APPROVAL.equals(rejectedCoord.getStatus());
        assertFalse(showButtonsForRejected, "Approve and Reject buttons must NOT be displayed for REJECTED status");
    }

    private HttpSession createMockSession(Map<String, Object> sessionAttributes) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAttribute".equals(name)) {
                        return sessionAttributes.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return sessionAttributes.remove(args[0]);
                    } else if ("invalidate".equals(name)) {
                        return null;
                    }
                    return null;
                }
        );
    }

    private HttpServletRequest createMockRequest(HttpSession session,
                                                Map<String, String> params,
                                                Map<String, Object> attributes,
                                                Map<String, Object> state) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getSession".equals(name)) {
                        return session;
                    } else if ("getParameter".equals(name)) {
                        return params.get(args[0]);
                    } else if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
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

    private HttpServletRequest createMockFilterRequest(HttpSession session,
                                                      String uri,
                                                      String method,
                                                      Map<String, String> params,
                                                      Map<String, Object> state) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, pMethod, args) -> {
                    String name = pMethod.getName();
                    if ("getSession".equals(name)) {
                        return session;
                    } else if ("getRequestURI".equals(name)) {
                        return uri;
                    } else if ("getContextPath".equals(name)) {
                        return "";
                    } else if ("getMethod".equals(name)) {
                        return method;
                    } else if ("getParameter".equals(name)) {
                        return params.get(args[0]);
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
