<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Jobs &amp; Internships</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 95%;
            margin: auto;
            background: white;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        h1 {
            text-align: center;
            color: #333;
            margin-bottom: 25px;
        }

        .nav-links {
            text-align: center;
            margin-bottom: 25px;
            padding: 10px;
            background: #f8f9fa;
            border-radius: 6px;
        }

        .nav-links a {
            margin: 0 12px;
            text-decoration: none;
            color: #0066cc;
            font-weight: 500;
        }

        .nav-links a.active {
            color: #333;
            font-weight: bold;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #842029;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #f5c2c7;
        }

        .alert-success {
            background-color: #d1e7dd;
            color: #0f5132;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #badbcc;
        }

        .toolbar {
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
            margin-bottom: 20px;
        }

        .btn-add {
            display: inline-block;
            padding: 10px 18px;
            background-color: #1976d2;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
        }

        .btn-add:hover {
            background-color: #125aa0;
        }

        .filters-container {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            align-items: center;
            background: #f8f9fa;
            padding: 15px;
            border-radius: 6px;
            margin-bottom: 20px;
        }

        .filter-form {
            display: inline-flex;
            gap: 8px;
            align-items: center;
        }

        .filter-form input,
        .filter-form select {
            padding: 8px 12px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
        }

        .filter-form button {
            padding: 8px 14px;
            background-color: #495057;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }

        .filter-form button:hover {
            background-color: #343a40;
        }

        .btn-clear {
            color: #6c757d;
            text-decoration: none;
            font-size: 13px;
            margin-left: 4px;
        }

        .btn-clear:hover {
            text-decoration: underline;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }

        th, td {
            padding: 12px;
            border: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #333;
            color: white;
            text-align: center;
        }

        tr:nth-child(even) {
            background-color: #f9f9f9;
        }

        .status-active {
            color: #28a745;
            font-weight: bold;
        }

        .status-closed {
            color: #dc3545;
            font-weight: bold;
        }

        .actions-cell {
            white-space: nowrap;
            text-align: center;
        }

        .action-link {
            text-decoration: none;
            color: #0066cc;
            font-weight: 500;
            margin: 0 4px;
        }

        .action-link:hover {
            text-decoration: underline;
        }

        .inline-form {
            display: inline;
            margin: 0;
            padding: 0;
        }

        .btn-delete {
            background: none;
            border: none;
            color: #dc3545;
            font-weight: 500;
            cursor: pointer;
            padding: 0;
            margin: 0 4px;
            font-size: 14px;
            font-family: inherit;
        }

        .btn-delete:hover {
            text-decoration: underline;
        }

        .empty-row {
            text-align: center;
            color: #777;
            padding: 30px;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="nav-links">
        <a class="active" href="${pageContext.request.contextPath}/jobs">Jobs &amp; Internships</a> |
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Available Jobs &amp; Internships</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            <c:out value="${errorMessage}"/>
        </div>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="alert-success">
            <c:out value="${successMessage}"/>
        </div>
    </c:if>

    <div class="toolbar">
        <a href="${pageContext.request.contextPath}/jobs?action=new" class="btn-add">
            + Add Job / Internship
        </a>
    </div>

    <!-- SEARCH & FILTER BAR -->
    <div class="filters-container">

        <!-- Search -->
        <form action="${pageContext.request.contextPath}/jobs" method="get" class="filter-form">
            <input type="text" name="search" placeholder="Search title, company, location"
                   value="<c:out value='${searchKeyword}'/>" style="width: 240px;">
            <button type="submit">Search</button>
            <c:if test="${not empty searchKeyword}">
                <a href="${pageContext.request.contextPath}/jobs" class="btn-clear">Clear</a>
            </c:if>
        </form>

        <!-- Filter by Type -->
        <form action="${pageContext.request.contextPath}/jobs" method="get" class="filter-form">
            <select name="jobType">
                <option value="">All Job Types</option>
                <option value="Full Time" ${selectedJobType == 'Full Time' ? 'selected' : ''}>Full Time</option>
                <option value="Part Time" ${selectedJobType == 'Part Time' ? 'selected' : ''}>Part Time</option>
                <option value="Internship" ${selectedJobType == 'Internship' ? 'selected' : ''}>Internship</option>
            </select>
            <button type="submit">Filter Type</button>
            <c:if test="${not empty selectedJobType}">
                <a href="${pageContext.request.contextPath}/jobs" class="btn-clear">Clear</a>
            </c:if>
        </form>

        <!-- Filter by Status -->
        <form action="${pageContext.request.contextPath}/jobs" method="get" class="filter-form">
            <select name="status">
                <option value="">All Statuses</option>
                <option value="Active" ${selectedStatus == 'Active' ? 'selected' : ''}>Active</option>
                <option value="Closed" ${selectedStatus == 'Closed' ? 'selected' : ''}>Closed</option>
            </select>
            <button type="submit">Filter Status</button>
            <c:if test="${not empty selectedStatus}">
                <a href="${pageContext.request.contextPath}/jobs" class="btn-clear">Clear</a>
            </c:if>
        </form>

        <!-- Sort -->
        <form action="${pageContext.request.contextPath}/jobs" method="get" class="filter-form">
            <select name="sortBy">
                <option value="">Sort By</option>
                <option value="title" ${selectedSortBy == 'title' ? 'selected' : ''}>Job Title</option>
                <option value="company" ${selectedSortBy == 'company' ? 'selected' : ''}>Company</option>
                <option value="job_type" ${selectedSortBy == 'job_type' ? 'selected' : ''}>Job Type</option>
                <option value="salary" ${selectedSortBy == 'salary' ? 'selected' : ''}>Salary</option>
            </select>
            <button type="submit">Sort</button>
            <c:if test="${not empty selectedSortBy}">
                <a href="${pageContext.request.contextPath}/jobs" class="btn-clear">Clear</a>
            </c:if>
        </form>

    </div>

    <!-- JOBS TABLE -->
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Company</th>
                <th>Location</th>
                <th>Salary</th>
                <th>Job Type</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty jobs}">
                    <c:forEach items="${jobs}" var="job">
                        <tr>
                            <td style="text-align: center;"><c:out value="${job.id}"/></td>
                            <td><strong><c:out value="${job.title}"/></strong></td>
                            <td><c:out value="${job.company}"/></td>
                            <td><c:out value="${job.location}"/></td>
                            <td><c:out value="${job.salary}"/></td>
                            <td style="text-align: center;"><c:out value="${job.jobType}"/></td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${job.status == 'Active'}">
                                        <span class="status-active">Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-closed">Closed</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="actions-cell">
                                <a href="${pageContext.request.contextPath}/jobs?action=view&id=${job.id}" class="action-link">View</a>
                                <span>|</span>
                                <a href="${pageContext.request.contextPath}/jobs?action=edit&id=${job.id}" class="action-link">Edit</a>
                                <span>|</span>
                                <form action="${pageContext.request.contextPath}/jobs" method="post" class="inline-form">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${job.id}">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <button type="submit" class="btn-delete"
                                            onclick="return confirm('Are you sure you want to delete this job?');">
                                        Delete
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="8" class="empty-row">No jobs or internships available.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>

</div>

</body>
</html>
