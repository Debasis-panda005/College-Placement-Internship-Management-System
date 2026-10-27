package com.college.backend.servlet;

import com.college.backend.dao.StudentDAO;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.CsrfUtil;
import com.college.backend.util.PasswordHashService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Servlet handling student account self-registration.
 * Provides a secure registration form on GET and processes registration on POST.
 * Validates input, hashes passwords using BCrypt, prevents CSRF attacks,
 * and delegates atomic creation to StudentDAO.registerStudent.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Standard RFC-compliant email validation pattern
    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final int MIN_PASSWORD_LENGTH = 6;

    private StudentDAO studentDAO;

    @Override
    public void init() {
        this.studentDAO = new StudentDAO();
    }

    // Setter for testing injection
    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    // =====================================================
    // GET: DISPLAY REGISTRATION FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure CSRF token is present in the user's session
        CsrfUtil.getOrCreateToken(request.getSession());

        // Pull any flash messages
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

        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp")
                .forward(request, response);
    }

    // =====================================================
    // POST: PROCESS STUDENT REGISTRATION
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
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String rollNumber = trimToNull(request.getParameter("rollNumber"));
        String department = trimToNull(request.getParameter("department"));
        String cgpaStr = trimToNull(request.getParameter("cgpa"));
        String graduationYearStr = trimToNull(request.getParameter("graduationYear"));
        String phone = trimToNull(request.getParameter("phone"));
        String skills = trimToNull(request.getParameter("skills"));
        String resumeUrl = trimToNull(request.getParameter("resumeUrl"));

        // 3. Preserve safe form fields for redisplay upon validation failure (NEVER repopulate passwords)
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        request.setAttribute("rollNumber", rollNumber);
        request.setAttribute("department", department);
        request.setAttribute("cgpa", cgpaStr);
        request.setAttribute("graduationYear", graduationYearStr);
        request.setAttribute("phone", phone);
        request.setAttribute("skills", skills);
        request.setAttribute("resumeUrl", resumeUrl);

        // 4. Validate required fields
        if (name == null || email == null || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()
                || rollNumber == null || department == null
                || cgpaStr == null || graduationYearStr == null) {
            request.setAttribute("errorMessage", "All required fields must be filled.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // 5. Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            request.setAttribute("errorMessage", "Please provide a valid email address.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // 6. Validate password criteria & confirmation match
        if (password.length() < MIN_PASSWORD_LENGTH) {
            request.setAttribute("errorMessage", "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // 7. Validate CGPA numeric bounds (0.00 to 10.00)
        BigDecimal cgpa;
        try {
            cgpa = new BigDecimal(cgpaStr);
            if (!Student.isValidCgpa(cgpa)) {
                request.setAttribute("errorMessage", "CGPA must be a valid number between 0.00 and 10.00.");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "CGPA must be a valid decimal number (e.g. 8.50).");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // 8. Validate graduation year bounds (2000 to 2100)
        int graduationYear;
        try {
            graduationYear = Integer.parseInt(graduationYearStr);
            if (!Student.isValidGraduationYear(graduationYear)) {
                request.setAttribute("errorMessage", "Graduation year must be between 2000 and 2100.");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Graduation year must be a valid 4-digit year (e.g. 2026).");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // 9. Hash password using BCrypt - never store or log plaintext
        String passwordHash = PasswordHashService.hashPassword(password);

        // 10. Construct domain models strictly enforcing STUDENT role
        User user = new User(name, email, passwordHash, User.ROLE_STUDENT);
        Student student = new Student(null, rollNumber, department, cgpa, graduationYear, phone, skills, resumeUrl);

        // 11. Delegate atomic creation to transactional DAO
        try {
            boolean registered = studentDAO.registerStudent(user, student);
            if (registered) {
                // Post/Redirect/Get pattern to prevent duplicate submission on refresh
                request.getSession().setAttribute("successMessage",
                        "Registration successful! You may now log in with your credentials.");
                response.sendRedirect(request.getContextPath() + "/login");
            } else {
                request.setAttribute("errorMessage", "Registration could not be completed. Please try again.");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            }
        } catch (DuplicateRecordException e) {
            String duplicateField = (e.getMessage() != null && e.getMessage().toLowerCase().contains("roll"))
                    ? "A student profile with this roll number is already registered."
                    : "An account with this email address already exists.";
            request.setAttribute("errorMessage", duplicateField);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", "Invalid registration details. Please verify your entries.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        } catch (DAOException e) {
            // Protect internal database details from leaking to the browser
            request.setAttribute("errorMessage", "A database error occurred while creating your account. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "An unexpected error occurred during registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
