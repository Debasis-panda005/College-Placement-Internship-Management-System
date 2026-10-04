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

        .btn-complete {
            background: #198754;
        }

        .btn-delete {
            background: #dc3545;
        }

        .btn:hover {
            opacity: 0.85;
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

    </style>

</head>

<body>

<div class="container">

    <h1>Interview Management</h1>


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
                       required>

                <input type="date"
                       name="interviewDate"
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
                                ${interview.applicationId}
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

                                <c:if test="${interview.status != 'COMPLETED'}">

                                    <a class="btn btn-complete"
                                       href="${pageContext.request.contextPath}/interviews?action=complete&id=${interview.id}">
                                        Complete
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