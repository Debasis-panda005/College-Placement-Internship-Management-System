
package com.college.placement.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.college.placement.internship.job.DBConnection;

public class StudentDAO {

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getConnection();
        } catch (Exception e) {
            throw new SQLException(
                "Unable to connect to the placement database.", e
            );
        }
    }

    // Get all students
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT id, name, email, phone, department, "
                   + "graduation_year FROM students ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapStudent(rs));
            }
        }

        return students;
    }

    // Get one student by ID
    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT id, name, email, phone, department, "
                   + "graduation_year FROM students WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        }

        return null;
    }

    // Convert a database row into a Student object
    private Student mapStudent(ResultSet rs) throws SQLException {
        Student student = new Student();

        student.setId(rs.getInt("id"));
        student.setName(rs.getString("name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setDepartment(rs.getString("department"));
        student.setGraduationYear(rs.getInt("graduation_year"));

        return student;
    }

    // Add a student
    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students "
                   + "(name, email, phone, department, graduation_year) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setString(4, student.getDepartment());
            ps.setInt(5, student.getGraduationYear());

            ps.executeUpdate();
        }
    }

    // Update an existing student
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, "
                   + "phone = ?, department = ?, graduation_year = ? "
                   + "WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setString(4, student.getDepartment());
            ps.setInt(5, student.getGraduationYear());
            ps.setInt(6, student.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete a student
    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
