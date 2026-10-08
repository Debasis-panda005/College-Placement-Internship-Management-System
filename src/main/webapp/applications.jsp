<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Application" %>

<!DOCTYPE html>
<html>
<head>
    <title>Applications</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 95%;
            margin: auto;
        }

        h1 {
            text-align: center;
            margin-bottom: 30px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
        }

        th, td {
            padding: 12px;
            border: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #1976d2;
            color: white;
        }

        tr:nth-child(even) {
            background: #f9f9f9;
        }

        .back-button {
            display: inline-block;
            padding: 10px 18px;
            background: #1976d2;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-bottom: 20px;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Student Applications</h1>

    <a href="JobServlet" class="back-button">
        Back to Jobs
    </a>

    <table>

        <tr>
            <th>ID</th>
            <th>Job ID</th>
            <th>Student Name</th>
            <th>Student Email</th>
            <th>Resume</th>
            <th>Applied Date</th>
        </tr>

        <%
            List<Application> applications =
                    (List<Application>) request.getAttribute("applications");

            if (applications != null && !applications.isEmpty()) {

                for (Application app : applications) {
        %>

        <tr>
            <td><%= app.getId() %></td>
            <td><%= app.getJobId() %></td>
            <td><%= app.getStudentName() %></td>
            <td><%= app.getStudentEmail() %></td>
            <td><%= app.getResume() %></td>
            <td><%= app.getAppliedDate() %></td>
        </tr>

        <%
            }

        } else {
        %>

        <tr>
            <td colspan="6" style="text-align:center;">
                No applications available.
            </td>
        </tr>

        <%
            }
        %>

    </table>

</div>

</body>
</html>