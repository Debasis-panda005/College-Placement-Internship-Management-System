<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Job" %>

<!DOCTYPE html>
<html>
<head>
    <title>Job & Internship List</title>

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
        }

        .add-button {
            display: inline-block;
            padding: 10px 18px;
            background: #1976d2;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-bottom: 20px;
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
    </style>
</head>

<body>

<div class="container">

    <h1>Available Jobs & Internships</h1>

    <a href="add-job.jsp" class="add-button">
        + Add Job / Internship
    </a>

    <table>

        <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Company</th>
            <th>Location</th>
            <th>Description</th>
            <th>Salary</th>
            <th>Job Type</th>
        </tr>

        <%
            List<Job> jobs = (List<Job>) request.getAttribute("jobs");

            if (jobs != null && !jobs.isEmpty()) {

                for (Job job : jobs) {
        %>

        <tr>
            <td><%= job.getId() %></td>
            <td><%= job.getTitle() %></td>
            <td><%= job.getCompany() %></td>
            <td><%= job.getLocation() %></td>
            <td><%= job.getDescription() %></td>
            <td><%= job.getSalary() %></td>
            <td><%= job.getJobType() %></td>
        </tr>

        <%
            }

        } else {
        %>

        <tr>
            <td colspan="7" style="text-align:center;">
                No jobs available.
            </td>
        </tr>

        <%
            }
        %>

    </table>

</div>

</body>
</html>