<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="applications" scope="request" />
<c:set var="pageTitle" value="Application Dossier #${application.id} | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 960px; margin: 36px auto 60px auto;">

    <!-- Page Header Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 50px; height: 50px; background: rgba(27, 54, 93, 0.08); border-radius: 12px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 24px;">
                <i class="fa-solid fa-address-card"></i>
            </div>
            <div>
                <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                    Application Dossier &bull; #${application.id}
                </h1>
                <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                    Candidate academic profile, opportunity specifications, and evaluation round timeline.
                </p>
            </div>
        </div>

        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/applications?action=history&id=${application.id}" class="btn btn-outline" style="font-size: 0.85rem; padding: 8px 14px; border-radius: var(--radius-sm);">
                <i class="fa-solid fa-clock-rotate-left" style="margin-right: 4px;"></i> Status History
            </a>
            <a href="${pageContext.request.contextPath}/interview-rounds?applicationId=${application.id}" class="btn btn-primary" style="font-size: 0.85rem; padding: 8px 14px; border-radius: var(--radius-sm);">
                <i class="fa-solid fa-bullseye" style="margin-right: 4px;"></i> Dynamic Rounds
            </a>
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

    <!-- SECTION 1: CANDIDATE INFORMATION -->
    <c:choose>
        <c:when test="${application.external or not empty externalStudent}">
            <div class="card" style="padding: 26px 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 22px;">
                <h2 style="font-size: 1.15rem; margin: 0 0 18px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; justify-content: space-between; padding-bottom: 12px; border-bottom: 1px solid var(--border-light);">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-user-graduate" style="color: var(--primary-navy);"></i> External Candidate Profile
                    </span>
                    <span style="font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 6px; background: #e0e7ff; color: #3730a3;">
                        <i class="fa-solid fa-building-columns" style="margin-right: 4px;"></i> External Applicant
                    </span>
                </h2>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px;">
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Candidate Full Name</span>
                        <span style="font-size: 1.05rem; font-weight: 700; color: var(--text-dark);">
                            <c:choose>
                                <c:when test="${not empty externalStudent && not empty externalStudent.fullName}"><c:out value="${externalStudent.fullName}"/></c:when>
                                <c:when test="${not empty externalStudent && not empty externalStudent.applicantName}"><c:out value="${externalStudent.applicantName}"/></c:when>
                                <c:when test="${not empty studentUser && not empty studentUser.name}"><c:out value="${studentUser.name}"/></c:when>
                                <c:otherwise>External Candidate</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">College / University</span>
                        <span style="font-size: 0.95rem; font-weight: 600; color: var(--text-dark);">
                            <c:out value="${not empty externalStudent ? externalStudent.collegeName : '-'}"/>
                            <c:if test="${not empty externalStudent && not empty externalStudent.collegeLocation}">
                                (<c:out value="${externalStudent.collegeLocation}"/>)
                            </c:if>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Degree &amp; Department</span>
                        <span style="font-size: 0.95rem; font-weight: 600; color: var(--text-dark);">
                            <c:out value="${not empty externalStudent ? externalStudent.degree : '-'}"/>
                            &bull; <c:out value="${not empty externalStudent ? externalStudent.department : '-'}"/>
                            <c:if test="${not empty externalStudent && not empty externalStudent.currentSemester}">
                                (Sem ${externalStudent.currentSemester})
                            </c:if>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Cumulative CGPA</span>
                        <span style="font-size: 1.1rem; font-weight: 800; color: var(--accent-teal);">
                            <c:out value="${not empty externalStudent ? externalStudent.cgpa : '-'}"/> / 10.00
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Email Address</span>
                        <span style="font-size: 0.92rem; color: var(--text-dark);">
                            <c:choose>
                                <c:when test="${not empty externalStudent && not empty externalStudent.email}"><c:out value="${externalStudent.email}"/></c:when>
                                <c:when test="${not empty externalStudent && not empty externalStudent.applicantEmail}"><c:out value="${externalStudent.applicantEmail}"/></c:when>
                                <c:when test="${not empty studentUser && not empty studentUser.email}"><c:out value="${studentUser.email}"/></c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Contact Phone &amp; Grad Year</span>
                        <span style="font-size: 0.92rem; color: var(--text-dark);">
                            <c:out value="${not empty externalStudent && not empty externalStudent.phone ? externalStudent.phone : '-'}"/>
                            <c:if test="${not empty externalStudent && not empty externalStudent.graduationYear}">
                                &bull; Class of ${externalStudent.graduationYear}
                            </c:if>
                        </span>
                    </div>
                </div>
                <c:if test="${not empty externalStudent && not empty externalStudent.resumeUrl}">
                    <div style="margin-top: 16px; padding-top: 14px; border-top: 1px solid var(--border-light); display: flex; align-items: center; justify-content: space-between;">
                        <span style="font-size: 0.85rem; color: var(--text-muted);">
                            <i class="fa-solid fa-paperclip" style="margin-right: 6px;"></i> Attached Resume Document
                        </span>
                        <a href="${pageContext.request.contextPath}/resume/download?externalStudentId=${externalStudent.id}" target="_blank" class="btn btn-outline" style="font-size: 0.85rem; display: inline-flex; align-items: center; gap: 6px;">
                            <i class="fa-solid fa-file-pdf" style="color: #ef4444;"></i> View Candidate Resume
                        </a>
                    </div>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card" style="padding: 26px 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 22px;">
                <h2 style="font-size: 1.15rem; margin: 0 0 18px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light);">
                    <i class="fa-solid fa-user-graduate" style="color: var(--primary-navy);"></i> Candidate Academic Information
                </h2>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px;">
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Candidate Full Name</span>
                        <span style="font-size: 1.05rem; font-weight: 700; color: var(--text-dark);">
                            <c:choose>
                                <c:when test="${not empty studentUser && not empty studentUser.name}"><c:out value="${studentUser.name}"/></c:when>
                                <c:when test="${not empty student && not empty student.name}"><c:out value="${student.name}"/></c:when>
                                <c:when test="${not empty student && not empty student.fullName}"><c:out value="${student.fullName}"/></c:when>
                                <c:otherwise>Registered Student</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Registration / Roll Number</span>
                        <span style="font-size: 1rem; font-weight: 600; color: var(--text-dark);">
                            <c:out value="${not empty student ? student.rollNumber : '-'}"/>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Department &amp; Class</span>
                        <span style="font-size: 0.95rem; font-weight: 600; color: var(--text-dark);">
                            <c:out value="${not empty student ? student.department : '-'}"/>
                            <c:if test="${not empty student && not empty student.graduationYear}">
                                (${student.graduationYear})
                            </c:if>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Cumulative CGPA</span>
                        <span style="font-size: 1.1rem; font-weight: 800; color: var(--accent-teal);">
                            <c:out value="${not empty student ? student.cgpa : '-'}"/> / 10.00
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Institutional Email</span>
                        <span style="font-size: 0.92rem; color: var(--text-dark);">
                            <c:choose>
                                <c:when test="${not empty studentUser && not empty studentUser.email}"><c:out value="${studentUser.email}"/></c:when>
                                <c:when test="${not empty student && not empty student.email}"><c:out value="${student.email}"/></c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Contact Phone</span>
                        <span style="font-size: 0.92rem; color: var(--text-dark);">
                            <c:out value="${not empty student && not empty student.phone ? student.phone : '-'}"/>
                        </span>
                    </div>
                </div>
                <c:if test="${not empty student && not empty student.skills}">
                    <div style="margin-top: 16px; padding-top: 14px; border-top: 1px solid var(--border-light);">
                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 6px;">Technical Competencies &amp; Skills</span>
                        <span style="font-size: 0.92rem; color: var(--primary-navy); background: rgba(27, 54, 93, 0.05); padding: 6px 12px; border-radius: var(--radius-sm); display: inline-block;">
                            <i class="fa-solid fa-code" style="margin-right: 6px;"></i><c:out value="${student.skills}"/>
                        </span>
                    </div>
                </c:if>
            </div>
        </c:otherwise>
    </c:choose>

    <!-- SECTION 2: OPPORTUNITY SPECIFICATIONS -->
    <div class="card" style="padding: 26px 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 22px;">
        <h2 style="font-size: 1.15rem; margin: 0 0 18px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light);">
            <i class="fa-solid fa-briefcase" style="color: var(--accent-gold);"></i> Opportunity Specifications
        </h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px;">
            <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Job / Internship Role</span>
                <span style="font-size: 1.05rem; font-weight: 700; color: var(--primary-navy);">
                    <c:out value="${not empty job ? job.title : 'Opportunity'}"/>
                </span>
            </div>
            <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Recruiting Partner</span>
                <span style="font-size: 1rem; font-weight: 600; color: var(--text-dark);">
                    <c:out value="${not empty job ? job.company : '-'}"/>
                </span>
            </div>
            <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Location &amp; Model</span>
                <span style="font-size: 0.95rem; color: var(--text-dark);">
                    <c:out value="${not empty job ? job.location : '-'}"/>
                    <c:if test="${not empty job && not empty job.jobType}">
                        &bull; <span class="badge" style="background: rgba(27, 54, 93, 0.08); color: var(--primary-navy); font-size: 0.72rem; padding: 2px 6px;">${job.jobType}</span>
                    </c:if>
                </span>
            </div>
            <div style="background: var(--bg-subtle); padding: 14px 16px; border-radius: var(--radius-md);">
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Compensation Package</span>
                <span style="font-size: 1.1rem; font-weight: 800; color: var(--success);">
                    <c:out value="${not empty job ? job.salary : '-'}"/>
                </span>
            </div>
        </div>
        <c:if test="${not empty job && not empty job.description}">
            <div style="margin-top: 16px; padding-top: 14px; border-top: 1px solid var(--border-light);">
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 6px;">Job Scope</span>
                <p style="font-size: 0.92rem; color: var(--text-dark); line-height: 1.6; margin: 0;">
                    <c:out value="${job.description}"/>
                </p>
            </div>
        </c:if>
    </div>

    <!-- SECTION 3: APPLICATION STATUS & INTERVIEW -->
    <div class="card" style="padding: 26px 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px;">
        <h2 style="font-size: 1.15rem; margin: 0 0 18px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light);">
            <i class="fa-solid fa-calendar-check" style="color: var(--accent-teal);"></i> Application Status &amp; Interview Lifecycle
        </h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 18px; align-items: center;">
            <div>
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 6px;">Current Lifecycle Stage</span>
                <div>
                    <c:choose>
                        <c:when test="${application.status == 'APPLIED'}">
                            <span class="status-badge badge-applied">APPLIED</span>
                        </c:when>
                        <c:when test="${application.status == 'SHORTLISTED'}">
                            <span class="status-badge badge-shortlisted">SHORTLISTED</span>
                        </c:when>
                        <c:when test="${application.status == 'SELECTED'}">
                            <span class="status-badge badge-selected">SELECTED</span>
                        </c:when>
                        <c:when test="${application.status == 'REJECTED'}">
                            <span class="status-badge badge-rejected">REJECTED</span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge"><c:out value="${application.status}"/></span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div>
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Application Submission Date</span>
                <span style="font-size: 0.95rem; font-weight: 600; color: var(--text-dark);">
                    <c:out value="${application.applicationDate}"/>
                </span>
            </div>

            <div>
                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase;">Primary Interview Schedule</span>
                <c:choose>
                    <c:when test="${not empty interview}">
                        <span style="font-size: 0.95rem; font-weight: 700; color: var(--primary-navy);">
                            <c:out value="${interview.interviewDate}"/> at <c:out value="${interview.interviewTime}"/>
                        </span>
                        <span style="font-size: 0.8rem; color: var(--text-muted); display: block; margin-top: 2px;">
                            Mode: <c:out value="${interview.mode}"/> | Status: <c:out value="${interview.status}"/>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span style="font-size: 0.88rem; color: var(--text-muted); font-style: italic;">
                            No primary session scheduled yet.
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${application.status == 'SELECTED' || application.status == 'REJECTED'}">
                <div>
                    <span style="font-size: 0.75rem; color: var(--text-muted); display: block; font-weight: 700; text-transform: uppercase; margin-bottom: 6px;">Official PDF Report</span>
                    <div style="display: flex; gap: 8px;">
                        <a href="${pageContext.request.contextPath}/interview-report?action=download&applicationId=${application.id}"
                           class="btn btn-outline" style="font-size: 0.8rem; padding: 6px 12px; color: var(--success); border-radius: var(--radius-sm);">
                            <i class="fa-solid fa-file-arrow-down" style="margin-right: 4px;"></i> Download PDF
                        </a>
                    </div>
                </div>
            </c:if>
        </div>
    </div>

    <!-- ACTION BUTTON BAR -->
    <div style="display: flex; gap: 12px; align-items: center; justify-content: space-between; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}/applications" class="btn btn-outline" style="padding: 10px 20px; border-radius: var(--radius-md);">
            &larr; Back to Applications
        </a>

        <div style="display: flex; gap: 10px;">
            <a href="${pageContext.request.contextPath}/interview-rounds?applicationId=${application.id}" class="btn btn-primary" style="padding: 10px 20px; border-radius: var(--radius-md);">
                <i class="fa-solid fa-bullseye" style="margin-right: 6px;"></i> Manage Dynamic Interview Rounds
            </a>

            <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                <c:if test="${application.status == 'SHORTLISTED'}">
                    <a href="${pageContext.request.contextPath}/interviews?applicationId=${application.id}" class="btn btn-secondary" style="padding: 10px 20px; border-radius: var(--radius-md);">
                        <i class="fa-solid fa-calendar-plus" style="margin-right: 6px;"></i> Schedule Primary Interview
                    </a>
                </c:if>
            </c:if>
        </div>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />