package com.college.backend;

import com.college.backend.controller.HomeController;
import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewReportDAO;
import com.college.backend.dao.InterviewRoundDAO;
import com.college.backend.dao.InterviewRoundHistoryDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.InterviewRoundHistory;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.service.InterviewPdfService;
import com.college.backend.servlet.InterviewPdfServlet;
import com.college.backend.servlet.InterviewRoundServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.CsrfUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Phase6DynamicRoundsAndPdfReportTests {

    // =====================================================
    // 1. INTERVIEW ROUND ENTITY TESTS
    // =====================================================

    @Test
    @DisplayName("InterviewRound entity getters, setters, and default properties verify correctly")
    void testInterviewRoundEntity() {
        InterviewRound round = new InterviewRound();
        round.setId(10L);
        round.setApplicationId(101L);
        round.setRoundNumber(1);
        round.setRoundName("Online Technical Assessment");
        round.setInterviewDate(LocalDate.of(2026, 11, 10));
        round.setInterviewTime(LocalTime.of(14, 0));
        round.setMode("ONLINE");
        round.setStatus("SCHEDULED");
        round.setResult("PENDING");
        round.setInterviewerName("Dr. Alan Turing");
        round.setInterviewerInfo("Senior Tech Lead");
        round.setRemarks("Covers Data Structures, Algorithms, Problem Solving");
        round.setFeedback("Solid analytical skills and clean code implementation.");
        round.setCancellationReason(null);
        LocalDateTime now = LocalDateTime.now();
        round.setCreatedAt(now);
        round.setUpdatedAt(now);

        assertEquals(10L, round.getId());
        assertEquals(101L, round.getApplicationId());
        assertEquals(1, round.getRoundNumber());
        assertEquals("Online Technical Assessment", round.getRoundName());
        assertEquals(LocalDate.of(2026, 11, 10), round.getInterviewDate());
        assertEquals(LocalTime.of(14, 0), round.getInterviewTime());
        assertEquals("ONLINE", round.getMode());
        assertEquals("SCHEDULED", round.getStatus());
        assertEquals("PENDING", round.getResult());
        assertEquals("Dr. Alan Turing", round.getInterviewerName());
        assertEquals("Senior Tech Lead", round.getInterviewerInfo());
        assertEquals("Solid analytical skills and clean code implementation.", round.getFeedback());
        assertNull(round.getCancellationReason());
        assertNotNull(round.toString());
    }

    @Test
    @DisplayName("InterviewRoundHistory entity tracks state changes and audit trails")
    void testInterviewRoundHistoryEntity() {
        InterviewRoundHistory history = new InterviewRoundHistory();
        history.setId(50L);
        history.setRoundId(10L);
        history.setApplicationId(101L);
        history.setAction("RESCHEDULED");
        history.setPreviousStatus("SCHEDULED");
        history.setNewStatus("SCHEDULED");
        history.setPreviousResult("PENDING");
        history.setNewResult("PENDING");
        history.setRemarks("Candidate requested deferral due to university semester exam");
        history.setPerformedBy("coordinator_test");
        history.setActionDate(LocalDateTime.now());

        assertEquals(50L, history.getId());
        assertEquals(10L, history.getRoundId());
        assertEquals(101L, history.getApplicationId());
        assertEquals("RESCHEDULED", history.getAction());
        assertEquals("coordinator_test", history.getPerformedBy());
        assertTrue(history.getRemarks().contains("semester exam"));
    }

    // =====================================================
    // 2. INTERVIEW REPORT ENTITY TESTS
    // =====================================================

    @Test
    @DisplayName("InterviewReport entity tracks cumulative performance and final selection decision")
    void testInterviewReportEntity() {
        InterviewReport report = new InterviewReport();
        report.setId(1L);
        report.setApplicationId(200L);
        String uuid = UUID.randomUUID().toString();
        report.setReportUuid(uuid);
        report.setFinalDecision("SELECTED");
        report.setDecisionDate(LocalDateTime.of(2026, 11, 20, 16, 0));
        report.setTotalRounds(3);
        report.setCompletedRounds(3);
        report.setPassedRounds(3);
        report.setFailedRounds(0);
        report.setCancelledRounds(0);
        report.setCoordinatorRemarks("Candidate cleared all technical, system design, and managerial rounds with distinction.");
        report.setOverallFeedback("Exceptional grasp of computer science fundamentals.");
        report.setGeneratedBy("placement_officer");
        report.setGeneratedAt(LocalDateTime.now());

        assertEquals(1L, report.getId());
        assertEquals(200L, report.getApplicationId());
        assertEquals(uuid, report.getReportUuid());
        assertEquals("SELECTED", report.getFinalDecision());
        assertEquals(3, report.getTotalRounds());
        assertEquals(3, report.getPassedRounds());
        assertEquals(0, report.getFailedRounds());
        assertEquals("placement_officer", report.getGeneratedBy());
        assertTrue(report.getCoordinatorRemarks().contains("distinction"));
    }

    // =====================================================
    // 3. INTERVIEW PDF SERVICE GENERATION TESTS
    // =====================================================

    @Test
    @DisplayName("InterviewPdfService generates valid PDF byte array for SELECTED candidate")
    void testInterviewPdfServiceGenerateSelectedReport() throws Exception {
        InterviewPdfService pdfService = new InterviewPdfService();

        // 1. Build test mock objects
        Application application = new Application(10L, 20L, LocalDate.of(2026, 10, 1), "SELECTED");
        application.setId(1001L);

        Student student = new Student();
        student.setId(10L);
        student.setUserId(5L);
        student.setRollNumber("CS2026-099");
        student.setDepartment("Computer Science & Engineering");
        student.setGraduationYear(2026);
        student.setCgpa(new BigDecimal("9.25"));
        student.setPhone("+91 9876543210");
        student.setSkills("Java, Spring Boot, React, SQL, Cloud Architecture");

        User studentUser = new User();
        studentUser.setId(5L);
        studentUser.setName("Priya Sharma");
        studentUser.setEmail("priya.sharma@college.edu");
        studentUser.setRole("STUDENT");

        Job job = new Job();
        job.setId(20L);
        job.setTitle("Associate Cloud Engineer");
        job.setCompany("Amazon Web Services");
        job.setLocation("Hyderabad, India");
        job.setJobType("Full Time");
        job.setSalary("18.5 LPA");

        InterviewReport report = new InterviewReport();
        report.setId(1L);
        report.setApplicationId(1001L);
        report.setReportUuid(UUID.randomUUID().toString());
        report.setFinalDecision("SELECTED");
        report.setDecisionDate(LocalDateTime.of(2026, 10, 25, 14, 0));
        report.setTotalRounds(3);
        report.setCompletedRounds(3);
        report.setPassedRounds(3);
        report.setFailedRounds(0);
        report.setCancelledRounds(0);
        report.setCoordinatorRemarks("Recommended for immediate corporate onboarding. Outstanding scores in all assessments.");
        report.setOverallFeedback("Exemplary architectural understanding and algorithmic maturity.");
        report.setGeneratedBy("Chief Placement Coordinator");
        report.setGeneratedAt(LocalDateTime.now());

        List<InterviewRound> rounds = new ArrayList<>();
        InterviewRound r1 = new InterviewRound();
        r1.setRoundNumber(1);
        r1.setRoundName("Round 1: Online Technical Assessment");
        r1.setInterviewDate(LocalDate.of(2026, 10, 10));
        r1.setInterviewTime(LocalTime.of(10, 0));
        r1.setMode("ONLINE");
        r1.setStatus("COMPLETED");
        r1.setResult("PASSED");
        r1.setInterviewerName("HackerRank Automation");
        r1.setFeedback("All test cases passed with O(n) space-time complexity.");
        rounds.add(r1);

        InterviewRound r2 = new InterviewRound();
        r2.setRoundNumber(2);
        r2.setRoundName("Round 2: Systems Design & Coding");
        r2.setInterviewDate(LocalDate.of(2026, 10, 15));
        r2.setInterviewTime(LocalTime.of(14, 30));
        r2.setMode("ONLINE");
        r2.setStatus("COMPLETED");
        r2.setResult("PASSED");
        r2.setInterviewerName("Senior Cloud Architect");
        r2.setFeedback("Designed scalable distributed caching architecture efficiently.");
        rounds.add(r2);

        InterviewRound r3 = new InterviewRound();
        r3.setRoundNumber(3);
        r3.setRoundName("Round 3: Leadership & Behavioral");
        r3.setInterviewDate(LocalDate.of(2026, 10, 20));
        r3.setInterviewTime(LocalTime.of(16, 0));
        r3.setMode("OFFLINE");
        r3.setStatus("COMPLETED");
        r3.setResult("PASSED");
        r3.setInterviewerName("Director of Campus Hiring");
        r3.setFeedback("Great team culture fit, clear communication, and leadership drive.");
        rounds.add(r3);

        byte[] pdfBytes = pdfService.generateInterviewReportPdf(report, application, student, studentUser, job, rounds);

        assertNotNull(pdfBytes, "PDF byte array must not be null");
        assertTrue(pdfBytes.length > 1000, "Generated PDF must contain binary PDF content > 1KB");

        // Verify PDF Header Magic Bytes: %PDF (0x25, 0x50, 0x44, 0x46)
        assertEquals((byte) 0x25, pdfBytes[0], "First byte must be '%' (0x25)");
        assertEquals((byte) 0x50, pdfBytes[1], "Second byte must be 'P' (0x50)");
        assertEquals((byte) 0x44, pdfBytes[2], "Third byte must be 'D' (0x44)");
        assertEquals((byte) 0x46, pdfBytes[3], "Fourth byte must be 'F' (0x46)");
    }

    @Test
    @DisplayName("InterviewPdfService generates valid PDF byte array for REJECTED candidate with feedback")
    void testInterviewPdfServiceGenerateRejectedReport() throws Exception {
        InterviewPdfService pdfService = new InterviewPdfService();

        Application application = new Application(11L, 21L, LocalDate.of(2026, 10, 2), "REJECTED");
        application.setId(1002L);

        Student student = new Student();
        student.setId(11L);
        student.setUserId(6L);
        student.setRollNumber("CS2026-102");
        student.setDepartment("Information Technology");
        student.setGraduationYear(2026);
        student.setCgpa(new BigDecimal("7.80"));

        User studentUser = new User();
        studentUser.setId(6L);
        studentUser.setName("Rahul Verma");
        studentUser.setEmail("rahul.verma@college.edu");

        Job job = new Job();
        job.setId(21L);
        job.setTitle("Software Engineer");
        job.setCompany("Microsoft");
        job.setLocation("Bangalore, India");
        job.setJobType("Full Time");
        job.setSalary("22.0 LPA");

        InterviewReport report = new InterviewReport();
        report.setId(2L);
        report.setApplicationId(1002L);
        report.setReportUuid(UUID.randomUUID().toString());
        report.setFinalDecision("REJECTED");
        report.setDecisionDate(LocalDateTime.of(2026, 10, 22, 11, 0));
        report.setTotalRounds(2);
        report.setCompletedRounds(2);
        report.setPassedRounds(1);
        report.setFailedRounds(1);
        report.setCancelledRounds(0);
        report.setCoordinatorRemarks("Candidate performed well in Round 1 but could not meet the threshold in Round 2 System Design.");
        report.setOverallFeedback("Encourage candidate to practice distributed concurrency and system scalability.");
        report.setGeneratedBy("Placement Officer");

        List<InterviewRound> rounds = new ArrayList<>();
        InterviewRound r1 = new InterviewRound();
        r1.setRoundNumber(1);
        r1.setRoundName("Round 1: Coding Assessment");
        r1.setStatus("COMPLETED");
        r1.setResult("PASSED");
        r1.setFeedback("Solved arrays problem accurately.");
        rounds.add(r1);

        InterviewRound r2 = new InterviewRound();
        r2.setRoundNumber(2);
        r2.setRoundName("Round 2: Technical Interview");
        r2.setStatus("COMPLETED");
        r2.setResult("FAILED");
        r2.setFeedback("Needed guidance on database indexing and thread synchronization.");
        rounds.add(r2);

        byte[] pdfBytes = pdfService.generateInterviewReportPdf(report, application, student, studentUser, job, rounds);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
        assertEquals((byte) 0x25, pdfBytes[0]);
        assertEquals((byte) 0x50, pdfBytes[1]);
        assertEquals((byte) 0x44, pdfBytes[2]);
        assertEquals((byte) 0x46, pdfBytes[3]);
    }

    // =====================================================
    // 4. INTERVIEW ROUND DAO IN-MEMORY MOCK OPERATIONS
    // =====================================================

    @Test
    @DisplayName("InterviewRoundDAO supports arbitrary sequences: 2 rounds, 3 rounds, 4 rounds, custom round names")
    void testDynamicRoundSequencesAndStateTransitions() {
        List<InterviewRound> roundStore = new ArrayList<>();

        InterviewRoundDAO mockDAO = new InterviewRoundDAO() {
            @Override
            public boolean createRound(InterviewRound round, String performedBy) {
                round.setId((long) (roundStore.size() + 1));
                roundStore.add(round);
                return true;
            }

            @Override
            public List<InterviewRound> getRoundsByApplicationId(Long applicationId) {
                List<InterviewRound> res = new ArrayList<>();
                for (InterviewRound r : roundStore) {
                    if (r.getApplicationId().equals(applicationId)) {
                        res.add(r);
                    }
                }
                return res;
            }

            @Override
            public InterviewRound getRoundById(Long roundId) {
                for (InterviewRound r : roundStore) {
                    if (r.getId().equals(roundId)) {
                        return r;
                    }
                }
                return null;
            }

            @Override
            public boolean completeRound(Long roundId, String result, String remarks, String feedback, String coordinatorName) {
                InterviewRound r = getRoundById(roundId);
                if (r != null) {
                    r.setStatus("COMPLETED");
                    r.setResult(result);
                    r.setRemarks(remarks);
                    r.setFeedback(feedback);
                    return true;
                }
                return false;
            }

            @Override
            public boolean cancelRound(Long roundId, String reason, String coordinatorName) {
                InterviewRound r = getRoundById(roundId);
                if (r != null) {
                    r.setStatus("CANCELLED");
                    r.setCancellationReason(reason);
                    return true;
                }
                return false;
            }

            @Override
            public boolean rescheduleRound(Long roundId, LocalDate date, LocalTime time, String mode,
                                           String interviewerName, String interviewerInfo, String remarks, String coordinatorName) {
                InterviewRound r = getRoundById(roundId);
                if (r != null) {
                    r.setInterviewDate(date);
                    r.setInterviewTime(time);
                    r.setMode(mode);
                    r.setInterviewerName(interviewerName);
                    r.setInterviewerInfo(interviewerInfo);
                    r.setRemarks(remarks);
                    return true;
                }
                return false;
            }
        };

        Long testAppId = 555L;

        // 1. Schedule Round 1: Online Assessment
        InterviewRound r1 = new InterviewRound();
        r1.setApplicationId(testAppId);
        r1.setRoundNumber(1);
        r1.setRoundName("Aptitude & Technical MCQ");
        r1.setInterviewDate(LocalDate.now().plusDays(2));
        r1.setInterviewTime(LocalTime.of(10, 0));
        r1.setMode("ONLINE");
        r1.setStatus("SCHEDULED");
        assertTrue(mockDAO.createRound(r1, "admin"));

        // 2. Complete Round 1 with PASSED
        assertTrue(mockDAO.completeRound(r1.getId(), "PASSED", "Top percentile", "Qualified for Coding Round", "admin"));
        assertEquals("COMPLETED", mockDAO.getRoundById(r1.getId()).getStatus());
        assertEquals("PASSED", mockDAO.getRoundById(r1.getId()).getResult());

        // 3. Schedule Round 2: Coding Assessment
        InterviewRound r2 = new InterviewRound();
        r2.setApplicationId(testAppId);
        r2.setRoundNumber(2);
        r2.setRoundName("Hands-on Coding Challenge");
        r2.setInterviewDate(LocalDate.now().plusDays(5));
        r2.setInterviewTime(LocalTime.of(14, 0));
        r2.setMode("ONLINE");
        r2.setStatus("SCHEDULED");
        assertTrue(mockDAO.createRound(r2, "admin"));

        // 4. Reschedule Round 2
        LocalDate rescheduledDate = LocalDate.now().plusDays(6);
        LocalTime rescheduledTime = LocalTime.of(15, 0);
        assertTrue(mockDAO.rescheduleRound(r2.getId(), rescheduledDate, rescheduledTime, "ONLINE",
                "Senior Dev", "Team Alpha", "Timing adjusted", "admin"));
        assertEquals(rescheduledDate, mockDAO.getRoundById(r2.getId()).getInterviewDate());

        // 5. Complete Round 2 with PASSED
        assertTrue(mockDAO.completeRound(r2.getId(), "PASSED", "Pass", "Clean algorithms", "admin"));

        // 6. Schedule Round 3: Technical Interview
        InterviewRound r3 = new InterviewRound();
        r3.setApplicationId(testAppId);
        r3.setRoundNumber(3);
        r3.setRoundName("Technical Architecture & Core Java");
        r3.setInterviewDate(LocalDate.now().plusDays(8));
        r3.setInterviewTime(LocalTime.of(11, 0));
        r3.setMode("OFFLINE");
        r3.setStatus("SCHEDULED");
        assertTrue(mockDAO.createRound(r3, "admin"));

        // 7. Schedule Round 4: HR & Cultural Fit
        InterviewRound r4 = new InterviewRound();
        r4.setApplicationId(testAppId);
        r4.setRoundNumber(4);
        r4.setRoundName("Director & HR Cultural Fit");
        r4.setInterviewDate(LocalDate.now().plusDays(10));
        r4.setInterviewTime(LocalTime.of(16, 0));
        r4.setMode("OFFLINE");
        r4.setStatus("SCHEDULED");
        assertTrue(mockDAO.createRound(r4, "admin"));

        // Cancel Round 4 with Reason
        assertTrue(mockDAO.cancelRound(r4.getId(), "Position filled by prior batch of interviewees", "admin"));
        assertEquals("CANCELLED", mockDAO.getRoundById(r4.getId()).getStatus());
        assertEquals("Position filled by prior batch of interviewees", mockDAO.getRoundById(r4.getId()).getCancellationReason());

        // Total rounds for application
        List<InterviewRound> allRounds = mockDAO.getRoundsByApplicationId(testAppId);
        assertEquals(4, allRounds.size());
    }

    // =====================================================
    // 5. INTERVIEW PDF SERVLET ACCESS CONTROL TESTS
    // =====================================================

    @Test
    @DisplayName("InterviewPdfServlet rejects unauthenticated requests with redirect to login")
    void testInterviewPdfServletRejectsUnauthenticated() throws Exception {
        InterviewPdfServlet servlet = new InterviewPdfServlet();

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("applicationId", "101");

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse response = createMockResponse(state);

        invokeDoGet(servlet, request, response);

        assertEquals("/college/login", state.get("redirectUrl"));
    }

    @Test
    @DisplayName("InterviewPdfServlet prevents IDOR: student cannot access another candidate's PDF report")
    void testInterviewPdfServletPreventsIdorForStudent() throws Exception {
        InterviewPdfServlet servlet = new InterviewPdfServlet();

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return new Application(999L, 10L, LocalDate.now(), "SELECTED");
            }
        };
        servlet.setApplicationDAO(mockAppDAO);

        StudentDAO mockStudentDAO = new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                Student s = new Student();
                s.setId(555L);
                s.setUserId(userId);
                return s;
            }
        };
        servlet.setStudentDAO(mockStudentDAO);

        InterviewReportDAO mockReportDAO = new InterviewReportDAO() {
            @Override
            public InterviewReport getReportByApplicationId(Long id) {
                return null;
            }
        };
        servlet.setReportDAO(mockReportDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 10L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("applicationId", "101");

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse response = createMockResponse(state);

        invokeDoGet(servlet, request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, state.get("errorCode"));
    }

    @Test
    @DisplayName("InterviewPdfServlet streams PDF to Coordinator for any application")
    void testInterviewPdfServletAllowsCoordinator() throws Exception {
        InterviewPdfServlet servlet = new InterviewPdfServlet();

        Application app = new Application(10L, 20L, LocalDate.now(), "SELECTED");
        app.setId(101L);

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return app;
            }
        };
        servlet.setApplicationDAO(mockAppDAO);

        InterviewReport report = new InterviewReport();
        report.setId(1L);
        report.setApplicationId(101L);
        report.setFinalDecision("SELECTED");
        report.setReportUuid(UUID.randomUUID().toString());
        report.setTotalRounds(1);
        report.setCompletedRounds(1);
        report.setPassedRounds(1);

        InterviewReportDAO mockReportDAO = new InterviewReportDAO() {
            @Override
            public InterviewReport getReportByApplicationId(Long applicationId) {
                return report;
            }
        };
        servlet.setReportDAO(mockReportDAO);

        Student student = new Student();
        student.setId(10L);
        student.setUserId(2L);
        student.setRollNumber("CS-TEST-01");
        student.setCgpa(new BigDecimal("8.50"));
        StudentDAO mockStudentDAO = new StudentDAO() {
            @Override
            public Student findById(Long id) {
                return student;
            }
        };
        servlet.setStudentDAO(mockStudentDAO);

        User user = new User();
        user.setId(2L);
        user.setName("Test Student");
        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public User findById(Long id) {
                return user;
            }
        };
        servlet.setUserDAO(mockUserDAO);

        Job job = new Job();
        job.setId(20L);
        job.setTitle("Software Engineer");
        job.setCompany("Google");
        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        };
        servlet.setJobDAO(mockJobDAO);

        InterviewRoundDAO mockRoundDAO = new InterviewRoundDAO() {
            @Override
            public List<InterviewRound> getRoundsByApplicationId(Long applicationId) {
                return Collections.emptyList();
            }
        };
        servlet.setRoundDAO(mockRoundDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("applicationId", "101");

        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        Map<String, Object> state = new HashMap<>();
        state.put("servletPath", "/interview-report/view");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse response = createStreamingMockResponse(state, responseBuffer);

        invokeDoGet(servlet, request, response);

        byte[] output = responseBuffer.toByteArray();
        assertTrue(output.length > 500, "PDF stream should be received by coordinator");
        assertEquals("application/pdf", state.get("contentType"));
        assertEquals((byte) 0x25, output[0]);
        assertEquals((byte) 0x50, output[1]);
        assertEquals((byte) 0x44, output[2]);
        assertEquals((byte) 0x46, output[3]);
    }

    // =====================================================
    // 6. INTERVIEW ROUND SERVLET ROLE-BASED ACCESS & ACTIONS
    // =====================================================

    @Test
    @DisplayName("InterviewRoundServlet blocks mutating POST actions from students with HTTP 403")
    void testInterviewRoundServletBlocksStudentPostActions() throws Exception {
        InterviewRoundServlet servlet = new InterviewRoundServlet();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 20L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token-123");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-token-123");
        params.put("action", "schedule");
        params.put("applicationId", "101");

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse response = createMockResponse(state);

        invokeDoPost(servlet, request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, state.get("errorCode"));
    }

    @Test
    @DisplayName("InterviewRoundServlet allows Coordinator to record final decision and generates report")
    void testInterviewRoundServletCoordinatorFinalDecision() throws Exception {
        InterviewRoundServlet servlet = new InterviewRoundServlet();

        final boolean[] appUpdated = {false};
        final boolean[] reportCreated = {false};

        Application app = new Application(10L, 20L, LocalDate.now(), "SHORTLISTED");
        app.setId(101L);

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return app;
            }

            @Override
            public boolean selectApplication(Long id) {
                if (id.equals(101L)) {
                    app.setStatus("SELECTED");
                    appUpdated[0] = true;
                    return true;
                }
                return false;
            }
        };
        servlet.setApplicationDAO(mockAppDAO);

        InterviewReportDAO mockReportDAO = new InterviewReportDAO() {
            @Override
            public boolean createOrUpdateReport(InterviewReport report) {
                reportCreated[0] = true;
                return true;
            }

            @Override
            public InterviewReport getReportByApplicationId(Long id) {
                return null;
            }
        };
        servlet.setReportDAO(mockReportDAO);

        InterviewRoundDAO mockRoundDAO = new InterviewRoundDAO() {
            @Override
            public List<InterviewRound> getRoundsByApplicationId(Long id) {
                InterviewRound r = new InterviewRound();
                r.setRoundNumber(1);
                r.setStatus("COMPLETED");
                r.setResult("PASSED");
                return List.of(r);
            }

            @Override
            public boolean hasActiveOrPendingRound(Long id) {
                return false;
            }

            @Override
            public boolean hasAnyFailedRound(Long id) {
                return false;
            }

            @Override
            public boolean isAllRoundsCompletedAndPassed(Long id) {
                return true;
            }

            @Override
            public Map<String, Integer> getRoundStatistics(Long id) {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("TOTAL", 1);
                stats.put("COMPLETED", 1);
                stats.put("PASSED", 1);
                stats.put("FAILED", 0);
                stats.put("CANCELLED", 0);
                return stats;
            }
        };
        servlet.setRoundDAO(mockRoundDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-csrf-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "valid-csrf-token");
        params.put("action", "recordFinalDecision");
        params.put("applicationId", "101");
        params.put("finalDecision", "SELECTED");
        params.put("coordinatorRemarks", "Candidate selected by hiring board.");
        params.put("overallFeedback", "Good domain knowledge.");

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse response = createMockResponse(state);

        invokeDoPost(servlet, request, response);

        assertTrue(appUpdated[0], "Application status must be updated to SELECTED");
        assertEquals("SELECTED", app.getStatus());
        assertTrue(reportCreated[0], "InterviewReport record must be created");
        assertEquals("/college/interview-rounds?applicationId=101", state.get("redirectUrl"));
    }

    // =====================================================
    // 7. HOME CONTROLLER & HOME SERVLET ROUTING TESTS
    // =====================================================

    @Test
    @DisplayName("HomeController maps root, /home, and /index to index view")
    void testHomeControllerViewMapping() {
        HomeController controller = new HomeController();
        assertEquals("index", controller.home(), "HomeController must return 'index' view");
    }

    @Test
    @DisplayName("HomeServlet forwards /home and /index to /index.jsp in pure Servlet container")
    void testHomeServletForwardsToIndexJsp() throws Exception {
        com.college.backend.servlet.HomeServlet servlet = new com.college.backend.servlet.HomeServlet();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest request = createMockRequest(createMockSession(new HashMap<>()), Collections.emptyMap(), new HashMap<>(), state);
        HttpServletResponse response = createMockResponse(state);

        invokeDoGet(servlet, request, response);

        assertEquals("/index.jsp", state.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(state.get("forwarded")));
    }

    // =====================================================
    // 8. DATA SEPARATION VERIFICATION TESTS
    // =====================================================

    @Test
    @DisplayName("Demo placement analytics data is isolated in static script and does not touch application database")
    void testDemoDataSeparationIntegrity() throws Exception {
        Path scriptPath = Path.of("backend/src/main/webapp/assets/js/placement-analytics-data.js");
        if (!Files.exists(scriptPath)) {
            scriptPath = Path.of("src/main/webapp/assets/js/placement-analytics-data.js");
        }
        assertTrue(Files.exists(scriptPath), "placement-analytics-data.js must exist");

        String jsContent = Files.readString(scriptPath);
        assertTrue(jsContent.contains("PlacementAnalytics"), "Must contain PlacementAnalytics configuration");
        assertTrue(jsContent.contains("Illustrative Demo Dataset"), "Must contain illustrative disclaimer");
        assertTrue(jsContent.contains("decoupled from MySQL"), "Must state decoupled from database");
        assertFalse(jsContent.contains("jdbc:mysql"), "Demo data script must never contain database connection strings");
        assertFalse(jsContent.contains("INSERT INTO"), "Demo data script must never execute database inserts");

        Path indexJspPath = Path.of("backend/src/main/webapp/index.jsp");
        if (!Files.exists(indexJspPath)) {
            indexJspPath = Path.of("src/main/webapp/index.jsp");
        }
        assertTrue(Files.exists(indexJspPath), "index.jsp must exist");
        String jspContent = Files.readString(indexJspPath);
        assertFalse(jspContent.contains("demo-data-banner"), "Landing page must not display demo-data-banner");
    }

    // =====================================================
    // REFLECTION & MOCK HELPER IMPLEMENTATIONS
    // =====================================================

    private void invokeDoGet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method m = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(servlet, req, resp);
    }

    private void invokeDoPost(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method m = servlet.getClass().getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(servlet, req, resp);
    }

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

    private HttpServletRequest createMockRequest(HttpSession session, Map<String, String> params,
                                                 Map<String, Object> attributes, Map<String, Object> state) {
        RequestDispatcher mockDispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        state.put("forwarded", true);
                        return null;
                    }
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getSession".equals(name)) {
                        return session;
                    } else if ("getParameter".equals(name)) {
                        return params != null ? params.get(args[0]) : null;
                    } else if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    } else if ("removeAttribute".equals(name)) {
                        return attributes.remove(args[0]);
                    } else if ("getRequestDispatcher".equals(name)) {
                        state.put("dispatchedPath", args[0]);
                        return mockDispatcher;
                    } else if ("getContextPath".equals(name)) {
                        return "/college";
                    } else if ("getServletPath".equals(name)) {
                        return state.getOrDefault("servletPath", "/test");
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
                    } else if ("setContentType".equals(name)) {
                        state.put("contentType", args[0]);
                        return null;
                    } else if ("setHeader".equals(name)) {
                        state.put("header_" + args[0], args[1]);
                        return null;
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createStreamingMockResponse(Map<String, Object> state, ByteArrayOutputStream buffer) {
        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
                buffer.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                buffer.write(b, off, len);
            }
        };

        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("sendError".equals(name)) {
                        state.put("errorCode", args[0]);
                        return null;
                    } else if ("sendRedirect".equals(name)) {
                        state.put("redirectUrl", args[0]);
                        return null;
                    } else if ("setContentType".equals(name)) {
                        state.put("contentType", args[0]);
                        return null;
                    } else if ("setHeader".equals(name)) {
                        state.put("header_" + args[0], args[1]);
                        return null;
                    } else if ("getOutputStream".equals(name)) {
                        return servletOut;
                    }
                    return null;
                }
        );
    }
}
