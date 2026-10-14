
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Internship" %>

<%
  List<Internship> internships =
          (List<Internship>) request.getAttribute("internships");

  String contextPath = request.getContextPath();
  String message = request.getParameter("message");
  String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Internship Management</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 30px; background: #f5f7fb; }
    h1 { color: #243b53; }
    .top { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    .btn { display: inline-block; padding: 9px 13px; border-radius: 5px;
      color: white; text-decoration: none; background: #2563eb; }
    .delete { background: #dc2626; }
    .notice { padding: 12px; background: #dcfce7; margin: 15px 0; }
    .error { padding: 12px; background: #fee2e2; margin: 15px 0; }
    table { width: 100%; border-collapse: collapse; background: white; margin-top: 20px; }
    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    th { background: #e8eef8; }
    .actions { display: flex; flex-wrap: wrap; gap: 6px; }
    @media(max-width: 750px) {
      .table-wrap { overflow-x: auto; }
      table { min-width: 750px; }
    }
  </style>
</head>
<body>

<div class="top">
  <h1>Internship Management</h1>
  <a class="btn" href="<%= contextPath %>/internships?action=new">
    + Add Internship
  </a>
</div>

<p>
  <a href="<%= contextPath %>/jobs">View Jobs</a>
  |
  <a href="<%= contextPath %>/admin/dashboard.jsp">Admin Dashboard</a>
</p>

<% if ("added".equals(message)) { %>
<div class="notice">Internship added successfully.</div>
<% } else if ("updated".equals(message)) { %>
<div class="notice">Internship updated successfully.</div>
<% } else if ("deleted".equals(message)) { %>
<div class="notice">Internship deleted successfully.</div>
<% } %>

<% if (error != null) { %>
<div class="error">The operation failed. Please check the server logs.</div>
<% } %>

<div class="table-wrap">
  <table>
    <thead>
    <tr>
      <th>ID</th>
      <th>Title</th>
      <th>Company</th>
      <th>Location</th>
      <th>Stipend</th>
      <th>Duration</th>
      <th>Deadline</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <% if (internships == null || internships.isEmpty()) { %>
    <tr>
      <td colspan="9">No internships available yet.</td>
    </tr>
    <% } else {
      for (Internship internship : internships) { %>
    <tr>
      <td><%= internship.getId() %></td>
      <td><%= internship.getTitle() %></td>
      <td><%= internship.getCompany() %></td>
      <td><%= internship.getLocation() == null ? "" : internship.getLocation() %></td>
      <td><%= internship.getStipend() == null ? "" : internship.getStipend() %></td>
      <td><%= internship.getDuration() == null ? "" : internship.getDuration() %></td>
      <td><%= internship.getApplicationDeadline() == null
              ? "" : internship.getApplicationDeadline() %></td>
      <td><%= internship.getStatus() == null ? "" : internship.getStatus() %></td>
      <td>
        <div class="actions">
          <a class="btn"
             href="<%= contextPath %>/internships?action=view&id=<%= internship.getId() %>">
            View
          </a>
          <a class="btn"
             href="<%= contextPath %>/internships?action=edit&id=<%= internship.getId() %>">
            Edit
          </a>
          <a class="btn delete"
             href="<%= contextPath %>/internships?action=delete&id=<%= internship.getId() %>"
             onclick="return confirm('Delete this internship?');">
            Delete
          </a>
        </div>
      </td>
    </tr>
    <%  }
    } %>
    </tbody>
  </table>
</div>

</body>
</html>
