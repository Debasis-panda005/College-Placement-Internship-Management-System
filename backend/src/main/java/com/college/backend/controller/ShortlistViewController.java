package com.college.backend.controller;

import com.college.backend.entity.Application;
import com.college.backend.service.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ShortlistViewController {

    private final ApplicationService applicationService;

    public ShortlistViewController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/shortlist")
    public String showShortlistedApplications(Model model) {

        List<Application> applications =
                applicationService.getAllApplications();

        model.addAttribute("applications", applications);

        return "shortlist";
    }
}