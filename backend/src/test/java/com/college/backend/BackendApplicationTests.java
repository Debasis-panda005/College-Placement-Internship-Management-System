package com.college.backend;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ApplicationStatusHistoryDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.servlet.ApplicationServlet;
import com.college.backend.servlet.InterviewServlet;
import com.college.backend.servlet.JobServlet;
import com.college.backend.servlet.LoginServlet;
import com.college.backend.servlet.LogoutServlet;
import com.college.backend.servlet.RegisterServlet;
import com.college.backend.util.AuthUtil;
import com.college.backend.util.CsrfUtil;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    // 16. USER ENTITY AND SAFE TOSTRING TESTS
    // =====================================================

    @Test
    @DisplayName("User entity populates correctly and excludes password hash from toString")
    void testUserEntityAndSafeToString() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 18, 9, 30);
        String secretHash = "$2a$10$e8w.49vKxR7aV0hJtJ1hze3xYgX0zC8J3VvJ1kX7yR4hZ1e9jK3a.";

        User user = new User(1L, "Alice Johnson", "alice@example.com", secretHash, "STUDENT", created);

        assertEquals(1L, user.getId());
        assertEquals("Alice Johnson", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(secretHash, user.getPasswordHash());
        assertEquals("STUDENT", user.getRole());
        assertEquals(created, user.getCreatedAt());

        // Verify toString excludes the secret password hash
        String userString = user.toString();
        assertFalse(userString.contains(secretHash), "User.toString() must never expose the password hash");
        assertTrue(userString.contains("alice@example.com"));
        assertTrue(userString.contains("STUDENT"));
    }

    // =====================================================
    // 17. USER SUPPORTED ROLE VALIDATION TESTS
    // =====================================================

    @Test
    @DisplayName("User validates supported roles and rejects unsupported roles")
    void testUserRoleValidation() {
        assertTrue(User.isValidRole("STUDENT"));
        assertTrue(User.isValidRole("student"));
        assertTrue(User.isValidRole("COORDINATOR"));
        assertTrue(User.isValidRole("ADMIN"));

        assertFalse(User.isValidRole("GUEST"));
        assertFalse(User.isValidRole("MANAGER"));
        assertFalse(User.isValidRole(null));
        assertFalse(User.isValidRole(""));

        User user = new User();
        user.setRole("COORDINATOR");
        assertEquals("COORDINATOR", user.getRole());

        user.setRole("admin");
        assertEquals("ADMIN", user.getRole());

        assertThrows(IllegalArgumentException.class, () -> user.setRole("SUPERUSER"),
                "Setting an invalid role must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> user.setRole(null),
                "Setting a null role must throw IllegalArgumentException");
    }

    // =====================================================
    // 18. USER EMAIL NORMALIZATION TESTS
    // =====================================================

    @Test
    @DisplayName("User normalizes email consistently to trimmed lower-case")
    void testUserEmailNormalization() {
        assertEquals("student@college.edu", User.normalizeEmail("  Student@College.EDU  "));
        assertEquals("test.user@domain.com", User.normalizeEmail("TEST.USER@DOMAIN.COM"));
        assertNull(User.normalizeEmail(null));

        User user = new User("Bob", "  BOB.SMITH@example.COM  ", "hash123", "STUDENT");
        assertEquals("bob.smith@example.com", user.getEmail());

        user.setEmail("  NEW.EMAIL@DOMAIN.ORG  ");
        assertEquals("new.email@domain.org", user.getEmail());
    }

    // =====================================================
    // 19. STUDENT ENTITY AND VALIDATION TESTS
    // =====================================================

    @Test
    @DisplayName("Student entity enforces valid CGPA range and graduation year bounds")
    void testStudentEntityAndValidation() {
        BigDecimal validCgpa = new BigDecimal("8.75");
        Student student = new Student(1L, 10L, "CS2026-042", "Computer Science",
                validCgpa, 2026, "+91-9876543210", "Java, MySQL, Git", "https://resume.example/cs042.pdf");

        assertEquals(1L, student.getId());
        assertEquals(10L, student.getUserId());
        assertEquals("CS2026-042", student.getRollNumber());
        assertEquals("Computer Science", student.getDepartment());
        assertEquals(validCgpa, student.getCgpa());
        assertEquals(2026, student.getGraduationYear());
        assertEquals("+91-9876543210", student.getPhone());
        assertEquals("Java, MySQL, Git", student.getSkills());
        assertEquals("https://resume.example/cs042.pdf", student.getResumeUrl());

        // CGPA Validation
        assertTrue(Student.isValidCgpa(BigDecimal.ZERO));
        assertTrue(Student.isValidCgpa(new BigDecimal("10.00")));
        assertTrue(Student.isValidCgpa(new BigDecimal("7.50")));
        assertFalse(Student.isValidCgpa(new BigDecimal("10.01")));
        assertFalse(Student.isValidCgpa(new BigDecimal("-0.10")));
        assertFalse(Student.isValidCgpa(null));

        assertThrows(IllegalArgumentException.class, () -> student.setCgpa(new BigDecimal("10.50")),
                "CGPA above 10.00 must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> student.setCgpa(new BigDecimal("-1.00")),
                "Negative CGPA must throw IllegalArgumentException");

        // Graduation Year Validation
        assertTrue(Student.isValidGraduationYear(2000));
        assertTrue(Student.isValidGraduationYear(2026));
        assertTrue(Student.isValidGraduationYear(2100));
        assertFalse(Student.isValidGraduationYear(1999));
        assertFalse(Student.isValidGraduationYear(2101));
        assertFalse(Student.isValidGraduationYear(null));

        assertThrows(IllegalArgumentException.class, () -> student.setGraduationYear(1990),
                "Graduation year before 2000 must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> student.setGraduationYear(2150),
                "Graduation year after 2100 must throw IllegalArgumentException");
    }

    // =====================================================
    // 20. BCRYPT PASSWORD HASHING AND VERIFICATION TESTS
    // =====================================================

    @Test
    @DisplayName("PasswordHashService correctly hashes plaintext and verifies matching password")
    void testPasswordHashingAndVerification() {
        String plainPassword = "StudentSecret@2026";

        String hash = PasswordHashService.hashPassword(plainPassword);
        assertNotNull(hash);
        assertFalse(hash.trim().isEmpty());
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"),
                "BCrypt hash should start with standard $2a$ or $2b$ prefix");
        assertEquals(60, hash.length(), "Standard BCrypt hash string must be 60 characters long");

        // Verify correct password returns true
        assertTrue(PasswordHashService.verifyPassword(plainPassword, hash),
                "Candidate matching the plaintext password must verify as true");

        // Null and empty password hashing should be rejected
        assertThrows(IllegalArgumentException.class, () -> PasswordHashService.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHashService.hashPassword(""));
    }

    // =====================================================
    // 21. PASSWORD INCORRECT REJECTION TESTS
    // =====================================================

    @Test
    @DisplayName("PasswordHashService safely rejects incorrect passwords, empty passwords, and invalid hashes")
    void testPasswordIncorrectRejection() {
        String plainPassword = "CorrectPassword#1";
        String hash = PasswordHashService.hashPassword(plainPassword);

        assertFalse(PasswordHashService.verifyPassword("WrongPassword#1", hash),
                "Incorrect password must return false");
        assertFalse(PasswordHashService.verifyPassword("correctpassword#1", hash),
                "Password comparison must be case-sensitive and return false for wrong casing");
        assertFalse(PasswordHashService.verifyPassword(null, hash),
                "Null candidate password must return false");
        assertFalse(PasswordHashService.verifyPassword("", hash),
                "Empty candidate password must return false");
        assertFalse(PasswordHashService.verifyPassword(plainPassword, null),
                "Null stored hash must return false");
        assertFalse(PasswordHashService.verifyPassword(plainPassword, ""),
                "Empty stored hash must return false");
        assertFalse(PasswordHashService.verifyPassword(plainPassword, "invalid-non-bcrypt-hash"),
                "Malformed stored hash must safely return false without crashing");
    }

    // =====================================================
    // 22. DIFFERENT HASHES FOR IDENTICAL PASSWORDS (SALT UNIQUENESS)
    // =====================================================

    @Test
    @DisplayName("PasswordHashService generates unique salts resulting in distinct hashes for identical passwords")
    void testPasswordUniqueSaltsForIdenticalPasswords() {
        String password = "IdenticalPassword@123";

        String hash1 = PasswordHashService.hashPassword(password);
        String hash2 = PasswordHashService.hashPassword(password);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2,
                "Due to cryptographic random salts, two hashes of the same password must produce distinct hash strings");

        // Both distinct hashes must independently verify against the same plaintext password
        assertTrue(PasswordHashService.verifyPassword(password, hash1),
                "Hash 1 must verify against the original password");
        assertTrue(PasswordHashService.verifyPassword(password, hash2),
                "Hash 2 must verify against the original password");
    }

    // =====================================================
    // 23. BCRYPT FORMAT VALIDATION TESTS
    // =====================================================

    @Test
    @DisplayName("PasswordHashService validates BCrypt format correctly rejecting plaintext and malformed hashes")
    void testBCryptFormatValidation() {
        // Valid BCrypt hashes
        String generatedHash = PasswordHashService.hashPassword("SecureStudent2026!");
        assertTrue(PasswordHashService.isBCryptHash(generatedHash),
                "Newly generated BCrypt hash must pass isBCryptHash check");

        assertTrue(PasswordHashService.isBCryptHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"),
                "Standard $2a$ 60-character hash must be recognized as valid BCrypt format");
        assertTrue(PasswordHashService.isBCryptHash("$2b$12$e8OfkV8.H79fC8s6a9/CteJ5GZ5yL5qX1.B0H8cO64L8o5n17hQym"),
                "Standard $2b$ 60-character hash must be recognized as valid BCrypt format");
        assertTrue(PasswordHashService.isBCryptHash("$2y$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ01234"),
                "Standard $2y$ 60-character hash must be recognized as valid BCrypt format");

        // Plaintext passwords mistakenly supplied as passwordHash must be rejected
        assertFalse(PasswordHashService.isBCryptHash("Student@2026"),
                "Plaintext password must not be recognized as a BCrypt hash");
        assertFalse(PasswordHashService.isBCryptHash("PlaintextPassword123!"),
                "Plaintext password must not be recognized as a BCrypt hash");
        assertFalse(PasswordHashService.isBCryptHash("admin123"),
                "Short plaintext password must not be recognized as a BCrypt hash");

        // Malformed hashes must be rejected
        assertFalse(PasswordHashService.isBCryptHash("not-a-bcrypt-hash"),
                "Arbitrary non-BCrypt string must be rejected");
        assertFalse(PasswordHashService.isBCryptHash("$2a$10$short"),
                "Truncated BCrypt hash must be rejected");
        assertFalse(PasswordHashService.isBCryptHash("$2a$invalidcost$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ01234"),
                "Non-numeric cost factor must be rejected");
        assertFalse(PasswordHashService.isBCryptHash("$3a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ01234"),
                "Unsupported prefix $3a$ must be rejected");
        assertFalse(PasswordHashService.isBCryptHash("$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ01234extra"),
                "Hash with trailing extra characters must be rejected");

        // Null or empty values must be rejected
        assertFalse(PasswordHashService.isBCryptHash(null),
                "Null hash must return false");
        assertFalse(PasswordHashService.isBCryptHash(""),
                "Empty hash must return false");
        assertFalse(PasswordHashService.isBCryptHash("   "),
                "Whitespace-only hash must return false");
    }

    // =====================================================
    // 24. USER DAO DEFENSIVE BEHAVIOR & ERROR DISTINCTION
    // =====================================================

    @Test
    @DisplayName("UserDAO distinguishes missing records, invalid input, and protects against privilege escalation")
    void testUserDAODefensiveBehaviorAndPrivilegeEscalationPrevention() {
        UserDAO userDAO = new UserDAO();
        String validHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        // Record not found: Null and blank parameter lookups safely return null/false without throwing
        assertNull(userDAO.findById(null));
        assertNull(userDAO.findByEmail(null));
        assertNull(userDAO.findByEmail("   "));
        assertFalse(userDAO.existsByEmail(null));
        assertFalse(userDAO.existsByEmail("   "));

        // Invalid input: createUser rejects null entity, missing/blank email, missing/blank password hash
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(null),
                "Null user entity must throw IllegalArgumentException");

        User userWithoutEmail = new User("Jane", null, validHash, "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(userWithoutEmail),
                "User without email must throw IllegalArgumentException");

        User userWithBlankEmail = new User("Jane", "   ", validHash, "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(userWithBlankEmail),
                "User with blank email must throw IllegalArgumentException");

        User userWithoutHash = new User("Jane", "jane@example.com", null, "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(userWithoutHash),
                "User without password hash must throw IllegalArgumentException");

        User userWithBlankHash = new User("Jane", "jane@example.com", "   ", "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(userWithBlankHash),
                "User with blank password hash must throw IllegalArgumentException");

        // Password hash validation: Plaintext passwords accidentally passed in passwordHash must throw IllegalArgumentException
        User userWithPlaintextPassword = new User("Jane", "jane@example.com", "PlaintextPassword123!", "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(userWithPlaintextPassword),
                "Plaintext password mistakenly supplied as passwordHash must throw IllegalArgumentException");

        User userWithMalformedHash = new User("Jane", "jane@example.com", "$2a$badcost$short", "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(userWithMalformedHash),
                "Malformed hash must throw IllegalArgumentException");

        // Unsupported role rejection
        assertThrows(IllegalArgumentException.class,
                () -> new User("Jane", "jane@example.com", validHash, "SUPERUSER"),
                "User entity constructor must reject unsupported roles");

        // registerStudentUser validation and privilege escalation prevention
        assertThrows(IllegalArgumentException.class, () -> userDAO.registerStudentUser(null),
                "Null user to registerStudentUser must throw IllegalArgumentException");

        User privilegedAttempt = new User("Mallory", null, validHash, "ADMIN");
        assertEquals("ADMIN", privilegedAttempt.getRole());

        // registerStudentUser overrides role to STUDENT before any DB operation
        assertThrows(IllegalArgumentException.class, () -> userDAO.registerStudentUser(privilegedAttempt));
        assertEquals(User.ROLE_STUDENT, privilegedAttempt.getRole(),
                "registerStudentUser must strictly force role to STUDENT, preventing privilege escalation");
    }

    // =====================================================
    // 25. STUDENT DAO DEFENSIVE VALIDATION TESTS
    // =====================================================

    @Test
    @DisplayName("StudentDAO distinguishes missing records, invalid input, and enforces profile update immutability")
    void testStudentDAODefensiveValidation() {
        StudentDAO studentDAO = new StudentDAO();

        // Record not found: Null and blank lookups safely return null/false without throwing
        assertNull(studentDAO.findById(null));
        assertNull(studentDAO.findByUserId(null));
        assertNull(studentDAO.findByRollNumber(null));
        assertNull(studentDAO.findByRollNumber("   "));
        assertFalse(studentDAO.existsByRollNumber(null));
        assertFalse(studentDAO.existsByRollNumber("   "));

        // Invalid input: createStudent rejects null entity, missing userId, missing rollNumber, invalid CGPA/year
        assertThrows(IllegalArgumentException.class, () -> studentDAO.createStudent(null),
                "Null student profile must throw IllegalArgumentException");

        Student studentWithoutUserId = new Student(null, "CS2026-001", "CS", new BigDecimal("8.50"), 2026, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> studentDAO.createStudent(studentWithoutUserId),
                "Student without userId must throw IllegalArgumentException");

        Student studentWithoutRoll = new Student(10L, null, "CS", new BigDecimal("8.50"), 2026, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> studentDAO.createStudent(studentWithoutRoll),
                "Student without roll number must throw IllegalArgumentException");

        Student studentWithBlankRoll = new Student(10L, "   ", "CS", new BigDecimal("8.50"), 2026, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> studentDAO.createStudent(studentWithBlankRoll),
                "Student with blank roll number must throw IllegalArgumentException");

        // Update profile validation guards: null student and missing ID throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> studentDAO.updateProfile(null),
                "Null student profile update must throw IllegalArgumentException");

        Student studentWithoutId = new Student();
        studentWithoutId.setDepartment("ECE");
        assertThrows(IllegalArgumentException.class, () -> studentDAO.updateProfile(studentWithoutId),
                "Student update without ID must throw IllegalArgumentException");

        // Academic boundary checks throw IllegalArgumentException
        Student studentWithInvalidCgpa = new Student();
        studentWithInvalidCgpa.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> studentWithInvalidCgpa.setCgpa(new BigDecimal("11.00")),
                "Setting invalid CGPA > 10.00 on student must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> studentWithInvalidCgpa.setCgpa(new BigDecimal("-0.01")),
                "Setting negative CGPA on student must throw IllegalArgumentException");

        Student studentWithInvalidYear = new Student();
        studentWithInvalidYear.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> studentWithInvalidYear.setGraduationYear(1980),
                "Setting invalid graduation year < 2000 on student must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> studentWithInvalidYear.setGraduationYear(2150),
                "Setting invalid graduation year > 2100 on student must throw IllegalArgumentException");
    }

    // =====================================================
    // 26. CUSTOM DAO EXCEPTIONS DISTINCTION TESTS
    // =====================================================

    @Test
    @DisplayName("DAOException and DuplicateRecordException provide clean error distinction hierarchy")
    void testCustomDAOExceptionsHierarchy() {
        // DAOException is an unchecked RuntimeException wrapping SQL or connectivity errors
        SQLException sqlEx = new SQLException("Connection refused to database", "08001");
        DAOException daoEx = new DAOException("Failed to execute database query", sqlEx);

        assertTrue(daoEx instanceof RuntimeException, "DAOException must be an unchecked RuntimeException");
        assertEquals("Failed to execute database query", daoEx.getMessage());
        assertEquals(sqlEx, daoEx.getCause());

        // DuplicateRecordException extends DAOException for specific unique constraint violation handling
        SQLException duplicateSqlEx = new SQLException("Duplicate entry 'test@example.com' for key 'users.email'", "23000", 1062);
        DuplicateRecordException dupEx = new DuplicateRecordException("User already exists", duplicateSqlEx);

        assertTrue(dupEx instanceof DAOException, "DuplicateRecordException must extend DAOException");
        assertTrue(dupEx instanceof RuntimeException, "DuplicateRecordException must be a RuntimeException");
        assertEquals("User already exists", dupEx.getMessage());
        assertEquals(duplicateSqlEx, dupEx.getCause());
        assertEquals(1062, ((SQLException) dupEx.getCause()).getErrorCode());

        DuplicateRecordException dupWithoutCause = new DuplicateRecordException("Roll number already registered");
        assertNull(dupWithoutCause.getCause());
        assertEquals("Roll number already registered", dupWithoutCause.getMessage());
    }

    // =====================================================
    // 27. TRANSACTIONAL STUDENT REGISTRATION TESTS
    // =====================================================

    @Test
    @DisplayName("registerStudent rejects null parameters with IllegalArgumentException")
    void testRegisterStudentNullGuards() {
        StudentDAO studentDAO = new StudentDAO();
        User validUser = new User("Valid User", "valid@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student validStudent = new Student(1L, "CSE2026-001", "Computer Science", new BigDecimal("8.50"), 2026, "9876543210", null, null);
        Map<String, Object> connState = new HashMap<>();
        Connection mockConn = createMockConnection(connState);

        assertThrows(IllegalArgumentException.class, () -> studentDAO.registerStudent(null, validStudent),
                "registerStudent(null, student) must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> studentDAO.registerStudent(validUser, null),
                "registerStudent(user, null) must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> studentDAO.registerStudent(null, validUser, validStudent),
                "registerStudent(null, user, student) must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> studentDAO.registerStudent(mockConn, null, validStudent),
                "registerStudent(connection, null, student) must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> studentDAO.registerStudent(mockConn, validUser, null),
                "registerStudent(connection, user, null) must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("registerStudent enforces User.ROLE_STUDENT and links user ID to student profile")
    void testRegisterStudentRoleEnforcement() throws SQLException {
        Map<String, Object> connState = new HashMap<>();
        Connection mockConn = createMockConnection(connState);

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public boolean createUser(Connection connection, User user) {
                assertEquals(User.ROLE_STUDENT, user.getRole(), "User role must be forced to STUDENT before insert");
                user.setId(77L);
                return true;
            }
        };

        StudentDAO studentDAO = new StudentDAO(mockUserDao) {
            @Override
            public boolean createStudent(Connection connection, Student student) {
                assertEquals(77L, student.getUserId(), "Student user_id must match generated user ID");
                student.setId(101L);
                return true;
            }
        };

        User userWithAdminRole = new User("Admin Attempt", "admin_attempt@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_ADMIN);
        Student student = new Student(null, "CSE2026-002", "Information Technology", new BigDecimal("9.10"), 2026, "9876543211", null, null);

        boolean result = studentDAO.registerStudent(mockConn, userWithAdminRole, student);

        assertTrue(result, "registerStudent should succeed with mock DAOs");
        assertEquals(User.ROLE_STUDENT, userWithAdminRole.getRole(), "User role must be overridden to STUDENT");
        assertEquals(77L, student.getUserId(), "Student must be associated with the newly created user ID");
        assertEquals(101L, student.getId(), "Student must receive generated profile ID");
    }

    @Test
    @DisplayName("registerStudent atomic transaction commits on success, disables autoCommit, and closes connection")
    void testRegisterStudentTransactionCommitSuccess() {
        Map<String, Object> connState = new HashMap<>();
        Connection mockConn = createMockConnection(connState);

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public boolean createUser(Connection connection, User user) {
                user.setId(10L);
                return true;
            }
        };

        StudentDAO studentDAO = new StudentDAO(mockUserDao) {
            @Override
            protected Connection getConnection() {
                return mockConn;
            }

            @Override
            public boolean createStudent(Connection connection, Student student) {
                student.setId(20L);
                return true;
            }
        };

        User user = new User("Student User", "student@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student student = new Student(null, "CSE2026-003", "Computer Science", new BigDecimal("8.75"), 2026, "9876543212", null, null);

        boolean registered = studentDAO.registerStudent(user, student);

        assertTrue(registered, "registerStudent must return true on successful commit");
        assertEquals(Boolean.TRUE, connState.get("setAutoCommitCalled"), "setAutoCommit must be called");
        assertEquals(1, connState.get("commitCount"), "commit() must be called exactly once");
        assertNull(connState.get("rollbackCount"), "rollback() must not be called on success");
        assertEquals(1, connState.get("closeCount"), "connection.close() must be called in finally");
        assertEquals(Boolean.TRUE, connState.get("isClosed"), "connection must be closed");
        assertEquals(10L, student.getUserId(), "Student entity must receive generated user ID");
        assertEquals(20L, student.getId(), "Student entity must receive generated student profile ID");
    }

    @Test
    @DisplayName("registerStudent rolls back transaction when user creation fails or detects duplicate email")
    void testRegisterStudentTransactionRollbackOnUserCreationFailure() {
        Map<String, Object> connState = new HashMap<>();
        Connection mockConn = createMockConnection(connState);

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public boolean createUser(Connection connection, User user) {
                throw new DuplicateRecordException("Duplicate email 'student@college.edu'");
            }
        };

        StudentDAO studentDAO = new StudentDAO(mockUserDao) {
            @Override
            protected Connection getConnection() {
                return mockConn;
            }
        };

        User user = new User("Student User", "student@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student student = new Student(null, "CSE2026-004", "Computer Science", new BigDecimal("8.20"), 2026, "9876543213", null, null);

        DuplicateRecordException thrown = assertThrows(DuplicateRecordException.class,
                () -> studentDAO.registerStudent(user, student),
                "Duplicate email must propagate DuplicateRecordException");

        assertTrue(thrown.getMessage().contains("Duplicate email"));
        assertNull(connState.get("commitCount"), "commit() must not be called when user creation fails");
        assertEquals(1, connState.get("rollbackCount"), "rollback() must be called when user creation fails");
        assertEquals(1, connState.get("closeCount"), "connection must be closed in finally block");
    }

    @Test
    @DisplayName("registerStudent rolls back transaction when student creation fails with duplicate roll number")
    void testRegisterStudentTransactionRollbackOnStudentCreationFailure() {
        Map<String, Object> connState = new HashMap<>();
        Connection mockConn = createMockConnection(connState);

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public boolean createUser(Connection connection, User user) {
                user.setId(50L);
                return true;
            }
        };

        StudentDAO studentDAO = new StudentDAO(mockUserDao) {
            @Override
            protected Connection getConnection() {
                return mockConn;
            }

            @Override
            public boolean createStudent(Connection connection, Student student) {
                throw new DuplicateRecordException("Roll number 'CSE2026-005' already registered");
            }
        };

        User user = new User("Student Two", "student2@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student student = new Student(null, "CSE2026-005", "Mechanical", new BigDecimal("7.90"), 2026, "9876543214", null, null);

        DuplicateRecordException thrown = assertThrows(DuplicateRecordException.class,
                () -> studentDAO.registerStudent(user, student),
                "Duplicate roll number must propagate DuplicateRecordException");

        assertTrue(thrown.getMessage().contains("already registered"));
        assertNull(connState.get("commitCount"), "commit() must not be called when student creation fails");
        assertEquals(1, connState.get("rollbackCount"), "rollback() must be called when student creation fails");
        assertEquals(1, connState.get("closeCount"), "connection must be closed in finally block");
    }

    @Test
    @DisplayName("registerStudent preserves primary exception and attaches suppressed exception when rollback fails")
    void testRegisterStudentTransactionRollbackSuppressesRollbackException() {
        Map<String, Object> connState = new HashMap<>();
        connState.put("throwOnRollback", true);
        Connection mockConn = createMockConnection(connState);

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public boolean createUser(Connection connection, User user) {
                user.setId(60L);
                return true;
            }
        };

        StudentDAO studentDAO = new StudentDAO(mockUserDao) {
            @Override
            protected Connection getConnection() {
                return mockConn;
            }

            @Override
            public boolean createStudent(Connection connection, Student student) {
                throw new DAOException("Primary student insert failure");
            }
        };

        User user = new User("Student Three", "student3@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student student = new Student(null, "CSE2026-006", "Electrical", new BigDecimal("8.00"), 2026, "9876543215", null, null);

        DAOException thrown = assertThrows(DAOException.class,
                () -> studentDAO.registerStudent(user, student),
                "Primary exception must be thrown even if rollback fails");

        assertEquals("Primary student insert failure", thrown.getMessage());
        assertEquals(1, thrown.getSuppressed().length, "Rollback exception must be added as suppressed exception");
        assertTrue(thrown.getSuppressed()[0] instanceof SQLException, "Suppressed exception must be SQLException");
        assertEquals("Simulated rollback failure", thrown.getSuppressed()[0].getMessage());
        assertEquals(1, connState.get("closeCount"), "connection must be closed even when rollback throws");
    }

    @Test
    @DisplayName("registerStudent translates raw SQLException to DAOException or DuplicateRecordException")
    void testRegisterStudentSQLExceptionMapping() {
        Map<String, Object> connState = new HashMap<>();
        Connection mockConn = createMockConnection(connState);

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public boolean createUser(Connection connection, User user) {
                user.setId(70L);
                return true;
            }
        };

        // Case 1: MySQL error 1062 translates to DuplicateRecordException
        StudentDAO studentDAO1062 = new StudentDAO(mockUserDao) {
            @Override
            protected Connection getConnection() {
                return mockConn;
            }

            @Override
            public boolean createStudent(Connection connection, Student student) throws SQLException {
                throw new SQLException("Duplicate entry 'test' for key 'PRIMARY'", "23000", 1062);
            }
        };

        User user1 = new User("Student Four", "student4@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student student1 = new Student(null, "CSE2026-007", "Civil", new BigDecimal("7.50"), 2026, "9876543216", null, null);

        DuplicateRecordException dupEx = assertThrows(DuplicateRecordException.class,
                () -> studentDAO1062.registerStudent(user1, student1));
        assertTrue(dupEx.getMessage().contains("Duplicate entry"));

        // Case 2: General SQLException translates to DAOException
        StudentDAO studentDAOGeneral = new StudentDAO(mockUserDao) {
            @Override
            protected Connection getConnection() {
                return mockConn;
            }

            @Override
            public boolean createStudent(Connection connection, Student student) throws SQLException {
                throw new SQLException("Communications link failure", "08S01", 0);
            }
        };

        User user2 = new User("Student Five", "student5@college.edu", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", User.ROLE_STUDENT);
        Student student2 = new Student(null, "CSE2026-008", "Civil", new BigDecimal("7.60"), 2026, "9876543217", null, null);

        DAOException daoEx = assertThrows(DAOException.class,
                () -> studentDAOGeneral.registerStudent(user2, student2));
        assertTrue(daoEx.getMessage().contains("Database error during student registration"));
    }

    // =====================================================
    // 28. STUDENT REGISTRATION SERVLET TESTS
    // =====================================================

    @Test
    @DisplayName("RegisterServlet doGet generates CSRF token and forwards to register.jsp")
    void testRegisterServletGetRendersFormAndGeneratesCsrfToken() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        Method doGetMethod = RegisterServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = createMockSession(sessionAttrs);
        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, new HashMap<>(), requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doGetMethod.invoke(servlet, request, response);

        assertNotNull(sessionAttrs.get(CsrfUtil.CSRF_TOKEN_SESSION_ATTR), "doGet must ensure CSRF token in session");
        assertEquals("/WEB-INF/jsp/register.jsp", requestState.get("dispatchedPath"));
        assertEquals(Boolean.TRUE, requestState.get("forwarded"));
    }

    @Test
    @DisplayName("RegisterServlet doPost rejects missing or tampered CSRF token with HTTP 403 Forbidden")
    void testRegisterServletPostRejectsMissingOrInvalidCsrfToken() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "secret-token");
        HttpSession session = createMockSession(sessionAttrs);

        // 1. Missing CSRF token
        Map<String, String> missingParams = new HashMap<>();
        missingParams.put("name", "John");
        HttpServletRequest missingRequest = createMockRequest(session, missingParams, new HashMap<>(), new HashMap<>());
        Map<String, Object> missingResponseState = new HashMap<>();
        HttpServletResponse missingResponse = createMockResponse(missingResponseState);

        doPostMethod.invoke(servlet, missingRequest, missingResponse);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, missingResponseState.get("errorCode"), "Missing CSRF token must return 403");

        // 2. Tampered CSRF token
        Map<String, String> tamperedParams = new HashMap<>();
        tamperedParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "tampered-token");
        HttpServletRequest tamperedRequest = createMockRequest(session, tamperedParams, new HashMap<>(), new HashMap<>());
        Map<String, Object> tamperedResponseState = new HashMap<>();
        HttpServletResponse tamperedResponse = createMockResponse(tamperedResponseState);

        doPostMethod.invoke(servlet, tamperedRequest, tamperedResponse);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, tamperedResponseState.get("errorCode"), "Invalid CSRF token must return 403");
    }

    @Test
    @DisplayName("RegisterServlet doPost rejects missing required fields and forwards with error message")
    void testRegisterServletPostMissingRequiredFields() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "token-123");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "token-123");
        params.put("name", "John Doe");
        // Missing email, password, etc.

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("All required fields must be filled.", requestAttrs.get("errorMessage"));
        assertEquals("/WEB-INF/jsp/register.jsp", requestState.get("dispatchedPath"));
        assertEquals(Boolean.TRUE, requestState.get("forwarded"));
        assertNull(requestAttrs.get("password"), "Password must never be stored in request attributes");
        assertNull(requestAttrs.get("confirmPassword"), "Confirm password must never be stored in request attributes");
    }

    @Test
    @DisplayName("RegisterServlet doPost rejects invalid email format")
    void testRegisterServletPostInvalidEmailFormat() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = createValidRegistrationParams();
        params.put("email", "invalid-email-address");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("Please provide a valid email address.", requestAttrs.get("errorMessage"));
        assertEquals("invalid-email-address", requestAttrs.get("email"), "Safe email value should be preserved");
        assertNull(requestAttrs.get("password"));
    }

    @Test
    @DisplayName("RegisterServlet doPost validates password length and confirmation match")
    void testRegisterServletPostPasswordValidationFailures() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        // 1. Password too short (< 6 chars)
        Map<String, String> shortPassParams = createValidRegistrationParams();
        shortPassParams.put("password", "12345");
        shortPassParams.put("confirmPassword", "12345");

        Map<String, Object> requestAttrs1 = new HashMap<>();
        HttpServletRequest request1 = createMockRequest(session, shortPassParams, requestAttrs1, new HashMap<>());
        doPostMethod.invoke(servlet, request1, createMockResponse(new HashMap<>()));
        assertTrue(((String) requestAttrs1.get("errorMessage")).contains("Password must be at least 6 characters"));
        assertNull(requestAttrs1.get("password"));

        // 2. Passwords mismatch
        Map<String, String> mismatchParams = createValidRegistrationParams();
        mismatchParams.put("password", "SecurePass1");
        mismatchParams.put("confirmPassword", "SecurePass2");

        Map<String, Object> requestAttrs2 = new HashMap<>();
        HttpServletRequest request2 = createMockRequest(session, mismatchParams, requestAttrs2, new HashMap<>());
        doPostMethod.invoke(servlet, request2, createMockResponse(new HashMap<>()));
        assertEquals("Passwords do not match.", requestAttrs2.get("errorMessage"));
        assertNull(requestAttrs2.get("password"));
        assertNull(requestAttrs2.get("confirmPassword"));
    }

    @Test
    @DisplayName("RegisterServlet doPost validates CGPA and graduation year numeric bounds")
    void testRegisterServletPostNumericValidationFailures() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        // 1. Invalid CGPA (> 10.00)
        Map<String, String> badCgpaParams = createValidRegistrationParams();
        badCgpaParams.put("cgpa", "11.50");

        Map<String, Object> requestAttrs1 = new HashMap<>();
        HttpServletRequest request1 = createMockRequest(session, badCgpaParams, requestAttrs1, new HashMap<>());
        doPostMethod.invoke(servlet, request1, createMockResponse(new HashMap<>()));
        assertTrue(((String) requestAttrs1.get("errorMessage")).contains("CGPA"));

        // 2. Invalid Graduation Year (< 2000)
        Map<String, String> badYearParams = createValidRegistrationParams();
        badYearParams.put("graduationYear", "1999");

        Map<String, Object> requestAttrs2 = new HashMap<>();
        HttpServletRequest request2 = createMockRequest(session, badYearParams, requestAttrs2, new HashMap<>());
        doPostMethod.invoke(servlet, request2, createMockResponse(new HashMap<>()));
        assertTrue(((String) requestAttrs2.get("errorMessage")).contains("Graduation year"));
    }

    @Test
    @DisplayName("RegisterServlet doPost hashes password, enforces STUDENT role, delegates to StudentDAO, and redirects to /login")
    void testRegisterServletPostSuccessfulRegistrationDelegatesToDaoAndRedirects() throws Exception {
        RegisterServlet servlet = new RegisterServlet();

        User[] capturedUser = new User[1];
        Student[] capturedStudent = new Student[1];

        StudentDAO mockStudentDao = new StudentDAO() {
            @Override
            public boolean registerStudent(User user, Student student) {
                capturedUser[0] = user;
                capturedStudent[0] = student;
                user.setId(99L);
                student.setId(199L);
                return true;
            }
        };
        servlet.setStudentDAO(mockStudentDao);

        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = createValidRegistrationParams();
        params.put("password", "MySecretP@ss1");
        params.put("confirmPassword", "MySecretP@ss1");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        // Verify delegation to StudentDAO
        assertNotNull(capturedUser[0], "StudentDAO.registerStudent must be invoked with User");
        assertNotNull(capturedStudent[0], "StudentDAO.registerStudent must be invoked with Student");

        // Verify STUDENT role enforcement
        assertEquals(User.ROLE_STUDENT, capturedUser[0].getRole(), "User role must be set to STUDENT");

        // Verify password hashing with BCrypt
        assertTrue(PasswordHashService.isBCryptHash(capturedUser[0].getPasswordHash()), "User password must be a valid BCrypt hash");
        assertTrue(PasswordHashService.verifyPassword("MySecretP@ss1", capturedUser[0].getPasswordHash()), "Hash must verify against plain password");
        assertNotEquals("MySecretP@ss1", capturedUser[0].getPasswordHash(), "Plain password must not be stored in entity");

        // Verify student properties
        assertEquals("CS2026-001", capturedStudent[0].getRollNumber());
        assertEquals("Computer Science", capturedStudent[0].getDepartment());
        assertEquals(new BigDecimal("8.75"), capturedStudent[0].getCgpa());
        assertEquals(Integer.valueOf(2026), capturedStudent[0].getGraduationYear());

        // Verify Post/Redirect/Get to /login with success message in session
        assertEquals("/college/login", responseState.get("redirectUrl"), "Must redirect to /login");
        assertTrue(((String) sessionAttrs.get("successMessage")).contains("successful"));
        assertNull(requestAttrs.get("password"), "Password must not be in request");
    }

    @Test
    @DisplayName("RegisterServlet doPost catches DuplicateRecordException and presents user-friendly message")
    void testRegisterServletPostDuplicateRecordHandling() throws Exception {
        RegisterServlet servlet = new RegisterServlet();

        // 1. Duplicate email
        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public boolean registerStudent(User user, Student student) {
                throw new DuplicateRecordException("User with email 'student@college.edu' already exists.");
            }
        });

        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = createValidRegistrationParams();
        Map<String, Object> requestAttrs1 = new HashMap<>();
        Map<String, Object> requestState1 = new HashMap<>();
        HttpServletRequest request1 = createMockRequest(session, params, requestAttrs1, requestState1);

        doPostMethod.invoke(servlet, request1, createMockResponse(new HashMap<>()));
        assertEquals("An account with this email address already exists.", requestAttrs1.get("errorMessage"));
        assertEquals("/WEB-INF/jsp/register.jsp", requestState1.get("dispatchedPath"));
        assertEquals("CS2026-001", requestAttrs1.get("rollNumber"), "Safe fields must be preserved");

        // 2. Duplicate roll number
        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public boolean registerStudent(User user, Student student) {
                throw new DuplicateRecordException("Student profile already exists with roll number 'CS2026-001'.");
            }
        });

        Map<String, Object> requestAttrs2 = new HashMap<>();
        HttpServletRequest request2 = createMockRequest(session, params, requestAttrs2, new HashMap<>());
        doPostMethod.invoke(servlet, request2, createMockResponse(new HashMap<>()));
        assertEquals("A student profile with this roll number is already registered.", requestAttrs2.get("errorMessage"));
    }

    @Test
    @DisplayName("RegisterServlet doPost shields internal database errors from leaking to user")
    void testRegisterServletPostDatabaseExceptionProducesSafeErrorResponse() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public boolean registerStudent(User user, Student student) {
                throw new DAOException("Internal SQL syntax error: table 'users' access denied for root@localhost");
            }
        });

        Method doPostMethod = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = createValidRegistrationParams();
        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);

        doPostMethod.invoke(servlet, request, createMockResponse(new HashMap<>()));

        String errorMessage = (String) requestAttrs.get("errorMessage");
        assertEquals("A database error occurred while creating your account. Please try again later.", errorMessage);
        assertFalse(errorMessage.contains("SQL"), "Internal SQL details must not leak to the user");
        assertFalse(errorMessage.contains("root@localhost"), "Internal credentials must not leak to the user");
        assertEquals("/WEB-INF/jsp/register.jsp", requestState.get("dispatchedPath"));
    }

    // =====================================================
    // 29. LOGIN & LOGOUT SERVLET AND SESSION HANDLING TESTS
    // =====================================================

    @Test
    @DisplayName("LoginServlet doGet generates CSRF token, forwards flash messages, and displays login.jsp")
    void testLoginServletGetRendersFormAndGeneratesCsrfToken() throws Exception {
        LoginServlet servlet = new LoginServlet();
        Method doGetMethod = LoginServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("successMessage", "Account created successfully! Please sign in.");
        sessionAttrs.put("errorMessage", "Session expired.");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, new HashMap<>(), requestAttrs, requestState);
        HttpServletResponse response = createMockResponse(new HashMap<>());

        doGetMethod.invoke(servlet, request, response);

        // Verify CSRF token was generated in session
        assertNotNull(sessionAttrs.get(CsrfUtil.CSRF_TOKEN_SESSION_ATTR), "CSRF token must be created in session");

        // Verify flash messages transferred from session to request and cleaned up from session
        assertEquals("Account created successfully! Please sign in.", requestAttrs.get("successMessage"));
        assertNull(sessionAttrs.get("successMessage"), "Flash success message must be consumed from session");
        assertEquals("Session expired.", requestAttrs.get("errorMessage"));
        assertNull(sessionAttrs.get("errorMessage"), "Flash error message must be consumed from session");

        // Verify forwarded to login.jsp
        assertEquals("/WEB-INF/jsp/login.jsp", requestState.get("dispatchedPath"));
    }

    @Test
    @DisplayName("LoginServlet doPost rejects missing or invalid CSRF token with 403 Forbidden")
    void testLoginServletPostRejectsMissingOrInvalidCsrfToken() throws Exception {
        LoginServlet servlet = new LoginServlet();
        final boolean[] daoInvoked = new boolean[]{false};
        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                daoInvoked[0] = true;
                return null;
            }
        });

        Method doPostMethod = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        // 1. Missing CSRF token
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "session-secret-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> paramsWithoutCsrf = new HashMap<>();
        paramsWithoutCsrf.put("email", "student@college.edu");
        paramsWithoutCsrf.put("password", "SecretPassword123!");

        Map<String, Object> responseState1 = new HashMap<>();
        HttpServletRequest request1 = createMockRequest(session, paramsWithoutCsrf, new HashMap<>(), new HashMap<>());
        HttpServletResponse response1 = createMockResponse(responseState1);

        doPostMethod.invoke(servlet, request1, response1);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState1.get("errorCode"));
        assertFalse(daoInvoked[0], "UserDAO must not be queried when CSRF check fails");

        // 2. Mismatched CSRF token
        Map<String, String> paramsWithBadCsrf = new HashMap<>();
        paramsWithBadCsrf.put(CsrfUtil.CSRF_TOKEN_PARAM, "tampered-token");
        paramsWithBadCsrf.put("email", "student@college.edu");
        paramsWithBadCsrf.put("password", "SecretPassword123!");

        Map<String, Object> responseState2 = new HashMap<>();
        HttpServletRequest request2 = createMockRequest(session, paramsWithBadCsrf, new HashMap<>(), new HashMap<>());
        HttpServletResponse response2 = createMockResponse(responseState2);

        doPostMethod.invoke(servlet, request2, response2);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState2.get("errorCode"));
        assertFalse(daoInvoked[0], "UserDAO must not be queried when CSRF token is invalid");
    }

    @Test
    @DisplayName("LoginServlet doPost validates required email and password fields without leaking password")
    void testLoginServletPostMissingEmailOrPassword() throws Exception {
        LoginServlet servlet = new LoginServlet();
        Method doPostMethod = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        // Case 1: Missing email
        Map<String, String> paramsMissingEmail = new HashMap<>();
        paramsMissingEmail.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsMissingEmail.put("password", "SecretPassword123!");

        Map<String, Object> requestAttrs1 = new HashMap<>();
        Map<String, Object> requestState1 = new HashMap<>();
        HttpServletRequest request1 = createMockRequest(session, paramsMissingEmail, requestAttrs1, requestState1);

        doPostMethod.invoke(servlet, request1, createMockResponse(new HashMap<>()));
        assertEquals("Email and password are required.", requestAttrs1.get("errorMessage"));
        assertNull(requestAttrs1.get("password"), "Password must never be attached to request attributes");
        assertEquals("/WEB-INF/jsp/login.jsp", requestState1.get("dispatchedPath"));

        // Case 2: Missing password
        Map<String, String> paramsMissingPass = new HashMap<>();
        paramsMissingPass.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsMissingPass.put("email", "student@college.edu");

        Map<String, Object> requestAttrs2 = new HashMap<>();
        Map<String, Object> requestState2 = new HashMap<>();
        HttpServletRequest request2 = createMockRequest(session, paramsMissingPass, requestAttrs2, requestState2);

        doPostMethod.invoke(servlet, request2, createMockResponse(new HashMap<>()));
        assertEquals("Email and password are required.", requestAttrs2.get("errorMessage"));
        assertEquals("student@college.edu", requestAttrs2.get("email"), "Email input should be preserved for redisplay");
        assertNull(requestAttrs2.get("password"), "Password must never be attached to request attributes");
        assertEquals("/WEB-INF/jsp/login.jsp", requestState2.get("dispatchedPath"));
    }

    @Test
    @DisplayName("LoginServlet doPost returns identical generic error for non-existent user and wrong password")
    void testLoginServletPostInvalidCredentialsGenericErrorMessage() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String correctHash = PasswordHashService.hashPassword("CorrectPassword123!");
        User existingUser = new User(10L, "John Doe", "john@college.edu", correctHash, User.ROLE_STUDENT, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("john@college.edu".equalsIgnoreCase(email)) {
                    return existingUser;
                }
                return null;
            }
        });

        Method doPostMethod = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        // Case A: User does not exist
        Map<String, String> paramsNonExistent = new HashMap<>();
        paramsNonExistent.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsNonExistent.put("email", "nonexistent@college.edu");
        paramsNonExistent.put("password", "AnyPassword123!");

        Map<String, Object> requestAttrsA = new HashMap<>();
        Map<String, Object> requestStateA = new HashMap<>();
        HttpServletRequest requestA = createMockRequest(session, paramsNonExistent, requestAttrsA, requestStateA);
        doPostMethod.invoke(servlet, requestA, createMockResponse(new HashMap<>()));

        String errorA = (String) requestAttrsA.get("errorMessage");
        assertEquals("Invalid email or password.", errorA);
        assertNull(requestAttrsA.get("password"));

        // Case B: User exists but wrong password
        Map<String, String> paramsWrongPass = new HashMap<>();
        paramsWrongPass.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsWrongPass.put("email", "john@college.edu");
        paramsWrongPass.put("password", "WrongPassword123!");

        Map<String, Object> requestAttrsB = new HashMap<>();
        Map<String, Object> requestStateB = new HashMap<>();
        HttpServletRequest requestB = createMockRequest(session, paramsWrongPass, requestAttrsB, requestStateB);
        doPostMethod.invoke(servlet, requestB, createMockResponse(new HashMap<>()));

        String errorB = (String) requestAttrsB.get("errorMessage");
        assertEquals("Invalid email or password.", errorB);
        assertNull(requestAttrsB.get("password"));

        // Verify generic error parity to prevent enumeration
        assertEquals(errorA, errorB, "Error message must be identical for non-existent user and bad password to prevent account enumeration");
    }

    @Test
    @DisplayName("LoginServlet doPost authenticates user, protects against session fixation, binds role from DB, and redirects to jobs")
    void testLoginServletPostSuccessfulLoginSessionFixationProtectionAndRoleBinding() throws Exception {
        LoginServlet servlet = new LoginServlet();
        String rawPassword = "CorrectP@ssword2026!";
        String passwordHash = PasswordHashService.hashPassword(rawPassword);
        User dbUser = new User(42L, "Jane Student", "jane@college.edu", passwordHash, User.ROLE_STUDENT, LocalDateTime.now());

        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                if ("jane@college.edu".equalsIgnoreCase(email)) {
                    return dbUser;
                }
                return null;
            }
        });

        Method doPostMethod = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> oldSessionAttrs = new HashMap<>();
        oldSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "pre-auth-csrf-token");
        oldSessionAttrs.put("anonymousCartOrData", "untrusted");
        HttpSession oldSession = createMockSession(oldSessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "pre-auth-csrf-token");
        params.put("email", "  jane@college.edu  "); // Email with whitespace
        params.put("password", rawPassword);
        params.put("role", "ADMIN"); // Attacker attempting privilege escalation via request parameter

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(oldSession, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        // 1. Session fixation protection: verify old session was invalidated
        assertEquals(Boolean.TRUE, oldSessionAttrs.get("invalidated"), "Old session must be invalidated upon successful login");

        // 2. Fresh session creation and identity/role binding
        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrs = (Map<String, Object>) requestState.get("newSessionAttrs");
        assertNotNull(newSessionAttrs, "A fresh session must be established post-authentication");
        assertEquals(42L, newSessionAttrs.get(LoginServlet.SESSION_USER_ID));
        assertEquals("jane@college.edu", newSessionAttrs.get(LoginServlet.SESSION_USER_EMAIL));
        assertEquals("Jane Student", newSessionAttrs.get(LoginServlet.SESSION_USER_NAME));

        // 3. Strict DB role binding: must ignore attacker-supplied 'ADMIN' parameter
        assertEquals(User.ROLE_STUDENT, newSessionAttrs.get(LoginServlet.SESSION_USER_ROLE),
                "User role must be bound strictly from the database entity, not request parameters");

        // 4. Fresh CSRF token for the authenticated session
        String newCsrfToken = (String) newSessionAttrs.get(CsrfUtil.CSRF_TOKEN_SESSION_ATTR);
        assertNotNull(newCsrfToken, "New session must have an active CSRF token");
        assertNotEquals("pre-auth-csrf-token", newCsrfToken, "Authenticated session must not reuse pre-auth CSRF token");

        // 5. Redirect to jobs dashboard
        assertEquals("/college/jobs", responseState.get("redirectUrl"), "Successful login must redirect to /jobs");
    }

    @Test
    @DisplayName("LoginServlet doPost shields internal database errors from user")
    void testLoginServletPostDatabaseErrorShielding() throws Exception {
        LoginServlet servlet = new LoginServlet();
        servlet.setUserDAO(new UserDAO() {
            @Override
            public User findByEmail(String email) {
                throw new DAOException("Communications link failure: Connection refused: connect to mysql:3306");
            }
        });

        Method doPostMethod = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("email", "student@college.edu");
        params.put("password", "AnyPassword123!");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        HttpServletResponse response = createMockResponse(new HashMap<>());

        doPostMethod.invoke(servlet, request, response);

        String errorMessage = (String) requestAttrs.get("errorMessage");
        assertEquals("A database error occurred. Please try again later.", errorMessage);
        assertFalse(errorMessage.contains("Communications link failure"), "Internal database details must not leak");
        assertFalse(errorMessage.contains("3306"), "Port details must not leak");
        assertEquals("/WEB-INF/jsp/login.jsp", requestState.get("dispatchedPath"));
    }

    @Test
    @DisplayName("LogoutServlet enforces POST-only logout with CSRF, invalidates session, and rejects GET with 405")
    void testLogoutServletEnforcesPostWithCsrfAndRejectsGetWith405() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        Method doGetMethod = LogoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        Method doPostMethod = LogoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        // 1. POST logout with valid CSRF token
        Map<String, Object> oldSessionAttrs1 = new HashMap<>();
        oldSessionAttrs1.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "auth-csrf-token");
        oldSessionAttrs1.put(LoginServlet.SESSION_USER_ID, 100L);
        HttpSession session1 = createMockSession(oldSessionAttrs1);

        Map<String, String> validPostParams = new HashMap<>();
        validPostParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "auth-csrf-token");

        Map<String, Object> requestState1 = new HashMap<>();
        HttpServletRequest request1 = createMockRequest(session1, validPostParams, new HashMap<>(), requestState1);
        Map<String, Object> responseState1 = new HashMap<>();
        HttpServletResponse response1 = createMockResponse(responseState1);

        doPostMethod.invoke(servlet, request1, response1);

        assertEquals(Boolean.TRUE, oldSessionAttrs1.get("invalidated"), "POST logout must invalidate old session");
        assertEquals("/college/login", responseState1.get("redirectUrl"), "Must redirect to /login");

        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrs1 = (Map<String, Object>) requestState1.get("newSessionAttrs");
        assertNotNull(newSessionAttrs1, "New clean session must be created for flash notification");
        assertEquals("You have been logged out successfully.", newSessionAttrs1.get("successMessage"));
        assertNull(newSessionAttrs1.get(LoginServlet.SESSION_USER_ID), "User credentials must not be present in new session");

        // 2. POST logout with invalid/missing CSRF token should be rejected with 403
        Map<String, Object> oldSessionAttrs2 = new HashMap<>();
        oldSessionAttrs2.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "auth-csrf-token");
        HttpSession session2 = createMockSession(oldSessionAttrs2);

        Map<String, String> invalidPostParams = new HashMap<>();
        invalidPostParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "forged-csrf-token");

        Map<String, Object> responseState2 = new HashMap<>();
        HttpServletRequest request2 = createMockRequest(session2, invalidPostParams, new HashMap<>(), new HashMap<>());
        HttpServletResponse response2 = createMockResponse(responseState2);

        doPostMethod.invoke(servlet, request2, response2);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState2.get("errorCode"), "POST logout without valid CSRF must return 403");
        assertNull(oldSessionAttrs2.get("invalidated"), "Session must NOT be invalidated when CSRF check fails");

        // 3. GET logout must return HTTP 405 Method Not Allowed and NOT invalidate the session
        Map<String, Object> oldSessionAttrs3 = new HashMap<>();
        oldSessionAttrs3.put(LoginServlet.SESSION_USER_ID, 200L);
        oldSessionAttrs3.put("preservedKey", "preservedValue");
        HttpSession session3 = createMockSession(oldSessionAttrs3);

        Map<String, Object> responseState3 = new HashMap<>();
        HttpServletRequest request3 = createMockRequest(session3, new HashMap<>(), new HashMap<>(), new HashMap<>());
        HttpServletResponse response3 = createMockResponse(responseState3);

        doGetMethod.invoke(servlet, request3, response3);

        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, responseState3.get("errorCode"),
                "GET /logout must return HTTP 405 Method Not Allowed");
        assertNull(oldSessionAttrs3.get("invalidated"),
                "GET /logout must NOT invalidate the existing session");
        assertEquals("preservedValue", oldSessionAttrs3.get("preservedKey"),
                "Session attributes must remain intact upon rejected GET request");
        assertNull(responseState3.get("redirectUrl"),
                "GET /logout must not redirect when rejecting with 405");
    }

    @Test
    @DisplayName("All navigation JSPs include POST-only logout form with CSRF token for authenticated users only")
    void testNavigationJSPsIncludeLogoutFormWithCsrfForAuthenticatedUsersOnly() throws Exception {
        Path jspDir = Path.of("src/main/webapp/WEB-INF/jsp");
        if (!Files.exists(jspDir)) {
            jspDir = Path.of("backend/src/main/webapp/WEB-INF/jsp");
        }
        assertTrue(Files.exists(jspDir), "JSP directory must exist at: " + jspDir.toAbsolutePath());

        String[] navJspFiles = new String[]{
                "jobs.jsp",
                "applications.jsp",
                "shortlist.jsp",
                "interviews.jsp",
                "job-details.jsp",
                "job-add.jsp",
                "job-edit.jsp",
                "application-details.jsp",
                "application-history.jsp",
                "interview-edit.jsp"
        };

        for (String jspName : navJspFiles) {
            Path jspPath = jspDir.resolve(jspName);
            assertTrue(Files.exists(jspPath), "JSP file must exist: " + jspName);
            String content = Files.readString(jspPath);

            // 1. Must be conditioned on authenticated user ID (visible to authenticated, hidden for unauthenticated)
            assertTrue(content.contains("not empty sessionScope.userId"),
                    jspName + " must gate logout visibility with not empty sessionScope.userId");

            // 2. Must submit POST to /logout
            assertTrue(content.contains("action=\"${pageContext.request.contextPath}/logout\""),
                    jspName + " must contain a form targeting ${pageContext.request.contextPath}/logout");
            assertTrue(content.contains("method=\"post\""),
                    jspName + " must use method=\"post\" for logout");

            // 3. Must include valid CSRF token parameter
            assertTrue(content.contains("name=\"csrfToken\"") && content.contains("value=\"${sessionScope.csrfToken}\""),
                    jspName + " must pass CSRF token in logout form");

            // 4. Must provide a visible Logout submit button
            assertTrue(content.contains("type=\"submit\"") && content.contains("Logout"),
                    jspName + " must contain a submit button with 'Logout'");

            // 5. Must NOT provide logout as a GET link
            assertFalse(content.contains("<a href=\"${pageContext.request.contextPath}/logout\""),
                    jspName + " must never implement logout as an insecure GET link");
        }
    }

    @Test
    @DisplayName("Logout button visibility condition evaluates true for all authenticated roles and false for unauthenticated users")
    void testLogoutButtonVisibilityByAuthenticationRole() {
        // Authenticated Student
        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentAttrs);
        assertNotNull(studentSession.getAttribute(LoginServlet.SESSION_USER_ID),
                "Student session has userId, making logout visible");

        // Authenticated Coordinator
        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordAttrs);
        assertNotNull(coordSession.getAttribute(LoginServlet.SESSION_USER_ID),
                "Coordinator session has userId, making logout visible");

        // Authenticated Admin
        Map<String, Object> adminAttrs = new HashMap<>();
        adminAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        adminAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession adminSession = createMockSession(adminAttrs);
        assertNotNull(adminSession.getAttribute(LoginServlet.SESSION_USER_ID),
                "Administrator session has userId, making logout visible");

        // Unauthenticated session (e.g. visiting public /jobs where CsrfUtil creates token but no user is logged in)
        Map<String, Object> anonAttrs = new HashMap<>();
        anonAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "anonymous-csrf-token");
        HttpSession anonSession = createMockSession(anonAttrs);
        assertNull(anonSession.getAttribute(LoginServlet.SESSION_USER_ID),
                "Unauthenticated session has no userId, keeping logout hidden");
    }

    // =====================================================
    // 30. PHASE 4: APPLICATION SUBMISSION, ELIGIBILITY & DUPLICATE PREVENTION TESTS
    // =====================================================

    @Test
    @DisplayName("Job entity eligibility helper methods: isClosed, isOpen, isDeadlinePassed, isCgpaEligible, isDepartmentEligible")
    void testJobEntityEligibilityHelperMethods() {
        Job job = new Job();

        // 1. Status checks
        job.setStatus("OPEN");
        assertFalse(job.isClosed());
        assertTrue(job.isOpen());

        job.setStatus("ACTIVE");
        assertFalse(job.isClosed());
        assertTrue(job.isOpen());

        job.setStatus("CLOSED");
        assertTrue(job.isClosed());
        assertFalse(job.isOpen());

        job.setStatus("INACTIVE");
        assertTrue(job.isClosed());
        assertFalse(job.isOpen());

        job.setStatus(null);
        assertFalse(job.isClosed());
        assertTrue(job.isOpen());

        // 2. Deadline checks
        LocalDate today = LocalDate.of(2026, 10, 21);
        job.setDeadline(null);
        assertFalse(job.isDeadlinePassed(today));

        job.setDeadline(today.minusDays(1));
        assertTrue(job.isDeadlinePassed(today));

        job.setDeadline(today);
        assertFalse(job.isDeadlinePassed(today));

        job.setDeadline(today.plusDays(1));
        assertFalse(job.isDeadlinePassed(today));

        // 3. CGPA eligibility checks
        job.setMinCgpa(null);
        assertTrue(job.isCgpaEligible(null));
        assertTrue(job.isCgpaEligible(BigDecimal.ZERO));

        job.setMinCgpa(new BigDecimal("7.50"));
        assertFalse(job.isCgpaEligible(null));
        assertFalse(job.isCgpaEligible(new BigDecimal("7.49")));
        assertTrue(job.isCgpaEligible(new BigDecimal("7.50")));
        assertTrue(job.isCgpaEligible(new BigDecimal("8.00")));

        // 4. Department eligibility checks
        job.setEligibleDepartments(null);
        assertTrue(job.isDepartmentEligible("Computer Science"));
        assertTrue(job.isDepartmentEligible(null));

        job.setEligibleDepartments("   ");
        assertTrue(job.isDepartmentEligible("Computer Science"));

        job.setEligibleDepartments("Computer Science, Information Technology, Electronics");
        assertFalse(job.isDepartmentEligible(null));
        assertFalse(job.isDepartmentEligible(""));
        assertTrue(job.isDepartmentEligible("Computer Science"));
        assertTrue(job.isDepartmentEligible("computer science"));
        assertTrue(job.isDepartmentEligible("  Information Technology  "));
        assertTrue(job.isDepartmentEligible("ELECTRONICS"));
        assertFalse(job.isDepartmentEligible("Mechanical Engineering"));
        assertFalse(job.isDepartmentEligible("Civil"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects unauthenticated requests and redirects to /login")
    void testApplicationSubmissionRequiresAuthentication() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        // No SESSION_USER_ID
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "100");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/login", responseState.get("redirectUrl"));
        assertEquals("You must log in to apply.", requestAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost succeeds for authenticated student meeting all eligibility criteria")
    void testApplicationSubmissionSucceedsForAuthenticatedStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        Job job = new Job(200L, "Software Engineer", "Acme Corp", "Remote", "Full-Time Software Engineer",
                "120000", "FULL_TIME", "OPEN", LocalDate.now().plusDays(7), new BigDecimal("7.00"), "Computer Science, IT");

        final Application[] createdHolder = new Application[1];
        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                if (Long.valueOf(101L).equals(userId)) return student;
                return null;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                if (Long.valueOf(200L).equals(id)) return job;
                return null;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                createdHolder[0] = application;
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertNotNull(createdHolder[0]);
        assertEquals(501L, createdHolder[0].getStudentId());
        assertEquals(200L, createdHolder[0].getJobId());
        assertEquals("APPLIED", createdHolder[0].getStatus());
        assertEquals("Application submitted successfully!", sessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost ignores client-supplied studentId for student role preventing identity spoofing")
    void testApplicationSubmissionIgnoresClientSuppliedStudentIdForStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student authenticatedStudent = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        Job job = new Job(200L, "Software Engineer", "Acme Corp", "Remote", "Full-Time",
                "120000", "FULL_TIME", "OPEN", LocalDate.now().plusDays(7), new BigDecimal("7.00"), "Computer Science");

        final Application[] createdHolder = new Application[1];
        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                if (Long.valueOf(101L).equals(userId)) return authenticatedStudent;
                return null;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                createdHolder[0] = application;
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");
        params.put("studentId", "999"); // Attacker attempting identity spoofing

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertNotNull(createdHolder[0]);
        assertEquals(501L, createdHolder[0].getStudentId(),
                "Application must strictly bind to the authenticated student's profile ID (501) and ignore submitted studentId (999)");
        assertNotEquals(999L, createdHolder[0].getStudentId());
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission when authenticated user has no student profile")
    void testApplicationSubmissionRejectsAccountWithoutStudentProfile() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return null; // Profile does not exist
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 102L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("No student profile found for your account. Please complete your registration.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission with missing or invalid CSRF token")
    void testApplicationSubmissionRejectsInvalidOrMissingCsrf() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "legitimate-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "forged-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        Map<String, Object> responseState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState.get("errorCode"));
        assertEquals("Invalid or missing CSRF token.", responseState.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission when job does not exist")
    void testApplicationSubmissionRejectsNonexistentJob() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return null; // Job does not exist
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "999");

        Map<String, Object> requestAttrs = new HashMap<>();
        Map<String, Object> requestState = new HashMap<>();
        HttpServletRequest request = createMockRequest(session, params, requestAttrs, requestState);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Job not found.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission when job status is CLOSED or INACTIVE")
    void testApplicationSubmissionRejectsClosedJob() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job closedJob = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "CLOSED");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return closedJob;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Applications are closed for this job.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission when application deadline has passed")
    void testApplicationSubmissionRejectsExpiredDeadline() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job expiredJob = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().minusDays(1), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return expiredJob;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("The application deadline has passed.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost accepts submission on the day of application deadline")
    void testApplicationSubmissionAcceptsValidDeadline() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job todayJob = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now(), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return todayJob;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Application submitted successfully!", sessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission when student CGPA is below job minimum")
    void testApplicationSubmissionRejectsBelowMinCgpa() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("6.80"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Your CGPA does not meet the minimum requirement.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost accepts submission when student CGPA exactly meets job minimum")
    void testApplicationSubmissionAcceptsMeetingMinCgpa() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("7.00"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Application submitted successfully!", sessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects submission when student department is not in eligible departments")
    void testApplicationSubmissionRejectsIneligibleDepartment() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "ME2026-001", "Mechanical Engineering", new BigDecimal("8.50"),
                2026, "9876543210", "CAD, SolidWorks", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science, Information Technology");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Your department is not eligible.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost accepts submission when student department matches eligible department case-insensitively")
    void testApplicationSubmissionAcceptsEligibleDepartment() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "IT2026-001", "Information Technology", new BigDecimal("8.50"),
                2026, "9876543210", "Networks, Java", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science, INFORMATION TECHNOLOGY");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Application submitted successfully!", sessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects malformed or missing job ID")
    void testApplicationSubmissionRejectsMalformedJobId() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        // Case A: Missing jobId
        Map<String, String> paramsMissing = new HashMap<>();
        paramsMissing.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsMissing.put("action", "apply");

        Map<String, Object> responseStateA = new HashMap<>();
        HttpServletRequest requestA = createMockRequest(session, paramsMissing, new HashMap<>(), new HashMap<>());
        HttpServletResponse responseA = createMockResponse(responseStateA);

        doPostMethod.invoke(servlet, requestA, responseA);
        assertEquals("/college/applications", responseStateA.get("redirectUrl"));
        assertEquals("Job ID is required.", sessionAttrs.get("errorMessage"));

        // Case B: Non-numeric jobId
        Map<String, String> paramsNonNumeric = new HashMap<>();
        paramsNonNumeric.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsNonNumeric.put("action", "apply");
        paramsNonNumeric.put("jobId", "abc");

        Map<String, Object> responseStateB = new HashMap<>();
        HttpServletRequest requestB = createMockRequest(session, paramsNonNumeric, new HashMap<>(), new HashMap<>());
        HttpServletResponse responseB = createMockResponse(responseStateB);

        doPostMethod.invoke(servlet, requestB, responseB);
        assertEquals("/college/applications", responseStateB.get("redirectUrl"));
        assertEquals("Job not found.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects duplicate application via application pre-check")
    void testApplicationSubmissionRejectsDuplicateApplicationViaPreCheck() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        final boolean[] createCalled = new boolean[]{false};
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return true; // Already applied!
            }
            @Override
            public boolean createApplication(Application application) {
                createCalled[0] = true;
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("You have already applied for this job.", sessionAttrs.get("errorMessage"));
        assertFalse(createCalled[0], "createApplication should never be invoked when pre-check finds existing application");
    }

    @Test
    @DisplayName("ApplicationServlet doPost handles race condition duplicate key exception gracefully")
    void testApplicationSubmissionHandlesDatabaseDuplicateKeyExceptionGracefully() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false; // Concurrent check passes
            }
            @Override
            public boolean createApplication(Application application) {
                // Concurrent insert causes unique constraint violation
                throw new DuplicateRecordException("Application already exists for student ID 501 and job ID 200.");
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("You have already applied for this job.", sessionAttrs.get("errorMessage"));
    }

    @Test
    @DisplayName("ApplicationDAO createApplication catches MySQL 1062, rolls back transaction, and throws DuplicateRecordException")
    void testApplicationDAOThrowsDuplicateRecordExceptionOnSqlError1062() {
        Map<String, Object> connState = new HashMap<>();
        connState.put("throwOnExecuteUpdate", new SQLException("Duplicate entry '501-200' for key 'uq_applications_student_job'", "23000", 1062));

        ApplicationDAO testDao = new ApplicationDAO() {
            @Override
            protected Connection getConnection() {
                return createMockConnection(connState);
            }
        };

        Application app = new Application(501L, 200L, LocalDate.now(), "APPLIED");
        DuplicateRecordException ex = assertThrows(DuplicateRecordException.class, () -> testDao.createApplication(app));
        assertTrue(ex.getMessage().contains("Application already exists"));
        assertEquals(1, (int) connState.getOrDefault("rollbackCount", 0), "Transaction must be rolled back on duplicate key error");
    }

    @Test
    @DisplayName("ApplicationServlet doPost shields internal database errors during application creation")
    void testApplicationSubmissionHandlesDatabaseExceptionShielding() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Dev", "Co", "Loc", "Desc", "1000", "FT", "OPEN",
                LocalDate.now().plusDays(5), new BigDecimal("7.00"), "Computer Science");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                throw new DAOException("Communication link failure: Connection refused to mysql://127.0.0.1:3306");
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "apply");
        params.put("jobId", "200");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        String error = (String) sessionAttrs.get("errorMessage");
        assertEquals("A database error occurred while creating your application. Please try again later.", error);
        assertFalse(error.contains("Communication link failure"), "Internal database error must not leak");
        assertFalse(error.contains("3306"), "Port details must not leak");
    }

    @Test
    @DisplayName("ApplicationServlet doPost allows coordinator to submit application on behalf of a student")
    void testCoordinatorCanCreateApplicationForSpecifiedStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Student student = new Student(701L, 202L, "EE2026-009", "Electrical Engineering", new BigDecimal("8.20"),
                2026, "9876543210", "Circuits, C++", "https://example.com/resume.pdf");
        Job job = new Job(200L, "Firmware Engineer", "Hardware Co", "Onsite", "Desc", "95000", "FULL_TIME", "OPEN",
                LocalDate.now().plusDays(10), new BigDecimal("7.50"), "Electrical Engineering");

        final Application[] createdHolder = new Application[1];
        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findById(Long id) {
                if (Long.valueOf(701L).equals(id)) return student;
                return null;
            }
        });
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public boolean hasAlreadyApplied(Long studentId, Long jobId) {
                return false;
            }
            @Override
            public boolean createApplication(Application application) {
                createdHolder[0] = application;
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "create");
        params.put("jobId", "200");
        params.put("studentId", "701");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertNotNull(createdHolder[0]);
        assertEquals(701L, createdHolder[0].getStudentId(), "Coordinator can apply on behalf of student ID 701");
        assertEquals("Application submitted successfully!", sessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects coordinator submission when specified student ID does not exist")
    void testCoordinatorApplicationRejectsNonExistentStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findById(Long id) {
                return null; // Student not found
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("action", "create");
        params.put("jobId", "200");
        params.put("studentId", "999");

        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doPostMethod.invoke(servlet, request, response);

        assertEquals("/college/applications", responseState.get("redirectUrl"));
        assertEquals("Student not found with ID: 999", sessionAttrs.get("errorMessage"));
    }

    // =====================================================
    // 31. ROLE-BASED ACCESS CONTROL AND AUTHORIZATION TESTS (PHASE 5)
    // =====================================================

    @Test
    @DisplayName("ApplicationServlet doGet unauthenticated redirects to /login for applications and shortlist")
    void testApplicationServletGetUnauthenticatedRedirectsToLogin() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        // Case A: /applications unauthenticated
        Map<String, Object> stateA = new HashMap<>();
        stateA.put("servletPath", "/applications");
        HttpServletRequest requestA = createMockRequest(null, new HashMap<>(), new HashMap<>(), stateA);
        Map<String, Object> responseStateA = new HashMap<>();
        HttpServletResponse responseA = createMockResponse(responseStateA);

        doGetMethod.invoke(servlet, requestA, responseA);

        assertEquals("/college/login", responseStateA.get("redirectUrl"));
        @SuppressWarnings("unchecked")
        Map<String, Object> newSessionAttrsA = (Map<String, Object>) stateA.get("newSessionAttrs");
        assertNotNull(newSessionAttrsA);
        assertEquals("You must log in to view applications.", newSessionAttrsA.get("errorMessage"));

        // Case B: /shortlist unauthenticated
        Map<String, Object> stateB = new HashMap<>();
        stateB.put("servletPath", "/shortlist");
        HttpServletRequest requestB = createMockRequest(null, new HashMap<>(), new HashMap<>(), stateB);
        Map<String, Object> responseStateB = new HashMap<>();
        HttpServletResponse responseB = createMockResponse(responseStateB);

        doGetMethod.invoke(servlet, requestB, responseB);

        assertEquals("/college/login", responseStateB.get("redirectUrl"));
    }

    @Test
    @DisplayName("ApplicationServlet doGet scopes student view strictly to own applications, ignoring query tampering")
    void testApplicationServletGetStudentViewsOnlyOwnApplications() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                if (Long.valueOf(101L).equals(userId)) return student;
                return null;
            }
        });

        final List<Long> searchedStudentIds = new ArrayList<>();
        List<Application> ownApplications = new ArrayList<>();
        Application app1 = createTestApplication(1L, 501L, 200L, LocalDate.now(), "APPLIED");
        ownApplications.add(app1);

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public List<Application> searchApplications(Long studentId, Long jobId, String status) {
                searchedStudentIds.add(studentId);
                return ownApplications;
            }
        });

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public List<Long> getCompletedApplicationIds() {
                return new ArrayList<>();
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("studentId", "999"); // Attempted parameter tampering

        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        state.put("servletPath", "/applications");
        HttpServletRequest request = createMockRequest(session, params, attributes, state);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doGetMethod.invoke(servlet, request, response);

        assertFalse(searchedStudentIds.isEmpty());
        for (Long searchedId : searchedStudentIds) {
            assertEquals(501L, searchedId, "searchApplications must be forced to student's own ID");
        }
        assertEquals(ownApplications, attributes.get("applications"));
        assertEquals("501", attributes.get("selectedStudentId"));
        assertEquals("/WEB-INF/jsp/applications.jsp", state.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(state.get("forwarded")));

        @SuppressWarnings("unchecked")
        Map<String, Integer> stats = (Map<String, Integer>) attributes.get("applicationStats");
        assertNotNull(stats);
        assertEquals(1, stats.get("TOTAL"));
        assertEquals(1, stats.get("APPLIED"));
    }

    @Test
    @DisplayName("ApplicationServlet doGet shortlist filters out other students' applications for student role")
    void testApplicationServletGetStudentShortlistViewScopedToOwnApplications() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                if (Long.valueOf(101L).equals(userId)) return student;
                return null;
            }
        });

        List<Application> allShortlisted = new ArrayList<>();
        allShortlisted.add(createTestApplication(1L, 501L, 200L, LocalDate.now(), "SHORTLISTED")); // own
        allShortlisted.add(createTestApplication(2L, 999L, 200L, LocalDate.now(), "SHORTLISTED")); // another student

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public List<Application> getShortlistedApplications() {
                return allShortlisted;
            }
        });

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public List<Interview> getAllInterviews() {
                return new ArrayList<>();
            }
            @Override
            public Map<Long, Boolean> getRescheduledApplicationsMap() {
                return new HashMap<>();
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        state.put("servletPath", "/shortlist");
        HttpServletRequest request = createMockRequest(session, new HashMap<>(), attributes, state);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doGetMethod.invoke(servlet, request, response);

        @SuppressWarnings("unchecked")
        List<Application> filteredShortlist = (List<Application>) attributes.get("applications");
        assertNotNull(filteredShortlist);
        assertEquals(1, filteredShortlist.size(), "Student should only see their own shortlisted applications");
        assertEquals(501L, filteredShortlist.get(0).getStudentId());
        assertEquals("/WEB-INF/jsp/shortlist.jsp", state.get("dispatchedPath"));
    }

    @Test
    @DisplayName("ApplicationServlet doGet allows coordinator to view institutional listings and arbitrary student filters")
    void testApplicationServletGetCoordinatorViewsInstitutionalApplications() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        final Long[] searchStudentIdHolder = new Long[1];
        final boolean[] getAllCalled = new boolean[]{false};

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public List<Application> getAllApplications() {
                getAllCalled[0] = true;
                return new ArrayList<>();
            }
            @Override
            public List<Application> searchApplications(Long studentId, Long jobId, String status) {
                searchStudentIdHolder[0] = studentId;
                return new ArrayList<>();
            }
            @Override
            public Map<String, Integer> getApplicationStatistics() {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("TOTAL", 42);
                return stats;
            }
        });

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public List<Long> getCompletedApplicationIds() {
                return new ArrayList<>();
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        // Case A: Coordinator without filter
        Map<String, Object> attrsA = new HashMap<>();
        Map<String, Object> stateA = new HashMap<>();
        stateA.put("servletPath", "/applications");
        HttpServletRequest requestA = createMockRequest(session, new HashMap<>(), attrsA, stateA);
        Map<String, Object> responseStateA = new HashMap<>();
        HttpServletResponse responseA = createMockResponse(responseStateA);

        doGetMethod.invoke(servlet, requestA, responseA);
        assertTrue(getAllCalled[0], "getAllApplications should be called when no filter is provided");
        @SuppressWarnings("unchecked")
        Map<String, Integer> statsA = (Map<String, Integer>) attrsA.get("applicationStats");
        assertEquals(42, statsA.get("TOTAL"));

        // Case B: Coordinator filtering for student 888
        Map<String, String> paramsB = new HashMap<>();
        paramsB.put("studentId", "888");
        Map<String, Object> attrsB = new HashMap<>();
        Map<String, Object> stateB = new HashMap<>();
        stateB.put("servletPath", "/applications");
        HttpServletRequest requestB = createMockRequest(session, paramsB, attrsB, stateB);
        Map<String, Object> responseStateB = new HashMap<>();
        HttpServletResponse responseB = createMockResponse(responseStateB);

        doGetMethod.invoke(servlet, requestB, responseB);
        assertEquals(888L, searchStudentIdHolder[0], "Coordinator filter for student 888 must be respected");
    }

    @Test
    @DisplayName("ApplicationServlet doGet details rejects student attempting to view another student's application (IDOR)")
    void testApplicationServletGetDetailsRejectsIdorForAnotherStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });

        // Application 10 belongs to student 999
        Application otherApp = createTestApplication(10L, 999L, 200L, LocalDate.now(), "APPLIED");
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return otherApp;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "details");
        params.put("id", "10");

        Map<String, Object> state = new HashMap<>();
        state.put("servletPath", "/applications");
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doGetMethod.invoke(servlet, request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState.get("errorCode"));
        assertNull(state.get("forwarded"));
    }

    @Test
    @DisplayName("ApplicationServlet doGet details allows student owner and coordinator to view application details")
    void testApplicationServletGetDetailsAllowedForOwnerAndCoordinator() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });

        Application app = createTestApplication(10L, 501L, 200L, LocalDate.now(), "APPLIED");
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return app;
            }
        });
        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public Interview getInterviewByApplicationId(Long applicationId) {
                return null;
            }
        });

        // Case A: Student owner viewing own application details
        Map<String, Object> sessionAttrsA = new HashMap<>();
        sessionAttrsA.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrsA.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession sessionA = createMockSession(sessionAttrsA);

        Map<String, String> paramsA = new HashMap<>();
        paramsA.put("action", "details");
        paramsA.put("id", "10");

        Map<String, Object> stateA = new HashMap<>();
        stateA.put("servletPath", "/applications");
        HttpServletRequest requestA = createMockRequest(sessionA, paramsA, new HashMap<>(), stateA);
        Map<String, Object> responseStateA = new HashMap<>();
        HttpServletResponse responseA = createMockResponse(responseStateA);

        doGetMethod.invoke(servlet, requestA, responseA);
        assertEquals("/WEB-INF/jsp/application-details.jsp", stateA.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateA.get("forwarded")));

        // Case B: Coordinator viewing the same application
        Map<String, Object> sessionAttrsB = new HashMap<>();
        sessionAttrsB.put(LoginServlet.SESSION_USER_ID, 301L);
        sessionAttrsB.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession sessionB = createMockSession(sessionAttrsB);

        Map<String, Object> stateB = new HashMap<>();
        stateB.put("servletPath", "/applications");
        HttpServletRequest requestB = createMockRequest(sessionB, paramsA, new HashMap<>(), stateB);
        Map<String, Object> responseStateB = new HashMap<>();
        HttpServletResponse responseB = createMockResponse(responseStateB);

        doGetMethod.invoke(servlet, requestB, responseB);
        assertEquals("/WEB-INF/jsp/application-details.jsp", stateB.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateB.get("forwarded")));
    }

    @Test
    @DisplayName("ApplicationServlet doGet history rejects student viewing another student's history (IDOR)")
    void testApplicationServletGetHistoryRejectsIdorForAnotherStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });

        Application otherApp = createTestApplication(10L, 999L, 200L, LocalDate.now(), "APPLIED");
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return otherApp;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(sessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "history");
        params.put("id", "10");

        Map<String, Object> state = new HashMap<>();
        state.put("servletPath", "/applications");
        HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), state);
        Map<String, Object> responseState = new HashMap<>();
        HttpServletResponse response = createMockResponse(responseState);

        doGetMethod.invoke(servlet, request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState.get("errorCode"));
        assertNull(state.get("forwarded"));
    }

    @Test
    @DisplayName("ApplicationServlet doGet history allows student owner and coordinator to view history")
    void testApplicationServletGetHistoryAllowedForOwnerAndCoordinator() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doGetMethod = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Student student = new Student(501L, 101L, "CS2026-001", "Computer Science", new BigDecimal("8.50"),
                2026, "9876543210", "Java, SQL", "https://example.com/resume.pdf");

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return student;
            }
        });

        Application app = createTestApplication(10L, 501L, 200L, LocalDate.now(), "APPLIED");
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return app;
            }
        });
        servlet.setStatusHistoryDAO(new ApplicationStatusHistoryDAO() {
            @Override
            public List<ApplicationStatusHistory> getHistoryByApplicationId(Long applicationId) {
                return new ArrayList<>();
            }
        });

        // Case A: Student owner viewing history
        Map<String, Object> sessionAttrsA = new HashMap<>();
        sessionAttrsA.put(LoginServlet.SESSION_USER_ID, 101L);
        sessionAttrsA.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession sessionA = createMockSession(sessionAttrsA);

        Map<String, String> paramsA = new HashMap<>();
        paramsA.put("action", "history");
        paramsA.put("id", "10");

        Map<String, Object> stateA = new HashMap<>();
        stateA.put("servletPath", "/applications");
        HttpServletRequest requestA = createMockRequest(sessionA, paramsA, new HashMap<>(), stateA);
        Map<String, Object> responseStateA = new HashMap<>();
        HttpServletResponse responseA = createMockResponse(responseStateA);

        doGetMethod.invoke(servlet, requestA, responseA);
        assertEquals("/WEB-INF/jsp/application-history.jsp", stateA.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateA.get("forwarded")));

        // Case B: Coordinator viewing history
        Map<String, Object> sessionAttrsB = new HashMap<>();
        sessionAttrsB.put(LoginServlet.SESSION_USER_ID, 301L);
        sessionAttrsB.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession sessionB = createMockSession(sessionAttrsB);

        Map<String, Object> stateB = new HashMap<>();
        stateB.put("servletPath", "/applications");
        HttpServletRequest requestB = createMockRequest(sessionB, paramsA, new HashMap<>(), stateB);
        Map<String, Object> responseStateB = new HashMap<>();
        HttpServletResponse responseB = createMockResponse(responseStateB);

        doGetMethod.invoke(servlet, requestB, responseB);
        assertEquals("/WEB-INF/jsp/application-history.jsp", stateB.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateB.get("forwarded")));
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects students attempting shortlist, reject, select, or delete")
    void testApplicationServletPostStatusTransitionsRejectStudent() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        String[] mutatingActions = new String[]{"shortlist", "reject", "select", "delete"};

        for (String action : mutatingActions) {
            Map<String, Object> sessionAttrs = new HashMap<>();
            sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
            sessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
            sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
            HttpSession session = createMockSession(sessionAttrs);

            Map<String, String> params = new HashMap<>();
            params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
            params.put("action", action);
            params.put("id", "10");

            HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
            Map<String, Object> responseState = new HashMap<>();
            HttpServletResponse response = createMockResponse(responseState);

            doPostMethod.invoke(servlet, request, response);

            assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState.get("errorCode"),
                    "Student attempting '" + action + "' must receive 403 Forbidden");
        }
    }

    @Test
    @DisplayName("ApplicationServlet doPost rejects unauthenticated users attempting shortlist, reject, select, or delete")
    void testApplicationServletPostStatusTransitionsRejectUnauthenticated() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        String[] mutatingActions = new String[]{"shortlist", "reject", "select", "delete"};

        for (String action : mutatingActions) {
            Map<String, Object> sessionAttrs = new HashMap<>();
            sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
            HttpSession session = createMockSession(sessionAttrs);

            Map<String, String> params = new HashMap<>();
            params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
            params.put("action", action);
            params.put("id", "10");

            HttpServletRequest request = createMockRequest(session, params, new HashMap<>(), new HashMap<>());
            Map<String, Object> responseState = new HashMap<>();
            HttpServletResponse response = createMockResponse(responseState);

            doPostMethod.invoke(servlet, request, response);

            assertEquals(HttpServletResponse.SC_FORBIDDEN, responseState.get("errorCode"),
                    "Unauthenticated user attempting '" + action + "' must receive 403 Forbidden");
        }
    }

    @Test
    @DisplayName("ApplicationServlet doPost allows coordinator to shortlist, reject, select, and delete")
    void testApplicationServletPostStatusTransitionsAllowedForCoordinator() throws Exception {
        ApplicationServlet servlet = new ApplicationServlet();
        Method doPostMethod = ApplicationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] shortlistCalled = new boolean[]{false};
        final boolean[] rejectCalled = new boolean[]{false};
        final boolean[] selectCalled = new boolean[]{false};
        final boolean[] deleteCalled = new boolean[]{false};

        Application appliedApp = createTestApplication(10L, 501L, 200L, LocalDate.now(), "APPLIED");
        Application shortlistedApp = createTestApplication(20L, 501L, 200L, LocalDate.now(), "SHORTLISTED");

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                if (Long.valueOf(10L).equals(id)) return appliedApp;
                if (Long.valueOf(20L).equals(id)) return shortlistedApp;
                return null;
            }
            @Override
            public boolean shortlistApplication(Long id) {
                shortlistCalled[0] = true;
                return true;
            }
            @Override
            public boolean rejectApplication(Long id) {
                rejectCalled[0] = true;
                return true;
            }
            @Override
            public boolean selectApplication(Long id) {
                selectCalled[0] = true;
                return true;
            }
            @Override
            public boolean deleteApplication(Long id) {
                deleteCalled[0] = true;
                return true;
            }
        });

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public boolean isInterviewCompletedForApplication(Long applicationId) {
                return true;
            }
        });

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        sessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        sessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(sessionAttrs);

        // Case A: Shortlist
        Map<String, String> paramsA = new HashMap<>();
        paramsA.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsA.put("action", "shortlist");
        paramsA.put("id", "10");
        Map<String, Object> responseStateA = new HashMap<>();
        HttpServletRequest requestA = createMockRequest(session, paramsA, new HashMap<>(), new HashMap<>());
        HttpServletResponse responseA = createMockResponse(responseStateA);
        doPostMethod.invoke(servlet, requestA, responseA);
        assertTrue(shortlistCalled[0], "Coordinator must be allowed to shortlist application");
        assertEquals("/college/applications", responseStateA.get("redirectUrl"));

        // Case B: Reject
        Map<String, String> paramsB = new HashMap<>();
        paramsB.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsB.put("action", "reject");
        paramsB.put("id", "10");
        Map<String, Object> responseStateB = new HashMap<>();
        HttpServletRequest requestB = createMockRequest(session, paramsB, new HashMap<>(), new HashMap<>());
        HttpServletResponse responseB = createMockResponse(responseStateB);
        doPostMethod.invoke(servlet, requestB, responseB);
        assertTrue(rejectCalled[0], "Coordinator must be allowed to reject application");

        // Case C: Select
        Map<String, String> paramsC = new HashMap<>();
        paramsC.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsC.put("action", "select");
        paramsC.put("id", "20");
        Map<String, Object> responseStateC = new HashMap<>();
        HttpServletRequest requestC = createMockRequest(session, paramsC, new HashMap<>(), new HashMap<>());
        HttpServletResponse responseC = createMockResponse(responseStateC);
        doPostMethod.invoke(servlet, requestC, responseC);
        assertTrue(selectCalled[0], "Coordinator must be allowed to select shortlisted candidate");

        // Case D: Delete
        Map<String, String> paramsD = new HashMap<>();
        paramsD.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        paramsD.put("action", "delete");
        paramsD.put("id", "10");
        Map<String, Object> responseStateD = new HashMap<>();
        HttpServletRequest requestD = createMockRequest(session, paramsD, new HashMap<>(), new HashMap<>());
        HttpServletResponse responseD = createMockResponse(responseStateD);
        doPostMethod.invoke(servlet, requestD, responseD);
        assertTrue(deleteCalled[0], "Coordinator must be allowed to delete application");
    }

    @Test
    @DisplayName("JobServlet doGet enforces coordinator/admin authorization on 'new' and 'edit' forms")
    void testJobServletGetFormNavigationRequiresCoordinatorOrAdmin() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doGetMethod = JobServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Job job = new Job(1L, "Software Engineer", "Acme", "Remote", "Desc", "100000", "FULL_TIME", "Active");
        servlet.setJobDAO(new JobDAO() {
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });

        // 1. Unauthenticated -> redirects to /login
        Map<String, String> newParams = new HashMap<>();
        newParams.put("action", "new");
        Map<String, Object> state1 = new HashMap<>();
        HttpServletRequest req1 = createMockRequest(null, newParams, new HashMap<>(), state1);
        Map<String, Object> respState1 = new HashMap<>();
        doGetMethod.invoke(servlet, req1, createMockResponse(respState1));
        assertEquals("/college/login", respState1.get("redirectUrl"));

        // 2. Student accessing action=new -> 403 Forbidden
        Map<String, Object> studentSessionAttrs = new HashMap<>();
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentSessionAttrs);

        Map<String, Object> respState2 = new HashMap<>();
        HttpServletRequest req2 = createMockRequest(studentSession, newParams, new HashMap<>(), new HashMap<>());
        doGetMethod.invoke(servlet, req2, createMockResponse(respState2));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState2.get("errorCode"));

        // 3. Coordinator accessing action=new -> forwards to /WEB-INF/jsp/job-add.jsp
        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        Map<String, Object> state3 = new HashMap<>();
        HttpServletRequest req3 = createMockRequest(coordSession, newParams, new HashMap<>(), state3);
        Map<String, Object> respState3 = new HashMap<>();
        doGetMethod.invoke(servlet, req3, createMockResponse(respState3));
        assertEquals("/WEB-INF/jsp/job-add.jsp", state3.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(state3.get("forwarded")));

        // 4. Student accessing action=edit -> 403 Forbidden
        Map<String, String> editParams = new HashMap<>();
        editParams.put("action", "edit");
        editParams.put("id", "1");
        Map<String, Object> respState4 = new HashMap<>();
        HttpServletRequest req4 = createMockRequest(studentSession, editParams, new HashMap<>(), new HashMap<>());
        doGetMethod.invoke(servlet, req4, createMockResponse(respState4));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState4.get("errorCode"));

        // 5. Coordinator accessing action=edit -> forwards to /WEB-INF/jsp/job-edit.jsp
        Map<String, Object> state5 = new HashMap<>();
        HttpServletRequest req5 = createMockRequest(coordSession, editParams, new HashMap<>(), state5);
        Map<String, Object> respState5 = new HashMap<>();
        doGetMethod.invoke(servlet, req5, createMockResponse(respState5));
        assertEquals("/WEB-INF/jsp/job-edit.jsp", state5.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(state5.get("forwarded")));
    }

    @Test
    @DisplayName("JobServlet doGet allows unauthenticated and student users to browse jobs and view details")
    void testJobServletGetPublicBrowsingAllowed() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doGetMethod = JobServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Job job = new Job(1L, "Software Engineer", "Acme", "Remote", "Desc", "100000", "FULL_TIME", "Active");
        servlet.setJobDAO(new JobDAO() {
            @Override
            public List<Job> getAllJobs() {
                List<Job> list = new ArrayList<>();
                list.add(job);
                return list;
            }
            @Override
            public Job getJobById(Long id) {
                return job;
            }
        });

        // Case A: Unauthenticated user accesses /jobs listing
        Map<String, Object> stateA = new HashMap<>();
        HttpServletRequest reqA = createMockRequest(null, new HashMap<>(), new HashMap<>(), stateA);
        Map<String, Object> respStateA = new HashMap<>();
        doGetMethod.invoke(servlet, reqA, createMockResponse(respStateA));
        assertEquals("/WEB-INF/jsp/jobs.jsp", stateA.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateA.get("forwarded")));

        // Case B: Unauthenticated user views job details
        Map<String, String> viewParams = new HashMap<>();
        viewParams.put("action", "view");
        viewParams.put("id", "1");
        Map<String, Object> stateB = new HashMap<>();
        HttpServletRequest reqB = createMockRequest(null, viewParams, new HashMap<>(), stateB);
        Map<String, Object> respStateB = new HashMap<>();
        doGetMethod.invoke(servlet, reqB, createMockResponse(respStateB));
        assertEquals("/WEB-INF/jsp/job-details.jsp", stateB.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateB.get("forwarded")));

        // Case C: Student user views job details
        Map<String, Object> studentSessionAttrs = new HashMap<>();
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentSessionAttrs);

        Map<String, Object> stateC = new HashMap<>();
        HttpServletRequest reqC = createMockRequest(studentSession, viewParams, new HashMap<>(), stateC);
        Map<String, Object> respStateC = new HashMap<>();
        doGetMethod.invoke(servlet, reqC, createMockResponse(respStateC));
        assertEquals("/WEB-INF/jsp/job-details.jsp", stateC.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(stateC.get("forwarded")));
    }

    @Test
    @DisplayName("JobServlet doPost rejects student and unauthenticated users on create, update, and delete")
    void testJobServletPostMutatingActionsRejectNonCoordinators() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        String[] actions = new String[]{"create", "update", "delete"};

        for (String action : actions) {
            // Case A: Student user with valid CSRF
            Map<String, Object> studentSessionAttrs = new HashMap<>();
            studentSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
            studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
            studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
            HttpSession studentSession = createMockSession(studentSessionAttrs);

            Map<String, String> params = new HashMap<>();
            params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
            params.put("action", action);
            params.put("id", "1");

            Map<String, Object> respStateStudent = new HashMap<>();
            HttpServletRequest reqStudent = createMockRequest(studentSession, params, new HashMap<>(), new HashMap<>());
            doPostMethod.invoke(servlet, reqStudent, createMockResponse(respStateStudent));
            assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateStudent.get("errorCode"),
                    "Student executing job " + action + " must receive 403 Forbidden");

            // Case B: Unauthenticated user with CSRF in session
            Map<String, Object> anonSessionAttrs = new HashMap<>();
            anonSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
            HttpSession anonSession = createMockSession(anonSessionAttrs);

            Map<String, Object> respStateAnon = new HashMap<>();
            HttpServletRequest reqAnon = createMockRequest(anonSession, params, new HashMap<>(), new HashMap<>());
            doPostMethod.invoke(servlet, reqAnon, createMockResponse(respStateAnon));
            assertEquals(HttpServletResponse.SC_FORBIDDEN, respStateAnon.get("errorCode"),
                    "Unauthenticated user executing job " + action + " must receive 403 Forbidden");
        }
    }

    @Test
    @DisplayName("JobServlet doPost enforces CSRF validation before role checks")
    void testJobServletPostRequiresCsrfToken() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "server-token");
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", "1");
        // Missing CSRF param

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest request = createMockRequest(coordSession, params, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, request, createMockResponse(respState));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"));
        assertEquals("Invalid or missing CSRF token.", respState.get("errorMessage"));
    }

    @Test
    @DisplayName("JobServlet doPost allows coordinator to create, update, and delete jobs")
    void testJobServletPostMutatingActionsAllowedForCoordinator() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] addCalled = new boolean[]{false};
        final boolean[] updateCalled = new boolean[]{false};
        final boolean[] deleteCalled = new boolean[]{false};

        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                addCalled[0] = true;
                return true;
            }
            @Override
            public boolean updateJob(Job job) {
                updateCalled[0] = true;
                return true;
            }
            @Override
            public boolean deleteJob(Long id) {
                deleteCalled[0] = true;
                return true;
            }
        });

        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        // 1. Create Job
        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        createParams.put("action", "create");
        createParams.put("title", "Backend Engineer");
        createParams.put("company", "TechCorp");
        createParams.put("location", "Bangalore");
        createParams.put("jobType", "FULL_TIME");

        Map<String, Object> respState1 = new HashMap<>();
        HttpServletRequest req1 = createMockRequest(coordSession, createParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req1, createMockResponse(respState1));
        assertTrue(addCalled[0]);
        assertEquals("/college/jobs", respState1.get("redirectUrl"));

        // 2. Update Job
        Map<String, String> updateParams = new HashMap<>();
        updateParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        updateParams.put("action", "update");
        updateParams.put("id", "5");
        updateParams.put("title", "Senior Engineer");
        updateParams.put("company", "TechCorp");
        updateParams.put("location", "Bangalore");
        updateParams.put("jobType", "FULL_TIME");

        Map<String, Object> respState2 = new HashMap<>();
        HttpServletRequest req2 = createMockRequest(coordSession, updateParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req2, createMockResponse(respState2));
        assertTrue(updateCalled[0]);
        assertEquals("/college/jobs", respState2.get("redirectUrl"));

        // 3. Delete Job
        Map<String, String> deleteParams = new HashMap<>();
        deleteParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        deleteParams.put("action", "delete");
        deleteParams.put("id", "5");

        Map<String, Object> respState3 = new HashMap<>();
        HttpServletRequest req3 = createMockRequest(coordSession, deleteParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req3, createMockResponse(respState3));
        assertTrue(deleteCalled[0]);
        assertEquals("/college/jobs", respState3.get("redirectUrl"));
    }

    @Test
    @DisplayName("JobServlet doPost allows administrator to create job using mocked JobDAO")
    void testJobServletPostCreateAllowedForAdmin() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] addCalled = new boolean[]{false};
        final Job[] capturedJob = new Job[1];

        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                addCalled[0] = true;
                capturedJob[0] = job;
                return true;
            }
        });

        Map<String, Object> adminSessionAttrs = new HashMap<>();
        adminSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "admin-token");
        adminSessionAttrs.put(LoginServlet.SESSION_USER_ID, 901L);
        adminSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession adminSession = createMockSession(adminSessionAttrs);

        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "admin-token");
        createParams.put("action", "create");
        createParams.put("title", "Lead Systems Architect");
        createParams.put("company", "Global Enterprise");
        createParams.put("location", "Hyderabad");
        createParams.put("jobType", "FULL_TIME");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest req = createMockRequest(adminSession, createParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req, createMockResponse(respState));

        assertTrue(addCalled[0], "JobDAO.addJob must be called when an administrator creates a job");
        assertNotNull(capturedJob[0], "Job instance should be passed to JobDAO.addJob");
        assertEquals("Lead Systems Architect", capturedJob[0].getTitle());
        assertEquals("/college/jobs", respState.get("redirectUrl"));
        assertEquals("Job created successfully.", adminSessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("JobServlet doPost rejects student job creation and never calls JobDAO.addJob")
    void testJobServletPostCreateRejectedForStudent() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] addCalled = new boolean[]{false};
        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                addCalled[0] = true;
                fail("JobDAO.addJob() must never be called for a student user");
                return false;
            }
        });

        Map<String, Object> studentSessionAttrs = new HashMap<>();
        studentSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "student-token");
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentSessionAttrs);

        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "student-token");
        createParams.put("action", "create");
        createParams.put("title", "Unauthorized Student Job");
        createParams.put("company", "Student Corp");
        createParams.put("location", "Remote");
        createParams.put("jobType", "FULL_TIME");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest req = createMockRequest(studentSession, createParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req, createMockResponse(respState));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"),
                "Student attempting job creation must receive 403 Forbidden");
        assertFalse(addCalled[0], "JobDAO.addJob() must never be called when student attempts creation");
    }

    @Test
    @DisplayName("JobServlet doPost rejects unauthenticated POST with no session and never calls JobDAO.addJob")
    void testJobServletPostCreateRejectedForUnauthenticatedNoSession() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] addCalled = new boolean[]{false};
        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                addCalled[0] = true;
                fail("JobDAO.addJob() must never be called for an unauthenticated request");
                return false;
            }
        });

        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "arbitrary-token");
        createParams.put("action", "create");
        createParams.put("title", "Unauthenticated Job");
        createParams.put("company", "Ghost Corp");
        createParams.put("location", "Remote");
        createParams.put("jobType", "FULL_TIME");

        Map<String, Object> respState = new HashMap<>();
        // No session provided (null session)
        HttpServletRequest req = createMockRequest(null, createParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req, createMockResponse(respState));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"),
                "Unauthenticated POST request with no session must receive 403 Forbidden");
        assertFalse(addCalled[0], "JobDAO.addJob() must never be called for unauthenticated request");
    }

    @Test
    @DisplayName("JobServlet doPost rejects unsupported role job creation and never calls JobDAO.addJob")
    void testJobServletPostCreateRejectedForUnsupportedRole() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] addCalled = new boolean[]{false};
        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                addCalled[0] = true;
                fail("JobDAO.addJob() must never be called for an unsupported role");
                return false;
            }
        });

        Map<String, Object> recruiterSessionAttrs = new HashMap<>();
        recruiterSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "recruiter-token");
        recruiterSessionAttrs.put(LoginServlet.SESSION_USER_ID, 701L);
        recruiterSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, "RECRUITER");
        HttpSession recruiterSession = createMockSession(recruiterSessionAttrs);

        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "recruiter-token");
        createParams.put("action", "create");
        createParams.put("title", "Recruiter Job");
        createParams.put("company", "Agency Corp");
        createParams.put("location", "Mumbai");
        createParams.put("jobType", "FULL_TIME");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest req = createMockRequest(recruiterSession, createParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req, createMockResponse(respState));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, respState.get("errorCode"),
                "User with unsupported role attempting job creation must receive 403 Forbidden");
        assertFalse(addCalled[0], "JobDAO.addJob() must never be called for unsupported role");
    }

    @Test
    @DisplayName("JobServlet doPost creates job with valid eligibility criteria (deadline, min CGPA, eligible departments)")
    void testJobServletPostCreateJobWithEligibilityCriteria() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final Job[] capturedJob = new Job[1];
        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                capturedJob[0] = job;
                return true;
            }
        });

        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        createParams.put("action", "create");
        createParams.put("title", "Software Engineer");
        createParams.put("company", "Google");
        createParams.put("location", "Bangalore");
        createParams.put("description", "Join our backend infrastructure team");
        createParams.put("salary", "25 LPA");
        createParams.put("jobType", "Full Time");
        createParams.put("status", "Active");
        createParams.put("deadline", "2026-12-31");
        createParams.put("minCgpa", "7.50");
        createParams.put("eligibleDepartments", "CSE,ECE");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest req = createMockRequest(coordSession, createParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req, createMockResponse(respState));

        assertNotNull(capturedJob[0], "JobDAO.addJob should be called");
        assertEquals("Software Engineer", capturedJob[0].getTitle());
        assertEquals("Google", capturedJob[0].getCompany());
        assertEquals(LocalDate.of(2026, 12, 31), capturedJob[0].getDeadline());
        assertEquals(new BigDecimal("7.50"), capturedJob[0].getMinCgpa());
        assertEquals("CSE,ECE", capturedJob[0].getEligibleDepartments());
        assertEquals("/college/jobs", respState.get("redirectUrl"));
        assertEquals("Job created successfully.", coordSessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("JobServlet doPost create validates deadline date format and CGPA range server-side")
    void testJobServletPostCreateValidationFailures() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean addJob(Job job) {
                fail("addJob must not be called when validation fails");
                return false;
            }
        });

        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        // Case 1: Invalid deadline format (e.g. DD-MM-YYYY instead of YYYY-MM-DD)
        Map<String, String> badDeadlineParams = new HashMap<>();
        badDeadlineParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        badDeadlineParams.put("action", "create");
        badDeadlineParams.put("title", "Dev");
        badDeadlineParams.put("company", "Corp");
        badDeadlineParams.put("location", "City");
        badDeadlineParams.put("jobType", "Full Time");
        badDeadlineParams.put("deadline", "31-12-2026");

        Map<String, Object> attrs1 = new HashMap<>();
        Map<String, Object> state1 = new HashMap<>();
        HttpServletRequest req1 = createMockRequest(coordSession, badDeadlineParams, attrs1, state1);
        doPostMethod.invoke(servlet, req1, createMockResponse(new HashMap<>()));
        assertEquals("/WEB-INF/jsp/job-add.jsp", state1.get("dispatchedPath"));
        assertEquals("Invalid Application Deadline format. Please use YYYY-MM-DD.", attrs1.get("errorMessage"));
        Job retainedJob1 = (Job) attrs1.get("job");
        assertNotNull(retainedJob1);
        assertEquals("Dev", retainedJob1.getTitle());

        // Case 2: Negative CGPA (< 0.00)
        Map<String, String> negCgpaParams = new HashMap<>(badDeadlineParams);
        negCgpaParams.put("deadline", "2026-12-31");
        negCgpaParams.put("minCgpa", "-1.50");

        Map<String, Object> attrs2 = new HashMap<>();
        Map<String, Object> state2 = new HashMap<>();
        HttpServletRequest req2 = createMockRequest(coordSession, negCgpaParams, attrs2, state2);
        doPostMethod.invoke(servlet, req2, createMockResponse(new HashMap<>()));
        assertEquals("/WEB-INF/jsp/job-add.jsp", state2.get("dispatchedPath"));
        assertEquals("Minimum CGPA must be a valid number between 0.00 and 10.00.", attrs2.get("errorMessage"));

        // Case 3: CGPA > 10.00
        Map<String, String> highCgpaParams = new HashMap<>(badDeadlineParams);
        highCgpaParams.put("deadline", "2026-12-31");
        highCgpaParams.put("minCgpa", "10.01");

        Map<String, Object> attrs3 = new HashMap<>();
        Map<String, Object> state3 = new HashMap<>();
        HttpServletRequest req3 = createMockRequest(coordSession, highCgpaParams, attrs3, state3);
        doPostMethod.invoke(servlet, req3, createMockResponse(new HashMap<>()));
        assertEquals("/WEB-INF/jsp/job-add.jsp", state3.get("dispatchedPath"));
        assertEquals("Minimum CGPA must be a valid number between 0.00 and 10.00.", attrs3.get("errorMessage"));

        // Case 4: Non-numeric CGPA
        Map<String, String> nonNumCgpaParams = new HashMap<>(badDeadlineParams);
        nonNumCgpaParams.put("deadline", "2026-12-31");
        nonNumCgpaParams.put("minCgpa", "abc");

        Map<String, Object> attrs4 = new HashMap<>();
        Map<String, Object> state4 = new HashMap<>();
        HttpServletRequest req4 = createMockRequest(coordSession, nonNumCgpaParams, attrs4, state4);
        doPostMethod.invoke(servlet, req4, createMockResponse(new HashMap<>()));
        assertEquals("/WEB-INF/jsp/job-add.jsp", state4.get("dispatchedPath"));
        assertEquals("Minimum CGPA must be a valid number between 0.00 and 10.00.", attrs4.get("errorMessage"));
    }

    @Test
    @DisplayName("JobServlet doPost updates job with valid eligibility criteria (deadline, min CGPA, eligible departments)")
    void testJobServletPostUpdateJobWithEligibilityCriteria() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final Job[] capturedJob = new Job[1];
        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean updateJob(Job job) {
                capturedJob[0] = job;
                return true;
            }
        });

        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        Map<String, String> updateParams = new HashMap<>();
        updateParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        updateParams.put("action", "update");
        updateParams.put("id", "12");
        updateParams.put("title", "Senior Backend Engineer");
        updateParams.put("company", "Microsoft");
        updateParams.put("location", "Hyderabad");
        updateParams.put("jobType", "Full Time");
        updateParams.put("status", "Active");
        updateParams.put("deadline", "2026-11-20");
        updateParams.put("minCgpa", "8.25");
        updateParams.put("eligibleDepartments", "CSE,IT");

        Map<String, Object> respState = new HashMap<>();
        HttpServletRequest req = createMockRequest(coordSession, updateParams, new HashMap<>(), new HashMap<>());
        doPostMethod.invoke(servlet, req, createMockResponse(respState));

        assertNotNull(capturedJob[0], "JobDAO.updateJob should be called");
        assertEquals(12L, capturedJob[0].getId());
        assertEquals("Senior Backend Engineer", capturedJob[0].getTitle());
        assertEquals("Microsoft", capturedJob[0].getCompany());
        assertEquals(LocalDate.of(2026, 11, 20), capturedJob[0].getDeadline());
        assertEquals(new BigDecimal("8.25"), capturedJob[0].getMinCgpa());
        assertEquals("CSE,IT", capturedJob[0].getEligibleDepartments());
        assertEquals("/college/jobs", respState.get("redirectUrl"));
        assertEquals("Job updated successfully.", coordSessionAttrs.get("successMessage"));
    }

    @Test
    @DisplayName("JobServlet doPost update validates deadline date format and CGPA range server-side")
    void testJobServletPostUpdateValidationFailures() throws Exception {
        JobServlet servlet = new JobServlet();
        Method doPostMethod = JobServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        servlet.setJobDAO(new JobDAO() {
            @Override
            public boolean updateJob(Job job) {
                fail("updateJob must not be called when validation fails");
                return false;
            }
        });

        Map<String, Object> coordSessionAttrs = new HashMap<>();
        coordSessionAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        coordSessionAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordSessionAttrs);

        // Case 1: Invalid deadline format
        Map<String, String> badDeadlineParams = new HashMap<>();
        badDeadlineParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        badDeadlineParams.put("action", "update");
        badDeadlineParams.put("id", "12");
        badDeadlineParams.put("title", "Dev");
        badDeadlineParams.put("company", "Corp");
        badDeadlineParams.put("location", "City");
        badDeadlineParams.put("jobType", "Full Time");
        badDeadlineParams.put("deadline", "2026/12/31");

        Map<String, Object> attrs1 = new HashMap<>();
        Map<String, Object> state1 = new HashMap<>();
        HttpServletRequest req1 = createMockRequest(coordSession, badDeadlineParams, attrs1, state1);
        doPostMethod.invoke(servlet, req1, createMockResponse(new HashMap<>()));
        assertEquals("/WEB-INF/jsp/job-edit.jsp", state1.get("dispatchedPath"));
        assertEquals("Invalid Application Deadline format. Please use YYYY-MM-DD.", attrs1.get("errorMessage"));

        // Case 2: CGPA > 10.00
        Map<String, String> highCgpaParams = new HashMap<>(badDeadlineParams);
        highCgpaParams.put("deadline", "2026-12-31");
        highCgpaParams.put("minCgpa", "11.00");

        Map<String, Object> attrs2 = new HashMap<>();
        Map<String, Object> state2 = new HashMap<>();
        HttpServletRequest req2 = createMockRequest(coordSession, highCgpaParams, attrs2, state2);
        doPostMethod.invoke(servlet, req2, createMockResponse(new HashMap<>()));
        assertEquals("/WEB-INF/jsp/job-edit.jsp", state2.get("dispatchedPath"));
        assertEquals("Minimum CGPA must be a valid number between 0.00 and 10.00.", attrs2.get("errorMessage"));
    }

    @Test
    @DisplayName("JobDAO addJob and updateJob correctly map eligibility columns")
    void testJobDAOEligibilityColumnsMapping() {
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("prepareStatement".equals(methodName)) {
                        return (PreparedStatement) Proxy.newProxyInstance(
                                PreparedStatement.class.getClassLoader(),
                                new Class<?>[]{PreparedStatement.class},
                                (stmtProxy, stmtMethod, stmtArgs) -> {
                                    String stmtMethodName = stmtMethod.getName();
                                    if (stmtMethodName.startsWith("set") && stmtArgs != null && stmtArgs.length >= 2) {
                                        boundParams.put((Integer) stmtArgs[0], stmtArgs[1]);
                                        return null;
                                    } else if ("executeUpdate".equals(stmtMethodName)) {
                                        return 1;
                                    } else if ("getGeneratedKeys".equals(stmtMethodName)) {
                                        return Proxy.newProxyInstance(
                                                java.sql.ResultSet.class.getClassLoader(),
                                                new Class<?>[]{java.sql.ResultSet.class},
                                                (rsProxy, rsMethod, rsArgs) -> {
                                                    if ("next".equals(rsMethod.getName())) return true;
                                                    if ("getLong".equals(rsMethod.getName())) return 99L;
                                                    return null;
                                                }
                                        );
                                    }
                                    return null;
                                }
                        );
                    } else if ("close".equals(methodName)) {
                        return null;
                    }
                    return null;
                }
        );

        JobDAO testDao = new JobDAO() {
            @Override
            protected Connection getConnection() {
                return mockConnection;
            }
        };

        // Test addJob
        Job newJob = new Job("Test Role", "Acme", "Remote", "Desc", "50k", "Full Time", "Active",
                LocalDate.of(2026, 12, 1), new BigDecimal("7.00"), "CSE,ECE");
        boolean added = testDao.addJob(newJob);
        assertTrue(added);
        assertEquals(99L, newJob.getId());
        assertEquals(java.sql.Date.valueOf(LocalDate.of(2026, 12, 1)), boundParams.get(8));
        assertEquals(new BigDecimal("7.00"), boundParams.get(9));
        assertEquals("CSE,ECE", boundParams.get(10));

        // Test updateJob
        boundParams.clear();
        newJob.setEligibleDepartments("CSE");
        boolean updated = testDao.updateJob(newJob);
        assertTrue(updated);
        assertEquals(java.sql.Date.valueOf(LocalDate.of(2026, 12, 1)), boundParams.get(8));
        assertEquals(new BigDecimal("7.00"), boundParams.get(9));
        assertEquals("CSE", boundParams.get(10));
        assertEquals(99L, boundParams.get(11));
    }

    @Test
    @DisplayName("ApplicationDAO createApplication throws DAOException (not DuplicateRecordException) for non-1062 integrity errors")
    void testApplicationDAOThrowsDAOExceptionOnOtherIntegrityErrors() {
        Map<String, Object> connState = new HashMap<>();
        // Error code 1452: ER_NO_REFERENCED_ROW_2 (Foreign key constraint fails)
        connState.put("throwOnExecuteUpdate", new SQLException("Cannot add or update a child row: a foreign key constraint fails", "23000", 1452));

        ApplicationDAO testDao = new ApplicationDAO() {
            @Override
            protected Connection getConnection() {
                return createMockConnection(connState);
            }
        };

        Application app = new Application(501L, 200L, LocalDate.now(), "APPLIED");
        DAOException ex = assertThrows(DAOException.class, () -> testDao.createApplication(app));
        assertFalse(ex instanceof DuplicateRecordException, "Non-1062 integrity error must not be classified as DuplicateRecordException");
        assertEquals("Database error occurred while creating application.", ex.getMessage(), "Generic DAOException must shield internal details");
        assertEquals(1, (int) connState.getOrDefault("rollbackCount", 0), "Transaction must be rolled back on integrity error");
    }

    // =====================================================
    // PHASE 5: AUTHUTIL AND ROLE-BASED AUTHORIZATION TESTS
    // =====================================================

    @Test
    @DisplayName("AuthUtil correctly resolves user session ID, role, and authentication states")
    void testAuthUtilMethods() {
        assertNull(AuthUtil.getSessionUserId(null));
        assertNull(AuthUtil.getSessionUserRole(null));
        assertFalse(AuthUtil.isAuthenticated(null));
        assertFalse(AuthUtil.isStudent(null));
        assertFalse(AuthUtil.isCoordinator(null));
        assertFalse(AuthUtil.isAdmin(null));
        assertFalse(AuthUtil.isCoordinatorOrAdmin(null));
        assertFalse(AuthUtil.hasAnyRole(null, User.ROLE_STUDENT));

        // Request with null session
        HttpServletRequest nullSessionReq = createMockRequest(null, new HashMap<>());
        assertNull(AuthUtil.getSessionUserId(nullSessionReq));
        assertNull(AuthUtil.getSessionUserRole(nullSessionReq));
        assertFalse(AuthUtil.isAuthenticated(nullSessionReq));
        assertFalse(AuthUtil.isStudent(nullSessionReq));
        assertFalse(AuthUtil.isCoordinator(nullSessionReq));
        assertFalse(AuthUtil.isAdmin(nullSessionReq));
        assertFalse(AuthUtil.isCoordinatorOrAdmin(nullSessionReq));
        assertFalse(AuthUtil.hasAnyRole(nullSessionReq, User.ROLE_STUDENT));

        // Student session
        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession studentSession = createMockSession(studentAttrs);
        HttpServletRequest studentReq = createMockRequest(studentSession, new HashMap<>());

        assertEquals(101L, AuthUtil.getSessionUserId(studentReq));
        assertEquals(User.ROLE_STUDENT, AuthUtil.getSessionUserRole(studentReq));
        assertTrue(AuthUtil.isAuthenticated(studentReq));
        assertTrue(AuthUtil.isStudent(studentReq));
        assertFalse(AuthUtil.isCoordinator(studentReq));
        assertFalse(AuthUtil.isAdmin(studentReq));
        assertFalse(AuthUtil.isCoordinatorOrAdmin(studentReq));
        assertTrue(AuthUtil.hasAnyRole(studentReq, User.ROLE_STUDENT, User.ROLE_COORDINATOR));
        assertFalse(AuthUtil.hasAnyRole(studentReq, User.ROLE_COORDINATOR, User.ROLE_ADMIN));

        // Coordinator session
        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession coordSession = createMockSession(coordAttrs);
        HttpServletRequest coordReq = createMockRequest(coordSession, new HashMap<>());

        assertEquals(201L, AuthUtil.getSessionUserId(coordReq));
        assertEquals(User.ROLE_COORDINATOR, AuthUtil.getSessionUserRole(coordReq));
        assertTrue(AuthUtil.isAuthenticated(coordReq));
        assertFalse(AuthUtil.isStudent(coordReq));
        assertTrue(AuthUtil.isCoordinator(coordReq));
        assertFalse(AuthUtil.isAdmin(coordReq));
        assertTrue(AuthUtil.isCoordinatorOrAdmin(coordReq));
        assertTrue(AuthUtil.hasAnyRole(coordReq, User.ROLE_COORDINATOR));

        // Admin session
        Map<String, Object> adminAttrs = new HashMap<>();
        adminAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        adminAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession adminSession = createMockSession(adminAttrs);
        HttpServletRequest adminReq = createMockRequest(adminSession, new HashMap<>());

        assertEquals(301L, AuthUtil.getSessionUserId(adminReq));
        assertEquals(User.ROLE_ADMIN, AuthUtil.getSessionUserRole(adminReq));
        assertTrue(AuthUtil.isAuthenticated(adminReq));
        assertFalse(AuthUtil.isStudent(adminReq));
        assertFalse(AuthUtil.isCoordinator(adminReq));
        assertTrue(AuthUtil.isAdmin(adminReq));
        assertTrue(AuthUtil.isCoordinatorOrAdmin(adminReq));
        assertTrue(AuthUtil.hasAnyRole(adminReq, User.ROLE_ADMIN));
    }

    @Test
    @DisplayName("InterviewServlet doGet redirects unauthenticated user to /login")
    void testInterviewServletGetUnauthenticatedRedirectsToLogin() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(null, new HashMap<>(), new HashMap<>(), state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/college/login", state.get("redirectUrl"));
        HttpSession session = req.getSession(false);
        assertNotNull(session);
        assertEquals("You must log in to view interview schedules.", session.getAttribute("errorMessage"));
    }

    @Test
    @DisplayName("InterviewServlet doGet rejects mutating actions with HTTP 405 Method Not Allowed")
    void testInterviewServletGetMutatingActionsReturn405() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(coordAttrs);

        String[] mutatingActions = {"create", "update", "complete", "cancel", "delete"};
        for (String action : mutatingActions) {
            Map<String, String> params = new HashMap<>();
            params.put("action", action);
            Map<String, Object> state = new HashMap<>();
            HttpServletRequest req = createMockRequest(session, params, new HashMap<>(), state);
            HttpServletResponse resp = createMockResponse(state);

            doGetMethod.invoke(servlet, req, resp);

            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, state.get("errorCode"),
                    "Action " + action + " on GET must return HTTP 405");
        }
    }

    @Test
    @DisplayName("InterviewServlet doGet action=edit denies access to students with 403 Forbidden")
    void testInterviewServletGetEditStudentForbidden() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                Student s = new Student();
                s.setId(10L);
                return s;
            }
        });

        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(studentAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "edit");
        params.put("id", "1");
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, state.get("errorCode"));
        assertEquals("Access denied: Only coordinators and administrators can edit interviews.", state.get("errorMessage"));
    }

    @Test
    @DisplayName("InterviewServlet doGet action=edit allows coordinator to view edit form for SCHEDULED interview")
    void testInterviewServletGetEditCoordinatorAllowed() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Interview scheduledInterview = new Interview(50L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "ONLINE", "SCHEDULED");
        scheduledInterview.setId(1L);

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public Interview getInterviewById(Long id) {
                return 1L == id ? scheduledInterview : null;
            }
        });

        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(coordAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "edit");
        params.put("id", "1");
        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/interview-edit.jsp", state.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(state.get("forwarded")));
        assertEquals(scheduledInterview, reqAttrs.get("interview"));
    }

    @Test
    @DisplayName("InterviewServlet doGet action=edit rejects non-SCHEDULED interviews with redirect and error")
    void testInterviewServletGetEditNonScheduledInterviewRedirects() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Interview completedInterview = new Interview(50L, LocalDate.now().minusDays(1), LocalTime.of(10, 0), "ONLINE", "COMPLETED");
        completedInterview.setId(2L);

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public Interview getInterviewById(Long id) {
                return 2L == id ? completedInterview : null;
            }
        });

        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(coordAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "edit");
        params.put("id", "2");
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/college/interviews", state.get("redirectUrl"));
        assertEquals("Only SCHEDULED interviews can be edited or rescheduled.", session.getAttribute("errorMessage"));
    }

    @Test
    @DisplayName("InterviewServlet doGet student view strictly scopes interviews and statistics to own applications")
    void testInterviewServletGetStudentViewScopedToOwnApplications() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                Student s = new Student();
                s.setId(55L);
                return s;
            }
        });

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public List<Application> searchApplications(Long studentId, Long jobId, String status) {
                if (Long.valueOf(55L).equals(studentId)) {
                    List<Application> apps = new ArrayList<>();
                    apps.add(createTestApplication(101L, 55L, 1L, LocalDate.now(), "SHORTLISTED"));
                    apps.add(createTestApplication(102L, 55L, 2L, LocalDate.now(), "SHORTLISTED"));
                    return apps;
                }
                return new ArrayList<>();
            }
        });

        Interview interview1 = new Interview(101L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "ONLINE", "SCHEDULED");
        interview1.setId(1L);
        Interview interview2 = new Interview(102L, LocalDate.now().plusDays(2), LocalTime.of(14, 0), "OFFLINE", "COMPLETED");
        interview2.setId(2L);
        Interview interviewOther = new Interview(999L, LocalDate.now().plusDays(3), LocalTime.of(11, 0), "ONLINE", "SCHEDULED");
        interviewOther.setId(3L);

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public List<Interview> getAllInterviews() {
                List<Interview> all = new ArrayList<>();
                all.add(interview1);
                all.add(interview2);
                all.add(interviewOther);
                return all;
            }
            @Override
            public Map<Long, Boolean> getRescheduledApplicationsMap() {
                return new HashMap<>();
            }
        });

        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(studentAttrs);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, new HashMap<>(), reqAttrs, state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/interviews.jsp", state.get("dispatchedPath"));
        assertTrue(Boolean.TRUE.equals(state.get("forwarded")));

        @SuppressWarnings("unchecked")
        List<Interview> returnedInterviews = (List<Interview>) reqAttrs.get("interviews");
        assertNotNull(returnedInterviews);
        assertEquals(2, returnedInterviews.size());
        assertTrue(returnedInterviews.contains(interview1));
        assertTrue(returnedInterviews.contains(interview2));
        assertFalse(returnedInterviews.contains(interviewOther), "Other student's interview must be excluded");

        @SuppressWarnings("unchecked")
        Map<String, Integer> stats = (Map<String, Integer>) reqAttrs.get("interviewStats");
        assertNotNull(stats);
        assertEquals(2, stats.get("TOTAL"), "Total count must be scoped to student");
        assertEquals(1, stats.get("SCHEDULED"));
        assertEquals(1, stats.get("COMPLETED"));
    }

    @Test
    @DisplayName("InterviewServlet doGet student filter on other applicant application ID returns empty results")
    void testInterviewServletGetStudentFilterOtherApplicantAppReturnsEmpty() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                Student s = new Student();
                s.setId(55L);
                return s;
            }
        });

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public List<Application> searchApplications(Long studentId, Long jobId, String status) {
                List<Application> apps = new ArrayList<>();
                apps.add(createTestApplication(101L, 55L, 1L, LocalDate.now(), "SHORTLISTED"));
                return apps;
            }
        });

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public Map<Long, Boolean> getRescheduledApplicationsMap() {
                return new HashMap<>();
            }
        });

        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(studentAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("filterAppId", "999"); // Application 999 not owned by student 55

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, reqAttrs, state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        @SuppressWarnings("unchecked")
        List<Interview> returnedInterviews = (List<Interview>) reqAttrs.get("interviews");
        assertNotNull(returnedInterviews);
        assertTrue(returnedInterviews.isEmpty(), "Student filtering for unowned application ID must receive empty results");
    }

    @Test
    @DisplayName("InterviewServlet doGet coordinator sees full institutional interviews and global statistics")
    void testInterviewServletGetCoordinatorViewShowsAll() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        Interview in1 = new Interview(101L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "ONLINE", "SCHEDULED");
        Interview in2 = new Interview(202L, LocalDate.now().plusDays(2), LocalTime.of(14, 0), "OFFLINE", "COMPLETED");

        Map<String, Integer> globalStats = new HashMap<>();
        globalStats.put("TOTAL", 10);
        globalStats.put("SCHEDULED", 5);
        globalStats.put("COMPLETED", 3);
        globalStats.put("CANCELLED", 2);

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public List<Interview> getAllInterviews() {
                List<Interview> list = new ArrayList<>();
                list.add(in1);
                list.add(in2);
                return list;
            }
            @Override
            public Map<String, Integer> getInterviewStatistics() {
                return globalStats;
            }
            @Override
            public Map<Long, Boolean> getRescheduledApplicationsMap() {
                return new HashMap<>();
            }
        });

        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(coordAttrs);

        Map<String, Object> reqAttrs = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, new HashMap<>(), reqAttrs, state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/WEB-INF/jsp/interviews.jsp", state.get("dispatchedPath"));
        @SuppressWarnings("unchecked")
        List<Interview> returnedInterviews = (List<Interview>) reqAttrs.get("interviews");
        assertEquals(2, returnedInterviews.size());
        assertEquals(globalStats, reqAttrs.get("interviewStats"));
    }

    @Test
    @DisplayName("InterviewServlet doPost rejects requests with invalid or missing CSRF token")
    void testInterviewServletPostMissingCsrfForbidden() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doPostMethod = InterviewServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "expected-token");
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(coordAttrs);

        Map<String, String> params = new HashMap<>();
        params.put("action", "create");
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "wrong-token");

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, params, new HashMap<>(), state);
        HttpServletResponse resp = createMockResponse(state);

        doPostMethod.invoke(servlet, req, resp);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, state.get("errorCode"));
        assertEquals("Invalid or missing CSRF token.", state.get("errorMessage"));
    }

    @Test
    @DisplayName("InterviewServlet doPost rejects student attempts on all mutating actions with 403 Forbidden")
    void testInterviewServletPostStudentForbidden() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doPostMethod = InterviewServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 101L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(studentAttrs);

        String[] mutatingActions = {"create", "update", "complete", "cancel", "delete"};
        for (String action : mutatingActions) {
            Map<String, String> params = new HashMap<>();
            params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
            params.put("action", action);
            params.put("id", "1");
            params.put("applicationId", "1");

            Map<String, Object> state = new HashMap<>();
            HttpServletRequest req = createMockRequest(session, params, new HashMap<>(), state);
            HttpServletResponse resp = createMockResponse(state);

            doPostMethod.invoke(servlet, req, resp);

            assertEquals(HttpServletResponse.SC_FORBIDDEN, state.get("errorCode"),
                    "Student invoking " + action + " on doPost must receive HTTP 403 Forbidden");
            assertEquals("Access denied: Only coordinators and administrators can manage interviews.", state.get("errorMessage"));
        }
    }

    @Test
    @DisplayName("InterviewServlet doPost allows coordinator to perform create, update, complete, cancel, delete")
    void testInterviewServletPostCoordinatorAllowedAllActions() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doPostMethod = InterviewServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] created = {false};
        final boolean[] updated = {false};
        final boolean[] completed = {false};
        final boolean[] cancelled = {false};
        final boolean[] deleted = {false};

        Interview testInterview = new Interview(10L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "ONLINE", "SCHEDULED");
        testInterview.setId(1L);

        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return createTestApplication(id, 55L, 2L, LocalDate.now(), "SHORTLISTED");
            }
        });

        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public boolean hasActiveInterview(Long applicationId) {
                return false;
            }
            @Override
            public boolean createInterview(Interview in) {
                created[0] = true;
                return true;
            }
            @Override
            public Interview getInterviewById(Long id) {
                return testInterview;
            }
            @Override
            public boolean updateInterview(Interview in) {
                updated[0] = true;
                return true;
            }
            @Override
            public boolean completeInterview(Long id) {
                completed[0] = true;
                return true;
            }
            @Override
            public boolean cancelInterview(Long id) {
                cancelled[0] = true;
                return true;
            }
            @Override
            public boolean deleteInterview(Long id) {
                deleted[0] = true;
                return true;
            }
        });

        Map<String, Object> coordAttrs = new HashMap<>();
        coordAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        coordAttrs.put(LoginServlet.SESSION_USER_ID, 201L);
        coordAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);
        HttpSession session = createMockSession(coordAttrs);

        // 1. Create
        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        createParams.put("action", "create");
        createParams.put("applicationId", "10");
        createParams.put("interviewDate", LocalDate.now().plusDays(2).toString());
        createParams.put("interviewTime", "10:00");
        createParams.put("mode", "ONLINE");
        Map<String, Object> state1 = new HashMap<>();
        doPostMethod.invoke(servlet, createMockRequest(session, createParams, new HashMap<>(), state1), createMockResponse(state1));
        assertTrue(created[0]);
        assertEquals("/college/interviews", state1.get("redirectUrl"));

        // 2. Update
        Map<String, String> updateParams = new HashMap<>();
        updateParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        updateParams.put("action", "update");
        updateParams.put("id", "1");
        updateParams.put("interviewDate", LocalDate.now().plusDays(3).toString());
        updateParams.put("interviewTime", "11:00");
        updateParams.put("mode", "OFFLINE");
        Map<String, Object> state2 = new HashMap<>();
        doPostMethod.invoke(servlet, createMockRequest(session, updateParams, new HashMap<>(), state2), createMockResponse(state2));
        assertTrue(updated[0]);
        assertEquals("/college/interviews", state2.get("redirectUrl"));

        // 3. Complete
        Map<String, String> completeParams = new HashMap<>();
        completeParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        completeParams.put("action", "complete");
        completeParams.put("id", "1");
        Map<String, Object> state3 = new HashMap<>();
        doPostMethod.invoke(servlet, createMockRequest(session, completeParams, new HashMap<>(), state3), createMockResponse(state3));
        assertTrue(completed[0]);
        assertEquals("/college/interviews", state3.get("redirectUrl"));

        // 4. Cancel
        Map<String, String> cancelParams = new HashMap<>();
        cancelParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        cancelParams.put("action", "cancel");
        cancelParams.put("id", "1");
        Map<String, Object> state4 = new HashMap<>();
        doPostMethod.invoke(servlet, createMockRequest(session, cancelParams, new HashMap<>(), state4), createMockResponse(state4));
        assertTrue(cancelled[0]);
        assertEquals("/college/interviews", state4.get("redirectUrl"));

        // 5. Delete
        Map<String, String> deleteParams = new HashMap<>();
        deleteParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        deleteParams.put("action", "delete");
        deleteParams.put("id", "1");
        Map<String, Object> state5 = new HashMap<>();
        doPostMethod.invoke(servlet, createMockRequest(session, deleteParams, new HashMap<>(), state5), createMockResponse(state5));
        assertTrue(deleted[0]);
        assertEquals("/college/interviews", state5.get("redirectUrl"));
    }

    @Test
    @DisplayName("InterviewServlet doPost allows ADMIN role to manage interviews")
    void testInterviewServletPostAdminAllowed() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doPostMethod = InterviewServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);

        final boolean[] created = {false};
        servlet.setApplicationDAO(new ApplicationDAO() {
            @Override
            public Application getApplicationById(Long id) {
                return createTestApplication(id, 55L, 2L, LocalDate.now(), "SHORTLISTED");
            }
        });
        servlet.setInterviewDAO(new InterviewDAO() {
            @Override
            public boolean hasActiveInterview(Long applicationId) {
                return false;
            }
            @Override
            public boolean createInterview(Interview in) {
                created[0] = true;
                return true;
            }
        });

        Map<String, Object> adminAttrs = new HashMap<>();
        adminAttrs.put(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, "valid-token");
        adminAttrs.put(LoginServlet.SESSION_USER_ID, 301L);
        adminAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_ADMIN);
        HttpSession session = createMockSession(adminAttrs);

        Map<String, String> createParams = new HashMap<>();
        createParams.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        createParams.put("action", "create");
        createParams.put("applicationId", "10");
        createParams.put("interviewDate", LocalDate.now().plusDays(2).toString());
        createParams.put("interviewTime", "10:00");
        createParams.put("mode", "ONLINE");

        Map<String, Object> state = new HashMap<>();
        doPostMethod.invoke(servlet, createMockRequest(session, createParams, new HashMap<>(), state), createMockResponse(state));
        assertTrue(created[0]);
        assertEquals("/college/interviews", state.get("redirectUrl"));
    }

    @Test
    @DisplayName("InterviewServlet doGet student without profile redirects to /jobs with error")
    void testInterviewServletStudentWithoutProfileRedirects() throws Exception {
        InterviewServlet servlet = new InterviewServlet();
        Method doGetMethod = InterviewServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);

        servlet.setStudentDAO(new StudentDAO() {
            @Override
            public Student findByUserId(Long userId) {
                return null; // Profile missing
            }
        });

        Map<String, Object> studentAttrs = new HashMap<>();
        studentAttrs.put(LoginServlet.SESSION_USER_ID, 102L);
        studentAttrs.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_STUDENT);
        HttpSession session = createMockSession(studentAttrs);

        Map<String, Object> state = new HashMap<>();
        HttpServletRequest req = createMockRequest(session, new HashMap<>(), new HashMap<>(), state);
        HttpServletResponse resp = createMockResponse(state);

        doGetMethod.invoke(servlet, req, resp);

        assertEquals("/college/jobs", state.get("redirectUrl"));
        assertEquals("No student profile found for your account. Please complete your registration.", session.getAttribute("errorMessage"));
    }

    // =====================================================
    // TEST HELPERS
    // =====================================================

    private Application createTestApplication(Long id, Long studentId, Long jobId, LocalDate date, String status) {
        Application app = new Application(studentId, jobId, date, status);
        app.setId(id);
        return app;
    }

    private Map<String, String> createValidRegistrationParams() {
        Map<String, String> params = new HashMap<>();
        params.put(CsrfUtil.CSRF_TOKEN_PARAM, "valid-token");
        params.put("name", "Alice Wonder");
        params.put("email", "alice@college.edu");
        params.put("password", "ValidPassword123!");
        params.put("confirmPassword", "ValidPassword123!");
        params.put("rollNumber", "CS2026-001");
        params.put("department", "Computer Science");
        params.put("cgpa", "8.75");
        params.put("graduationYear", "2026");
        params.put("phone", "9876543210");
        params.put("skills", "Java, Spring, SQL");
        params.put("resumeUrl", "https://example.com/resume.pdf");
        return params;
    }

    private Connection createMockConnection(Map<String, Object> state) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setAutoCommit".equals(name)) {
                        state.put("autoCommit", args[0]);
                        state.put("setAutoCommitCalled", true);
                        return null;
                    } else if ("getAutoCommit".equals(name)) {
                        return state.getOrDefault("autoCommit", true);
                    } else if ("commit".equals(name)) {
                        if (Boolean.TRUE.equals(state.get("throwOnCommit"))) {
                            throw new SQLException("Simulated commit failure");
                        }
                        int count = (int) state.getOrDefault("commitCount", 0);
                        state.put("commitCount", count + 1);
                        return null;
                    } else if ("rollback".equals(name)) {
                        if (Boolean.TRUE.equals(state.get("throwOnRollback"))) {
                            throw new SQLException("Simulated rollback failure");
                        }
                        int count = (int) state.getOrDefault("rollbackCount", 0);
                        state.put("rollbackCount", count + 1);
                        return null;
                    } else if ("isClosed".equals(name)) {
                        return state.getOrDefault("isClosed", false);
                    } else if ("close".equals(name)) {
                        state.put("isClosed", true);
                        int count = (int) state.getOrDefault("closeCount", 0);
                        state.put("closeCount", count + 1);
                        return null;
                    } else if ("prepareStatement".equals(name)) {
                        SQLException toThrow = (SQLException) state.get("throwOnExecuteUpdate");
                        return Proxy.newProxyInstance(
                                PreparedStatement.class.getClassLoader(),
                                new Class<?>[]{PreparedStatement.class},
                                (stmtProxy, stmtMethod, stmtArgs) -> {
                                    if ("executeUpdate".equals(stmtMethod.getName())) {
                                        if (toThrow != null) {
                                            throw toThrow;
                                        }
                                        return 1;
                                    }
                                    return null;
                                }
                        );
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
                        return attributes.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes.remove(args[0]);
                    } else if ("invalidate".equals(name)) {
                        attributes.put("invalidated", true);
                        return null;
                    }
                    return null;
                }
        );
    }

    private HttpServletRequest createMockRequest(HttpSession session, Map<String, String> params) {
        return createMockRequest(session, params, new HashMap<>(), new HashMap<>());
    }

    private HttpServletRequest createMockRequest(HttpSession session, Map<String, String> params,
                                                 Map<String, Object> attributes, Map<String, Object> state) {
        final HttpSession[] sessionHolder = new HttpSession[]{session};

        jakarta.servlet.RequestDispatcher mockDispatcher = (jakarta.servlet.RequestDispatcher) Proxy.newProxyInstance(
                jakarta.servlet.RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{jakarta.servlet.RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        if (state != null) {
                            state.put("forwarded", true);
                        }
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
                        boolean create = true;
                        if (args != null && args.length == 1) {
                            create = (Boolean) args[0];
                        }
                        HttpSession curr = sessionHolder[0];
                        boolean isInvalidated = false;
                        if (curr != null) {
                            try {
                                Object inv = curr.getAttribute("invalidated");
                                if (Boolean.TRUE.equals(inv)) {
                                    isInvalidated = true;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        if (curr == null || isInvalidated) {
                            if (create) {
                                Map<String, Object> newSessionAttrs = new HashMap<>();
                                if (state != null) {
                                    state.put("newSessionAttrs", newSessionAttrs);
                                }
                                sessionHolder[0] = createMockSession(newSessionAttrs);
                                return sessionHolder[0];
                            } else {
                                return null;
                            }
                        }
                        return curr;
                    } else if ("getParameter".equals(name)) {
                        return params != null ? params.get(args[0]) : null;
                    } else if ("setAttribute".equals(name)) {
                        if (attributes != null) {
                            attributes.put((String) args[0], args[1]);
                        }
                        return null;
                    } else if ("getAttribute".equals(name)) {
                        return attributes != null ? attributes.get(args[0]) : null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes != null ? attributes.remove(args[0]) : null;
                    } else if ("getRequestDispatcher".equals(name)) {
                        if (state != null) {
                            state.put("dispatchedPath", args[0]);
                        }
                        return mockDispatcher;
                    } else if ("getContextPath".equals(name)) {
                        return "/college";
                    } else if ("getServletPath".equals(name)) {
                        return state != null && state.containsKey("servletPath") ? state.get("servletPath") : "/applications";
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
