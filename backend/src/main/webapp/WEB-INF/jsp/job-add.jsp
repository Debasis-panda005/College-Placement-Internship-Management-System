<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Add Job / Internship</title>
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
        <a href="${pageContext.request.contextPath}/jobs">Jobs &amp; Internships</a> |
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Add New Job / Internship</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            <c:out value="${errorMessage}"/>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/jobs" method="post" onsubmit="return validateJobForm()">

        <input type="hidden" name="action" value="create">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

        <div class="form-group">
            <label for="title">Job Title *</label>
            <input type="text" id="title" name="title" class="form-control"
                   value="<c:out value='${job.title}'/>" required placeholder="e.g. Software Engineer Intern">
        </div>

        <div class="form-group">
            <label for="company">Company Name *</label>
            <input type="text" id="company" name="company" class="form-control"
                   value="<c:out value='${job.company}'/>" required placeholder="e.g. Acme Technologies">
        </div>

        <div class="form-group">
            <label for="location">Location *</label>
            <input type="text" id="location" name="location" class="form-control"
                   value="<c:out value='${job.location}'/>" required placeholder="e.g. Bangalore, India (or Remote)">
        </div>

        <div class="form-group">
            <label for="description">Job Description</label>
            <textarea id="description" name="description" class="form-control"
                      placeholder="Brief role responsibilities and criteria"><c:out value="${job.description}"/></textarea>
        </div>

        <div class="form-group">
            <label for="salary">Salary / Stipend</label>
            <input type="text" id="salary" name="salary" class="form-control"
                   value="<c:out value='${job.salary}'/>" placeholder="e.g. 50,000 / month or 8 LPA">
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
            <label for="status">Status *</label>
            <select id="status" name="status" class="form-control" required>
                <option value="Active" ${empty job.status || job.status == 'Active' ? 'selected' : ''}>Active</option>
                <option value="Closed" ${job.status == 'Closed' ? 'selected' : ''}>Closed</option>
            </select>
        </div>

        <div class="button-bar">
            <button type="submit" class="btn-submit">Create Job</button>
            <a href="${pageContext.request.contextPath}/jobs" class="btn-cancel">Cancel</a>
        </div>

    </form>

</div>

<script>
    function validateJobForm() {
        var title = document.getElementById("title").value.trim();
        var company = document.getElementById("company").value.trim();
        var location = document.getElementById("location").value.trim();
        var jobType = document.getElementById("jobType").value;

        if (!title || !company || !location || !jobType) {
            alert("Please fill in all required fields (Title, Company, Location, Job Type).");
            return false;
        }
        return true;
    }
</script>

</body>
</html>
