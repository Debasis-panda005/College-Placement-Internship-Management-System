<%@ page import="com.college.placement.internship.job.Job" %>

<%
    Job job = (Job) request.getAttribute("job");
%>

<!DOCTYPE html>
<html>
<head>
    <title>Apply for Job</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 40px;
        }

        .container {
            width: 500px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        h2 {
            margin-bottom: 10px;
        }

        .job-info {
            background-color: #f1f1f1;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
        }

        label {
            display: block;
            margin-top: 15px;
            margin-bottom: 5px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 10px;
            box-sizing: border-box;
        }

        button {
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            border: none;
            cursor: pointer;
        }

        .back {
            margin-left: 10px;
        }
    </style>
</head>

<body>

<div class="container">

    <h2>Apply for Job</h2>

    <div class="job-info">
        <strong>Job:</strong> <%= job.getTitle() %><br>
        <strong>Company:</strong> <%= job.getCompany() %><br>
        <strong>Location:</strong> <%= job.getLocation() %>
    </div>

    <form action="ApplicationServlet" method="post">

        <input type="hidden"
               name="jobId"
               value="<%= job.getId() %>">

        <label>Student Name</label>
        <input type="text"
               name="studentName"
               required>

        <label>Student Email</label>
        <input type="email"
               name="studentEmail"
               required>

        <label>Resume</label>
        <input type="text"
               name="resume"
               placeholder="Resume filename">

        <button type="submit">
            Submit Application
        </button>

        <a class="back"
           href="JobServlet">
            Cancel
        </a>

    </form>

</div>

</body>
</html>