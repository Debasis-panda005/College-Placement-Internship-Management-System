<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register | College Placement System</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }
        .container {
            width: 360px;
            max-width: calc(100% - 48px);
            background: white;
            padding: 28px;
            border-radius: 10px;
            box-shadow: 0 2px 12px rgba(0,0,0,.1);
        }
        h1 { text-align: center; }
        label { display: block; margin-top: 16px; }
        input {
            box-sizing: border-box;
            width: 100%;
            padding: 11px;
            margin-top: 6px;
        }
        button {
            width: 100%;
            padding: 12px;
            margin-top: 22px;
            border: 0;
            border-radius: 5px;
            background: #2457c5;
            color: white;
            cursor: pointer;
        }
        .error { color: #b00020; }
        .message { text-align: center; }
    </style>
</head>
<body>
<div class="container">
    <h1>Create Account</h1>

    <% if (request.getAttribute("error") != null) { %>
    <p class="error message">${requestScope.error}</p>
    <% } %>

    <form action="${pageContext.request.contextPath}/register" method="post">
        <label for="name">Full Name</label>
        <input type="text" id="name" name="name"
               autocomplete="name" maxlength="100" required>

        <label for="email">Email</label>
        <input type="email" id="email" name="email"
               autocomplete="email" maxlength="150" required>

        <label for="password">Password (at least 8 characters)</label>
        <input type="password" id="password" name="password"
               autocomplete="new-password" minlength="8" required>

        <button type="submit">Register</button>
    </form>

    <p class="message">
        Already have an account?
        <a href="${pageContext.request.contextPath}/login">Log in</a>
    </p>
</div>
</body>
</html>