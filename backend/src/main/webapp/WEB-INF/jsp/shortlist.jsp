<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>Shortlisted Candidates</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 92%;
            margin: 30px auto;
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

        .alert-error {
            background-color: #f8d7da;
            color: #842029;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #f5c2c7;
        }

        .alert-success {
            background-color: #d1e7dd;
            color: #0f5132;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #badbcc;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }

        th,
        td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: center;
        }

        th {
            background-color: #333;
            color: white;
        }

        tr:nth-child(even) {
            background-color: #f8f8f8;
        }

        .status {
            font-weight: bold;
        }

        .shortlisted {
            font-weight: bold;
            color: #e67e22;
        }

        .scheduled {
            font-weight: bold;
            color: #0d6efd;
        }

        .completed {
            font-weight: bold;
            color: #198754;
        }

        .cancelled {
            font-weight: bold;
            color: #dc3545;
        }

        .rescheduled {
            font-weight: bold;
            color: #fd7e14;
        }

        .not-scheduled {
            font-weight: bold;
            color: #6c757d;
            font-style: italic;
        }

        .actions {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 4px;
        }

        .action-button {
            display: inline-block;
            padding: 6px 10px;
            margin: 2px;
            text-decoration: none;
            border-radius: 4px;
            color: white;
            font-size: 13px;
        }

        .action-button:hover {
            opacity: 0.85;
        }

        .interview-btn {
            background-color: #0d6efd;
        }

        .select-btn {
            background-color: #198754;
        }

        .reject-btn {
            background-color: #6c757d;
        }

        .view-btn {
            background-color: #17a2b8;
        }

        .history-btn {
            background-color: #6f42c1;
        }

        .empty {
            text-align: center;
            padding: 20px;
            color: #666;
        }
    </style>
</head>

<body>

<div class="container">

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a class="active" href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Shortlisted Candidates</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            ${errorMessage}
        </div>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="alert-success">
            ${successMessage}
        </div>
    </c:if>

    <table>

        <thead>
        <tr>
            <th>ID</th>
            <th>Student ID</th>
            <th>Job ID</th>
            <th>Application Date</th>
            <th>Application Status</th>
            <th>Interview Status</th>
            <th>Interview Date</th>
            <th>Interview Time</th>
            <th>Mode</th>
            <th>Actions</th>
        </tr>
        </thead>

        <tbody>

        <c:choose>

            <c:when test="${not empty applications}">

                <c:forEach var="application" items="${applications}">

                    <c:set var="interview" value="${interviewMap[application.id]}" />

                    <tr>
                        <td>${application.id}</td>
                        <td>${application.studentId}</td>
                        <td>${application.jobId}</td>
                        <td>${application.applicationDate}</td>

                        <td>
                            <span class="shortlisted">
                                ${application.status}
                            </span>
                        </td>

                        <td>
                            <c:set var="isRescheduled" value="${not empty rescheduledMap[application.id] && rescheduledMap[application.id]}" />
                            <c:choose>
                                <c:when test="${empty interview}">
                                    <span class="not-scheduled">NOT SCHEDULED</span>
                                </c:when>
                                <c:when test="${interview.status == 'COMPLETED'}">
                                    <span class="completed">COMPLETED</span>
                                </c:when>
                                <c:when test="${interview.status == 'CANCELLED'}">
                                    <span class="cancelled">CANCELLED</span>
                                </c:when>
                                <c:when test="${interview.status == 'SCHEDULED' && isRescheduled}">
                                    <span class="rescheduled">RESCHEDULED</span>
                                </c:when>
                                <c:when test="${interview.status == 'SCHEDULED'}">
                                    <span class="scheduled">SCHEDULED</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status">${interview.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty interview && not empty interview.interviewDate}">
                                    ${interview.interviewDate}
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty interview && not empty interview.interviewTime}">
                                    ${interview.interviewTime}
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty interview && not empty interview.mode}">
                                    ${interview.mode}
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <div class="actions">
                                <c:choose>
                                    <c:when test="${empty interview}">
                                        <a class="action-button interview-btn"
                                           href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}">
                                            Schedule Interview
                                        </a>
                                        <a class="action-button view-btn"
                                           href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}">
                                            View Details
                                        </a>
                                        <a class="action-button history-btn"
                                           href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
                                            History
                                        </a>
                                    </c:when>

                                    <c:when test="${interview.status == 'SCHEDULED'}">
                                        <a class="action-button view-btn"
                                           href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}">
                                            View Details
                                        </a>
                                        <a class="action-button history-btn"
                                           href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
                                            History
                                        </a>
                                    </c:when>

                                    <c:when test="${interview.status == 'COMPLETED'}">
                                        <a class="action-button select-btn"
                                           href="${pageContext.request.contextPath}/applications?action=select&id=${application.id}&from=shortlist">
                                            Select
                                        </a>
                                        <a class="action-button reject-btn"
                                           href="${pageContext.request.contextPath}/applications?action=reject&id=${application.id}&from=shortlist"
                                           onclick="return confirm('Are you sure you want to reject this candidate?');">
                                            Reject
                                        </a>
                                        <a class="action-button view-btn"
                                           href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}">
                                            View Details
                                        </a>
                                        <a class="action-button history-btn"
                                           href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
                                            History
                                        </a>
                                    </c:when>

                                    <c:when test="${interview.status == 'CANCELLED'}">
                                        <a class="action-button interview-btn"
                                           href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}">
                                            Schedule Interview
                                        </a>
                                        <a class="action-button view-btn"
                                           href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}">
                                            View Details
                                        </a>
                                        <a class="action-button history-btn"
                                           href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
                                            History
                                        </a>
                                    </c:when>

                                    <c:otherwise>
                                        <a class="action-button view-btn"
                                           href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}">
                                            View Details
                                        </a>
                                        <a class="action-button history-btn"
                                           href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
                                            History
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </td>
                    </tr>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <tr>
                    <td colspan="10" class="empty">
                        No shortlisted applications found.
                    </td>
                </tr>

            </c:otherwise>

        </c:choose>

        </tbody>

    </table>

</div>

</body>
</html>