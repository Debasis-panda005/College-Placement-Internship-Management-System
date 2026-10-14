<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.college.placement.internship.job.Internship" %>

<%
  Internship i = (Internship) request.getAttribute("internship");

  if (i == null) {
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

  <style>
    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      padding: 30px 16px;
      font-family: Arial, sans-serif;
      background: #f3f6fb;
      color: #1e293b;
    }

    .details-card {
      max-width: 750px;
      margin: 0 auto;
      padding: 24px;
      background: #ffffff;
      border-radius: 14px;
      box-shadow: 0 8px 28px rgba(0, 0, 0, 0.08);
    }

    h2 {
      margin: 0 0 12px;
      color: #1d4ed8;
      font-size: 22px;
      overflow-wrap: anywhere;
    }

    .detail {
      padding: 8px 0;
      border-bottom: 1px solid #e2e8f0;
      font-size: 14px;
      line-height: 1.4;
      overflow-wrap: anywhere;
    }

    .detail strong {
      display: block;
      margin-bottom: 3px;
      color: #334155;
      font-size: 14px;
      font-weight: 700;
    }

    .value {
      white-space: pre-wrap;
    }

    .status {
      display: inline-block;
      padding: 4px 10px;
      background: #dcfce7;
      color: #166534;
      border-radius: 20px;
      font-size: 12px;
      font-weight: bold;
    }

    .back-link {
      display: inline-block;
      margin-top: 18px;
      padding: 10px 16px;
      background: #2563eb;
      color: white;
      text-decoration: none;
      border-radius: 6px;
      font-size: 14px;
    }

    .back-link:hover {
      background: #1d4ed8;
    }

    @media (max-width: 600px) {
      body {
        padding: 16px 10px;
      }

      .details-card {
        padding: 18px;
      }
    }
  </style>
</head>

<body>

<div class="details-card">

  <h2><%= i.getTitle() %></h2>

  <div class="detail">
    <strong>Company</strong>
    <div class="value"><%= i.getCompany() == null || i.getCompany().isBlank()
            ? "Not specified" : i.getCompany() %></div>
  </div>

  <div class="detail">
    <strong>Location</strong>
    <div class="value"><%= i.getLocation() == null || i.getLocation().isBlank()
            ? "Not specified" : i.getLocation() %></div>
  </div>

  <div class="detail">
    <strong>Description</strong>
    <div class="value"><%= i.getDescription() == null || i.getDescription().isBlank()
            ? "Not specified" : i.getDescription() %></div>
  </div>

  <div class="detail">
    <strong>Stipend</strong>
    <div class="value"><%= i.getStipend() == null || i.getStipend().isBlank()
            ? "Not specified" : i.getStipend() %></div>
  </div>

  <div class="detail">
    <strong>Duration</strong>
    <div class="value"><%= i.getDuration() == null || i.getDuration().isBlank()
            ? "Not specified" : i.getDuration() %></div>
  </div>

  <div class="detail">
    <strong>Eligibility</strong>
    <div class="value"><%= i.getEligibility() == null || i.getEligibility().isBlank()
            ? "Not specified" : i.getEligibility() %></div>
  </div>

  <div class="detail">
    <strong>Application Deadline</strong>
    <div class="value"><%= i.getApplicationDeadline() == null
            ? "Not specified" : i.getApplicationDeadline() %></div>
  </div>

  <div class="detail">
    <strong>Status</strong>
    <span class="status"><%= i.getStatus() == null
            ? "Unknown" : i.getStatus() %></span>
  </div>

  <a class="back-link"
     href="${pageContext.request.contextPath}/internships">
    Back to Internships
  </a>

</div>

</body>
</html>
