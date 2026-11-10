<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="${job.isInternship() || isInternshipsEndpoint ? 'internships' : 'jobs'}" scope="request" />
<c:set var="pageTitle" value="${job.title} | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 900px; margin: 36px auto 60px auto;">

    <!-- Opportunity Header Card -->
    <div class="card" style="padding: 28px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 20px;">
        <div style="display: flex; gap: 20px; align-items: flex-start;">
            <div style="width: 58px; height: 58px; background: rgba(27, 54, 93, 0.08); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 26px; flex-shrink: 0;">
                <i class="fa-solid fa-building"></i>
            </div>
            <div>
                <h1 class="page-title" style="margin: 0 0 6px 0; font-size: 1.65rem; color: var(--primary-navy); font-weight: 800;">
                    <c:out value="${job.title}"/>
                </h1>
                <div style="display: flex; align-items: center; gap: 14px; flex-wrap: wrap; color: var(--text-muted); font-size: 0.95rem;">
                    <span style="font-weight: 600; color: var(--text-dark);"><i class="fa-regular fa-building" style="margin-right: 4px;"></i><c:out value="${job.company}"/></span>
                    <span>&bull;</span>
                    <span><i class="fa-solid fa-location-dot" style="margin-right: 4px; color: var(--danger);"></i><c:out value="${job.location}"/></span>
                    <span>&bull;</span>
                    <span style="font-size: 0.85rem; background: var(--bg-subtle); padding: 2px 8px; border-radius: 4px;">Ref: #${job.id}</span>
                </div>
            </div>
        </div>
        <div>
            <c:choose>
                <c:when test="${job.status == 'Active'}">
                    <span class="badge badge-success" style="font-size: 0.85rem; padding: 7px 16px; border-radius: 20px;">
                        <i class="fa-solid fa-circle" style="font-size:7px; vertical-align:middle; margin-right:5px;"></i>Active Drive
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="badge badge-danger" style="font-size: 0.85rem; padding: 7px 16px; border-radius: 20px;">
                        <i class="fa-solid fa-circle" style="font-size:7px; vertical-align:middle; margin-right:5px;"></i>Drive Closed
                    </span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- MAIN SPECIFICATIONS CARD -->
    <div class="card" style="padding: 28px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px;">
        <h2 style="font-size: 1.15rem; margin: 0 0 20px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light);">
            <i class="fa-solid fa-briefcase" style="color: var(--accent-gold);"></i> Role Specifications &amp; Compensation
        </h2>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px;">
            <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid var(--success);">
                <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Package / Stipend</span>
                <span style="font-size: 1.2rem; font-weight: 800; color: var(--success);">
                    <c:out value="${job.salary}"/>
                </span>
            </div>

            <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid var(--primary-navy);">
                <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Engagement Model</span>
                <span style="font-size: 1.05rem; font-weight: 700; color: var(--primary-navy);">
                    <c:out value="${job.jobType}"/>
                </span>
            </div>

            <c:if test="${not empty job.duration}">
                <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid var(--accent-gold);">
                    <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Tenure / Duration</span>
                    <span style="font-size: 1.05rem; font-weight: 600; color: var(--text-dark);">
                        <c:out value="${job.duration}"/>
                    </span>
                </div>
            </c:if>

            <c:if test="${not empty job.deadline}">
                <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid var(--warning);">
                    <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Application Deadline</span>
                    <span style="font-size: 1.05rem; font-weight: 700; color: var(--warning);">
                        <i class="fa-regular fa-calendar-check" style="margin-right: 4px;"></i> <c:out value="${job.deadline}"/>
                    </span>
                </div>
            </c:if>

            <c:if test="${not empty job.minCgpa}">
                <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid var(--accent-teal);">
                    <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Minimum CGPA Threshold</span>
                    <span style="font-size: 1.05rem; font-weight: 700; color: var(--accent-teal);">
                        <c:out value="${job.minCgpa}"/> / 10.00
                    </span>
                </div>
            </c:if>

            <c:if test="${not empty job.eligibleDepartments}">
                <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid var(--secondary-blue);">
                    <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Eligible Engineering Streams</span>
                    <span style="font-size: 0.95rem; font-weight: 600; color: var(--text-dark);">
                        <c:out value="${job.eligibleDepartments}"/>
                    </span>
                </div>
            </c:if>

            <div style="background: var(--bg-subtle); padding: 16px; border-radius: var(--radius-md); border-left: 3px solid #6366f1;">
                <span style="font-size: 0.78rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Candidate Audience</span>
                <span style="font-size: 0.95rem; font-weight: 700; color: #4338ca;">
                    <c:choose>
                        <c:when test="${job.targetAudience == 'EXTERNAL'}">
                            <i class="fa fa-university"></i> External Students Only
                        </c:when>
                        <c:when test="${job.targetAudience == 'BOTH'}">
                            <i class="fa fa-users"></i> CUTM &amp; External
                        </c:when>
                        <c:otherwise>
                            <i class="fa fa-graduation-cap"></i> CUTM Internal Only
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>

        <div style="margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--border-light);">
            <h3 style="font-size: 0.9rem; color: var(--primary-navy); font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 8px;">
                Job Description &amp; Technical Requirements
            </h3>
            <div style="font-size: 0.96rem; color: var(--text-dark); line-height: 1.7; background: #fafbfc; padding: 18px 20px; border-radius: var(--radius-md); border: 1px solid var(--border-light);">
                <c:choose>
                    <c:when test="${not empty job.description}">
                        <c:out value="${job.description}"/>
                    </c:when>
                    <c:otherwise>
                        <span style="color: var(--text-muted); font-style: italic;">No specific job description provided by recruiter.</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- APPLICATION CALL TO ACTION CARD -->
    <div class="card" style="padding: 28px 32px; border-radius: var(--radius-lg); background: white; border: 1px solid var(--border-light); box-shadow: var(--shadow-sm); margin-bottom: 24px;">
        <h3 style="margin: 0 0 10px 0; color: var(--primary-navy); font-size: 1.15rem; font-weight: 700; display: flex; align-items: center; gap: 8px;">
            <i class="fa-solid fa-paper-plane" style="color: var(--primary-navy);"></i> Candidate Application Process
        </h3>
        <c:choose>
            <c:when test="${job.status == 'Active'}">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <c:set var="isAudienceEligible" value="${(empty sessionScope.studentType or sessionScope.studentType == 'INTERNAL') ? job.eligibleForInternal : job.eligibleForExternal}" />
                        <c:choose>
                            <c:when test="${sessionScope.userRole == 'STUDENT' and not isAudienceEligible}">
                                <div class="alert alert-warning" style="margin: 0; background: #fffbeb; color: #b45309; border: 1px solid #fde68a; padding: 14px 18px; border-radius: 8px; display: flex; align-items: center; gap: 10px;">
                                    <i class="fa fa-exclamation-triangle" style="font-size: 1.2rem;"></i>
                                    <div>
                                        <strong>Audience Notice:</strong> This opportunity is specifically designated for
                                        <strong><c:out value="${job.targetAudience == 'EXTERNAL' ? 'external university students' : 'Centurion University (CUTM) students'}"/></strong>.
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p style="color: var(--text-muted); font-size: 0.92rem; margin: 0 0 18px 0; line-height: 1.5;">
                                    Applying will submit your verified candidate profile, academic records, and resume directly to the recruiting team for initial screening.
                                </p>
                                <form action="${pageContext.request.contextPath}/applications" method="post" style="margin: 0;"
                                      onsubmit="return (typeof PortalPopups !== 'undefined') ? PortalPopups.confirmAction(event, 'Are you sure you want to apply for ${job.title} at ${job.company}?') : confirm('Are you sure you want to apply for this opportunity?');">
                                    <input type="hidden" name="action" value="apply">
                                    <input type="hidden" name="jobId" value="${job.id}">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <button type="submit" class="btn btn-primary" style="padding: 12px 26px; font-weight: 700; font-size: 1rem; border-radius: var(--radius-md);">
                                        <i class="fa-solid fa-check" style="margin-right: 6px;"></i> Submit Application for this Opportunity
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--text-muted); font-size: 0.92rem; margin: 0 0 16px 0;">
                            Please sign in with your verified institutional student credentials to submit an application for this drive.
                        </p>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="padding: 10px 22px; border-radius: var(--radius-md);">
                            <i class="fa-solid fa-right-to-bracket" style="margin-right: 6px;"></i> Sign In to Apply
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <div class="alert alert-error" style="margin: 0;" role="alert">
                    <i class="fa-solid fa-circle-xmark alert-icon"></i>
                    <div class="alert-content">
                        <strong>Notice:</strong> Applications are officially closed for this position.
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
            <div style="margin-top: 20px; padding-top: 16px; border-top: 1px dashed var(--border-light); display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px;">
                <span style="font-size: 0.88rem; color: var(--text-muted);">
                    Administrative Applicant Tracking:
                </span>
                <a href="${pageContext.request.contextPath}/applications?jobId=${job.id}" class="btn btn-outline" style="font-size: 0.85rem; padding: 7px 16px; border-radius: var(--radius-sm);">
                    <i class="fa-solid fa-users" style="margin-right: 6px;"></i> View All Candidate Applications &rarr;
                </a>
            </div>
        </c:if>
    </div>

    <!-- BUTTON BAR -->
    <div style="display: flex; gap: 12px; align-items: center; justify-content: space-between; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}${job.isInternship() || isInternshipsEndpoint ? '/internships' : '/jobs'}" class="btn btn-outline" style="padding: 10px 20px; border-radius: var(--radius-md);">
            &larr; <c:out value="${job.isInternship() || isInternshipsEndpoint ? 'Back to Internships' : 'Back to Opportunities'}"/>
        </a>

        <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
            <a href="${pageContext.request.contextPath}/jobs?action=edit&id=${job.id}" class="btn btn-secondary" style="padding: 10px 20px; border-radius: var(--radius-md);">
                <i class="fa-solid fa-pen-to-square" style="margin-right: 6px;"></i> <c:out value="${job.isInternship() ? 'Edit Internship' : 'Edit Opportunity'}"/>
            </a>
        </c:if>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />