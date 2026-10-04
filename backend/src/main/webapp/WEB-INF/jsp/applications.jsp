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

        input {
            padding: 10px;
            margin: 5px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        button {
            padding: 10px 18px;
            border: none;
            border-radius: 5px;
            background-color: #333;
            color: white;
            cursor: pointer;
        }

        button:hover {
            background-color: #555;
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
            border-radius: 4px;
            color: white;
            font-size: 13px;
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

        .empty {
            text-align: center;
            padding: 20px;
            color: #777;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>College Placement Applications</h1>

    <div class="form-section">

        <h2>Create New Application</h2>

        <form action="${pageContext.request.contextPath}/applications"
              method="post">

            <input type="hidden"
                   name="action"
                   value="create">

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

                            <c:if test="${application.status == 'APPLIED'}">

                                <a class="action-button shortlist-btn"
                                   href="${pageContext.request.contextPath}/applications?action=shortlist&id=${application.id}">
                                    Shortlist
                                </a>

                            </c:if>


                            <c:if test="${application.status == 'SHORTLISTED'}">

                                <a class="action-button select-btn"
                                   href="${pageContext.request.contextPath}/applications?action=select&id=${application.id}">
                                    Select
                                </a>

                            </c:if>


                            <a class="action-button delete-btn"
                               href="${pageContext.request.contextPath}/applications?action=delete&id=${application.id}"
                               onclick="return confirm('Are you sure you want to delete this application?');">
                                Delete
                            </a>

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