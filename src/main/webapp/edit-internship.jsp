
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
  <title>Edit Internship</title>

  <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="details-page-header">
  <h1>Edit Internship</h1>
</div>

<form method="post"
      action="<%= request.getContextPath() %>/internships"
      class="edit-internship-form">

  <input type="hidden" name="action" value="update">
  <input type="hidden" name="id" value="<%= internship.getId() %>">

  <label for="title">Title</label>
  <input id="title" name="title" required
         value="<%= internship.getTitle() == null ? "" : internship.getTitle() %>">

  <label for="company">Company</label>
  <input id="company" name="company" required
         value="<%= internship.getCompany() == null ? "" : internship.getCompany() %>">

  <label for="location">Location</label>
  <input id="location" name="location"
         value="<%= internship.getLocation() == null ? "" : internship.getLocation() %>">

  <label for="description">Description</label>
  <textarea id="description" name="description"><%= internship.getDescription() == null ? "" : internship.getDescription() %></textarea>

  <label for="stipend">Stipend</label>
  <input id="stipend" name="stipend"
         value="<%= internship.getStipend() == null ? "" : internship.getStipend() %>">

  <label for="duration">Duration</label>
  <input id="duration" name="duration"
         value="<%= internship.getDuration() == null ? "" : internship.getDuration() %>">

  <label for="eligibility">Eligibility</label>
  <textarea id="eligibility" name="eligibility"><%= internship.getEligibility() == null ? "" : internship.getEligibility() %></textarea>

  <label for="applicationDeadline">Application deadline</label>
  <input type="date" id="applicationDeadline"
         name="applicationDeadline"
         value="<%= internship.getApplicationDeadline() == null ? "" : internship.getApplicationDeadline() %>">

  <label for="status">Status</label>
  <select id="status" name="status">
    <option value="Active"
            <%= "Active".equals(internship.getStatus()) ? "selected" : "" %>>
      Active
    </option>
    <option value="Inactive"
            <%= "Inactive".equals(internship.getStatus()) ? "selected" : "" %>>
      Inactive
    </option>
  </select>

  <button type="submit">Save Changes</button>
</form>

<div class="form-actions">
  <a class="btn btn-secondary"
     href="<%= request.getContextPath() %>/internships">
    Cancel
  </a>
</div>

</body>
</html>
