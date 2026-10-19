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
    <title>Job Details</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body class="compact-page">

<div class="compact-container">

<div class="details-page-header">
    <h2>Job / Internship Details</h2>
</div>

<div class="details-card internship-details-card">

    <div class="details-card-heading">
        <span class="details-icon">&#128188;</span>
        <div>
            <h2><%= job != null && job.getTitle() != null ? job.getTitle() : "Job Details" %></h2>
            <p class="company-name">
                <%= job != null && job.getCompany() != null ? job.getCompany() : "" %>
            </p>
        </div>
    </div>

    <div class="internship-info-grid">

        <div class="info-item">
            <span class="info-label">Job ID</span>
            <span class="info-value"><%= job != null ? job.getId() : "" %></span>
        </div>

        <div class="info-item">
            <span class="info-label">Job Title</span>
            <span class="info-value"><%= job != null && job.getTitle() != null ? job.getTitle() : "" %></span>
        </div>

        <div class="info-item">
            <span class="info-label">Company</span>
            <span class="info-value"><%= job != null && job.getCompany() != null ? job.getCompany() : "" %></span>
        </div>

        <div class="info-item">
            <span class="info-label">Location</span>
            <span class="info-value"><%= job != null && job.getLocation() != null ? job.getLocation() : "" %></span>
        </div>

        <div class="info-item">
            <span class="info-label">Salary</span>
            <span class="info-value"><%= job != null && job.getSalary() != null ? job.getSalary() : "" %></span>
        </div>

        <div class="info-item">
            <span class="info-label">Job Type</span>
            <span class="info-value"><%= job != null && job.getJobType() != null ? job.getJobType() : "" %></span>
        </div>

        <div class="info-item">
            <span class="info-label">Status</span>
            <span class="info-value">
                <%
                    if (job != null && "Active".equals(job.getStatus())) {
                %>
                <span class="active-status">Active</span>
                <%
                    } else {
                %>
                <span class="closed-status">Closed</span>
                <%
                    }
                %>
            </span>
        </div>

    </div>

    <div class="description-section">
        <h3>Description</h3>
        <p><%= job != null && job.getDescription() != null && !job.getDescription().isEmpty() ? job.getDescription() : "No description provided." %></p>
    </div>

    <div class="details-actions">
        <a href="JobServlet" class="btn btn-secondary">
            ← Back to Jobs
        </a>
    </div>

</div>

</div>

</body>
</html>