<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="applications" scope="request" />
<c:set var="pageTitle" value="Placement &amp; Internship Applications | Centurion University Placement Portal" scope="request" />

<!-- Common Header -->
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<div class="portal-container" style="max-width: 1240px; margin: 30px auto 60px auto;">

    <!-- Page Header Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 48px; height: 48px; background: rgba(27, 54, 93, 0.08); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 22px;">
                <i class="fa-solid fa-file-signature"></i>
            </div>
            <div>
                <span style="font-size: 11px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em; display: block; margin-bottom: 2px;">
                    Application Lifecycle &amp; Tracking
                </span>
                <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                    <c:choose>
                        <c:when test="${sessionScope.userRole == 'STUDENT'}">My Campus Applications</c:when>
                        <c:otherwise>Candidate Applications Management</c:otherwise>
                    </c:choose>
                </h1>
                <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                    Manage student submissions, verify academic eligibility, coordinate multi-round interviews, and record hiring decisions.
                </p>
            </div>
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

    <!-- KPI Statistic Cards (Clickable Cards) -->
    <div class="kpi-grid" style="margin-bottom: 24px;">
        <a href="${pageContext.request.contextPath}/applications" class="kpi-card kpi-blue clickable-card" title="View all applications">
            <div class="kpi-value">${applicationStats.TOTAL}</div>
            <div class="kpi-label"><i class="fa-solid fa-list-check" style="margin-right: 4px;"></i> Total Applications</div>
        </a>
        <a href="${pageContext.request.contextPath}/applications?status=APPLIED" class="kpi-card clickable-card" title="Filter APPLIED status">
            <div class="kpi-value" style="color: var(--text-dark);">${applicationStats.APPLIED}</div>
            <div class="kpi-label"><i class="fa-solid fa-paper-plane" style="margin-right: 4px;"></i> Applied</div>
        </a>
        <a href="${pageContext.request.contextPath}/shortlist" class="kpi-card kpi-gold clickable-card" title="View Shortlisted Pool">
            <div class="kpi-value" style="color: var(--accent-gold);">${applicationStats.SHORTLISTED}</div>
            <div class="kpi-label"><i class="fa-solid fa-star" style="margin-right: 4px;"></i> Shortlisted Pool</div>
        </a>
        <a href="${pageContext.request.contextPath}/applications?status=SELECTED" class="kpi-card kpi-green clickable-card" title="Filter SELECTED status">
            <div class="kpi-value" style="color: var(--success);">${applicationStats.SELECTED}</div>
            <div class="kpi-label"><i class="fa-solid fa-trophy" style="margin-right: 4px;"></i> Selected Offers</div>
        </a>
        <a href="${pageContext.request.contextPath}/applications?status=REJECTED" class="kpi-card clickable-card" title="Filter REJECTED status">
            <div class="kpi-value" style="color: var(--danger);">${applicationStats.REJECTED}</div>
            <div class="kpi-label"><i class="fa-solid fa-circle-xmark" style="margin-right: 4px;"></i> Rejected</div>
        </a>
    </div>

    <!-- Create Application Form Section -->
    <div class="card" style="padding: 24px 28px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; background: white;">
        <div style="margin-bottom: 16px;">
            <h3 style="font-size: 1.05rem; margin: 0 0 4px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px;">
                <i class="fa-solid fa-plus-circle" style="color: var(--primary-navy);"></i> Submit New Application
            </h3>
            <p style="font-size: 0.85rem; color: var(--text-muted); margin: 0;">
                Directly submit an application for an active campus placement or internship drive.
            </p>
        </div>

        <form action="${pageContext.request.contextPath}/applications" method="post">
            <input type="hidden" name="action" value="create">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; align-items: flex-end;">
                <c:choose>
                    <c:when test="${sessionScope.userRole == 'STUDENT'}">
                        <div class="form-group" style="margin: 0; background: var(--bg-subtle); padding: 10px 14px; border-radius: var(--radius-sm); border: 1px solid var(--border-light); display: flex; align-items: center; height: 42px;">
                            <span style="font-size: 0.85rem; color: var(--text-muted);">
                                👤 Applying as: <strong style="color: var(--primary-navy);"><c:out value="${sessionScope.userName}"/></strong>
                            </span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="form-group" style="margin: 0;">
                            <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Student ID <span style="color:var(--danger);">*</span></label>
                            <input type="number" name="studentId" class="form-control" placeholder="Student Profile ID" required style="height: 42px;" />
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Opportunity ID <span style="color:var(--danger);">*</span></label>
                    <input type="number" name="jobId" class="form-control" placeholder="Job / Internship ID" required style="height: 42px;" />
                </div>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Submission Date (Optional)</label>
                    <input type="date" name="applicationDate" class="form-control" style="height: 42px;" />
                </div>

                <div class="form-group" style="margin: 0;">
                    <button type="submit" class="btn btn-primary" style="width: 100%; height: 42px; font-weight: 700; border-radius: var(--radius-sm);">
                        <i class="fa-solid fa-paper-plane" style="margin-right: 6px;"></i> Submit Application
                    </button>
                </div>
            </div>
        </form>
    </div>

    <!-- Filter & Search Section -->
    <div class="card" style="padding: 20px 24px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; background: white;">
        <h3 style="font-size: 1rem; margin: 0 0 14px 0; color: var(--primary-navy); font-weight: 700; display: flex; align-items: center; gap: 8px;">
            <i class="fa-solid fa-filter" style="color: var(--accent-gold);"></i> Filter &amp; Search Applications
        </h3>
        <form action="${pageContext.request.contextPath}/applications" method="get">
            <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end;">
                <c:if test="${sessionScope.userRole != 'STUDENT'}">
                    <div class="form-group" style="flex: 1; min-width: 160px; margin: 0;">
                        <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Student ID</label>
                        <input type="number" name="studentId" class="form-control" placeholder="e.g. 101" value="<c:out value='${selectedStudentId}'/>" style="height: 40px;" />
                    </div>
                </c:if>

                <c:if test="${sessionScope.userRole != 'STUDENT'}">
                    <div class="form-group" style="flex: 1; min-width: 150px; margin: 0;">
                        <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Candidate Type</label>
                        <select name="category" class="form-control" style="height: 40px;">
                            <option value="ALL" ${selectedCategory == 'ALL' ? 'selected' : ''}>All Candidates</option>
                            <option value="INTERNAL" ${selectedCategory == 'INTERNAL' ? 'selected' : ''}>Centurion (Internal)</option>
                            <option value="EXTERNAL" ${selectedCategory == 'EXTERNAL' ? 'selected' : ''}>External Candidates</option>
                        </select>
                    </div>
                </c:if>

                <div class="form-group" style="flex: 1; min-width: 160px; margin: 0;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Opportunity ID</label>
                    <input type="number" name="jobId" class="form-control" placeholder="e.g. 201" value="<c:out value='${selectedJobId}'/>" style="height: 40px;" />
                </div>

                <div class="form-group" style="flex: 1; min-width: 160px; margin: 0;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Status</label>
                    <select name="status" class="form-control" style="height: 40px;">
                        <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                        <option value="APPLIED" ${selectedStatus == 'APPLIED' ? 'selected' : ''}>APPLIED</option>
                        <option value="SHORTLISTED" ${selectedStatus == 'SHORTLISTED' ? 'selected' : ''}>SHORTLISTED</option>
                        <option value="SELECTED" ${selectedStatus == 'SELECTED' ? 'selected' : ''}>SELECTED</option>
                        <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>REJECTED</option>
                    </select>
                </div>

                <div style="display: flex; gap: 8px; margin: 0;">
                    <button type="submit" class="btn btn-primary" style="height: 40px; padding: 0 18px; font-weight: 600; border-radius: var(--radius-sm);">
                        Filter
                    </button>
                    <a href="${pageContext.request.contextPath}/applications" class="btn btn-outline" style="height: 40px; padding: 0 14px; display: inline-flex; align-items: center; border-radius: var(--radius-sm);">
                        Reset
                    </a>
                </div>
            </div>
        </form>
    </div>

    <!-- Applications Table -->
    <div class="card" style="padding: 0; overflow: hidden; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);">
        <div style="padding: 16px 22px; background: white; border-bottom: 1px solid var(--border-light); display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 1.05rem; color: var(--primary-navy); font-weight: 700;">
                Application Records
            </div>
            <span style="font-size: 0.82rem; background: var(--bg-subtle); color: var(--text-muted); padding: 4px 10px; border-radius: 12px; font-weight: 600;">
                Showing <c:out value="${empty applications ? 0 : applications.size()}"/> application(s)
            </span>
        </div>

        <c:choose>
            <c:when test="${empty applications}">
                <div style="text-align: center; padding: 48px 20px; background: white;">
                    <div style="font-size: 2.2rem; margin-bottom: 10px; color: var(--accent-gold);">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </div>
                    <h4 style="margin: 0 0 6px 0; color: var(--primary-navy); font-size: 1.15rem; font-weight: 700;">Application Not Found</h4>
                    <p style="font-size: 0.9rem; color: var(--text-muted); max-width: 480px; margin: 0 auto 18px auto; line-height: 1.5;">
                        No placement applications match the specified search parameters or filter criteria. Please check entered identifiers or clear filters.
                    </p>
                    <a href="${pageContext.request.contextPath}/applications" class="btn btn-primary" style="font-size: 0.85rem; padding: 8px 18px; border-radius: var(--radius-sm);">
                        Reset All Filters
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table class="portal-table data-table" style="margin: 0; width: 100%; border-collapse: collapse;">
                        <thead>
                            <tr>
                                <th style="padding: 14px 16px; text-align: left;">App ID</th>
                                <th style="padding: 14px 16px; text-align: left;">Candidate Profile</th>
                                <th style="padding: 14px 16px; text-align: left;">Opportunity &amp; Company</th>
                                <th style="padding: 14px 16px; text-align: center;">Applied Date</th>
                                <th style="padding: 14px 16px; text-align: center;">Status</th>
                                <th style="padding: 14px 16px; text-align: center;">Interview &amp; Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="app" items="${applications}">
                                <c:set var="isExt" value="${app.external or not empty externalStudentMap[app.externalStudentId] or not empty externalStudentMap[app.studentId]}" />
                                <c:set var="extStudent" value="${not empty externalStudentMap[app.externalStudentId] ? externalStudentMap[app.externalStudentId] : externalStudentMap[app.studentId]}" />
                                <c:set var="stud" value="${studentMap[app.studentId]}" />
                                <c:set var="usr" value="${not empty userMap[stud.userId] ? userMap[stud.userId] : (not empty userMap[app.studentId] ? userMap[app.studentId] : userMap[extStudent.userId])}" />
                                <c:set var="jb" value="${jobMap[app.jobId]}" />

                                <tr style="border-bottom: 1px solid var(--border-light);">
                                    <td style="padding: 14px 16px; text-align: left;">
                                        <a href="${pageContext.request.contextPath}/applications?action=details&id=${app.id}" style="font-weight: 700; color: var(--primary-navy); text-decoration: none;">
                                            #${app.id}
                                        </a>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: left;">
                                        <c:choose>
                                            <c:when test="${isExt and not empty extStudent}">
                                                <div style="font-weight: 700; color: var(--text-dark); display: flex; align-items: center; gap: 6px;">
                                                    <c:choose>
                                                        <c:when test="${not empty extStudent.fullName}"><c:out value="${extStudent.fullName}"/></c:when>
                                                        <c:when test="${not empty extStudent.applicantName}"><c:out value="${extStudent.applicantName}"/></c:when>
                                                        <c:when test="${not empty usr.name}"><c:out value="${usr.name}"/></c:when>
                                                        <c:otherwise>External Candidate</c:otherwise>
                                                    </c:choose>
                                                    <span style="font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; background: #e0e7ff; color: #3730a3;">External</span>
                                                </div>
                                                <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                                    <c:out value="${extStudent.collegeName}"/> &bull;
                                                    <c:out value="${extStudent.degree}"/> (<c:out value="${extStudent.department}"/>) &bull;
                                                    CGPA: <strong><c:out value="${extStudent.cgpa}"/></strong>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div style="font-weight: 700; color: var(--text-dark);">
                                                    <c:choose>
                                                        <c:when test="${not empty usr.name}"><c:out value="${usr.name}"/></c:when>
                                                        <c:when test="${not empty stud.name}"><c:out value="${stud.name}"/></c:when>
                                                        <c:when test="${not empty stud.fullName}"><c:out value="${stud.fullName}"/></c:when>
                                                        <c:otherwise>Student #${app.studentId}</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                                    <c:if test="${not empty stud.rollNumber}">Roll: <c:out value="${stud.rollNumber}"/> &bull; </c:if>
                                                    <c:if test="${not empty stud.department}"><c:out value="${stud.department}"/> &bull; </c:if>
                                                    <c:if test="${not empty stud.cgpa}">CGPA: <strong><c:out value="${stud.cgpa}"/></strong></c:if>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: left;">
                                        <div style="font-weight: 700; color: var(--primary-navy);">
                                            <c:choose>
                                                <c:when test="${not empty jb.title}"><c:out value="${jb.title}"/></c:when>
                                                <c:otherwise>Opportunity #${app.jobId}</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                                            <c:if test="${not empty jb.company}"><c:out value="${jb.company}"/> &bull; </c:if>
                                            <c:if test="${not empty jb.jobType}"><c:out value="${jb.jobType}"/></c:if>
                                        </div>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center; color: var(--text-muted); font-size: 0.88rem;">
                                        ${app.applicationDate}
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${app.status == 'APPLIED'}">
                                                <span class="status-badge badge-applied">APPLIED</span>
                                            </c:when>
                                            <c:when test="${app.status == 'SHORTLISTED'}">
                                                <span class="status-badge badge-shortlisted">SHORTLISTED</span>
                                            </c:when>
                                            <c:when test="${app.status == 'SELECTED'}">
                                                <span class="status-badge badge-selected">SELECTED</span>
                                            </c:when>
                                            <c:when test="${app.status == 'REJECTED'}">
                                                <span class="status-badge badge-rejected">REJECTED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge"><c:out value="${app.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center;">
                                        <div style="display: flex; gap: 6px; flex-wrap: wrap; align-items: center; justify-content: center;">
                                            <a href="${pageContext.request.contextPath}/applications?action=details&id=${app.id}" class="btn btn-outline btn-sm" title="View Application Details" style="font-size: 0.78rem; padding: 5px 10px;">
                                                <i class="fa-solid fa-eye" style="margin-right: 3px;"></i> Details
                                            </a>
                                            <a href="${pageContext.request.contextPath}/applications?action=history&id=${app.id}" class="btn btn-outline btn-sm" title="Audit History" style="font-size: 0.78rem; padding: 5px 10px;">
                                                <i class="fa-solid fa-clock-rotate-left" style="margin-right: 3px;"></i> History
                                            </a>

                                            <%-- Multi-round interview & report link --%>
                                            <c:if test="${app.status == 'SHORTLISTED' || app.status == 'SELECTED' || app.status == 'REJECTED'}">
                                                <a href="${pageContext.request.contextPath}/interview-rounds?applicationId=${app.id}" class="btn btn-primary btn-sm" title="Manage Multi-Round Evaluations" style="font-size: 0.78rem; padding: 5px 10px;">
                                                    <i class="fa-solid fa-bullseye" style="margin-right: 3px;"></i> Rounds
                                                </a>
                                            </c:if>

                                            <%-- Official PDF Report Link if Selected/Rejected --%>
                                            <c:if test="${app.status == 'SELECTED' || app.status == 'REJECTED'}">
                                                <a href="${pageContext.request.contextPath}/interview-report?action=download&applicationId=${app.id}" class="btn btn-accent btn-sm" title="Download Official PDF Report" style="font-size: 0.78rem; padding: 5px 10px;">
                                                    <i class="fa-solid fa-file-pdf" style="margin-right: 3px;"></i> PDF
                                                </a>
                                            </c:if>

                                            <%-- Coordinator Actions with Popups --%>
                                            <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                                                <c:if test="${app.status == 'APPLIED'}">
                                                    <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;" data-action-type="shortlist">
                                                        <input type="hidden" name="action" value="shortlist">
                                                        <input type="hidden" name="id" value="${app.id}">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                        <button type="submit" class="btn btn-success btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                            <i class="fa-solid fa-check" style="margin-right: 3px;"></i> Shortlist
                                                        </button>
                                                    </form>
                                                </c:if>

                                                <c:if test="${app.status == 'SHORTLISTED'}">
                                                    <c:choose>
                                                        <c:when test="${completedAppIds.contains(app.id)}">
                                                            <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;" data-action-type="select">
                                                                <input type="hidden" name="action" value="select">
                                                                <input type="hidden" name="id" value="${app.id}">
                                                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                                <button type="submit" class="btn btn-success btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                                    <i class="fa-solid fa-award" style="margin-right: 3px;"></i> Select
                                                                </button>
                                                            </form>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <button type="button" class="btn btn-outline btn-sm" disabled title="Candidate must complete an interview first" style="font-size: 0.78rem; padding: 5px 10px; opacity: 0.6;">
                                                                Select
                                                            </button>
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;" data-action-type="reject">
                                                        <input type="hidden" name="action" value="reject">
                                                        <input type="hidden" name="id" value="${app.id}">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                        <button type="submit" class="btn btn-danger btn-sm" style="font-size: 0.78rem; padding: 5px 10px;">
                                                            <i class="fa-solid fa-xmark" style="margin-right: 3px;"></i> Reject
                                                        </button>
                                                    </form>
                                                </c:if>

                                                <c:if test="${app.status != 'SELECTED'}">
                                                    <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;" data-action-type="delete">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="id" value="${app.id}">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                                        <button type="submit" class="btn btn-outline btn-sm" style="color:var(--danger); font-size: 0.78rem; padding: 5px 8px;" title="Delete Record">
                                                            <i class="fa-solid fa-trash-can"></i>
                                                        </button>
                                                    </form>
                                                </c:if>
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