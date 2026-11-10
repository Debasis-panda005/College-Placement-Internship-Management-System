<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="dashboard" scope="request" />
<c:set var="pageTitle" value="External Student Details | Admin Dashboard" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page-container" style="max-width: 900px; margin: 30px auto 60px auto;">

    <div style="margin-bottom: 20px;">
        <a href="${pageContext.request.contextPath}/admin/dashboard#external-students" class="btn btn-outline" style="padding: 6px 14px; font-size: 0.88rem;">
            <i class="fa-solid fa-arrow-left" style="margin-right: 6px;"></i> Back to Dashboard
        </a>
    </div>

    <div class="card" style="padding: 30px; border-radius: var(--radius-lg); box-shadow: var(--shadow-md); border: 1px solid var(--border-light); background: #ffffff;">
        
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 15px; border-bottom: 1px solid var(--border-light); padding-bottom: 20px; margin-bottom: 24px;">
            <div>
                <span class="badge" style="background: rgba(230, 154, 24, 0.15); color: var(--accent-gold); font-weight: 700; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; padding: 4px 10px; border-radius: var(--radius-sm); margin-bottom: 8px; display: inline-block;">
                    External Student Candidate
                </span>
                <h1 style="font-size: 1.6rem; margin: 0; color: var(--primary-navy); font-weight: 800;">
                    <c:out value="${student.applicantName}"/>
                </h1>
                <div style="font-size: 0.9rem; color: var(--text-muted); margin-top: 4px;">
                    <i class="fa-solid fa-university" style="margin-right: 4px; color: var(--accent-gold);"></i>
                    <strong><c:out value="${student.collegeName}"/></strong> &bull; <c:out value="${student.collegeLocation}"/>
                </div>
            </div>

            <div>
                <c:choose>
                    <c:when test="${student.verificationStatus == 'APPROVED'}">
                        <span class="status-badge badge-passed" style="font-size: 0.95rem; padding: 6px 14px;">
                            <i class="fa-solid fa-circle-check" style="margin-right: 5px;"></i> Approved
                        </span>
                    </c:when>
                    <c:when test="${student.verificationStatus == 'REJECTED'}">
                        <span class="status-badge badge-failed" style="font-size: 0.95rem; padding: 6px 14px;">
                            <i class="fa-solid fa-circle-xmark" style="margin-right: 5px;"></i> Rejected
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="status-badge badge-scheduled" style="font-size: 0.95rem; padding: 6px 14px;">
                            <i class="fa-solid fa-clock" style="margin-right: 5px;"></i> Pending Verification
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Student Academic & Profile Details Grid -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 20px; margin-bottom: 30px;">
            <div style="background: var(--bg-subtle); padding: 18px; border-radius: var(--radius-md);">
                <div style="font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Degree &amp; Department</div>
                <div style="font-size: 1.05rem; font-weight: 700; color: var(--primary-navy); margin-top: 4px;">
                    <c:out value="${student.degree}"/> in <c:out value="${student.department}"/>
                </div>
            </div>

            <div style="background: var(--bg-subtle); padding: 18px; border-radius: var(--radius-md);">
                <div style="font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Academic Performance</div>
                <div style="font-size: 1.05rem; font-weight: 700; color: var(--primary-navy); margin-top: 4px;">
                    CGPA: <c:out value="${student.cgpa}"/> &bull; Semester: <c:out value="${student.currentSemester}"/>
                </div>
            </div>

            <div style="background: var(--bg-subtle); padding: 18px; border-radius: var(--radius-md);">
                <div style="font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Graduation Year</div>
                <div style="font-size: 1.05rem; font-weight: 700; color: var(--primary-navy); margin-top: 4px;">
                    Batch of <c:out value="${student.graduationYear}"/>
                </div>
            </div>

            <div style="background: var(--bg-subtle); padding: 18px; border-radius: var(--radius-md);">
                <div style="font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Contact Information</div>
                <div style="font-size: 0.95rem; font-weight: 600; color: var(--primary-navy); margin-top: 4px;">
                    <div><i class="fa-solid fa-envelope" style="margin-right: 6px; color: var(--text-muted);"></i> <c:out value="${student.applicantEmail}"/></div>
                    <div style="margin-top: 3px;"><i class="fa-solid fa-phone" style="margin-right: 6px; color: var(--text-muted);"></i> <c:out value="${student.phone}"/></div>
                </div>
            </div>
        </div>

        <!-- Resume Download Section -->
        <div style="background: #f8fafc; border: 1px solid var(--border-light); padding: 20px; border-radius: var(--radius-md); margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px;">
            <div>
                <h4 style="margin: 0 0 4px 0; color: var(--primary-navy); font-size: 1.05rem;">
                    <i class="fa-solid fa-file-pdf" style="color: var(--danger); margin-right: 6px;"></i> Applicant Resume Document
                </h4>
                <div style="font-size: 0.85rem; color: var(--text-muted);">
                    Uploaded during registration for academic qualification verification.
                </div>
            </div>
            <div>
                <c:choose>
                    <c:when test="${not empty student.resumeUrl}">
                        <a href="${pageContext.request.contextPath}${student.resumeUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-outline" style="padding: 8px 16px; font-weight: 700;">
                            <i class="fa-solid fa-download" style="margin-right: 6px;"></i> View / Download Resume
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span style="color: var(--text-muted); font-size: 0.88rem; font-style: italic;">No resume file attached</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Approval / Rejection Actions for Admins if PENDING -->
        <c:if test="${sessionScope.userRole == 'ADMIN' && student.verificationStatus == 'PENDING'}">
            <div style="border-top: 1px solid var(--border-light); padding-top: 24px; margin-bottom: 30px;">
                <h3 style="font-size: 1.1rem; color: var(--primary-navy); margin: 0 0 16px 0;">
                    <i class="fa-solid fa-user-shield" style="margin-right: 6px;"></i> Administrator Decision Controls
                </h3>
                <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                    <form action="${pageContext.request.contextPath}/admin/external-students" method="post" style="display: inline;" onsubmit="return confirm('Are you sure you want to APPROVE this external student registration?');">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
                        <input type="hidden" name="action" value="approve"/>
                        <input type="hidden" name="id" value="${student.id}"/>
                        <button type="submit" class="btn btn-primary" style="background: var(--success); border-color: var(--success); padding: 10px 20px; font-weight: 700;">
                            <i class="fa-solid fa-check" style="margin-right: 6px;"></i> Approve External Student
                        </button>
                    </form>

                    <button type="button" class="btn btn-primary" style="background: var(--danger); border-color: var(--danger); padding: 10px 20px; font-weight: 700;" onclick="document.getElementById('rejectModal').style.display='flex';">
                        <i class="fa-solid fa-xmark" style="margin-right: 6px;"></i> Reject Registration
                    </button>
                </div>
            </div>
        </c:if>

        <!-- Audit History Log -->
        <div style="border-top: 1px solid var(--border-light); padding-top: 24px;">
            <h3 style="font-size: 1.1rem; color: var(--primary-navy); margin: 0 0 16px 0;">
                <i class="fa-solid fa-clock-rotate-left" style="margin-right: 6px;"></i> Lifecycle &amp; Verification Audit Trail
            </h3>

            <c:choose>
                <c:when test="${not empty auditLogs}">
                    <div class="table-responsive-wrapper">
                        <table class="portal-table">
                            <thead>
                                <tr>
                                    <th>Timestamp</th>
                                    <th>Action</th>
                                    <th>Auditor</th>
                                    <th>Previous Status</th>
                                    <th>New Status</th>
                                    <th>Remarks / Reason</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="log" items="${auditLogs}">
                                    <tr>
                                        <td><c:out value="${log.actionTimestamp}"/></td>
                                        <td><strong><c:out value="${log.action}"/></strong></td>
                                        <td><c:out value="${not empty log.performedByName ? log.performedByName : 'Administrator'}"/></td>
                                        <td><span class="badge"><c:out value="${log.previousStatus}"/></span></td>
                                        <td><span class="badge"><c:out value="${log.newStatus}"/></span></td>
                                        <td><c:out value="${log.remarks}"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="font-size: 0.9rem; color: var(--text-muted); font-style: italic;">
                        Registration registered on <c:out value="${student.createdAt}"/>.
                        <c:if test="${not empty student.adminNotes}">
                            <div style="margin-top: 4px;"><strong>Administrative Notes:</strong> <c:out value="${student.adminNotes}"/></div>
                        </c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

    </div>

</main>

<!-- Rejection Reason Modal -->
<div id="rejectModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 9999; justify-content: center; align-items: center;">
    <div style="background: #ffffff; padding: 28px; border-radius: var(--radius-md); max-width: 500px; width: 90%; box-shadow: var(--shadow-lg);">
        <h3 style="margin-top: 0; color: var(--danger); font-size: 1.25rem;">
            <i class="fa-solid fa-triangle-exclamation"></i> Reject Registration Request
        </h3>
        <p style="color: var(--text-muted); font-size: 0.9rem;">
            Please provide a specific administrative reason for rejecting the registration of <strong><c:out value="${student.applicantName}"/></strong>:
        </p>
        <form action="${pageContext.request.contextPath}/admin/external-students" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
            <input type="hidden" name="action" value="reject"/>
            <input type="hidden" name="id" value="${student.id}"/>
            <div class="form-group" style="margin-bottom: 20px;">
                <label class="form-label" for="adminNotes">Rejection Reason <span style="color:var(--danger);">*</span></label>
                <textarea id="adminNotes" name="adminNotes" class="form-control" rows="3" required placeholder="e.g. Ineligible degree program / incomplete resume verification"></textarea>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" class="btn btn-outline" onclick="document.getElementById('rejectModal').style.display='none';">Cancel</button>
                <button type="submit" class="btn btn-primary" style="background: var(--danger); border-color: var(--danger);">Confirm Rejection</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
