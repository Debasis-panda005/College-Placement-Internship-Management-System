
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Student Management</title>
<style>
    * { box-sizing: border-box; }

    body {
        margin: 0;
        font-family: Arial, sans-serif;
        background: #f4f6f9;
        color: #1f2937;
    }

    header {
        background: #1f2937;
        color: white;
        padding: 18px 24px;
        font-size: 20px;
        font-weight: bold;
    }

    main {
        max-width: 950px;
        margin: 35px auto;
        padding: 0 20px;
    }

    .panel {
        background: white;
        padding: 28px;
        border: 1px solid #e1e5eb;
        border-radius: 10px;
    }

    .muted { color: #64748b; line-height: 1.7; }

    a {
        display: inline-block;
        margin-top: 16px;
        color: #0757e8;
        text-decoration: none;
    }

    a:hover { text-decoration: underline; }
</style>
</head>
<body>
<header>College Placement &amp; Internship Management System</header>

<main>
    <section class="panel">
        <h1>Student Management</h1>

        <p class="muted">
            Welcome to the Student Management module.
        </p>

        <p class="muted">
            This page is a starting point. Student registration,
            student records, editing, and deletion still need to
            be connected to a student database and servlet.
        </p>

        <a href="${pageContext.request.contextPath}/admin/dashboard">
            &larr; Back to Admin Dashboard
        </a>
    </section>
</main>
</body>
</html>
