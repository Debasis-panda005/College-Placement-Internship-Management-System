<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="applications" scope="request" />
<c:set var="pageTitle" value="Application Status History #${application.id} | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 960px; margin: 36px auto 60px auto; padding: 0 16px;">

    <!-- Page Header Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 52px; height: 52px; background: rgba(27, 54, 93, 0.08); border-radius: 12px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 24px;">
                <i class="fa-solid fa-timeline"></i>
            </div>
            <div>
                <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                    Status History &amp; Milestone Audit
                </h1>
                <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                    Chronological lifecycle event audit and multi-round interview progression for Application #${application.id}.
                </p>
            </div>
        </div>
        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}" class="btn btn-outline" style="font-size: 0.85rem; padding: 8px 16px; border-radius: var(--radius-sm);">
                &larr; View Details Dossier
            </a>
            <c:if test="${not empty interviewReport || not empty interviewRounds}">
                <a href="${pageContext.request.contextPath}/interview-report/pdf?applicationId=${application.id}" class="btn btn-secondary" target="_blank" style="font-size: 0.85rem; padding: 8px 16px; border-radius: var(--radius-sm); display: flex; align-items: center; gap: 6px;">
                    <i class="fa-solid fa-file-pdf"></i> Download PDF
                </a>
            </c:if>
        </div>
    </div>

    <!-- SUMMARY CARD (Preserved as requested) -->
    <div class="card" style="margin-bottom: 24px; border-left: 4px solid var(--primary-navy); padding: 22px 28px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); background: white;">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 18px;">
            <div>
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Candidate Profile</span>
                <span style="font-size: 1.05rem; font-weight: 700; color: var(--text-dark);">
                    <c:out value="${not empty studentUser ? studentUser.name : (not empty student ? student.rollNumber : 'Candidate')}"/>
                </span>
                <span style="font-size: 0.82rem; color: var(--text-muted); display: block; margin-top: 2px;">
                    Roll: <c:out value="${not empty student ? student.rollNumber : '-'}"/>
                </span>
            </div>
            <div>
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Opportunity Role</span>
                <span style="font-size: 1.05rem; font-weight: 700; color: var(--primary-navy);">
                    <c:out value="${not empty job ? job.title : 'Opportunity'}"/>
                </span>
                <span style="font-size: 0.82rem; color: var(--text-muted); display: block; margin-top: 2px;">
                    <c:out value="${not empty job ? job.company : '-'}"/>
                </span>
            </div>
            <div>
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Current Lifecycle Status</span>
                <div style="margin-top: 4px;">
                    <c:choose>
                        <c:when test="${application.status == 'APPLIED'}"><span class="status-badge badge-applied">APPLIED</span></c:when>
                        <c:when test="${application.status == 'SHORTLISTED'}"><span class="status-badge badge-shortlisted">SHORTLISTED</span></c:when>
                        <c:when test="${application.status == 'INTERVIEW_SCHEDULED'}"><span class="status-badge badge-interview">INTERVIEW SCHEDULED</span></c:when>
                        <c:when test="${application.status == 'SELECTED'}"><span class="status-badge badge-selected">SELECTED</span></c:when>
                        <c:when test="${application.status == 'REJECTED'}"><span class="status-badge badge-rejected">REJECTED</span></c:when>
                        <c:otherwise><span class="status-badge"><c:out value="${application.status}"/></span></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

    <!-- TIMELINE CARD -->
    <div class="card" style="padding: 28px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); background: white;">
        <h2 style="font-size: 1.18rem; margin: 0 0 28px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 10px; padding-bottom: 14px; border-bottom: 1px solid var(--border-light);">
            <i class="fa-solid fa-clock-rotate-left" style="color: var(--accent-orange, #E87722);"></i> Progression Milestone Audit Log
        </h2>

        <c:choose>
            <c:when test="${not empty statusHistory}">
                <!-- Vertical Timeline Container -->
                <div class="audit-timeline" style="position: relative; padding-left: 54px; margin-left: 6px;">
                    <!-- Connecting Vertical Line -->
                    <div style="position: absolute; left: 21px; top: 20px; bottom: 24px; width: 3px; background: #E2E8F0; border-radius: 2px;"></div>

                    <c:forEach var="item" items="${statusHistory}">
                        <c:set var="eventColor" value="#1B365D"/>
                        <c:set var="eventIcon" value="fa-solid fa-circle-check"/>
                        <c:set var="eventTitle" value="${item.status}"/>
                        <c:choose>
                            <c:when test="${item.status == 'APPLIED'}">
                                <c:set var="eventColor" value="#3B82F6"/>
                                <c:set var="eventIcon" value="fa-solid fa-file-arrow-up"/>
                                <c:set var="eventTitle" value="Application Submitted"/>
                            </c:when>
                            <c:when test="${item.status == 'SHORTLISTED'}">
                                <c:set var="eventColor" value="#10B981"/>
                                <c:set var="eventIcon" value="fa-solid fa-user-check"/>
                                <c:set var="eventTitle" value="Application Shortlisted"/>
                            </c:when>
                            <c:when test="${item.status == 'INTERVIEW_SCHEDULED'}">
                                <c:set var="eventColor" value="#8B5CF6"/>
                                <c:set var="eventIcon" value="fa-solid fa-calendar-check"/>
                                <c:set var="eventTitle" value="Interview Scheduled"/>
                            </c:when>
                            <c:when test="${item.status == 'INTERVIEW_RESCHEDULED'}">
                                <c:set var="eventColor" value="#F59E0B"/>
                                <c:set var="eventIcon" value="fa-solid fa-clock-rotate-left"/>
                                <c:set var="eventTitle" value="Interview Rescheduled"/>
                            </c:when>
                            <c:when test="${item.status == 'INTERVIEW_CANCELLED'}">
                                <c:set var="eventColor" value="#EF4444"/>
                                <c:set var="eventIcon" value="fa-solid fa-calendar-xmark"/>
                                <c:set var="eventTitle" value="Interview Cancelled"/>
                            </c:when>
                            <c:when test="${item.status == 'INTERVIEW_COMPLETED'}">
                                <c:set var="eventColor" value="#059669"/>
                                <c:set var="eventIcon" value="fa-solid fa-clipboard-check"/>
                                <c:set var="eventTitle" value="Interview Completed"/>
                            </c:when>
                            <c:when test="${item.status == 'SELECTED'}">
                                <c:set var="eventColor" value="#16A34A"/>
                                <c:set var="eventIcon" value="fa-solid fa-award"/>
                                <c:set var="eventTitle" value="Final Selection Offer"/>
                            </c:when>
                            <c:when test="${item.status == 'REJECTED'}">
                                <c:set var="eventColor" value="#DC2626"/>
                                <c:set var="eventIcon" value="fa-solid fa-circle-xmark"/>
                                <c:set var="eventTitle" value="Application Rejected"/>
                            </c:when>
                        </c:choose>

                        <div class="timeline-step" style="position: relative; margin-bottom: 30px;">

                            <!-- Milestone Icon Circle Node -->
                            <div class="timeline-node" style="position: absolute; left: -54px; top: 0px; width: 44px; height: 44px; border-radius: 50%; background: ${eventColor}; display: flex; align-items: center; justify-content: center; color: #ffffff; font-size: 16px; box-shadow: 0 3px 8px rgba(0,0,0,0.12); border: 3px solid #ffffff; z-index: 2;">
                                <i class="${eventIcon}"></i>
                            </div>

                            <!-- Milestone Content Card -->
                            <div style="background: #ffffff; border: 1px solid #E2E8F0; border-radius: 12px; padding: 20px 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.04);">

                                <!-- Card Header: Title, Timestamp, Status Badge -->
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; flex-wrap: wrap; gap: 8px;">
                                    <div>
                                        <h3 style="margin: 0 0 4px 0; font-size: 1.05rem; font-weight: 700; color: var(--primary-navy, #1B365D); display: flex; align-items: center; gap: 8px;">
                                            <c:out value="${eventTitle}"/>
                                        </h3>
                                        <div style="font-size: 0.82rem; color: #64748B;">
                                            <i class="fa-regular fa-clock" style="margin-right: 4px;"></i><c:out value="${item.formattedChangedAt}"/>
                                        </div>
                                    </div>
                                    <div>
                                        <span class="badge" style="background: rgba(27, 54, 93, 0.08); color: var(--primary-navy, #1B365D); font-weight: 700; font-size: 0.82rem; padding: 5px 12px; border-radius: 6px;">
                                            <c:out value="${item.status}"/>
                                        </span>
                                    </div>
                                </div>

                                <!-- Narrative Summary / Comments -->
                                <div style="font-size: 0.9rem; color: #334155; line-height: 1.5;">
                                    <c:choose>
                                        <c:when test="${item.status == 'APPLIED'}">
                                            Application submitted by <strong><c:out value="${not empty studentUser ? studentUser.name : 'Candidate'}"/></strong> (<c:out value="${not empty student ? student.rollNumber : '-'}"/>) for the position of <strong><c:out value="${not empty job ? job.title : 'Opportunity'}"/></strong> at <strong><c:out value="${not empty job ? job.company : '-'}"/></strong>.
                                        </c:when>
                                        <c:when test="${item.status == 'SHORTLISTED'}">
                                            Candidate profile met academic criteria and cleared departmental shortlisting to enter the active interview pool.
                                        </c:when>
                                        <c:when test="${item.status == 'INTERVIEW_SCHEDULED'}">
                                            Formal placement interview process initialized and scheduled for the candidate.
                                        </c:when>
                                        <c:when test="${item.status == 'INTERVIEW_RESCHEDULED'}">
                                            Interview timetable rescheduled. Updated schedule confirmed on portal.
                                        </c:when>
                                        <c:when test="${item.status == 'INTERVIEW_CANCELLED'}">
                                            Interview schedule was cancelled.
                                        </c:when>
                                        <c:when test="${item.status == 'INTERVIEW_COMPLETED'}">
                                            All scheduled evaluation rounds were concluded and performance feedback compiled.
                                        </c:when>
                                        <c:when test="${item.status == 'SELECTED'}">
                                            Institutional evaluation approved: Candidate officially selected and extended a placement offer.
                                        </c:when>
                                        <c:when test="${item.status == 'REJECTED'}">
                                            Candidate application was not selected during the final decision review.
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${item.statusDescription}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- ============================================================= -->
                                <!-- SPECIAL SUBSECTION 1: DYNAMIC ROUNDS UNDER INTERVIEW_SCHEDULED -->
                                <!-- ============================================================= -->
                                <c:if test="${item.status == 'INTERVIEW_SCHEDULED'}">

                                    <!-- Primary Interview Info Box -->
                                    <c:if test="${not empty interview}">
                                        <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; padding: 14px 18px; margin-top: 14px; display: flex; flex-wrap: wrap; gap: 20px; align-items: center;">
                                            <div>
                                                <span style="font-size: 0.72rem; text-transform: uppercase; color: #64748B; font-weight: 700; display: block;">Initial Interview Date</span>
                                                <span style="font-size: 0.95rem; font-weight: 700; color: #1B365D;">
                                                    <i class="fa-regular fa-calendar" style="color: #E87722; margin-right: 5px;"></i>${interview.formattedInterviewDate}
                                                </span>
                                            </div>
                                            <div>
                                                <span style="font-size: 0.72rem; text-transform: uppercase; color: #64748B; font-weight: 700; display: block;">Time</span>
                                                <span style="font-size: 0.95rem; font-weight: 700; color: #1B365D;">
                                                    <i class="fa-regular fa-clock" style="color: #E87722; margin-right: 5px;"></i>${interview.formattedInterviewTime}
                                                </span>
                                            </div>
                                            <div>
                                                <span style="font-size: 0.72rem; text-transform: uppercase; color: #64748B; font-weight: 700; display: block;">Mode</span>
                                                <span style="font-size: 0.95rem; font-weight: 700; color: #1B365D;">
                                                    <c:choose>
                                                        <c:when test="${interview.mode == 'ONLINE'}"><i class="fa-solid fa-video" style="color: #0284C7; margin-right: 5px;"></i> Online</c:when>
                                                        <c:otherwise><i class="fa-solid fa-building" style="color: #64748B; margin-right: 5px;"></i> Offline</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                            <div>
                                                <span style="font-size: 0.72rem; text-transform: uppercase; color: #64748B; font-weight: 700; display: block;">Status</span>
                                                <span class="badge" style="background: rgba(2, 132, 199, 0.1); color: #0284C7; font-weight: 700;">${interview.status}</span>
                                            </div>
                                        </div>
                                    </c:if>

                                    <!-- Nested Dynamic Rounds Subsection (Requirement 3 & 4) -->
                                    <c:choose>
                                        <c:when test="${not empty interviewRounds}">
                                            <div style="margin-top: 20px; border-top: 1px dashed #CBD5E1; padding-top: 18px;">
                                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 8px;">
                                                    <h4 style="margin: 0; font-size: 0.98rem; font-weight: 700; color: #1B365D; display: flex; align-items: center; gap: 8px;">
                                                        <i class="fa-solid fa-layer-group" style="color: #E87722;"></i> Interview Rounds (${interviewRounds.size()} Rounds Configured)
                                                    </h4>
                                                    <button type="button" class="btn btn-outline" onclick="toggleAllRounds()" style="font-size: 0.75rem; padding: 4px 10px; border-radius: 4px; background: white;">
                                                        <i class="fa-solid fa-up-right-and-down-left-from-center" style="margin-right: 4px;"></i> Expand/Collapse All
                                                    </button>
                                                </div>

                                                <!-- Dynamic Rounds Summary Table -->
                                                <div class="table-responsive" style="margin-bottom: 16px; border: 1px solid #E2E8F0; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.02);">
                                                    <table class="table" style="width: 100%; margin: 0; font-size: 0.86rem; border-collapse: collapse;">
                                                        <thead style="background: #1B365D; color: #ffffff;">
                                                            <tr>
                                                                <th style="padding: 10px 14px; text-align: left; font-weight: 600;">Round</th>
                                                                <th style="padding: 10px 14px; text-align: left; font-weight: 600;">Round Name</th>
                                                                <th style="padding: 10px 14px; text-align: left; font-weight: 600;">Date &amp; Time</th>
                                                                <th style="padding: 10px 14px; text-align: left; font-weight: 600;">Mode</th>
                                                                <th style="padding: 10px 14px; text-align: center; font-weight: 600;">Result</th>
                                                                <th style="padding: 10px 14px; text-align: center; font-weight: 600;">Status</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach var="round" items="${interviewRounds}">
                                                                <tr style="border-bottom: 1px solid #E2E8F0; background: #ffffff;">
                                                                    <td style="padding: 10px 14px; font-weight: 700; color: #1B365D;">Round ${round.roundNumber}</td>
                                                                    <td style="padding: 10px 14px; font-weight: 600;"><c:out value="${round.roundName}"/></td>
                                                                    <td style="padding: 10px 14px; color: #334155;">
                                                                        <i class="fa-regular fa-calendar" style="color: #E87722; margin-right: 4px;"></i>${round.formattedInterviewDate}, ${round.formattedInterviewTime}
                                                                    </td>
                                                                    <td style="padding: 10px 14px;">
                                                                        <span class="badge" style="background: #F1F5F9; color: #334155; font-size: 0.75rem;">${round.mode}</span>
                                                                    </td>
                                                                    <td style="padding: 10px 14px; text-align: center;">
                                                                        <c:choose>
                                                                            <c:when test="${round.result == 'PASSED'}">
                                                                                <span class="badge" style="background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; font-weight: 700;"><i class="fa-solid fa-check"></i> Passed</span>
                                                                            </c:when>
                                                                            <c:when test="${round.result == 'FAILED'}">
                                                                                <span class="badge" style="background: #FEF2F2; color: #DC2626; border: 1px solid #FECACA; font-weight: 700;"><i class="fa-solid fa-xmark"></i> Failed</span>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <span class="badge" style="background: #FFFBEB; color: #D97706; border: 1px solid #FDE68A; font-weight: 700;"><i class="fa-regular fa-clock"></i> Pending</span>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </td>
                                                                    <td style="padding: 10px 14px; text-align: center;">
                                                                        <c:choose>
                                                                            <c:when test="${round.status == 'COMPLETED'}">
                                                                                <span class="badge" style="background: #10B981; color: white; font-weight: 600;">Completed</span>
                                                                            </c:when>
                                                                            <c:when test="${round.status == 'SCHEDULED'}">
                                                                                <span class="badge" style="background: #0284C7; color: white; font-weight: 600;">Scheduled</span>
                                                                            </c:when>
                                                                            <c:when test="${round.status == 'RESCHEDULED'}">
                                                                                <span class="badge" style="background: #E87722; color: white; font-weight: 600;">Rescheduled</span>
                                                                            </c:when>
                                                                            <c:when test="${round.status == 'CANCELLED'}">
                                                                                <span class="badge" style="background: #EF4444; color: white; font-weight: 600;">Cancelled</span>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <span class="badge" style="background: #64748B; color: white; font-weight: 600;">${round.status}</span>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </td>
                                                                </tr>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                </div>

                                                <!-- Detailed Expandable / Collapsible Round Cards (Requirement 3 & 4) -->
                                                <div class="round-cards-container" style="display: flex; flex-direction: column; gap: 12px;">
                                                    <c:forEach var="round" items="${interviewRounds}">
                                                        <div class="round-card" id="round-card-${round.id}" style="border: 1px solid #E2E8F0; border-radius: 8px; background: #FAFBFD; overflow: hidden;">
                                                            <!-- Toggle Header -->
                                                            <div class="round-card-header" onclick="toggleRoundDetails(${round.id})" style="padding: 12px 18px; background: #F8FAFC; cursor: pointer; display: flex; justify-content: space-between; align-items: center; user-select: none;">
                                                                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                                                                    <span style="background: #1B365D; color: white; font-size: 0.75rem; font-weight: 700; padding: 3px 8px; border-radius: 4px;">Round ${round.roundNumber}</span>
                                                                    <strong style="color: #1B365D; font-size: 0.92rem;"><c:out value="${round.roundName}"/></strong>
                                                                    <span style="font-size: 0.8rem; color: #64748B;"><i class="fa-regular fa-calendar" style="color: #E87722; margin-right: 4px;"></i>${round.formattedInterviewDate} at ${round.formattedInterviewTime}</span>
                                                                </div>
                                                                <div style="display: flex; align-items: center; gap: 8px;">
                                                                    <c:if test="${round.result == 'PASSED'}"><span class="badge" style="background: #ECFDF5; color: #059669; font-size: 0.72rem; font-weight: 700;">PASSED</span></c:if>
                                                                    <c:if test="${round.result == 'FAILED'}"><span class="badge" style="background: #FEF2F2; color: #DC2626; font-size: 0.72rem; font-weight: 700;">FAILED</span></c:if>
                                                                    <c:if test="${round.result == 'PENDING'}"><span class="badge" style="background: #FFFBEB; color: #D97706; font-size: 0.72rem; font-weight: 700;">PENDING</span></c:if>
                                                                    <i class="fa-solid fa-chevron-down round-toggle-icon" id="round-icon-${round.id}" style="color: #64748B; font-size: 0.82rem; transition: transform 0.2s;"></i>
                                                                </div>
                                                            </div>

                                                            <!-- Details Body -->
                                                            <div class="round-card-body" id="round-body-${round.id}" style="padding: 16px 20px; display: block; background: white; border-top: 1px solid #E2E8F0;">
                                                                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 12px; font-size: 0.85rem;">
                                                                    <div>
                                                                        <span style="color: #64748B; font-size: 0.75rem; text-transform: uppercase; font-weight: 600; display: block;">Interviewer / Panel</span>
                                                                        <span style="color: #1E293B; font-weight: 600;">
                                                                            <c:out value="${not empty round.interviewerName ? round.interviewerName : 'Panel Assigned'}"/>
                                                                            <c:if test="${not empty round.interviewerInfo}">
                                                                                <span style="color: #64748B; font-weight: normal;">(<c:out value="${round.interviewerInfo}"/>)</span>
                                                                            </c:if>
                                                                        </span>
                                                                    </div>
                                                                    <div>
                                                                        <span style="color: #64748B; font-size: 0.75rem; text-transform: uppercase; font-weight: 600; display: block;">Interview Mode</span>
                                                                        <span style="color: #1E293B; font-weight: 600;"><c:out value="${round.mode}"/></span>
                                                                    </div>
                                                                    <div>
                                                                        <span style="color: #64748B; font-size: 0.75rem; text-transform: uppercase; font-weight: 600; display: block;">Round Status</span>
                                                                        <span style="color: #1E293B; font-weight: 600;"><c:out value="${round.status}"/></span>
                                                                    </div>
                                                                </div>

                                                                <!-- Feedback and Remarks -->
                                                                <c:if test="${not empty round.feedback || not empty round.remarks}">
                                                                    <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 6px; padding: 12px 16px; margin-bottom: 12px; font-size: 0.86rem;">
                                                                        <c:if test="${not empty round.feedback}">
                                                                            <div style="margin-bottom: 6px;">
                                                                                <strong style="color: #1B365D;"><i class="fa-solid fa-comment-dots" style="color: #0284C7; margin-right: 5px;"></i> Evaluation Feedback:</strong>
                                                                                <span style="color: #334155;"><c:out value="${round.feedback}"/></span>
                                                                            </div>
                                                                        </c:if>
                                                                        <c:if test="${not empty round.remarks}">
                                                                            <div>
                                                                                <strong style="color: #1B365D;"><i class="fa-solid fa-pen" style="color: #64748B; margin-right: 5px;"></i> Internal Remarks:</strong>
                                                                                <span style="color: #334155;"><c:out value="${round.remarks}"/></span>
                                                                            </div>
                                                                        </c:if>
                                                                    </div>
                                                                </c:if>

                                                                <!-- Cancellation Reason -->
                                                                <c:if test="${not empty round.cancellationReason}">
                                                                    <div style="background: #FEF2F2; border-left: 4px solid #EF4444; padding: 10px 14px; border-radius: 4px; font-size: 0.85rem; color: #991B1B; margin-bottom: 12px;">
                                                                        <strong>Cancellation Reason:</strong> <c:out value="${round.cancellationReason}"/>
                                                                    </div>
                                                                </c:if>

                                                                <!-- Round Rescheduling & Audit History (Requirement 4) -->
                                                                <c:set var="rHistoryList" value="${roundHistoryMap[round.id]}"/>
                                                                <c:if test="${not empty rHistoryList}">
                                                                    <div style="margin-top: 12px; border-top: 1px solid #E2E8F0; padding-top: 10px;">
                                                                        <div style="font-size: 0.78rem; font-weight: 700; color: #1B365D; text-transform: uppercase; margin-bottom: 8px; display: flex; align-items: center; gap: 6px;">
                                                                            <i class="fa-solid fa-clock-rotate-left" style="color: #E87722;"></i> Round Audit &amp; Rescheduling History
                                                                        </div>
                                                                        <div style="display: flex; flex-direction: column; gap: 8px;">
                                                                            <c:forEach var="rh" items="${rHistoryList}">
                                                                                <c:choose>
                                                                                    <c:when test="${rh.action == 'RESCHEDULED'}">
                                                                                        <div style="background: #FFF7ED; border-left: 4px solid #E87722; padding: 10px 14px; border-radius: 4px; font-size: 0.83rem;">
                                                                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; flex-wrap: wrap;">
                                                                                                <strong style="color: #9A3412;"><i class="fa-solid fa-calendar-days"></i> Round Rescheduled</strong>
                                                                                                <span style="color: #7C2D12; font-size: 0.75rem;"><i class="fa-regular fa-clock"></i> ${rh.formattedActionDate}</span>
                                                                                            </div>
                                                                                            <div style="color: #431407; margin-bottom: 2px;">
                                                                                                <c:out value="${rh.remarks}"/>
                                                                                            </div>
                                                                                            <div style="font-size: 0.74rem; color: #7C2D12;">
                                                                                                Action recorded by: <c:out value="${not empty rh.performedBy ? rh.performedBy : 'Coordinator'}"/>
                                                                                            </div>
                                                                                        </div>
                                                                                    </c:when>
                                                                                    <c:otherwise>
                                                                                        <div style="background: #F8FAFC; border-left: 3px solid #CBD5E1; padding: 6px 12px; border-radius: 4px; font-size: 0.8rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 6px;">
                                                                                            <span style="color: #334155;">
                                                                                                <strong><c:out value="${rh.action}"/>:</strong> <c:out value="${rh.remarks}"/>
                                                                                            </span>
                                                                                            <span style="color: #64748B; font-size: 0.74rem;">
                                                                                                <c:out value="${rh.performedBy}"/> &bull; ${rh.formattedActionDate}
                                                                                            </span>
                                                                                        </div>
                                                                                    </c:otherwise>
                                                                                </c:choose>
                                                                            </c:forEach>
                                                                        </div>
                                                                    </div>
                                                                </c:if>

                                                            </div>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="margin-top: 14px; padding: 12px 16px; background: #F8FAFC; border-radius: 6px; font-size: 0.85rem; color: #64748B;">
                                                <i class="fa-solid fa-info-circle" style="color: #0284C7; margin-right: 6px;"></i> Primary interview scheduled. Multi-round evaluation stages have not been configured yet.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                </c:if>

                                <!-- ============================================================= -->
                                <!-- SPECIAL SUBSECTION 2: FINAL DECISION DOSSIER                 -->
                                <!-- ============================================================= -->
                                <c:if test="${(item.status == 'SELECTED' || item.status == 'REJECTED') && not empty interviewReport}">
                                    <div style="margin-top: 16px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; padding: 16px 20px;">
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 8px;">
                                            <span style="font-size: 0.78rem; text-transform: uppercase; font-weight: 700; color: #1B365D; display: flex; align-items: center; gap: 6px;">
                                                <i class="fa-solid fa-certificate" style="color: #059669;"></i> Formal Evaluation Dossier Report
                                            </span>
                                            <span style="font-size: 0.78rem; font-weight: 700; color: #0284C7; background: rgba(2, 132, 199, 0.08); padding: 3px 8px; border-radius: 4px;">
                                                UUID: ${interviewReport.reportUuid}
                                            </span>
                                        </div>

                                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-bottom: 12px; font-size: 0.85rem;">
                                            <div>
                                                <span style="color: #64748B; font-size: 0.75rem; text-transform: uppercase; font-weight: 600; display: block;">Rounds Cleared</span>
                                                <strong style="color: #059669;">${interviewReport.passedRounds} / ${interviewReport.totalRounds} Passed</strong>
                                            </div>
                                            <div>
                                                <span style="color: #64748B; font-size: 0.75rem; text-transform: uppercase; font-weight: 600; display: block;">Decision Recorded By</span>
                                                <span style="color: #1E293B; font-weight: 600;"><c:out value="${interviewReport.generatedBy}"/></span>
                                            </div>
                                        </div>

                                        <c:if test="${not empty interviewReport.coordinatorRemarks}">
                                            <div style="margin-bottom: 8px; font-size: 0.85rem;">
                                                <strong style="color: #1B365D;">Coordinator Remarks:</strong>
                                                <span style="color: #334155;"><c:out value="${interviewReport.coordinatorRemarks}"/></span>
                                            </div>
                                        </c:if>

                                        <c:if test="${not empty interviewReport.overallFeedback}">
                                            <div style="font-size: 0.85rem;">
                                                <strong style="color: #1B365D;">Institutional Feedback:</strong>
                                                <span style="color: #334155;"><c:out value="${interviewReport.overallFeedback}"/></span>
                                            </div>
                                        </c:if>
                                    </div>
                                </c:if>

                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div style="text-align: center; padding: 40px 20px; color: var(--text-muted);">
                    <div style="font-size: 2.2rem; margin-bottom: 8px; color: var(--accent-orange, #E87722);">
                        <i class="fa-solid fa-scroll"></i>
                    </div>
                    <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary-navy);">No Audit History Entries Available</div>
                    <div style="font-size: 0.88rem; margin-top: 4px;">Initial status was recorded upon application creation.</div>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Bottom Action Navigation -->
        <div style="margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--border-light); display: flex; gap: 12px; justify-content: space-between; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/applications" class="btn btn-outline" style="padding: 10px 20px; border-radius: var(--radius-md);">
                &larr; Back to Applications
            </a>
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/interview-rounds?applicationId=${application.id}" class="btn btn-primary" style="padding: 10px 20px; border-radius: var(--radius-md);">
                        <i class="fa-solid fa-layer-group" style="margin-right: 6px;"></i> Manage Interview Rounds
                    </a>
                </c:if>
                <c:if test="${not empty interviewReport || not empty interviewRounds}">
                    <a href="${pageContext.request.contextPath}/interview-report/pdf?applicationId=${application.id}" class="btn btn-secondary" target="_blank" style="padding: 10px 20px; border-radius: var(--radius-md);">
                        <i class="fa-solid fa-file-pdf" style="margin-right: 6px;"></i> Download PDF Dossier
                    </a>
                </c:if>
            </div>
        </div>
    </div>

</main>

<script>
function toggleRoundDetails(roundId) {
    var body = document.getElementById('round-body-' + roundId);
    var icon = document.getElementById('round-icon-' + roundId);
    if (!body || !icon) return;
    if (body.style.display === 'none') {
        body.style.display = 'block';
        icon.style.transform = 'rotate(0deg)';
    } else {
        body.style.display = 'none';
        icon.style.transform = 'rotate(-90deg)';
    }
}

var allExpanded = true;
function toggleAllRounds() {
    allExpanded = !allExpanded;
    var bodies = document.querySelectorAll('.round-card-body');
    var icons = document.querySelectorAll('.round-toggle-icon');
    bodies.forEach(function(b) {
        b.style.display = allExpanded ? 'block' : 'none';
    });
    icons.forEach(function(i) {
        i.style.transform = allExpanded ? 'rotate(0deg)' : 'rotate(-90deg)';
    });
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />