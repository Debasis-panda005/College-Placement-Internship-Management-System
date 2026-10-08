<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>

    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background-color: #f4f6f9;
        }

        .navbar {
            background-color: #1f2937;
            color: white;
            padding: 18px 30px;
            font-size: 22px;
            font-weight: bold;
        }

        .container {
            padding: 30px;
        }

        .welcome {
            margin-bottom: 25px;
        }

        .welcome h1 {
            margin-bottom: 5px;
        }

        .cards {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
        }

        .card {
            background-color: white;
            width: 220px;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .card h2 {
            margin: 0 0 10px;
        }

        .card p {
            color: #666;
        }

        .card a {
            display: inline-block;
            margin-top: 10px;
            text-decoration: none;
            color: #2563eb;
            font-weight: bold;
        }
    </style>
</head>

<body>

<div class="navbar">
    College Placement &amp; Internship Management System
</div>

<div class="container">

    <div class="welcome">
        <h1>Admin Dashboard</h1>
        <p>Welcome to the Admin Panel</p>
    </div>

    <div class="cards">

    <div class="card">
        <h2>Students</h2>
        <p>Manage registered students</p>
        <a href="#">Manage Students</a>
    </div>

    <div class="card">
        <h2>Companies</h2>
        <p>Manage companies</p>
        <a href="#">Manage Companies</a>
    </div>

    <div class="card">
        <h2>Jobs</h2>
        <p>Total Jobs: ${totalJobs}</p>
        <a href="#">Manage Jobs</a>
    </div>

    <div class="card">
        <h2>Internships</h2>
        <p>Manage internship opportunities</p>
        <a href="#">Manage Internships</a>
    </div>

    <div class="card">
        <h2>Applications</h2>
        <p>Total Applications: ${totalApplications}</p>
        <a href="#">Manage Applications</a>
    </div>

</div>

</div>

</body>
</html>