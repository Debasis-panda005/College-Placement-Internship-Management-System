package com.college.backend.controller;

import com.college.backend.entity.Application;
import com.college.backend.service.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ApplicationViewController {

    private final ApplicationService applicationService;

    public ApplicationViewController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/applications")
    public String showApplications(Model model) {

        List<Application> applications =
                applicationService.getAllApplications();

        model.addAttribute("applications", applications);

        return "applications";
    }
}