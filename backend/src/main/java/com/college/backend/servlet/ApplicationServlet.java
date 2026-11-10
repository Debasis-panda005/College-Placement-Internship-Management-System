package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.ApplicationStatusHistoryDAO;
import com.college.backend.dao.ExternalStudentDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.dao.InterviewReportDAO;
import com.college.backend.dao.InterviewRoundDAO;
import com.college.backend.dao.InterviewRoundHistoryDAO;
import com.college.backend.dao.JobDAO;
import com.college.backend.dao.StudentDAO;
import com.college.backend.dao.UserDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.ExternalStudent;
import com.college.backend.entity.Interview;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.InterviewRoundHistory;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.college.backend.exception.DAOException;
import com.college.backend.exception.DuplicateRecordException;
import com.college.backend.util.CsrfUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({"/applications", "/shortlist"})
public class ApplicationServlet extends HttpServlet {

    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;
    private ApplicationStatusHistoryDAO statusHistoryDAO;
    private StudentDAO studentDAO;
    private ExternalStudentDAO externalStudentDAO = new ExternalStudentDAO();
    private JobDAO jobDAO;
    private UserDAO userDAO = new UserDAO();
    private InterviewRoundDAO roundDAO = new InterviewRoundDAO();
    private InterviewRoundHistoryDAO roundHistoryDAO = new InterviewRoundHistoryDAO();
    private InterviewReportDAO reportDAO = new InterviewReportDAO();

    @Override
    public void init() {
        applicationDAO = new ApplicationDAO();
        interviewDAO = new InterviewDAO();
        statusHistoryDAO = new ApplicationStatusHistoryDAO();
        studentDAO = new StudentDAO();
        externalStudentDAO = new ExternalStudentDAO();
        jobDAO = new JobDAO();
        userDAO = new UserDAO();
        roundDAO = new InterviewRoundDAO();
        roundHistoryDAO = new InterviewRoundHistoryDAO();
        reportDAO = new InterviewReportDAO();
    }

    public void setExternalStudentDAO(ExternalStudentDAO externalStudentDAO) {
        this.externalStudentDAO = externalStudentDAO;
    }

    // Setters for test dependency injection
    public void setApplicationDAO(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    public void setInterviewDAO(InterviewDAO interviewDAO) {
        this.interviewDAO = interviewDAO;
    }

    public void setStatusHistoryDAO(ApplicationStatusHistoryDAO statusHistoryDAO) {
        this.statusHistoryDAO = statusHistoryDAO;
    }

    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public void setUserDAO(UserDAO userDAO) { this.userDAO = userDAO; }

    public void setJobDAO(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    public void setRoundDAO(InterviewRoundDAO roundDAO) {
        this.roundDAO = roundDAO;
    }

    public void setRoundHistoryDAO(InterviewRoundHistoryDAO roundHistoryDAO) {
        this.roundHistoryDAO = roundHistoryDAO;
    }

    public void setReportDAO(InterviewReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    private void attachDisplayMaps(HttpServletRequest request, List<Application> appList) {
        Map<Long, Student> studentMap = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();
        Map<Long, Job> jobMap = new HashMap<>();
        Map<Long, ExternalStudent> externalStudentMap = new HashMap<>();

        if (appList != null) {
            for (Application app : appList) {
                if (app != null) {
                    Long sId = app.getStudentId();
                    Long extId = app.getExternalStudentId() != null ? app.getExternalStudentId() : sId;

                    if (app.isExternal()) {
                        ExternalStudent es = null;
                        if (externalStudentDAO != null && extId != null) {
                            try {
                                es = externalStudentDAO.findByIdOrUserId(extId);
                            } catch (Exception ignored) {}
                        }
                        if (es != null) {
                            if (extId != null) externalStudentMap.put(extId, es);
                            if (sId != null) externalStudentMap.put(sId, es);
                            if (es.getId() != null) externalStudentMap.put(es.getId(), es);
                            if (es.getUserId() != null) externalStudentMap.put(es.getUserId(), es);

                            if (es.getUserId() != null && userDAO != null) {
                                try {
                                    User u = userMap.get(es.getUserId());
                                    if (u == null) {
                                        u = userDAO.findById(es.getUserId());
                                        if (u != null) {
                                            userMap.put(es.getUserId(), u);
                                        }
                                    }
                                    if (u != null) {
                                        if (sId != null) userMap.put(sId, u);
                                        if (extId != null) userMap.put(extId, u);
                                        if (es.getApplicantName() == null || es.getApplicantName().isEmpty()) {
                                            es.setApplicantName(u.getName());
                                        }
                                        if (es.getApplicantEmail() == null || es.getApplicantEmail().isEmpty()) {
                                            es.setApplicantEmail(u.getEmail());
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                        } else if (studentDAO != null && sId != null) {
                            try {
                                Student s = studentDAO.findByIdOrUserId(sId);
                                if (s != null) {
                                    studentMap.put(sId, s);
                                    if (s.getId() != null) studentMap.put(s.getId(), s);
                                    if (s.getUserId() != null) {
                                        studentMap.put(s.getUserId(), s);
                                        if (userDAO != null) {
                                            User u = userMap.get(s.getUserId());
                                            if (u == null) {
                                                u = userDAO.findById(s.getUserId());
                                                if (u != null) userMap.put(s.getUserId(), u);
                                            }
                                            if (u != null) {
                                                userMap.put(sId, u);
                                                if (s.getName() == null) s.setName(u.getName());
                                                if (s.getEmail() == null) s.setEmail(u.getEmail());
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    } else {
                        // Internal student application
                        Student s = null;
                        if (studentDAO != null && sId != null) {
                            try {
                                s = studentDAO.findByIdOrUserId(sId);
                            } catch (Exception ignored) {}
                        }
                        if (s != null) {
                            studentMap.put(sId, s);
                            if (s.getId() != null) studentMap.put(s.getId(), s);
                            if (s.getUserId() != null) studentMap.put(s.getUserId(), s);

                            if (s.getUserId() != null && userDAO != null) {
                                try {
                                    User u = userMap.get(s.getUserId());
                                    if (u == null) {
                                        u = userDAO.findById(s.getUserId());
                                        if (u != null) userMap.put(s.getUserId(), u);
                                    }
                                    if (u != null) {
                                        userMap.put(sId, u);
                                        if (s.getName() == null || s.getName().isEmpty()) {
                                            s.setName(u.getName());
                                        }
                                        if (s.getEmail() == null || s.getEmail().isEmpty()) {
                                            s.setEmail(u.getEmail());
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                        } else {
                            // Fallback: Check if this application belongs to an external student
                            ExternalStudent es = null;
                            if (externalStudentDAO != null && (extId != null || sId != null)) {
                                try {
                                    es = externalStudentDAO.findByIdOrUserId(extId != null ? extId : sId);
                                } catch (Exception ignored) {}
                            }
                            if (es != null) {
                                if (extId != null) externalStudentMap.put(extId, es);
                                if (sId != null) externalStudentMap.put(sId, es);
                                if (es.getId() != null) externalStudentMap.put(es.getId(), es);
                                if (es.getUserId() != null) externalStudentMap.put(es.getUserId(), es);

                                if (es.getUserId() != null && userDAO != null) {
                                    try {
                                        User u = userMap.get(es.getUserId());
                                        if (u == null) {
                                            u = userDAO.findById(es.getUserId());
                                            if (u != null) userMap.put(es.getUserId(), u);
                                        }
                                        if (u != null) {
                                            if (sId != null) userMap.put(sId, u);
                                            if (extId != null) userMap.put(extId, u);
                                            if (es.getApplicantName() == null || es.getApplicantName().isEmpty()) {
                                                es.setApplicantName(u.getName());
                                            }
                                            if (es.getApplicantEmail() == null || es.getApplicantEmail().isEmpty()) {
                                                es.setApplicantEmail(u.getEmail());
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                }
                            } else if (userDAO != null && sId != null) {
                                try {
                                    User u = userDAO.findById(sId);
                                    if (u != null) {
                                        userMap.put(sId, u);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }

                    if (app.getJobId() != null && !jobMap.containsKey(app.getJobId()) && jobDAO != null) {
                        try {
                            Job j = jobDAO.getJobById(app.getJobId());
                            if (j != null) {
                                jobMap.put(app.getJobId(), j);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        request.setAttribute("studentMap", studentMap);
        request.setAttribute("userMap", userMap);
        request.setAttribute("jobMap", jobMap);
        request.setAttribute("externalStudentMap", externalStudentMap);
    }

    private void populateApplicationCandidateAttributes(HttpServletRequest request, Application application) {
        if (application == null) {
            return;
        }

        ExternalStudent extS = null;
        Student s = null;
        User studentUser = null;

        Long extId = application.getExternalStudentId() != null ? application.getExternalStudentId() : application.getStudentId();
        Long sId = application.getStudentId();

        if (application.isExternal() && externalStudentDAO != null && extId != null) {
            try {
                extS = externalStudentDAO.findByIdOrUserId(extId);
            } catch (Exception ignored) {}
        }

        if (extS == null && studentDAO != null && sId != null) {
            try {
                s = studentDAO.findByIdOrUserId(sId);
            } catch (Exception ignored) {}
        }

        // Fallback: If not found yet, check the other DAO
        if (s == null && extS == null) {
            if (externalStudentDAO != null && (extId != null || sId != null)) {
                try {
                    extS = externalStudentDAO.findByIdOrUserId(extId != null ? extId : sId);
                } catch (Exception ignored) {}
            }
        }

        if (extS != null) {
            if (extS.getUserId() != null && userDAO != null) {
                try {
                    studentUser = userDAO.findById(extS.getUserId());
                    if (studentUser != null) {
                        if (extS.getApplicantName() == null || extS.getApplicantName().isEmpty()) {
                            extS.setApplicantName(studentUser.getName());
                        }
                        if (extS.getApplicantEmail() == null || extS.getApplicantEmail().isEmpty()) {
                            extS.setApplicantEmail(studentUser.getEmail());
                        }
                    }
                } catch (Exception ignored) {}
            }
            request.setAttribute("externalStudent", extS);
            request.setAttribute("studentUser", studentUser);
        } else if (s != null) {
            if (s.getUserId() != null && userDAO != null) {
                try {
                    studentUser = userDAO.findById(s.getUserId());
                    if (studentUser != null) {
                        if (s.getName() == null || s.getName().isEmpty()) {
                            s.setName(studentUser.getName());
                        }
                        if (s.getEmail() == null || s.getEmail().isEmpty()) {
                            s.setEmail(studentUser.getEmail());
                        }
                    }
                } catch (Exception ignored) {}
            }
            request.setAttribute("student", s);
            request.setAttribute("studentUser", studentUser);
        } else if (sId != null && userDAO != null) {
            try {
                studentUser = userDAO.findById(sId);
                request.setAttribute("studentUser", studentUser);
            } catch (Exception ignored) {}
        }
    }

    // ============================
    // GET
    // ============================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        CsrfUtil.getOrCreateToken(request.getSession());

        // 1. Enforce Authentication on all Application pages (Requirement 5.1.1)
        HttpSession session = request.getSession(false);
        Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
        String sessionUserRole = session != null ? (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE) : null;

        if (sessionUserId == null) {
            HttpSession newSession = request.getSession(true);
            CsrfUtil.getOrCreateToken(newSession);
            newSession.setAttribute("errorMessage", "You must log in to view applications.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isCoordinatorOrAdmin = User.ROLE_COORDINATOR.equals(sessionUserRole) || User.ROLE_ADMIN.equals(sessionUserRole);
        String sessionStudentType = session != null ? (String) session.getAttribute("studentType") : null;
        boolean isExternalStudent = "EXTERNAL".equalsIgnoreCase(sessionStudentType);

        Student currentStudent = null;
        ExternalStudent currentExternalStudent = null;

        if (!isCoordinatorOrAdmin) {
            if (isExternalStudent) {
                currentExternalStudent = externalStudentDAO != null ? externalStudentDAO.findByUserId(sessionUserId) : null;
                if (currentExternalStudent == null) {
                    HttpSession currSession = request.getSession(false);
                    if (currSession != null) {
                        currSession.setAttribute("errorMessage", "No external candidate profile found for your account.");
                    }
                    response.sendRedirect(request.getContextPath() + "/jobs");
                    return;
                }
            } else {
                currentStudent = studentDAO.findByUserId(sessionUserId);
                if (currentStudent == null) {
                    HttpSession currSession = request.getSession(false);
                    if (currSession != null) {
                        currSession.setAttribute("errorMessage", "No student profile found for your account. Please complete your registration.");
                    }
                    response.sendRedirect(request.getContextPath() + "/jobs");
                    return;
                }
            }
        }

        String servletPath = request.getServletPath();

        // Handle /shortlist route - display shortlisted applications (Requirement 5.1.1, 5.1.2, 5.1.3)
        if ("/shortlist".equals(servletPath)) {

            String errorMessage =
                    (String) request.getSession().getAttribute("errorMessage");
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                request.getSession().removeAttribute("errorMessage");
            }

            String successMessage =
                    (String) request.getSession().getAttribute("successMessage");
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                request.getSession().removeAttribute("successMessage");
            }

            List<Application> shortlistedApplications;
            if (isCoordinatorOrAdmin) {
                shortlistedApplications = applicationDAO.getShortlistedApplications();
            } else if (isExternalStudent && currentExternalStudent != null) {
                List<Application> myApps = applicationDAO.getApplicationsByExternalStudent(currentExternalStudent.getId());
                shortlistedApplications = new ArrayList<>();
                if (myApps != null) {
                    for (Application app : myApps) {
                        if ("SHORTLISTED".equalsIgnoreCase(app.getStatus())) {
                            shortlistedApplications.add(app);
                        }
                    }
                }
            } else {
                List<Application> allShortlisted = applicationDAO.getShortlistedApplications();
                shortlistedApplications = new ArrayList<>();
                if (allShortlisted != null && currentStudent != null) {
                    for (Application app : allShortlisted) {
                        if (currentStudent.getId().equals(app.getStudentId())) {
                            shortlistedApplications.add(app);
                        }
                    }
                }
            }

            List<Interview> allInterviews =
                    interviewDAO.getAllInterviews();

            Map<Long, Interview> interviewMap = new HashMap<>();
            if (allInterviews != null) {
                for (Interview interview : allInterviews) {
                    if (interview.getApplicationId() != null) {
                        Long appId = interview.getApplicationId();
                        Interview existing = interviewMap.get(appId);
                        if (existing == null || (interview.getId() != null && (existing.getId() == null || interview.getId() > existing.getId()))) {
                            interviewMap.put(appId, interview);
                        }
                    }
                }
            }

            Map<Long, Boolean> rescheduledMap =
                    interviewDAO.getRescheduledApplicationsMap();

            request.setAttribute(
                    "applications",
                    shortlistedApplications
            );

            request.setAttribute(
                    "interviewMap",
                    interviewMap
            );

            request.setAttribute(
                    "rescheduledMap",
                    rescheduledMap
            );
            attachDisplayMaps(request, shortlistedApplications
            );

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/shortlist.jsp"
            ).forward(request, response);

            return;
        }

        String action = request.getParameter("action");

        // Reject state-changing actions on GET with HTTP 405 Method Not Allowed
        if ("shortlist".equals(action) || "select".equals(action) || "reject".equals(action) || "delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method GET is not supported for action: " + action);
            return;
        }

        // View Application Details (Requirement 5.1.2, 5.1.5 IDOR Prevention)
        if ("details".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application != null) {

                        // Object ownership validation to prevent IDOR
                        if (!isCoordinatorOrAdmin) {
                            boolean isOwner;
                            if (isExternalStudent && currentExternalStudent != null) {
                                Long extId = currentExternalStudent.getId();
                                Long extUserId = currentExternalStudent.getUserId();
                                isOwner = (extId != null && (extId.equals(application.getExternalStudentId()) || extId.equals(application.getStudentId())))
                                        || (extUserId != null && (extUserId.equals(application.getExternalStudentId()) || extUserId.equals(application.getStudentId())));
                            } else if (currentStudent != null) {
                                isOwner = (currentStudent.getId().equals(application.getStudentId())
                                        || (currentStudent.getUserId() != null && currentStudent.getUserId().equals(application.getStudentId())))
                                        && !application.isExternal();
                            } else {
                                isOwner = false;
                            }
                            if (!isOwner) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                        "Access denied: You are not authorized to view this application.");
                                return;
                            }
                        }

                        Interview interview =
                                interviewDAO.getInterviewByApplicationId(id);

                        populateApplicationCandidateAttributes(request, application);

                        if (jobDAO != null) {
                            try {
                                request.setAttribute("job", jobDAO.getJobById(application.getJobId()));
                            } catch (Exception ignored) {}
                        }

                        request.setAttribute(
                                "application",
                                application
                        );

                        request.setAttribute(
                                "interview",
                                interview
                        );
                        request.setAttribute(
                                "dummyDetails",
                                "dummy"
                        );

                        request.getRequestDispatcher(
                                "/WEB-INF/jsp/application-details.jsp"
                        ).forward(request, response);

                        return;

                    } else {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    }

                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }

        // View Application Status History Timeline (Requirement 5.1.2, 5.1.5 IDOR Prevention)
        if ("history".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application != null) {

                        // Object ownership validation to prevent IDOR
                        if (!isCoordinatorOrAdmin) {
                            boolean isOwner;
                            if (isExternalStudent && currentExternalStudent != null) {
                                Long extId = currentExternalStudent.getId();
                                Long extUserId = currentExternalStudent.getUserId();
                                isOwner = (extId != null && (extId.equals(application.getExternalStudentId()) || extId.equals(application.getStudentId())))
                                        || (extUserId != null && (extUserId.equals(application.getExternalStudentId()) || extUserId.equals(application.getStudentId())));
                            } else if (currentStudent != null) {
                                isOwner = (currentStudent.getId().equals(application.getStudentId())
                                        || (currentStudent.getUserId() != null && currentStudent.getUserId().equals(application.getStudentId())))
                                        && !application.isExternal();
                            } else {
                                isOwner = false;
                            }
                            if (!isOwner) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                        "Access denied: You are not authorized to view this application history.");
                                return;
                            }
                        }

                        List<ApplicationStatusHistory> rawStatusHistory =
                                statusHistoryDAO != null ? statusHistoryDAO.getHistoryByApplicationId(id) : new ArrayList<>();

                        Interview interview =
                                interviewDAO != null ? interviewDAO.getInterviewByApplicationId(id) : null;

                        List<InterviewRound> interviewRounds =
                                roundDAO != null ? roundDAO.getRoundsByApplicationId(id) : new ArrayList<>();

                        Map<Long, List<InterviewRoundHistory>> roundHistoryMap = new HashMap<>();
                        if (roundHistoryDAO != null && interviewRounds != null) {
                            for (InterviewRound r : interviewRounds) {
                                if (r.getId() != null) {
                                    roundHistoryMap.put(r.getId(), roundHistoryDAO.getHistoryByRoundId(r.getId()));
                                }
                            }
                        }

                        InterviewReport interviewReport =
                                reportDAO != null ? reportDAO.getReportByApplicationId(id) : null;

                        List<ApplicationStatusHistory> statusHistory =
                                reconcileStatusHistory(application, rawStatusHistory, interview, interviewRounds, interviewReport);

                        request.setAttribute(
                                "application",
                                application
                        );

                        request.setAttribute(
                                "statusHistory",
                                statusHistory
                        );

                        request.setAttribute(
                                "interview",
                                interview
                        );

                        request.setAttribute(
                                "interviewRounds",
                                interviewRounds
                        );

                        request.setAttribute(
                                "roundHistoryMap",
                                roundHistoryMap
                        );

                        request.setAttribute(
                                "interviewReport",
                                interviewReport
                        );
                        try {
                            populateApplicationCandidateAttributes(request, application);
                            if (jobDAO != null) {
                                request.setAttribute("job", jobDAO.getJobById(application.getJobId()));
                            }
                        } catch (Exception ignored) {}

                        request.getRequestDispatcher(
                                "/WEB-INF/jsp/application-history.jsp"
                        ).forward(request, response);

                        return;

                    } else {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    }

                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            } else {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Application ID is required"
                );
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

            return;
        }

        // Check for error messages from session
        String errorMessage =
                (String) request.getSession().getAttribute("errorMessage");

        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }

        String successMessage =
                (String) request.getSession().getAttribute("successMessage");

        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            request.getSession().removeAttribute("successMessage");
        }

        // Parse search and filter parameters
        String studentIdParam = request.getParameter("studentId");
        String jobIdParam = request.getParameter("jobId");
        String statusParam = request.getParameter("status");
        String categoryParam = request.getParameter("category");

        Long filterStudentId = null;
        if (isCoordinatorOrAdmin) {
            if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
                try {
                    filterStudentId = Long.parseLong(studentIdParam.trim());
                } catch (NumberFormatException e) {
                    // Ignore invalid number format gracefully
                }
            }
        } else if (isExternalStudent && currentExternalStudent != null) {
            filterStudentId = currentExternalStudent.getId();
        } else if (currentStudent != null) {
            // Student role: strictly bound to own student record
            filterStudentId = currentStudent.getId();
        }

        Long filterJobId = null;
        if (jobIdParam != null && !jobIdParam.trim().isEmpty()) {
            try {
                filterJobId = Long.parseLong(jobIdParam.trim());
            } catch (NumberFormatException e) {
                // Ignore invalid number format gracefully
            }
        }

        String filterStatus = null;
        if (statusParam != null && !statusParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusParam.trim())) {
            filterStatus = statusParam.trim();
        }

        // Retrieve filtered or all applications (Requirement 5.1.2, 5.1.3)
        List<Application> applications;
        if (isCoordinatorOrAdmin) {
            if (filterStudentId != null || filterJobId != null || filterStatus != null) {
                applications = applicationDAO.searchApplications(filterStudentId, filterJobId, filterStatus);
            } else {
                applications = applicationDAO.getAllApplications();
            }

            // Filter by candidate category (INTERNAL vs EXTERNAL) if requested
            if (categoryParam != null && !categoryParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(categoryParam.trim())) {
                List<Application> filtered = new ArrayList<>();
                for (Application app : applications) {
                    if ("EXTERNAL".equalsIgnoreCase(categoryParam.trim()) && app.isExternal()) {
                        filtered.add(app);
                    } else if ("INTERNAL".equalsIgnoreCase(categoryParam.trim()) && !app.isExternal()) {
                        filtered.add(app);
                    }
                }
                applications = filtered;
            }
        } else if (isExternalStudent && currentExternalStudent != null) {
            // External Student: always retrieve applications scoped to own external student ID
            List<Application> myApps = applicationDAO.getApplicationsByExternalStudent(currentExternalStudent.getId());
            applications = new ArrayList<>();
            if (myApps != null) {
                for (Application app : myApps) {
                    boolean matchesJob = (filterJobId == null || filterJobId.equals(app.getJobId()));
                    boolean matchesStatus = (filterStatus == null || (app.getStatus() != null && app.getStatus().equalsIgnoreCase(filterStatus)));
                    if (matchesJob && matchesStatus) {
                        applications.add(app);
                    }
                }
            }
        } else {
            // Internal Student: always retrieve applications scoped to own student ID
            applications = applicationDAO.searchApplications(filterStudentId, filterJobId, filterStatus);
        }

        List<Long> completedAppIds =
                interviewDAO.getCompletedApplicationIds();

        request.setAttribute(
                "applications",
                applications
        );

        request.setAttribute(
                "completedAppIds",
                completedAppIds
        );

        // Retain filter parameter values for JSP form inputs
        String resolvedStudentIdStr = "";
        if (isCoordinatorOrAdmin) {
            resolvedStudentIdStr = (studentIdParam != null ? studentIdParam.trim() : "");
        } else if (isExternalStudent && currentExternalStudent != null) {
            resolvedStudentIdStr = currentExternalStudent.getId().toString();
        } else if (currentStudent != null) {
            resolvedStudentIdStr = currentStudent.getId().toString();
        }
        request.setAttribute("selectedStudentId", resolvedStudentIdStr);
        request.setAttribute("selectedJobId", jobIdParam != null ? jobIdParam.trim() : "");
        request.setAttribute("selectedStatus", statusParam != null ? statusParam.trim() : "ALL");
        request.setAttribute("selectedCategory", categoryParam != null ? categoryParam.trim() : "ALL");

        // Retrieve application statistics for dashboard summary
        Map<String, Integer> applicationStats;
        if (isCoordinatorOrAdmin) {
            applicationStats = applicationDAO.getApplicationStatistics();
        } else {
            applicationStats = new HashMap<>();
            applicationStats.put("TOTAL", 0);
            applicationStats.put("APPLIED", 0);
            applicationStats.put("SHORTLISTED", 0);
            applicationStats.put("SELECTED", 0);
            applicationStats.put("REJECTED", 0);

            List<Application> allStudentApps;
            if (isExternalStudent && currentExternalStudent != null) {
                allStudentApps = applicationDAO.getApplicationsByExternalStudent(currentExternalStudent.getId());
            } else if (currentStudent != null) {
                allStudentApps = applicationDAO.searchApplications(currentStudent.getId(), null, null);
            } else {
                allStudentApps = new ArrayList<>();
            }

            if (allStudentApps != null) {
                for (Application app : allStudentApps) {
                    applicationStats.put("TOTAL", applicationStats.get("TOTAL") + 1);
                    String st = app.getStatus();
                    if (st != null && applicationStats.containsKey(st.toUpperCase())) {
                        applicationStats.put(st.toUpperCase(), applicationStats.get(st.toUpperCase()) + 1);
                    }
                }
            }
        }

        request.setAttribute(
                "applicationStats",
                applicationStats
        );
        attachDisplayMaps(request, applications);

        request.getRequestDispatcher(
                "/WEB-INF/jsp/applications.jsp"
        ).forward(request, response);
    }


    // ============================
    // POST
    // ============================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Validate CSRF Token
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        String action = request.getParameter("action");

        // Handle application creation (both "create" and "apply" actions)
        if ("create".equals(action) || "apply".equals(action)) {
            handleCreateApplication(request, response);
            return;
        }

        // Status transitions and deletions require COORDINATOR or ADMIN role (Requirement 5.1.4)
        HttpSession session = request.getSession(false);
        Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
        String sessionUserRole = session != null ? (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE) : null;

        boolean isCoordinatorOrAdmin = sessionUserId != null &&
                (User.ROLE_COORDINATOR.equals(sessionUserRole) || User.ROLE_ADMIN.equals(sessionUserRole));

        if ("shortlist".equals(action) || "reject".equals(action) || "select".equals(action) || "delete".equals(action)) {
            if (!isCoordinatorOrAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied: Only coordinators and administrators can perform this action.");
                return;
            }
        }

        // Shortlist (Allowed only from APPLIED)
        if ("shortlist".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application == null) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    } else if (!"APPLIED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot shortlist: Application is currently "
                                        + application.getStatus()
                                        + " (Only APPLIED applications can be shortlisted)!"
                        );
                    } else {
                        applicationDAO.shortlistApplication(id);
                    }
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

        }

        // Reject (Allowed only from APPLIED or SHORTLISTED)
        else if ("reject".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application == null) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    } else if ("SELECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot reject: Candidate has already been SELECTED!"
                        );
                    } else if ("REJECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application is already REJECTED!"
                        );
                    } else {
                        boolean rejected = applicationDAO.rejectApplication(id);
                        if (rejected) {
                            request.getSession().setAttribute(
                                    "successMessage",
                                    "Application rejected successfully."
                            );
                        } else {
                            request.getSession().setAttribute(
                                    "errorMessage",
                                    "Failed to reject application ID: " + id
                            );
                        }
                    }
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            String from = request.getParameter("from");
            String redirectTarget = "shortlist".equals(from) ? "/shortlist" : ("interviews".equals(from) ? "/interviews" : "/applications");

            response.sendRedirect(
                    request.getContextPath() + redirectTarget
            );

        }

        // Select (Allowed only from SHORTLISTED and interview must be COMPLETED)
        else if ("select".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    Application application = applicationDAO.getApplicationById(id);

                    if (application == null) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application not found with ID: " + id
                        );
                    } else if ("APPLIED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot select: Application must be SHORTLISTED and complete an interview first!"
                        );
                    } else if ("REJECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot select: Application is already REJECTED!"
                        );
                    } else if ("SELECTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Application is already SELECTED!"
                        );
                    } else if (!"SHORTLISTED".equals(application.getStatus())) {
                        request.getSession().setAttribute(
                                "errorMessage",
                                "Cannot select: Application is currently " + application.getStatus() + "!"
                        );
                    } else {
                        // Application is SHORTLISTED: verify interview is COMPLETED
                        boolean completed =
                                interviewDAO.isInterviewCompletedForApplication(id);

                        if (completed) {
                            boolean selected = applicationDAO.selectApplication(id);
                            if (selected) {
                                request.getSession().setAttribute(
                                        "successMessage",
                                        "Application selected successfully."
                                );
                            } else {
                                request.getSession().setAttribute(
                                        "errorMessage",
                                        "Failed to select application ID: " + id
                                );
                            }
                        } else {
                            request.getSession().setAttribute(
                                    "errorMessage",
                                    "Candidate can only be SELECTED after the interview is COMPLETED!"
                            );
                        }
                    }
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            String from = request.getParameter("from");
            String redirectTarget = "shortlist".equals(from) ? "/shortlist" : ("interviews".equals(from) ? "/interviews" : "/applications");

            response.sendRedirect(
                    request.getContextPath() + redirectTarget
            );

        }

        // Delete
        else if ("delete".equals(action)) {

            String idParameter = request.getParameter("id");

            if (idParameter != null && !idParameter.trim().isEmpty()) {

                try {
                    Long id = Long.parseLong(idParameter.trim());
                    applicationDAO.deleteApplication(id);
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute(
                            "errorMessage",
                            "Invalid Application ID: " + idParameter
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );

        }

        // Default fallback
        else {
            response.sendRedirect(
                    request.getContextPath() + "/applications"
            );
        }
    }

    /**
     * Processes student or coordinator application submissions with mandatory authentication,
     * server-side identity resolution, job eligibility validation, and duplicate prevention.
     */
    private void handleCreateApplication(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Enforce Authentication (Requirement 4.1.A, 4.1.E)
        HttpSession session = request.getSession(false);
        Long sessionUserId = session != null ? (Long) session.getAttribute(LoginServlet.SESSION_USER_ID) : null;
        String sessionUserRole = session != null ? (String) session.getAttribute(LoginServlet.SESSION_USER_ROLE) : null;

        if (sessionUserId == null) {
            HttpSession newSession = request.getSession(true);
            CsrfUtil.getOrCreateToken(newSession);
            newSession.setAttribute("errorMessage", "You must log in to apply.");
            request.setAttribute("errorMessage", "You must log in to apply.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Resolve Applicant Student Identity (Requirement 4.1.B, 4.1.C, 4.1.D, 4.1.F, 4.1.G)
        Student applicantStudent = null;
        ExternalStudent applicantExternalStudent = null;
        Long applicantStudentId = null;

        boolean isCoordinatorOrAdmin = User.ROLE_COORDINATOR.equals(sessionUserRole) || User.ROLE_ADMIN.equals(sessionUserRole);
        String submittedStudentIdParam = request.getParameter("studentId");
        String sessionStudentType = session != null ? (String) session.getAttribute("studentType") : null;
        boolean isExternalStudentSession = "EXTERNAL".equalsIgnoreCase(sessionStudentType);

        if (isCoordinatorOrAdmin && submittedStudentIdParam != null && !submittedStudentIdParam.trim().isEmpty()) {
            // Coordinator/Admin administrative workflow: can register on behalf of a specific student ID
            try {
                applicantStudentId = Long.parseLong(submittedStudentIdParam.trim());
                applicantStudent = studentDAO != null ? studentDAO.findByIdOrUserId(applicantStudentId) : null;
                if (applicantStudent == null) {
                    if (externalStudentDAO != null) {
                        applicantExternalStudent = externalStudentDAO.findByIdOrUserId(applicantStudentId);
                    }
                    if (applicantExternalStudent == null) {
                        setApplicationErrorAndRedirect(request, response, "Student not found with ID: " + applicantStudentId);
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                setApplicationErrorAndRedirect(request, response, "Invalid Student ID: " + submittedStudentIdParam);
                return;
            }
        } else {
            // Student self-application workflow:
            // Check if authenticated user is an external student first
            if (isExternalStudentSession && externalStudentDAO != null) {
                applicantExternalStudent = externalStudentDAO.findByUserId(sessionUserId);
            }

            if (applicantExternalStudent == null) {
                applicantStudent = studentDAO.findByUserId(sessionUserId);
            }

            if (applicantStudent == null && applicantExternalStudent == null && externalStudentDAO != null) {
                applicantExternalStudent = externalStudentDAO.findByUserId(sessionUserId);
            }

            if (applicantStudent == null && applicantExternalStudent == null) {
                setApplicationErrorAndRedirect(request, response, "No student profile found for your account. Please complete your registration.");
                return;
            }

            if (applicantStudent != null) {
                applicantStudentId = applicantStudent.getId();
            } else {
                applicantStudentId = applicantExternalStudent.getId();
            }
        }

        // 3. Validate Job ID (Requirement 4.1.I, 4.2.G)
        String jobIdParam = request.getParameter("jobId");
        if (jobIdParam == null || jobIdParam.trim().isEmpty()) {
            setApplicationErrorAndRedirect(request, response, "Job ID is required.");
            return;
        }

        Long jobId;
        try {
            jobId = Long.parseLong(jobIdParam.trim());
        } catch (NumberFormatException e) {
            setApplicationErrorAndRedirect(request, response, "Job not found.");
            return;
        }

        // 4. Eligibility Check A: Job exists (Requirement 4.2.A)
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            setApplicationErrorAndRedirect(request, response, "Job not found.");
            return;
        }

        // 5. Eligibility Check B: Job is not closed (Requirement 4.2.B)
        if (job.isClosed()) {
            setApplicationErrorAndRedirect(request, response, "Applications are closed for this job.");
            return;
        }

        // 6. Eligibility Check C: Application deadline (Requirement 4.2.C)
        LocalDate today = LocalDate.now();
        if (job.isDeadlinePassed(today)) {
            setApplicationErrorAndRedirect(request, response, "The application deadline has passed.");
            return;
        }

        // 7. Verify Eligibility, Target Audience, CGPA & Department
        boolean isExternalApplicant = (applicantExternalStudent != null);

        if (isExternalApplicant) {
            // External candidate checks
            if (!ExternalStudent.STATUS_APPROVED.equalsIgnoreCase(applicantExternalStudent.getVerificationStatus())) {
                setApplicationErrorAndRedirect(request, response, "Your external student registration is pending administrator approval or has been rejected.");
                return;
            }

            if (!job.isEligibleForExternal()) {
                setApplicationErrorAndRedirect(request, response, "This job opportunity is only open to Centurion University internal students.");
                return;
            }

            if (!job.isCgpaEligible(applicantExternalStudent.getCgpa())) {
                setApplicationErrorAndRedirect(request, response, "Your CGPA does not meet the minimum requirement.");
                return;
            }

            if (!job.isDepartmentEligible(applicantExternalStudent.getDepartment())) {
                setApplicationErrorAndRedirect(request, response, "Your department is not eligible for this opportunity.");
                return;
            }

            if (applicationDAO.hasAlreadyAppliedExternal(applicantExternalStudent.getId(), jobId) ||
                    applicationDAO.hasAlreadyApplied(applicantExternalStudent.getId(), jobId)) {
                setApplicationErrorAndRedirect(request, response, "You have already applied for this job.");
                return;
            }
        } else {
            // Internal Centurion student checks
            if (!job.isEligibleForInternal()) {
                setApplicationErrorAndRedirect(request, response, "This job opportunity is only open to external candidates.");
                return;
            }

            if (!job.isCgpaEligible(applicantStudent.getCgpa())) {
                setApplicationErrorAndRedirect(request, response, "Your CGPA does not meet the minimum requirement.");
                return;
            }

            if (!job.isDepartmentEligible(applicantStudent.getDepartment())) {
                setApplicationErrorAndRedirect(request, response, "Your department is not eligible.");
                return;
            }

            if (applicationDAO.hasAlreadyApplied(applicantStudentId, jobId)) {
                setApplicationErrorAndRedirect(request, response, "You have already applied for this job.");
                return;
            }
        }

        // 8. Application Date & Persistence (Requirement 4.1.K, 4.1.L, 4.3.G, 4.3.I)
        LocalDate applicationDate = today;
        String appDateParam = request.getParameter("applicationDate");
        if (appDateParam != null && !appDateParam.trim().isEmpty()) {
            try {
                applicationDate = LocalDate.parse(appDateParam.trim());
            } catch (DateTimeParseException ignored) {
                applicationDate = today;
            }
        }

        Application application = new Application(applicantStudentId, jobId, applicationDate, "APPLIED");
        if (isExternalApplicant) {
            application.setApplicantType(Application.APPLICANT_EXTERNAL);
            application.setExternalStudentId(applicantExternalStudent.getId());
        } else {
            application.setApplicantType(Application.APPLICANT_INTERNAL);
        }

        try {
            boolean created = applicationDAO.createApplication(application);
            if (created) {
                HttpSession currSession = request.getSession(false);
                if (currSession != null) {
                    currSession.setAttribute("successMessage", "Application submitted successfully!");
                }
                request.setAttribute("successMessage", "Application submitted successfully!");
                response.sendRedirect(request.getContextPath() + "/applications");
            } else {
                setApplicationErrorAndRedirect(request, response, "Application could not be created. Please try again later.");
            }
        } catch (DuplicateRecordException e) {
            // Handled database-level unique constraint violation (concurrent submission race condition)
            setApplicationErrorAndRedirect(request, response, "You have already applied for this job.");
        } catch (DAOException e) {
            setApplicationErrorAndRedirect(request, response, "A database error occurred while creating your application. Please try again later.");
        }
    }

    private void setApplicationErrorAndRedirect(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("errorMessage", errorMessage);
        }
        request.setAttribute("errorMessage", errorMessage);
        response.sendRedirect(request.getContextPath() + "/applications");
    }

    private List<ApplicationStatusHistory> reconcileStatusHistory(
            Application application,
            List<ApplicationStatusHistory> rawHistory,
            Interview interview,
            List<InterviewRound> interviewRounds,
            InterviewReport interviewReport) {

        List<ApplicationStatusHistory> result = new ArrayList<>();
        if (rawHistory != null) {
            String lastStatus = null;
            for (ApplicationStatusHistory h : rawHistory) {
                if (h != null && h.getStatus() != null) {
                    String s = h.getStatus().trim().toUpperCase();
                    // Deduplicate consecutive identical statuses
                    if (!s.equals(lastStatus)) {
                        result.add(h);
                        lastStatus = s;
                    }
                }
            }
        }

        boolean hasApplied = false;
        boolean hasShortlisted = false;
        boolean hasInterviewScheduled = false;
        boolean hasInterviewCompleted = false;
        boolean hasSelected = false;
        boolean hasRejected = false;

        for (ApplicationStatusHistory h : result) {
            String s = h.getStatus() != null ? h.getStatus().toUpperCase() : "";
            if (s.contains("APPLIED")) hasApplied = true;
            if (s.contains("SHORTLISTED")) hasShortlisted = true;
            if (s.contains("INTERVIEW_SCHEDULED") || s.contains("INTERVIEW SCHEDULED")) hasInterviewScheduled = true;
            if (s.contains("INTERVIEW_COMPLETED") || s.contains("INTERVIEW COMPLETED")) hasInterviewCompleted = true;
            if (s.contains("SELECTED")) hasSelected = true;
            if (s.contains("REJECTED")) hasRejected = true;
        }

        LocalDateTime appDateTime = (application != null && application.getApplicationDate() != null)
                ? application.getApplicationDate().atTime(9, 0)
                : LocalDateTime.now().minusDays(7);

        // 1. Ensure APPLIED milestone exists
        if (!hasApplied && application != null) {
            ApplicationStatusHistory appliedHistory = new ApplicationStatusHistory(
                    application.getId(),
                    "APPLIED",
                    appDateTime
            );
            result.add(0, appliedHistory);
        }

        // 2. Ensure SHORTLISTED milestone exists if shortlisted or further along
        boolean shouldHaveShortlisted = (interview != null) ||
                (interviewRounds != null && !interviewRounds.isEmpty()) ||
                (application != null && ("SHORTLISTED".equalsIgnoreCase(application.getStatus()) ||
                        "SELECTED".equalsIgnoreCase(application.getStatus()) ||
                        "REJECTED".equalsIgnoreCase(application.getStatus())));

        if (!hasShortlisted && shouldHaveShortlisted && application != null) {
            LocalDateTime shortlistTime = appDateTime.plusHours(2);
            ApplicationStatusHistory shortlistedHistory = new ApplicationStatusHistory(
                    application.getId(),
                    "SHORTLISTED",
                    shortlistTime
            );
            int insertIdx = 0;
            for (int i = 0; i < result.size(); i++) {
                if ("APPLIED".equalsIgnoreCase(result.get(i).getStatus())) {
                    insertIdx = i + 1;
                    break;
                }
            }
            result.add(insertIdx, shortlistedHistory);
        }

        // 3. Ensure INTERVIEW_SCHEDULED milestone exists if interview or rounds exist
        boolean hasInterviewOrRounds = (interview != null) || (interviewRounds != null && !interviewRounds.isEmpty());
        if (!hasInterviewScheduled && hasInterviewOrRounds && application != null) {
            LocalDateTime interviewTime = null;
            if (interview != null && interview.getInterviewDate() != null) {
                interviewTime = interview.getInterviewDate().atTime(
                        interview.getInterviewTime() != null ? interview.getInterviewTime() : LocalTime.of(10, 0));
            } else if (interviewRounds != null && !interviewRounds.isEmpty() && interviewRounds.get(0).getInterviewDate() != null) {
                interviewTime = interviewRounds.get(0).getInterviewDate().atTime(
                        interviewRounds.get(0).getInterviewTime() != null ? interviewRounds.get(0).getInterviewTime() : LocalTime.of(10, 0));
            } else {
                interviewTime = appDateTime.plusDays(1);
            }

            ApplicationStatusHistory interviewSchedHistory = new ApplicationStatusHistory(
                    application.getId(),
                    "INTERVIEW_SCHEDULED",
                    interviewTime
            );
            int insertIdx = result.size();
            for (int i = 0; i < result.size(); i++) {
                if ("SHORTLISTED".equalsIgnoreCase(result.get(i).getStatus())) {
                    insertIdx = i + 1;
                }
            }
            result.add(insertIdx, interviewSchedHistory);
        }

        // 4. Ensure INTERVIEW_COMPLETED milestone exists if completed or selected
        boolean isInterviewCompleted = (interview != null && "COMPLETED".equalsIgnoreCase(interview.getStatus())) ||
                (interviewReport != null && interviewReport.getCompletedRounds() != null && interviewReport.getCompletedRounds() > 0) ||
                (application != null && "SELECTED".equalsIgnoreCase(application.getStatus()));

        if (!hasInterviewCompleted && isInterviewCompleted && application != null) {
            LocalDateTime completedTime = appDateTime.plusDays(2);
            if (interview != null && interview.getInterviewDate() != null) {
                completedTime = interview.getInterviewDate().atTime(
                        interview.getInterviewTime() != null ? interview.getInterviewTime().plusHours(2) : LocalTime.of(12, 0));
            }
            ApplicationStatusHistory completedHistory = new ApplicationStatusHistory(
                    application.getId(),
                    "INTERVIEW_COMPLETED",
                    completedTime
            );
            int insertIdx = result.size();
            for (int i = 0; i < result.size(); i++) {
                String s = result.get(i).getStatus();
                if ("INTERVIEW_SCHEDULED".equalsIgnoreCase(s) || "INTERVIEW_RESCHEDULED".equalsIgnoreCase(s)) {
                    insertIdx = i + 1;
                }
            }
            result.add(insertIdx, completedHistory);
        }

        // 5. Ensure SELECTED milestone exists if current status is SELECTED
        if (!hasSelected && application != null && "SELECTED".equalsIgnoreCase(application.getStatus())) {
            LocalDateTime selectedTime = (interviewReport != null && interviewReport.getDecisionDate() != null)
                    ? interviewReport.getDecisionDate()
                    : LocalDateTime.now();
            ApplicationStatusHistory selectedHistory = new ApplicationStatusHistory(
                    application.getId(),
                    "SELECTED",
                    selectedTime
            );
            result.add(selectedHistory);
        }

        // 6. Ensure REJECTED milestone exists if current status is REJECTED
        if (!hasRejected && application != null && "REJECTED".equalsIgnoreCase(application.getStatus())) {
            LocalDateTime rejectedTime = (interviewReport != null && interviewReport.getDecisionDate() != null)
                    ? interviewReport.getDecisionDate()
                    : LocalDateTime.now();
            ApplicationStatusHistory rejectedHistory = new ApplicationStatusHistory(
                    application.getId(),
                    "REJECTED",
                    rejectedTime
            );
            result.add(rejectedHistory);
        }

        // Chronological sort
        result.sort(Comparator.comparing(h -> h.getChangedAt() != null ? h.getChangedAt() : LocalDateTime.MIN));

        return result;
    }
}
