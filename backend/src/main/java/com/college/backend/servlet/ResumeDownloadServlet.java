package com.college.backend.servlet;

import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller servlet for securely downloading and viewing student and external candidate resumes.
 * Enforces role authorization (Staff or owning candidate only to prevent IDOR)
 * and path traversal validation.
 */
@WebServlet({"/resume/download", "/resume/view"})
public class ResumeDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ResumeDownloadServlet.class.getName());

    private ExternalStudentDAO externalStudentDAO = new ExternalStudentDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void init() {
        this.externalStudentDAO = new ExternalStudentDAO();
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
    }

    // Setters for test dependency injection
    public void setExternalStudentDAO(ExternalStudentDAO externalStudentDAO) {
        this.externalStudentDAO = externalStudentDAO;
    }

    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce Authentication
        if (!AuthUtil.isAuthenticated(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isStaff = AuthUtil.isCoordinatorOrAdmin(request);
        Long sessionUserId = AuthUtil.getSessionUserId(request);

        String extStudentIdParam = request.getParameter("externalStudentId");
        String studentIdParam = request.getParameter("studentId");
        String filePathToServe = null;
        String candidateName = "Candidate";

        if (extStudentIdParam != null && !extStudentIdParam.trim().isEmpty()) {
            try {
                Long extId = Long.parseLong(extStudentIdParam.trim());
                ExternalStudent extStudent = externalStudentDAO != null ? externalStudentDAO.findById(extId) : null;
                if (extStudent == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "External student record not found.");
                    return;
                }

                // IDOR Authorization: Staff or profile owner
                if (!isStaff && (sessionUserId == null || !sessionUserId.equals(extStudent.getUserId()))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You are not authorized to view this resume.");
                    return;
                }

                filePathToServe = extStudent.getResumeUrl();
                if (extStudent.getFullName() != null) {
                    candidateName = extStudent.getFullName();
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid external student identifier.");
                return;
            }
        } else if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
            try {
                Long sId = Long.parseLong(studentIdParam.trim());
                Student student = studentDAO != null ? studentDAO.findById(sId) : null;
                if (student == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student record not found.");
                    return;
                }

                // IDOR Authorization: Staff or profile owner
                if (!isStaff && (sessionUserId == null || !sessionUserId.equals(student.getUserId()))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You are not authorized to view this resume.");
                    return;
                }

                filePathToServe = student.getResumePath();
                if (userDAO != null && student.getUserId() != null) {
                    try {
                        User u = userDAO.findById(student.getUserId());
                        if (u != null && u.getName() != null) {
                            candidateName = u.getName();
                        }
                    } catch (Exception ignored) {}
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student identifier.");
                return;
            }
        } else {
            // Self-service resolution if neither param passed
            if (!isStaff && sessionUserId != null) {
                ExternalStudent ext = externalStudentDAO != null ? externalStudentDAO.findByUserId(sessionUserId) : null;
                if (ext != null) {
                    filePathToServe = ext.getResumeUrl();
                    if (ext.getFullName() != null) candidateName = ext.getFullName();
                } else if (studentDAO != null) {
                    Student s = studentDAO.findByUserId(sessionUserId);
                    if (s != null) {
                        filePathToServe = s.getResumePath();
                        if (userDAO != null && s.getUserId() != null) {
                            try {
                                User u = userDAO.findById(s.getUserId());
                                if (u != null && u.getName() != null) candidateName = u.getName();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }

        if (filePathToServe == null || filePathToServe.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No resume has been uploaded for this candidate.");
            return;
        }

        // If resume path is a full external URL (http / https)
        if (filePathToServe.startsWith("http://") || filePathToServe.startsWith("https://")) {
            response.sendRedirect(filePathToServe);
            return;
        }

        // Resolve local file on server with path traversal defense
        File targetFile = resolveResumeFile(request, filePathToServe);
        if (targetFile == null || !targetFile.exists() || !targetFile.isFile()) {
            LOGGER.log(Level.WARNING, "Resume file not found at: {0}", filePathToServe);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resume file is not available on the server.");
            return;
        }

        // Detect Content-Type
        String filename = targetFile.getName();
        String lowerName = filename.toLowerCase();
        String mimeType = "application/octet-stream";
        if (lowerName.endsWith(".pdf")) {
            mimeType = "application/pdf";
        } else if (lowerName.endsWith(".docx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerName.endsWith(".doc")) {
            mimeType = "application/msword";
        }

        String safeCandidate = candidateName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String extension = lowerName.contains(".") ? lowerName.substring(lowerName.lastIndexOf('.')) : ".pdf";
        String downloadName = "Resume_" + safeCandidate + extension;

        boolean isDownload = "download".equalsIgnoreCase(request.getParameter("mode")) ||
                (request.getServletPath() != null && request.getServletPath().contains("download"));

        response.setContentType(mimeType);
        response.setContentLengthLong(targetFile.length());
        response.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Disposition", (isDownload ? "attachment" : "inline") + "; filename=\"" + downloadName + "\"");

        try (FileInputStream in = new FileInputStream(targetFile);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    private File resolveResumeFile(HttpServletRequest request, String filePath) {
        if (filePath == null) return null;

        // Path traversal defense
        if (filePath.contains("..")) {
            return null;
        }

        // Try direct file first
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file;
        }

        // Try servlet context real path
        try {
            String realPath = request.getServletContext().getRealPath(filePath);
            if (realPath != null) {
                File realFile = new File(realPath);
                if (realFile.exists() && realFile.isFile()) {
                    return realFile;
                }
            }
        } catch (Exception ignored) {}

        // Try tmpdir upload location
        try {
            String cleanName = Paths.get(filePath).getFileName().toString();
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "college_placement_uploads" + File.separator + "resumes", cleanName);
            if (tmpFile.exists() && tmpFile.isFile()) {
                return tmpFile;
            }
        } catch (Exception ignored) {}

        return null;
    }
}
