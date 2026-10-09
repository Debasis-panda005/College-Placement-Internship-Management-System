<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>Edit / Reschedule Interview</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 80%;
            max-width: 650px;
            margin: 40px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            text-align: center;
            color: #222;
            margin-bottom: 25px;
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

        .nav-links a.active {
            color: #333;
            font-weight: bold;
        }

        .form-section {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 8px;
            border: 1px solid #e9ecef;
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
            border-radius: 6px;
            font-size: 14px;
        }

        .readonly-control {
            background-color: #e9ecef;
            cursor: not-allowed;
            color: #495057;
        }

        .btn {
            display: inline-block;
            padding: 10px 20px;
            border: none;
            border-radius: 6px;
            color: white;
            cursor: pointer;
            text-decoration: none;
            font-size: 14px;
        }

        .btn-update {
            background: #0d6efd;
        }

        .btn-cancel {
            background: #6c757d;
            margin-left: 10px;
        }

        .btn:hover {
            opacity: 0.85;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #842029;
            padding: 12px 16px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #f5c2c7;
        }

        .button-bar {
            margin-top: 25px;
            display: flex;
            align-items: center;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a class="active" href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
    </div>

    <h1>Edit / Reschedule Interview</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            ${errorMessage}
        </div>
    </c:if>

    <div class="form-section">
        <form action="${pageContext.request.contextPath}/interviews" method="post">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="${interview.id}">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <div class="form-group">
                <label for="applicationId">Application ID</label>
                <input type="text"
                       id="applicationId"
                       class="form-control readonly-control"
                       value="${interview.applicationId}"
                       readonly>
            </div>

            <div class="form-group">
                <label for="interviewDate">Interview Date</label>
                <input type="date"
                       id="interviewDate"
                       name="interviewDate"
                       class="form-control"
                       value="${interview.interviewDate}"
                       min="${today}"
                       required>
            </div>

            <div class="form-group">
                <label for="interviewTime">Interview Time</label>
                <input type="time"
                       id="interviewTime"
                       name="interviewTime"
                       class="form-control"
                       value="${interview.interviewTime}"
                       required>
            </div>

            <div class="form-group">
                <label for="mode">Mode</label>
                <select id="mode" name="mode" class="form-control" required>
                    <option value="ONLINE" ${interview.mode == 'ONLINE' ? 'selected' : ''}>Online</option>
                    <option value="OFFLINE" ${interview.mode == 'OFFLINE' ? 'selected' : ''}>Offline</option>
                </select>
            </div>

            <div class="button-bar">
                <button type="submit" class="btn btn-update">Update Interview</button>
                <a href="${pageContext.request.contextPath}/interviews" class="btn btn-cancel">Cancel</a>
            </div>
        </form>
    </div>

</div>

</body>
</html>
