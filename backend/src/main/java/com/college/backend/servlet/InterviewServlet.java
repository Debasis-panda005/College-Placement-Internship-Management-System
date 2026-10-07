package com.college.backend.servlet;

import com.college.backend.dao.ApplicationDAO;
import com.college.backend.dao.InterviewDAO;
import com.college.backend.entity.Application;
import com.college.backend.entity.Interview;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@WebServlet("/interviews")
public class InterviewServlet extends HttpServlet {

    private InterviewDAO interviewDAO;
    private ApplicationDAO applicationDAO;

    @Override
    public void init() {
        interviewDAO = new InterviewDAO();
        applicationDAO = new ApplicationDAO();
    }

    // =====================================================
    // GET REQUEST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // -------------------------------------------------
        // DELETE INTERVIEW
        // -------------------------------------------------

        if ("delete".equals(action)) {

            Long id = Long.parseLong(
                    request.getParameter("id")
            );

            interviewDAO.deleteInterview(id);

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );

        }

        // -------------------------------------------------
        // COMPLETE INTERVIEW
        // -------------------------------------------------

        else if ("complete".equals(action)) {

            Long id = Long.parseLong(
                    request.getParameter("id")
            );

            Interview interview =
                    interviewDAO.getInterviewById(id);

            if (interview != null) {

                interview.setStatus("COMPLETED");

                interviewDAO.updateInterview(interview);
            }

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );

        }

        // -------------------------------------------------
        // DEFAULT - SHOW ALL INTERVIEWS
        // -------------------------------------------------

        else {

            // Pre-fill applicationId if provided (e.g. from shortlist page)
            String appIdParameter = request.getParameter("applicationId");
            if (appIdParameter != null && !appIdParameter.isEmpty()) {
                try {
                    Long selectedAppId = Long.parseLong(appIdParameter);
                    request.setAttribute("selectedApplicationId", selectedAppId);
                } catch (NumberFormatException e) {
                    // ignore invalid format
                }
            }

            // Check for error messages from session
            String errorMessage =
                    (String) request.getSession().getAttribute("errorMessage");

            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                request.getSession().removeAttribute("errorMessage");
            }

            List<Interview> interviews =
                    interviewDAO.getAllInterviews();

            request.setAttribute(
                    "interviews",
                    interviews
            );

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/interviews.jsp"
            ).forward(request, response);
        }
    }


    // =====================================================
    // POST REQUEST
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // -------------------------------------------------
        // CREATE INTERVIEW
        // -------------------------------------------------

        if ("create".equals(action)) {

            Long applicationId =
                    Long.parseLong(
                            request.getParameter("applicationId")
                    );

            // Enforce business rule: Interview should only be scheduled for a SHORTLISTED application
            Application application =
                    applicationDAO.getApplicationById(applicationId);

            if (application == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Application ID " + applicationId + " does not exist!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (!"SHORTLISTED".equals(application.getStatus())) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Interview can only be scheduled for a SHORTLISTED application (Current status: "
                                + application.getStatus() + ")!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            if (interviewDAO.hasActiveInterview(applicationId)) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "An interview is already scheduled for Application ID " + applicationId + "!"
                );
                response.sendRedirect(
                        request.getContextPath() + "/interviews"
                );
                return;
            }

            LocalDate interviewDate =
                    LocalDate.parse(
                            request.getParameter("interviewDate")
                    );

            LocalTime interviewTime =
                    LocalTime.parse(
                            request.getParameter("interviewTime")
                    );

            String mode =
                    request.getParameter("mode");

            Interview interview =
                    new Interview(
                            applicationId,
                            interviewDate,
                            interviewTime,
                            mode,
                            "SCHEDULED"
                    );

            interviewDAO.createInterview(interview);

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
        }

        // -------------------------------------------------
        // UPDATE INTERVIEW
        // -------------------------------------------------

        else if ("update".equals(action)) {

            Long id =
                    Long.parseLong(
                            request.getParameter("id")
                    );

            Long applicationId =
                    Long.parseLong(
                            request.getParameter("applicationId")
                    );

            LocalDate interviewDate =
                    LocalDate.parse(
                            request.getParameter("interviewDate")
                    );

            LocalTime interviewTime =
                    LocalTime.parse(
                            request.getParameter("interviewTime")
                    );

            String mode =
                    request.getParameter("mode");

            String status =
                    request.getParameter("status");

            Interview interview =
                    new Interview(
                            applicationId,
                            interviewDate,
                            interviewTime,
                            mode,
                            status
                    );

            interview.setId(id);

            interviewDAO.updateInterview(interview);

            response.sendRedirect(
                    request.getContextPath() + "/interviews"
            );
        }
    }
}