<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>College Placement & Internship Management</title>
  <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="container">
  <div class="details-page-header">
    <h1>College Placement & Internship Management System</h1>
  </div>

  <h2>Choose a Module</h2>

  <ul>
    <li>
      <a href="${pageContext.request.contextPath}/JobServlet">
        Jobs
      </a>
    </li>
    <li>
      <a href="${pageContext.request.contextPath}/internships">
        Internships
      </a>
    </li>
  </ul>
</div>

</body>
</html>
