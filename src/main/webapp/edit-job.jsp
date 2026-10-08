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

    .back-button {
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

  <form action="JobServlet"
        method="post"
        onsubmit="return validateForm()">

    <!-- JOB ID -->
    <input type="hidden"
           name="id"
           value="<%= job.getId() %>">


    <label>Job Title</label>

    <input type="text"
           id="title"
           name="title"
           value="<%= job.getTitle() %>"
           required>


    <label>Company</label>

    <input type="text"
           id="company"
           name="company"
           value="<%= job.getCompany() %>"
           required>


    <label>Location</label>

    <input type="text"
           id="location"
           name="location"
           value="<%= job.getLocation() %>"
           required>


    <label>Description</label>

    <textarea name="description"><%= job.getDescription() %></textarea>


    <label>Salary</label>

    <input type="text"
           id="salary"
           name="salary"
           value="<%= job.getSalary() %>"
           required>


    <label>Job Type</label>

    <select id="jobType"
            name="jobType"
            required>

      <option value="">Select Job Type</option>

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


    <label>Status</label>

    <select id="status"
            name="status"
            required>

      <option value="Active"
              <%= "Active".equals(job.getStatus()) ? "selected" : "" %>>
        Active
      </option>

      <option value="Closed"
              <%= "Closed".equals(job.getStatus()) ? "selected" : "" %>>
        Closed
      </option>

    </select>


    <button type="submit">
      Update Job
    </button>

  </form>


  <a href="JobServlet" class="back-button">
    ← Back to Jobs
  </a>

</div>


<script>

  function validateForm() {

    let title =
            document.getElementById("title").value.trim();

    let company =
            document.getElementById("company").value.trim();

    let location =
            document.getElementById("location").value.trim();

    let salary =
            document.getElementById("salary").value.trim();

    let jobType =
            document.getElementById("jobType").value;

    let status =
            document.getElementById("status").value;


    if (title === "") {
      alert("Please enter the job title.");
      return false;
    }


    if (company === "") {
      alert("Please enter the company name.");
      return false;
    }


    if (location === "") {
      alert("Please enter the location.");
      return false;
    }


    if (salary === "") {
      alert("Please enter the salary.");
      return false;
    }


    if (jobType === "") {
      alert("Please select a job type.");
      return false;
    }


    if (status === "") {
      alert("Please select a status.");
      return false;
    }


    return true;
  }

</script>

</body>
</html>