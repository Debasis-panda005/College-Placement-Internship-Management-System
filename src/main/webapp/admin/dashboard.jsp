 <%-- College Placement & Internship Management System --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | College Placement & Internship Management System</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            color: #1f2937;
        }

        /* Existing navbar theme */
        .navbar {
            background: #1f2937;
            color: #ffffff;
            padding: 18px 30px;
            font-size: 21px;
            font-weight: 700;
        }

        .navbar-inner {
            max-width: 1250px;
            margin: 0 auto;
        }

        .container {
            max-width: 1250px;
            margin: 0 auto;
            padding: 35px 30px 45px;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            gap: 20px;
            flex-wrap: wrap;
            margin-bottom: 30px;
        }

        .page-header h1 {
            margin: 0 0 10px;
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.5px;
        }

        .page-header p {
            margin: 0;
            color: #6b7280;
            font-size: 14px;
            line-height: 1.6;
        }

        .section-heading {
            margin: 0 0 16px;
            font-size: 17px;
            font-weight: 700;
        }

        /* Statistics cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 22px;
            margin-bottom: 35px;
        }

        .stat-card {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 2px 8px rgba(31, 41, 55, 0.04);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }

        .stat-card:hover,
        .module-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 7px 18px rgba(31, 41, 55, 0.08);
        }

        .stat-label {
            margin: 0 0 14px;
            color: #6b7280;
            font-size: 14px;
            font-weight: 600;
        }

        .stat-number {
            margin: 0 0 18px;
            font-size: 34px;
            line-height: 1.1;
            font-weight: 700;
            color: #1f2937;
        }

        .card-link {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: #2563eb;
            text-decoration: none;
            font-size: 13px;
            font-weight: 600;
        }

        .card-link:hover {
            text-decoration: underline;
        }

        /* Management module cards */
        .modules-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 22px;
        }

        .module-card {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            min-height: 185px;
            padding: 24px;
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(31, 41, 55, 0.04);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }

        .module-number {
            margin-bottom: 18px;
            color: #6b7280;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 1px;
        }

        .module-card h3 {
            margin: 0 0 10px;
            font-size: 17px;
            font-weight: 700;
        }

        .module-card p {
            margin: 0 0 20px;
            color: #6b7280;
            font-size: 13px;
            line-height: 1.6;
        }

        .module-card .card-link {
            margin-top: auto;
        }

        .footer {
            margin-top: 38px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
            color: #6b7280;
            font-size: 12px;
            text-align: center;
        }

        @media (max-width: 900px) {
            .modules-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 600px) {
            .navbar {
                padding: 17px 20px;
                font-size: 17px;
                line-height: 1.5;
            }

            .container {
                padding: 25px 18px 35px;
            }

            .page-header h1 {
                font-size: 24px;
            }

            .stats-grid,
            .modules-grid {
                grid-template-columns: 1fr;
                gap: 15px;
            }

            .stat-card {
                padding: 22px;
            }

            .module-card {
                min-height: 165px;
                padding: 22px;
            }
        }

        @media (prefers-reduced-motion: reduce) {
            .stat-card,
            .module-card {
                transition: none;
            }
        }
    </style>
</head>

<body>

    <header class="navbar">
        <div class="navbar-inner">
            College Placement &amp; Internship Management System
        </div>
    </header>

    <main class="container">

        <section class="page-header">
            <div>
                <h1>Admin Dashboard</h1>
                <p>Manage placement activities, job opportunities and student applications.</p>
            </div>
        </section>

        <section aria-labelledby="overview-heading">
            <h2 class="section-heading" id="overview-heading">Overview</h2>

            <div class="stats-grid">

                <article class="stat-card">
                    <p class="stat-label">Total Jobs</p>
                    <p class="stat-number">${totalJobs}</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/jobs.jsp">
                        Manage jobs &rarr;
                    </a>
                </article>

                <article class="stat-card">
                    <p class="stat-label">Total Applications</p>
                    <p class="stat-number">${totalApplications}</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/ApplicationServlet">
                        View applications &rarr;
                    </a>
                </article>

            </div>
        </section>

        <section aria-labelledby="management-heading">
            <h2 class="section-heading" id="management-heading">Management</h2>

            <div class="modules-grid">

                <article class="module-card">
                    <span class="module-number">STUDENTS</span>
                    <h3>Student Management</h3>
                    <p>Access the student area and related placement information.</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/">
                        Open student area &rarr;
                    </a>
                </article>

                <article class="module-card">
                    <span class="module-number">COMPANIES</span>
                    <h3>Company Management</h3>
                    <p>Access the system area for company-related information.</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/">
                        Open company area &rarr;
                    </a>
                </article>

                <article class="module-card">
                    <span class="module-number">JOBS</span>
                    <h3>Job Management</h3>
                    <p>View available jobs and manage existing job postings.</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/jobs.jsp">
                        Manage jobs &rarr;
                    </a>
                </article>

                <article class="module-card">
                    <span class="module-number">INTERNSHIPS</span>
                    <h3>Internship Opportunities</h3>
                    <p>Browse the opportunities currently available in the system.</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/jobs.jsp">
                        Browse opportunities &rarr;
                    </a>
                </article>

                <article class="module-card">
                    <span class="module-number">APPLICATIONS</span>
                    <h3>Application Management</h3>
                    <p>Review submitted applications and monitor application activity.</p>
                    <a class="card-link"
                       href="${pageContext.request.contextPath}/ApplicationServlet">
                        View applications &rarr;
                    </a>
                </article>

            </div>
        </section>

        <footer class="footer">
            College Placement &amp; Internship Management System
        </footer>

    </main>

</body>
</html>