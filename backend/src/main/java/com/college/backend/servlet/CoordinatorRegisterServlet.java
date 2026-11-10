package com.college.backend.servlet;

import com.college.backend.dao.CoordinatorDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Coordinator;
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
import java.util.regex.Pattern;

/**
 * Servlet handling Placement Coordinator self-registration requests.
 * All public coordinator registrations are placed into PENDING_APPROVAL status.
 * Requires administrator verification and approval prior to portal access.
 * Enforces role security, BCrypt password hashing, and duplicate protection.
 */
@WebServlet({"/coordinator/register", "/coordinator-register"})
public class CoordinatorRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final int MIN_PASSWORD_LENGTH = 8;

    private UserDAO userDAO;
    private CoordinatorDAO coordinatorDAO;

    @Override
    public void init() {
        this.userDAO = new UserDAO();
        this.coordinatorDAO = new CoordinatorDAO(this.userDAO);
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setCoordinatorDAO(CoordinatorDAO coordinatorDAO) {
        this.coordinatorDAO = coordinatorDAO;
    }

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

        request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce CSRF protection
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // 2. Extract and sanitize inputs
        String name = trimToNull(request.getParameter("name"));
        String email = trimToNull(request.getParameter("email"));
        String employeeId = trimToNull(request.getParameter("employeeId"));
        String department = trimToNull(request.getParameter("department"));
        String designation = trimToNull(request.getParameter("designation"));
        String phone = trimToNull(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // 3. Preserve safe parameters for redisplay upon error (NEVER passwords)
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        request.setAttribute("employeeId", employeeId);
        request.setAttribute("department", department);
        request.setAttribute("designation", designation);
        request.setAttribute("phone", phone);

        // 4. Validate required fields
        if (name == null || email == null || employeeId == null || department == null
                || designation == null || phone == null || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            request.setAttribute("errorMessage", "All required fields must be filled.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            return;
        }

        // 5. Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            request.setAttribute("errorMessage", "Please provide a valid institutional email address.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            return;
        }

        // 6. Validate password criteria & confirmation match
        if (password.length() < MIN_PASSWORD_LENGTH) {
            request.setAttribute("errorMessage", "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            return;
        }

        // 7. Pre-check duplicate email
        try {
            if (userDAO != null && userDAO.existsByEmail(email)) {
                request.setAttribute("errorMessage", "An account with this institutional email address already exists.");
                request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
                return;
            }
        } catch (DAOException e) {
            // DB connectivity error
            request.setAttribute("errorMessage", "Database error verifying email uniqueness. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            return;
        }

        // 8. Pre-check duplicate employee ID
        try {
            if (coordinatorDAO != null && coordinatorDAO.existsByEmployeeId(employeeId)) {
                request.setAttribute("errorMessage", "A coordinator profile with Employee ID '" + employeeId + "' already exists.");
                request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
                return;
            }
        } catch (DAOException e) {
            request.setAttribute("errorMessage", "Database error verifying Employee ID. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            return;
        }

        // 9. Hash password using BCrypt
        String passwordHash = PasswordHashService.hashPassword(password);

        // 10. Construct domain models strictly enforcing COORDINATOR role and PENDING_APPROVAL status
        User user = new User(name, email, passwordHash, User.ROLE_COORDINATOR, User.STATUS_PENDING_APPROVAL);
        Coordinator coordinator = new Coordinator(null, employeeId, department, designation, phone);
        coordinator.setStatus(Coordinator.STATUS_PENDING_APPROVAL);

        // 11. Delegate atomic creation to transactional DAO
        try {
            boolean registered = coordinatorDAO.registerCoordinator(user, coordinator);
            if (registered) {
                request.getSession().setAttribute("successMessage",
                        "Registration request submitted successfully! Your coordinator account is pending administrator approval. You will receive access once approved.");
                response.sendRedirect(request.getContextPath() + "/login?role=coordinator");
            } else {
                request.setAttribute("errorMessage", "Registration could not be completed. Please try again.");
                request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
            }
        } catch (DuplicateRecordException e) {
            String duplicateField = (e.getMessage() != null && e.getMessage().toLowerCase().contains("employee"))
                    ? "A coordinator profile with this Employee ID is already registered."
                    : "An account with this institutional email address already exists.";
            request.setAttribute("errorMessage", duplicateField);
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", "Invalid registration details. Please verify your entries.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
        } catch (DAOException e) {
            request.setAttribute("errorMessage", "A database error occurred while creating your account. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "An unexpected error occurred during coordinator registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator-register.jsp").forward(request, response);
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
