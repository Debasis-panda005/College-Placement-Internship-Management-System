<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | College Placement System</title>
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
            width: 340px;
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
        .success { color: #18733b; }
        .message { text-align: center; }
    </style>
</head>
<body>
<div class="container">
    <h1>Login</h1>

    <% if (request.getAttribute("error") != null) { %>
    <p class="error message"><%= request.getAttribute("error") %></p>
    <% } %>

    <% if ("true".equals(request.getParameter("registered"))) { %>
    <p class="success message">Registration successful. Please log in.</p>
    <% } %>

    <form action="${pageContext.request.contextPath}/login" method="post">
        <label for="email">Email</label>
        <input type="email" id="email" name="email"
               autocomplete="email" required>

        <label for="password">Password</label>
        <input type="password" id="password" name="password"
               autocomplete="current-password" required>

        <button type="submit">Log In</button>
    </form>

    <p class="message">
        Don't have an account?
        <a href="${pageContext.request.contextPath}/register">Register</a>
    </p>
</div>
</body>
</html>