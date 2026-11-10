package com.college.backend.service;

import com.college.backend.entity.Application;
import com.college.backend.entity.InterviewReport;
import com.college.backend.entity.InterviewRound;
import com.college.backend.entity.Job;
import com.college.backend.entity.Student;
import com.college.backend.entity.User;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InterviewPdfService {

    private static final Color PRIMARY_NAVY = new Color(26, 54, 93);
    private static final Color SECONDARY_BLUE = new Color(15, 43, 72);
    private static final Color ACCENT_GOLD = new Color(217, 119, 6);
    private static final Color SUCCESS_GREEN = new Color(22, 101, 52);
    private static final Color DANGER_RED = new Color(153, 27, 27);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    private static final Color BORDER_GRAY = new Color(226, 232, 240);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public byte[] generateInterviewReportPdf(InterviewReport report,
                                            Application application,
                                            Student student,
                                            User studentUser,
                                            Job job,
                                            List<InterviewRound> rounds) throws IOException {
        return generateInterviewReportPdf(report, application, student, studentUser, null, job, rounds);
    }

    public byte[] generateInterviewReportPdf(InterviewReport report,
                                            Application application,
                                            Student student,
                                            User studentUser,
                                            com.college.backend.entity.ExternalStudent externalStudent,
                                            Job job,
                                            List<InterviewRound> rounds) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterEvent());

            document.open();

            // Fonts
            Font institutionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, PRIMARY_NAVY);
            Font cellDeptFont = FontFactory.getFont(FontFactory.HELVETICA, 10, ACCENT_GOLD);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, SECONDARY_BLUE);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);

            Font sectionHeadingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
            Font tableHeadFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_DARK);

            // ==========================================
            // INSTITUTION & REPORT HEADER
            // ==========================================
            Paragraph instHeading = new Paragraph("CENTURION UNIVERSITY OF TECHNOLOGY AND MANAGEMENT", institutionFont);
            instHeading.setAlignment(Element.ALIGN_CENTER);
            document.add(instHeading);

            Paragraph deptHeading = new Paragraph("Training & Placement Cell  |  Centre for Corporate Relations", cellDeptFont);
            deptHeading.setAlignment(Element.ALIGN_CENTER);
            deptHeading.setSpacingAfter(4);
            document.add(deptHeading);

            Paragraph docTitle = new Paragraph("FINAL CANDIDATE EVALUATION & SELECTION REPORT", titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(6);
            document.add(docTitle);

            // Metadata banner
            String reportIdStr = report != null && report.getReportUuid() != null ? report.getReportUuid() : "REP-" + (application != null ? application.getId() : "N/A");
            String genDateStr = report != null && report.getGeneratedAt() != null ?
                    report.getGeneratedAt().format(DT_FMT) : java.time.LocalDateTime.now().format(DT_FMT);

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1, 1});
            metaTable.setSpacingAfter(10);

            PdfPCell cellLeft = new PdfPCell(new Phrase("Report ID: " + reportIdStr, metaFont));
            cellLeft.setBorder(Rectangle.BOTTOM);
            cellLeft.setBorderColor(BORDER_GRAY);
            cellLeft.setPaddingBottom(4);

            PdfPCell cellRight = new PdfPCell(new Phrase("Generated: " + genDateStr, metaFont));
            cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellRight.setBorder(Rectangle.BOTTOM);
            cellRight.setBorderColor(BORDER_GRAY);
            cellRight.setPaddingBottom(4);

            metaTable.addCell(cellLeft);
            metaTable.addCell(cellRight);
            document.add(metaTable);

            // ==========================================
            // SECTION 1 & 2: CANDIDATE & OPPORTUNITY DETAILS
            // ==========================================
            PdfPTable infoGrid = new PdfPTable(2);
            infoGrid.setWidthPercentage(100);
            infoGrid.setWidths(new float[]{1, 1});
            infoGrid.setSpacingAfter(10);

            // Candidate Column
            PdfPTable candidateTable = new PdfPTable(2);
            candidateTable.setWidthPercentage(100);
            candidateTable.setWidths(new float[]{3.5f, 6.5f});

            if (externalStudent != null || (application != null && application.isExternal())) {
                addSectionHeader(candidateTable, "EXTERNAL CANDIDATE INFORMATION", sectionHeadingFont, 2);
                addInfoRow(candidateTable, "Name:", externalStudent != null && externalStudent.getFullName() != null ? externalStudent.getFullName() : (studentUser != null ? studentUser.getName() : "External Candidate"), labelFont, valueFont);
                addInfoRow(candidateTable, "College:", externalStudent != null && externalStudent.getCollegeName() != null ? externalStudent.getCollegeName() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Location:", externalStudent != null && externalStudent.getCollegeLocation() != null ? externalStudent.getCollegeLocation() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Degree:", (externalStudent != null && externalStudent.getDegree() != null ? externalStudent.getDegree() : "N/A") + (externalStudent != null && externalStudent.getDepartment() != null ? " (" + externalStudent.getDepartment() + ")" : ""), labelFont, valueFont);
                addInfoRow(candidateTable, "CGPA:", externalStudent != null && externalStudent.getCgpa() != null ? externalStudent.getCgpa().toString() + " / 10.0" : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Graduation:", externalStudent != null && externalStudent.getGraduationYear() != null ? externalStudent.getGraduationYear().toString() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Email:", externalStudent != null && externalStudent.getEmail() != null ? externalStudent.getEmail() : (studentUser != null ? studentUser.getEmail() : "N/A"), labelFont, valueFont);
                addInfoRow(candidateTable, "Phone:", externalStudent != null && externalStudent.getPhone() != null ? externalStudent.getPhone() : "N/A", labelFont, valueFont);
            } else if (student != null) {
                addSectionHeader(candidateTable, "CANDIDATE INFORMATION", sectionHeadingFont, 2);
                addInfoRow(candidateTable, "Name:", studentUser != null ? studentUser.getName() : "Candidate #" + student.getId(), labelFont, valueFont);
                addInfoRow(candidateTable, "Roll Number:", student.getRollNumber() != null ? student.getRollNumber() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Department:", student.getDepartment() != null ? student.getDepartment() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "CGPA:", student.getCgpa() != null ? student.getCgpa().toString() + " / 10.0" : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Graduation:", student.getGraduationYear() != null ? student.getGraduationYear().toString() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Email:", studentUser != null ? studentUser.getEmail() : "N/A", labelFont, valueFont);
                addInfoRow(candidateTable, "Phone:", student.getPhone() != null ? student.getPhone() : "N/A", labelFont, valueFont);
            } else {
                addSectionHeader(candidateTable, "CANDIDATE INFORMATION", sectionHeadingFont, 2);
                addInfoRow(candidateTable, "Name:", studentUser != null ? studentUser.getName() : (application != null ? "Applicant #" + application.getStudentId() : "N/A"), labelFont, valueFont);
                addInfoRow(candidateTable, "Email:", studentUser != null ? studentUser.getEmail() : "N/A", labelFont, valueFont);
            }

            PdfPCell candidateWrapper = new PdfPCell(candidateTable);
            candidateWrapper.setBorder(Rectangle.NO_BORDER);
            candidateWrapper.setPaddingRight(5);
            infoGrid.addCell(candidateWrapper);

            // Opportunity Column
            PdfPTable jobTable = new PdfPTable(2);
            jobTable.setWidthPercentage(100);
            jobTable.setWidths(new float[]{3.5f, 6.5f});

            addSectionHeader(jobTable, "OPPORTUNITY DETAILS", sectionHeadingFont, 2);
            addInfoRow(jobTable, "Company:", job != null ? job.getCompany() : "N/A", labelFont, valueFont);
            addInfoRow(jobTable, "Job Title:", job != null ? job.getTitle() : "N/A", labelFont, valueFont);
            addInfoRow(jobTable, "Opportunity Type:", job != null ? job.getJobType() : "N/A", labelFont, valueFont);
            addInfoRow(jobTable, "Location:", job != null ? job.getLocation() : "N/A", labelFont, valueFont);
            addInfoRow(jobTable, "Package / Stipend:", job != null && job.getSalary() != null ? job.getSalary() : "As per company policy", labelFont, valueFont);
            addInfoRow(jobTable, "Applied On:", application != null && application.getApplicationDate() != null ?
                    application.getApplicationDate().format(DATE_FMT) : "N/A", labelFont, valueFont);
            addInfoRow(jobTable, "Application ID:", application != null ? "APP-" + application.getId() : "N/A", labelFont, valueFont);

            PdfPCell jobWrapper = new PdfPCell(jobTable);
            jobWrapper.setBorder(Rectangle.NO_BORDER);
            jobWrapper.setPaddingLeft(5);
            infoGrid.addCell(jobWrapper);

            document.add(infoGrid);

            // ==========================================
            // SECTION 3: INTERVIEW ASSESSMENT SUMMARY METRICS
            // ==========================================
            int totalRounds = report != null ? report.getTotalRounds() : (rounds != null ? rounds.size() : 0);
            int compRounds = report != null ? report.getCompletedRounds() : 0;
            int passRounds = report != null ? report.getPassedRounds() : 0;
            int failRounds = report != null ? report.getFailedRounds() : 0;
            int cancRounds = report != null ? report.getCancelledRounds() : 0;

            if (report == null && rounds != null) {
                for (InterviewRound r : rounds) {
                    if (InterviewRound.STATUS_COMPLETED.equalsIgnoreCase(r.getStatus())) {
                        compRounds++;
                    } else if (InterviewRound.STATUS_CANCELLED.equalsIgnoreCase(r.getStatus())) {
                        cancRounds++;
                    }
                    if (InterviewRound.RESULT_PASSED.equalsIgnoreCase(r.getResult())) {
                        passRounds++;
                    } else if (InterviewRound.RESULT_FAILED.equalsIgnoreCase(r.getResult())) {
                        failRounds++;
                    }
                }
            }

            PdfPTable summaryTable = new PdfPTable(5);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(10);
            addSectionHeader(summaryTable, "INTERVIEW ASSESSMENT SUMMARY METRICS", sectionHeadingFont, 5);

            addMetricCell(summaryTable, "Total Rounds", String.valueOf(totalRounds), PRIMARY_NAVY);
            addMetricCell(summaryTable, "Completed", String.valueOf(compRounds), SECONDARY_BLUE);
            addMetricCell(summaryTable, "Passed", String.valueOf(passRounds), SUCCESS_GREEN);
            addMetricCell(summaryTable, "Failed", String.valueOf(failRounds), failRounds > 0 ? DANGER_RED : TEXT_MUTED);
            addMetricCell(summaryTable, "Cancelled", String.valueOf(cancRounds), cancRounds > 0 ? ACCENT_GOLD : TEXT_MUTED);

            document.add(summaryTable);

            // ==========================================
            // SECTION 4: ROUND-BY-ROUND EVALUATION TABLE
            // ==========================================
            PdfPTable roundsTable = new PdfPTable(7);
            roundsTable.setWidthPercentage(100);
            roundsTable.setWidths(new float[]{0.8f, 2.2f, 1.8f, 1.2f, 1.8f, 1.2f, 3.0f});
            roundsTable.setSpacingAfter(12);

            addSectionHeader(roundsTable, "ROUND-BY-ROUND EVALUATION BREAKDOWN", sectionHeadingFont, 7);

            // Headers
            addTableHeader(roundsTable, "Rnd", tableHeadFont);
            addTableHeader(roundsTable, "Round Name", tableHeadFont);
            addTableHeader(roundsTable, "Date & Time", tableHeadFont);
            addTableHeader(roundsTable, "Mode", tableHeadFont);
            addTableHeader(roundsTable, "Interviewer", tableHeadFont);
            addTableHeader(roundsTable, "Result", tableHeadFont);
            addTableHeader(roundsTable, "Feedback & Remarks", tableHeadFont);

            if (rounds == null || rounds.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No interview rounds recorded.", tableCellFont));
                emptyCell.setColspan(7);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(8);
                roundsTable.addCell(emptyCell);
            } else {
                boolean alternate = false;
                for (InterviewRound r : rounds) {
                    Color rowBg = alternate ? LIGHT_BG : Color.WHITE;
                    alternate = !alternate;

                    addTableCell(roundsTable, String.valueOf(r.getRoundNumber()), tableCellFont, rowBg, Element.ALIGN_CENTER);
                    addTableCell(roundsTable, r.getRoundName() != null ? r.getRoundName() : "Round " + r.getRoundNumber(), tableCellFont, rowBg, Element.ALIGN_LEFT);

                    String dateTimeStr = "";
                    if (r.getInterviewDate() != null) {
                        dateTimeStr = r.getInterviewDate().format(DATE_FMT);
                    }
                    if (r.getInterviewTime() != null) {
                        dateTimeStr += "\n" + r.getInterviewTime().format(TIME_FMT);
                    }
                    addTableCell(roundsTable, dateTimeStr, tableCellFont, rowBg, Element.ALIGN_CENTER);
                    addTableCell(roundsTable, r.getMode() != null ? r.getMode() : "ONLINE", tableCellFont, rowBg, Element.ALIGN_CENTER);
                    addTableCell(roundsTable, r.getInterviewerName() != null && !r.getInterviewerName().trim().isEmpty() ?
                            r.getInterviewerName() : "Placement Panel", tableCellFont, rowBg, Element.ALIGN_LEFT);

                    // Result with color
                    String resStr = r.getResult() != null ? r.getResult() : "PENDING";
                    Color resColor = TEXT_MUTED;
                    if (InterviewRound.RESULT_PASSED.equalsIgnoreCase(resStr)) {
                        resColor = SUCCESS_GREEN;
                    } else if (InterviewRound.RESULT_FAILED.equalsIgnoreCase(resStr)) {
                        resColor = DANGER_RED;
                    }
                    Font resFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, resColor);
                    addTableCell(roundsTable, resStr, resFont, rowBg, Element.ALIGN_CENTER);

                    // Remarks & feedback
                    StringBuilder fb = new StringBuilder();
                    if (r.getFeedback() != null && !r.getFeedback().trim().isEmpty()) {
                        fb.append(r.getFeedback().trim());
                    }
                    if (r.getRemarks() != null && !r.getRemarks().trim().isEmpty()) {
                        if (fb.length() > 0) fb.append("\n---\n");
                        fb.append("Remarks: ").append(r.getRemarks().trim());
                    }
                    if (r.getCancellationReason() != null && !r.getCancellationReason().trim().isEmpty()) {
                        if (fb.length() > 0) fb.append("\n");
                        fb.append("Cancellation: ").append(r.getCancellationReason().trim());
                    }
                    if (fb.length() == 0) {
                        fb.append("Evaluation completed.");
                    }
                    addTableCell(roundsTable, fb.toString(), tableCellFont, rowBg, Element.ALIGN_LEFT);
                }
            }

            document.add(roundsTable);

            // ==========================================
            // SECTION 5: FINAL DETERMINATION & DECISION
            // ==========================================
            String finalDecision = report != null && report.getFinalDecision() != null ?
                    report.getFinalDecision() : (application != null ? application.getStatus() : "EVALUATION_COMPLETE");

            Color decisionColor = SUCCESS_GREEN;
            String decisionBanner = "FINAL SELECTION DETERMINATION: SELECTED";
            if ("REJECTED".equalsIgnoreCase(finalDecision)) {
                decisionColor = DANGER_RED;
                decisionBanner = "FINAL SELECTION DETERMINATION: REJECTED";
            }

            PdfPTable decisionTable = new PdfPTable(1);
            decisionTable.setWidthPercentage(100);
            decisionTable.setSpacingAfter(15);

            PdfPCell decHeader = new PdfPCell(new Phrase(decisionBanner, sectionHeadingFont));
            decHeader.setBackgroundColor(decisionColor);
            decHeader.setPadding(6);
            decHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            decisionTable.addCell(decHeader);

            PdfPTable decBody = new PdfPTable(2);
            decBody.setWidthPercentage(100);
            decBody.setWidths(new float[]{3.0f, 7.0f});

            String decDate = report != null && report.getDecisionDate() != null ?
                    report.getDecisionDate().format(DATE_FMT) : java.time.LocalDate.now().format(DATE_FMT);
            String coordRemarks = report != null && report.getCoordinatorRemarks() != null ?
                    report.getCoordinatorRemarks() : "Candidate evaluated according to institutional recruitment criteria.";
            String overallFeedback = report != null && report.getOverallFeedback() != null ?
                    report.getOverallFeedback() : "All configured evaluation parameters fulfilled.";
            String authOfficer = report != null && report.getGeneratedBy() != null ? report.getGeneratedBy() : "Head - Training & Placement Cell";

            addInfoRow(decBody, "Authorized Determination:", finalDecision, labelFont,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, decisionColor));
            addInfoRow(decBody, "Decision Record Date:", decDate, labelFont, valueFont);
            addInfoRow(decBody, "Coordinator Remarks:", coordRemarks, labelFont, valueFont);
            addInfoRow(decBody, "Overall Evaluation:", overallFeedback, labelFont, valueFont);
            addInfoRow(decBody, "Signed & Verified By:", authOfficer, labelFont, valueFont);

            PdfPCell decBodyWrapper = new PdfPCell(decBody);
            decBodyWrapper.setBorder(Rectangle.BOX);
            decBodyWrapper.setBorderColor(decisionColor);
            decBodyWrapper.setPadding(0);
            decisionTable.addCell(decBodyWrapper);

            document.add(decisionTable);

            // ==========================================
            // SECTION 6: INSTITUTIONAL SIGNATURE BLOCK
            // ==========================================
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setWidths(new float[]{1, 1});

            PdfPCell sigLeft = new PdfPCell();
            sigLeft.setBorder(Rectangle.NO_BORDER);
            sigLeft.addElement(new Paragraph("____________________________________", labelFont));
            sigLeft.addElement(new Paragraph("Candidate Acknowledgement", labelFont));
            sigLeft.addElement(new Paragraph("Date: " + java.time.LocalDate.now().format(DATE_FMT), metaFont));

            PdfPCell sigRight = new PdfPCell();
            sigRight.setBorder(Rectangle.NO_BORDER);
            sigRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph sigLine = new Paragraph("____________________________________", labelFont);
            sigLine.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(sigLine);
            Paragraph sigTitle = new Paragraph("Training & Placement Officer", labelFont);
            sigTitle.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(sigTitle);
            Paragraph sealNotice = new Paragraph("Institutional Seal & Sign-off", metaFont);
            sealNotice.setAlignment(Element.ALIGN_RIGHT);
            sigRight.addElement(sealNotice);

            sigTable.addCell(sigLeft);
            sigTable.addCell(sigRight);
            document.add(sigTable);

            document.close();

        } catch (DocumentException e) {
            throw new IOException("Failed to generate PDF document: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addSectionHeader(PdfPTable table, String title, Font font, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(PRIMARY_NAVY);
        cell.setColspan(colspan);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBackgroundColor(LIGHT_BG);
        lCell.setPadding(4);
        lCell.setBorderColor(BORDER_GRAY);

        PdfPCell vCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        vCell.setPadding(4);
        vCell.setBorderColor(BORDER_GRAY);

        table.addCell(lCell);
        table.addCell(vCell);
    }

    private void addMetricCell(PdfPTable table, String title, String value, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_BG);
        cell.setBorderColor(BORDER_GRAY);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, color);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 7, TEXT_MUTED);

        Paragraph valP = new Paragraph(value, valFont);
        valP.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(valP);

        Paragraph titleP = new Paragraph(title.toUpperCase(), titleFont);
        titleP.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(titleP);

        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(SECONDARY_BLUE);
        cell.setPadding(5);
        cell.setBorderColor(BORDER_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(4);
        cell.setBorderColor(BORDER_GRAY);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    // Page event for header/footer and page numbers
    private static class HeaderFooterEvent extends PdfPageEventHelper {
        private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            footer.setTotalWidth(document.right() - document.left());
            footer.setWidthPercentage(100);

            PdfPCell leftCell = new PdfPCell(new Phrase("CONFIDENTIAL – Centurion University Placement & Training Cell Official Assessment Record", footerFont));
            leftCell.setBorder(Rectangle.TOP);
            leftCell.setBorderColor(BORDER_GRAY);
            leftCell.setPaddingTop(4);

            PdfPCell rightCell = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setBorder(Rectangle.TOP);
            rightCell.setBorderColor(BORDER_GRAY);
            rightCell.setPaddingTop(4);

            footer.addCell(leftCell);
            footer.addCell(rightCell);

            footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
        }
    }
}
