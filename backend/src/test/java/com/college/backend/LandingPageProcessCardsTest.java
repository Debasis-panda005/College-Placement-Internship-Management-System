package com.college.backend;

import com.college.backend.servlet.ApplicationServlet;
import com.college.backend.servlet.InterviewServlet;
import com.college.backend.servlet.JobServlet;
import com.college.backend.servlet.RegisterServlet;
import jakarta.servlet.annotation.WebServlet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying the interactive clickable cards in the
 * placement process timeline on the public landing page (index.jsp).
 */
public class LandingPageProcessCardsTest {

    private static final Path INDEX_JSP_PATH = Paths.get("src/main/webapp/index.jsp");
    private static final Path CSS_PATH = Paths.get("src/main/webapp/assets/css/portal-theme.css");

    private Path resolvePath(Path relativePath) {
        if (Files.exists(relativePath)) {
            return relativePath;
        }
        Path backendRelative = Paths.get("backend").resolve(relativePath);
        if (Files.exists(backendRelative)) {
            return backendRelative;
        }
        return relativePath;
    }

    @Test
    @DisplayName("Verify index.jsp contains exactly 8 clickable process step anchor cards")
    void testEightClickableProcessCardsExist() throws IOException {
        Path jspFile = resolvePath(INDEX_JSP_PATH);
        assertTrue(Files.exists(jspFile), "index.jsp must exist at " + jspFile.toAbsolutePath());

        String content = Files.readString(jspFile);

        // Pattern matching <a ... class="process-step-card" ...>
        Pattern cardPattern = Pattern.compile("<a[^>]*class=[\"'][^\"']*process-step-card[^\"']*[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = cardPattern.matcher(content);

        List<String> cardTags = new ArrayList<>();
        while (matcher.find()) {
            cardTags.add(matcher.group());
        }

        assertEquals(8, cardTags.size(), "There must be exactly 8 clickable .process-step-card hyperlinks in index.jsp");
    }

    @Test
    @DisplayName("Verify route mapping for all 8 placement process steps")
    void testProcessCardNavigationRoutes() throws IOException {
        Path jspFile = resolvePath(INDEX_JSP_PATH);
        String content = Files.readString(jspFile);

        // Extract the .process-timeline section
        Pattern timelinePattern = Pattern.compile("<div class=[\"']process-timeline[\"']>(.*?)</div>\\s*</section>", Pattern.DOTALL);
        Matcher timelineMatcher = timelinePattern.matcher(content);
        assertTrue(timelineMatcher.find(), "process-timeline section must be present in index.jsp");

        String timelineContent = timelineMatcher.group(1);

        // Extract each <a ... class="process-step-card" ...> block
        Pattern blockPattern = Pattern.compile("<a\\s+href=[\"']([^\"']+)[\"'][^>]*class=[\"'][^\"']*process-step-card[^\"']*[\"'][^>]*>.*?<div class=[\"']step-number[\"']>(\\d+)</div>.*?<div class=[\"']step-title[\"']>([^<]+)</div>.*?</a>", Pattern.DOTALL);
        Matcher blockMatcher = blockPattern.matcher(timelineContent);

        int count = 0;
        while (blockMatcher.find()) {
            count++;
            String href = blockMatcher.group(1);
            int stepNumber = Integer.parseInt(blockMatcher.group(2));
            String stepTitle = blockMatcher.group(3).trim();

            assertEquals(count, stepNumber, "Step numbers must be strictly sequential 1 through 8");

            switch (stepNumber) {
                case 1 -> {
                    assertEquals("Student Registration", stepTitle);
                    assertTrue(href.contains("/register"), "Step 1 must navigate to /register");
                }
                case 2 -> {
                    assertEquals("Profile Completion", stepTitle);
                    assertTrue(href.contains("/register"), "Step 2 must navigate to /register");
                }
                case 3 -> {
                    assertEquals("Eligibility Check", stepTitle);
                    assertTrue(href.contains("/jobs"), "Step 3 must navigate to /jobs");
                }
                case 4 -> {
                    assertEquals("Opportunity Application", stepTitle);
                    assertTrue(href.contains("/applications") || href.contains("/jobs"), "Step 4 must navigate to /applications or /jobs");
                }
                case 5 -> {
                    assertEquals("Candidate Shortlisting", stepTitle);
                    assertTrue(href.contains("/shortlist"), "Step 5 must navigate to /shortlist");
                }
                case 6 -> {
                    assertEquals("Interview Rounds", stepTitle);
                    assertTrue(href.contains("/interviews"), "Step 6 must navigate to /interviews");
                }
                case 7 -> {
                    assertEquals("Final Selection", stepTitle);
                    assertTrue(href.contains("/applications"), "Step 7 must navigate to /applications");
                }
                case 8 -> {
                    assertEquals("Automatic PDF Report", stepTitle);
                    assertTrue(href.contains("/interviews"), "Step 8 must navigate to /interviews");
                }
                default -> fail("Unexpected step number: " + stepNumber);
            }
        }

        assertEquals(8, count, "All 8 steps must be matched in order");
    }

    @Test
    @DisplayName("Verify target servlet mappings exist for all process destinations")
    void testServletMappingsExistForTargetRoutes() {
        // Step 1 & 2: /register -> RegisterServlet
        WebServlet regAnnotation = RegisterServlet.class.getAnnotation(WebServlet.class);
        assertNotNull(regAnnotation, "RegisterServlet must have @WebServlet annotation");
        assertTrue(Arrays.asList(regAnnotation.value()).contains("/register"));

        // Step 3: /jobs -> JobServlet
        WebServlet jobAnnotation = JobServlet.class.getAnnotation(WebServlet.class);
        assertNotNull(jobAnnotation, "JobServlet must have @WebServlet annotation");
        assertTrue(Arrays.asList(jobAnnotation.value()).contains("/jobs"));

        // Step 4 & 7: /applications -> ApplicationServlet
        WebServlet appAnnotation = ApplicationServlet.class.getAnnotation(WebServlet.class);
        assertNotNull(appAnnotation, "ApplicationServlet must have @WebServlet annotation");
        assertTrue(Arrays.asList(appAnnotation.value()).contains("/applications"));

        // Step 5: /shortlist -> ApplicationServlet
        assertTrue(Arrays.asList(appAnnotation.value()).contains("/shortlist"));

        // Step 6 & 8: /interviews -> InterviewServlet
        WebServlet intAnnotation = InterviewServlet.class.getAnnotation(WebServlet.class);
        assertNotNull(intAnnotation, "InterviewServlet must have @WebServlet annotation");
        assertTrue(Arrays.asList(intAnnotation.value()).contains("/interviews"));
    }

    @Test
    @DisplayName("Verify CSS rules define cursor pointer, hover elevation, and focus-visible accessibility")
    void testCssAccessibilityAndHoverRules() throws IOException {
        Path cssFile = resolvePath(CSS_PATH);
        assertTrue(Files.exists(cssFile), "portal-theme.css must exist at " + cssFile.toAbsolutePath());

        String css = Files.readString(cssFile);

        assertTrue(css.contains(".process-step-card"), "portal-theme.css must contain .process-step-card class");
        assertTrue(css.contains("cursor: pointer"), "portal-theme.css must define cursor: pointer on card");
        assertTrue(css.contains(":focus-visible"), "portal-theme.css must define :focus-visible for keyboard navigation");
        assertTrue(css.contains(".process-step-card:hover"), "portal-theme.css must define hover state for cards");
    }

    @Test
    @DisplayName("Verify no nested interactive elements inside process cards")
    void testNoNestedInteractiveElementsInProcessCards() throws IOException {
        Path jspFile = resolvePath(INDEX_JSP_PATH);
        String content = Files.readString(jspFile);

        Pattern blockPattern = Pattern.compile("<a[^>]*class=[\"'][^\"']*process-step-card[^\"']*[\"'][^>]*>(.*?)</a>", Pattern.DOTALL);
        Matcher matcher = blockPattern.matcher(content);

        int count = 0;
        while (matcher.find()) {
            count++;
            String innerHtml = matcher.group(1);

            // Ensure no nested <a>, <button>, <input>, or <select> elements
            assertFalse(innerHtml.toLowerCase().contains("<a "), "Process card #" + count + " must not contain nested <a> links");
            assertFalse(innerHtml.toLowerCase().contains("<button"), "Process card #" + count + " must not contain nested <button>");
            assertFalse(innerHtml.toLowerCase().contains("<input"), "Process card #" + count + " must not contain nested <input>");
            assertFalse(innerHtml.toLowerCase().contains("<select"), "Process card #" + count + " must not contain nested <select>");
        }
        assertEquals(8, count);
    }
}
