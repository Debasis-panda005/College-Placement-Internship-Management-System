<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Internship" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Internship Management</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="internship-header">
    <h1>Internship Management</h1>
</div>

<div class="internship-toolbar">
    <a class="add-button"
       href="${pageContext.request.contextPath}/internships?action=new">
        + Add Internship
    </a>
</div>

<%
    String message = request.getParameter("message");

    if ("added".equals(message)) {
%>
<p class="message">Internship added successfully.</p>
<%
} else if ("updated".equals(message)) {
%>
<p class="message">Internship updated successfully.</p>
<%
} else if ("deleted".equals(message)) {
%>
<p class="message">Internship deleted successfully.</p>
<%
    }

    List<Internship> internships =
            (List<Internship>) request.getAttribute("internships");
%>

<div class="internship-table-wrapper">
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
            <td colspan="9" class="empty">
                No internships found. Use Add Internship to create one.
            </td>
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
            <td><%= internship.getApplicationDeadline() == null ? "" : internship.getApplicationDeadline() %></td>
            <td><%= internship.getStatus() == null ? "" : internship.getStatus() %></td>

            <td class="actions">
                <a class="action-link"
                   href="<%= request.getContextPath() %>/internships?action=view&id=<%= internship.getId() %>">View</a>

                <a class="action-link"
                   href="<%= request.getContextPath() %>/internships?action=edit&id=<%= internship.getId() %>">Edit</a>

                <a class="action-link"
                   href="<%= request.getContextPath() %>/internships?action=delete&id=<%= internship.getId() %>"
                   onclick="return confirm('Delete this internship?')">Delete</a>
            </td>
        </tr>
        <%  }
        } %>
        </tbody>
    </table>
</div>

<div class="dashboard-footer">
    <a class="dashboard-button"
       href="${pageContext.request.contextPath}/index.jsp">
        &#8592; Back to Home
    </a>
</div>

</body>
</html>