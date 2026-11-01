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

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
