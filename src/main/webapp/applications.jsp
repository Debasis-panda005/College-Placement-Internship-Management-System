
<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.job.Application" %>

<%!
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    List<Application> applications =
            (List<Application>) request.getAttribute("applications");

    String filterSearch =
            (String) request.getAttribute("filterSearch");
    String filterJobId =
            (String) request.getAttribute("filterJobId");
    String filterStatus =
            (String) request.getAttribute("filterStatus");
    String filterSort =
            (String) request.getAttribute("filterSort");

    if (filterSearch == null) filterSearch = "";
    if (filterJobId == null) filterJobId = "";
    if (filterStatus == null) filterStatus = "";
    if (filterSort == null) filterSort = "newest";

    String ctx = request.getContextPath();
    String message = request.getParameter("message");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Applications</title>

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
            margin-bottom: 30px;
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

        .back-button {
            display: inline-block;
            padding: 10px 18px;
            background: #1976d2;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-bottom: 20px;
        }

        .filter-panel {
            background: white;
            padding: 18px;
            margin: 0 0 20px;
            border-radius: 6px;
            border: 1px solid #ddd;
        }

        .filter-grid {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 12px;
        }

        .filter-grid label {
            display: block;
            margin-bottom: 6px;
            font-size: 14px;
        }

        .filter-grid input,
        .filter-grid select {
            box-sizing: border-box;
            width: 100%;
            padding: 9px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        button {
            padding: 8px 10px;
            background: #1976d2;
            color: white;
            border: 0;
            border-radius: 4px;
            cursor: pointer;
        }

        .filter-button {
            margin-top: 15px;
            padding: 10px 16px;
        }

        .reset-button {
            display: inline-block;
            margin-left: 8px;
            padding: 10px 16px;
            background: #64748b;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }

        .message {
            margin: 10px 0 20px;
            color: #166534;
        }

        .error {
            color: #b91c1c;
        }

        .table-wrap {
            overflow-x: auto;
        }

        .status-form {
            min-width: 150px;
        }

        .status-form select {
            width: 100%;
            padding: 7px;
            margin-bottom: 6px;
        }

        @media (max-width: 800px) {
            .filter-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 500px) {
            body {
                padding: 12px;
            }

            .filter-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Student Applications</h1>

    <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 20px;">

    <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 20px;">

    <a href="${pageContext.request.contextPath}/JobServlet"
       style="display: inline-block;
              background-color: #1976d2;
              color: white;
              padding: 10px 18px;
              text-decoration: none;
              border: 1px solid #1976d2;
              border-radius: 5px;
              font-weight: 600;">
        Back to Jobs
    </a>

    <a href="${pageContext.request.contextPath}/admin/dashboard"
       style="display: inline-block;
              background-color: #1976d2;
              color: white;
              padding: 10px 18px;
              text-decoration: none;
              border: 1px solid #1976d2;
              border-radius: 5px;
              font-weight: 600;">
        Back to Dashboard
    </a>

</div>

</div>

    <%
        if ("statusUpdated".equals(message)) {
    %>
        <p class="message">Application status updated successfully.</p>
    <%
        } else if ("statusUpdateFailed".equals(message)) {
    %>
        <p class="message error">
            Status was not updated. Please check the application ID and status.
        </p>
    <%
        }
    %>

    <!-- APPLICATION FILTERS -->
    <section class="filter-panel">
        <h2>Filter Applications</h2>

        <form method="get" action="<%=ctx%>/ApplicationServlet">

            <div class="filter-grid">

                <div>
                    <label for="search">Student Name or Email</label>
                    <input type="text"
                           id="search"
                           name="search"
                           placeholder="Search student..."
                           value="<%=esc(filterSearch)%>">
                </div>

                <div>
                    <label for="jobId">Job ID</label>
                    <input type="number"
                           id="jobId"
                           name="jobId"
                           min="1"
                           placeholder="Enter job ID"
                           value="<%=esc(filterJobId)%>">
                </div>

                <div>
                    <label for="status">Application Status</label>
                    <select id="status" name="status">
                        <option value=""
                            <%=filterStatus.isEmpty() ? "selected" : ""%>>
                            All Statuses
                        </option>

                        <option value="Applied"
                            <%="Applied".equals(filterStatus) ? "selected" : ""%>>
                            Applied
                        </option>

                        <option value="Shortlisted"
                            <%="Shortlisted".equals(filterStatus) ? "selected" : ""%>>
                            Shortlisted
                        </option>

                        <option value="Selected"
                            <%="Selected".equals(filterStatus) ? "selected" : ""%>>
                            Selected
                        </option>

                        <option value="Rejected"
                            <%="Rejected".equals(filterStatus) ? "selected" : ""%>>
                            Rejected
                        </option>
                    </select>
                </div>

                <div>
                    <label for="sort">Sort By Date</label>
                    <select id="sort" name="sort">
                        <option value="newest"
                            <%="newest".equals(filterSort) ? "selected" : ""%>>
                            Newest First
                        </option>

                        <option value="oldest"
                            <%="oldest".equals(filterSort) ? "selected" : ""%>>
                            Oldest First
                        </option>
                    </select>
                </div>

            </div>

            <button type="submit" class="filter-button">
                Apply Filters
            </button>

            <a href="<%=ctx%>/ApplicationServlet" class="reset-button">
                Clear Filters
            </a>

        </form>
    </section>

    <!-- APPLICATION RESULTS -->
    <div class="table-wrap">
        <table>

            <tr>
                <th>ID</th>
                <th>Job ID</th>
                <th>Student Name</th>
                <th>Student Email</th>
                <th>Resume</th>
                <th>Applied Date</th>
                <th>Status</th>
            </tr>

            <%
                if (applications != null && !applications.isEmpty()) {
                    for (Application app : applications) {
            %>

            <tr>
                <td><%=app.getId()%></td>
                <td><%=app.getJobId()%></td>
                <td><%=esc(app.getStudentName())%></td>
                <td><%=esc(app.getStudentEmail())%></td>
                <td><%=esc(app.getResume())%></td>
                <td><%=esc(app.getAppliedDate())%></td>

                <td>
                    <form class="status-form"
                          method="post"
                          action="<%=ctx%>/ApplicationServlet">

                        <input type="hidden"
                               name="action"
                               value="updateStatus">

                        <input type="hidden"
                               name="applicationId"
                               value="<%=app.getId()%>">

                        <select name="status" required>
                            <option value="Applied"
                                <%="Applied".equals(app.getStatus())
                                    ? "selected" : ""%>>
                                Applied
                            </option>

                            <option value="Shortlisted"
                                <%="Shortlisted".equals(app.getStatus())
                                    ? "selected" : ""%>>
                                Shortlisted
                            </option>

                            <option value="Selected"
                                <%="Selected".equals(app.getStatus())
                                    ? "selected" : ""%>>
                                Selected
                            </option>

                            <option value="Rejected"
                                <%="Rejected".equals(app.getStatus())
                                    ? "selected" : ""%>>
                                Rejected
                            </option>
                        </select>

                        <button type="submit">Update Status</button>
                    </form>
                </td>
            </tr>

            <%
                    }
                } else {
            %>

            <tr>
                <td colspan="7" style="text-align:center;">
                    No applications match your filters.
                </td>
            </tr>

            <%
                }
            %>

        </table>
    </div>

</div>

</body>
</html>
