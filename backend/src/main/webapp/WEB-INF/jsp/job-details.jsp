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

        .nav-links form {
            display: inline;
            margin: 0;
            padding: 0;
        }

        .nav-links .btn-logout {
            background: none;
            border: none;
            color: #dc3545;
            font-weight: 500;
            cursor: pointer;
            padding: 0;
            margin: 0 12px;
            font-size: 14px;
            font-family: inherit;
        }

        .nav-links .btn-logout:hover {
            text-decoration: underline;
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
        <a href="${pageContext.request.contextPath}/jobs">Jobs</a> |
        <a href="${pageContext.request.contextPath}/internships">Internships</a> |
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
        <c:if test="${not empty sessionScope.userId}">
            |
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display: inline; margin: 0; padding: 0;">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn-logout">Logout</button>
            </form>
        </c:if>
    </div>

    <h1><c:out value="${job.isInternship() ? 'Internship Details' : 'Job Details'}"/></h1>

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
        <div class="detail-label"><c:out value="${job.isInternship() ? 'Stipend:' : 'Salary / Stipend:'}"/></div>
        <div class="detail-value"><c:out value="${job.salary}"/></div>
    </div>

    <c:if test="${not empty job.duration}">
        <div class="detail-row">
            <div class="detail-label">Duration:</div>
            <div class="detail-value"><c:out value="${job.duration}"/></div>
        </div>
    </c:if>

    <div class="detail-row">
        <div class="detail-label">Job Type:</div>
        <div class="detail-value"><c:out value="${job.jobType}"/></div>
    </div>

    <c:if test="${not empty job.deadline}">
        <div class="detail-row">
            <div class="detail-label">Application Deadline:</div>
            <div class="detail-value"><strong><c:out value="${job.deadline}"/></strong></div>
        </div>
    </c:if>

    <c:if test="${not empty job.minCgpa}">
        <div class="detail-row">
            <div class="detail-label">Minimum CGPA:</div>
            <div class="detail-value"><c:out value="${job.minCgpa}"/> / 10.00</div>
        </div>
    </c:if>

    <c:if test="${not empty job.eligibleDepartments}">
        <div class="detail-row">
            <div class="detail-label">Eligible Departments:</div>
            <div class="detail-value"><c:out value="${job.eligibleDepartments}"/></div>
        </div>
    </c:if>

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

    <!-- STUDENT APPLICATION FLOW -->
    <div class="notice-box">
        <h3>Student Application</h3>
        <c:choose>
            <c:when test="${job.status == 'Active'}">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <p>Submit your application directly using your authenticated student profile.</p>
                        <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline-block; margin-top: 10px;">
                            <input type="hidden" name="action" value="apply">
                            <input type="hidden" name="jobId" value="${job.id}">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <button type="submit" class="btn btn-manage-app" style="cursor: pointer; border: none; font-size: 14px;">
                                Apply Now
                            </button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <p>You must be signed in as a student to apply for this job.</p>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="margin-top: 10px;">
                            Log In to Apply
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <p style="color: #dc3545; font-weight: bold;">
                    Applications are closed for this position.
                </p>
            </c:otherwise>
        </c:choose>

        <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
            <div style="margin-top: 15px; padding-top: 12px; border-top: 1px dashed #b6d4fe;">
                <p style="margin: 0 0 8px 0; font-size: 13px; color: #555;">
                    Administrative pipeline management:
                </p>
                <a href="${pageContext.request.contextPath}/applications?jobId=${job.id}" class="btn btn-secondary" style="font-size: 13px; padding: 6px 12px;">
                    View / Manage Applications for this Job
                </a>
            </div>
        </c:if>
    </div>

    <div class="actions-bar">
        <a href="${pageContext.request.contextPath}${job.isInternship() || isInternshipsEndpoint ? '/internships' : '/jobs'}" class="btn btn-secondary">
            &larr; <c:out value="${job.isInternship() || isInternshipsEndpoint ? 'Back to Internships' : 'Back to Jobs'}"/>
        </a>
        <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
            <a href="${pageContext.request.contextPath}/jobs?action=edit&id=${job.id}" class="btn btn-primary">
                <c:out value="${job.isInternship() ? 'Edit Internship' : 'Edit Job'}"/>
            </a>
        </c:if>
    </div>

</div>

</body>
</html>
