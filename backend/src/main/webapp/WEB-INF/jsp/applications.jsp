<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Applications</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 90%;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
        }

        h1 {
            text-align: center;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 25px;
        }

        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: center;
        }

        th {
            background: #333;
            color: white;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>College Placement Applications</h1>

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
            <tr>
                <td>${application.id}</td>
                <td>${application.studentId}</td>
                <td>${application.jobId}</td>
                <td>${application.applicationDate}</td>
                <td>${application.status}</td>
            </tr>
        </c:forEach>

        </tbody>

    </table>

</div>

</body>
</html>