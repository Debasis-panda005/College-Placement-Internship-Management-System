<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="interviews" scope="request" />
<c:set var="pageTitle" value="Interview Management &amp; Scheduling | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 1240px; margin: 30px auto 60px auto;">

    <!-- Page Header Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 48px; height: 48px; background: rgba(27, 54, 93, 0.08); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 22px;">
                <i class="fa-solid fa-calendar-check"></i>
            </div>
            <div>
                <span style="font-size: 11px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em; display: block; margin-bottom: 2px;">
                    Technical &amp; Corporate Evaluation
                </span>
                <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                    Interview Management &amp; Scheduling
                </h1>
                <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                    Track interview timelines, coordinate multi-stage technical assessments, and record evaluation reports.
                </p>
            </div>
        </div>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error" style="margin-bottom: 20px;" role="alert">
            <i class="fa-solid fa-circle-exclamation alert-icon"></i>
            <div class="alert-content">
                <strong>Notice:</strong> <c:out value="${errorMessage}"/>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success" style="margin-bottom: 20px;" role="alert">
            <i class="fa-solid fa-circle-check alert-icon"></i>
            <div class="alert-content">
                <strong>Success:</strong> <c:out value="${successMessage}"/>
            </div>
        </div>
    </c:if>

    <!-- STATS CARDS -->
    <div class="kpi-grid" style="margin-bottom: 24px;">
        <a href="${pageContext.request.contextPath}/interviews" class="kpi-card kpi-blue clickable-card" title="View all interviews">
            <div class="kpi-value">${not empty interviewStats ? interviewStats['TOTAL'] : 0}</div>
            <div class="kpi-label"><i class="fa-solid fa-calendar-days" style="margin-right: 4px;"></i> Total Interviews</div>
        </a>

        <a href="${pageContext.request.contextPath}/interviews?filterStatus=SCHEDULED" class="kpi-card clickable-card" title="Filter SCHEDULED sessions">
            <div class="kpi-value" style="color: var(--secondary-blue);">${not empty interviewStats ? interviewStats['SCHEDULED'] : 0}</div>
            <div class="kpi-label"><i class="fa-regular fa-clock" style="margin-right: 4px;"></i> Scheduled</div>
        </a>

        <a href="${pageContext.request.contextPath}/interviews?filterStatus=COMPLETED" class="kpi-card kpi-green clickable-card" title="Filter COMPLETED sessions">
            <div class="kpi-value" style="color: var(--success);">${not empty interviewStats ? interviewStats['COMPLETED'] : 0}</div>
            <div class="kpi-label"><i class="fa-solid fa-circle-check" style="margin-right: 4px;"></i> Completed</div>
        </a>

        <a href="${pageContext.request.contextPath}/interviews?filterStatus=CANCELLED" class="kpi-card clickable-card" title="Filter CANCELLED sessions">
            <div class="kpi-value" style="color: var(--danger);">${not empty interviewStats ? interviewStats['CANCELLED'] : 0}</div>
            <div class="kpi-label"><i class="fa-solid fa-ban" style="margin-right: 4px;"></i> Cancelled</div>
        </a>

        <a href="${pageContext.request.contextPath}/interviews" class="kpi-card kpi-gold clickable-card" title="Rescheduled sessions">
            <div class="kpi-value" style="color: var(--accent-gold);">${not empty interviewStats ? interviewStats['RESCHEDULED'] : 0}</div>
            <div class="kpi-label"><i class="fa-solid fa-arrows-rotate" style="margin-right: 4px;"></i> Rescheduled</div>
        </a>
    </div>

    <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
        <!-- SCHEDULE NEW INTERVIEW (Staff Only) -->
        <div class="card" style="margin-bottom: 24px; border-left: 4px solid var(--primary-navy); padding: 24px 28px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); background: white;">
            <h2 style="font-size: 1.05rem; margin: 0 0 16px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px;">
                <i class="fa-solid fa-calendar-plus" style="color: var(--primary-navy);"></i> Schedule Primary Interview Session
            </h2>
            <form action="${pageContext.request.contextPath}/interviews" method="post" id="scheduleInterviewForm">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

                <div style="display: flex; gap: 14px; flex-wrap: wrap; align-items: flex-end;">
                    <div class="form-group" style="flex: 1; min-width: 170px; margin: 0;">
                        <label class="form-label" for="applicationId" style="font-size: 0.8rem; font-weight: 700;">App Ref ID <span style="color:var(--danger);">*</span></label>
                        <input type="number" id="applicationId" name="applicationId" class="form-control"
                               placeholder="e.g. 101" value="${selectedApplicationId}" required min="1" style="height: 42px;">
                    </div>

                    <div class="form-group" style="flex: 1; min-width: 170px; margin: 0;">
                        <label class="form-label" for="interviewDate" style="font-size: 0.8rem; font-weight: 700;">Interview Date <span style="color:var(--danger);">*</span></label>
                        <input type="date" id="interviewDate" name="interviewDate" class="form-control"
                               min="${today}" required style="height: 42px;">
                    </div>

                    <div class="form-group" style="flex: 1; min-width: 150px; margin: 0;">
                        <label class="form-label" for="interviewTime" style="font-size: 0.8rem; font-weight: 700;">Time <span style="color:var(--danger);">*</span></label>
                        <input type="time" id="interviewTime" name="interviewTime" class="form-control" required style="height: 42px;">
                    </div>

                    <div class="form-group" style="flex: 1; min-width: 160px; margin: 0;">
                        <label class="form-label" for="mode" style="font-size: 0.8rem; font-weight: 700;">Evaluation Mode <span style="color:var(--danger);">*</span></label>
                        <select id="mode" name="mode" class="form-control" required style="height: 42px;">
                            <option value="">Select Mode</option>
                            <option value="ONLINE">Online (Virtual)</option>
                            <option value="OFFLINE">Offline (Campus)</option>
                        </select>
                    </div>

                    <div style="margin: 0;">
                        <button type="submit" class="btn btn-primary" style="height: 42px; padding: 0 22px; font-weight: 700; border-radius: var(--radius-sm);">
                            <i class="fa-solid fa-calendar-check" style="margin-right: 6px;"></i> Schedule Session
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </c:if>

    <!-- FILTER & SEARCH INTERVIEWS -->
    <div class="card" style="margin-bottom: 24px; background: white; border: 1px solid var(--border-light); padding: 20px 24px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm);">
        <h3 style="font-size: 1rem; margin: 0 0 14px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px;">
            <i class="fa-solid fa-filter" style="color: var(--accent-gold);"></i> Filter Interview Sessions
        </h3>
        <form action="${pageContext.request.contextPath}/interviews" method="get" style="display: flex; gap: 14px; flex-wrap: wrap; align-items: flex-end;">
            <div class="form-group" style="flex: 1; min-width: 150px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Application ID</label>
                <input type="number" name="filterAppId" class="form-control" placeholder="Application ID"
                       value="${selectedFilterAppId}" style="height: 40px;">
            </div>

            <div class="form-group" style="flex: 1; min-width: 160px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Filter by Date</label>
                <input type="date" name="filterDate" class="form-control" value="${selectedFilterDate}" style="height: 40px;">
            </div>

            <div class="form-group" style="flex: 1; min-width: 140px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Mode</label>
                <select name="filterMode" class="form-control" style="height: 40px;">
                    <option value="ALL" ${selectedFilterMode == 'ALL' ? 'selected' : ''}>All Modes</option>
                    <option value="ONLINE" ${selectedFilterMode == 'ONLINE' ? 'selected' : ''}>Online</option>
                    <option value="OFFLINE" ${selectedFilterMode == 'OFFLINE' ? 'selected' : ''}>Offline</option>
                </select>
            </div>

            <div class="form-group" style="flex: 1; min-width: 150px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Status</label>
                <select name="filterStatus" class="form-control" style="height: 40px;">
                    <option value="ALL" ${selectedFilterStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                    <option value="SCHEDULED" ${selectedFilterStatus == 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                    <option value="COMPLETED" ${selectedFilterStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    <option value="CANCELLED" ${selectedFilterStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                </select>
            </div>

            <div style="display: flex; gap: 8px; margin: 0;">
                <button type="submit" class="btn btn-primary" style="height: 40px; padding: 0 18px; font-weight: 600; border-radius: var(--radius-sm);">
                    Filter
                </button>
                <a href="${pageContext.request.contextPath}/interviews" class="btn btn-outline" style="height: 40px; padding: 0 14px; display: inline-flex; align-items: center; border-radius: var(--radius-sm);">
                    Reset
                </a>
            </div>
        </form>
    </div>

    <!-- INTERVIEW LISTING TABLE -->
    <div class="card" style="padding: 0; overflow: hidden; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);">
        <div style="padding: 16px 22px; background: white; border-bottom: 1px solid var(--border-light); display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 1.05rem; color: var(--primary-navy); font-weight: 700;">
                Scheduled Interview Sessions
            </div>
            <span style="font-size: 0.82rem; background: var(--bg-subtle); color: var(--text-muted); padding: 4px 10px; border-radius: 12px; font-weight: 600;">
                Showing ${not empty interviews ? interviews.size() : 0} session(s)
            </span>
        </div>

        <div style="overflow-x: auto;">
            <table class="data-table" style="margin: 0; width: 100%; border-collapse: collapse;">
                <thead>
                    <tr>
                        <th style="padding: 14px 16px; text-align: left;">Candidate Profile</th>
                        <th style="padding: 14px 16px; text-align: left;">Opportunity &amp; Company</th>
                        <th style="padding: 14px 16px; text-align: center;">Date &amp; Time</th>
                        <th style="padding: 14px 16px; text-align: center;">Mode</th>
                        <th style="padding: 14px 16px; text-align: center;">Status</th>
                        <th style="padding: 14px 16px; text-align: center;">Dynamic Rounds</th>
                        <th style="padding: 14px 16px; text-align: center;">Actions &amp; Reports</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty interviews}">
                            <c:forEach var="interview" items="${interviews}">
                                <c:set var="app" value="${applicationMap[interview.applicationId]}" />
                                <c:set var="stud" value="${not empty app ? studentMap[app.studentId] : null}" />
                                <c:set var="user" value="${not empty stud ? userMap[stud.userId] : null}" />
                                <c:set var="job" value="${not empty app ? jobMap[app.jobId] : null}" />
                                <c:set var="report" value="${reportMap[interview.applicationId]}" />

                                <tr style="border-bottom: 1px solid var(--border-light);">
                                    <!-- Candidate Info -->
                                    <td style="padding: 14px 16px; text-align: left;">
                                        <div style="font-weight: 700; color: var(--text-dark);">
                                            <c:out value="${not empty user ? user.name : (not empty stud ? stud.rollNumber : 'Student')}"/>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                            <c:if test="${not empty stud}">
                                                Roll: <c:out value="${stud.rollNumber}"/> &bull; <c:out value="${stud.department}"/>
                                            </c:if>
                                            <span style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-top: 1px;">App Ref: #${interview.applicationId}</span>
                                        </div>
                                    </td>

                                    <!-- Opportunity Info -->
                                    <td style="padding: 14px 16px; text-align: left;">
                                        <div style="font-weight: 700; color: var(--primary-navy);">
                                            <c:out value="${not empty job ? job.title : 'Job Role'}"/>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                            <c:out value="${not empty job ? job.company : '-'}"/>
                                            <c:if test="${not empty job && not empty job.jobType}">
                                                &bull; <span class="badge" style="background: rgba(27, 54, 93, 0.08); color: var(--primary-navy); font-size: 0.72rem; padding: 2px 6px;">${job.jobType}</span>
                                            </c:if>
                                        </div>
                                    </td>

                                    <!-- Date & Time -->
                                    <td style="padding: 14px 16px; text-align: center; white-space: nowrap;">
                                        <div style="font-weight: 700; color: var(--text-dark);">
                                            <c:out value="${interview.interviewDate}"/>
                                        </div>
                                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">
                                            <i class="fa-regular fa-clock" style="margin-right: 3px;"></i><c:out value="${interview.interviewTime}"/>
                                        </div>
                                    </td>

                                    <!-- Mode -->
                                    <td style="padding: 14px 16px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${interview.mode == 'ONLINE'}">
                                                <span class="badge" style="background: #e0f2fe; color: #0284c7; font-weight: 700; font-size: 0.75rem;">
                                                    <i class="fa-solid fa-video" style="margin-right: 4px;"></i>ONLINE
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: #fef3c7; color: #b45309; font-weight: 700; font-size: 0.75rem;">
                                                    <i class="fa-solid fa-building-user" style="margin-right: 4px;"></i>OFFLINE
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- Status -->
                                    <td style="padding: 14px 16px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${interview.status == 'COMPLETED'}">
                                                <span class="status-badge badge-completed">COMPLETED</span>
                                            </c:when>
                                            <c:when test="${interview.status == 'CANCELLED'}">
                                                <span class="status-badge badge-cancelled">CANCELLED</span>
                                            </c:when>
                                            <c:when test="${interview.status == 'SCHEDULED' && not empty rescheduledMap[interview.applicationId] && rescheduledMap[interview.applicationId]}">
                                                <span class="status-badge badge-rescheduled">RESCHEDULED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge badge-scheduled">${interview.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- Dynamic Rounds Feature -->
                                    <td style="padding: 14px 16px; text-align: center;">
                                        <a href="${pageContext.request.contextPath}/interview-rounds?applicationId=${interview.applicationId}"
                                           class="btn btn-primary btn-sm" style="font-size: 0.78rem; padding: 6px 12px; border-radius: var(--radius-sm); white-space: nowrap;">
                                            <i class="fa-solid fa-bullseye" style="margin-right: 4px;"></i> Manage Rounds
                                        </a>
                                    </td>

                                    <!-- Actions & PDF Reports -->
                                    <td style="padding: 14px 16px; text-align: center;">
                                        <div style="display: flex; gap: 6px; justify-content: center; align-items: center; flex-wrap: wrap;">
                                            <!-- PDF Report Link if Available -->
                                            <c:if test="${not empty report}">
                                                <a href="${pageContext.request.contextPath}/interview-report?action=download&applicationId=${interview.applicationId}"
                                                   class="btn btn-outline btn-sm" style="font-size: 0.78rem; padding: 5px 10px; color: var(--success); border-radius: var(--radius-sm);"
                                                   title="Download Official PDF Report">
                                                    <i class="fa-solid fa-file-pdf" style="margin-right: 3px;"></i> PDF
                                                </a>
                                            </c:if>

                                            <c:choose>
                                                <c:when test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                                                    <c:if test="${interview.status == 'SCHEDULED'}">
                                                        <a href="${pageContext.request.contextPath}/interviews?action=edit&id=${interview.id}"
                                                           class="btn btn-outline btn-sm" style="font-size: 0.78rem; padding: 5px 10px; border-radius: var(--radius-sm);" title="Reschedule session">
                                                            <i class="fa-solid fa-pen-to-square"></i>
                                                        </a>

                                                        <form action="${pageContext.request.contextPath}/interviews" method="post" style="display: inline; margin: 0;">
                                                            <input type="hidden" name="action" value="complete">
                                                            <input type="hidden" name="id" value="${interview.id}">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <button type="submit" class="btn btn-success btn-sm" style="font-size: 0.78rem; padding: 5px 10px; border-radius: var(--radius-sm);">
                                                                <i class="fa-solid fa-check" style="margin-right: 3px;"></i> Complete
                                                            </button>
                                                        </form>

                                                        <form action="${pageContext.request.contextPath}/interviews" method="post" style="display: inline; margin: 0;"
                                                              onsubmit="return (typeof PortalPopups !== 'undefined') ? PortalPopups.confirmAction(event, 'Are you sure you want to cancel this interview session?') : confirm('Are you sure you want to cancel this interview session?');">
                                                            <input type="hidden" name="action" value="cancel">
                                                            <input type="hidden" name="id" value="${interview.id}">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <button type="submit" class="btn btn-danger btn-sm" style="font-size: 0.78rem; padding: 5px 10px; border-radius: var(--radius-sm);">
                                                                Cancel
                                                            </button>
                                                        </form>
                                                    </c:if>

                                                    <c:if test="${interview.status == 'COMPLETED'}">
                                                        <form action="${pageContext.request.contextPath}/applications" method="post" style="display: inline; margin: 0;">
                                                            <input type="hidden" name="action" value="select">
                                                            <input type="hidden" name="id" value="${interview.applicationId}">
                                                            <input type="hidden" name="from" value="interviews">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <button type="submit" class="btn btn-success btn-sm" style="font-size: 0.78rem; padding: 5px 10px; border-radius: var(--radius-sm);">
                                                                <i class="fa-solid fa-award" style="margin-right: 3px;"></i> Select
                                                            </button>
                                                        </form>

                                                        <form action="${pageContext.request.contextPath}/applications" method="post" style="display: inline; margin: 0;"
                                                              onsubmit="return (typeof PortalPopups !== 'undefined') ? PortalPopups.confirmAction(event, 'Are you sure you want to reject this candidate?') : confirm('Are you sure you want to reject this candidate?');">
                                                            <input type="hidden" name="action" value="reject">
                                                            <input type="hidden" name="id" value="${interview.applicationId}">
                                                            <input type="hidden" name="from" value="interviews">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <button type="submit" class="btn btn-danger btn-sm" style="font-size: 0.78rem; padding: 5px 10px; border-radius: var(--radius-sm);">
                                                                <i class="fa-solid fa-xmark" style="margin-right: 3px;"></i> Reject
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/applications?action=details&id=${interview.applicationId}"
                                                       class="btn btn-outline btn-sm" style="font-size: 0.78rem; padding: 5px 10px; border-radius: var(--radius-sm);">
                                                        <i class="fa-solid fa-eye" style="margin-right: 3px;"></i> Details
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 48px 20px; color: var(--text-muted);">
                                    <div style="font-size: 2.2rem; margin-bottom: 10px; color: var(--accent-gold);">
                                        <i class="fa-solid fa-calendar-xmark"></i>
                                    </div>
                                    <div style="font-weight: 700; font-size: 1.15rem; color: var(--primary-navy);">No Interview Records Found</div>
                                    <div style="font-size: 0.9rem; margin-top: 4px;">There are no interview sessions matching your filter criteria.</div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />