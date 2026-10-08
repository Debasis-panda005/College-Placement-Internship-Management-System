<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>Application Status History - #${application.id}</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 80%;
            max-width: 850px;
            margin: 30px auto;
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            text-align: center;
            color: #333;
            margin-bottom: 25px;
        }

        .nav-links {
            text-align: center;
            margin-bottom: 25px;
            padding: 10px;
            background: #f8f9fa;
            border-radius: 6px;
        }

        .nav-links a {
            margin: 0 12px;
            text-decoration: none;
            color: #0066cc;
            font-weight: 500;
        }

        .nav-links a.active {
            color: #333;
            font-weight: bold;
        }

        .section-card {
            background-color: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 8px;
            padding: 22px;
            margin-bottom: 25px;
        }

        .section-card h2 {
            margin-top: 0;
            margin-bottom: 18px;
            color: #333;
            font-size: 18px;
            border-bottom: 2px solid #dee2e6;
            padding-bottom: 10px;
        }

        .detail-row {
            display: flex;
            padding: 10px 0;
            border-bottom: 1px solid #e9ecef;
        }

        .detail-row:last-child {
            border-bottom: none;
        }

        .detail-label {
            width: 220px;
            font-weight: bold;
            color: #495057;
        }

        .detail-value {
            flex: 1;
            color: #212529;
        }

        .status {
            font-weight: bold;
        }

        .applied {
            color: #0066cc;
        }

        .shortlisted {
            color: #e67e22;
        }

        .interview {
            color: #0d6efd;
        }

        .selected {
            color: #198754;
        }

        .rejected {
            color: #dc3545;
        }

        /* Vertical Timeline Styles */
        .timeline {
            position: relative;
            padding: 20px 10px 10px 35px;
            margin-left: 10px;
        }

        .timeline::before {
            content: '';
            position: absolute;
            top: 25px;
            bottom: 25px;
            left: 14px;
            width: 2px;
            background: #cbd5e1;
        }

        .timeline-item {
            position: relative;
            margin-bottom: 28px;
        }

        .timeline-item:last-child {
            margin-bottom: 0;
        }

        .timeline-marker {
            position: absolute;
            left: -29px;
            top: 6px;
            width: 16px;
            height: 16px;
            border-radius: 50%;
            background-color: white;
            border: 3px solid #6c757d;
            box-shadow: 0 0 0 2px rgba(0,0,0,0.06);
            z-index: 1;
        }

        .timeline-marker.applied {
            border-color: #0066cc;
            background-color: #0066cc;
        }

        .timeline-marker.shortlisted {
            border-color: #e67e22;
            background-color: #e67e22;
        }

        .timeline-marker.interview {
            border-color: #0d6efd;
            background-color: #0d6efd;
        }

        .timeline-marker.selected {
            border-color: #198754;
            background-color: #198754;
        }

        .timeline-marker.rejected {
            border-color: #dc3545;
            background-color: #dc3545;
        }

        .timeline-content {
            background: #ffffff;
            border: 1px solid #dee2e6;
            border-left: 4px solid #6c757d;
            border-radius: 6px;
            padding: 14px 18px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .timeline-content.applied {
            border-left-color: #0066cc;
        }

        .timeline-content.shortlisted {
            border-left-color: #e67e22;
        }

        .timeline-content.interview {
            border-left-color: #0d6efd;
        }

        .timeline-content.selected {
            border-left-color: #198754;
        }

        .timeline-content.rejected {
            border-left-color: #dc3545;
        }

        .timeline-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 6px;
        }

        .timeline-status {
            font-weight: bold;
            font-size: 15px;
            letter-spacing: 0.4px;
        }

        .timeline-time {
            font-size: 13px;
            color: #6c757d;
            font-weight: 500;
        }

        .timeline-desc {
            font-size: 14px;
            color: #495057;
            margin: 0;
        }

        .empty-history {
            text-align: center;
            padding: 30px;
            color: #6c757d;
            font-style: italic;
            background: #ffffff;
            border: 1px dashed #cbd5e1;
            border-radius: 6px;
        }

        .button-bar {
            margin-top: 25px;
            display: flex;
            gap: 12px;
            align-items: center;
        }

        .btn {
            display: inline-block;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            text-decoration: none;
            font-size: 14px;
            cursor: pointer;
        }

        .btn-back {
            background-color: #6c757d;
            color: white;
        }

        .btn-back:hover {
            background-color: #5a6268;
        }
    </style>
</head>

<body>

<div class="container">

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Application History</h1>

    <!-- Application Information Section -->
    <div class="section-card">

        <h2>Application Information</h2>

        <div class="detail-row">
            <div class="detail-label">Application ID:</div>
            <div class="detail-value">#${application.id}</div>
        </div>

        <div class="detail-row">
            <div class="detail-label">Student ID:</div>
            <div class="detail-value">${application.studentId}</div>
        </div>

        <div class="detail-row">
            <div class="detail-label">Job ID:</div>
            <div class="detail-value">${application.jobId}</div>
        </div>

        <div class="detail-row">
            <div class="detail-label">Application Date:</div>
            <div class="detail-value">${application.applicationDate}</div>
        </div>

        <div class="detail-row">
            <div class="detail-label">Current Status:</div>
            <div class="detail-value">
                <c:choose>
                    <c:when test="${application.status == 'APPLIED'}">
                        <span class="status applied">APPLIED</span>
                    </c:when>
                    <c:when test="${application.status == 'SHORTLISTED'}">
                        <span class="status shortlisted">SHORTLISTED</span>
                    </c:when>
                    <c:when test="${application.status == 'SELECTED'}">
                        <span class="status selected">SELECTED</span>
                    </c:when>
                    <c:when test="${application.status == 'REJECTED'}">
                        <span class="status rejected">REJECTED</span>
                    </c:when>
                    <c:otherwise>
                        <span class="status">${application.status}</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

    <!-- Timeline Section -->
    <div class="section-card">

        <h2>Status History Timeline</h2>

        <c:choose>

            <c:when test="${not empty statusHistory}">

                <div class="timeline">

                    <c:forEach var="history" items="${statusHistory}">

                        <div class="timeline-item">

                            <div class="timeline-marker ${history.statusClass}"></div>

                            <div class="timeline-content ${history.statusClass}">

                                <div class="timeline-header">
                                    <span class="timeline-status ${history.statusClass}">
                                        ${history.displayStatus}
                                    </span>
                                    <span class="timeline-time">
                                        ${history.formattedChangedAt}
                                    </span>
                                </div>

                                <p class="timeline-desc">
                                    ${history.statusDescription}
                                </p>

                            </div>

                        </div>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>

                <div class="empty-history">
                    No status history available for this application.
                </div>

            </c:otherwise>

        </c:choose>

    </div>

    <!-- Navigation Actions -->
    <div class="button-bar">
        <a class="btn btn-back"
           href="${pageContext.request.contextPath}/applications">
            &larr; Back to Applications
        </a>
    </div>

</div>

</body>
</html>
