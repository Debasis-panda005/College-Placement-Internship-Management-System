<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Application" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Student Applications</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>
<div class="container">

    <div class="details-page-header">
        <h1>Student Applications</h1>
    </div>

    <%
        String message = request.getParameter("message");

        if ("statusUpdated".equals(message)) {
    %>
    <div class="message">Application status updated successfully.</div>
    <%
        }
    %>

    <table>
        <tr>
            <th>ID</th>
            <th>Job ID</th>
            <th>Student Name</th>
            <th>Student Email</th>
            <th>Resume</th>
            <th>Applied Date</th>
            <th>Current Status</th>
            <th>Update Status</th>
        </tr>

        <%
            List<Application> applications =
                    (List<Application>) request.getAttribute("applications");

            if (applications != null && !applications.isEmpty()) {
                for (Application app : applications) {

                    String status = app.getStatus();

                    if (status == null || status.isEmpty()) {
                        status = "Pending";
                    }

                    String statusClass = status.toLowerCase();
        %>

        <tr>
            <td><%= app.getId() %></td>
            <td><%= app.getJobId() %></td>
            <td><%= app.getStudentName() %></td>
            <td><%= app.getStudentEmail() %></td>
            <td><%= app.getResume() %></td>
            <td><%= app.getAppliedDate() %></td>

            <td>
                <span class="status <%= statusClass %>">
                    <%= status %>
                </span>
            </td>

            <td>
                <form action="ApplicationServlet"
                      method="post"
                      class="status-form">

                    <input type="hidden"
                           name="action"
                           value="updateStatus">

                    <input type="hidden"
                           name="id"
                           value="<%= app.getId() %>">

                    <select name="status" required>
                        <option value="Pending"
                                <%= "Pending".equals(status) ? "selected" : "" %>>
                            Pending
                        </option>

                        <option value="Shortlisted"
                                <%= "Shortlisted".equals(status) ? "selected" : "" %>>
                            Shortlisted
                        </option>

                        <option value="Selected"
                                <%= "Selected".equals(status) ? "selected" : "" %>>
                            Selected
                        </option>

                        <option value="Rejected"
                                <%= "Rejected".equals(status) ? "selected" : "" %>>
                            Rejected
                        </option>
                    </select>

                    <button type="submit">Update</button>
                </form>
            </td>
        </tr>

        <%
            }
        } else {
        %>

        <tr>
            <td colspan="8" style="text-align:center;">
                No applications available.
            </td>
        </tr>

        <%
            }
        %>

    </table>

    <div class="page-actions">
        <a href="JobServlet" class="back-button">Back to Jobs</a>

        <a href="<%= request.getContextPath() %>/admin/dashboard"
           class="dashboard-button">
            Back to Dashboard
        </a>
    </div>

</div>
</body>
</html>