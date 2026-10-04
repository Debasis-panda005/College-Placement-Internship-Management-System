package com.college.backend.dao;

import com.college.backend.entity.Application;

import java.time.LocalDate;
import java.util.List;

public class ApplicationDAOTest {

    public static void main(String[] args) {

        ApplicationDAO applicationDAO = new ApplicationDAO();

        // 1. Create Application
        Application application = new Application(
                1L,
                1L,
                LocalDate.now(),
                "APPLIED"
        );

        boolean created = applicationDAO.createApplication(application);

        System.out.println("Create Application: " + created);


        // 2. Get All Applications
        List<Application> applications =
                applicationDAO.getAllApplications();

        System.out.println("\nAll Applications:");

        for (Application app : applications) {

            System.out.println(
                    "ID: " + app.getId()
                            + " | Student ID: " + app.getStudentId()
                            + " | Job ID: " + app.getJobId()
                            + " | Date: " + app.getApplicationDate()
                            + " | Status: " + app.getStatus()
            );
        }


        // 3. Shortlist Application
        boolean shortlisted =
                applicationDAO.shortlistApplication(1L);

        System.out.println(
                "\nShortlist Application: " + shortlisted
        );


        // 4. Verify Shortlisted Application
        Application shortlistedApplication =
                applicationDAO.getApplicationById(1L);

        if (shortlistedApplication != null) {

            System.out.println(
                    "Application ID: "
                            + shortlistedApplication.getId()
                            + " | Status: "
                            + shortlistedApplication.getStatus()
            );

        } else {

            System.out.println(
                    "Application not found."
            );
        }
    }
}