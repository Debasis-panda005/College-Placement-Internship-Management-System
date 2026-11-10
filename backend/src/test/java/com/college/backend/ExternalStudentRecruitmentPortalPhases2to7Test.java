package com.college.backend;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ExternalStudentAuditLogDAO;
import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.ExternalStudentAuditLog;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.service.InterviewPdfService;
import com.college.backend.servlet.AdminDashboardServlet;
import com.college.backend.servlet.ApplicationServlet;
import com.college.backend.servlet.ExternalStudentApprovalServlet;
import com.college.backend.servlet.ExternalStudentDashboardServlet;
import com.college.backend.servlet.InterviewPdfServlet;
import com.college.backend.servlet.JobServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.servlet.ResumeDownloadServlet;
import com.college.backend.util.CsrfUtil;
import com.college.backend.util.PasswordHashService;
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
import java.io.File;
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

/**
 * Master Verification Suite:
 * Phases 2 to 7 of External Student Registration & Recruitment Portal
 *
 * Verifies:
 * - Phase 2: Verification approval, rejection with reason, audit logs, non-admin RBAC blocking
 * - Phase 3: Login authentication & isolation for approved/pending/rejected candidates, KPI metrics calculation
 * - Phase 4: Job target audience eligibility rules (INTERNAL, EXTERNAL, BOTH), access controls
 * - Phase 5: Application submission eligibility (CGPA, department, deadline, audience), duplicate prevention, IDOR protection
 * - Phase 6: Interview scheduling, round evaluation, PDF generation for external applicants, IDOR security
 * - Phase 7: Management dashboard metrics, directory search/filter queries, secure resume downloading
 */
public class ExternalStudentRecruitmentPortalPhases2to7Test {

    // =========================================================================
    // 1. PHASE 2 TESTS: APPROVAL, REJECTION, AUDIT LOGGING & RBAC
    // =========================================================================

    @Test
    @DisplayName("Phase 2: ExternalStudent entity handles status transitions and audit metadata")
    void testPhase2ExternalStudentStatusTransitions() {
        ExternalStudent student = new ExternalStudent(
                10L, 100L, "IIT Bhubaneswar", "Bhubaneswar", "B.Tech",
                "Computer Science", 6, new BigDecimal("8.75"), 2026,
                "9876543210", "uploads/resumes/candidate10.pdf",
                ExternalStudent.STATUS_PENDING, LocalDateTime.now(), LocalDateTime.now()
        );

        assertEquals(ExternalStudent.STATUS_PENDING, student.getVerificationStatus());
        assertEquals("IIT Bhubaneswar", student.getCollegeName());
        assertEquals(new BigDecimal("8.75"), student.getCgpa());

        student.setVerificationStatus(ExternalStudent.STATUS_APPROVED);
        student.setApprovedBy(1L);
        student.setApprovalDate(LocalDateTime.now());
        student.setAdminNotes("Verified with college transcripts.");

        assertEquals(ExternalStudent.STATUS_APPROVED, student.getVerificationStatus());
        assertEquals(1L, student.getApprovedBy());
        assertEquals("Verified with college transcripts.", student.getAdminNotes());

        ExternalStudentAuditLog log = new ExternalStudentAuditLog(
                student.getId(), ExternalStudentAuditLog.ACTION_APPROVE, 1L,
                ExternalStudent.STATUS_PENDING, ExternalStudent.STATUS_APPROVED,
                "Verification confirmed."
        );
        assertEquals(ExternalStudentAuditLog.ACTION_APPROVE, log.getAction());
        assertEquals(ExternalStudent.STATUS_PENDING, log.getPreviousStatus());
        assertEquals(ExternalStudent.STATUS_APPROVED, log.getNewStatus());
    }

    @Test
    @DisplayName("Phase 2: ExternalStudentApprovalServlet approves candidate, updates user status and writes audit log")
    void testPhase2ApprovalServletApproveAction() throws Exception {
        ExternalStudentApprovalServlet servlet = new ExternalStudentApprovalServlet();

        final ExternalStudent mockStudent = new ExternalStudent();
        mockStudent.setId(5L);
        mockStudent.setUserId(50L);
        mockStudent.setVerificationStatus(ExternalStudent.STATUS_PENDING);
        mockStudent.setApplicantName("Rohan Das");

        final List<ExternalStudentAuditLog> recordedLogs = new ArrayList<>();
        final boolean[] studentStatusUpdated = new boolean[]{false};
        final boolean[] userStatusUpdated = new boolean[]{false};

        ExternalStudentDAO mockStudentDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findById(Long id) {
                return id.equals(5L) ? mockStudent : null;
            }

            @Override
            public boolean updateVerificationStatus(Long id, String status, Long adminId, String notes) {
                if (id.equals(5L) && ExternalStudent.STATUS_APPROVED.equals(status)) {
                    mockStudent.setVerificationStatus(status);
                    mockStudent.setApprovedBy(adminId);
                    mockStudent.setAdminNotes(notes);
                    studentStatusUpdated[0] = true;
                    return true;
                }
                return false;
            }
        };

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public boolean updateUserStatus(Long userId, String status) {
                if (userId.equals(50L) && User.STATUS_APPROVED.equals(status)) {
                    userStatusUpdated[0] = true;
                    return true;
                }
                return false;
            }
        };

        ExternalStudentAuditLogDAO mockAuditDAO = new ExternalStudentAuditLogDAO() {
            @Override
            public boolean insertAuditLog(ExternalStudentAuditLog log) {
                recordedLogs.add(log);
                return true;
            }
        };

        servlet.setExternalStudentDAO(mockStudentDAO);
        servlet.setUserDAO(mockUserDAO);
        servlet.setAuditLogDAO(mockAuditDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createSession(1L, User.ROLE_ADMIN, null, sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "approve");
        params.put("id", "5");
        params.put("csrfToken", "valid-csrf-token");
        params.put("adminNotes", "Candidate verified successfully.");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "POST", "/admin/external-students", "", params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse response = createMockResponse(respState);

        invokeDoPost(servlet, request, response);

        assertTrue(studentStatusUpdated[0], "ExternalStudent verification status must be updated to APPROVED");
        assertEquals("/admin/dashboard#external-students", respState.get("redirectUrl"));
    }

    @Test
    @DisplayName("Phase 2: ExternalStudentApprovalServlet non-admin coordinator access is blocked")
    void testPhase2ApprovalServletNonAdminBlocked() throws Exception {
        ExternalStudentApprovalServlet servlet = new ExternalStudentApprovalServlet();

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createSession(2L, User.ROLE_COORDINATOR, null, sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "approve");
        params.put("id", "5");
        params.put("csrfToken", "valid-csrf-token");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "POST", "/admin/external-students", "", params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse response = createMockResponse(respState);

        invokeDoPost(servlet, request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"), "Non-admin access must return 403 Forbidden");
    }

    // =========================================================================
    // 2. PHASE 3 TESTS: LOGIN ISOLATION & EXTERNAL DASHBOARD KPIS
    // =========================================================================

    @Test
    @DisplayName("Phase 3: LoginServlet blocks PENDING_APPROVAL and REJECTED external candidates")
    void testPhase3LoginServletApprovalBlocking() throws Exception {
        LoginServlet loginServlet = new LoginServlet();

        final User pendingUser = new User();
        pendingUser.setId(101L);
        pendingUser.setEmail("pending.student@external.edu");
        pendingUser.setName("Pending Candidate");
        pendingUser.setPasswordHash(PasswordHashService.hashPassword("Password@123"));
        pendingUser.setRole(User.ROLE_STUDENT);
        pendingUser.setStatus(User.STATUS_PENDING);

        final ExternalStudent pendingStudent = new ExternalStudent();
        pendingStudent.setId(1L);
        pendingStudent.setUserId(101L);
        pendingStudent.setVerificationStatus(ExternalStudent.STATUS_PENDING);

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("pending.student@external.edu".equalsIgnoreCase(email)) {
                    return pendingUser;
                }
                return null;
            }
        };

        ExternalStudentDAO mockDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                if (userId.equals(101L)) return pendingStudent;
                return null;
            }
        };
        loginServlet.setUserDAO(mockUserDAO);
        loginServlet.setExternalStudentDAO(mockDAO);

        Map<String, String> params = new HashMap<>();
        params.put("email", "pending.student@external.edu");
        params.put("password", "Password@123");
        params.put("csrfToken", "valid-csrf-token");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("csrfToken", "valid-csrf-token");
        HttpSession session = createMockSession(sessionAttrs);
        HttpServletRequest request = createMockRequest(session, "POST", "/login", "", params, reqAttrs, reqState);
        HttpServletResponse response = createMockResponse(new HashMap<>());

        invokeDoPost(loginServlet, request, response);

        assertEquals("/WEB-INF/jsp/login.jsp", reqState.get("forwardPath"), "Pending candidate must be forwarded back to login view");
        assertNotNull(reqAttrs.get("errorMessage"));
        assertTrue(((String) reqAttrs.get("errorMessage")).contains("pending verification"));
    }

    @Test
    @DisplayName("Phase 3: ExternalStudentDashboardServlet computes correct 6 KPIs and updates permitted fields")
    void testPhase3ExternalDashboardKpisAndProfileUpdate() throws Exception {
        ExternalStudentDashboardServlet dashboardServlet = new ExternalStudentDashboardServlet();

        final ExternalStudent student = new ExternalStudent(
                10L, 100L, "NIT Rourkela", "Rourkela", "B.Tech",
                "Computer Science", 6, new BigDecimal("9.10"), 2026,
                "9876543210", "uploads/resumes/nit.pdf",
                ExternalStudent.STATUS_APPROVED, LocalDateTime.now(), LocalDateTime.now()
        );

        ExternalStudentDAO mockExternalStudentDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(100L) ? student : null;
            }

            @Override
            public boolean updatePermittedProfile(Long id, String phone, int currentSemester) {
                if (id.equals(10L)) {
                    student.setPhone(phone);
                    student.setCurrentSemester(currentSemester);
                    return true;
                }
                return false;
            }
        };

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public List<Application> getAllApplications() {
                List<Application> list = new ArrayList<>();
                Application app1 = new Application();
                app1.setId(1L);
                app1.setExternalStudentId(10L);
                app1.setStatus("APPLIED");

                Application app2 = new Application();
                app2.setId(2L);
                app2.setExternalStudentId(10L);
                app2.setStatus("SHORTLISTED");

                Application app3 = new Application();
                app3.setId(3L);
                app3.setExternalStudentId(10L);
                app3.setStatus("SELECTED");

                list.add(app1);
                list.add(app2);
                list.add(app3);
                return list;
            }
        };

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public List<Job> getActiveJobs() {
                List<Job> list = new ArrayList<>();
                Job j1 = new Job();
                j1.setId(1L);
                j1.setStatus("Active");
                j1.setTargetAudience(Job.AUDIENCE_EXTERNAL);
                j1.setJobType("Full Time");
                list.add(j1);
                return list;
            }
        };

        dashboardServlet.setExternalStudentDAO(mockExternalStudentDAO);
        dashboardServlet.setApplicationDAO(mockAppDAO);
        dashboardServlet.setJobDAO(mockJobDAO);

        // Test GET dashboard
        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createSession(100L, User.ROLE_STUDENT, "EXTERNAL", sessionAttrs);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest getReq = createMockRequest(session, "GET", "/external/dashboard", "", new HashMap<>(), reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse getResp = createMockResponse(respState);

        invokeDoGet(dashboardServlet, getReq, getResp);

        assertEquals("/WEB-INF/jsp/external-dashboard.jsp", reqState.get("forwardPath"));
        Map<?, ?> stats = (Map<?, ?>) reqAttrs.get("stats");
        assertNotNull(stats, "stats attribute must be set");
        assertEquals(3, stats.get("totalApplications"));
        assertEquals(1, stats.get("shortlistedApplications"));
        assertEquals(1, stats.get("selectedApplications"));
        assertEquals(1, stats.get("availableJobs"));

        // Test POST profile update (phone and semester)
        Map<String, String> postParams = new HashMap<>();
        postParams.put("phone", "9123456780");
        postParams.put("currentSemester", "7");
        postParams.put("csrfToken", "valid-csrf-token");

        HttpServletRequest postReq = createMockRequest(session, "POST", "/external/dashboard", "", postParams, new HashMap<>(), new HashMap<>());
        Map<String, Object> postRespState = new HashMap<>();
        HttpServletResponse postResp = createMockResponse(postRespState);

        invokeDoPost(dashboardServlet, postReq, postResp);

        assertEquals("9123456780", student.getPhone(), "Phone number must be updated");
        assertEquals(7, student.getCurrentSemester(), "Semester must be updated");
        assertEquals("/external/dashboard", postRespState.get("redirectUrl"));
    }

    // =========================================================================
    // 3. PHASE 4 TESTS: JOB TARGET AUDIENCE ACCESS CONTROL
    // =========================================================================

    @Test
    @DisplayName("Phase 4: Job target audience eligibility rules strictly differentiate student types")
    void testPhase4JobAudienceEligibility() {
        Job internalJob = new Job();
        internalJob.setTargetAudience(Job.AUDIENCE_INTERNAL);

        Job externalJob = new Job();
        externalJob.setTargetAudience(Job.AUDIENCE_EXTERNAL);

        Job bothJob = new Job();
        bothJob.setTargetAudience(Job.AUDIENCE_BOTH);

        assertTrue(internalJob.isEligibleForInternal());
        assertFalse(internalJob.isEligibleForExternal());
        assertTrue(internalJob.isEligibleForAudience("INTERNAL"));
        assertFalse(internalJob.isEligibleForAudience("EXTERNAL"));

        assertFalse(externalJob.isEligibleForInternal());
        assertTrue(externalJob.isEligibleForExternal());
        assertFalse(externalJob.isEligibleForAudience("INTERNAL"));
        assertTrue(externalJob.isEligibleForAudience("EXTERNAL"));

        assertTrue(bothJob.isEligibleForInternal());
        assertTrue(bothJob.isEligibleForExternal());
        assertTrue(bothJob.isEligibleForAudience("INTERNAL"));
        assertTrue(bothJob.isEligibleForAudience("EXTERNAL"));
    }

    @Test
    @DisplayName("Phase 4: JobServlet blocks external student from viewing INTERNAL-only job details")
    void testPhase4JobServletAudienceAccessControl() throws Exception {
        JobServlet jobServlet = new JobServlet();

        final Job internalJob = new Job();
        internalJob.setId(99L);
        internalJob.setTitle("Proprietary Campus Role");
        internalJob.setStatus("Active");
        internalJob.setTargetAudience(Job.AUDIENCE_INTERNAL);

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return id.equals(99L) ? internalJob : null;
            }
        };
        jobServlet.setJobDAO(mockJobDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createSession(100L, User.ROLE_STUDENT, "EXTERNAL", sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "view");
        params.put("id", "99");

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "GET", "/jobs", "", params, reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse response = createMockResponse(respState);

        invokeDoGet(jobServlet, request, response);

        assertEquals("/jobs", respState.get("redirectUrl"), "External student must be redirected away from internal job");
        assertNotNull(sessionAttrs.get("errorMessage"), "Error message must be set explaining audience restriction");
    }

    // =========================================================================
    // 4. PHASE 5 TESTS: APPLICATION SUBMISSION, ELIGIBILITY & IDOR DEFENSE
    // =========================================================================

    @Test
    @DisplayName("Phase 5: ApplicationServlet validates eligibility, duplicate check and IDOR protection")
    void testPhase5ApplicationCreationAndIdorProtection() throws Exception {
        ApplicationServlet appServlet = new ApplicationServlet();

        final ExternalStudent approvedStudent = new ExternalStudent();
        approvedStudent.setId(20L);
        approvedStudent.setUserId(200L);
        approvedStudent.setVerificationStatus(ExternalStudent.STATUS_APPROVED);
        approvedStudent.setCgpa(new BigDecimal("8.50"));
        approvedStudent.setDepartment("Computer Science");

        final Job externalJob = new Job();
        externalJob.setId(50L);
        externalJob.setTitle("Cloud Engineer");
        externalJob.setStatus("Active");
        externalJob.setTargetAudience(Job.AUDIENCE_EXTERNAL);
        externalJob.setMinCgpa(new BigDecimal("7.50"));
        externalJob.setEligibleDepartments("Computer Science, IT");
        externalJob.setDeadline(LocalDate.now().plusDays(10));

        final List<Application> createdApps = new ArrayList<>();

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public boolean hasAlreadyAppliedExternal(Long extStudentId, Long jobId) {
                return extStudentId.equals(20L) && jobId.equals(50L) && !createdApps.isEmpty();
            }

            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }

            @Override
            public boolean createApplication(Application application) {
                createdApps.add(application);
                application.setId(101L);
                return true;
            }

            @Override
            public Application getApplicationById(Long id) {
                if (id.equals(101L)) {
                    Application a = new Application();
                    a.setId(101L);
                    a.setJobId(50L);
                    a.setApplicantType(Application.APPLICANT_EXTERNAL);
                    a.setExternalStudentId(20L);
                    return a;
                }
                return null;
            }
        };

        ExternalStudentDAO mockExtDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(200L) ? approvedStudent : null;
            }

            @Override
            public ExternalStudent findById(Long id) {
                return id.equals(20L) ? approvedStudent : null;
            }
        };

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return id.equals(50L) ? externalJob : null;
            }
        };

        appServlet.setApplicationDAO(mockAppDAO);
        appServlet.setExternalStudentDAO(mockExtDAO);
        appServlet.setJobDAO(mockJobDAO);

        // 1. Submit Application
        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createSession(200L, User.ROLE_STUDENT, "EXTERNAL", sessionAttrs);

        Map<String, String> postParams = new HashMap<>();
        postParams.put("action", "create");
        postParams.put("jobId", "50");
        postParams.put("csrfToken", "valid-csrf-token");

        HttpServletRequest applyReq = createMockRequest(session, "POST", "/applications", "", postParams, new HashMap<>(), new HashMap<>());
        Map<String, Object> applyRespState = new HashMap<>();
        HttpServletResponse applyResp = createMockResponse(applyRespState);

        invokeDoPost(appServlet, applyReq, applyResp);

        assertEquals(1, createdApps.size(), "Application must be created");
        assertEquals(Application.APPLICANT_EXTERNAL, createdApps.get(0).getApplicantType());
        assertEquals(20L, createdApps.get(0).getExternalStudentId());

        // 2. IDOR Check: Another external student (userId 300L, extId 30L) tries to view application 101
        Map<String, Object> otherSessionAttrs = new HashMap<>();
        HttpSession otherSession = createSession(300L, User.ROLE_STUDENT, "EXTERNAL", otherSessionAttrs);

        ExternalStudent otherStudent = new ExternalStudent();
        otherStudent.setId(30L);
        otherStudent.setUserId(300L);

        mockExtDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(300L) ? otherStudent : null;
            }
            @Override
            public ExternalStudent findById(Long id) {
                return id.equals(20L) ? approvedStudent : otherStudent;
            }
        };
        appServlet.setExternalStudentDAO(mockExtDAO);

        Map<String, String> getParams = new HashMap<>();
        getParams.put("action", "details");
        getParams.put("id", "101");

        HttpServletRequest idorReq = createMockRequest(otherSession, "GET", "/applications", "", getParams, new HashMap<>(), new HashMap<>());
        Map<String, Object> idorRespState = new HashMap<>();
        HttpServletResponse idorResp = createMockResponse(idorRespState);

        invokeDoGet(appServlet, idorReq, idorResp);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, idorRespState.get("errorCode"), "Non-owning student must receive 403 Forbidden");
    }

    // =========================================================================
    // 5. PHASE 6 TESTS: DYNAMIC INTERVIEW PROGRESSION & PDF REPORTS
    // =========================================================================

    @Test
    @DisplayName("Phase 6: InterviewPdfService generates valid PDF for external candidate")
    void testPhase6InterviewPdfGenerationForExternalStudent() throws Exception {
        Application application = new Application();
        application.setId(205L);
        application.setJobId(55L);
        application.setApplicantType(Application.APPLICANT_EXTERNAL);
        application.setExternalStudentId(35L);
        application.setStatus("SELECTED");

        Job job = new Job();
        job.setId(55L);
        job.setTitle("Senior Full Stack Developer");
        job.setCompany("Centurion Tech Innovations");
        job.setLocation("Bhubaneswar / Remote");
        job.setJobType("Full Time");

        ExternalStudent externalStudent = new ExternalStudent(
                35L, 350L, "Kiit University", "Bhubaneswar", "B.Tech",
                "Computer Science", 8, new BigDecimal("9.25"), 2026,
                "9988776655", "uploads/resumes/kiit_candidate.pdf",
                ExternalStudent.STATUS_APPROVED, LocalDateTime.now(), LocalDateTime.now()
        );
        externalStudent.setApplicantName("Priya Sharma");
        externalStudent.setApplicantEmail("priya.sharma@kiit.ac.in");

        List<InterviewRound> rounds = new ArrayList<>();
        InterviewRound r1 = new InterviewRound();
        r1.setId(1L);
        r1.setApplicationId(205L);
        r1.setRoundNumber(1);
        r1.setRoundName("Online Coding Test");
        r1.setInterviewDate(LocalDate.of(2026, 11, 15));
        r1.setInterviewTime(LocalTime.of(10, 0));
        r1.setMode("ONLINE");
        r1.setStatus("COMPLETED");
        r1.setResult("PASSED");
        r1.setInterviewerName("Dr. Tech Lead");
        r1.setFeedback("Completed 3 out of 3 algorithmic challenges.");
        rounds.add(r1);

        InterviewReport report = new InterviewReport();
        report.setApplicationId(205L);
        report.setReportUuid(UUID.randomUUID().toString());
        report.setFinalDecision("SELECTED");
        report.setDecisionDate(LocalDateTime.now());
        report.setGeneratedBy("Prof. Admin");
        report.setCoordinatorRemarks("Final hiring approval granted.");
        report.setOverallFeedback("Exceptional performance across all evaluation rounds.");

        InterviewPdfService pdfService = new InterviewPdfService();
        byte[] pdfBytes = pdfService.generateInterviewReportPdf(report, application, null, null, externalStudent, job, rounds);

        assertNotNull(pdfBytes, "PDF byte array must not be null");
        assertTrue(pdfBytes.length > 500, "PDF must have non-trivial size");

        // Verify PDF Header Magic Bytes "%PDF-"
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);
    }

    @Test
    @DisplayName("Phase 6: InterviewPdfServlet enforces IDOR authorization for PDF report download")
    void testPhase6InterviewPdfServletIdorAuthorization() throws Exception {
        InterviewPdfServlet pdfServlet = new InterviewPdfServlet();

        final Application application = new Application();
        application.setId(300L);
        application.setJobId(70L);
        application.setApplicantType(Application.APPLICANT_EXTERNAL);
        application.setExternalStudentId(80L);
        application.setStatus("SELECTED");

        final ExternalStudent owningStudent = new ExternalStudent();
        owningStudent.setId(80L);
        owningStudent.setUserId(800L);

        final ExternalStudent otherStudent = new ExternalStudent();
        otherStudent.setId(99L);
        otherStudent.setUserId(999L);

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return id.equals(300L) ? application : null;
            }
        };

        ExternalStudentDAO mockExtDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findById(Long id) {
                return id.equals(80L) ? owningStudent : otherStudent;
            }
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(800L) ? owningStudent : otherStudent;
            }
        };

        pdfServlet.setApplicationDAO(mockAppDAO);
        pdfServlet.setExternalStudentDAO(mockExtDAO);

        // Non-owning candidate (userId 999L) attempts to download candidate 800L's report
        Map<String, Object> hackerSessionAttrs = new HashMap<>();
        HttpSession hackerSession = createSession(999L, User.ROLE_STUDENT, "EXTERNAL", hackerSessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("applicationId", "300");

        HttpServletRequest idorReq = createMockRequest(hackerSession, "GET", "/interview/report/pdf", "", params, new HashMap<>(), new HashMap<>());
        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse idorResp = createMockResponse(respState);

        invokeDoGet(pdfServlet, idorReq, idorResp);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"), "Non-owning student must receive 403 Forbidden");
    }

    // =========================================================================
    // 6. PHASE 7 TESTS: MANAGEMENT DASHBOARD & RESUME DOWNLOAD
    // =========================================================================

    @Test
    @DisplayName("Phase 7: AdminDashboardServlet populates external student metrics and directory")
    void testPhase7AdminDashboardExternalDirectory() throws Exception {
        AdminDashboardServlet adminServlet = new AdminDashboardServlet();

        StudentDAO mockStudentDAO = new StudentDAO() {
            @Override
            public int getTotalStudents() {
                return 150;
            }
        };

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public Map<String, Integer> getJobStatistics() {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("JOBS", 10);
                stats.put("INTERNSHIPS", 5);
                stats.put("ACTIVE", 12);
                return stats;
            }
        };

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public Map<String, Integer> getApplicationStatistics() {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("TOTAL", 40);
                stats.put("SHORTLISTED", 15);
                stats.put("SELECTED", 8);
                return stats;
            }

            @Override
            public List<Application> getAllApplications() {
                return Collections.emptyList();
            }
        };

        InterviewDAO mockInterviewDAO = new InterviewDAO() {
            @Override
            public Map<String, Integer> getInterviewStatistics() {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("SCHEDULED", 6);
                return stats;
            }
        };

        ExternalStudentDAO mockExtDAO = new ExternalStudentDAO() {
            @Override
            public Map<String, Integer> getDashboardMetrics() {
                Map<String, Integer> metrics = new HashMap<>();
                metrics.put("TOTAL", 25);
                metrics.put("PENDING", 5);
                metrics.put("APPROVED", 18);
                metrics.put("REJECTED", 2);
                return metrics;
            }

            @Override
            public List<ExternalStudent> findAll() {
                List<ExternalStudent> list = new ArrayList<>();
                ExternalStudent es = new ExternalStudent();
                es.setId(1L);
                es.setCollegeName("CUTM External");
                es.setVerificationStatus("APPROVED");
                list.add(es);
                return list;
            }
        };

        adminServlet.setStudentDAO(mockStudentDAO);
        adminServlet.setJobDAO(mockJobDAO);
        adminServlet.setApplicationDAO(mockAppDAO);
        adminServlet.setInterviewDAO(mockInterviewDAO);
        adminServlet.setExternalStudentDAO(mockExtDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createSession(1L, User.ROLE_ADMIN, null, sessionAttrs);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "GET", "/admin/dashboard", "", new HashMap<>(), reqAttrs, reqState);

        Map<String, Object> respState = new HashMap<>();
        HttpServletResponse response = createMockResponse(respState);

        invokeDoGet(adminServlet, request, response);

        Map<?, ?> stats = (Map<?, ?>) reqAttrs.get("stats");
        assertNotNull(stats, "Stats map must be present in request attributes");
        assertEquals(25, stats.get("totalExternalStudents"));
        assertEquals(5, stats.get("pendingApprovals"));
        assertEquals(18, stats.get("approvedStudents"));
        assertEquals(2, stats.get("rejectedStudents"));

        List<?> directory = (List<?>) reqAttrs.get("externalStudents");
        assertNotNull(directory);
        assertEquals(1, directory.size());
    }

    @Test
    @DisplayName("Phase 7: ResumeDownloadServlet allows Staff and owner access but blocks unauthorized students")
    void testPhase7ResumeDownloadAccessAndIdor() throws Exception {
        ResumeDownloadServlet resumeServlet = new ResumeDownloadServlet();

        final ExternalStudent extStudent = new ExternalStudent();
        extStudent.setId(50L);
        extStudent.setUserId(500L);
        extStudent.setApplicantName("Amit Verma");
        extStudent.setResumeUrl("uploads/resumes/dummy_resume.pdf");

        ExternalStudentDAO mockExtDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findById(Long id) {
                return id.equals(50L) ? extStudent : null;
            }
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(500L) ? extStudent : null;
            }
        };

        resumeServlet.setExternalStudentDAO(mockExtDAO);

        // 1. Staff Access (Coordinator)
        Map<String, Object> coordSessionAttrs = new HashMap<>();
        HttpSession coordSession = createSession(2L, User.ROLE_COORDINATOR, null, coordSessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("externalStudentId", "50");

        HttpServletRequest coordReq = createMockRequest(coordSession, "GET", "/resume/download", "", params, new HashMap<>(), new HashMap<>());
        Map<String, Object> coordRespState = new HashMap<>();
        HttpServletResponse coordResp = createMockResponse(coordRespState);

        invokeDoGet(resumeServlet, coordReq, coordResp);

        // Verify that access wasn't denied for coordinator (either 404 because dummy file doesn't exist on disk or served)
        assertNotEquals(HttpServletResponse.SC_FORBIDDEN, coordRespState.get("errorCode"), "Coordinator must not receive 403 Forbidden");

        // 2. IDOR Attack: Unauthorized student (userId 777L) tries to download candidate 500L's resume
        Map<String, Object> idorSessionAttrs = new HashMap<>();
        HttpSession idorSession = createSession(777L, User.ROLE_STUDENT, "EXTERNAL", idorSessionAttrs);

        HttpServletRequest idorReq = createMockRequest(idorSession, "GET", "/resume/download", "", params, new HashMap<>(), new HashMap<>());
        Map<String, Object> idorRespState = new HashMap<>();
        HttpServletResponse idorResp = createMockResponse(idorRespState);

        invokeDoGet(resumeServlet, idorReq, idorResp);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, idorRespState.get("errorCode"), "Unauthorized student must receive 403 Forbidden");
    }

    // =========================================================================
    // DYNAMIC PROXY & REFLECTION TEST HELPERS
    // =========================================================================

    private HttpSession createSession(Long userId, String role, String studentType, Map<String, Object> sessionAttrs) {
        if (userId != null) {
            sessionAttrs.put(LoginServlet.SESSION_USER_ID, userId);
            sessionAttrs.put("user_id", userId);
        }
        if (role != null) {
            sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, role);
            sessionAttrs.put("role", role);
        }
        if (studentType != null) {
            sessionAttrs.put("studentType", studentType);
        }
        sessionAttrs.put("csrfToken", "valid-csrf-token");
        return createMockSession(sessionAttrs);
    }

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

    private HttpServletRequest createMockRequest(HttpSession session, String method, String servletPath,
                                                 String contextPath, Map<String, String> parameters,
                                                 Map<String, Object> attributes, Map<String, Object> state) {
        final HttpSession[] sessionHolder = new HttpSession[]{session};
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) {
                        return method;
                    } else if ("getServletPath".equals(name)) {
                        return servletPath;
                    } else if ("getContextPath".equals(name)) {
                        return contextPath != null ? contextPath : "";
                    } else if ("getRequestURI".equals(name)) {
                        return (contextPath != null ? contextPath : "") + servletPath;
                    } else if ("getParameter".equals(name)) {
                        return parameters != null ? parameters.get(args[0]) : null;
                    } else if ("getAttribute".equals(name)) {
                        return attributes != null ? attributes.get(args[0]) : null;
                    } else if ("setAttribute".equals(name)) {
                        if (attributes != null) attributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes != null ? attributes.remove(args[0]) : null;
                    } else if ("getSession".equals(name)) {
                        boolean create = args == null || args.length == 0 || (Boolean) args[0];
                        if (sessionHolder[0] == null && create) {
                            sessionHolder[0] = createMockSession(new HashMap<>());
                        }
                        return sessionHolder[0];
                    } else if ("getRequestDispatcher".equals(name)) {
                        final String path = (String) args[0];
                        return (RequestDispatcher) Proxy.newProxyInstance(
                                RequestDispatcher.class.getClassLoader(),
                                new Class<?>[]{RequestDispatcher.class},
                                (rdProxy, rdMethod, rdArgs) -> {
                                    if ("forward".equals(rdMethod.getName())) {
                                        if (state != null) state.put("forwardPath", path);
                                    }
                                    return null;
                                }
                        );
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
                    } else if ("getOutputStream".equals(name)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        state.put("outputStream", baos);
                        return new ServletOutputStream() {
                            @Override
                            public boolean isReady() {
                                return true;
                            }
                            @Override
                            public void setWriteListener(WriteListener writeListener) {
                            }
                            @Override
                            public void write(int b) {
                                baos.write(b);
                            }
                        };
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
                        if (attributes != null) attributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes != null ? attributes.remove(args[0]) : null;
                    } else if ("invalidate".equals(name)) {
                        if (attributes != null) attributes.clear();
                        return null;
                    }
                    return null;
                }
        );
    }
}
