<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Job Details - <c:out value="${job.title}"/></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 700px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
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

        h1 {
            color: #333;
            margin-top: 0;
            margin-bottom: 20px;
            font-size: 24px;
        }

        .detail-row {
            display: flex;
            padding: 12px 0;
            border-bottom: 1px solid #eee;
        }

        .detail-label {
            width: 160px;
            font-weight: bold;
            color: #495057;
        }

        .detail-value {
            flex: 1;
            color: #212529;
        }

        .status-active {
            color: #28a745;
            font-weight: bold;
        }

        .status-closed {
            color: #dc3545;
            font-weight: bold;
        }

        .notice-box {
            background-color: #e8f4fd;
            border: 1px solid #b6d4fe;
            border-radius: 6px;
            padding: 16px;
            margin-top: 25px;
            margin-bottom: 20px;
        }

        .notice-box h3 {
            margin-top: 0;
            margin-bottom: 8px;
            font-size: 16px;
            color: #084298;
        }

        .notice-box p {
            margin: 0 0 10px 0;
            font-size: 14px;
            color: #084298;
            line-height: 1.5;
        }

        .actions-bar {
            margin-top: 25px;
            display: flex;
            gap: 12px;
            align-items: center;
        }

        .btn {
            display: inline-block;
            padding: 10px 18px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
            font-size: 14px;
        }

        .btn-primary {
            background-color: #1976d2;
            color: white;
        }

        .btn-primary:hover {
            background-color: #125aa0;
        }

        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }

        .btn-secondary:hover {
            background-color: #5a6268;
        }

        .btn-manage-app {
            background-color: #28a745;
            color: white;
        }

        .btn-manage-app:hover {
            background-color: #218838;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/jobs">Jobs &amp; Internships</a> |
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Job / Internship Details</h1>

    <div class="detail-row">
        <div class="detail-label">Job ID:</div>
        <div class="detail-value"><strong><c:out value="${job.id}"/></strong></div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Title:</div>
        <div class="detail-value"><c:out value="${job.title}"/></div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Company:</div>
        <div class="detail-value"><c:out value="${job.company}"/></div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Location:</div>
        <div class="detail-value"><c:out value="${job.location}"/></div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Description:</div>
        <div class="detail-value">
            <c:choose>
                <c:when test="${not empty job.description}">
                    <c:out value="${job.description}"/>
                </c:when>
                <c:otherwise>
                    <span style="color: #888;">No description provided.</span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Salary / Stipend:</div>
        <div class="detail-value"><c:out value="${job.salary}"/></div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Job Type:</div>
        <div class="detail-value"><c:out value="${job.jobType}"/></div>
    </div>

    <div class="detail-row">
        <div class="detail-label">Status:</div>
        <div class="detail-value">
            <c:choose>
                <c:when test="${job.status == 'Active'}">
                    <span class="status-active">Active</span>
                </c:when>
                <c:otherwise>
                    <span class="status-closed">Closed</span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- STUDENT APPLICATION FLOW NOTICE (AUTHENTICATION DEPENDENCY) -->
    <div class="notice-box">
        <h3>Student Application Workflow</h3>
        <p>
            <strong>Authentication Dependency:</strong> Direct student self-application requires verified
            session authentication from the upcoming <code>feature/auth-student</code> module to ensure student identity
            is never trusted from unverified browser input.
        </p>
        <p>
            Coordinators and administrators can manage and register candidate applications for this position
            (Job ID: <strong><c:out value="${job.id}"/></strong>) through the central application pipeline.
        </p>
        <a href="${pageContext.request.contextPath}/applications?jobId=${job.id}" class="btn btn-manage-app">
            View / Create Applications for this Job
        </a>
    </div>

    <div class="actions-bar">
        <a href="${pageContext.request.contextPath}/jobs" class="btn btn-secondary">
            &larr; Back to Jobs
        </a>
        <a href="${pageContext.request.contextPath}/jobs?action=edit&id=${job.id}" class="btn btn-primary">
            Edit Job
        </a>
    </div>

</div>

</body>
</html>
