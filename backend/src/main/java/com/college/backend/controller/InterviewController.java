package com.college.backend.controller;

import com.college.backend.entity.Interview;
import com.college.backend.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    // Create interview
    @PostMapping
    public Interview createInterview(@RequestBody Interview interview) {
        return interviewService.createInterview(interview);
    }

    // Get all interviews
    @GetMapping
    public List<Interview> getAllInterviews() {
        return interviewService.getAllInterviews();
    }

    // Get interview by ID
    @GetMapping("/{id}")
    public ResponseEntity<Interview> getInterviewById(@PathVariable Long id) {
        return interviewService.getInterviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update interview
    @PutMapping("/{id}")
    public ResponseEntity<Interview> updateInterview(
            @PathVariable Long id,
            @RequestBody Interview interview) {

        Interview updatedInterview =
                interviewService.updateInterview(id, interview);

        if (updatedInterview != null) {
            return ResponseEntity.ok(updatedInterview);
        }

        return ResponseEntity.notFound().build();
    }

    // Delete interview
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return ResponseEntity.noContent().build();
    }
}