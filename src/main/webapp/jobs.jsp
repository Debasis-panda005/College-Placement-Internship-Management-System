<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Job" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Job & Internship List</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="container">

    <div class="details-page-header">
        <h1>Available Jobs & Internships</h1>
    </div>

    <div class="jobs-actions">
        <a href="add-job.jsp" class="add-button">
            + Add Job / Internship
        </a>

        <a href="ApplicationServlet" class="add-button">
            View Applications
        </a>
    </div>

    <!-- SUCCESS MESSAGE -->

    <%
        String message = (String) request.getAttribute("message");

        if ("added".equals(message)) {
    %>

    <div class="message">
        Job added successfully!
    </div>

    <%
    } else if ("updated".equals(message)) {
    %>

    <div class="message">
        Job updated successfully!
    </div>

    <%
    } else if ("deleted".equals(message)) {
    %>

    <div class="message">
        Job deleted successfully!
    </div>

    <%
    } else if ("applicationSubmitted".equals(message)) {
    %>

    <div class="message">
        Application submitted successfully!
    </div>

    <%
        }
    %>

    <!-- CONTROLS TOOLBAR -->
    <div class="jobs-toolbar">

        <!-- SEARCH -->
        <form action="JobServlet"
              method="get"
              class="toolbar-group">

            <input type="text"
                   name="search"
                   placeholder="Search by title, company or location"
                   value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">

            <button type="submit">
                Search
            </button>

            <a href="JobServlet"
               class="btn-clear">
                Clear
            </a>

        </form>

        <!-- FILTER -->
        <form action="JobServlet"
              method="get"
              class="toolbar-group">

            <select name="jobType">
                <option value="">All Job Types</option>
                <option value="Full Time" <%= "Full Time".equals(request.getParameter("jobType")) ? "selected" : "" %>>
                    Full Time
                </option>
                <option value="Part Time" <%= "Part Time".equals(request.getParameter("jobType")) ? "selected" : "" %>>
                    Part Time
                </option>
                <option value="Internship" <%= "Internship".equals(request.getParameter("jobType")) ? "selected" : "" %>>
                    Internship
                </option>
            </select>

            <button type="submit">
                Filter
            </button>

            <a href="JobServlet"
               class="btn-clear">
                Clear
            </a>

        </form>

        <!-- STATUS FILTER -->
        <form action="JobServlet"
              method="get"
              class="toolbar-group">

            <select name="status">
                <option value="">
                    All Statuses
                </option>
                <option value="Active" <%= "Active".equals(request.getParameter("status")) ? "selected" : "" %>>
                    Active
                </option>
                <option value="Closed" <%= "Closed".equals(request.getParameter("status")) ? "selected" : "" %>>
                    Closed
                </option>
            </select>

            <button type="submit">
                Filter Status
            </button>

            <a href="JobServlet"
               class="btn-clear">
                Clear
            </a>

        </form>

        <!-- SORT -->
        <form action="JobServlet"
              method="get"
              class="toolbar-group">

            <select name="sortBy">
                <option value="">
                    Sort Jobs By
                </option>
                <option value="title" <%= "title".equals(request.getParameter("sortBy")) ? "selected" : "" %>>
                    Job Title
                </option>
                <option value="company" <%= "company".equals(request.getParameter("sortBy")) ? "selected" : "" %>>
                    Company
                </option>
                <option value="jobType" <%= "jobType".equals(request.getParameter("sortBy")) ? "selected" : "" %>>
                    Job Type
                </option>
            </select>

            <button type="submit">
                Sort
            </button>

            <a href="JobServlet"
               class="btn-clear">
                Clear
            </a>

        </form>

    </div>


    <!-- JOB TABLE -->

    <table>

        <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Company</th>
            <th>Location</th>
            <th>Description</th>
            <th>Salary</th>
            <th>Job Type</th>
            <th>Status</th>
            <th class="action-cell">Action</th>
        </tr>


        <%

            List<Job> jobs =
                    (List<Job>) request.getAttribute("jobs");

            if (jobs != null && !jobs.isEmpty()) {

                for (Job job : jobs) {

        %>

        <tr>

            <td>
                <%= job.getId() %>
            </td>

            <td>
                <%= job.getTitle() %>
            </td>

            <td>
                <%= job.getCompany() %>
            </td>

            <td>
                <%= job.getLocation() %>
            </td>

            <td>
                <%= job.getDescription() %>
            </td>

            <td>
                <%= job.getSalary() %>
            </td>

            <td>
                <%= job.getJobType() %>
            </td>


            <!-- STATUS -->

            <td>

                <%
                    if ("Active".equals(job.getStatus())) {
                %>

                <span class="active-status">
                    Active
                </span>

                <%
                } else {
                %>

                <span class="closed-status">
                    Closed
                </span>

                <%
                    }
                %>

            </td>


            <!-- ACTIONS -->

            <td>

                <div style="
        display: flex;
        align-items: center;
        white-space: nowrap;
        min-width: 230px;
    ">

                    <a href="JobServlet?action=view&id=<%= job.getId() %>"
                       class="action-link">
                        View
                    </a>

                    <span>&nbsp; | &nbsp;</span>

                    <a href="JobServlet?action=edit&id=<%= job.getId() %>"
                       class="action-link">
                        Edit
                    </a>

                    <span>&nbsp; | &nbsp;</span>

                    <a href="JobServlet?action=delete&id=<%= job.getId() %>"
                       class="action-link"
                       onclick="return confirm('Are you sure you want to delete this job?');">
                        Delete
                    </a>

                    <span>&nbsp; | &nbsp;</span>

                    <a href="JobServlet?action=apply&id=<%= job.getId() %>"
                       class="action-link">
                        Apply
                    </a>

                </div>

            </td>

        </tr>

        <%

            }

        } else {

        %>

        <tr>

            <td colspan="9"
                style="text-align:center;">
                No jobs available.
            </td>

        </tr>

        <%

            }

        %>

    </table>

    <div class="dashboard-footer">
        <a href="<%= request.getContextPath() %>/admin/dashboard"
           class="dashboard-button">
            Back to Dashboard
        </a>
    </div>

</div>

</body>
</html>