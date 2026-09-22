package com.college.backend.service;

import com.college.backend.entity.Interview;
import com.college.backend.repository.InterviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;

    public InterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    // Create interview
    public Interview createInterview(Interview interview) {
        return interviewRepository.save(interview);
    }

    // Get all interviews
    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    // Get interview by ID
    public Optional<Interview> getInterviewById(Long id) {
        return interviewRepository.findById(id);
    }

    // Update interview
    public Interview updateInterview(Long id, Interview interview) {
        Optional<Interview> existingInterview =
                interviewRepository.findById(id);

        if (existingInterview.isPresent()) {
            Interview existing = existingInterview.get();

            existing.setApplicationId(interview.getApplicationId());
            existing.setInterviewDate(interview.getInterviewDate());
            existing.setInterviewTime(interview.getInterviewTime());
            existing.setMode(interview.getMode());
            existing.setStatus(interview.getStatus());

            return interviewRepository.save(existing);
        }

        return null;
    }

    // Delete interview
    public void deleteInterview(Long id) {
        interviewRepository.deleteById(id);
    }
}