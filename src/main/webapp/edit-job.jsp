<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.college.placement.internship.job.Job" %>

<%
  Job job = (Job) request.getAttribute("job");
%>

<!DOCTYPE html>
<html>
<head>
  <title>Edit Job</title>

  <style>
    body {
      font-family: Arial, sans-serif;
      background: #f4f6f8;
      margin: 0;
      padding: 40px;
    }

    .container {
      width: 500px;
      margin: auto;
      background: white;
      padding: 30px;
      border-radius: 10px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    h2 {
      text-align: center;
    }

    label {
      display: block;
      margin-top: 15px;
      font-weight: bold;
    }

    input,
    textarea,
    select {
      width: 100%;
      padding: 10px;
      margin-top: 5px;
      box-sizing: border-box;
    }

    textarea {
      height: 100px;
    }

    button {
      width: 100%;
      margin-top: 20px;
      padding: 12px;
      background: #1976d2;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }

    button:hover {
      background: #125aa0;
    }

    .back {
      display: block;
      text-align: center;
      margin-top: 15px;
      text-decoration: none;
    }
  </style>
</head>

<body>

<div class="container">

  <h2>Edit Job / Internship</h2>

  <form action="JobServlet" method="post">

    <!-- Hidden ID -->
    <input type="hidden" name="id" value="<%= job.getId() %>">

    <label>Job Title</label>
    <input type="text"
           name="title"
           value="<%= job.getTitle() %>"
           required>

    <label>Company</label>
    <input type="text"
           name="company"
           value="<%= job.getCompany() %>"
           required>

    <label>Location</label>
    <input type="text"
           name="location"
           value="<%= job.getLocation() %>">

    <label>Description</label>
    <textarea name="description"><%= job.getDescription() %></textarea>

    <label>Salary</label>
    <input type="text"
           name="salary"
           value="<%= job.getSalary() %>">

    <label>Job Type</label>

    <select name="jobType">

      <option value="Full Time"
              <%= "Full Time".equals(job.getJobType()) ? "selected" : "" %>>
        Full Time
      </option>

      <option value="Part Time"
              <%= "Part Time".equals(job.getJobType()) ? "selected" : "" %>>
        Part Time
      </option>

      <option value="Internship"
              <%= "Internship".equals(job.getJobType()) ? "selected" : "" %>>
        Internship
      </option>

    </select>

    <button type="submit">
      Update Job
    </button>

  </form>

  <a href="JobServlet" class="back">
    ← Back to Job List
  </a>

</div>

</body>
</html>