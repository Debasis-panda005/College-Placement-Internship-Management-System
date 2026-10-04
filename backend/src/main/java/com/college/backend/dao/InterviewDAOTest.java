package com.college.backend.dao;

import com.college.backend.entity.Interview;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class InterviewDAOTest {

    public static void main(String[] args) {

        InterviewDAO interviewDAO = new InterviewDAO();

        // 1. Create Interview
        Interview interview = new Interview(
                1L,
                LocalDate.now(),
                LocalTime.of(10, 30),
                "ONLINE",
                "SCHEDULED"
        );

        boolean created = interviewDAO.createInterview(interview);

        System.out.println("Create Interview: " + created);

        // 2. Get All Interviews
        List<Interview> interviews =
                interviewDAO.getAllInterviews();

        System.out.println("\nAll Interviews:");

        for (Interview item : interviews) {

            System.out.println(
                    "ID: " + item.getId()
                            + " | Application ID: " + item.getApplicationId()
                            + " | Date: " + item.getInterviewDate()
                            + " | Time: " + item.getInterviewTime()
                            + " | Mode: " + item.getMode()
                            + " | Status: " + item.getStatus()
            );
        }

        // 3. Get Interview By ID
        Interview foundInterview =
                interviewDAO.getInterviewById(1L);

        System.out.println("\nInterview By ID:");

        if (foundInterview != null) {

            System.out.println(
                    "ID: " + foundInterview.getId()
                            + " | Application ID: "
                            + foundInterview.getApplicationId()
                            + " | Date: "
                            + foundInterview.getInterviewDate()
                            + " | Time: "
                            + foundInterview.getInterviewTime()
                            + " | Mode: "
                            + foundInterview.getMode()
                            + " | Status: "
                            + foundInterview.getStatus()
            );

        } else {
            System.out.println("Interview not found.");
        }

        // 4. Update Interview
        if (foundInterview != null) {

            foundInterview.setInterviewDate(
                    LocalDate.now().plusDays(1)
            );

            foundInterview.setInterviewTime(
                    LocalTime.of(11, 0)
            );

            foundInterview.setMode("OFFLINE");
            foundInterview.setStatus("COMPLETED");

            boolean updated =
                    interviewDAO.updateInterview(foundInterview);

            System.out.println(
                    "\nUpdate Interview: " + updated
            );
        }

        // 5. Delete Interview
        boolean deleted =
                interviewDAO.deleteInterview(1L);

        System.out.println(
                "Delete Interview: " + deleted
        );
    }
}