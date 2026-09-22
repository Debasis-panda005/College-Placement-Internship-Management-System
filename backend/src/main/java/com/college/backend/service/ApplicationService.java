package com.college.backend.service;

import com.college.backend.entity.Application;
import com.college.backend.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    // Create a new application
    public Application createApplication(Application application) {
        return applicationRepository.save(application);
    }

    // Get all applications
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    // Get application by ID
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    // Update application
    public Application updateApplication(Long id, Application updatedApplication) {

        Application existingApplication =
                applicationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Application not found with id: " + id));

        existingApplication.setStudentId(updatedApplication.getStudentId());
        existingApplication.setJobId(updatedApplication.getJobId());
        existingApplication.setApplicationDate(updatedApplication.getApplicationDate());
        existingApplication.setStatus(updatedApplication.getStatus());

        return applicationRepository.save(existingApplication);
    }

    // Delete application
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }
}