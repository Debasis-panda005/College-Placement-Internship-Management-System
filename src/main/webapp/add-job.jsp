<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Add Job</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 40px;
        }

        .container {
            width: 500px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }

        h2 {
            text-align: center;
        }

        label {
            display: block;
            margin-top: 15px;
            font-weight: bold;
        }

        input,
        textarea,
        select {
            width: 100%;
            padding: 10px;
            margin-top: 5px;
            box-sizing: border-box;
        }

        textarea {
            height: 100px;
        }

        button {
            width: 100%;
            margin-top: 20px;
            padding: 12px;
            background: #1976d2;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }

        button:hover {
            background: #125aa0;
        }
    </style>
</head>

<body>

<div class="container">

    <h2>Add Job / Internship</h2>

    <form action="JobServlet"
          method="post"
          onsubmit="return validateForm()">

        <label>Job Title</label>
        <input type="text"
               id="title"
               name="title"
               required>

        <label>Company</label>
        <input type="text"
               id="company"
               name="company"
               required>

        <label>Location</label>
        <input type="text"
               id="location"
               name="location"
               required>

        <label>Description</label>
        <textarea name="description"></textarea>

        <label>Salary</label>
        <input type="text"
               id="salary"
               name="salary"
               required>

        <label>Job Type</label>
        <select id="jobType"
                name="jobType"
                required>

            <option value="">Select Job Type</option>
            <option value="Full Time">Full Time</option>
            <option value="Part Time">Part Time</option>
            <option value="Internship">Internship</option>

        </select>

        <button type="submit">
            Add Job
        </button>

    </form>

</div>

<script>

    function validateForm() {

        let title = document.getElementById("title").value.trim();
        let company = document.getElementById("company").value.trim();
        let location = document.getElementById("location").value.trim();
        let salary = document.getElementById("salary").value.trim();
        let jobType = document.getElementById("jobType").value;

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

        return true;
    }

</script>

</body>
</html>