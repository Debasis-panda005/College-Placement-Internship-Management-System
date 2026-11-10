<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="interviews" scope="request" />
<c:set var="pageTitle" value="Dynamic Interview Rounds - ${applicantUser.name} | Centurion University Placement Portal" scope="request" />

<!-- Common Navigation Header -->
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="portal-container" style="max-width: 1240px; margin: 30px auto 60px auto;">

    <!-- Top breadcrumb / navigation links -->
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; flex-wrap:wrap; gap:10px;">
        <div>
            <a href="${pageContext.request.contextPath}/interviews" class="btn btn-outline" style="font-size: 0.85rem; padding: 8px 16px; border-radius: var(--radius-sm);">
                &larr; Back to Interviews Overview
            </a>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/applications?action=details&id=${application.id}" class="btn btn-outline" style="font-size: 0.85rem; padding: 8px 16px; border-radius: var(--radius-sm);">
                <i class="fa-solid fa-address-card" style="margin-right: 4px;"></i> View Full Application Details
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

    <!-- Candidate & Opportunity Context Card -->
    <div class="card" style="border-left: 5px solid var(--primary-navy); padding: 26px 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; background: white;">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:16px;">
            <div>
                <span style="font-size:11px; font-weight:700; color:var(--accent-gold); text-transform:uppercase; letter-spacing:0.08em; display:block; margin-bottom:4px;">
                    Candidate Evaluation Drive
                </span>
                <h2 style="font-size:1.6rem; margin:0 0 6px 0; color: var(--primary-navy); font-weight: 800;">
                    <c:choose>
                        <c:when test="${not empty externalApplicant}">
                            <c:out value="${externalApplicant.fullName}"/>
                            <span style="font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 4px; background: #e0e7ff; color: #3730a3; vertical-align: middle; margin-left: 6px;">
                                <i class="fa-solid fa-building-columns" style="margin-right: 4px;"></i> External Candidate
                            </span>
                            <span style="font-size:1rem; font-weight:400; color:var(--text-muted); margin-left: 6px;">
                                (<c:out value="${externalApplicant.collegeName}"/> &bull; <c:out value="${externalApplicant.degree}"/> - <c:out value="${externalApplicant.department}"/>)
                            </span>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${applicantUser.name}"/>
                            <span style="font-size:1rem; font-weight:400; color:var(--text-muted); margin-left: 6px;">
                                (<c:out value="${applicant.rollNumber}"/> &bull; <c:out value="${applicant.department}"/>)
                            </span>
                        </c:otherwise>
                    </c:choose>
                </h2>
                <div style="font-size:0.95rem; color:var(--text-dark);">
                    Applied for: <strong style="color:var(--primary-navy);"><c:out value="${job.title}"/></strong> at <strong><c:out value="${job.company}"/></strong>
                    &bull; Current CGPA: <strong><c:out value="${not empty externalApplicant ? externalApplicant.cgpa : applicant.cgpa}"/></strong>
                </div>
            </div>

            <div style="text-align:right;">
                <div style="font-size:0.8rem; color:var(--text-muted); margin-bottom:6px; font-weight: 600;">Application Lifecycle Status</div>
                <c:choose>
                    <c:when test="${application.status == 'SELECTED'}">
                        <span class="status-badge badge-selected" style="font-size:0.9rem; padding:7px 16px; border-radius: 20px;"><i class="fa-solid fa-circle-check" style="margin-right: 6px;"></i> SELECTED</span>
                    </c:when>
                    <c:when test="${application.status == 'REJECTED'}">
                        <span class="status-badge badge-rejected" style="font-size:0.9rem; padding:7px 16px; border-radius: 20px;"><i class="fa-solid fa-circle-xmark" style="margin-right: 6px;"></i> REJECTED</span>
                    </c:when>
                    <c:when test="${application.status == 'SHORTLISTED'}">
                        <span class="status-badge badge-shortlisted" style="font-size:0.9rem; padding:7px 16px; border-radius: 20px;"><i class="fa-solid fa-clock" style="margin-right: 6px;"></i> SHORTLISTED FOR INTERVIEWS</span>
                    </c:when>
                    <c:otherwise>
                        <span class="status-badge badge-applied" style="font-size:0.9rem; padding:7px 16px; border-radius: 20px;"><c:out value="${application.status}"/></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Assessment metrics strip -->
        <div style="display:flex; gap:20px; margin-top:20px; padding-top:16px; border-top:1px solid var(--border-light); flex-wrap:wrap; font-size: 0.9rem;">
            <div><strong>Total Rounds:</strong> <span style="font-weight: 700; color: var(--primary-navy);">${stats.TOTAL}</span></div>
            <div><strong>Completed:</strong> <span style="font-weight: 700; color: var(--primary-navy);">${stats.COMPLETED}</span></div>
            <div><strong>Passed:</strong> <span style="color:var(--success); font-weight:700;">${stats.PASSED}</span></div>
            <div><strong>Failed:</strong> <span style="color:var(--danger); font-weight:700;">${stats.FAILED}</span></div>
            <div><strong>Cancelled:</strong> <span style="color:var(--text-muted); font-weight: 700;">${stats.CANCELLED}</span></div>
        </div>
    </div>

    <!-- Official PDF Evaluation Report Banner (When Selected or Rejected) -->
    <c:if test="${application.status == 'SELECTED' || application.status == 'REJECTED'}">
        <div class="card" style="background: linear-gradient(135deg, #10243E 0%, #1B365D 100%); color:#ffffff; border:none; padding: 26px 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-md); margin-bottom: 24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:16px;">
                <div>
                    <div style="font-size:11px; font-weight:700; color:var(--accent-gold); text-transform:uppercase; letter-spacing:0.08em; margin-bottom: 4px;">
                        Official Verification Document
                    </div>
                    <h3 style="color:#ffffff; margin:0 0 6px 0; font-size:1.3rem; font-weight: 800;">
                        Candidate Interview &amp; Selection Report (PDF)
                    </h3>
                    <p style="color:#cbd5e1; margin:0; font-size:0.9rem; max-width: 680px; line-height: 1.5;">
                        Official authenticated institutional evaluation dossier containing full round-by-round evaluations, interview scores, and authorized determination.
                    </p>
                </div>
                <div style="display:flex; gap:12px; flex-wrap:wrap;">
                    <a href="${pageContext.request.contextPath}/interview-report?action=view&applicationId=${application.id}" target="_blank" class="btn" style="background: rgba(255,255,255,0.15); color: white; border: 1px solid rgba(255,255,255,0.25); font-size: 0.88rem; padding: 10px 18px; border-radius: var(--radius-md);">
                        <i class="fa-solid fa-eye" style="margin-right: 6px;"></i> View PDF Preview
                    </a>
                    <a href="${pageContext.request.contextPath}/interview-report?action=download&applicationId=${application.id}" class="btn" style="background: var(--accent-gold); color: #10243E; font-weight: 700; font-size: 0.88rem; padding: 10px 18px; border-radius: var(--radius-md);">
                        <i class="fa-solid fa-file-arrow-down" style="margin-right: 6px;"></i> Download Official PDF
                    </a>
                </div>
            </div>
        </div>
    </c:if>

    <!-- Interview Rounds Evaluation Timeline -->
    <div class="card" style="padding: 28px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; background: white;">
        <h3 style="font-size:1.15rem; margin:0 0 4px 0; color: var(--primary-navy); font-weight: 700;">Multi-Round Interview Evaluation Timeline</h3>
        <p style="font-size:0.88rem; color:var(--text-muted); margin:0 0 24px 0;">
            Sequential round progression for this opportunity. Each stage must be successfully completed and passed before the next round proceeds.
        </p>

        <c:choose>
            <c:when test="${empty rounds}">
                <div style="text-align:center; padding:48px 20px; background:var(--bg-subtle); border-radius:var(--radius-md); border:1px dashed var(--border-light);">
                    <div style="font-size:2.2rem; margin-bottom:10px; color: var(--accent-gold);">
                        <i class="fa-solid fa-list-check"></i>
                    </div>
                    <h4 style="margin:0 0 6px 0; color:var(--primary-navy); font-size: 1.1rem; font-weight: 700;">No Interview Rounds Scheduled Yet</h4>
                    <p style="font-size:0.9rem; color:var(--text-muted); max-width:480px; margin:0 auto 16px auto;">
                        Use the scheduling section below to configure Round 1 (e.g., Aptitude, Coding, or Technical Round) for this shortlisted candidate.
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="round-timeline">
                    <c:forEach var="round" items="${rounds}">
                        <div class="round-timeline-card">
                            <div class="round-circle ${round.result == 'PASSED' ? 'circle-passed' : (round.result == 'FAILED' ? 'circle-failed' : '')}">
                                ${round.roundNumber}
                            </div>
                            <div class="round-details-box" style="box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);">
                                <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:10px; margin-bottom:12px;">
                                    <div>
                                        <h4 style="margin:0; font-size:1.1rem; color:var(--primary-navy); font-weight: 700;">
                                            <c:out value="${round.roundName}"/>
                                        </h4>
                                        <div style="font-size:0.85rem; color:var(--text-muted); margin-top:3px;">
                                            <i class="fa-regular fa-calendar" style="margin-right: 3px;"></i> <strong>${round.interviewDate}</strong> at <strong>${round.interviewTime}</strong>
                                            &bull; Mode: <strong>${round.mode}</strong>
                                        </div>
                                    </div>
                                    <div style="display:flex; gap:8px; align-items:center;">
                                        <span class="status-badge ${round.status == 'COMPLETED' ? 'badge-completed' : (round.status == 'CANCELLED' ? 'badge-cancelled' : 'badge-scheduled')}">
                                            <c:out value="${round.status}"/>
                                        </span>
                                        <c:if test="${round.result != 'PENDING'}">
                                            <span class="status-badge ${round.result == 'PASSED' ? 'badge-passed' : 'badge-failed'}">
                                                <c:out value="${round.result}"/>
                                            </span>
                                        </c:if>
                                    </div>
                                </div>

                                <div style="font-size:0.9rem; color:var(--text-dark); margin-bottom:10px;">
                                    <strong>Interviewer:</strong>
                                    <c:out value="${not empty round.interviewerName ? round.interviewerName : 'Panel Evaluation Team'}"/>
                                    <c:if test="${not empty round.interviewerInfo}">
                                        &bull; <span style="color:var(--text-muted);"><c:out value="${round.interviewerInfo}"/></span>
                                    </c:if>
                                </div>

                                <c:if test="${not empty round.feedback}">
                                    <div style="background:var(--bg-subtle); padding:12px 16px; border-radius:var(--radius-sm); font-size:0.9rem; margin-bottom:8px; border-left: 3px solid var(--accent-gold);">
                                        <strong>Evaluation Feedback:</strong> <c:out value="${round.feedback}"/>
                                    </div>
                                </c:if>

                                <c:if test="${not empty round.remarks}">
                                    <div style="font-size:0.85rem; color:var(--text-muted); margin-bottom:8px;">
                                        <em>Remarks: <c:out value="${round.remarks}"/></em>
                                    </div>
                                </c:if>

                                <c:if test="${not empty round.cancellationReason}">
                                    <div style="background:#fee2e2; color:#991b1b; padding:10px 14px; border-radius:var(--radius-sm); font-size:0.88rem; margin-bottom: 8px;">
                                        <strong>Cancellation Reason:</strong> <c:out value="${round.cancellationReason}"/>
                                    </div>
                                </c:if>

                                <!-- Action controls for Coordinator / Admin -->
                                <c:if test="${isStaff && (round.status == 'SCHEDULED' || round.status == 'RESCHEDULED')}">
                                    <div style="margin-top:14px; padding-top:12px; border-top:1px solid var(--border-light); display:flex; gap:10px; flex-wrap:wrap;">
                                        <!-- Complete Round Button / Dropdown Trigger -->
                                        <button type="button" class="btn btn-success btn-sm" onclick="document.getElementById('completeForm_${round.id}').style.display = (document.getElementById('completeForm_${round.id}').style.display === 'none' ? 'block' : 'none');">
                                            <i class="fa-solid fa-check" style="margin-right: 4px;"></i> Record Result (Complete Round)
                                        </button>

                                        <!-- Reschedule Button Trigger -->
                                        <button type="button" class="btn btn-outline btn-sm" onclick="document.getElementById('rescheduleForm_${round.id}').style.display = (document.getElementById('rescheduleForm_${round.id}').style.display === 'none' ? 'block' : 'none');">
                                            <i class="fa-regular fa-clock" style="margin-right: 4px;"></i> Reschedule
                                        </button>

                                        <!-- Cancel Button Trigger -->
                                        <button type="button" class="btn btn-danger btn-sm" onclick="document.getElementById('cancelForm_${round.id}').style.display = (document.getElementById('cancelForm_${round.id}').style.display === 'none' ? 'block' : 'none');">
                                            <i class="fa-solid fa-xmark" style="margin-right: 4px;"></i> Cancel
                                        </button>
                                    </div>

                                    <!-- Inline Complete Form -->
                                    <div id="completeForm_${round.id}" style="display:none; background:#f0fdf4; border:1px solid #bbf7d0; border-radius:var(--radius-md); padding:18px; margin-top:14px;">
                                        <h5 style="margin-top:0; color:#166534; font-size:0.95rem; font-weight: 700;">Complete Round &amp; Record Result: ${round.roundName}</h5>
                                        <form action="${pageContext.request.contextPath}/interview-rounds" method="post">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                                            <input type="hidden" name="action" value="completeRound" />
                                            <input type="hidden" name="applicationId" value="${application.id}" />
                                            <input type="hidden" name="roundId" value="${round.id}" />

                                            <div class="form-row">
                                                <div class="form-col">
                                                    <label class="form-label" style="font-weight: 700;">Evaluation Result <span style="color:var(--danger);">*</span></label>
                                                    <select name="result" class="form-control" required style="height: 40px;">
                                                        <option value="PASSED">PASSED (Candidate clears this round)</option>
                                                        <option value="FAILED">FAILED (Candidate did not clear)</option>
                                                    </select>
                                                </div>
                                            </div>

                                            <div class="form-group" style="margin-top:12px;">
                                                <label class="form-label" style="font-weight: 700;">Feedback to Candidate <span style="color:var(--danger);">*</span></label>
                                                <textarea name="feedback" class="form-control" rows="2" placeholder="e.g. Excellent problem solving skills in data structures and coding proficiency." required></textarea>
                                            </div>

                                            <div class="form-group" style="margin-top: 12px;">
                                                <label class="form-label" style="font-weight: 700;">Internal Coordinator Remarks</label>
                                                <textarea name="remarks" class="form-control" rows="1" placeholder="e.g. Recommended for Technical Round 2."></textarea>
                                            </div>

                                            <div style="display:flex; justify-content:flex-end; gap:8px; margin-top: 14px;">
                                                <button type="button" class="btn btn-outline btn-sm" onclick="document.getElementById('completeForm_${round.id}').style.display='none'">Close</button>
                                                <button type="submit" class="btn btn-success btn-sm">Submit Evaluation</button>
                                            </div>
                                        </form>
                                    </div>

                                    <!-- Inline Reschedule Form -->
                                    <div id="rescheduleForm_${round.id}" style="display:none; background:#f8fafc; border:1px solid var(--border-light); border-radius:var(--radius-md); padding:18px; margin-top:14px;">
                                        <h5 style="margin-top:0; color:var(--primary-navy); font-size:0.95rem; font-weight: 700;">Reschedule: ${round.roundName}</h5>
                                        <form action="${pageContext.request.contextPath}/interview-rounds" method="post">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                                            <input type="hidden" name="action" value="rescheduleRound" />
                                            <input type="hidden" name="applicationId" value="${application.id}" />
                                            <input type="hidden" name="roundId" value="${round.id}" />

                                            <div class="form-row">
                                                <div class="form-col">
                                                    <label class="form-label" style="font-weight: 700;">New Date <span style="color:var(--danger);">*</span></label>
                                                    <input type="date" name="interviewDate" class="form-control" min="${today}" value="${round.interviewDate}" required style="height: 40px;" />
                                                </div>
                                                <div class="form-col">
                                                    <label class="form-label" style="font-weight: 700;">New Time <span style="color:var(--danger);">*</span></label>
                                                    <input type="time" name="interviewTime" class="form-control" value="${round.interviewTime}" required style="height: 40px;" />
                                                </div>
                                                <div class="form-col">
                                                    <label class="form-label" style="font-weight: 700;">Mode <span style="color:var(--danger);">*</span></label>
                                                    <select name="mode" class="form-control" required style="height: 40px;">
                                                        <option value="ONLINE" ${round.mode == 'ONLINE' ? 'selected' : ''}>ONLINE</option>
                                                        <option value="OFFLINE" ${round.mode == 'OFFLINE' ? 'selected' : ''}>OFFLINE</option>
                                                    </select>
                                                </div>
                                            </div>

                                            <div class="form-group" style="margin-top:12px;">
                                                <label class="form-label" style="font-weight: 700;">Reason for Rescheduling</label>
                                                <input type="text" name="remarks" class="form-control" placeholder="e.g. Interviewer requested timing adjustment." style="height: 40px;" />
                                            </div>

                                            <div style="display:flex; justify-content:flex-end; gap:8px; margin-top: 14px;">
                                                <button type="button" class="btn btn-outline btn-sm" onclick="document.getElementById('rescheduleForm_${round.id}').style.display='none'">Close</button>
                                                <button type="submit" class="btn btn-primary btn-sm">Confirm Reschedule</button>
                                            </div>
                                        </form>
                                    </div>

                                    <!-- Inline Cancel Form -->
                                    <div id="cancelForm_${round.id}" style="display:none; background:#fee2e2; border:1px solid #fca5a5; border-radius:var(--radius-md); padding:18px; margin-top:14px;">
                                        <h5 style="margin-top:0; color:#991b1b; font-size:0.95rem; font-weight: 700;">Cancel Round: ${round.roundName}</h5>
                                        <form action="${pageContext.request.contextPath}/interview-rounds" method="post" data-action-type="cancelRound">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                                            <input type="hidden" name="action" value="cancelRound" />
                                            <input type="hidden" name="applicationId" value="${application.id}" />
                                            <input type="hidden" name="roundId" value="${round.id}" />

                                            <div class="form-group">
                                                <label class="form-label" style="font-weight: 700;">Reason for Cancellation <span style="color:var(--danger);">*</span></label>
                                                <input type="text" name="cancellationReason" class="form-control" placeholder="e.g. Drive postponed by recruiting company." required style="height: 40px;" />
                                            </div>

                                            <div style="display:flex; justify-content:flex-end; gap:8px; margin-top: 14px;">
                                                <button type="button" class="btn btn-outline btn-sm" onclick="document.getElementById('cancelForm_${round.id}').style.display='none'">Close</button>
                                                <button type="submit" class="btn btn-danger btn-sm">Confirm Cancellation</button>
                                            </div>
                                        </form>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Schedule Next Round Form (For Staff) -->
    <c:if test="${isStaff && application.status != 'SELECTED' && application.status != 'REJECTED' && not hasFailedRound && not hasActiveRound}">
        <div class="card" id="scheduleRoundCard" style="padding: 28px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; background: white;">
            <h3 style="font-size:1.15rem; margin:0 0 4px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px;">
                <i class="fa-solid fa-calendar-plus" style="color: var(--primary-navy);"></i> Schedule Round ${nextRoundNumber}
            </h3>
            <p style="font-size:0.88rem; color:var(--text-muted); margin:0 0 20px 0;">
                Configure the next sequential interview round for this company's hiring drive.
            </p>

            <form id="scheduleRoundForm" action="${pageContext.request.contextPath}/interview-rounds" method="post">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                <input type="hidden" name="action" value="addRound" />
                <input type="hidden" name="applicationId" value="${application.id}" />
                <input type="hidden" name="roundNumber" value="${nextRoundNumber}" />

                <div class="form-row">
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Round Name / Stage <span style="color:var(--danger);">*</span></label>
                        <select name="roundName" id="roundNameSelect" class="form-control" required onchange="toggleCustomRound(this.value)" style="height: 40px;">
                            <option value="Aptitude Round">Aptitude Round</option>
                            <option value="Group Discussion (GD)">Group Discussion (GD)</option>
                            <option value="Coding Assessment">Coding Assessment</option>
                            <option value="Technical Round 1" selected>Technical Round 1</option>
                            <option value="Technical Round 2">Technical Round 2</option>
                            <option value="Managerial Round">Managerial Round</option>
                            <option value="HR Round">HR Round</option>
                            <option value="Final HR Round">Final HR Round</option>
                            <option value="OTHER">Custom Round Name &hellip;</option>
                        </select>
                    </div>

                    <div class="form-col" id="customRoundGroup" style="display:none;">
                        <label class="form-label" style="font-weight: 700;">Custom Round Name <span style="color:var(--danger);">*</span></label>
                        <input type="text" name="customRoundName" id="customRoundInput" class="form-control" placeholder="e.g. System Design Interview" style="height: 40px;" />
                    </div>

                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Interview Mode <span style="color:var(--danger);">*</span></label>
                        <select name="mode" class="form-control" required style="height: 40px;">
                            <option value="ONLINE">ONLINE (Virtual / Video Call)</option>
                            <option value="OFFLINE">OFFLINE (On-Campus Placement Cell)</option>
                        </select>
                    </div>
                </div>

                <div class="form-row" style="margin-top:16px;">
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Interview Date <span style="color:var(--danger);">*</span></label>
                        <input type="date" name="interviewDate" class="form-control" min="${today}" required style="height: 40px;" />
                    </div>
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Interview Time <span style="color:var(--danger);">*</span></label>
                        <input type="time" name="interviewTime" class="form-control" required style="height: 40px;" />
                    </div>
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Interviewer Name / Panel</label>
                        <input type="text" name="interviewerName" class="form-control" placeholder="e.g. Rajesh Sharma, VP Engineering" style="height: 40px;" />
                    </div>
                </div>

                <div class="form-group" style="margin-top:16px;">
                    <label class="form-label" style="font-weight: 700;">Interviewer Contact / Meeting Details</label>
                    <textarea name="interviewerInfo" class="form-control" rows="2" placeholder="e.g. Google Meet Link: https://meet.google.com/xyz or Room: Seminar Hall 2, Placement Block"></textarea>
                </div>

                <div style="display:flex; justify-content:flex-end; margin-top:16px;">
                    <button type="submit" class="btn btn-primary" style="padding: 12px 24px; border-radius: var(--radius-md); font-weight: 700;">
                        <i class="fa-solid fa-calendar-plus" style="margin-right: 6px;"></i> Schedule Round ${nextRoundNumber}
                    </button>
                </div>
            </form>
        </div>
    </c:if>

    <!-- Final Determination Section (For Staff) -->
    <c:if test="${isStaff && application.status != 'SELECTED' && application.status != 'REJECTED'}">
        <div class="card" style="border: 2px solid var(--accent-gold); background: #fffcf7; padding: 28px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); margin-bottom: 24px;">
            <div style="display:flex; align-items:center; gap:10px; margin-bottom:8px;">
                <span style="font-size:24px;">⚖️</span>
                <h3 style="margin:0; font-size:1.15rem; color:var(--primary-navy); font-weight: 800;">Record Final Placement Determination</h3>
            </div>
            <p style="font-size:0.88rem; color:var(--text-muted); margin-bottom:18px;">
                Once all company-specific interview rounds have completed, the Placement Officer records the authoritative final determination.
            </p>

            <form action="${pageContext.request.contextPath}/interview-rounds" method="post">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                <input type="hidden" name="action" value="recordFinalDecision" />
                <input type="hidden" name="applicationId" value="${application.id}" />

                <div class="form-row">
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Final Determination <span style="color:var(--danger);">*</span></label>
                        <select name="finalDecision" class="form-control" required style="height: 40px;">
                            <c:choose>
                                <c:when test="${allCompletedAndPassed}">
                                    <option value="SELECTED" style="color:var(--success); font-weight:bold;">SELECTED (Candidate Offered Placement)</option>
                                    <option value="REJECTED" style="color:var(--danger);">REJECTED (Candidate Not Selected)</option>
                                </c:when>
                                <c:otherwise>
                                    <option value="REJECTED" style="color:var(--danger);">REJECTED (Candidate Did Not Pass Round / Drive Closed)</option>
                                </c:otherwise>
                            </c:choose>
                        </select>
                    </div>
                </div>

                <div class="form-row" style="margin-top:16px;">
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Coordinator Official Remarks</label>
                        <textarea name="coordinatorRemarks" class="form-control" rows="2" placeholder="e.g. Candidate exhibited strong algorithmic foundations and cultural alignment. Recommended for immediate onboarding."></textarea>
                    </div>
                    <div class="form-col">
                        <label class="form-label" style="font-weight: 700;">Overall Evaluation Feedback</label>
                        <textarea name="overallFeedback" class="form-control" rows="2" placeholder="e.g. Congratulations! Offer letter issuance pending corporate HR documentation."></textarea>
                    </div>
                </div>

                <div style="display:flex; justify-content:flex-end; gap:12px; margin-top:20px;">
                    <button type="submit" class="btn btn-accent" style="padding: 12px 24px; border-radius: var(--radius-md); font-weight: 700;">
                        ⚖️ Record Official Determination &amp; Issue PDF Report
                    </button>
                </div>
            </form>
        </div>
    </c:if>

</div>

<script>
    function toggleCustomRound(val) {
        var customGroup = document.getElementById("customRoundGroup");
        var customInput = document.getElementById("customRoundInput");
        if (!customGroup || !customInput) return;
        if (val === "OTHER") {
            customGroup.style.display = "block";
            customInput.required = true;
        } else {
            customGroup.style.display = "none";
            customInput.required = false;
        }
    }
</script>

<!-- Institutional Footer -->
<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
