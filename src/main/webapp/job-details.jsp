<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.college.placement.internship.job.Job" %>

<%
    Job job = (Job) request.getAttribute("job");
%>

<!DOCTYPE html>
<html>
<head>
    <title>Job Details</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 40px;
        }

        .container {
            width: 600px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }

        h2 {
            text-align: center;
            margin-bottom: 30px;
        }

        .detail {
            margin-bottom: 15px;
        }

        .label {
            font-weight: bold;
        }

        .active-status {
            color: green;
            font-weight: bold;
        }

        .closed-status {
            color: red;
            font-weight: bold;
        }

        .back-button {
            display: block;
            text-align: center;
            margin-top: 25px;
            text-decoration: none;
        }
    </style>
</head>

<body>

<div class="container">

    <h2>Job / Internship Details</h2>

    <div class="detail">
        <span class="label">Job ID:</span>
        <%= job.getId() %>
    </div>

    <div class="detail">
        <span class="label">Job Title:</span>
        <%= job.getTitle() %>
    </div>

    <div class="detail">
        <span class="label">Company:</span>
        <%= job.getCompany() %>
    </div>

    <div class="detail">
        <span class="label">Location:</span>
        <%= job.getLocation() %>
    </div>

    <div class="detail">
        <span class="label">Description:</span>
        <%= job.getDescription() %>
    </div>

    <div class="detail">
        <span class="label">Salary:</span>
        <%= job.getSalary() %>
    </div>

    <div class="detail">
        <span class="label">Job Type:</span>
        <%= job.getJobType() %>
    </div>

    <div class="detail">
        <span class="label">Status:</span>

        <%
            if ("Active".equals(job.getStatus())) {
        %>

        <span class="active-status">
                Active
            </span>

        <%
        } else {
        %>

        <span class="closed-status">
                Closed
            </span>

        <%
            }
        %>

    </div>

    <a href="JobServlet" class="back-button">
        ← Back to Jobs
    </a>

</div>

</body>
</html>