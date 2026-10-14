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
  <title>Edit Internship</title>

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

    .form-card {
      max-width: 650px;
      margin: 0 auto;
      padding: 24px;
      background: #ffffff;
      border-radius: 14px;
      box-shadow: 0 8px 28px rgba(0, 0, 0, 0.08);
    }

    h2 {
      margin: 0 0 18px;
      color: #1d4ed8;
      font-size: 22px;
    }

    label {
      display: block;
      margin-bottom: 5px;
      font-size: 14px;
      font-weight: 700;
    }

    input, textarea, select {
      width: 100%;
      padding: 10px;
      margin: 0 0 14px;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      background: #ffffff;
      font-family: inherit;
      font-size: 14px;
    }

    textarea {
      min-height: 75px;
      resize: vertical;
    }

    input:focus, textarea:focus, select:focus {
      outline: none;
      border-color: #2563eb;
      box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.12);
    }

    button {
      padding: 11px 18px;
      background: #2563eb;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
    }

    button:hover {
      background: #1d4ed8;
    }

    .cancel {
      display: inline-block;
      margin-left: 12px;
      color: #475569;
      font-size: 14px;
      text-decoration: none;
    }

    .cancel:hover {
      color: #1d4ed8;
    }

    @media (max-width: 600px) {
      body {
        padding: 16px 10px;
      }

      .form-card {
        padding: 18px;
      }
    }
  </style>
</head>

<body>

<div class="form-card">

  <h2>Edit Internship</h2>

  <form action="${pageContext.request.contextPath}/internships"
        method="post">

    <input type="hidden" name="action" value="update">
    <input type="hidden" name="id" value="<%= i.getId() %>">

    <label for="title">Title</label>
    <input id="title" name="title" required maxlength="150"
           value="<%= i.getTitle() == null ? "" : i.getTitle() %>">

    <label for="company">Company</label>
    <input id="company" name="company" required maxlength="150"
           value="<%= i.getCompany() == null ? "" : i.getCompany() %>">

    <label for="location">Location</label>
    <input id="location" name="location"
           value="<%= i.getLocation() == null ? "" : i.getLocation() %>">

    <label for="description">Description</label>
    <textarea id="description" name="description"><%= i.getDescription() == null ? "" : i.getDescription() %></textarea>

    <label for="stipend">Stipend</label>
    <input id="stipend" name="stipend"
           value="<%= i.getStipend() == null ? "" : i.getStipend() %>">

    <label for="duration">Duration</label>
    <input id="duration" name="duration"
           value="<%= i.getDuration() == null ? "" : i.getDuration() %>">

    <label for="eligibility">Eligibility</label>
    <textarea id="eligibility" name="eligibility"><%= i.getEligibility() == null ? "" : i.getEligibility() %></textarea>

    <label for="applicationDeadline">Application Deadline</label>
    <input id="applicationDeadline" type="date"
           name="applicationDeadline"
           value="<%= i.getApplicationDeadline() == null ? "" : i.getApplicationDeadline() %>">

    <label for="status">Status</label>
    <select id="status" name="status">
      <option value="Active"
              <%= "Active".equals(i.getStatus()) ? "selected" : "" %>>
        Active
      </option>
      <option value="Closed"
              <%= "Closed".equals(i.getStatus()) ? "selected" : "" %>>
        Closed
      </option>
    </select>

    <button type="submit">Update Internship</button>

    <a class="cancel"
       href="${pageContext.request.contextPath}/internships">
      Cancel
    </a>

  </form>

</div>

</body>
</html>
