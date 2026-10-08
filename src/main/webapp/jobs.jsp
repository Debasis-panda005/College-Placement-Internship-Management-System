<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Job" %>

<!DOCTYPE html>
<html>
<head>
    <title>Job & Internship List</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 95%;
            margin: auto;
        }

        h1 {
            text-align: center;
        }

        .add-button {
            display: inline-block;
            padding: 10px 18px;
            background: #1976d2;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-bottom: 20px;
        }

        .message {
            padding: 12px;
            margin-bottom: 20px;
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
            border-radius: 5px;
            text-align: center;
            font-weight: bold;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
        }

        th, td {
            padding: 12px;
            border: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #1976d2;
            color: white;
        }

        tr:nth-child(even) {
            background: #f9f9f9;
        }

        .action-link {
            text-decoration: none;
            white-space: nowrap;
        }

        /* Keep all action links on one line */
        .action-cell {
            white-space: nowrap;
            width: 220px;
        }

        .active-status {
            color: green;
            font-weight: bold;
        }

        .closed-status {
            color: red;
            font-weight: bold;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Available Jobs & Internships</h1>

    <a href="add-job.jsp" class="add-button">
        + Add Job / Internship
    </a>


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
        }
    %>


    <!-- SEARCH -->

    <form action="JobServlet"
          method="get"
          style="margin-bottom: 20px;">

        <input type="text"
               name="search"
               placeholder="Search by title, company or location"
               style="width: 300px; padding: 10px;">

        <button type="submit"
                style="padding: 10px 18px;">
            Search
        </button>

        <a href="JobServlet"
           style="margin-left: 10px;">
            Clear
        </a>

    </form>


    <!-- FILTER -->

    <form action="JobServlet"
          method="get"
          style="margin-bottom: 20px;">

        <select name="jobType"
                style="padding: 10px; width: 200px;">

            <option value="">All Job Types</option>

            <option value="Full Time">
                Full Time
            </option>

            <option value="Part Time">
                Part Time
            </option>

            <option value="Internship">
                Internship
            </option>

        </select>

        <button type="submit"
                style="padding: 10px 18px;">
            Filter
        </button>

        <a href="JobServlet"
           style="margin-left: 10px;">
            Clear
        </a>

    </form>


    <!-- STATUS FILTER -->

    <form action="JobServlet"
          method="get"
          style="margin-bottom: 20px;">

        <select name="status"
                style="padding: 10px; width: 200px;">

            <option value="">
                All Statuses
            </option>

            <option value="Active">
                Active
            </option>

            <option value="Closed">
                Closed
            </option>

        </select>

        <button type="submit"
                style="padding: 10px 18px;">
            Filter Status
        </button>

        <a href="JobServlet"
           style="margin-left: 10px;">
            Clear
        </a>

    </form>


    <!-- SORT -->

    <form action="JobServlet"
          method="get"
          style="margin-bottom: 20px;">

        <select name="sortBy"
                style="padding: 10px; width: 200px;">

            <option value="">
                Sort Jobs By
            </option>

            <option value="title">
                Job Title
            </option>

            <option value="company">
                Company
            </option>

            <option value="jobType">
                Job Type
            </option>

        </select>

        <button type="submit"
                style="padding: 10px 18px;">
            Sort
        </button>

        <a href="JobServlet"
           style="margin-left: 10px;">
            Clear
        </a>

    </form>


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

            </td>$env:CATALINA_HOME="D:\apache-tomcat-11.0.24"

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

</div>

</body>
</html>