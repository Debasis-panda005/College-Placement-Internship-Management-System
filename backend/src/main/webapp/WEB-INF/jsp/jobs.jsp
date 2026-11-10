<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="${isInternshipsEndpoint ? 'internships' : 'jobs'}" scope="request" />
<c:set var="pageTitle" value="${isInternshipsEndpoint ? 'Internship Drives' : 'Campus Placement Drives'} | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 1200px; margin: 30px auto 60px auto;">

    <!-- Page Header Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 48px; height: 48px; background: rgba(27, 54, 93, 0.08); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 22px;">
                <i class="fa-solid <c:out value="${isInternshipsEndpoint ? 'fa-id-badge' : 'fa-briefcase'}"/>"></i>
            </div>
            <div>
                <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.5rem; color: var(--primary-navy); font-weight: 800;">
                    <c:out value="${isInternshipsEndpoint ? 'Campus Internship Opportunities' : 'Campus Placement & Internship Opportunities'}"/>
                </h1>
                <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                    Explore verified campus recruitment drives, industry apprenticeships, and stipend internships.
                </p>
            </div>
        </div>

        <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
            <div>
                <c:choose>
                    <c:when test="${isInternshipsEndpoint}">
                        <a href="${pageContext.request.contextPath}/jobs?action=new&type=Internship" class="btn btn-primary" style="padding: 10px 18px; border-radius: var(--radius-md);">
                            <i class="fa-solid fa-plus" style="margin-right: 6px;"></i> Post New Internship
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/jobs?action=new" class="btn btn-primary" style="padding: 10px 18px; border-radius: var(--radius-md);">
                            <i class="fa-solid fa-plus" style="margin-right: 6px;"></i> Post Opportunity
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
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

    <!-- SEARCH & FILTER TOOLBAR -->
    <div class="card" style="margin-bottom: 24px; background: white; border: 1px solid var(--border-light); padding: 20px 24px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm);">
        <form action="${pageContext.request.contextPath}${isInternshipsEndpoint ? '/internships' : '/jobs'}" method="get"
              style="display: flex; gap: 14px; flex-wrap: wrap; align-items: flex-end;">

            <!-- Search input -->
            <div class="form-group" style="flex: 2; min-width: 220px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700; color: var(--text-dark); margin-bottom: 5px;">
                    <i class="fa-solid fa-magnifying-glass" style="margin-right: 4px; color: var(--primary-navy);"></i> Search Opportunities
                </label>
                <input type="text" name="search" class="form-control" placeholder="Search by role title, company, location..."
                       value="<c:out value='${searchKeyword}'/>" style="height: 40px; border-radius: var(--radius-sm);">
            </div>

            <!-- Job Type Filter (if not on internships endpoint) -->
            <c:if test="${not isInternshipsEndpoint}">
                <div class="form-group" style="flex: 1; min-width: 150px; margin: 0;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 700; color: var(--text-dark); margin-bottom: 5px;">
                        Opportunity Type
                    </label>
                    <select name="jobType" class="form-control" style="height: 40px; border-radius: var(--radius-sm);">
                        <option value="">All Types</option>
                        <option value="Full Time" ${selectedJobType == 'Full Time' ? 'selected' : ''}>Full Time</option>
                        <option value="Part Time" ${selectedJobType == 'Part Time' ? 'selected' : ''}>Part Time</option>
                        <option value="Internship" ${selectedJobType == 'Internship' ? 'selected' : ''}>Internship</option>
                    </select>
                </div>
            </c:if>

            <!-- Status Filter -->
            <div class="form-group" style="flex: 1; min-width: 140px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700; color: var(--text-dark); margin-bottom: 5px;">
                    Application Status
                </label>
                <select name="status" class="form-control" style="height: 40px; border-radius: var(--radius-sm);">
                    <option value="">All Statuses</option>
                    <option value="Active" ${selectedStatus == 'Active' ? 'selected' : ''}>Active</option>
                    <option value="Closed" ${selectedStatus == 'Closed' ? 'selected' : ''}>Closed</option>
                </select>
            </div>

            <!-- Sort By -->
            <div class="form-group" style="flex: 1; min-width: 140px; margin: 0;">
                <label class="form-label" style="font-size: 0.8rem; font-weight: 700; color: var(--text-dark); margin-bottom: 5px;">
                    Sort Listings
                </label>
                <select name="sortBy" class="form-control" style="height: 40px; border-radius: var(--radius-sm);">
                    <option value="">Sort Order</option>
                    <option value="title" ${selectedSortBy == 'title' ? 'selected' : ''}>Role Title</option>
                    <option value="company" ${selectedSortBy == 'company' ? 'selected' : ''}>Company Name</option>
                    <option value="salary" ${selectedSortBy == 'salary' ? 'selected' : ''}>Package / Stipend</option>
                </select>
            </div>

            <!-- Audience Filter (Coordinators/Admins) -->
            <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                <div class="form-group" style="flex: 1; min-width: 140px; margin: 0;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 700; color: var(--text-dark); margin-bottom: 5px;">
                        Audience
                    </label>
                    <select name="audience" class="form-control" style="height: 40px; border-radius: var(--radius-sm);">
                        <option value="">All Audiences</option>
                        <option value="INTERNAL" ${selectedAudience == 'INTERNAL' ? 'selected' : ''}>CUTM Only</option>
                        <option value="EXTERNAL" ${selectedAudience == 'EXTERNAL' ? 'selected' : ''}>External Only</option>
                        <option value="BOTH" ${selectedAudience == 'BOTH' ? 'selected' : ''}>Both CUTM &amp; External</option>
                    </select>
                </div>
            </c:if>

            <div style="display: flex; gap: 8px; margin: 0;">
                <button type="submit" class="btn btn-primary" style="height: 40px; padding: 0 18px; border-radius: var(--radius-sm); font-weight: 600;">
                    Filter
                </button>
                <a href="${pageContext.request.contextPath}${isInternshipsEndpoint ? '/internships' : '/jobs'}" class="btn btn-outline" style="height: 40px; padding: 0 14px; display: inline-flex; align-items: center; border-radius: var(--radius-sm);">
                    Reset
                </a>
            </div>
        </form>
    </div>

    <!-- OPPORTUNITIES TABLE / CARDS -->
    <div class="card" style="padding: 0; overflow: hidden; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);">
        <div style="padding: 16px 22px; background: white; border-bottom: 1px solid var(--border-light); display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 1.05rem; color: var(--primary-navy); font-weight: 700;">
                Available Listings
            </div>
            <span style="font-size: 0.82rem; background: var(--bg-subtle); color: var(--text-muted); padding: 4px 10px; border-radius: 12px; font-weight: 600;">
                ${not empty jobs ? jobs.size() : 0} listing(s) found
            </span>
        </div>

        <div style="overflow-x: auto;">
            <table class="data-table" style="margin: 0; width: 100%; border-collapse: collapse;">
                <thead>
                    <tr>
                        <th style="padding: 14px 16px; text-align: left;">Role &amp; Opportunity</th>
                        <th style="padding: 14px 16px; text-align: left;">Company</th>
                        <th style="padding: 14px 16px; text-align: left;">Location</th>
                        <th style="padding: 14px 16px; text-align: right;">Package / Stipend</th>
                        <th style="padding: 14px 16px; text-align: center;">Type &amp; Duration</th>
                        <th style="padding: 14px 16px; text-align: center;">Status</th>
                        <th style="padding: 14px 16px; text-align: center;">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty jobs}">
                            <c:forEach items="${jobs}" var="job">
                                <tr style="border-bottom: 1px solid var(--border-light);">
                                    <td style="padding: 14px 16px; text-align: left;">
                                        <a href="${pageContext.request.contextPath}/jobs?action=view&id=${job.id}"
                                           style="font-weight: 700; color: var(--primary-navy); text-decoration: none;">
                                            <c:out value="${job.title}"/>
                                        </a>
                                        <span style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-top: 2px;">Ref: #${job.id}</span>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: left; font-weight: 600; color: var(--text-dark);">
                                        <i class="fa-regular fa-building" style="color: var(--text-muted); margin-right: 6px;"></i><c:out value="${job.company}"/>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: left; color: var(--text-muted); font-size: 0.88rem;">
                                        <i class="fa-solid fa-location-dot" style="color: var(--danger); margin-right: 4px; font-size: 11px;"></i><c:out value="${job.location}"/>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: right; font-weight: 700; color: var(--success); font-size: 0.95rem;">
                                        <c:out value="${job.salary}"/>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center;">
                                        <span class="badge" style="background: rgba(27, 54, 93, 0.08); color: var(--primary-navy); font-weight: 600; font-size: 0.75rem;">
                                            <c:out value="${job.jobType}"/>
                                        </span>
                                        <c:if test="${not empty job.duration}">
                                            <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 3px;">
                                                <i class="fa-regular fa-clock" style="font-size: 10px;"></i> <c:out value="${job.duration}"/>
                                            </div>
                                        </c:if>
                                        <div>
                                            <c:choose>
                                                <c:when test="${job.targetAudience == 'EXTERNAL'}">
                                                    <span class="badge" style="background: #f5f3ff; color: #7c3aed; border: 1px solid #ddd6fe; font-size: 0.68rem; margin-top: 4px; display: inline-block;">
                                                        <i class="fa fa-university"></i> External
                                                    </span>
                                                </c:when>
                                                <c:when test="${job.targetAudience == 'BOTH'}">
                                                    <span class="badge" style="background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; font-size: 0.68rem; margin-top: 4px; display: inline-block;">
                                                        <i class="fa fa-users"></i> All Pools
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: #f8fafc; color: #64748b; border: 1px solid #e2e8f0; font-size: 0.68rem; margin-top: 4px; display: inline-block;">
                                                        <i class="fa fa-graduation-cap"></i> CUTM
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center;">
                                        <c:choose>
                                            <c:when test="${job.status == 'Active'}">
                                                <span class="badge badge-success"><i class="fa-solid fa-circle" style="font-size:7px; vertical-align:middle; margin-right:4px;"></i>Active</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-danger"><i class="fa-solid fa-circle" style="font-size:7px; vertical-align:middle; margin-right:4px;"></i>Closed</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 16px; text-align: center; white-space: nowrap;">
                                        <div style="display: flex; gap: 6px; justify-content: center; align-items: center;">
                                            <a href="${pageContext.request.contextPath}/jobs?action=view&id=${job.id}"
                                               class="btn btn-outline" style="font-size: 0.78rem; padding: 6px 12px; border-radius: var(--radius-sm);" title="View details and requirements">
                                                <i class="fa-solid fa-eye" style="margin-right: 4px;"></i> View
                                            </a>

                                            <c:if test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                                                <a href="${pageContext.request.contextPath}/jobs?action=edit&id=${job.id}"
                                                   class="btn btn-secondary" style="font-size: 0.78rem; padding: 6px 12px; border-radius: var(--radius-sm);" title="Edit listing">
                                                    <i class="fa-solid fa-pen-to-square"></i>
                                                </a>

                                                <form action="${pageContext.request.contextPath}/jobs" method="post" style="display: inline; margin: 0;"
                                                      onsubmit="return (typeof PortalPopups !== 'undefined') ? PortalPopups.confirmAction(event, 'Are you sure you want to delete this listing?') : confirm('Are you sure you want to delete this listing?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="${job.id}">
                                                    <c:if test="${isInternshipsEndpoint}">
                                                        <input type="hidden" name="source" value="internships">
                                                    </c:if>
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <button type="submit" class="btn btn-danger" style="font-size: 0.78rem; padding: 6px 10px; border-radius: var(--radius-sm);" title="Delete listing">
                                                        <i class="fa-solid fa-trash-can"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 48px 20px; color: var(--text-muted);">
                                    <div style="font-size: 2.2rem; margin-bottom: 10px; color: var(--accent-gold);">
                                        <i class="fa-solid fa-briefcase"></i>
                                    </div>
                                    <div style="font-weight: 700; font-size: 1.15rem; color: var(--primary-navy);">No Opportunities Found</div>
                                    <div style="font-size: 0.9rem; margin-top: 6px; max-width: 420px; margin-left: auto; margin-right: auto;">
                                        No <c:out value="${isInternshipsEndpoint ? 'internships' : 'jobs or internships'}"/> match your filter criteria. Try adjusting keywords or clear filters.
                                    </div>
                                    <div style="margin-top: 16px;">
                                        <a href="${pageContext.request.contextPath}${isInternshipsEndpoint ? '/internships' : '/jobs'}" class="btn btn-outline" style="font-size: 0.85rem; padding: 7px 16px;">
                                            Reset All Filters
                                        </a>
                                    </div>
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