<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>Application Details</title>

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

        .selected {
            color: #198754;
        }

        .rejected {
            color: #dc3545;
        }

        .notice-empty {
            color: #6c757d;
            font-style: italic;
            margin-bottom: 15px;
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

        .btn-interview {
            background-color: #0d6efd;
            color: white;
        }

        .btn-interview:hover {
            background-color: #0b5ed7;
        }

        .btn-history {
            background-color: #6f42c1;
            color: white;
        }

        .btn-history:hover {
            background-color: #59359a;
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

    <h1>Application Details</h1>

    <!-- Application Information Section -->
    <div class="section-card">

        <h2>Application Information</h2>

        <div class="detail-row">
            <div class="detail-label">Application ID:</div>
            <div class="detail-value">${application.id}</div>
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
            <div class="detail-label">Application Status:</div>
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

    <!-- Related Interview Information Section -->
    <div class="section-card">

        <h2>Interview Information</h2>

        <c:choose>

            <c:when test="${not empty interview}">

                <div class="detail-row">
                    <div class="detail-label">Interview ID:</div>
                    <div class="detail-value">${interview.id}</div>
                </div>

                <div class="detail-row">
                    <div class="detail-label">Interview Date:</div>
                    <div class="detail-value">${interview.interviewDate}</div>
                </div>

                <div class="detail-row">
                    <div class="detail-label">Interview Time:</div>
                    <div class="detail-value">${interview.interviewTime}</div>
                </div>

                <div class="detail-row">
                    <div class="detail-label">Interview Mode:</div>
                    <div class="detail-value">${interview.mode}</div>
                </div>

                <div class="detail-row">
                    <div class="detail-label">Interview Status:</div>
                    <div class="detail-value">
                        <span class="status">${interview.status}</span>
                    </div>
                </div>

            </c:when>

            <c:otherwise>

                <p class="notice-empty">
                    No interview scheduled for this application yet.
                </p>

                <c:if test="${application.status == 'SHORTLISTED'}">
                    <a class="btn btn-interview"
                       href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}">
                        Schedule Interview
                    </a>
                </c:if>

            </c:otherwise>

        </c:choose>

    </div>

    <!-- Navigation Actions -->
    <div class="button-bar">
        <a class="btn btn-history"
           href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
            View Status History
        </a>
        <a class="btn btn-back"
           href="${pageContext.request.contextPath}/applications">
            Back to Applications
        </a>
    </div>

</div>

</body>
</html>
