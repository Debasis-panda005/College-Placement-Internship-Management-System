<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="dashboard" scope="request" />
<c:set var="pageTitle" value="Admin Dashboard | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="portal-container">

    <!-- Header Section -->
    <div style="margin-bottom: 28px;">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
            <span class="badge badge-gold" style="font-size: 11px; padding: 4px 10px; font-weight: 700;">
                <i class="fas fa-shield-alt"></i> Administrative Console
            </span>
            <span class="badge badge-primary" style="font-size: 11px; padding: 4px 10px;">Live Real-Time Telemetry</span>
        </div>
        <h1 style="font-size: 28px; margin: 0 0 6px 0; color: var(--navy-900);">
            <i class="fas fa-tachometer-alt" style="color: var(--gold-500); margin-right: 8px;"></i>Placement &amp; Internship Admin Dashboard
        </h1>
        <p style="color: var(--text-muted); font-size: 14px; margin: 0;">
            Real-time operational indicators across registered students, opportunity drives, applicant evaluations, and multi-stage interview rounds.
        </p>
    </div>

    <!-- Flash Alerts -->
    <c:if test="${not empty errorMessage}">
        <div class="portal-alert portal-alert-danger" role="alert">
            <i class="fas fa-exclamation-circle" style="font-size: 18px;"></i>
            <div><c:out value="${errorMessage}"/></div>
        </div>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="portal-alert portal-alert-success" role="alert">
            <i class="fas fa-check-circle" style="font-size: 18px;"></i>
            <div><c:out value="${successMessage}"/></div>
        </div>
    </c:if>

    <!-- KPI Metrics Section -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3 style="font-size: 18px; margin: 0; color: var(--navy-900); display: flex; align-items: center; gap: 8px;">
            <i class="fas fa-chart-line" style="color: var(--navy-700);"></i> Live Placement Performance
        </h3>
        <span style="font-size: 12px; color: var(--text-muted);">
            <i class="fas fa-sync-alt"></i> Auto-synced with MySQL Database
        </span>
    </div>

    <div class="kpi-grid">
        <a href="${pageContext.request.contextPath}/applications" class="kpi-card kpi-blue clickable-card" title="Click to view candidate applications">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.totalStudents ? stats.totalStudents : (not empty totalStudents ? totalStudents : 0)}"/></div>
                    <div class="kpi-label">Registered Students</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(27, 54, 93, 0.08); display: flex; align-items: center; justify-content: center; color: var(--navy-700);">
                    <i class="fas fa-user-graduate" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--navy-700); display: flex; align-items: center; gap: 4px;">
                View Student Profiles <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/jobs?type=Full+Time" class="kpi-card kpi-green clickable-card" title="Click to view Full-Time opportunities">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.totalJobs ? stats.totalJobs : (not empty totalJobs ? totalJobs : 0)}"/></div>
                    <div class="kpi-label">Full-Time Jobs</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(46, 125, 50, 0.1); display: flex; align-items: center; justify-content: center; color: var(--success);">
                    <i class="fas fa-briefcase" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--success); display: flex; align-items: center; gap: 4px;">
                View Full-Time Postings <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/jobs?type=Internship" class="kpi-card kpi-gold clickable-card" title="Click to view Internship opportunities">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.totalInternships ? stats.totalInternships : (not empty totalInternships ? totalInternships : 0)}"/></div>
                    <div class="kpi-label">Internship Postings</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(230, 154, 24, 0.12); display: flex; align-items: center; justify-content: center; color: var(--gold-600);">
                    <i class="fas fa-laptop-code" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--gold-600); display: flex; align-items: center; gap: 4px;">
                View Internships <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/jobs?status=Active" class="kpi-card clickable-card" title="Click to view Active hiring drives">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.activeJobs ? stats.activeJobs : (not empty activeJobs ? activeJobs : (not empty activeOpportunities ? activeOpportunities : 0))}"/></div>
                    <div class="kpi-label">Active Drives</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(16, 36, 62, 0.08); display: flex; align-items: center; justify-content: center; color: var(--navy-800);">
                    <i class="fas fa-bullhorn" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--text-muted); display: flex; align-items: center; gap: 4px;">
                Filter Active Drives <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/applications" class="kpi-card kpi-blue clickable-card" title="Click to view all applications">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.totalApplications ? stats.totalApplications : (not empty totalApplications ? totalApplications : 0)}"/></div>
                    <div class="kpi-label">Total Applications</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(27, 54, 93, 0.08); display: flex; align-items: center; justify-content: center; color: var(--navy-700);">
                    <i class="fas fa-file-signature" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--navy-700); display: flex; align-items: center; gap: 4px;">
                Review All Applications <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/shortlist" class="kpi-card kpi-gold clickable-card" title="Click to manage shortlisted students">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.shortlistedApplications ? stats.shortlistedApplications : (not empty shortlistedApplications ? shortlistedApplications : 0)}"/></div>
                    <div class="kpi-label">Shortlisted Pool</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(230, 154, 24, 0.12); display: flex; align-items: center; justify-content: center; color: var(--gold-600);">
                    <i class="fas fa-user-check" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--gold-600); display: flex; align-items: center; gap: 4px;">
                Manage Shortlist <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/applications?filterStatus=SELECTED" class="kpi-card kpi-green clickable-card" title="Click to view placed candidates">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.selectedApplications ? stats.selectedApplications : (not empty selectedApplications ? selectedApplications : 0)}"/></div>
                    <div class="kpi-label">Final Selections</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(46, 125, 50, 0.1); display: flex; align-items: center; justify-content: center; color: var(--success);">
                    <i class="fas fa-trophy" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--success); display: flex; align-items: center; gap: 4px;">
                Placed Candidates <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/interviews" class="kpi-card clickable-card" title="Click to view scheduled interview calendar">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <div class="kpi-value"><c:out value="${not empty stats.scheduledInterviews ? stats.scheduledInterviews : (not empty scheduledInterviews ? scheduledInterviews : 0)}"/></div>
                    <div class="kpi-label">Active Interviews</div>
                </div>
                <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(16, 36, 62, 0.08); display: flex; align-items: center; justify-content: center; color: var(--navy-800);">
                    <i class="fas fa-calendar-alt" style="font-size: 18px;"></i>
                </div>
            </div>
            <div style="margin-top: 12px; font-size: 12px; font-weight: 600; color: var(--navy-700); display: flex; align-items: center; gap: 4px;">
                Interview Calendar <i class="fas fa-arrow-right" style="font-size: 10px;"></i>
            </div>
        </a>
    </div>

    <!-- Management Portals & Operations Grid -->
    <h3 style="font-size: 18px; margin: 36px 0 16px 0; color: var(--navy-900); display: flex; align-items: center; gap: 8px;">
        <i class="fas fa-th-large" style="color: var(--navy-700);"></i> Operational Management Portals
    </h3>

    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px; margin-bottom: 32px;">
        <!-- Portal Card 1: Jobs & Drives -->
        <div class="portal-card" style="margin-bottom: 0; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <div style="width: 44px; height: 44px; border-radius: 10px; background: rgba(230, 154, 24, 0.15); display: flex; align-items: center; justify-content: center; color: var(--gold-600); font-size: 20px;">
                        <i class="fas fa-bullhorn"></i>
                    </div>
                    <div>
                        <h4 style="margin: 0; font-size: 16px; color: var(--navy-900);">Post Opportunity / Internship</h4>
                        <span style="font-size: 12px; color: var(--text-muted);">Recruitment Drives</span>
                    </div>
                </div>
                <p style="font-size: 13px; color: var(--text-muted); line-height: 1.5; margin-bottom: 20px;">
                    Publish and configure company recruitment drives with eligibility criteria including minimum CGPA thresholds, allowable department filters, job descriptions, and strict deadlines.
                </p>
            </div>
            <div style="display: flex; gap: 8px;">
                <a href="${pageContext.request.contextPath}/jobs?action=add" class="btn btn-primary btn-sm" style="flex: 1; text-align: center;">
                    <i class="fas fa-plus-circle"></i> Create Posting
                </a>
                <a href="${pageContext.request.contextPath}/jobs" class="btn btn-outline btn-sm" title="View all job drives">
                    <i class="fas fa-list"></i>
                </a>
            </div>
        </div>

        <!-- Portal Card 2: Shortlist & Evaluation -->
        <div class="portal-card" style="margin-bottom: 0; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <div style="width: 44px; height: 44px; border-radius: 10px; background: rgba(27, 54, 93, 0.12); display: flex; align-items: center; justify-content: center; color: var(--navy-700); font-size: 20px;">
                        <i class="fas fa-user-check"></i>
                    </div>
                    <div>
                        <h4 style="margin: 0; font-size: 16px; color: var(--navy-900);">Shortlist &amp; Evaluation</h4>
                        <span style="font-size: 12px; color: var(--text-muted);">Candidate Screening</span>
                    </div>
                </div>
                <p style="font-size: 13px; color: var(--text-muted); line-height: 1.5; margin-bottom: 20px;">
                    Filter student candidate pools by CGPA, backlogs, and departmental eligibility. Approve candidate shortlists and transition applicants directly into multi-stage interview rounds.
                </p>
            </div>
            <div style="display: flex; gap: 8px;">
                <a href="${pageContext.request.contextPath}/shortlist" class="btn btn-accent btn-sm" style="flex: 1; text-align: center;">
                    <i class="fas fa-filter"></i> Open Shortlist Manager
                </a>
                <a href="${pageContext.request.contextPath}/applications" class="btn btn-outline btn-sm" title="View all applicant records">
                    <i class="fas fa-users"></i>
                </a>
            </div>
        </div>

        <!-- Portal Card 3: Dynamic Interview Rounds -->
        <div class="portal-card" style="margin-bottom: 0; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <div style="width: 44px; height: 44px; border-radius: 10px; background: rgba(46, 125, 50, 0.12); display: flex; align-items: center; justify-content: center; color: var(--success); font-size: 20px;">
                        <i class="fas fa-calendar-check"></i>
                    </div>
                    <div>
                        <h4 style="margin: 0; font-size: 16px; color: var(--navy-900);">Interview Round Schedules</h4>
                        <span style="font-size: 12px; color: var(--text-muted);">Dynamic Multi-Round Pipeline</span>
                    </div>
                </div>
                <p style="font-size: 13px; color: var(--text-muted); line-height: 1.5; margin-bottom: 20px;">
                    Monitor candidate progression across Online Assessment, Technical Interview, and HR evaluation. Record round evaluations, manage schedules, and issue PDF placement reports.
                </p>
            </div>
            <div style="display: flex; gap: 8px;">
                <a href="${pageContext.request.contextPath}/interviews" class="btn btn-primary btn-sm" style="flex: 1; text-align: center;">
                    <i class="fas fa-calendar-alt"></i> View Schedule
                </a>
            </div>
        </div>
    </div>

    <!-- Coordinator Registration Requests Section -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin: 36px 0 16px 0;">
        <h3 style="font-size: 18px; margin: 0; color: var(--navy-900); display: flex; align-items: center; gap: 8px;">
            <i class="fas fa-user-shield" style="color: var(--gold-500);"></i> Coordinator Registration Requests
        </h3>
        <span class="badge ${pendingCoordinatorCount > 0 ? 'badge-gold' : 'badge-primary'}" style="font-size: 12px; padding: 4px 10px;">
            <i class="fas fa-clock"></i> <c:out value="${pendingCoordinatorCount != null ? pendingCoordinatorCount : 0}"/> Pending Approval
        </span>
    </div>

    <div class="portal-card" style="padding: 0; overflow: hidden; margin-bottom: 32px; border: 1px solid var(--border-light); border-radius: var(--radius-md); box-shadow: var(--shadow-sm);">
        <c:choose>
            <c:when test="${empty coordinatorRequests}">
                <div style="text-align: center; padding: 36px 20px; background: white;">
                    <div style="font-size: 2rem; margin-bottom: 8px; color: var(--gold-500);">
                        <i class="fas fa-user-check"></i>
                    </div>
                    <h4 style="margin: 0 0 6px 0; color: var(--navy-900); font-size: 1.05rem; font-weight: 700;">No Coordinator Requests</h4>
                    <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 440px; margin: 0 auto; line-height: 1.5;">
                        There are currently no new or pending placement coordinator registration requests in the system.
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table class="portal-table data-table" style="margin: 0; width: 100%; border-collapse: collapse; font-size: 0.9rem;">
                        <thead style="background: var(--navy-900); color: #ffffff;">
                            <tr>
                                <th style="padding: 12px 14px; text-align: left;">Coordinator Name</th>
                                <th style="padding: 12px 14px; text-align: left;">Institutional Email</th>
                                <th style="padding: 12px 14px; text-align: left;">Employee ID</th>
                                <th style="padding: 12px 14px; text-align: left;">Department</th>
                                <th style="padding: 12px 14px; text-align: left;">Designation</th>
                                <th style="padding: 12px 14px; text-align: center;">Registration Date</th>
                                <th style="padding: 12px 14px; text-align: center;">Approval Status</th>
                                <th style="padding: 12px 14px; text-align: center;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="req" items="${coordinatorRequests}">
                                <tr style="border-bottom: 1px solid var(--border-light);">
                                    <td style="padding: 12px 14px; font-weight: 600; color: var(--navy-900);">
                                        <i class="fas fa-user-tie" style="margin-right: 6px; color: var(--gold-600);"></i>
                                        <c:out value="${req.coordinatorName != null ? req.coordinatorName : 'Coordinator'}"/>
                                    </td>
                                    <td style="padding: 12px 14px; color: var(--text-muted);">
                                        <i class="fas fa-envelope" style="margin-right: 4px; font-size: 0.8rem;"></i>
                                        <c:out value="${req.institutionalEmail}"/>
                                    </td>
                                    <td style="padding: 12px 14px; font-family: monospace; font-weight: 700; color: var(--navy-800);">
                                        <c:out value="${req.employeeId}"/>
                                    </td>
                                    <td style="padding: 12px 14px;">
                                        <c:out value="${req.department}"/>
                                    </td>
                                    <td style="padding: 12px 14px; color: var(--text-muted);">
                                        <c:out value="${req.designation}"/>
                                    </td>
                                    <td style="padding: 12px 14px; text-align: center; white-space: nowrap; font-size: 0.85rem;">
                                        <c:out value="${req.createdAt}"/>
                                    </td>
                                    <td style="padding: 12px 14px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${req.status == 'PENDING_APPROVAL'}">
                                                <span class="badge badge-gold" style="font-size: 11px; padding: 4px 8px; font-weight: 700;">
                                                    <i class="fas fa-hourglass-half"></i> PENDING
                                                </span>
                                            </c:when>
                                            <c:when test="${req.status == 'APPROVED'}">
                                                <span class="badge" style="background: rgba(46, 125, 50, 0.15); color: var(--success); font-size: 11px; padding: 4px 8px; font-weight: 700;">
                                                    <i class="fas fa-check-circle"></i> APPROVED
                                                </span>
                                            </c:when>
                                            <c:when test="${req.status == 'REJECTED'}">
                                                <span class="badge" style="background: rgba(211, 47, 47, 0.15); color: var(--danger); font-size: 11px; padding: 4px 8px; font-weight: 700;">
                                                    <i class="fas fa-times-circle"></i> REJECTED
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="font-size: 11px; padding: 4px 8px;"><c:out value="${req.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="padding: 12px 14px; text-align: center;">
                                        <div style="display: inline-flex; gap: 6px; align-items: center; justify-content: center; flex-wrap: wrap;">
                                            <!-- View Details Button -->
                                            <button type="button" class="btn btn-outline btn-sm"
                                                    style="padding: 5px 11px; font-size: 12px; font-weight: 600; border: 1px solid var(--navy-800); color: var(--navy-800); border-radius: 4px; background: transparent; cursor: pointer; display: inline-flex; align-items: center; gap: 4px;"
                                                    title="View Coordinator Dossier"
                                                    onclick="viewCoordinatorDetails('${req.id}', '<c:out value="${req.coordinatorName}"/>', '<c:out value="${req.institutionalEmail}"/>', '<c:out value="${req.employeeId}"/>', '<c:out value="${req.department}"/>', '<c:out value="${req.designation}"/>', '<c:out value="${req.phone}"/>', '<c:out value="${req.status}"/>', '<c:out value="${req.createdAt}"/>', '<c:out value="${req.approvedByName}"/>', '<c:out value="${req.approvalDate}"/>', '<c:out value="${req.adminNotes}"/>');">
                                                <i class="fas fa-eye"></i> Details
                                            </button>

                                            <!-- Approve & Reject Actions: Displayed exclusively for coordinators with PENDING_APPROVAL status -->
                                            <c:if test="${req.status == 'PENDING_APPROVAL'}">
                                                <!-- Approve Action (Green) -->
                                                <form method="post" action="${pageContext.request.contextPath}/admin/coordinators" style="display: inline; margin: 0;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="approve">
                                                    <input type="hidden" name="id" value="${req.id}">
                                                    <button type="submit" class="btn btn-sm"
                                                            style="background: #2e7d32; color: #ffffff; padding: 5px 11px; font-size: 12px; font-weight: 600; border: none; border-radius: 4px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.12);"
                                                            title="Authorize and approve coordinator request"
                                                            onclick="return confirm('Authorize and approve coordinator access for <c:out value="${req.coordinatorName}"/>?');">
                                                        <i class="fas fa-check"></i> Approve
                                                    </button>
                                                </form>

                                                <!-- Reject Action (Red) -->
                                                <form method="post" action="${pageContext.request.contextPath}/admin/coordinators" style="display: inline; margin: 0;" onsubmit="return handleRejectCoordinator(this, '<c:out value="${req.coordinatorName}"/>');">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="reject">
                                                    <input type="hidden" name="id" value="${req.id}">
                                                    <input type="hidden" name="adminNotes" value="">
                                                    <button type="submit" class="btn btn-sm"
                                                            style="background: #c62828; color: #ffffff; padding: 5px 11px; font-size: 12px; font-weight: 600; border: none; border-radius: 4px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.12);"
                                                            title="Reject coordinator request">
                                                        <i class="fas fa-times"></i> Reject
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- External Candidate Recruitment Management Section -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin: 36px 0 16px 0;">
        <h3 style="font-size: 18px; margin: 0; color: var(--navy-900); display: flex; align-items: center; gap: 8px;">
            <i class="fa-solid fa-graduation-cap" style="color: var(--gold-500);"></i> External Candidate Recruitment Management
        </h3>
        <span class="badge ${pendingApprovals > 0 ? 'badge-gold' : 'badge-primary'}" style="font-size: 12px; padding: 4px 10px;">
            <i class="fa-solid fa-clock"></i> <c:out value="${pendingApprovals != null ? pendingApprovals : 0}"/> Pending Verification
        </span>
    </div>

    <!-- External Portal KPI Grid -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 14px; margin-bottom: 20px;">
        <div class="kpi-card" style="padding: 16px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Total Registrations</div>
            <div style="font-size: 24px; font-weight: 800; color: var(--navy-900); margin: 4px 0;">
                <c:out value="${not empty totalExternalStudents ? totalExternalStudents : 0}"/>
            </div>
            <div style="font-size: 11px; color: var(--text-muted);">External Applicants</div>
        </div>
        <div class="kpi-card kpi-gold" style="padding: 16px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--gold-600); text-transform: uppercase;">Pending Verification</div>
            <div style="font-size: 24px; font-weight: 800; color: var(--gold-600); margin: 4px 0;">
                <c:out value="${not empty pendingApprovals ? pendingApprovals : 0}"/>
            </div>
            <div style="font-size: 11px; color: var(--gold-600);">Requires Admin Action</div>
        </div>
        <div class="kpi-card" style="padding: 16px; border-left: 4px solid var(--success);">
            <div style="font-size: 11px; font-weight: 700; color: var(--success); text-transform: uppercase;">Approved Candidates</div>
            <div style="font-size: 24px; font-weight: 800; color: var(--success); margin: 4px 0;">
                <c:out value="${not empty approvedStudents ? approvedStudents : 0}"/>
            </div>
            <div style="font-size: 11px; color: var(--text-muted);">Active &amp; Eligible</div>
        </div>
        <div class="kpi-card" style="padding: 16px; border-left: 4px solid #ef4444;">
            <div style="font-size: 11px; font-weight: 700; color: #ef4444; text-transform: uppercase;">Rejected</div>
            <div style="font-size: 24px; font-weight: 800; color: #ef4444; margin: 4px 0;">
                <c:out value="${not empty rejectedStudents ? rejectedStudents : 0}"/>
            </div>
            <div style="font-size: 11px; color: var(--text-muted);">Not Verified</div>
        </div>
        <div class="kpi-card kpi-blue" style="padding: 16px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--navy-700); text-transform: uppercase;">External Applications</div>
            <div style="font-size: 24px; font-weight: 800; color: var(--navy-700); margin: 4px 0;">
                <c:out value="${not empty externalApplications ? externalApplications : 0}"/>
            </div>
            <div style="font-size: 11px; color: var(--text-muted);">Opportunities Applied</div>
        </div>
        <div class="kpi-card" style="padding: 16px; border-left: 4px solid var(--accent-teal);">
            <div style="font-size: 11px; font-weight: 700; color: var(--accent-teal); text-transform: uppercase;">Selected / Placed</div>
            <div style="font-size: 24px; font-weight: 800; color: var(--accent-teal); margin: 4px 0;">
                <c:out value="${not empty externalSelected ? externalSelected : 0}"/>
            </div>
            <div style="font-size: 11px; color: var(--text-muted);">Offers Conferred</div>
        </div>
    </div>

    <!-- External Directory Filter & Search Form -->
    <div class="portal-card" style="padding: 18px 22px; margin-bottom: 20px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); background: white;">
        <form action="${pageContext.request.contextPath}/admin/dashboard" method="get">
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)) 160px; gap: 12px; align-items: flex-end;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" style="font-size: 0.78rem; font-weight: 700;">Candidate Name or Email</label>
                    <input type="text" name="extQuery" class="form-control" placeholder="e.g. John Doe or email" value="<c:out value='${selectedExtQuery}'/>" style="height: 38px;" />
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" style="font-size: 0.78rem; font-weight: 700;">Verification Status</label>
                    <select name="extStatus" class="form-control" style="height: 38px;">
                        <option value="ALL" ${selectedExtStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                        <option value="PENDING" ${selectedExtStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="APPROVED" ${selectedExtStatus == 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                        <option value="REJECTED" ${selectedExtStatus == 'REJECTED' ? 'selected' : ''}>REJECTED</option>
                    </select>
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" style="font-size: 0.78rem; font-weight: 700;">Degree / Course</label>
                    <input type="text" name="extDegree" class="form-control" placeholder="e.g. B.Tech, MCA" value="<c:out value='${selectedExtDegree}'/>" style="height: 38px;" />
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" style="font-size: 0.78rem; font-weight: 700;">College / University</label>
                    <input type="text" name="extCollege" class="form-control" placeholder="e.g. NIT, KIIT" value="<c:out value='${selectedExtCollege}'/>" style="height: 38px;" />
                </div>
                <div style="display: flex; gap: 6px; margin: 0;">
                    <button type="submit" class="btn btn-primary" style="flex: 1; height: 38px; font-weight: 700; border-radius: var(--radius-sm); padding: 0 12px;">
                        <i class="fa-solid fa-filter"></i> Filter
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline" style="height: 38px; display: inline-flex; align-items: center; border-radius: var(--radius-sm); padding: 0 10px;" title="Reset filters">
                        Reset
                    </a>
                </div>
            </div>
        </form>
    </div>

    <!-- External Candidate Table -->
    <div class="portal-card" style="padding: 0; overflow: hidden; margin-bottom: 32px; border: 1px solid var(--border-light); border-radius: var(--radius-md); box-shadow: var(--shadow-sm); background: white;">
        <c:choose>
            <c:when test="${empty externalStudents}">
                <div style="text-align: center; padding: 40px 20px; background: white;">
                    <div style="font-size: 2rem; margin-bottom: 8px; color: var(--gold-500);">
                        <i class="fa-solid fa-user-graduate"></i>
                    </div>
                    <h4 style="margin: 0 0 6px 0; color: var(--navy-900); font-size: 1.05rem; font-weight: 700;">No External Candidate Records</h4>
                    <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 440px; margin: 0 auto; line-height: 1.5;">
                        No external student registration records match your filter criteria.
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table class="portal-table data-table" style="margin: 0; width: 100%; border-collapse: collapse; font-size: 0.9rem;">
                        <thead>
                            <tr>
                                <th style="padding: 12px 16px; text-align: left;">Candidate</th>
                                <th style="padding: 12px 16px; text-align: left;">Institution &amp; Location</th>
                                <th style="padding: 12px 16px; text-align: left;">Degree &amp; Department</th>
                                <th style="padding: 12px 16px; text-align: center;">CGPA &amp; Sem</th>
                                <th style="padding: 12px 16px; text-align: center;">Status</th>
                                <th style="padding: 12px 16px; text-align: center;">Management Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="ext" items="${externalStudents}">
                                <tr style="border-bottom: 1px solid var(--border-light);">
                                    <td style="padding: 12px 16px; text-align: left;">
                                        <div style="font-weight: 700; color: var(--navy-900);">
                                            <c:out value="${ext.fullName}"/>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted);">
                                            <c:out value="${ext.email}"/>
                                            <c:if test="${not empty ext.phone}"> &bull; <c:out value="${ext.phone}"/></c:if>
                                        </div>
                                    </td>
                                    <td style="padding: 12px 16px; text-align: left;">
                                        <div style="font-weight: 600; color: var(--navy-800);">
                                            <c:out value="${ext.collegeName}"/>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted);">
                                            <c:out value="${ext.collegeLocation}"/>
                                        </div>
                                    </td>
                                    <td style="padding: 12px 16px; text-align: left;">
                                        <div style="font-weight: 600; color: var(--text-dark);">
                                            <c:out value="${ext.degree}"/>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted);">
                                            <c:out value="${ext.department}"/>
                                            <c:if test="${not empty ext.graduationYear}"> &bull; Class of ${ext.graduationYear}</c:if>
                                        </div>
                                    </td>
                                    <td style="padding: 12px 16px; text-align: center;">
                                        <div style="font-weight: 800; color: var(--accent-teal);">
                                            <c:out value="${ext.cgpa}"/> / 10.0
                                        </div>
                                        <div style="font-size: 11px; color: var(--text-muted);">
                                            Sem ${ext.currentSemester}
                                        </div>
                                    </td>
                                    <td style="padding: 12px 16px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${ext.status == 'PENDING'}">
                                                <span class="status-badge badge-pending" style="font-size: 11px; padding: 4px 8px; border-radius: 12px;">
                                                    <i class="fa-solid fa-clock" style="margin-right: 3px;"></i> PENDING
                                                </span>
                                            </c:when>
                                            <c:when test="${ext.status == 'APPROVED'}">
                                                <span class="status-badge badge-selected" style="font-size: 11px; padding: 4px 8px; border-radius: 12px;">
                                                    <i class="fa-solid fa-circle-check" style="margin-right: 3px;"></i> APPROVED
                                                </span>
                                            </c:when>
                                            <c:when test="${ext.status == 'REJECTED'}">
                                                <span class="status-badge badge-rejected" style="font-size: 11px; padding: 4px 8px; border-radius: 12px;">
                                                    <i class="fa-solid fa-circle-xmark" style="margin-right: 3px;"></i> REJECTED
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge" style="font-size: 11px; padding: 4px 8px; border-radius: 12px;"><c:out value="${ext.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="padding: 12px 16px; text-align: center;">
                                        <div style="display: flex; gap: 6px; justify-content: center; align-items: center; flex-wrap: wrap;">
                                            <c:if test="${ext.status == 'PENDING'}">
                                                <form action="${pageContext.request.contextPath}/admin/external-students" method="post" style="margin: 0; display: inline;">
                                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                    <input type="hidden" name="action" value="approve">
                                                    <input type="hidden" name="id" value="${ext.id}">
                                                    <button type="submit" class="btn btn-sm" style="background: #16a34a; color: white; border: none; padding: 5px 10px; font-size: 12px; font-weight: 700; border-radius: 4px; display: inline-flex; align-items: center; gap: 4px;" title="Approve candidate registration">
                                                        <i class="fa-solid fa-check"></i> Approve
                                                    </button>
                                                </form>

                                                <form action="${pageContext.request.contextPath}/admin/external-students" method="post" style="margin: 0; display: inline;" onsubmit="return handleRejectExternal(this, '<c:out value="${ext.fullName}"/>');">
                                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                    <input type="hidden" name="action" value="reject">
                                                    <input type="hidden" name="id" value="${ext.id}">
                                                    <input type="hidden" name="adminNotes" value="">
                                                    <button type="submit" class="btn btn-sm" style="background: #dc2626; color: white; border: none; padding: 5px 10px; font-size: 12px; font-weight: 700; border-radius: 4px; display: inline-flex; align-items: center; gap: 4px;" title="Reject candidate registration">
                                                        <i class="fa-solid fa-xmark"></i> Reject
                                                    </button>
                                                </form>
                                            </c:if>

                                            <a href="${pageContext.request.contextPath}/admin/external-students?action=details&id=${ext.id}" class="btn btn-outline btn-sm" style="padding: 5px 10px; font-size: 12px; display: inline-flex; align-items: center; gap: 4px;" title="View candidate dossier &amp; audit history">
                                                <i class="fa-solid fa-eye"></i> Details
                                            </a>

                                            <c:if test="${not empty ext.resumeUrl}">
                                                <a href="${pageContext.request.contextPath}/resume/download?externalStudentId=${ext.id}" target="_blank" class="btn btn-outline btn-sm" style="padding: 5px 10px; font-size: 12px; display: inline-flex; align-items: center; gap: 4px; border-color: #cbd5e1;" title="View attached resume document">
                                                    <i class="fa-solid fa-file-pdf" style="color: #ef4444;"></i> Resume
                                                </a>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Quick Navigation Bar -->
    <div class="portal-card" style="background: linear-gradient(135deg, var(--navy-900) 0%, var(--navy-800) 100%); color: #ffffff; padding: 24px;">
        <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 16px;">
            <div>
                <h4 style="color: #ffffff; margin: 0 0 6px 0; font-size: 16px;">
                    <i class="fas fa-download" style="color: var(--gold-400); margin-right: 8px;"></i>Placement Records &amp; Verification
                </h4>
                <p style="color: rgba(255, 255, 255, 0.75); font-size: 13px; margin: 0;">
                    Official placement offer validation reports and multi-round interview dossiers are available in PDF format.
                </p>
            </div>
            <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/applications" class="btn btn-outline" style="border-color: rgba(255, 255, 255, 0.3); color: #ffffff; background: rgba(255, 255, 255, 0.08);">
                    <i class="fas fa-file-pdf" style="color: var(--gold-400);"></i> Applicant Dossiers
                </a>
                <a href="${pageContext.request.contextPath}/interviews" class="btn btn-accent">
                    <i class="fas fa-award"></i> Verification Reports
                </a>
            </div>
        </div>
    </div>

    <!-- Coordinator Details Modal -->
    <div id="coordDetailsModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.6); z-index: 2000; align-items: center; justify-content: center; padding: 16px;">
        <div style="background: #ffffff; border-radius: var(--radius-lg); max-width: 540px; width: 100%; box-shadow: var(--shadow-lg); overflow: hidden; border: 1px solid var(--border-light);">
            <div style="background: var(--navy-900); color: #ffffff; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid var(--accent-gold);">
                <h4 style="margin: 0; font-size: 1.1rem; color: #ffffff; display: flex; align-items: center; gap: 8px;">
                    <i class="fas fa-id-badge" style="color: var(--accent-gold);"></i> Coordinator Request Dossier
                </h4>
                <button type="button" onclick="closeCoordinatorModal()" style="background: transparent; border: none; color: #ffffff; font-size: 1.2rem; cursor: pointer;">&times;</button>
            </div>
            <div style="padding: 20px 24px; font-size: 0.9rem;">
                <div style="display: grid; grid-template-columns: 140px 1fr; gap: 10px; line-height: 1.6;">
                    <strong style="color: var(--navy-900);">Full Name:</strong> <span id="modalCoordName"></span>
                    <strong style="color: var(--navy-900);">Email Address:</strong> <span id="modalCoordEmail"></span>
                    <strong style="color: var(--navy-900);">Employee ID:</strong> <span id="modalCoordEmpId" style="font-family: monospace; font-weight: 700;"></span>
                    <strong style="color: var(--navy-900);">Department:</strong> <span id="modalCoordDept"></span>
                    <strong style="color: var(--navy-900);">Designation:</strong> <span id="modalCoordDesig"></span>
                    <strong style="color: var(--navy-900);">Phone Number:</strong> <span id="modalCoordPhone"></span>
                    <strong style="color: var(--navy-900);">Current Status:</strong> <span id="modalCoordStatus"></span>
                    <strong style="color: var(--navy-900);">Submitted At:</strong> <span id="modalCoordDate"></span>
                    <strong style="color: var(--navy-900);">Audited By:</strong> <span id="modalCoordAuditor"></span>
                    <strong style="color: var(--navy-900);">Audit Date:</strong> <span id="modalCoordAuditDate"></span>
                    <strong style="color: var(--navy-900);">Admin Remarks:</strong> <span id="modalCoordNotes"></span>
                </div>
            </div>
            <div style="padding: 12px 20px; background: var(--bg-subtle); display: flex; justify-content: flex-end; border-top: 1px solid var(--border-light);">
                <button type="button" class="btn btn-outline btn-sm" onclick="closeCoordinatorModal()">Close</button>
            </div>
        </div>
    </div>

    <script>
        function viewCoordinatorDetails(id, name, email, empId, dept, desig, phone, status, date, auditor, auditDate, notes) {
            document.getElementById('modalCoordName').textContent = name || '-';
            document.getElementById('modalCoordEmail').textContent = email || '-';
            document.getElementById('modalCoordEmpId').textContent = empId || '-';
            document.getElementById('modalCoordDept').textContent = dept || '-';
            document.getElementById('modalCoordDesig').textContent = desig || '-';
            document.getElementById('modalCoordPhone').textContent = phone || '-';
            document.getElementById('modalCoordStatus').textContent = status || '-';
            document.getElementById('modalCoordDate').textContent = date || '-';
            document.getElementById('modalCoordAuditor').textContent = auditor || 'None (Pending)';
            document.getElementById('modalCoordAuditDate').textContent = auditDate || '-';
            document.getElementById('modalCoordNotes').textContent = notes || 'No notes recorded';

            var modal = document.getElementById('coordDetailsModal');
            modal.style.display = 'flex';
        }

        function closeCoordinatorModal() {
            var modal = document.getElementById('coordDetailsModal');
            modal.style.display = 'none';
        }

        function handleRejectCoordinator(form, coordinatorName) {
            var confirmed = confirm('Are you sure you want to reject the coordinator registration request for ' + coordinatorName + '?');
            if (!confirmed) {
                return false;
            }
            var reason = prompt('Please enter the reason for rejection (Optional):', '');
            if (reason === null) {
                // User clicked cancel on reason prompt
                return false;
            }
            form.querySelector('input[name="adminNotes"]').value = reason;
            return true;
        }

        function handleRejectExternal(form, candidateName) {
            var confirmed = confirm('Are you sure you want to reject the external student registration request for ' + candidateName + '?');
            if (!confirmed) {
                return false;
            }
            var reason = prompt('Please enter the reason for rejection (Optional):', '');
            if (reason === null) {
                return false;
            }
            form.querySelector('input[name="adminNotes"]').value = reason;
            return true;
        }

        window.onclick = function(event) {
            var modal = document.getElementById('coordDetailsModal');
            if (event.target === modal) {
                closeCoordinatorModal();
            }
        };
    </script>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
