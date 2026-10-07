<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>Shortlisted Applications</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 90%;
            margin: 40px auto;
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        h1 {
            text-align: center;
            margin-bottom: 30px;
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
            background-color: #333;
            color: white;
        }

        tr:nth-child(even) {
            background-color: #f2f2f2;
        }

        .shortlisted {
            font-weight: bold;
            color: #e67e22;
        }

        .action-button {
            display: inline-block;
            padding: 7px 12px;
            text-decoration: none;
            border-radius: 4px;
            color: white;
            font-size: 13px;
        }

        .interview-btn {
            background-color: #0d6efd;
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

    <h1>Shortlisted Applications</h1>

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

                <c:forEach var="application" items="${applications}">

                    <tr>
                        <td>${application.id}</td>
                        <td>${application.studentId}</td>
                        <td>${application.jobId}</td>
                        <td>${application.applicationDate}</td>

                        <td class="shortlisted">
                            ${application.status}
                        </td>

                        <td>
                            <a class="action-button interview-btn"
                               href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}">
                                Schedule Interview
                            </a>
                        </td>
                    </tr>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <tr>
                    <td colspan="6" class="empty">
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