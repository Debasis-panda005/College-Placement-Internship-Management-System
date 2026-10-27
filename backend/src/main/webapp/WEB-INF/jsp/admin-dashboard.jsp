<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - College Placement System</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 30px;
        }

        .container {
            width: 95%;
            max-width: 1200px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
        }

        .header-section {
            text-align: center;
            margin-bottom: 25px;
        }

        h1 {
            color: #2c3e50;
            margin-bottom: 6px;
            font-size: 28px;
        }

        .subtitle {
            color: #7f8c8d;
            font-size: 15px;
            margin-top: 0;
        }

        .nav-links {
            text-align: center;
            margin-bottom: 30px;
            padding: 12px;
            background: #f8f9fa;
            border-radius: 6px;
        }

        .nav-links a {
            margin: 0 10px;
            text-decoration: none;
            color: #0066cc;
            font-weight: 500;
            font-size: 14px;
        }

        .nav-links a.active {
            color: #2c3e50;
            font-weight: bold;
        }

        .nav-links form {
            display: inline;
            margin: 0;
            padding: 0;
        }

        .nav-links .btn-logout {
            background: none;
            border: none;
            color: #dc3545;
            font-weight: 500;
            cursor: pointer;
            padding: 0;
            margin: 0 10px;
            font-size: 14px;
            font-family: inherit;
        }

        .nav-links .btn-logout:hover {
            text-decoration: underline;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #842029;
            padding: 12px 16px;
            margin-bottom: 25px;
            border-radius: 5px;
            border: 1px solid #f5c2c7;
        }

        .alert-success {
            background-color: #d1e7dd;
            color: #0f5132;
            padding: 12px 16px;
            margin-bottom: 25px;
            border-radius: 5px;
            border: 1px solid #badbcc;
        }

        .section-title {
            color: #34495e;
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 16px;
            padding-bottom: 8px;
            border-bottom: 2px solid #ecf0f1;
        }

        /* Metric Grid */
        .metrics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
            gap: 20px;
            margin-bottom: 35px;
        }

        .metric-card {
            background: #ffffff;
            border: 1px solid #e1e8ed;
            border-radius: 8px;
            padding: 20px;
            text-align: center;
            transition: transform 0.2s, box-shadow 0.2s;
            box-shadow: 0 1px 4px rgba(0,0,0,0.05);
        }

        .metric-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }

        .metric-card.accent-students { border-top: 4px solid #3498db; }
        .metric-card.accent-jobs { border-top: 4px solid #2ecc71; }
        .metric-card.accent-internships { border-top: 4px solid #9b59b6; }
        .metric-card.accent-active { border-top: 4px solid #1abc9c; }
        .metric-card.accent-applications { border-top: 4px solid #f39c12; }
        .metric-card.accent-shortlisted { border-top: 4px solid #e67e22; }
        .metric-card.accent-selected { border-top: 4px solid #27ae60; }
        .metric-card.accent-interviews { border-top: 4px solid #e74c3c; }

        .metric-number {
            font-size: 32px;
            font-weight: bold;
            color: #2c3e50;
            margin: 10px 0 6px 0;
        }

        .metric-label {
            font-size: 13px;
            color: #7f8c8d;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-weight: 600;
        }

        /* Action Panels */
        .actions-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 20px;
            margin-bottom: 20px;
        }

        .action-card {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 20px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .action-card h3 {
            margin-top: 0;
            margin-bottom: 8px;
            color: #1e293b;
            font-size: 16px;
        }

        .action-card p {
            color: #64748b;
            font-size: 13px;
            margin-bottom: 16px;
            line-height: 1.4;
        }

        .btn-action {
            display: inline-block;
            padding: 9px 16px;
            background-color: #2563eb;
            color: #ffffff;
            text-decoration: none;
            border-radius: 5px;
            font-size: 13px;
            font-weight: 600;
            text-align: center;
            transition: background-color 0.15s;
        }

        .btn-action:hover {
            background-color: #1d4ed8;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="nav-links">
        <a class="active" href="${pageContext.request.contextPath}/admin/dashboard">Admin Dashboard</a> |
        <a href="${pageContext.request.contextPath}/jobs">Jobs</a> |
        <a href="${pageContext.request.contextPath}/internships">Internships</a> |
        <a href="${pageContext.request.contextPath}/applications">All Applications</a> |
        <a href="${pageContext.request.contextPath}/shortlist">Shortlisted Candidates</a> |
        <a href="${pageContext.request.contextPath}/interviews">Interview Schedule</a>
        <c:if test="${not empty sessionScope.userId}">
            |
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display: inline; margin: 0; padding: 0;">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <button type="submit" class="btn-logout">Logout</button>
            </form>
        </c:if>
    </div>

    <div class="header-section">
        <h1>Placement &amp; Internship Admin Dashboard</h1>
        <p class="subtitle">System Overview, Real-Time Key Performance Indicators, and Management Portals</p>
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

    <div class="section-title">Key Performance Indicators</div>
    <div class="metrics-grid">
        <div class="metric-card accent-students">
            <div class="metric-number"><c:out value="${totalStudents}"/></div>
            <div class="metric-label">Total Students</div>
        </div>

        <div class="metric-card accent-jobs">
            <div class="metric-number"><c:out value="${totalJobs}"/></div>
            <div class="metric-label">Total Jobs</div>
        </div>

        <div class="metric-card accent-internships">
            <div class="metric-number"><c:out value="${totalInternships}"/></div>
            <div class="metric-label">Total Internships</div>
        </div>

        <div class="metric-card accent-active">
            <div class="metric-number"><c:out value="${activeOpportunities}"/></div>
            <div class="metric-label">Active Opportunities</div>
        </div>

        <div class="metric-card accent-applications">
            <div class="metric-number"><c:out value="${totalApplications}"/></div>
            <div class="metric-label">Total Applications</div>
        </div>

        <div class="metric-card accent-shortlisted">
            <div class="metric-number"><c:out value="${shortlistedApplications}"/></div>
            <div class="metric-label">Shortlisted Applications</div>
        </div>

        <div class="metric-card accent-selected">
            <div class="metric-number"><c:out value="${selectedApplications}"/></div>
            <div class="metric-label">Selected Applications</div>
        </div>

        <div class="metric-card accent-interviews">
            <div class="metric-number"><c:out value="${scheduledInterviews}"/></div>
            <div class="metric-label">Scheduled Interviews</div>
        </div>
    </div>

    <div class="section-title">Management Portals</div>
    <div class="actions-grid">
        <div class="action-card">
            <div>
                <h3>Jobs Management</h3>
                <p>Browse active full-time and part-time jobs, create new postings, update compensation, and manage application deadlines.</p>
            </div>
            <a href="${pageContext.request.contextPath}/jobs" class="btn-action">Manage Jobs &rarr;</a>
        </div>

        <div class="action-card">
            <div>
                <h3>Internship Management</h3>
                <p>Manage internship opportunities, durations (e.g. 3 Months, 6 Months), monthly stipends, and academic eligibility requirements.</p>
            </div>
            <a href="${pageContext.request.contextPath}/internships" class="btn-action">Manage Internships &rarr;</a>
        </div>

        <div class="action-card">
            <div>
                <h3>All Applications</h3>
                <p>Review comprehensive applicant lists, filter by job or student status, review academic eligibility, and track placement history.</p>
            </div>
            <a href="${pageContext.request.contextPath}/applications" class="btn-action">Review Applications &rarr;</a>
        </div>

        <div class="action-card">
            <div>
                <h3>Shortlisted Candidates</h3>
                <p>Access candidates shortlisted for placement rounds, review interview statuses, and record final hiring selections.</p>
            </div>
            <a href="${pageContext.request.contextPath}/shortlist" class="btn-action">View Shortlist &rarr;</a>
        </div>

        <div class="action-card">
            <div>
                <h3>Interview Schedule</h3>
                <p>Coordinate online and offline interview rounds, manage interview dates/times, and record completion outcomes.</p>
            </div>
            <a href="${pageContext.request.contextPath}/interviews" class="btn-action">Manage Schedule &rarr;</a>
        </div>
    </div>

</div>

</body>
</html>
