<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><c:out value="${job.isInternship() ? 'Edit Internship' : 'Edit Job / Internship'}"/></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 550px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
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

        .nav-links form {
            display: inline;
            margin: 0;
            padding: 0;
        }

        .nav-links .btn-logout {
            background: none;
            border: none;
            color: #dc3545;
            font-weight: 500;
            cursor: pointer;
            padding: 0;
            margin: 0 12px;
            font-size: 14px;
            font-family: inherit;
        }

        .nav-links .btn-logout:hover {
            text-decoration: underline;
        }

        h1 {
            text-align: center;
            color: #333;
            margin-top: 0;
            margin-bottom: 25px;
            font-size: 22px;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #842029;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #f5c2c7;
        }

        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-weight: bold;
            color: #495057;
            font-size: 14px;
        }

        .form-control {
            width: 100%;
            box-sizing: border-box;
            padding: 10px 12px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 14px;
        }

        .readonly-control {
            background-color: #e9ecef;
            cursor: not-allowed;
            color: #495057;
        }

        textarea.form-control {
            height: 90px;
            resize: vertical;
        }

        .button-bar {
            margin-top: 25px;
            display: flex;
            gap: 12px;
            align-items: center;
        }

        .btn-submit {
            padding: 10px 22px;
            background-color: #1976d2;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-weight: bold;
            font-size: 14px;
        }

        .btn-submit:hover {
            background-color: #125aa0;
        }

        .btn-cancel {
            color: #6c757d;
            text-decoration: none;
            font-size: 14px;
        }

        .btn-cancel:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/jobs">Jobs</a> |
        <a href="${pageContext.request.contextPath}/internships">Internships</a> |
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
        <c:if test="${not empty sessionScope.userId}">
            |
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display: inline; margin: 0; padding: 0;">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn-logout">Logout</button>
            </form>
        </c:if>
    </div>

    <h1><c:out value="${job.isInternship() ? 'Edit Internship' : 'Edit Job / Internship'}"/></h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            <c:out value="${errorMessage}"/>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/jobs" method="post" onsubmit="return validateJobEditForm()">

        <input type="hidden" name="action" value="update">
        <input type="hidden" name="id" value="${job.id}">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <c:if test="${job.isInternship() || isInternshipsEndpoint}">
            <input type="hidden" name="source" value="internships">
        </c:if>

        <div class="form-group">
            <label>Job ID</label>
            <input type="text" class="form-control readonly-control" value="<c:out value='${job.id}'/>" readonly>
        </div>

        <div class="form-group">
            <label for="title">Job Title *</label>
            <input type="text" id="title" name="title" class="form-control"
                   value="<c:out value='${job.title}'/>" required>
        </div>

        <div class="form-group">
            <label for="company">Company Name *</label>
            <input type="text" id="company" name="company" class="form-control"
                   value="<c:out value='${job.company}'/>" required>
        </div>

        <div class="form-group">
            <label for="location">Location *</label>
            <input type="text" id="location" name="location" class="form-control"
                   value="<c:out value='${job.location}'/>" required>
        </div>

        <div class="form-group">
            <label for="description">Job Description</label>
            <textarea id="description" name="description" class="form-control"><c:out value="${job.description}"/></textarea>
        </div>

        <div class="form-group">
            <label for="salary"><c:out value="${job.isInternship() ? 'Stipend' : 'Salary / Stipend'}"/></label>
            <input type="text" id="salary" name="salary" class="form-control"
                   value="<c:out value='${job.salary}'/>">
        </div>

        <div class="form-group" id="durationGroup">
            <label for="duration">Internship Duration (Optional)</label>
            <input type="text" id="duration" name="duration" class="form-control"
                   value="<c:out value='${job.duration}'/>" placeholder="e.g. 6 Months, 3 Months, Summer 2026">
        </div>

        <div class="form-group">
            <label for="jobType">Job Type *</label>
            <select id="jobType" name="jobType" class="form-control" required>
                <option value="">Select Job Type</option>
                <option value="Full Time" ${job.jobType == 'Full Time' ? 'selected' : ''}>Full Time</option>
                <option value="Part Time" ${job.jobType == 'Part Time' ? 'selected' : ''}>Part Time</option>
                <option value="Internship" ${job.jobType == 'Internship' ? 'selected' : ''}>Internship</option>
            </select>
        </div>

        <div class="form-group">
            <label for="deadline">Application Deadline</label>
            <input type="date" id="deadline" name="deadline" class="form-control"
                   value="<c:out value='${job.deadline}'/>">
        </div>

        <div class="form-group">
            <label for="minCgpa">Minimum CGPA (0.00 – 10.00)</label>
            <input type="number" step="0.01" min="0.00" max="10.00" id="minCgpa" name="minCgpa" class="form-control"
                   value="<c:out value='${job.minCgpa}'/>" placeholder="e.g. 7.50 (leave blank for none)">
        </div>

        <div class="form-group">
            <label for="eligibleDepartments">Eligible Departments</label>
            <input type="text" id="eligibleDepartments" name="eligibleDepartments" class="form-control"
                   value="<c:out value='${job.eligibleDepartments}'/>" placeholder="e.g. CSE,ECE (or blank / ALL for unrestricted)">
        </div>

        <div class="form-group">
            <label for="status">Status *</label>
            <select id="status" name="status" class="form-control" required>
                <option value="Active" ${job.status == 'Active' ? 'selected' : ''}>Active</option>
                <option value="Closed" ${job.status == 'Closed' ? 'selected' : ''}>Closed</option>
            </select>
        </div>

        <div class="button-bar">
            <button type="submit" class="btn-submit">
                <c:out value="${job.isInternship() ? 'Update Internship' : 'Update Job'}"/>
            </button>
            <a href="${pageContext.request.contextPath}${job.isInternship() || isInternshipsEndpoint ? '/internships' : '/jobs'}" class="btn-cancel">Cancel</a>
        </div>

    </form>

</div>

<script>
    function validateJobEditForm() {
        var title = document.getElementById("title").value.trim();
        var company = document.getElementById("company").value.trim();
        var location = document.getElementById("location").value.trim();
        var jobType = document.getElementById("jobType").value;

        if (!title || !company || !location || !jobType) {
            alert("Please fill in all required fields (Title, Company, Location, Job Type).");
            return false;
        }

        var minCgpaInput = document.getElementById("minCgpa").value.trim();
        if (minCgpaInput !== "") {
            var cgpa = parseFloat(minCgpaInput);
            if (isNaN(cgpa) || cgpa < 0.00 || cgpa > 10.00) {
                alert("Minimum CGPA must be a number between 0.00 and 10.00.");
                return false;
            }
        }

        return true;
    }
</script>

</body>
</html>
