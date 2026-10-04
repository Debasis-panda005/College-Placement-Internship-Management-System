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

    <h1>Shortlisted Applications</h1>

    <table>

        <thead>
        <tr>
            <th>ID</th>
            <th>Student ID</th>
            <th>Job ID</th>
            <th>Application Date</th>
            <th>Status</th>
        </tr>
        </thead>

        <tbody>

        <c:forEach var="application" items="${applications}">

            <c:if test="${application.status == 'SHORTLISTED'}">

                <tr>
                    <td>${application.id}</td>
                    <td>${application.studentId}</td>
                    <td>${application.jobId}</td>
                    <td>${application.applicationDate}</td>

                    <td class="shortlisted">
                            ${application.status}
                    </td>
                </tr>

            </c:if>

        </c:forEach>

        </tbody>

    </table>

</div>

</body>
</html>