<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="shortlist" scope="request" />
<c:set var="pageTitle" value="Shortlisted Candidates Pool | Centurion University Placement Portal" scope="request" />

<!-- Common Navigation Header -->
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<div class="portal-container" style="max-width: 1240px; margin: 30px auto 60px auto;">

    <!-- Title Section Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 48px; height: 48px; background: rgba(230, 154, 24, 0.12); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: var(--accent-gold); font-size: 22px;">
                <i class="fa-solid fa-star"></i>
            </div>
            <div>
                <span style="font-size: 11px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em; display: block; margin-bottom: 2px;">
                    Interview Stage Evaluation
                </span>
                <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                    Shortlisted Candidates Pool
                </h1>
                <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                    Candidates approved for corporate interviews. Coordinate multi-round evaluations and record final selection decisions.
                </p>
            </div>
        </div>

        <div>
            <a href="${pageContext.request.contextPath}/applications" class="btn btn-outline" style="font-size: 0.85rem; padding: 8px 16px; border-radius: var(--radius-sm);">
                <i class="fa-solid fa-list" style="margin-right: 4px;"></i> View All Applications
            </a>
        </div>
    </div>

    <!-- Flash alerts -->
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

    <div class="card" style="padding: 0; overflow: hidden; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);">
        <div style="padding: 16px 22px; background: white; border-bottom: 1px solid var(--border-light); display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 1.05rem; color: var(--primary-navy); font-weight: 700;">
                Approved Candidates for Interview Evaluation
            </div>
            <span style="font-size: 0.82rem; background: var(--bg-subtle); color: var(--text-muted); padding: 4px 10px; border-radius: 12px; font-weight: 600;">
                <c:out value="${empty applications ? 0 : applications.size()}"/> candidate(s) shortlisted
            </span>
        </div>

        <c:choose>
            <c:when test="${empty applications}">
                <div style="text-align: center; padding: 48px 20px; background: white;">
                    <div style="font-size: 2.2rem; margin-bottom: 10px; color: var(--accent-gold);">
                        <i class="fa-solid fa-clipboard-user"></i>
                    </div>
                    <h4 style="margin: 0 0 6px 0; color: var(--primary-navy); font-size: 1.15rem; font-weight: 700;">No Shortlisted Candidates Found</h4>
                    <p style="font-size: 0.9rem; color: var(--text-muted); max-width: 480px; margin: 0 auto 18px auto; line-height: 1.5;">
                        There are currently no candidates in the shortlisted pool. Review applied student submissions in the applications portal to shortlist candidates for interview drives.
                    </p>
                    <a href="${pageContext.request.contextPath}/applications" class="btn btn-primary" style="font-size: 0.85rem; padding: 8px 18px; border-radius: var(--radius-sm);">
                        View All Applications
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table class="portal-table data-table" style="margin: 0; width: 100%; border-collapse: collapse;">
                        <thead>
                            <tr>
                                <th style="padding: 14px 16px; text-align: left;">App #</th>
                                <th style="padding: 14px 16px; text-align: left;">Candidate Profile</th>
                                <th style="padding: 14px 16px; text-align: left;">Opportunity &amp; Company</th>
                                <th style="padding: 14px 16px; text-align: center;">Applied Date</th>
                                <th style="padding: 14px 16px; text-align: center;">Interview Status</th>
                                <th style="padding: 14px 16px; text-align: center;">Schedule</th>
                                <th style="padding: 14px 16px; text-align: center;">Mode</th>
                                <th style="padding: 14px 16px; text-align: center;">Actions &amp; Evaluations</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="application" items="${applications}">
                                <c:set var="interview" value="${interviewMap[application.id]}" />
                                <c:set var="stud" value="${studentMap[application.studentId]}" />
                                <c:set var="usr" value="${userMap[stud.userId]}" />
                                <c:set var="jb" value="${jobMap[application.jobId]}" />
                                <c:set var="isRescheduled" value="${not empty rescheduledMap[application.id] && rescheduledMap[application.id]}" />

                                <tr style="border-bottom: 1px solid var(--border-light);">
                                    <td style="padding: 14px 16px; text-align: left;">
                                        <a href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}" style="font-weight: 700; color: var(--primary-navy); text-decoration: none;">
                                            #${application.id}
                                        </a>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: left;">
                                        <div style="font-weight: 700; color: var(--text-dark);">
                                            <c:choose>
                                                <c:when test="${not empty usr.name}"><c:out value="${usr.name}"/></c:when>
                                                <c:otherwise>Candidate #${application.studentId}</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                            <c:if test="${not empty stud.rollNumber}">Roll: <c:out value="${stud.rollNumber}"/> &bull; </c:if>
                                            <c:if test="${not empty stud.department}"><c:out value="${stud.department}"/> &bull; </c:if>
                                            <c:if test="${not empty stud.cgpa}">CGPA: <strong><c:out value="${stud.cgpa}"/></strong></c:if>
                                        </div>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: left;">
                                        <div style="font-weight: 700; color: var(--primary-navy);">
                                            <c:choose>
                                                <c:when test="${not empty jb.title}"><c:out value="${jb.title}"/></c:when>
                                                <c:otherwise>Opportunity #${application.jobId}</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                            <c:if test="${not empty jb.company}"><c:out value="${jb.company}"/> &bull; </c:if>
                                            <c:if test="${not empty jb.jobType}"><c:out value="${jb.jobType}"/></c:if>
                                        </div>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center; color: var(--text-muted); font-size: 0.88rem;">${application.applicationDate}</td>

                                    <td style="padding: 14px 16px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${empty interview}">
                                                <span class="status-badge badge-applied">NOT SCHEDULED</span>
                                            </c:when>
                                            <c:when test="${interview.status == 'COMPLETED'}">
                                                <span class="status-badge badge-completed">COMPLETED</span>
                                            </c:when>
                                            <c:when test="${interview.status == 'CANCELLED'}">
                                                <span class="status-badge badge-cancelled">CANCELLED</span>
                                            </c:when>
                                            <c:when test="${interview.status == 'SCHEDULED' && isRescheduled}">
                                                <span class="status-badge badge-rescheduled">RESCHEDULED</span>
                                            </c:when>
                                            <c:when test="${interview.status == 'SCHEDULED'}">
                                                <span class="status-badge badge-scheduled">SCHEDULED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge"><c:out value="${interview.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center; font-size: 0.88rem;">
                                        <c:choose>
                                            <c:when test="${not empty interview && not empty interview.interviewDate}">
                                                <strong style="color: var(--text-dark);">${interview.interviewDate}</strong>
                                                <c:if test="${not empty interview.interviewTime}">
                                                    <div style="color: var(--text-muted); font-size: 0.78rem;">${interview.interviewTime}</div>
                                                </c:if>
                                            </c:when>
                                            <c:otherwise><span style="color: var(--text-muted);">-</span></c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center; font-size: 0.88rem;">
                                        <c:choose>
                                            <c:when test="${not empty interview && not empty interview.mode}">
                                                <span class="badge" style="background: rgba(27, 54, 93, 0.08); color: var(--primary-navy); font-size: 0.75rem;">${interview.mode}</span>
                                            </c:when>
                                            <c:otherwise><span style="color: var(--text-muted);">-</span></c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center;">
                                        <div style="display: flex; gap: 6px; flex-wrap: wrap; align-items: center; justify-content: center;">
                                            <!-- Dynamic Multi-Round Interview Manager -->
                                            <a href="${pageContext.request.contextPath}/interview-rounds?applicationId=${application.id}" class="btn btn-primary btn-sm" title="Manage Multi-Round Evaluations" style="font-size: 0.78rem; padding: 5px 10px;">
                                                <i class="fa-solid fa-bullseye" style="margin-right: 3px;"></i> Rounds
                                            </a>

                                            <a href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}" class="btn btn-outline btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                <i class="fa-solid fa-eye" style="margin-right: 3px;"></i> Details
                                            </a>

                                            <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                                                <c:choose>
                                                    <c:when test="${empty interview}">
                                                        <a href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}" class="btn btn-accent btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                            <i class="fa-solid fa-calendar-plus" style="margin-right: 3px;"></i> Schedule
                                                        </a>
                                                    </c:when>
                                                    <c:when test="${interview.status == 'SCHEDULED'}">
                                                        <form action="${pageContext.request.contextPath}/interviews" method="post" style="display:inline; margin:0;">
                                                            <input type="hidden" name="action" value="complete">
                                                            <input type="hidden" name="id" value="${interview.id}">
                                                            <input type="hidden" name="from" value="shortlist">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                            <button type="submit" class="btn btn-success btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                                <i class="fa-solid fa-check" style="margin-right: 3px;"></i> Complete
                                                            </button>
                                                        </form>

                                                        <a href="${pageContext.request.contextPath}/interviews?action=edit&id=${interview.id}" class="btn btn-outline btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                            Reschedule
                                                        </a>

                                                        <form action="${pageContext.request.contextPath}/interviews" method="post" style="display:inline; margin:0;" data-action-type="cancelRound">
                                                            <input type="hidden" name="action" value="cancel">
                                                            <input type="hidden" name="id" value="${interview.id}">
                                                            <input type="hidden" name="from" value="shortlist">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                            <button type="submit" class="btn btn-danger btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                                Cancel
                                                            </button>
                                                        </form>
                                                    </c:when>

                                                    <c:when test="${interview.status == 'COMPLETED'}">
                                                        <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;" data-action-type="select">
                                                            <input type="hidden" name="action" value="select">
                                                            <input type="hidden" name="id" value="${application.id}">
                                                            <input type="hidden" name="from" value="shortlist">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                            <button type="submit" class="btn btn-success btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                                <i class="fa-solid fa-award" style="margin-right: 3px;"></i> Select Candidate
                                                            </button>
                                                        </form>

                                                        <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;" data-action-type="reject">
                                                            <input type="hidden" name="action" value="reject">
                                                            <input type="hidden" name="id" value="${application.id}">
                                                            <input type="hidden" name="from" value="shortlist">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                            <button type="submit" class="btn btn-danger btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                                <i class="fa-solid fa-xmark" style="margin-right: 3px;"></i> Reject
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                </c:choose>
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

</div>

<!-- Common Footer -->
<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />