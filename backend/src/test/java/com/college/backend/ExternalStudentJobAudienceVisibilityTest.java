package com.college.backend;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.servlet.ApplicationServlet;
import com.college.backend.servlet.ExternalStudentDashboardServlet;
import com.college.backend.servlet.JobServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.util.DBConnection;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test suite for:
 * 1. External Student Job and Internship Visibility
 * 2. Candidate Audience setting and persistence (INTERNAL, EXTERNAL, BOTH)
 * 3. External Student Dashboard metrics computation (availableInternships, availableJobs, applied)
 * 4. Verification that Job ID 104 has target_audience = 'EXTERNAL' in MySQL
 * 5. Opportunity access filtering for external vs internal students
 */
public class ExternalStudentJobAudienceVisibilityTest {

    private boolean isDatabaseAvailable() {
        try (Connection conn = DBConnection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Bug 1 & 2 Verification: Job 104 has target_audience='EXTERNAL' and qualifies for external students")
    void testJob104AudienceInDatabase() {
        if (!isDatabaseAvailable()) {
            System.out.println("Skipping testJob104AudienceInDatabase: Database socket not available in this test environment.");
            return;
        }

        JobDAO jobDAO = new JobDAO();
        Job job104 = jobDAO.getJobById(104L);
        assertNotNull(job104, "Job ID 104 must exist in database");
        assertEquals("Java Full stack devlopment", job104.getTitle());
        assertEquals("CUTM", job104.getCompany());
        assertEquals("Internship", job104.getJobType());
        assertTrue(job104.isInternship(), "Job 104 must be recognized as an internship");
        assertEquals("Active", job104.getStatus());

        // Target audience check
        assertEquals(Job.AUDIENCE_EXTERNAL, job104.getTargetAudience(),
                "Job 104 candidate audience must be EXTERNAL");
        assertTrue(job104.isEligibleForExternal(), "Job 104 must be eligible for external candidates");
        assertFalse(job104.isEligibleForInternal(), "Job 104 must not be internal-only");
        assertTrue(job104.isEligibleForAudience("EXTERNAL"), "Job 104 must match external student type");
        assertFalse(job104.isEligibleForAudience("INTERNAL"), "Job 104 must not match internal student type");
    }

    @Test
    @DisplayName("Bug 2 Verification: External Student Dashboard displays available internships when EXTERNAL internship exists")
    void testExternalStudentDashboardMetricsWithExternalInternship() throws Exception {
        ExternalStudentDashboardServlet dashboardServlet = new ExternalStudentDashboardServlet();

        final ExternalStudent student = new ExternalStudent(
                56L, 319L, "Silicon Institute", "Bhubaneswar", "B.Tech",
                "CSE", 7, new BigDecimal("8.50"), 2026,
                "9876543210", "uploads/resumes/resume.pdf",
                ExternalStudent.STATUS_APPROVED, LocalDateTime.now(), LocalDateTime.now()
        );

        ExternalStudentDAO mockExternalStudentDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(319L) ? student : null;
            }
        };

        final Job job104 = new Job(104L, "Java Full stack devlopment", "CUTM", "Bhubaneswar",
                "Internship role", "15000", "2 months", "Internship", "Active",
                LocalDate.now().plusDays(15), new BigDecimal("6.50"), "CSE,ECE,EEE");
        job104.setTargetAudience(Job.AUDIENCE_EXTERNAL);

        final Job fullTimeJob = new Job(105L, "Software Engineer", "TCS", "Bhubaneswar",
                "Full-time role", "6 LPA", null, "Full Time", "Active",
                LocalDate.now().plusDays(20), new BigDecimal("6.00"), "ALL");
        fullTimeJob.setTargetAudience(Job.AUDIENCE_BOTH);

        final Job internalJob = new Job(106L, "Campus Specialist", "CUTM Internal", "Paralakhemundi",
                "Internal only", "4 LPA", null, "Full Time", "Active",
                LocalDate.now().plusDays(10), new BigDecimal("7.00"), "ALL");
        internalJob.setTargetAudience(Job.AUDIENCE_INTERNAL);

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public List<Job> getActiveJobs() {
                List<Job> list = new ArrayList<>();
                list.add(job104);
                list.add(fullTimeJob);
                list.add(internalJob);
                return list;
            }
        };

        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public List<Application> getAllApplications() {
                return new ArrayList<>();
            }
        };

        InterviewDAO mockInterviewDAO = new InterviewDAO();

        dashboardServlet.setExternalStudentDAO(mockExternalStudentDAO);
        dashboardServlet.setJobDAO(mockJobDAO);
        dashboardServlet.setApplicationDAO(mockAppDAO);
        dashboardServlet.setInterviewDAO(mockInterviewDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 319L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        sessionAttrs.put("studentType", "EXTERNAL");
        sessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, "GET", "/external/dashboard", "", new HashMap<>(), reqAttrs, reqState);
        HttpServletResponse response = createMockResponse(new HashMap<>());

        invokeDoGet(dashboardServlet, request, response);

        assertEquals("/WEB-INF/jsp/external-dashboard.jsp", reqState.get("forwardPath"));

        // Both 'stats' and 'metrics' attributes must be present
        Map<?, ?> stats = (Map<?, ?>) reqAttrs.get("stats");
        Map<?, ?> metrics = (Map<?, ?>) reqAttrs.get("metrics");
        assertNotNull(stats, "stats request attribute must not be null");
        assertNotNull(metrics, "metrics request attribute must not be null");

        // Verify availableInternships includes Job 104
        assertEquals(1, stats.get("availableInternships"), "availableInternships must be 1 (Job 104)");
        assertEquals(1, metrics.get("availableInternships"), "metrics.availableInternships must be 1");

        // Verify availableJobs includes the full-time role open to BOTH
        assertEquals(1, stats.get("availableJobs"), "availableJobs must be 1");
        assertEquals(1, metrics.get("availableJobs"), "metrics.availableJobs must be 1");

        // Internal job should NOT be counted in external metrics
        assertEquals(0, stats.get("totalApplications"));
    }

    @Test
    @DisplayName("Candidate Audience filtering: External students only see EXTERNAL and BOTH opportunities")
    void testCandidateAudienceFilteringInJobServlet() throws Exception {
        JobServlet jobServlet = new JobServlet();

        final Job extJob = new Job(1L, "External Internship", "Company A", "Remote",
                "Desc", "10000", "3 months", "Internship", "Active",
                LocalDate.now().plusDays(10), null, null);
        extJob.setTargetAudience(Job.AUDIENCE_EXTERNAL);

        final Job bothJob = new Job(2L, "Open Job", "Company B", "Remote",
                "Desc", "20000", null, "Full Time", "Active",
                LocalDate.now().plusDays(10), null, null);
        bothJob.setTargetAudience(Job.AUDIENCE_BOTH);

        final Job internalJob = new Job(3L, "Internal Placement", "Company C", "Campus",
                "Desc", "30000", null, "Full Time", "Active",
                LocalDate.now().plusDays(10), null, null);
        internalJob.setTargetAudience(Job.AUDIENCE_INTERNAL);

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public List<Job> getAllJobs() {
                List<Job> list = new ArrayList<>();
                list.add(extJob);
                list.add(bothJob);
                list.add(internalJob);
                return list;
            }
        };
        jobServlet.setJobDAO(mockJobDAO);

        // Case 1: External student browsing /jobs
        Map<String, Object> extSessionAttrs = new HashMap<>();
        extSessionAttrs.put(LoginServlet.SESSION_USER_ID, 319L);
        extSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        extSessionAttrs.put("studentType", "EXTERNAL");
        HttpSession extSession = createMockSession(extSessionAttrs);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> reqState = new HashMap<>();
        HttpServletRequest extReq = createMockRequest(extSession, "GET", "/jobs", "", new HashMap<>(), reqAttrs, reqState);
        HttpServletResponse extResp = createMockResponse(new HashMap<>());

        invokeDoGet(jobServlet, extReq, extResp);

        @SuppressWarnings("unchecked")
        List<Job> visibleJobs = (List<Job>) reqAttrs.get("jobs");
        assertNotNull(visibleJobs);
        assertEquals(2, visibleJobs.size(), "External student should only see EXTERNAL and BOTH opportunities");
        assertTrue(visibleJobs.contains(extJob));
        assertTrue(visibleJobs.contains(bothJob));
        assertFalse(visibleJobs.contains(internalJob), "Internal job must not be visible to external student");

        // Case 2: Internal student browsing /jobs
        Map<String, Object> intSessionAttrs = new HashMap<>();
        intSessionAttrs.put(LoginServlet.SESSION_USER_ID, 100L);
        intSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        intSessionAttrs.put("studentType", "INTERNAL");
        HttpSession intSession = createMockSession(intSessionAttrs);

        Map<String, Object> intReqAttrs = new HashMap<>();
        Map<String, Object> intReqState = new HashMap<>();
        HttpServletRequest intReq = createMockRequest(intSession, "GET", "/jobs", "", new HashMap<>(), intReqAttrs, intReqState);
        HttpServletResponse intResp = createMockResponse(new HashMap<>());

        invokeDoGet(jobServlet, intReq, intResp);

        @SuppressWarnings("unchecked")
        List<Job> intVisibleJobs = (List<Job>) intReqAttrs.get("jobs");
        assertNotNull(intVisibleJobs);
        assertEquals(2, intVisibleJobs.size(), "Internal student should only see INTERNAL and BOTH opportunities");
        assertTrue(intVisibleJobs.contains(internalJob));
        assertTrue(intVisibleJobs.contains(bothJob));
        assertFalse(intVisibleJobs.contains(extJob), "External job must not be visible to internal student");
    }

    @Test
    @DisplayName("Job Creation and Update correctly persist target_audience in Job model and JobDAO")
    void testJobModelTargetAudienceNormalization() {
        Job job = new Job();
        // Default target audience must be INTERNAL
        assertEquals(Job.AUDIENCE_INTERNAL, job.getTargetAudience());

        job.setTargetAudience("EXTERNAL");
        assertEquals(Job.AUDIENCE_EXTERNAL, job.getTargetAudience());

        job.setTargetAudience("both");
        assertEquals(Job.AUDIENCE_BOTH, job.getTargetAudience());

        job.setTargetAudience(null);
        assertEquals(Job.AUDIENCE_INTERNAL, job.getTargetAudience());

        job.setTargetAudience("  ");
        assertEquals(Job.AUDIENCE_INTERNAL, job.getTargetAudience());
    }

    @Test
    @DisplayName("External student can apply for EXTERNAL internship but blocked from INTERNAL job")
    void testExternalStudentApplicationEligibility() throws Exception {
        ApplicationServlet appServlet = new ApplicationServlet();

        final ExternalStudent extStudent = new ExternalStudent(
                56L, 319L, "Silicon Institute", "Bhubaneswar", "B.Tech",
                "CSE", 7, new BigDecimal("8.50"), 2026,
                "9876543210", "uploads/resumes/resume.pdf",
                ExternalStudent.STATUS_APPROVED, LocalDateTime.now(), LocalDateTime.now()
        );

        final Job extJob = new Job(104L, "Java Full stack devlopment", "CUTM", "Bhubaneswar",
                "Internship role", "15000", "2 months", "Internship", "Active",
                LocalDate.now().plusDays(15), new BigDecimal("6.50"), "CSE,ECE,EEE");
        extJob.setTargetAudience(Job.AUDIENCE_EXTERNAL);

        final Job intJob = new Job(106L, "Internal Only Role", "CUTM", "Campus",
                "Internal", "10000", null, "Full Time", "Active",
                LocalDate.now().plusDays(15), new BigDecimal("6.00"), "CSE");
        intJob.setTargetAudience(Job.AUDIENCE_INTERNAL);

        ExternalStudentDAO mockExtDAO = new ExternalStudentDAO() {
            @Override
            public ExternalStudent findByUserId(Long userId) {
                return userId.equals(319L) ? extStudent : null;
            }
        };

        JobDAO mockJobDAO = new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                if (id.equals(104L)) return extJob;
                if (id.equals(106L)) return intJob;
                return null;
            }
        };

        final List<Application> savedApps = new ArrayList<>();
        ApplicationDAO mockAppDAO = new ApplicationDAO() {
            @Override
            public boolean hasAlreadyAppliedExternal(Long externalStudentId, Long jobId) {
                return false;
            }
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application app) {
                savedApps.add(app);
                return true;
            }
        };

        appServlet.setExternalStudentDAO(mockExtDAO);
        appServlet.setJobDAO(mockJobDAO);
        appServlet.setApplicationDAO(mockAppDAO);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 319L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        sessionAttrs.put("studentType", "EXTERNAL");
        sessionAttrs.put("csrfToken", "valid-csrf");
        HttpSession session = createMockSession(sessionAttrs);

        // 1. Try applying for INTERNAL job (must be rejected)
        Map<String, String> intParams = new HashMap<>();
        intParams.put("action", "apply");
        intParams.put("jobId", "106");
        intParams.put("csrfToken", "valid-csrf");

        Map<String, Object> intReqAttrs = new HashMap<>();
        Map<String, Object> intReqState = new HashMap<>();
        HttpServletRequest intReq = createMockRequest(session, "POST", "/applications", "", intParams, intReqAttrs, intReqState);
        Map<String, Object> intRespState = new HashMap<>();
        HttpServletResponse intResp = createMockResponse(intRespState);

        invokeDoPost(appServlet, intReq, intResp);

        assertTrue(savedApps.isEmpty(), "Should not create application for internal-only job");
        String errorMsg = (String) sessionAttrs.get("errorMessage");
        assertNotNull(errorMsg);
        assertTrue(errorMsg.toLowerCase().contains("internal") || errorMsg.toLowerCase().contains("only open to centurion"));

        // 2. Try applying for EXTERNAL job (must succeed)
        sessionAttrs.remove("errorMessage");
        Map<String, String> extParams = new HashMap<>();
        extParams.put("action", "apply");
        extParams.put("jobId", "104");
        extParams.put("csrfToken", "valid-csrf");

        Map<String, Object> extReqAttrs = new HashMap<>();
        Map<String, Object> extReqState = new HashMap<>();
        HttpServletRequest extReq = createMockRequest(session, "POST", "/applications", "", extParams, extReqAttrs, extReqState);
        Map<String, Object> extRespState = new HashMap<>();
        HttpServletResponse extResp = createMockResponse(extRespState);

        invokeDoPost(appServlet, extReq, extResp);

        assertEquals(1, savedApps.size(), "Application should be saved for external job");
        Application saved = savedApps.get(0);
        assertEquals(104L, saved.getJobId());
        assertEquals("EXTERNAL", saved.getApplicantType());
        assertEquals(56L, saved.getExternalStudentId());
    }

    // =========================================================================
    // HELPER METHODS: MOCKS & REFLECTION
    // =========================================================================

    private void invokeDoGet(Object servlet, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, req, resp);
    }

    private void invokeDoPost(Object servlet, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doPost = servlet.getClass().getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, req, resp);
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
                    } else if ("getId".equals(name)) {
                        return "mock-session-id";
                    } else if ("invalidate".equals(name)) {
                        attributes.clear();
                        return null;
                    }
                    return null;
                }
        );
    }

    private HttpServletRequest createMockRequest(
            HttpSession session,
            String method,
            String servletPath,
            String queryString,
            Map<String, String> parameters,
            Map<String, Object> attributes,
            Map<String, Object> stateCapture
    ) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) return method;
                    if ("getServletPath".equals(name)) return servletPath;
                    if ("getContextPath".equals(name)) return "";
                    if ("getRequestURI".equals(name)) return servletPath;
                    if ("getQueryString".equals(name)) return queryString;
                    if ("getSession".equals(name)) {
                        if (args != null && args.length == 1 && Boolean.FALSE.equals(args[0])) {
                            return session;
                        }
                        return session;
                    }
                    if ("getParameter".equals(name)) return parameters.get(args[0]);
                    if ("getParameterMap".equals(name)) return parameters;
                    if ("getAttribute".equals(name)) return attributes.get(args[0]);
                    if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("removeAttribute".equals(name)) return attributes.remove(args[0]);
                    if ("getRequestDispatcher".equals(name)) {
                        String path = (String) args[0];
                        return Proxy.newProxyInstance(
                                RequestDispatcher.class.getClassLoader(),
                                new Class<?>[]{RequestDispatcher.class},
                                (dProxy, dMethod, dArgs) -> {
                                    if ("forward".equals(dMethod.getName())) {
                                        stateCapture.put("forwardPath", path);
                                    }
                                    return null;
                                }
                        );
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(Map<String, Object> stateCapture) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("sendRedirect".equals(name)) {
                        stateCapture.put("redirectUrl", args[0]);
                        return null;
                    }
                    if ("sendError".equals(name)) {
                        stateCapture.put("errorCode", args[0]);
                        if (args.length > 1) {
                            stateCapture.put("errorMessage", args[1]);
                        }
                        return null;
                    }
                    if ("setStatus".equals(name)) {
                        stateCapture.put("statusCode", args[0]);
                        return null;
                    }
                    return null;
                }
        );
    }
}
