package com.college.backend;

import com.college.backend.dao.*;
import com.college.backend.entity.*;
import com.college.backend.servlet.ApplicationServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.DBConnection;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test verifying the Application Status History & Milestone Audit system:
 * 1. Application 42 multi-round timeline rendering with nested rounds and interview report.
 * 2. Legacy application milestone history reconciliation.
 * 3. Round rescheduling history preservation (original schedule, rescheduled schedule, reason).
 * 4. IDOR security enforcement (unauthorized students receive 403 Forbidden; owners and coordinators allowed).
 */
public class ApplicationStatusHistoryIntegrationTest {

    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;
    private InterviewRoundDAO roundDAO;
    private InterviewRoundHistoryDAO roundHistoryDAO;
    private InterviewReportDAO reportDAO;
    private StudentDAO studentDAO;
    private UserDAO userDAO;
    private JobDAO jobDAO;

    private Long tempAppId = null;
    private Long tempRoundId = null;
    private Long tempJobId = null;
    private Long tempStudentId = null;
    private Long tempUserId = null;

    @BeforeAll
    static void initDbConfig() {
        if (System.getProperty("db.password") == null && System.getenv("DB_PASSWORD") == null) {
            System.setProperty("db.password", "Tapu@123");
        }
    }

    @BeforeEach
    void setUp() {
        applicationDAO = new ApplicationDAO();
        interviewDAO = new InterviewDAO();
        roundDAO = new InterviewRoundDAO();
        roundHistoryDAO = new InterviewRoundHistoryDAO();
        reportDAO = new InterviewReportDAO();
        studentDAO = new StudentDAO();
        userDAO = new UserDAO();
        jobDAO = new JobDAO();
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DBConnection.getConnection()) {
            if (tempAppId != null) {
                if (tempRoundId != null) {
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interview_round_history WHERE application_id = ?")) {
                        stmt.setLong(1, tempAppId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interview_rounds WHERE application_id = ?")) {
                        stmt.setLong(1, tempAppId);
                        stmt.executeUpdate();
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interviews WHERE application_id = ?")) {
                    stmt.setLong(1, tempAppId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM application_status_history WHERE application_id = ?")) {
                    stmt.setLong(1, tempAppId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM applications WHERE id = ?")) {
                    stmt.setLong(1, tempAppId);
                    stmt.executeUpdate();
                }
            }
            if (tempJobId != null) {
                jobDAO.deleteJob(tempJobId);
            }
            if (tempStudentId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
                    stmt.setLong(1, tempStudentId);
                    stmt.executeUpdate();
                }
            }
            if (tempUserId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    stmt.setLong(1, tempUserId);
                    stmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Teardown error: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Application 42 multi-round timeline, nested rounds, and interview report dossier")
    void testApplication42MultiRoundHistory() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        servlet.init();

        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "history");
        params.put("id", "42");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/application-history.jsp", state.get("dispatchedPath"),
                "Should dispatch to application-history.jsp");

        Application app = (Application) reqAttrs.get("application");
        assertNotNull(app, "Application should be present");
        assertEquals(42L, app.getId().longValue());

        @SuppressWarnings("unchecked")
        List<ApplicationStatusHistory> statusHistory = (List<ApplicationStatusHistory>) reqAttrs.get("statusHistory");
        assertNotNull(statusHistory, "Status history should not be null");
        assertFalse(statusHistory.isEmpty(), "Status history should contain milestones");

        // Verify key milestones are present in order
        boolean hasApplied = statusHistory.stream().anyMatch(h -> "APPLIED".equalsIgnoreCase(h.getStatus()));
        boolean hasInterviewScheduled = statusHistory.stream().anyMatch(h -> "INTERVIEW_SCHEDULED".equalsIgnoreCase(h.getStatus()) || "INTERVIEW SCHEDULED".equalsIgnoreCase(h.getStatus()));
        assertTrue(hasApplied, "Timeline must include Application Submitted milestone");
        assertTrue(hasInterviewScheduled, "Timeline must include Interview Scheduled milestone");

        // Verify dynamic rounds are loaded
        @SuppressWarnings("unchecked")
        List<InterviewRound> rounds = (List<InterviewRound>) reqAttrs.get("interviewRounds");
        assertNotNull(rounds, "Interview rounds should be present");
        assertTrue(rounds.size() >= 3, "Application 42 should have at least 3 rounds");

        // Verify rounds contain dynamic names and results
        boolean hasAptitude = rounds.stream().anyMatch(r -> r.getRoundName().toLowerCase().contains("aptitude"));
        boolean hasCoding = rounds.stream().anyMatch(r -> r.getRoundName().toLowerCase().contains("coding"));
        boolean hasHR = rounds.stream().anyMatch(r -> r.getRoundName().toLowerCase().contains("hr"));
        assertTrue(hasAptitude, "Round 1 Aptitude should be present");
        assertTrue(hasCoding, "Round 2 Coding should be present");
        assertTrue(hasHR, "Round 3 HR should be present");

        // Verify interview report dossier
        InterviewReport report = (InterviewReport) reqAttrs.get("interviewReport");
        assertNotNull(report, "Application 42 should have an interview report dossier");
        assertEquals("SELECTED", report.getFinalDecision());
        assertEquals("REP-405ABC95", report.getReportUuid());

        // Verify round history audit map
        @SuppressWarnings("unchecked")
        Map<Long, List<InterviewRoundHistory>> roundHistoryMap = (Map<Long, List<InterviewRoundHistory>>) reqAttrs.get("roundHistoryMap");
        assertNotNull(roundHistoryMap, "Round history map should be present");
        for (InterviewRound r : rounds) {
            assertTrue(roundHistoryMap.containsKey(r.getId()), "Audit history must be available for round " + r.getId());
        }
    }

    @Test
    @DisplayName("Verify legacy application milestone history reconciliation")
    void testLegacyApplicationHistoryReconciliation() throws Exception {
        // Query existing applications to find an application
        List<Application> allApps = applicationDAO.getAllApplications();
        assertNotNull(allApps);
        assertFalse(allApps.isEmpty(), "At least one application must exist in database");

        // Use the first application in the database
        Application targetApp = allApps.get(0);
        Long targetAppId = targetApp.getId();

        ApplicationServlet servlet = new ApplicationServlet();
        servlet.init();

        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "history");
        params.put("id", String.valueOf(targetAppId));

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/application-history.jsp", state.get("dispatchedPath"));

        @SuppressWarnings("unchecked")
        List<ApplicationStatusHistory> statusHistory = (List<ApplicationStatusHistory>) reqAttrs.get("statusHistory");
        assertNotNull(statusHistory, "Reconciled status history should not be null");
        assertFalse(statusHistory.isEmpty(), "Application must have reconciled history events");

        // Verify chronological ordering
        for (int i = 0; i < statusHistory.size() - 1; i++) {
            ApplicationStatusHistory current = statusHistory.get(i);
            ApplicationStatusHistory next = statusHistory.get(i + 1);
            if (current.getChangedAt() != null && next.getChangedAt() != null) {
                assertFalse(current.getChangedAt().isAfter(next.getChangedAt()),
                        "Milestones must be strictly chronological: " + current.getStatus() + " (" + current.getChangedAt() + ") vs " + next.getStatus() + " (" + next.getChangedAt() + ")");
            }
        }
    }

    @Test
    @DisplayName("Verify dynamic round rescheduling history preserves original schedule, new schedule and reason")
    void testRoundReschedulingHistoryPreservation() throws Exception {
        // Create an isolated test student and test job to avoid duplicate application constraints
        String testRollNumber = "TEST-RESCHED-" + System.currentTimeMillis();
        String testEmail = "resched." + System.currentTimeMillis() + "@test.edu";
        User testUser = new User("Resched Test Candidate", testEmail, PasswordHashService.hashPassword("Pass@1234"), User.ROLE_STUDENT);
        Student testStudent = new Student(null, testRollNumber, "Computer Science", new BigDecimal("8.50"), 2026, "9876543210", "Java", "https://example.com/resume.pdf");
        studentDAO.registerStudent(testUser, testStudent);
        tempUserId = testUser.getId();
        tempStudentId = testStudent.getId();

        Job testJob = new Job();
        testJob.setTitle("Resched Test Job");
        testJob.setCompany("Test Corp");
        testJob.setLocation("Bhubaneswar");
        testJob.setDescription("Test Desc");
        testJob.setSalary("10 LPA");
        testJob.setJobType("Full Time");
        testJob.setStatus("Active");
        testJob.setDeadline(LocalDate.now().plusMonths(1));
        testJob.setMinCgpa(new BigDecimal("7.00"));
        testJob.setEligibleDepartments("Computer Science");
        jobDAO.addJob(testJob);
        tempJobId = testJob.getId();

        Application app = new Application(tempStudentId, tempJobId, LocalDate.now().minusDays(5), "SHORTLISTED");
        boolean created = applicationDAO.createApplication(app);
        assertTrue(created, "Test application creation must succeed");
        tempAppId = app.getId();

        Interview interview = new Interview(tempAppId, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "ONLINE", "SCHEDULED");
        interviewDAO.createInterview(interview);

        // Create round
        InterviewRound round = new InterviewRound(tempAppId, 1, "Technical Round", LocalDate.now().plusDays(2), LocalTime.of(10, 0), "ONLINE");
        round.setInterviewerName("Senior Tech Lead");
        boolean rCreated = roundDAO.createRound(round, "COORDINATOR");
        assertTrue(rCreated, "Round creation must succeed");
        tempRoundId = round.getId();

        // Reschedule round with old schedule, new schedule and reason
        LocalDate newDate = LocalDate.now().plusDays(4);
        LocalTime newTime = LocalTime.of(14, 30);
        String reason = "Interviewer unavailable due to emergency";
        boolean rescheduled = roundDAO.rescheduleRound(tempRoundId, newDate, newTime, "ONLINE", "Senior Tech Lead", null, reason, "COORDINATOR");
        assertTrue(rescheduled, "Round rescheduling must succeed");

        // Query audit history directly
        List<InterviewRoundHistory> historyList = roundHistoryDAO.getHistoryByRoundId(tempRoundId);
        assertNotNull(historyList);
        assertEquals(2, historyList.size(), "Should have CREATED and RESCHEDULED history records");

        InterviewRoundHistory rescheduleRecord = historyList.stream()
                .filter(h -> "RESCHEDULED".equalsIgnoreCase(h.getAction()))
                .findFirst()
                .orElse(null);

        assertNotNull(rescheduleRecord, "Rescheduled history record must exist");
        assertTrue(rescheduleRecord.getRemarks().contains("Original schedule:"), "Must log original schedule");
        assertTrue(rescheduleRecord.getRemarks().contains("Rescheduled to:"), "Must log new schedule");
        assertTrue(rescheduleRecord.getRemarks().contains(reason), "Must log rescheduling reason");

        // Verify via ApplicationServlet action=history
        ApplicationServlet servlet = new ApplicationServlet();
        servlet.init();

        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "history");
        params.put("id", String.valueOf(tempAppId));

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);
        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/application-history.jsp", state.get("dispatchedPath"));
        @SuppressWarnings("unchecked")
        Map<Long, List<InterviewRoundHistory>> map = (Map<Long, List<InterviewRoundHistory>>) reqAttrs.get("roundHistoryMap");
        assertNotNull(map);
        List<InterviewRoundHistory> roundHistories = map.get(tempRoundId);
        assertNotNull(roundHistories);
        assertTrue(roundHistories.stream().anyMatch(h -> "RESCHEDULED".equalsIgnoreCase(h.getAction())));
    }

    @Test
    @DisplayName("Verify IDOR security: student cannot view another student's application history")
    void testIdorProtectionStudentForbidden() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        servlet.init();

        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        // Find the owner of Application 42
        Application app42 = applicationDAO.getApplicationById(42L);
        assertNotNull(app42);
        Long ownerStudentId = app42.getStudentId();

        // Create a different student ID for the unauthorized session
        Long unauthorizedStudentId = (ownerStudentId != null && ownerStudentId == 9999L) ? 8888L : 9999L;

        // Mock student user
        User unauthStudentUser = new User();
        unauthStudentUser.setId(99999L);
        unauthStudentUser.setRole(User.ROLE_STUDENT);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, unauthStudentUser.getId());
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "history");
        params.put("id", "42");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        // Inject a StudentDAO that returns the unauthorized student profile for user 99999
        StudentDAO mockStudentDAO = new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                Student s = new Student();
                s.setId(unauthorizedStudentId);
                s.setUserId(userId);
                return s;
            }
        };
        servlet.setStudentDAO(mockStudentDAO);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"),
                "Unauthorized student must receive HTTP 403 Forbidden");
        assertNull(state.get("dispatchedPath"), "Request should not be dispatched to JSP");
    }

    @Test
    @DisplayName("Verify IDOR security: student CAN view their OWN application history")
    void testIdorProtectionStudentAllowedOwnApplication() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        servlet.init();

        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Application app42 = applicationDAO.getApplicationById(42L);
        assertNotNull(app42);
        Long ownerStudentId = app42.getStudentId();
        assertNotNull(ownerStudentId);

        Student ownerStudent = studentDAO.findById(ownerStudentId);
        assertNotNull(ownerStudent);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, ownerStudent.getUserId());
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "history");
        params.put("id", "42");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        doGetMethod.invoke(servlet, req, resp);

        assertNull(respState.get("errorCode"), "Owner student should not receive error code");
        assertEquals("/WEB-INF/jsp/application-history.jsp", state.get("dispatchedPath"),
                "Owner student must be granted access to own history");
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
                                (dispProxy, dispMethod, dispArgs) -> null
                        );
                    } else if ("getContextPath".equals(name)) {
                        return "";
                    } else if ("getServletPath".equals(name)) {
                        return "/applications";
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
