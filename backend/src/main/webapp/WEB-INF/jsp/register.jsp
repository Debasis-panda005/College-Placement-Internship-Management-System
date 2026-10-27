<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Student Registration - Placement &amp; Internship Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 580px;
            margin: auto;
            background: white;
            padding: 35px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .header-section {
            text-align: center;
            margin-bottom: 25px;
        }

        h1 {
            color: #333;
            margin-top: 0;
            margin-bottom: 8px;
            font-size: 24px;
        }

        .subtitle {
            color: #6c757d;
            font-size: 14px;
            margin: 0;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #842029;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #f5c2c7;
            font-size: 14px;
        }

        .alert-success {
            background-color: #d1e7dd;
            color: #0f5132;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #badbcc;
            font-size: 14px;
        }

        .form-row {
            display: flex;
            gap: 15px;
        }

        .form-col {
            flex: 1;
        }

        .form-group {
            margin-bottom: 16px;
        }

        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-weight: bold;
            color: #495057;
            font-size: 13px;
        }

        .form-control {
            width: 100%;
            box-sizing: border-box;
            padding: 10px 12px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 14px;
        }

        .form-control:focus {
            border-color: #1976d2;
            outline: none;
            box-shadow: 0 0 0 2px rgba(25,118,210,0.2);
        }

        .btn-submit {
            width: 100%;
            padding: 12px;
            background-color: #1976d2;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-weight: bold;
            font-size: 15px;
            margin-top: 10px;
        }

        .btn-submit:hover {
            background-color: #125aa0;
        }

        .footer-links {
            text-align: center;
            margin-top: 25px;
            padding-top: 18px;
            border-top: 1px solid #eee;
            font-size: 14px;
            color: #555;
        }

        .footer-links a {
            color: #1976d2;
            text-decoration: none;
            font-weight: 500;
        }

        .footer-links a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="header-section">
        <h1>Student Registration</h1>
        <p class="subtitle">Create your student account to apply for campus placements and internships</p>
    </div>

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

    <form action="${pageContext.request.contextPath}/register" method="post" onsubmit="return validateRegisterForm()">

        <!-- CSRF Protection Token -->
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

        <!-- Personal & Account Details -->
        <div class="form-group">
            <label for="name">Full Name *</label>
            <input type="text" id="name" name="name" class="form-control"
                   value="<c:out value='${name}'/>" required placeholder="e.g. John Doe">
        </div>

        <div class="form-group">
            <label for="email">College Email Address *</label>
            <input type="email" id="email" name="email" class="form-control"
                   value="<c:out value='${email}'/>" required placeholder="e.g. student@college.edu">
        </div>

        <div class="form-row">
            <div class="form-col form-group">
                <label for="password">Password * (min. 6 characters)</label>
                <input type="password" id="password" name="password" class="form-control"
                       required placeholder="Create a secure password">
            </div>
            <div class="form-col form-group">
                <label for="confirmPassword">Confirm Password *</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                       required placeholder="Repeat password">
            </div>
        </div>

        <!-- Academic Details -->
        <div class="form-row">
            <div class="form-col form-group">
                <label for="rollNumber">Roll / Registration Number *</label>
                <input type="text" id="rollNumber" name="rollNumber" class="form-control"
                       value="<c:out value='${rollNumber}'/>" required placeholder="e.g. CS2026-042">
            </div>
            <div class="form-col form-group">
                <label for="department">Department / Branch *</label>
                <input type="text" id="department" name="department" class="form-control"
                       value="<c:out value='${department}'/>" required placeholder="e.g. Computer Science">
            </div>
        </div>

        <div class="form-row">
            <div class="form-col form-group">
                <label for="cgpa">Current CGPA * (0.00 - 10.00)</label>
                <input type="number" step="0.01" min="0.00" max="10.00" id="cgpa" name="cgpa" class="form-control"
                       value="<c:out value='${cgpa}'/>" required placeholder="e.g. 8.75">
            </div>
            <div class="form-col form-group">
                <label for="graduationYear">Graduation Year * (2000 - 2100)</label>
                <input type="number" min="2000" max="2100" id="graduationYear" name="graduationYear" class="form-control"
                       value="<c:out value='${graduationYear}'/>" required placeholder="e.g. 2026">
            </div>
        </div>

        <!-- Contact & Profile Details (Optional) -->
        <div class="form-group">
            <label for="phone">Phone Number</label>
            <input type="tel" id="phone" name="phone" class="form-control"
                   value="<c:out value='${phone}'/>" placeholder="e.g. 9876543210">
        </div>

        <div class="form-group">
            <label for="skills">Key Skills</label>
            <input type="text" id="skills" name="skills" class="form-control"
                   value="<c:out value='${skills}'/>" placeholder="e.g. Java, Spring Boot, MySQL, React">
        </div>

        <div class="form-group">
            <label for="resumeUrl">Resume URL</label>
            <input type="url" id="resumeUrl" name="resumeUrl" class="form-control"
                   value="<c:out value='${resumeUrl}'/>" placeholder="e.g. https://drive.google.com/your-resume">
        </div>

        <button type="submit" class="btn-submit">Register as Student</button>

    </form>

    <div class="footer-links">
        Already have an account? <a href="${pageContext.request.contextPath}/login">Log in here</a>
    </div>

</div>

<script>
    function validateRegisterForm() {
        var name = document.getElementById("name").value.trim();
        var email = document.getElementById("email").value.trim();
        var password = document.getElementById("password").value;
        var confirmPassword = document.getElementById("confirmPassword").value;
        var rollNumber = document.getElementById("rollNumber").value.trim();
        var department = document.getElementById("department").value.trim();
        var cgpa = parseFloat(document.getElementById("cgpa").value);
        var year = parseInt(document.getElementById("graduationYear").value, 10);

        if (!name || !email || !password || !confirmPassword || !rollNumber || !department) {
            alert("Please fill in all required fields.");
            return false;
        }

        if (password.length < 6) {
            alert("Password must be at least 6 characters long.");
            return false;
        }

        if (password !== confirmPassword) {
            alert("Passwords do not match.");
            return false;
        }

        if (isNaN(cgpa) || cgpa < 0.0 || cgpa > 10.0) {
            alert("Please enter a valid CGPA between 0.00 and 10.00.");
            return false;
        }

        if (isNaN(year) || year < 2000 || year > 2100) {
            alert("Please enter a valid graduation year between 2000 and 2100.");
            return false;
        }

        return true;
    }
</script>

</body>
</html>
