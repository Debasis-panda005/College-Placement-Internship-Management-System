
package com.college.placement.student;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private StudentDAO dao;

    @Override
    public void init() throws ServletException {
        dao = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            // Open the edit form for one student
            if ("edit".equals(action)) {
                int id = parseId(request.getParameter("id"));

                if (id <= 0) {
                    response.sendError(400, "Invalid student ID.");
                    return;
                }

                Student student = dao.getStudentById(id);

                if (student == null) {
                    response.sendError(404, "Student not found.");
                    return;
                }

                request.setAttribute("student", student);
                request.getRequestDispatcher(
                    "/admin/edit-student.jsp"
                ).forward(request, response);

                return;
            }

            // Display all students
            request.setAttribute("students", dao.getAllStudents());

            request.getRequestDispatcher("/admin/students.jsp")
                   .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid student ID.");
        } catch (SQLException e) {
            getServletContext().log("Unable to load students.", e);
            throw new ServletException("Unable to load students.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                Student student = readStudent(request);

                dao.addStudent(student);

            } else if ("update".equals(action)) {
                int id = parseId(request.getParameter("id"));

                if (id <= 0) {
                    response.sendError(400, "Invalid student ID.");
                    return;
                }

                Student existing = dao.getStudentById(id);

                if (existing == null) {
                    response.sendError(404, "Student not found.");
                    return;
                }

                Student student = readStudent(request);
                student.setId(id);

                dao.updateStudent(student);

            } else if ("delete".equals(action)) {
                int id = parseId(request.getParameter("id"));

                if (id <= 0) {
                    response.sendError(400, "Invalid student ID.");
                    return;
                }

                if (!dao.deleteStudent(id)) {
                    response.sendError(404, "Student not found.");
                    return;
                }

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect(
                request.getContextPath() + "/students"
            );

        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid student ID or graduation year.");
        } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Unable to save student.", e);

            // Includes duplicate email errors from the database.
            throw new ServletException(
                "Could not save the student. Check that the email "
                + "is not already registered.", e
            );
        }
    }

    private Student readStudent(HttpServletRequest request) {
        String name = clean(request.getParameter("name"));
        String email = clean(request.getParameter("email"));
        String yearText = clean(request.getParameter("graduationYear"));

        if (name == null || email == null || yearText == null) {
            throw new IllegalArgumentException(
                "Name, email, and graduation year are required."
            );
        }

        int year = Integer.parseInt(yearText);

        if (year < 1900 || year > 2200) {
            throw new IllegalArgumentException(
                "Enter a valid graduation year."
            );
        }

        Student student = new Student();

        student.setName(name);
        student.setEmail(email);
        student.setPhone(clean(request.getParameter("phone")));
        student.setDepartment(clean(request.getParameter("department")));
        student.setGraduationYear(year);

        return student;
    }

    private int parseId(String value) {
        return Integer.parseInt(value);
    }

    private String clean(String value) {
        return value == null || value.trim().isEmpty()
                ? null : value.trim();
    }
}
