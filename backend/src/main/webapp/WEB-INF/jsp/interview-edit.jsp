<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="interviews" scope="request" />
<c:set var="pageTitle" value="Edit / Reschedule Interview | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<c:if test="${not empty sessionScope.userId}">
    <form action="${pageContext.request.contextPath}/logout" method="post" style="display:none;" aria-hidden="true">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</c:if>

<main class="page-container" style="max-width: 680px; margin: 36px auto 60px auto;">

    <!-- Page Header Banner -->
    <div style="background: white; border-radius: var(--radius-lg); padding: 24px 28px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); margin-bottom: 24px; display: flex; align-items: center; gap: 16px;">
        <div style="width: 50px; height: 50px; background: rgba(27, 54, 93, 0.08); border-radius: 12px; display: flex; align-items: center; justify-content: center; color: var(--primary-navy); font-size: 24px;">
            <i class="fa-solid fa-clock-rotate-left"></i>
        </div>
        <div>
            <h1 class="page-title" style="margin: 0 0 4px 0; font-size: 1.55rem; color: var(--primary-navy); font-weight: 800;">
                Edit / Reschedule Interview Session
            </h1>
            <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.9rem;">
                Modify schedule timing, date, or delivery format for Application #${interview.applicationId}.
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

    <div class="card" style="padding: 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); background: white;">
        <form action="${pageContext.request.contextPath}/interviews" method="post" id="editInterviewForm">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="${interview.id}">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <div class="form-group" style="margin-bottom: 20px;">
                <label class="form-label" for="applicationId">Candidate Application Reference</label>
                <input type="text" id="applicationId" class="form-control"
                       value="Application #${interview.applicationId}" readonly
                       style="background-color: var(--bg-subtle); cursor: not-allowed; color: var(--text-dark); font-weight: 600;">
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; margin-bottom: 20px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="interviewDate">Interview Date <span style="color:var(--danger);">*</span></label>
                    <input type="date" id="interviewDate" name="interviewDate" class="form-control"
                           value="${interview.interviewDate}" min="${today}" required>
                </div>

                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="interviewTime">Interview Time <span style="color:var(--danger);">*</span></label>
                    <input type="time" id="interviewTime" name="interviewTime" class="form-control"
                           value="${interview.interviewTime}" required>
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 26px;">
                <label class="form-label" for="mode">Evaluation Delivery Format <span style="color:var(--danger);">*</span></label>
                <select id="mode" name="mode" class="form-control" required>
                    <option value="ONLINE" ${interview.mode == 'ONLINE' ? 'selected' : ''}>Online (Virtual Meeting / Video Link)</option>
                    <option value="OFFLINE" ${interview.mode == 'OFFLINE' ? 'selected' : ''}>Offline (On-Campus Placement Cell)</option>
                </select>
            </div>

            <div style="display: flex; gap: 14px; align-items: center; justify-content: flex-end;">
                <a href="${pageContext.request.contextPath}/interviews" class="btn btn-outline" style="padding: 12px 22px; border-radius: var(--radius-md);">
                    Cancel
                </a>
                <button type="submit" class="btn btn-primary" style="padding: 12px 28px; border-radius: var(--radius-md); font-weight: 700;">
                    <i class="fa-solid fa-floppy-disk" style="margin-right: 6px;"></i> Update Schedule
                </button>
            </div>
        </form>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />