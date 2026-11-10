package com.college.backend.servlet;

import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.CsrfUtil;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Servlet handling External Student self-registration for students from other colleges/universities.
 * All public registrations are placed into PENDING verification status.
 * Requires administrator verification prior to applying for placement opportunities.
 * Enforces role security (STUDENT), BCrypt password hashing, and duplicate protection.
 */
@WebServlet({"/external/register", "/external-register"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 5 * 1024 * 1024,         // 5 MB max file size
        maxRequestSize = 10 * 1024 * 1024      // 10 MB total request size
)
public class ExternalStudentRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList(".pdf", ".doc", ".docx")
    );

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
            Arrays.asList(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/octet-stream"
            )
    );

    private UserDAO userDAO;
    private ExternalStudentDAO externalStudentDAO;

    @Override
    public void init() {
        this.userDAO = new UserDAO();
        this.externalStudentDAO = new ExternalStudentDAO(this.userDAO);
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setExternalStudentDAO(ExternalStudentDAO externalStudentDAO) {
        this.externalStudentDAO = externalStudentDAO;
    }

    // =====================================================
    // GET: DISPLAY EXTERNAL REGISTRATION FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure CSRF token is present in the session
        CsrfUtil.getOrCreateToken(request.getSession());

        // Extract flash messages if any
        String successMessage = (String) request.getSession().getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            request.getSession().removeAttribute("successMessage");
        }

        String errorMessage = (String) request.getSession().getAttribute("errorMessage");
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }

        request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp")
                .forward(request, response);
    }

    // =====================================================
    // POST: PROCESS EXTERNAL STUDENT REGISTRATION
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce CSRF protection
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // 2. Extract and sanitize parameters
        String name = trimToNull(request.getParameter("name"));
        String email = trimToNull(request.getParameter("email"));
        String phone = trimToNull(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String collegeName = trimToNull(request.getParameter("collegeName"));
        String collegeLocation = trimToNull(request.getParameter("collegeLocation"));
        String degree = trimToNull(request.getParameter("degree"));
        String department = trimToNull(request.getParameter("department"));
        String currentSemesterStr = trimToNull(request.getParameter("currentSemester"));
        String cgpaStr = trimToNull(request.getParameter("cgpa"));
        String graduationYearStr = trimToNull(request.getParameter("graduationYear"));
        String resumeUrlParam = trimToNull(request.getParameter("resumeUrl"));

        // 3. Preserve safe parameters for redisplay upon error (NEVER preserve passwords)
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("collegeName", collegeName);
        request.setAttribute("collegeLocation", collegeLocation);
        request.setAttribute("degree", degree);
        request.setAttribute("department", department);
        request.setAttribute("currentSemester", currentSemesterStr);
        request.setAttribute("cgpa", cgpaStr);
        request.setAttribute("graduationYear", graduationYearStr);
        request.setAttribute("resumeUrl", resumeUrlParam);

        // 4. Validate required text fields
        if (name == null || email == null || phone == null
                || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()
                || collegeName == null || collegeLocation == null
                || degree == null || department == null
                || currentSemesterStr == null || cgpaStr == null || graduationYearStr == null) {
            request.setAttribute("errorMessage", "All required fields must be filled.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 5. Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            request.setAttribute("errorMessage", "Please provide a valid email address.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 6. Validate password criteria & confirmation match
        if (password.length() < MIN_PASSWORD_LENGTH) {
            request.setAttribute("errorMessage", "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 7. Validate Semester (1 to 12)
        int currentSemester;
        try {
            currentSemester = Integer.parseInt(currentSemesterStr);
            if (!ExternalStudent.isValidSemester(currentSemester)) {
                request.setAttribute("errorMessage", "Current semester must be between 1 and 12.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Current semester must be a valid integer (e.g. 6).");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 8. Validate CGPA numeric bounds (0.00 to 10.00)
        BigDecimal cgpa;
        try {
            cgpa = new BigDecimal(cgpaStr);
            if (!ExternalStudent.isValidCgpa(cgpa)) {
                request.setAttribute("errorMessage", "CGPA must be a valid number between 0.00 and 10.00.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "CGPA must be a valid decimal number (e.g. 8.75).");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 9. Validate Graduation Year bounds (2000 to 2100)
        int graduationYear;
        try {
            graduationYear = Integer.parseInt(graduationYearStr);
            if (!ExternalStudent.isValidGraduationYear(graduationYear)) {
                request.setAttribute("errorMessage", "Graduation year must be between 2000 and 2100.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Graduation year must be a valid 4-digit year (e.g. 2026).");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 10. Validate & Process Uploaded Resume File
        Part resumePart = null;
        try {
            resumePart = request.getPart("resume");
        } catch (Exception ignored) {
            // Multipart parsing might not be present in non-multipart requests or testing
        }

        String savedResumeUrl = null;
        if (resumePart != null && resumePart.getSize() > 0) {
            if (resumePart.getSize() > MAX_FILE_SIZE_BYTES) {
                request.setAttribute("errorMessage", "Uploaded resume exceeds the maximum allowed file size of 5 MB.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }

            String submittedFileName = resumePart.getSubmittedFileName();
            if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
                request.setAttribute("errorMessage", "Please provide a valid resume file.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }

            String extension = "";
            int dotIdx = submittedFileName.lastIndexOf('.');
            if (dotIdx >= 0) {
                extension = submittedFileName.substring(dotIdx).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                request.setAttribute("errorMessage", "Only PDF, DOC, or DOCX resume documents are supported.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }

            String contentType = resumePart.getContentType();
            if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                request.setAttribute("errorMessage", "Invalid file type. Only PDF and Word documents are permitted.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }

            try {
                savedResumeUrl = saveUploadedResume(resumePart, request);
            } catch (IOException e) {
                request.setAttribute("errorMessage", "Failed to save uploaded resume. Please try again.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }
        } else if (resumeUrlParam != null && !resumeUrlParam.isEmpty()) {
            // Fallback for tests or external URL inputs
            savedResumeUrl = resumeUrlParam;
        } else {
            request.setAttribute("errorMessage", "Please upload your resume document (PDF, DOC, or DOCX).");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 11. Pre-check duplicate email
        try {
            if (userDAO != null && userDAO.existsByEmail(email)) {
                request.setAttribute("errorMessage", "An account with this email address already exists. Please sign in or use another email.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
                return;
            }
        } catch (DAOException e) {
            request.setAttribute("errorMessage", "Database error verifying email uniqueness. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            return;
        }

        // 12. Hash password using BCrypt - never store plaintext passwords
        String passwordHash = PasswordHashService.hashPassword(password);

        // 13. Construct domain models strictly enforcing STUDENT role and PENDING status
        User user = new User(name, email, passwordHash, User.ROLE_STUDENT, User.STATUS_PENDING);
        ExternalStudent student = new ExternalStudent(null, collegeName, collegeLocation, degree,
                department, currentSemester, cgpa, graduationYear, phone, savedResumeUrl);
        student.setVerificationStatus(ExternalStudent.STATUS_PENDING);

        // 14. Delegate atomic creation to transactional DAO
        try {
            boolean registered = externalStudentDAO.registerExternalStudent(user, student);
            if (registered) {
                // Flash success message and redirect to login
                request.getSession().setAttribute("successMessage",
                        "Registration submitted successfully! Your account is currently pending verification. You will be able to apply for opportunities once approved.");
                response.sendRedirect(request.getContextPath() + "/login");
            } else {
                request.setAttribute("errorMessage", "Registration could not be completed. Please try again.");
                request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
            }
        } catch (DuplicateRecordException e) {
            request.setAttribute("errorMessage", "An account with this email address already exists.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", "Invalid registration details: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
        } catch (DAOException e) {
            request.setAttribute("errorMessage", "A database error occurred while creating your account. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "An unexpected error occurred during registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/external-register.jsp").forward(request, response);
        }
    }

    private String saveUploadedResume(Part filePart, HttpServletRequest request) throws IOException {
        String submittedFileName = filePart.getSubmittedFileName();
        String cleanFileName = new File(submittedFileName).getName();
        String safeBaseName = cleanFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        String uniqueFileName = UUID.randomUUID().toString() + "_" + safeBaseName;

        String uploadDir = null;
        try {
            uploadDir = request.getServletContext().getRealPath("/uploads/resumes");
        } catch (Exception ignored) {
        }

        File dir;
        if (uploadDir != null && !uploadDir.isEmpty()) {
            dir = new File(uploadDir);
        } else {
            dir = new File(System.getProperty("java.io.tmpdir"), "college_placement_uploads" + File.separator + "resumes");
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File targetFile = new File(dir, uniqueFileName);
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/resumes/" + uniqueFileName;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
