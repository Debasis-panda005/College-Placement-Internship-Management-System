<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Application" %>

<!DOCTYPE html>
<html>
<head>
    <title>Student Applications</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 98%;
            margin: auto;
        }

        h1 {
            text-align: center;
            margin-bottom: 25px;
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

        .message {
            background: #e0f2e9;
            color: #176b3a;
            padding: 12px;
            margin-bottom: 15px;
            border-radius: 5px;
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

        select {
            padding: 7px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        button {
            padding: 7px 10px;
            background: #1976d2;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        button:hover {
            background: #125ca1;
        }

        .status {
            font-weight: bold;
        }

        .pending {
            color: #9a6700;
        }

        .shortlisted {
            color: #1565c0;
        }

        .selected {
            color: #16803c;
        }

        .rejected {
            color: #c62828;
        }

        .status-form {
            display: flex;
            align-items: center;
            gap: 6px;
        }
    </style>
</head>

<body>
<div class="container">

    <h1>Student Applications</h1>

    <a href="JobServlet" class="back-button">Back to Jobs</a>

    <%
        String message = request.getParameter("message");

        if ("statusUpdated".equals(message)) {
    %>
    <div class="message">Application status updated successfully.</div>
    <%
        }
    %>

    <table>
        <tr>
            <th>ID</th>
            <th>Job ID</th>
            <th>Student Name</th>
            <th>Student Email</th>
            <th>Resume</th>
            <th>Applied Date</th>
            <th>Current Status</th>
            <th>Update Status</th>
        </tr>

        <%
            List<Application> applications =
                    (List<Application>) request.getAttribute("applications");

            if (applications != null && !applications.isEmpty()) {
                for (Application app : applications) {

                    String status = app.getStatus();

                    if (status == null || status.isEmpty()) {
                        status = "Pending";
                    }

                    String statusClass = status.toLowerCase();
        %>

        <tr>
            <td><%= app.getId() %></td>
            <td><%= app.getJobId() %></td>
            <td><%= app.getStudentName() %></td>
            <td><%= app.getStudentEmail() %></td>
            <td><%= app.getResume() %></td>
            <td><%= app.getAppliedDate() %></td>

            <td>
                <span class="status <%= statusClass %>">
                    <%= status %>
                </span>
            </td>

            <td>
                <form action="ApplicationServlet"
                      method="post"
                      class="status-form">

                    <input type="hidden"
                           name="action"
                           value="updateStatus">

                    <input type="hidden"
                           name="id"
                           value="<%= app.getId() %>">

                    <select name="status" required>
                        <option value="Pending"
                                <%= "Pending".equals(status) ? "selected" : "" %>>
                            Pending
                        </option>

                        <option value="Shortlisted"
                                <%= "Shortlisted".equals(status) ? "selected" : "" %>>
                            Shortlisted
                        </option>

                        <option value="Selected"
                                <%= "Selected".equals(status) ? "selected" : "" %>>
                            Selected
                        </option>

                        <option value="Rejected"
                                <%= "Rejected".equals(status) ? "selected" : "" %>>
                            Rejected
                        </option>
                    </select>

                    <button type="submit">Update</button>
                </form>
            </td>
        </tr>

        <%
            }
        } else {
        %>

        <tr>
            <td colspan="8" style="text-align:center;">
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