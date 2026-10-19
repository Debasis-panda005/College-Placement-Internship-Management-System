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
    <title>Apply for Job</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="details-page-header">
    <h2>Apply for Job</h2>
</div>

<div class="apply-job-card">

    <div class="job-summary">
        <p><strong>Job:</strong> <%= job != null && job.getTitle() != null ? job.getTitle() : "" %></p>
        <p><strong>Company:</strong> <%= job != null && job.getCompany() != null ? job.getCompany() : "" %></p>
        <p><strong>Location:</strong> <%= job != null && job.getLocation() != null ? job.getLocation() : "" %></p>
    </div>

    <form action="ApplicationServlet" method="post" class="apply-job-form">

        <input type="hidden"
               name="jobId"
               value="<%= job != null ? job.getId() : "" %>">

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

        <div style="margin-top: 20px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
            <button type="submit">
                Submit Application
            </button>

            <a class="btn btn-secondary"
               href="JobServlet">
                Cancel
            </a>
        </div>

    </form>

</div>

</body>
</html>