package com.college.backend;

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
import com.college.backend.servlet.JobServlet;
import com.college.backend.servlet.RegisterServlet;
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
import java.sql.Connection;
import java.sql.SQLException;
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
    // TEST HELPERS
    // =====================================================

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
                        return session;
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
