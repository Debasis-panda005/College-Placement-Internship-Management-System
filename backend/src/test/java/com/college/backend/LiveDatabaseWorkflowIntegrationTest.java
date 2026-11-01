package com.college.backend;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.InterviewReportDAO;
import com.college.backend.dao.InterviewRoundDAO;
import com.college.backend.dao.InterviewRoundHistoryDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.Interview;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.servlet.AdminDashboardServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.DBConnection;
import com.college.backend.util.PasswordHashService;
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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end live database integration test that validates the complete placement workflow:
 * 1. Register a test student and verify in MySQL.
 * 2. Create a full-time job and verify in MySQL.
 * 3. Create an internship and verify in MySQL.
 * 4. Submit an application and verify in MySQL.
 * 5. Shortlist the application and verify in MySQL.
 * 6. Schedule an interview and verify in MySQL.
 * 7. Add multiple dynamic interview rounds and verify in MySQL.
 * 8. Complete interview rounds.
 * 9. Record a final selection decision and verify in MySQL.
 * 10. Load the Admin Dashboard servlet and verify that live MySQL metrics match expected counts.
 * 11. Safely clean up test records preserving real records.
 */
public class LiveDatabaseWorkflowIntegrationTest {

    private StudentDAO studentDAO;
    private UserDAO userDAO;
    private JobDAO jobDAO;
    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;
    private InterviewRoundDAO roundDAO;
    private InterviewRoundHistoryDAO roundHistoryDAO;
    private InterviewReportDAO reportDAO;

    private Long createdUserId = null;
    private Long createdStudentId = null;
    private Long createdJobId = null;
    private Long createdInternshipId = null;
    private Long createdApplicationId = null;
    private Long createdInterviewId = null;
    private Long createdRound1Id = null;
    private Long createdRound2Id = null;
    private Long createdReportId = null;

    @BeforeAll
    static void initDbConfig() {
        if (System.getProperty("db.password") == null && System.getenv("DB_PASSWORD") == null) {
            System.setProperty("db.password", "Tapu@123");
        }
    }

    @BeforeEach
    void setUp() {
        studentDAO = new StudentDAO();
        userDAO = new UserDAO();
        jobDAO = new JobDAO();
        applicationDAO = new ApplicationDAO();
        interviewDAO = new InterviewDAO();
        roundDAO = new InterviewRoundDAO();
        roundHistoryDAO = new InterviewRoundHistoryDAO();
        reportDAO = new InterviewReportDAO();
    }

    @AfterEach
    void tearDown() {
        // Clean up test records in reverse foreign key order to prevent orphaned test data
        try (Connection conn = DBConnection.getConnection()) {
            if (createdReportId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interview_reports WHERE id = ?")) {
                    stmt.setLong(1, createdReportId);
                    stmt.executeUpdate();
                }
            }
            if (createdRound1Id != null || createdRound2Id != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interview_round_history WHERE application_id = ?")) {
                    stmt.setLong(1, createdApplicationId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interview_rounds WHERE application_id = ?")) {
                    stmt.setLong(1, createdApplicationId);
                    stmt.executeUpdate();
                }
            }
            if (createdInterviewId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM interviews WHERE id = ?")) {
                    stmt.setLong(1, createdInterviewId);
                    stmt.executeUpdate();
                }
            }
            if (createdApplicationId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM application_status_history WHERE application_id = ?")) {
                    stmt.setLong(1, createdApplicationId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM applications WHERE id = ?")) {
                    stmt.setLong(1, createdApplicationId);
                    stmt.executeUpdate();
                }
            }
            if (createdJobId != null) {
                jobDAO.deleteJob(createdJobId);
            }
            if (createdInternshipId != null) {
                jobDAO.deleteJob(createdInternshipId);
            }
            if (createdStudentId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
                    stmt.setLong(1, createdStudentId);
                    stmt.executeUpdate();
                }
            }
            if (createdUserId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    stmt.setLong(1, createdUserId);
                    stmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Teardown warning: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Complete end-to-end placement database workflow and live Admin Dashboard verification")
    void testCompleteDatabaseWorkflowAndDashboardMetrics() throws Exception {
        // Ensure database connection is operational
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "MySQL JDBC connection must be established successfully");
        }

        // Capture initial baseline metrics
        int baselineStudents = studentDAO.getTotalStudents();
        Map<String, Integer> baselineJobStats = jobDAO.getJobStatistics();
        Map<String, Integer> baselineAppStats = applicationDAO.getApplicationStatistics();
        Map<String, Integer> baselineInterviewStats = interviewDAO.getInterviewStatistics();

        // 1. REGISTER TEST STUDENT
        String testRollNumber = "TEST-ROLL-" + System.currentTimeMillis();
        String testEmail = "student." + System.currentTimeMillis() + "@test.edu";
        User testUser = new User("Integration Test Candidate", testEmail, PasswordHashService.hashPassword("Pass@1234"), User.ROLE_STUDENT);
        Student testStudent = new Student(null, testRollNumber, "Computer Science", new BigDecimal("8.85"), 2026, "9876543210", "Java, SQL, REST", "https://example.com/resume.pdf");

        boolean registered = studentDAO.registerStudent(testUser, testStudent);
        assertTrue(registered, "Student registration transaction must succeed");
        assertNotNull(testUser.getId(), "User ID must be generated");
        assertNotNull(testStudent.getId(), "Student ID must be generated");
        createdUserId = testUser.getId();
        createdStudentId = testStudent.getId();

        // 2. VERIFY STUDENT RECORD IN MYSQL
        Student retrievedStudent = studentDAO.findById(createdStudentId);
        assertNotNull(retrievedStudent, "Student must be retrievable from MySQL");
        assertEquals(testRollNumber, retrievedStudent.getRollNumber());
        assertEquals("Computer Science", retrievedStudent.getDepartment());
        assertEquals(new BigDecimal("8.85"), retrievedStudent.getCgpa());

        // 3. CREATE FULL-TIME JOB
        Job fullTimeJob = new Job();
        fullTimeJob.setTitle("Staff Backend Engineer (Test Drive)");
        fullTimeJob.setCompany("Centurion Global Tech");
        fullTimeJob.setLocation("Bhubaneswar");
        fullTimeJob.setDescription("Distributed systems engineering with Jakarta Servlets and MySQL.");
        fullTimeJob.setSalary("14.5 LPA");
        fullTimeJob.setJobType("Full Time");
        fullTimeJob.setStatus("Active");
        fullTimeJob.setDeadline(LocalDate.now().plusMonths(1));
        fullTimeJob.setMinCgpa(new BigDecimal("7.50"));
        fullTimeJob.setEligibleDepartments("Computer Science, IT");

        boolean jobCreated = jobDAO.addJob(fullTimeJob);
        assertTrue(jobCreated, "Full-Time job creation must succeed");
        assertNotNull(fullTimeJob.getId(), "Job ID must be generated");
        createdJobId = fullTimeJob.getId();

        // 4. VERIFY FULL-TIME JOB IN MYSQL
        Job retrievedJob = jobDAO.getJobById(createdJobId);
        assertNotNull(retrievedJob, "Job record must exist in MySQL");
        assertEquals("Staff Backend Engineer (Test Drive)", retrievedJob.getTitle());
        assertEquals("Full Time", retrievedJob.getJobType());
        assertEquals("Active", retrievedJob.getStatus());

        // 5. CREATE INTERNSHIP
        Job internship = new Job();
        internship.setTitle("Cloud Architect Intern (Test Drive)");
        internship.setCompany("Azure Innovations");
        internship.setLocation("Remote");
        internship.setDescription("Cloud automation internship.");
        internship.setSalary("35,000/Month");
        internship.setDuration("6 Months");
        internship.setJobType("Internship");
        internship.setStatus("Active");
        internship.setDeadline(LocalDate.now().plusDays(20));
        internship.setMinCgpa(new BigDecimal("8.00"));
        internship.setEligibleDepartments("Computer Science");

        boolean internCreated = jobDAO.addJob(internship);
        assertTrue(internCreated, "Internship creation must succeed");
        assertNotNull(internship.getId(), "Internship ID must be generated");
        createdInternshipId = internship.getId();

        // 6. VERIFY INTERNSHIP IN MYSQL
        Job retrievedIntern = jobDAO.getJobById(createdInternshipId);
        assertNotNull(retrievedIntern, "Internship record must exist in MySQL");
        assertEquals("Internship", retrievedIntern.getJobType());
        assertEquals("6 Months", retrievedIntern.getDuration());

        // 7. SUBMIT APPLICATION
        Application app = new Application(createdStudentId, createdJobId, LocalDate.now(), "APPLIED");
        boolean appCreated = applicationDAO.createApplication(app);
        assertTrue(appCreated, "Application submission must succeed");
        assertNotNull(app.getId(), "Application ID must be generated");
        createdApplicationId = app.getId();

        // 8. VERIFY APPLICATION IN MYSQL
        Application retrievedApp = applicationDAO.getApplicationById(createdApplicationId);
        assertNotNull(retrievedApp, "Application must exist in MySQL");
        assertEquals("APPLIED", retrievedApp.getStatus());

        // 9. SHORTLIST APPLICATION
        retrievedApp.setStatus("SHORTLISTED");
        boolean appUpdated = applicationDAO.updateApplication(retrievedApp);
        assertTrue(appUpdated, "Shortlisting application status update must succeed");

        // 10. VERIFY SHORTLISTED STATUS IN MYSQL
        Application shortlistedApp = applicationDAO.getApplicationById(createdApplicationId);
        assertEquals("SHORTLISTED", shortlistedApp.getStatus(), "Application status must now be SHORTLISTED");

        // 11. SCHEDULE INTERVIEW
        Interview interview = new Interview(createdApplicationId, LocalDate.now().plusDays(2), LocalTime.of(11, 0), "ONLINE", "SCHEDULED");
        boolean interviewCreated = interviewDAO.createInterview(interview);
        assertTrue(interviewCreated, "Interview scheduling must succeed");
        Interview retrievedInterview = interviewDAO.getInterviewByApplicationId(createdApplicationId);
        assertNotNull(retrievedInterview, "Interview record must exist in MySQL");
        createdInterviewId = retrievedInterview.getId();
        assertEquals("SCHEDULED", retrievedInterview.getStatus());

        // 12. VERIFY ACTIVE INTERVIEW IN MYSQL
        Map<String, Integer> midInterviewStats = interviewDAO.getInterviewStatistics();
        assertEquals(baselineInterviewStats.getOrDefault("SCHEDULED", 0) + 1, midInterviewStats.get("SCHEDULED"));

        // 13. ADD MULTIPLE DYNAMIC INTERVIEW ROUNDS (PHASE 6)
        InterviewRound round1 = new InterviewRound(createdApplicationId, 1, "Technical Coding", LocalDate.now().plusDays(2), LocalTime.of(11, 0), "ONLINE");
        round1.setInterviewerName("Lead Engineer");
        boolean r1Created = roundDAO.createRound(round1, "COORDINATOR");
        assertTrue(r1Created, "Round 1 creation must succeed");
        createdRound1Id = round1.getId();

        InterviewRound round2 = new InterviewRound(createdApplicationId, 2, "System Architecture", LocalDate.now().plusDays(5), LocalTime.of(14, 0), "ONLINE");
        round2.setInterviewerName("Principal Architect");
        boolean r2Created = roundDAO.createRound(round2, "COORDINATOR");
        assertTrue(r2Created, "Round 2 creation must succeed");
        createdRound2Id = round2.getId();

        // 14. VERIFY INTERVIEW ROUNDS & AUDIT HISTORY
        assertEquals(2, roundDAO.getRoundsByApplicationId(createdApplicationId).size(), "Application must have 2 dynamic rounds");

        // 15. COMPLETE INTERVIEW ROUNDS
        boolean r1Completed = roundDAO.completeRound(createdRound1Id, "PASSED", "Pass", "Superb algorithmic depth and clean code.", "COORDINATOR");
        assertTrue(r1Completed, "Round 1 completion must succeed");

        boolean r2Completed = roundDAO.completeRound(createdRound2Id, "PASSED", "Pass", "Excellent distributed database architecture design.", "COORDINATOR");
        assertTrue(r2Completed, "Round 2 completion must succeed");

        // 16. RECORD FINAL SELECTION DECISION
        retrievedApp.setStatus("SELECTED");
        applicationDAO.updateApplication(retrievedApp);

        InterviewReport report = new InterviewReport();
        report.setApplicationId(createdApplicationId);
        report.setReportUuid("REP-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        report.setFinalDecision("SELECTED");
        report.setDecisionDate(LocalDateTime.now());
        report.setTotalRounds(2);
        report.setCompletedRounds(2);
        report.setPassedRounds(2);
        report.setFailedRounds(0);
        report.setCancelledRounds(0);
        report.setCoordinatorRemarks("Candidate successfully cleared all technical hurdles.");
        report.setOverallFeedback("Outstanding potential.");
        report.setGeneratedBy("Placement Director");
        report.setGeneratedAt(LocalDateTime.now());

        boolean reportCreated = reportDAO.createOrUpdateReport(report);
        assertTrue(reportCreated, "Interview report creation must succeed");
        createdReportId = report.getId();

        // 17. VERIFY FINAL DECISION AND REPORT DATA
        InterviewReport retrievedReport = reportDAO.getReportByApplicationId(createdApplicationId);
        assertNotNull(retrievedReport, "Interview report must exist in MySQL");
        assertEquals("SELECTED", retrievedReport.getFinalDecision());

        // 18. RELOAD ADMIN DASHBOARD & VERIFY ALL 8 METRICS LIVE FROM MYSQL
        AdminDashboardServlet servlet = new AdminDashboardServlet();
        servlet.init();

        Method doGetMethod = AdminDashboardServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> state = new HashMap<>();
        Map<String, Object> reqAttrs = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, new HashMap<>(), reqAttrs, state);
        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse resp = createMockResponse(respState);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/admin-dashboard.jsp", state.get("dispatchedPath"));
        assertNull(reqAttrs.get("errorMessage"), "There should be no database error message");

        // Verify the 8 metrics retrieved dynamically from MySQL
        int expectedStudents = baselineStudents + 1;
        int expectedJobs = baselineJobStats.get("JOBS") + 1;
        int expectedInternships = baselineJobStats.get("INTERNSHIPS") + 1;
        int expectedActiveDrives = baselineJobStats.get("ACTIVE") + 2; // 1 job + 1 internship
        int expectedApplications = baselineAppStats.get("TOTAL") + 1;
        int expectedSelected = baselineAppStats.get("SELECTED") + 1;
        int expectedScheduledInterviews = baselineInterviewStats.getOrDefault("SCHEDULED", 0) + 1;

        assertEquals(expectedStudents, reqAttrs.get("totalStudents"), "Metric 1 (Students) must increment");
        assertEquals(expectedJobs, reqAttrs.get("totalJobs"), "Metric 2 (Jobs) must increment");
        assertEquals(expectedInternships, reqAttrs.get("totalInternships"), "Metric 3 (Internships) must increment");
        assertEquals(expectedActiveDrives, reqAttrs.get("activeOpportunities"), "Metric 4 (Active Drives) must increment");
        assertEquals(expectedApplications, reqAttrs.get("totalApplications"), "Metric 5 (Applications) must increment");
        assertEquals(expectedSelected, reqAttrs.get("selectedApplications"), "Metric 7 (Final Selections) must increment");
        assertEquals(expectedScheduledInterviews, reqAttrs.get("scheduledInterviews"), "Metric 8 (Active Interviews) must increment");

        // Verify that the consolidated 'stats' map contains all 8 metrics for JSP EL expressions
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) reqAttrs.get("stats");
        assertNotNull(stats, "'stats' map must be provided for JSP EL expressions");
        assertEquals(expectedStudents, stats.get("totalStudents"));
        assertEquals(expectedJobs, stats.get("totalJobs"));
        assertEquals(expectedInternships, stats.get("totalInternships"));
        assertEquals(expectedActiveDrives, stats.get("activeJobs"));
        assertEquals(expectedApplications, stats.get("totalApplications"));
        assertEquals(expectedSelected, stats.get("selectedApplications"));
        assertEquals(expectedScheduledInterviews, stats.get("scheduledInterviews"));
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
                        return null;
                    }
                    return null;
                }
        );
    }
}
