package com.college.backend;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.servlet.ApplicationServlet;
import com.college.backend.servlet.LoginServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test for Candidate Name and Academic Details Data Persistence:
 * - External students (e.g., Application #110 / Subashree Das / SCS Autonomous college)
 * - Internal CUTM students
 * - ApplicationDAO applicant_type & external_student_id mapping
 * - ApplicationServlet candidate attribute resolution
 */
public class CandidateNameAndDataPersistenceTest {

    private ApplicationDAO applicationDAO;
    private StudentDAO studentDAO;
    private ExternalStudentDAO externalStudentDAO;
    private UserDAO userDAO;
    private JobDAO jobDAO;
    private InterviewDAO interviewDAO;
    private ApplicationServlet applicationServlet;

    @BeforeAll
    static void initDbConfig() {
        if (System.getProperty("db.password") == null && System.getenv("DB_PASSWORD") == null) {
            System.setProperty("db.password", "Tapu@123");
        }
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        externalStudentDAO = new ExternalStudentDAO(userDAO);
        applicationDAO = new ApplicationDAO();
        jobDAO = new JobDAO();
        interviewDAO = new InterviewDAO();

        applicationServlet = new ApplicationServlet();
        applicationServlet.setApplicationDAO(applicationDAO);
        applicationServlet.setStudentDAO(studentDAO);
        applicationServlet.setExternalStudentDAO(externalStudentDAO);
        applicationServlet.setUserDAO(userDAO);
        applicationServlet.setJobDAO(jobDAO);
        applicationServlet.setInterviewDAO(interviewDAO);
    }

    @Test
    @DisplayName("Verify Application #110 maps external student and resolves Subashree Das")
    void testApplication110SubashreeDasCandidateNameAndAcademicDetails() {
        Application app = applicationDAO.getApplicationById(110L);
        assertNotNull(app, "Application #110 should exist in MySQL");
        assertTrue(app.isExternal(), "Application #110 must be recognized as external");
        assertEquals("EXTERNAL", app.getApplicantType(), "applicantType should be EXTERNAL");
        assertEquals(56L, app.getExternalStudentId(), "externalStudentId should be 56");

        // Verify External Student Profile
        ExternalStudent extS = externalStudentDAO.findByIdOrUserId(app.getExternalStudentId());
        assertNotNull(extS, "External student #56 should exist in MySQL");
        assertEquals("SCS Autonomous college", extS.getCollegeName());
        assertEquals("CSE", extS.getDepartment());
        assertEquals(new BigDecimal("7.90"), extS.getCgpa());
        assertEquals("9765432107", extS.getPhone());
        assertEquals(4, extS.getCurrentSemester());

        // Verify User Entity
        assertNotNull(extS.getUserId());
        User user = userDAO.findById(extS.getUserId());
        assertNotNull(user, "User record #319 should exist in MySQL");
        assertEquals("Subashree Das", user.getName(), "Candidate full name must be Subashree Das");
        assertEquals("subashree123@gmail.com", user.getEmail());
    }

    @Test
    @DisplayName("Verify ApplicationServlet details action loads candidate profile for external student")
    void testApplicationServletDetailsForExternalStudent() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "details");
        parameters.put("id", "110");

        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(LoginServlet.SESSION_USER_ID, 1L);
        sessionAttributes.put(LoginServlet.SESSION_USER_ROLE, User.ROLE_COORDINATOR);

        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAttribute".equals(name)) {
                        return sessionAttributes.get(args[0]);
                    }
                    if ("setAttribute".equals(name)) {
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    }
                    return null;
                }
        );

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> null
        );

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getParameter".equals(name)) {
                        return parameters.get(args[0]);
                    }
                    if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    }
                    if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getSession".equals(name)) {
                        return session;
                    }
                    if ("getRequestDispatcher".equals(name)) {
                        return dispatcher;
                    }
                    if ("getContextPath".equals(name)) {
                        return "";
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );

        Method doGet = ApplicationServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(applicationServlet, request, response);

        ExternalStudent loadedExtStudent = (ExternalStudent) attributes.get("externalStudent");
        assertNotNull(loadedExtStudent, "externalStudent attribute must be populated");
        assertEquals("Subashree Das", loadedExtStudent.getFullName(), "Candidate name must be Subashree Das");
        assertEquals("SCS Autonomous college", loadedExtStudent.getCollegeName());
        assertEquals("CSE", loadedExtStudent.getDepartment());
        assertEquals(new BigDecimal("7.90"), loadedExtStudent.getCgpa());

        User loadedUser = (User) attributes.get("studentUser");
        assertNotNull(loadedUser, "studentUser attribute must be populated");
        assertEquals("Subashree Das", loadedUser.getName());
        assertEquals("subashree123@gmail.com", loadedUser.getEmail());
    }

    @Test
    @DisplayName("Verify attachDisplayMaps populates candidate names for all applications")
    void testAttachDisplayMapsCandidateNames() throws Exception {
        List<Application> apps = applicationDAO.getAllApplications();
        assertNotNull(apps);
        assertFalse(apps.isEmpty());

        Map<String, Object> attributes = new HashMap<>();
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    }
                    return null;
                }
        );

        Method attachDisplayMaps = ApplicationServlet.class.getDeclaredMethod("attachDisplayMaps", HttpServletRequest.class, List.class);
        attachDisplayMaps.setAccessible(true);
        attachDisplayMaps.invoke(applicationServlet, request, apps);

        @SuppressWarnings("unchecked")
        Map<Long, ExternalStudent> extMap = (Map<Long, ExternalStudent>) attributes.get("externalStudentMap");
        @SuppressWarnings("unchecked")
        Map<Long, Student> studMap = (Map<Long, Student>) attributes.get("studentMap");
        @SuppressWarnings("unchecked")
        Map<Long, User> userMap = (Map<Long, User>) attributes.get("userMap");

        assertNotNull(extMap, "externalStudentMap must be present");
        assertNotNull(studMap, "studentMap must be present");
        assertNotNull(userMap, "userMap must be present");

        // Verify Application 110 is mapped with Subashree Das
        ExternalStudent subashree = extMap.get(56L);
        assertNotNull(subashree, "External student #56 should be in externalStudentMap");
        assertEquals("Subashree Das", subashree.getFullName());
        assertEquals("SCS Autonomous college", subashree.getCollegeName());
    }

    @Test
    @DisplayName("Verify StudentDAO joined student name retrieval for internal students")
    void testInternalStudentNameJoinedRetrieval() {
        List<Application> apps = applicationDAO.getAllApplications();
        assertNotNull(apps);
        boolean tested = false;
        for (Application app : apps) {
            if (!app.isExternal() && app.getStudentId() != null) {
                Student resolved = studentDAO.findByIdOrUserId(app.getStudentId());
                if (resolved != null) {
                    assertNotNull(resolved.getName(), "Internal student name should be populated from joined users table");
                    assertNotNull(resolved.getEmail(), "Internal student email should be populated from joined users table");
                    tested = true;
                    break;
                }
            }
        }
        assertTrue(tested, "At least one internal student application should be tested");
    }
}
