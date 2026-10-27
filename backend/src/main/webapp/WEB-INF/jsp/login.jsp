<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Placement &amp; Internship Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 420px;
            margin: 60px auto auto auto;
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

        .form-group {
            margin-bottom: 18px;
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
        <h1>Welcome Back</h1>
        <p class="subtitle">Log in to your placement and internship portal</p>
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

    <form action="${pageContext.request.contextPath}/login" method="post" onsubmit="return validateLoginForm()">

        <!-- CSRF Protection Token -->
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

        <div class="form-group">
            <label for="email">Email Address</label>
            <input type="email" id="email" name="email" class="form-control"
                   value="<c:out value='${email}'/>" required autofocus placeholder="e.g. student@college.edu">
        </div>

        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" class="form-control"
                   required placeholder="Enter your password">
        </div>

        <button type="submit" class="btn-submit">Log In</button>

    </form>

    <div class="footer-links">
        Don't have an account? <a href="${pageContext.request.contextPath}/register">Register here</a>
    </div>

</div>

<script>
    function validateLoginForm() {
        var email = document.getElementById("email").value.trim();
        var password = document.getElementById("password").value;

        if (!email || !password) {
            alert("Please enter both email and password.");
            return false;
        }
        return true;
    }
</script>

</body>
</html>
