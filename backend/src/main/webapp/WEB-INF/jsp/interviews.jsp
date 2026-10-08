<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>Interview Management</title>

    <style>

        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 90%;
            margin: 40px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            text-align: center;
            color: #222;
            margin-bottom: 30px;
        }

        .form-section {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 8px;
            margin-bottom: 30px;
        }

        .form-section h2 {
            margin-top: 0;
            margin-bottom: 20px;
        }

        .form-row {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
            align-items: center;
        }

        input,
        select {
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
        }

        input {
            width: 170px;
        }

        select {
            width: 180px;
        }

        .btn {
            padding: 12px 18px;
            border: none;
            border-radius: 6px;
            color: white;
            cursor: pointer;
            text-decoration: none;
            font-size: 14px;
        }

        .btn-create {
            background: #333;
        }

        .btn-edit {
            background: #0d6efd;
        }

        .btn-complete {
            background: #198754;
        }

        .btn-select {
            background: #198754;
        }

        .btn-reject {
            background: #6c757d;
        }

        .btn-delete {
            background: #dc3545;
        }

        .btn-filter {
            background: #0d6efd;
        }

        .btn-reset {
            background: #6c757d;
        }

        .btn:hover {
            opacity: 0.85;
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

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th,
        td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: center;
        }

        th {
            background: #333;
            color: white;
        }

        tr:nth-child(even) {
            background: #f7f7f7;
        }

        .status {
            font-weight: bold;
        }

        .scheduled {
            color: #0d6efd;
        }

        .completed {
            color: #198754;
        }

        .actions {
            display: flex;
            justify-content: center;
            gap: 8px;
        }

        .empty {
            text-align: center;
            padding: 25px;
            color: #777;
        }

        .app-link {
            color: #0066cc;
            text-decoration: none;
            font-weight: 600;
        }

        .app-link:hover {
            text-decoration: underline;
        }

    </style>

</head>

<body>

<div class="container">

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a class="active" href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Interview Management</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            ${errorMessage}
        </div>
    </c:if>

    <!-- CREATE INTERVIEW -->

    <div class="form-section">

        <h2>Schedule New Interview</h2>

        <form action="${pageContext.request.contextPath}/interviews"
              method="post">

            <input type="hidden"
                   name="action"
                   value="create">

            <div class="form-row">

                <input type="number"
                       name="applicationId"
                       placeholder="Application ID"
                       value="${selectedApplicationId}"
                       required>

                <input type="date"
                       name="interviewDate"
                       min="${today}"
                       required>

                <input type="time"
                       name="interviewTime"
                       required>

                <select name="mode" required>

                    <option value="">Select Mode</option>

                    <option value="ONLINE">
                        Online
                    </option>

                    <option value="OFFLINE">
                        Offline
                    </option>

                </select>

                <button type="submit"
                        class="btn btn-create">
                    Schedule Interview
                </button>

            </div>

        </form>

    </div>


    <!-- FILTER INTERVIEWS -->

    <div class="form-section">

        <h2>Filter Interviews</h2>

        <form action="${pageContext.request.contextPath}/interviews"
              method="get">

            <div class="form-row">

                <input type="number"
                       name="filterAppId"
                       placeholder="Application ID"
                       value="${selectedFilterAppId}">

                <input type="date"
                       name="filterDate"
                       value="${selectedFilterDate}">

                <select name="filterMode">
                    <option value="ALL" ${selectedFilterMode == 'ALL' ? 'selected' : ''}>All Modes</option>
                    <option value="ONLINE" ${selectedFilterMode == 'ONLINE' ? 'selected' : ''}>Online</option>
                    <option value="OFFLINE" ${selectedFilterMode == 'OFFLINE' ? 'selected' : ''}>Offline</option>
                </select>

                <select name="filterStatus">
                    <option value="ALL" ${selectedFilterStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                    <option value="SCHEDULED" ${selectedFilterStatus == 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                    <option value="COMPLETED" ${selectedFilterStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                </select>

                <button type="submit"
                        class="btn btn-filter">
                    Filter
                </button>

                <a class="btn btn-reset"
                   href="${pageContext.request.contextPath}/interviews">
                    Reset
                </a>

            </div>

        </form>

    </div>


    <!-- INTERVIEW TABLE -->

    <table>

        <thead>

        <tr>

            <th>ID</th>
            <th>Application ID</th>
            <th>Interview Date</th>
            <th>Interview Time</th>
            <th>Mode</th>
            <th>Status</th>
            <th>Actions</th>

        </tr>

        </thead>


        <tbody>

        <c:choose>

            <c:when test="${not empty interviews}">

                <c:forEach var="interview"
                           items="${interviews}">

                    <tr>

                        <td>
                                ${interview.id}
                        </td>

                        <td>
                            <a class="app-link"
                               href="${pageContext.request.contextPath}/applications?action=details&id=${interview.applicationId}">
                                #${interview.applicationId}
                            </a>
                        </td>

                        <td>
                                ${interview.interviewDate}
                        </td>

                        <td>
                                ${interview.interviewTime}
                        </td>

                        <td>
                                ${interview.mode}
                        </td>

                        <td class="status">

                            <c:choose>

                                <c:when test="${interview.status == 'COMPLETED'}">

                                    <span class="completed">
                                        COMPLETED
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="scheduled">
                                            ${interview.status}
                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <td>

                            <div class="actions">

                                <c:if test="${interview.status == 'SCHEDULED'}">

                                    <a class="btn btn-edit"
                                       href="${pageContext.request.contextPath}/interviews?action=edit&id=${interview.id}">
                                        Edit / Reschedule
                                    </a>

                                    <a class="btn btn-complete"
                                       href="${pageContext.request.contextPath}/interviews?action=complete&id=${interview.id}">
                                        Complete
                                    </a>

                                </c:if>

                                <c:if test="${interview.status == 'COMPLETED'}">

                                    <a class="btn btn-select"
                                       href="${pageContext.request.contextPath}/applications?action=select&id=${interview.applicationId}">
                                        Select
                                    </a>

                                    <a class="btn btn-reject"
                                       href="${pageContext.request.contextPath}/applications?action=reject&id=${interview.applicationId}"
                                       onclick="return confirm('Are you sure you want to reject this candidate?');">
                                        Reject
                                    </a>

                                </c:if>

                                <a class="btn btn-delete"
                                   href="${pageContext.request.contextPath}/interviews?action=delete&id=${interview.id}"
                                   onclick="return confirm('Are you sure you want to delete this interview?');">
                                    Delete
                                </a>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

            </c:when>


            <c:otherwise>

                <tr>

                    <td colspan="7" class="empty">
                        No interviews found.
                    </td>

                </tr>

            </c:otherwise>

        </c:choose>

        </tbody>

    </table>

</div>

</body>
</html>
