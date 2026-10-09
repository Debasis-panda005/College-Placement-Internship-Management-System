package com.college.backend;

import com.college.backend.entity.Application;
import com.college.backend.entity.ApplicationStatusHistory;
import com.college.backend.entity.Interview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BackendApplicationTests {

    // =====================================================
    // 1. APPLICATION ENTITY UNIT TESTS
    // =====================================================

    @Test
    @DisplayName("Application entity constructor and getters/setters verify correctly")
    void testApplicationEntity() {
        LocalDate now = LocalDate.of(2026, 10, 9);
        Application application = new Application(101L, 201L, now, "APPLIED");

        assertNull(application.getId());
        assertEquals(101L, application.getStudentId());
        assertEquals(201L, application.getJobId());
        assertEquals(now, application.getApplicationDate());
        assertEquals("APPLIED", application.getStatus());

        application.setId(1L);
        application.setStatus("SHORTLISTED");
        assertEquals(1L, application.getId());
        assertEquals("SHORTLISTED", application.getStatus());
    }

    // =====================================================
    // 2. INTERVIEW ENTITY UNIT TESTS
    // =====================================================

    @Test
    @DisplayName("Interview entity constructor and getters/setters verify correctly")
    void testInterviewEntity() {
        LocalDate interviewDate = LocalDate.of(2026, 10, 15);
        LocalTime interviewTime = LocalTime.of(10, 30);
        Interview interview = new Interview(1L, interviewDate, interviewTime, "ONLINE", "SCHEDULED");

        assertNull(interview.getId());
        assertEquals(1L, interview.getApplicationId());
        assertEquals(interviewDate, interview.getInterviewDate());
        assertEquals(interviewTime, interview.getInterviewTime());
        assertEquals("ONLINE", interview.getMode());
        assertEquals("SCHEDULED", interview.getStatus());

        interview.setId(5L);
        interview.setStatus("COMPLETED");
        interview.setMode("OFFLINE");
        assertEquals(5L, interview.getId());
        assertEquals("COMPLETED", interview.getStatus());
        assertEquals("OFFLINE", interview.getMode());
    }

    // =====================================================
    // 3. APPLICATION STATUS HISTORY DISPLAY STATUS TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getDisplayStatus transforms interview events properly")
    void testApplicationStatusHistoryDisplayStatus() {
        ApplicationStatusHistory scheduled = new ApplicationStatusHistory(1L, "INTERVIEW_SCHEDULED");
        assertEquals("INTERVIEW SCHEDULED", scheduled.getDisplayStatus());

        ApplicationStatusHistory rescheduled = new ApplicationStatusHistory(1L, "INTERVIEW_RESCHEDULED");
        assertEquals("INTERVIEW RESCHEDULED", rescheduled.getDisplayStatus());

        ApplicationStatusHistory completed = new ApplicationStatusHistory(1L, "INTERVIEW_COMPLETED");
        assertEquals("INTERVIEW COMPLETED", completed.getDisplayStatus());

        ApplicationStatusHistory cancelled = new ApplicationStatusHistory(1L, "INTERVIEW_CANCELLED");
        assertEquals("INTERVIEW CANCELLED", cancelled.getDisplayStatus());

        ApplicationStatusHistory custom = new ApplicationStatusHistory(1L, "SHORTLISTED");
        assertEquals("SHORTLISTED", custom.getDisplayStatus());

        ApplicationStatusHistory nullStatus = new ApplicationStatusHistory(1L, null);
        assertEquals("", nullStatus.getDisplayStatus());
    }

    // =====================================================
    // 4. APPLICATION STATUS HISTORY TIMELINE DESCRIPTION TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getStatusDescription returns user-friendly descriptions")
    void testApplicationStatusHistoryStatusDescription() {
        ApplicationStatusHistory history = new ApplicationStatusHistory();

        history.setStatus("APPLIED");
        assertEquals("Application created", history.getStatusDescription());

        history.setStatus("SHORTLISTED");
        assertEquals("Candidate shortlisted", history.getStatusDescription());

        history.setStatus("INTERVIEW_SCHEDULED");
        assertEquals("Interview scheduled", history.getStatusDescription());

        history.setStatus("INTERVIEW_RESCHEDULED");
        assertEquals("Interview rescheduled", history.getStatusDescription());

        history.setStatus("INTERVIEW_COMPLETED");
        assertEquals("Interview completed", history.getStatusDescription());

        history.setStatus("INTERVIEW_CANCELLED");
        assertEquals("Interview cancelled", history.getStatusDescription());

        history.setStatus("SELECTED");
        assertEquals("Candidate selected", history.getStatusDescription());

        history.setStatus("REJECTED");
        assertEquals("Candidate rejected", history.getStatusDescription());

        history.setStatus(null);
        assertEquals("", history.getStatusDescription());
    }

    // =====================================================
    // 5. APPLICATION STATUS HISTORY CSS CLASS TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getStatusClass maps to correct CSS badge classes")
    void testApplicationStatusHistoryStatusClass() {
        ApplicationStatusHistory history = new ApplicationStatusHistory();

        history.setStatus("APPLIED");
        assertEquals("applied", history.getStatusClass());

        history.setStatus("SHORTLISTED");
        assertEquals("shortlisted", history.getStatusClass());

        history.setStatus("INTERVIEW_SCHEDULED");
        assertEquals("interview", history.getStatusClass());

        history.setStatus("INTERVIEW_RESCHEDULED");
        assertEquals("interview", history.getStatusClass());

        history.setStatus("INTERVIEW_COMPLETED");
        assertEquals("interview", history.getStatusClass());

        history.setStatus("INTERVIEW_CANCELLED");
        assertEquals("cancelled", history.getStatusClass());

        history.setStatus("SELECTED");
        assertEquals("selected", history.getStatusClass());

        history.setStatus("REJECTED");
        assertEquals("rejected", history.getStatusClass());

        history.setStatus(null);
        assertEquals("applied", history.getStatusClass());
    }

    // =====================================================
    // 6. APPLICATION STATUS HISTORY TIMESTAMP FORMATTING TESTS
    // =====================================================

    @Test
    @DisplayName("ApplicationStatusHistory getFormattedChangedAt formats date and time correctly")
    void testApplicationStatusHistoryFormattedChangedAt() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 9, 14, 30);
        ApplicationStatusHistory history = new ApplicationStatusHistory(1L, "APPLIED", time);

        assertEquals("09 oct 2026, 02:30 pm", history.getFormattedChangedAt().toLowerCase());

        history.setChangedAt(null);
        assertEquals("", history.getFormattedChangedAt());
    }
}
