<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.college.placement.internship.job.Job" %>

<%
  Job job = (Job) request.getAttribute("job");
%>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Edit Job</title>

  <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="details-page-header">
  <h2>Edit Job / Internship</h2>
</div>

  <form action="JobServlet"
        method="post"
        onsubmit="return validateForm()"
        class="job-edit-form">

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

  <div class="form-actions">
    <a href="JobServlet" class="btn btn-secondary">
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