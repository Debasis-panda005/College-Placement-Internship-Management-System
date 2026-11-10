<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="${param.type == 'Internship' || isInternshipsEndpoint ? 'internships' : 'jobs'}" scope="request" />
<c:set var="pageTitle" value="${param.type == 'Internship' || job.jobType == 'Internship' ? 'Post New Internship' : 'Post New Opportunity'} | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 760px; margin: 36px auto 60px auto;">

    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; align-items: center; gap: 16px;">
        <div style="width: 50px; height: 50px; background: rgba(27, 54, 93, 0.08); border-radius: 12px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 24px;">
            <i class="fa-solid fa-file-circle-plus"></i>
        </div>
        <div>
            <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                <c:out value="${param.type == 'Internship' || job.jobType == 'Internship' ? 'Post New Internship Opening' : 'Post Placement Opportunity'}"/>
            </h1>
            <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.9rem;">
                Publish verified recruitment drives or internship positions for eligible student cohorts.
            </p>
        </div>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error" style="margin-bottom: 20px;" role="alert">
            <i class="fa-solid fa-circle-exclamation alert-icon"></i>
            <div class="alert-content">
                <strong>Error:</strong> <c:out value="${errorMessage}"/>
            </div>
        </div>
    </c:if>

    <div class="card" style="padding: 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);">
        <form action="${pageContext.request.contextPath}/jobs" method="post" id="jobForm" onsubmit="return (typeof PortalValidation !== 'undefined' && PortalValidation.validateJob) ? PortalValidation.validateJob(this) : true;">
            <input type="hidden" name="action" value="create">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <c:if test="${param.type == 'Internship' || isInternshipsEndpoint}">
                <input type="hidden" name="source" value="internships">
            </c:if>

            <div class="form-group" style="margin-bottom: 18px;">
                <label class="form-label" for="title">Opportunity Role Title <span style="color:var(--danger);">*</span></label>
                <input type="text" id="title" name="title" class="form-control"
                       value="<c:out value='${job.title}'/>" required placeholder="e.g. Graduate Software Engineer or AI Research Intern">
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 18px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="company">Recruiting Organization <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="company" name="company" class="form-control"
                           value="<c:out value='${job.company}'/>" required placeholder="e.g. Infosys, TCS, Amazon">
                </div>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="location">Drive / Job Location <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="location" name="location" class="form-control"
                           value="<c:out value='${job.location}'/>" required placeholder="e.g. Bangalore, Hyderabad, Remote">
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 18px;">
                <label class="form-label" for="description">Role Description, Responsibilities &amp; Prerequisites</label>
                <textarea id="description" name="description" class="form-control" rows="4"
                          placeholder="Outline job specifications, technology stack, interview expectations..."><c:out value="${job.description}"/></textarea>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 18px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="salary"><c:out value="${param.type == 'Internship' || job.jobType == 'Internship' ? 'Monthly Stipend' : 'Annual CTC / Stipend'}"/></label>
                    <input type="text" id="salary" name="salary" class="form-control"
                           value="<c:out value='${job.salary}'/>" placeholder="e.g. 8.5 LPA or 45,000 / month">
                </div>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="jobType">Engagement Model <span style="color:var(--danger);">*</span></label>
                    <select id="jobType" name="jobType" class="form-control" required>
                        <option value="">Select Engagement Type</option>
                        <option value="Full Time" ${job.jobType == 'Full Time' ? 'selected' : ''}>Full Time</option>
                        <option value="Part Time" ${job.jobType == 'Part Time' ? 'selected' : ''}>Part Time</option>
                        <option value="Internship" ${param.type == 'Internship' || job.jobType == 'Internship' ? 'selected' : ''}>Internship</option>
                    </select>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 18px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="duration">Internship Duration (Optional)</label>
                    <input type="text" id="duration" name="duration" class="form-control"
                           value="<c:out value='${job.duration}'/>" placeholder="e.g. 6 Months, Summer 2026">
                </div>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="deadline">Application Deadline</label>
                    <input type="date" id="deadline" name="deadline" class="form-control"
                           value="<c:out value='${job.deadline}'/>">
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 18px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="minCgpa">Minimum CGPA (0.00 – 10.00)</label>
                    <input type="number" step="0.01" min="0.00" max="10.00" id="minCgpa" name="minCgpa" class="form-control"
                           value="<c:out value='${job.minCgpa}'/>" placeholder="e.g. 7.50">
                </div>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="status">Posting Status <span style="color:var(--danger);">*</span></label>
                    <select id="status" name="status" class="form-control" required>
                        <option value="Active" ${empty job.status || job.status == 'Active' ? 'selected' : ''}>Active (Open for applications)</option>
                        <option value="Closed" ${job.status == 'Closed' ? 'selected' : ''}>Closed (Archived)</option>
                    </select>
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 18px;">
                <label class="form-label" for="targetAudience">Target Candidate Audience <span style="color:var(--danger);">*</span></label>
                <select id="targetAudience" name="targetAudience" class="form-control" required>
                    <option value="INTERNAL" ${empty job.targetAudience || job.targetAudience == 'INTERNAL' ? 'selected' : ''}>Internal CUTM Students Only</option>
                    <option value="EXTERNAL" ${job.targetAudience == 'EXTERNAL' ? 'selected' : ''}>External University Students Only</option>
                    <option value="BOTH" ${job.targetAudience == 'BOTH' ? 'selected' : ''}>Both Internal (CUTM) &amp; External Students</option>
                </select>
                <small style="color: var(--text-muted); font-size: 0.8rem; display: block; margin-top: 4px;">
                    Control which student category can view and apply for this opportunity.
                </small>
            </div>

            <div class="form-group" style="margin-bottom: 26px;">
                <label class="form-label" for="eligibleDepartments">Eligible Branches / Departments</label>
                <input type="text" id="eligibleDepartments" name="eligibleDepartments" class="form-control"
                       value="<c:out value='${job.eligibleDepartments}'/>" placeholder="e.g. CSE,ECE,IT,MECH (or leave blank for all)">
            </div>

            <div style="display: flex; gap: 14px; align-items: center; justify-content: flex-end;">
                <a href="${pageContext.request.contextPath}${param.type == 'Internship' || isInternshipsEndpoint ? '/internships' : '/jobs'}" class="btn btn-outline" style="padding: 12px 22px; border-radius: var(--radius-md);">
                    Cancel
                </a>
                <button type="submit" class="btn btn-primary" style="padding: 12px 28px; border-radius: var(--radius-md); font-weight: 700;">
                    <i class="fa-solid fa-cloud-arrow-up" style="margin-right: 6px;"></i> <c:out value="${param.type == 'Internship' || job.jobType == 'Internship' ? 'Publish Internship Drive' : 'Publish Opportunity'}"/>
                </button>
            </div>
        </form>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />