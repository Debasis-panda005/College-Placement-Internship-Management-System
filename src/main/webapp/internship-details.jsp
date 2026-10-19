<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.college.placement.internship.job.Internship" %>
<%
  Internship internship = (Internship) request.getAttribute("internship");

  if (internship == null) {
    response.sendError(404, "Internship not found");
    return;
  }
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Internship Details</title>

  <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="compact-page">

<div class="compact-container">

<div class="details-page-header">
  <h1>Internship Details</h1>
  <p>Explore the internship information below</p>
</div>

<div class="details-card internship-details-card">

  <div class="details-card-heading">
    <span class="details-icon">&#9733;</span>
    <div>
      <h2><%= internship.getTitle() == null ? "Internship Details" : internship.getTitle() %></h2>
      <p class="company-name">
        <%= internship.getCompany() == null ? "Company not provided" : internship.getCompany() %>
      </p>
    </div>
  </div>

  <div class="internship-info-grid">

    <div class="info-item">
      <span class="info-label">Location</span>
      <span class="info-value"><%= internship.getLocation() == null || internship.getLocation().isEmpty() ? "Not provided" : internship.getLocation() %></span>
    </div>

    <div class="info-item">
      <span class="info-label">Stipend</span>
      <span class="info-value"><%= internship.getStipend() == null ? "Not provided" : internship.getStipend() %></span>
    </div>

    <div class="info-item">
      <span class="info-label">Duration</span>
      <span class="info-value"><%= internship.getDuration() == null || internship.getDuration().isEmpty() ? "Not provided" : internship.getDuration() %></span>
    </div>

    <div class="info-item">
      <span class="info-label">Eligibility</span>
      <span class="info-value"><%= internship.getEligibility() == null || internship.getEligibility().isEmpty() ? "Not provided" : internship.getEligibility() %></span>
    </div>

    <div class="info-item">
      <span class="info-label">Application Deadline</span>
      <span class="info-value"><%= internship.getApplicationDeadline() == null ? "Not provided" : internship.getApplicationDeadline() %></span>
    </div>

    <div class="info-item">
      <span class="info-label">Status</span>
      <span class="info-value status-badge"><%= internship.getStatus() == null || internship.getStatus().isEmpty() ? "Not provided" : internship.getStatus() %></span>
    </div>

  </div>

  <div class="description-section">
    <h3>Internship Description</h3>
    <p><%= internship.getDescription() == null || internship.getDescription().isEmpty() ? "No description provided." : internship.getDescription() %></p>
  </div>

  <div class="details-actions">
    <a class="btn edit-internship-button"
       href="<%= request.getContextPath() %>/internships?action=edit&id=<%= internship.getId() %>">
      &#9998; Edit Internship
    </a>

    <a class="btn btn-secondary"
       href="<%= request.getContextPath() %>/internships">
      Back to Internships
    </a>
  </div>

</div>

<div class="dashboard-footer">
  <a class="dashboard-button"
     href="${pageContext.request.contextPath}/admin/dashboard">
    &#8592; Back to Dashboard
  </a>
</div>

</div>

</body>
</html>