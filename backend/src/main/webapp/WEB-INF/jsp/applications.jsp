<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>College Placement Applications</title>

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
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            text-align: center;
            color: #333;
            margin-bottom: 25px;
        }

        .form-section {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
        }

        .form-section h2 {
            margin-top: 0;
            color: #333;
        }

        input,
        select {
            padding: 10px;
            margin: 5px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 14px;
        }

        button {
            padding: 10px 18px;
            border: none;
            border-radius: 5px;
            background-color: #333;
            color: white;
            cursor: pointer;
            font-size: 14px;
        }

        button:hover {
            background-color: #555;
        }

        .btn-reset {
            display: inline-block;
            padding: 10px 18px;
            margin: 5px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-size: 14px;
        }

        .btn-reset:hover {
            background-color: #5a6268;
        }

        .stats-container {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            margin-bottom: 25px;
        }

        .stat-card {
            flex: 1;
            min-width: 140px;
            background-color: #f8f9fa;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 16px 12px;
            text-align: center;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .stat-card h3 {
            margin: 0;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #6c757d;
        }

        .stat-card .stat-count {
            font-size: 26px;
            font-weight: bold;
            margin-top: 6px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
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

        .action-button {
            display: inline-block;
            padding: 7px 10px;
            margin: 2px;
            text-decoration: none;
            border: none;
            border-radius: 4px;
            color: white;
            font-size: 13px;
            cursor: pointer;
            font-family: inherit;
            vertical-align: middle;
        }

        .action-button:hover {
            opacity: 0.85;
        }

        .inline-form {
            display: inline;
            margin: 0;
            padding: 0;
        }

        .shortlist-btn {
            background-color: #e67e22;
        }

        .select-btn {
            background-color: #198754;
        }

        .delete-btn {
            background-color: #dc3545;
        }

        .reject-btn {
            background-color: #6c757d;
        }

        .interview-btn {
            background-color: #0d6efd;
        }

        .view-btn {
            background-color: #17a2b8;
        }

        .history-btn {
            background-color: #6f42c1;
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

        .empty {
            text-align: center;
            padding: 20px;
            color: #777;
        }
    </style>
</head>

<body>

<div class="container">

    <div class="nav-links">
        <a class="active" href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>College Placement Applications</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            ${errorMessage}
        </div>
    </c:if>

    <div class="stats-container">

        <div class="stat-card">
            <h3>Total Applications</h3>
            <div class="stat-count" style="color: #333;">${applicationStats.TOTAL}</div>
        </div>

        <div class="stat-card">
            <h3>Applied</h3>
            <div class="stat-count applied">${applicationStats.APPLIED}</div>
        </div>

        <div class="stat-card">
            <h3>Shortlisted</h3>
            <div class="stat-count shortlisted">${applicationStats.SHORTLISTED}</div>
        </div>

        <div class="stat-card">
            <h3>Selected</h3>
            <div class="stat-count selected">${applicationStats.SELECTED}</div>
        </div>

        <div class="stat-card">
            <h3>Rejected</h3>
            <div class="stat-count rejected">${applicationStats.REJECTED}</div>
        </div>

    </div>

    <div class="form-section">

        <h2>Create New Application</h2>

        <form action="${pageContext.request.contextPath}/applications"
              method="post">

            <input type="hidden"
                   name="action"
                   value="create">

            <input type="hidden"
                   name="csrfToken"
                   value="${sessionScope.csrfToken}">

            <input type="number"
                   name="studentId"
                   placeholder="Student ID"
                   required>

            <input type="number"
                   name="jobId"
                   placeholder="Job ID"
                   required>

            <input type="date"
                   name="applicationDate"
                   required>

            <button type="submit">
                Apply
            </button>

        </form>

    </div>

    <div class="form-section">

        <h2>Filter Applications</h2>

        <form action="${pageContext.request.contextPath}/applications"
              method="get">

            <input type="number"
                   name="studentId"
                   placeholder="Student ID"
                   value="${selectedStudentId}">

            <input type="number"
                   name="jobId"
                   placeholder="Job ID"
                   value="${selectedJobId}">

            <select name="status">
                <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                <option value="APPLIED" ${selectedStatus == 'APPLIED' ? 'selected' : ''}>APPLIED</option>
                <option value="SHORTLISTED" ${selectedStatus == 'SHORTLISTED' ? 'selected' : ''}>SHORTLISTED</option>
                <option value="SELECTED" ${selectedStatus == 'SELECTED' ? 'selected' : ''}>SELECTED</option>
                <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>REJECTED</option>
            </select>

            <button type="submit">
                Filter
            </button>

            <a class="btn-reset"
               href="${pageContext.request.contextPath}/applications">
                Reset
            </a>

        </form>

    </div>


    <table>

        <thead>

        <tr>
            <th>ID</th>
            <th>Student ID</th>
            <th>Job ID</th>
            <th>Application Date</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>

        </thead>


        <tbody>

        <c:choose>

            <c:when test="${not empty applications}">

                <c:forEach var="application"
                           items="${applications}">

                    <tr>

                        <td>
                                ${application.id}
                        </td>

                        <td>
                                ${application.studentId}
                        </td>

                        <td>
                                ${application.jobId}
                        </td>

                        <td>
                                ${application.applicationDate}
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${application.status == 'APPLIED'}">
                                    <span class="status applied">
                                        APPLIED
                                    </span>
                                </c:when>

                                <c:when test="${application.status == 'SHORTLISTED'}">
                                    <span class="status shortlisted">
                                        SHORTLISTED
                                    </span>
                                </c:when>

                                <c:when test="${application.status == 'SELECTED'}">
                                    <span class="status selected">
                                        SELECTED
                                    </span>
                                </c:when>

                                <c:when test="${application.status == 'REJECTED'}">
                                    <span class="status rejected">
                                        REJECTED
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="status">
                                            ${application.status}
                                    </span>
                                </c:otherwise>

                            </c:choose>

                        </td>


                        <td>

                            <a class="action-button view-btn"
                               href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}">
                                View Details
                            </a>

                            <a class="action-button history-btn"
                               href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}">
                                History
                            </a>

                            <c:if test="${application.status == 'APPLIED'}">

                                <form action="${pageContext.request.contextPath}/applications" method="post" class="inline-form">
                                    <input type="hidden" name="action" value="shortlist">
                                    <input type="hidden" name="id" value="${application.id}">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <button type="submit" class="action-button shortlist-btn">Shortlist</button>
                                </form>

                                <form action="${pageContext.request.contextPath}/applications" method="post" class="inline-form">
                                    <input type="hidden" name="action" value="reject">
                                    <input type="hidden" name="id" value="${application.id}">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <button type="submit" class="action-button reject-btn"
                                            onclick="return confirm('Are you sure you want to reject this application?');">
                                        Reject
                                    </button>
                                </form>

                            </c:if>


                            <c:if test="${application.status == 'SHORTLISTED'}">

                                <c:choose>
                                    <c:when test="${completedAppIds.contains(application.id)}">

                                        <form action="${pageContext.request.contextPath}/applications" method="post" class="inline-form">
                                            <input type="hidden" name="action" value="select">
                                            <input type="hidden" name="id" value="${application.id}">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <button type="submit" class="action-button select-btn">Select</button>
                                        </form>

                                        <form action="${pageContext.request.contextPath}/applications" method="post" class="inline-form">
                                            <input type="hidden" name="action" value="reject">
                                            <input type="hidden" name="id" value="${application.id}">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <button type="submit" class="action-button reject-btn"
                                                    onclick="return confirm('Are you sure you want to reject this application?');">
                                                Reject
                                            </button>
                                        </form>

                                    </c:when>

                                    <c:otherwise>

                                        <a class="action-button interview-btn"
                                           href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}">
                                            Schedule Interview
                                        </a>

                                        <form action="${pageContext.request.contextPath}/applications" method="post" class="inline-form">
                                            <input type="hidden" name="action" value="reject">
                                            <input type="hidden" name="id" value="${application.id}">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <button type="submit" class="action-button reject-btn"
                                                    onclick="return confirm('Are you sure you want to reject this application?');">
                                                Reject
                                            </button>
                                        </form>

                                    </c:otherwise>
                                </c:choose>

                            </c:if>


                            <form action="${pageContext.request.contextPath}/applications" method="post" class="inline-form">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${application.id}">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <button type="submit" class="action-button delete-btn"
                                        onclick="return confirm('Are you sure you want to delete this application?');">
                                    Delete
                                </button>
                            </form>

                        </td>

                    </tr>

                </c:forEach>

            </c:when>


            <c:otherwise>

                <tr>

                    <td colspan="6" class="empty">
                        No applications found.
                    </td>

                </tr>

            </c:otherwise>

        </c:choose>

        </tbody>

    </table>

</div>

</body>
</html>
