package com.college.backend.dao;

import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for Student profiles.
 * Manages database persistence for student academic and placement details.
 * Enforces 1-to-1 linkage with User accounts having role = 'STUDENT',
 * uniqueness of roll numbers, and academic validation bounds.
 * Distinguishes invalid input (IllegalArgumentException), duplicate records (DuplicateRecordException),
 * database failure (DAOException), and missing records (returns null/false).
 */
public class StudentDAO {

    /**
     * Finds a student profile by its primary key ID.
     *
     * @param id student profile ID
     * @return Student entity if found, null if not found or id is null
     * @throws DAOException on database connectivity or query failure
     */
    public Student findById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT id, user_id, roll_number, department, cgpa, graduation_year, phone, skills, resume_url " +
                "FROM students WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding student by ID " + id + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds a student profile associated with a specific user ID.
     *
     * @param userId primary key of the associated user
     * @return Student entity if found, null if not found or userId is null
     * @throws DAOException on database connectivity or query failure
     */
    public Student findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        String sql = "SELECT id, user_id, roll_number, department, cgpa, graduation_year, phone, skills, resume_url " +
                "FROM students WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding student by User ID " + userId + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds a student profile by roll number.
     * Normalizes roll number to uppercase before querying.
     *
     * @param rollNumber student university roll number
     * @return Student entity if found, null if not found or rollNumber is blank
     * @throws DAOException on database connectivity or query failure
     */
    public Student findByRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return null;
        }
        String normalizedRollNumber = rollNumber.trim().toUpperCase();

        String sql = "SELECT id, user_id, roll_number, department, cgpa, graduation_year, phone, skills, resume_url " +
                "FROM students WHERE roll_number = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedRollNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error finding student by roll number '" + normalizedRollNumber + "': " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Checks if a roll number is already in use.
     *
     * @param rollNumber candidate roll number
     * @return true if already registered, false otherwise
     * @throws DAOException on database connectivity or query failure
     */
    public boolean existsByRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return false;
        }
        String normalized = rollNumber.trim().toUpperCase();

        String sql = "SELECT COUNT(*) FROM students WHERE roll_number = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalized);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Database error checking roll number '" + normalized + "': " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Creates a new student profile in the database.
     * Enforces the invariant that the referenced user exists and has role 'STUDENT'.
     * Validates CGPA, graduation year, and unique constraints.
     *
     * @param student student profile to create
     * @return true if successfully created
     * @throws IllegalArgumentException on invalid input or non-STUDENT user role
     * @throws DuplicateRecordException if roll number or user association is already registered
     * @throws DAOException on database connectivity or query failure
     */
    public boolean createStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student profile cannot be null.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            return createStudent(connection, student);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate entry creating student profile: " + e.getMessage(), e);
            }
            throw new DAOException("Database error creating student profile: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a student profile using an active database Connection.
     * Enables transactional composition with User creation during registration.
     *
     * @param connection active database connection
     * @param student student profile to create
     * @return true if successfully inserted
     * @throws IllegalArgumentException on invalid input or non-STUDENT user role
     * @throws DuplicateRecordException if roll number or user association is already registered
     * @throws SQLException on database error (permits caller transaction rollback)
     */
    public boolean createStudent(Connection connection, Student student) throws SQLException {
        if (student == null) {
            throw new IllegalArgumentException("Student profile cannot be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null.");
        }

        if (student.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for student profile.");
        }

        String rollNumber = student.getRollNumber();
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number is required for student profile.");
        }
        student.setRollNumber(rollNumber);

        if (!Student.isValidCgpa(student.getCgpa())) {
            throw new IllegalArgumentException("Invalid CGPA: " + student.getCgpa() + ". CGPA must be between 0.00 and 10.00.");
        }

        if (!Student.isValidGraduationYear(student.getGraduationYear())) {
            throw new IllegalArgumentException("Invalid graduation year: " + student.getGraduationYear() + ". Year must be between 2000 and 2100.");
        }

        // Invariant Verification: The associated user must exist and have role = 'STUDENT'
        String checkUserSql = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
            checkStmt.setLong(1, student.getUserId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Associated user with ID " + student.getUserId() + " does not exist.");
                }
                String role = rs.getString("role");
                if (!User.ROLE_STUDENT.equalsIgnoreCase(role)) {
                    throw new IllegalArgumentException("Associated user ID " + student.getUserId()
                            + " has role '" + role + "'. Student profiles can only be associated with users having role STUDENT.");
                }
            }
        }

        // Check if student profile already exists for this user_id (1-to-1 constraint)
        String checkExistingSql = "SELECT COUNT(*) FROM students WHERE user_id = ?";
        try (PreparedStatement checkExistingStmt = connection.prepareStatement(checkExistingSql)) {
            checkExistingStmt.setLong(1, student.getUserId());
            try (ResultSet rs = checkExistingStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateRecordException("Student profile already exists for User ID " + student.getUserId());
                }
            }
        }

        // Check if roll number is already used
        String checkRollSql = "SELECT COUNT(*) FROM students WHERE roll_number = ?";
        try (PreparedStatement checkRollStmt = connection.prepareStatement(checkRollSql)) {
            checkRollStmt.setString(1, student.getRollNumber());
            try (ResultSet rs = checkRollStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateRecordException("Student profile already exists with roll number '" + student.getRollNumber() + "'.");
                }
            }
        }

        String sql = "INSERT INTO students (user_id, user_role, roll_number, department, cgpa, graduation_year, phone, skills, resume_url) " +
                "VALUES (?, 'STUDENT', ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, student.getUserId());
            statement.setString(2, student.getRollNumber());
            statement.setString(3, student.getDepartment());
            statement.setBigDecimal(4, student.getCgpa());
            statement.setInt(5, student.getGraduationYear());
            statement.setString(6, student.getPhone());
            statement.setString(7, student.getSkills());
            statement.setString(8, student.getResumeUrl());

            int affected = statement.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRecordException("Duplicate entry creating student profile: " + e.getMessage(), e);
            }
            throw e;
        }

        return false;
    }

    /**
     * Updates an existing student's academic and contact profile.
     * Note: Normal profile updates CANNOT change user_id, user_role, or roll_number,
     * maintaining security and relational integrity.
     *
     * @param student student profile with updated fields
     * @return true if updated successfully, false if record not found
     * @throws IllegalArgumentException on invalid input
     * @throws DAOException on database connectivity or query failure
     */
    public boolean updateProfile(Student student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Student profile and profile ID are required for update.");
        }

        if (student.getCgpa() != null && !Student.isValidCgpa(student.getCgpa())) {
            throw new IllegalArgumentException("Invalid CGPA: " + student.getCgpa());
        }

        if (student.getGraduationYear() != null && !Student.isValidGraduationYear(student.getGraduationYear())) {
            throw new IllegalArgumentException("Invalid graduation year: " + student.getGraduationYear());
        }

        String sql = "UPDATE students SET department = ?, cgpa = ?, graduation_year = ?, phone = ?, skills = ?, resume_url = ? " +
                "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getDepartment());
            statement.setBigDecimal(2, student.getCgpa());
            statement.setInt(3, student.getGraduationYear() != null ? student.getGraduationYear() : 0);
            statement.setString(4, student.getPhone());
            statement.setString(5, student.getSkills());
            statement.setString(6, student.getResumeUrl());
            statement.setLong(7, student.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Database error updating student profile ID " + student.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Maps a ResultSet row to a Student entity.
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getLong("id"));
        student.setUserId(rs.getLong("user_id"));
        student.setRollNumber(rs.getString("roll_number"));
        student.setDepartment(rs.getString("department"));
        student.setCgpa(rs.getBigDecimal("cgpa"));
        student.setGraduationYear(rs.getInt("graduation_year"));
        student.setPhone(rs.getString("phone"));
        student.setSkills(rs.getString("skills"));
        student.setResumeUrl(rs.getString("resume_url"));
        return student;
    }
}
