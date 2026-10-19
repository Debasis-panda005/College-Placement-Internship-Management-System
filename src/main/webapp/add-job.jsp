<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Job</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body class="compact-page">

<div class="compact-container">

<div class="details-page-header">
    <h2>Add Job / Internship</h2>
</div>

<form action="JobServlet"
      method="post"
      onsubmit="return validateForm()"
      class="add-job-form">

    <div class="form-grid">
        <div class="form-group">
            <label for="title">Job Title</label>
            <input type="text"
                   id="title"
                   name="title"
                   required>
        </div>

        <div class="form-group">
            <label for="company">Company</label>
            <input type="text"
                   id="company"
                   name="company"
                   required>
        </div>

        <div class="form-group">
            <label for="location">Location</label>
            <input type="text"
                   id="location"
                   name="location"
                   required>
        </div>

        <div class="form-group">
            <label for="salary">Salary</label>
            <input type="text"
                   id="salary"
                   name="salary"
                   required>
        </div>

        <div class="form-group">
            <label for="jobType">Job Type</label>
            <select id="jobType"
                    name="jobType"
                    required>
                <option value="">Select Job Type</option>
                <option value="Full Time">Full Time</option>
                <option value="Part Time">Part Time</option>
                <option value="Internship">Internship</option>
            </select>
        </div>

        <div class="form-group">
            <label for="status">Status</label>
            <select id="status"
                    name="status"
                    required>
                <option value="Active">Active</option>
                <option value="Closed">Closed</option>
            </select>
        </div>

        <div class="form-group form-group-full">
            <label for="description">Description</label>
            <textarea id="description"
                      name="description"></textarea>
        </div>
    </div>

    <div class="form-actions">
        <button type="submit">
            Add Job
        </button>
        <a href="JobServlet" class="btn btn-secondary">
            ← Back to Jobs
        </a>
    </div>

</form>

</div>

<script>

    function validateForm() {

        let title =
            document.getElementById("title").value.trim();

        let company =
            document.getElementById("company").value.trim();

        let location =
            document.getElementById("location").value.trim();

        let salary =
            document.getElementById("salary").value.trim();

        let jobType =
            document.getElementById("jobType").value;

        let status =
            document.getElementById("status").value;


        if (title === "") {
            alert("Please enter the job title.");
            return false;
        }

        if (company === "") {
            alert("Please enter the company name.");
            return false;
        }

        if (location === "") {
            alert("Please enter the location.");
            return false;
        }

        if (salary === "") {
            alert("Please enter the salary.");
            return false;
        }

        if (jobType === "") {
            alert("Please select a job type.");
            return false;
        }

        if (status === "") {
            alert("Please select a status.");
            return false;
        }

        return true;
    }

</script>

</body>
</html>