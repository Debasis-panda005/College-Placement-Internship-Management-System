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

        h1 {
            text-align: center;
            color: #1976d2;
            margin-bottom: 30px;
        }

        .detail {
            margin-bottom: 18px;
        }

        .label {
            font-weight: bold;
            display: block;
            margin-bottom: 5px;
        }

        .value {
            padding: 10px;
            background: #f4f6f8;
            border-radius: 5px;
        }

        .back-button {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 18px;
            background: #1976d2;
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }

        .back-button:hover {
            background: #125aa0;
        }

    </style>

</head>

<body>

<div class="container">

    <h1>Job / Internship Details</h1>

    <div class="detail">
        <span class="label">Job ID</span>
        <div class="value">
            <%= job.getId() %>
        </div>
    </div>

    <div class="detail">
        <span class="label">Job Title</span>
        <div class="value">
            <%= job.getTitle() %>
        </div>
    </div>

    <div class="detail">
        <span class="label">Company</span>
        <div class="value">
            <%= job.getCompany() %>
        </div>
    </div>

    <div class="detail">
        <span class="label">Location</span>
        <div class="value">
            <%= job.getLocation() %>
        </div>
    </div>

    <div class="detail">
        <span class="label">Description</span>
        <div class="value">
            <%= job.getDescription() %>
        </div>
    </div>

    <div class="detail">
        <span class="label">Salary</span>
        <div class="value">
            <%= job.getSalary() %>
        </div>
    </div>

    <div class="detail">
        <span class="label">Job Type</span>
        <div class="value">
            <%= job.getJobType() %>
        </div>
    </div>

    <a href="JobServlet" class="back-button">
        ← Back to Jobs
    </a>

</div>

</body>
</html>