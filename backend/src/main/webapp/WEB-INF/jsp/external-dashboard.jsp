<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="common/header.jsp">
    <jsp:param name="pageTitle" value="External Candidate Portal - Centurion Placement & Internship System"/>
    <jsp:param name="activeNav" value="dashboard"/>
</jsp:include>

<main class="dashboard-wrapper">
    <div class="portal-container" style="max-width: 1200px; margin: 30px auto; padding: 0 20px;">

        <!-- Success/Error Notifications -->
        <c:if test="${not empty sessionScope.flashSuccess or not empty successMessage}">
            <div class="alert alert-success" style="background:#dcfce7; color:#166534; border:1px solid #86efac; border-radius:8px; padding:14px 18px; margin-bottom:24px; display:flex; align-items:center; gap:12px;">
                <i class="fa fa-check-circle" style="font-size: 1.25rem;"></i>
                <span><c:out value="${not empty sessionScope.flashSuccess ? sessionScope.flashSuccess : successMessage}"/></span>
            </div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>

        <c:if test="${not empty sessionScope.flashError or not empty errorMessage}">
            <div class="alert alert-danger" style="background:#fee2e2; color:#991b1b; border:1px solid #fca5a5; border-radius:8px; padding:14px 18px; margin-bottom:24px; display:flex; align-items:center; gap:12px;">
                <i class="fa fa-exclamation-triangle" style="font-size: 1.25rem;"></i>
                <span><c:out value="${not empty sessionScope.flashError ? sessionScope.flashError : errorMessage}"/></span>
            </div>
            <c:remove var="flashError" scope="session"/>
        </c:if>

        <!-- Candidate Profile Header Card -->
        <div class="profile-hero-card" style="background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #ffffff; border-radius: 12px; padding: 28px 32px; margin-bottom: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.12);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 20px;">
                <div>
                    <div style="display:flex; align-items:center; gap:12px; margin-bottom: 8px;">
                        <h1 style="font-size: 1.75rem; font-weight: 700; margin: 0; color: #ffffff;">
                            <c:out value="${student.applicantName}"/>
                        </h1>
                        <span class="badge" style="background: #10b981; color: #ffffff; font-weight: 600; padding: 4px 12px; border-radius: 9999px; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.5px;">
                            <i class="fa fa-check-circle"></i> Verified Candidate
                        </span>
                        <span class="badge" style="background: #f59e0b; color: #1e293b; font-weight: 700; padding: 4px 10px; border-radius: 9999px; font-size: 0.8rem;">
                            <i class="fa fa-university"></i> External
                        </span>
                    </div>
                    <p style="font-size: 1rem; color: #cbd5e1; margin: 0 0 16px 0;">
                        <i class="fa fa-graduation-cap"></i> <c:out value="${student.collegeName}"/> &bull; <c:out value="${student.collegeLocation}"/>
                    </p>

                    <div style="display: flex; flex-wrap: wrap; gap: 24px; font-size: 0.95rem; color: #e2e8f0;">
                        <div>
                            <span style="color: #94a3b8; font-size: 0.85rem; display: block;">Degree &amp; Department</span>
                            <strong><c:out value="${student.degree}"/> (<c:out value="${student.department}"/>)</strong>
                        </div>
                        <div>
                            <span style="color: #94a3b8; font-size: 0.85rem; display: block;">Current Semester</span>
                            <strong>Semester <c:out value="${student.currentSemester}"/></strong>
                        </div>
                        <div>
                            <span style="color: #94a3b8; font-size: 0.85rem; display: block;">Cumulative CGPA</span>
                            <strong style="color: #fbbf24;"><c:out value="${student.cgpa}"/> / 10.0</strong>
                        </div>
                        <div>
                            <span style="color: #94a3b8; font-size: 0.85rem; display: block;">Graduation Year</span>
                            <strong><c:out value="${student.graduationYear}"/></strong>
                        </div>
                        <div>
                            <span style="color: #94a3b8; font-size: 0.85rem; display: block;">Contact Phone</span>
                            <strong><c:out value="${student.phone}"/></strong>
                        </div>
                    </div>
                </div>

                <div style="display: flex; flex-direction: column; gap: 10px; align-items: flex-end;">
                    <button type="button" class="btn btn-warning" onclick="openProfileEditModal()" style="background: #f59e0b; color: #0f172a; border: none; font-weight: 600; padding: 9px 18px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                        <i class="fa fa-user-edit"></i> Update Contact Info
                    </button>
                    <c:if test="${not empty student.resumePath}">
                        <a href="${pageContext.request.contextPath}/resume/download?type=external&id=${student.id}" target="_blank" class="btn btn-outline-light" style="background: rgba(255,255,255,0.15); color: #ffffff; border: 1px solid rgba(255,255,255,0.4); font-size: 0.85rem; padding: 7px 14px; border-radius: 6px; text-decoration: none; display: flex; align-items: center; gap: 6px;">
                            <i class="fa fa-file-pdf"></i> View Uploaded Resume
                        </a>
                    </c:if>
                </div>
            </div>
        </div>

        <!-- Metric KPI Cards Grid -->
        <h2 style="font-size: 1.3rem; font-weight: 700; color: #1e3a8a; margin-bottom: 18px; display: flex; align-items: center; gap: 8px;">
            <i class="fa fa-chart-pie" style="color: #d97706;"></i> Recruitment &amp; Placement Overview
        </h2>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 18px; margin-bottom: 35px;">
            <!-- Available Internships -->
            <a href="${pageContext.request.contextPath}/jobs" style="text-decoration:none; color:inherit;">
                <div class="metric-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-top: 4px solid #3b82f6; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase;">Internships</span>
                        <div style="width: 36px; height: 36px; border-radius: 50%; background: #eff6ff; color: #3b82f6; display: flex; align-items: center; justify-content: center;">
                            <i class="fa fa-graduation-cap"></i>
                        </div>
                    </div>
                    <div style="font-size: 1.85rem; font-weight: 800; color: #0f172a;"><c:out value="${metrics.availableInternships != null ? metrics.availableInternships : (stats.availableInternships != null ? stats.availableInternships : 0)}"/></div>
                    <span style="font-size: 0.8rem; color: #3b82f6; font-weight: 500;">Open for External &rarr;</span>
                </div>
            </a>

            <!-- Available Jobs -->
            <a href="${pageContext.request.contextPath}/jobs" style="text-decoration:none; color:inherit;">
                <div class="metric-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-top: 4px solid #0284c7; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase;">Full-Time Roles</span>
                        <div style="width: 36px; height: 36px; border-radius: 50%; background: #f0f9ff; color: #0284c7; display: flex; align-items: center; justify-content: center;">
                            <i class="fa fa-briefcase"></i>
                        </div>
                    </div>
                    <div style="font-size: 1.85rem; font-weight: 800; color: #0f172a;"><c:out value="${metrics.availableJobs != null ? metrics.availableJobs : (stats.availableJobs != null ? stats.availableJobs : 0)}"/></div>
                    <span style="font-size: 0.8rem; color: #0284c7; font-weight: 500;">Explore Openings &rarr;</span>
                </div>
            </a>

            <!-- Total Applications -->
            <a href="${pageContext.request.contextPath}/applications" style="text-decoration:none; color:inherit;">
                <div class="metric-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-top: 4px solid #8b5cf6; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase;">Applied</span>
                        <div style="width: 36px; height: 36px; border-radius: 50%; background: #f5f3ff; color: #8b5cf6; display: flex; align-items: center; justify-content: center;">
                            <i class="fa fa-paper-plane"></i>
                        </div>
                    </div>
                    <div style="font-size: 1.85rem; font-weight: 800; color: #0f172a;"><c:out value="${metrics.totalApplications != null ? metrics.totalApplications : (stats.totalApplications != null ? stats.totalApplications : 0)}"/></div>
                    <span style="font-size: 0.8rem; color: #8b5cf6; font-weight: 500;">View Status &rarr;</span>
                </div>
            </a>

            <!-- Shortlisted Applications -->
            <a href="${pageContext.request.contextPath}/applications" style="text-decoration:none; color:inherit;">
                <div class="metric-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-top: 4px solid #f59e0b; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase;">Shortlisted</span>
                        <div style="width: 36px; height: 36px; border-radius: 50%; background: #fffbeb; color: #f59e0b; display: flex; align-items: center; justify-content: center;">
                            <i class="fa fa-star"></i>
                        </div>
                    </div>
                    <div style="font-size: 1.85rem; font-weight: 800; color: #0f172a;"><c:out value="${metrics.shortlistedApplications != null ? metrics.shortlistedApplications : (stats.shortlistedApplications != null ? stats.shortlistedApplications : 0)}"/></div>
                    <span style="font-size: 0.8rem; color: #f59e0b; font-weight: 500;">In Progress &rarr;</span>
                </div>
            </a>

            <!-- Upcoming Interviews -->
            <a href="${pageContext.request.contextPath}/interviews" style="text-decoration:none; color:inherit;">
                <div class="metric-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-top: 4px solid #06b6d4; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase;">Interviews</span>
                        <div style="width: 36px; height: 36px; border-radius: 50%; background: #ecfeff; color: #06b6d4; display: flex; align-items: center; justify-content: center;">
                            <i class="fa fa-calendar-check"></i>
                        </div>
                    </div>
                    <div style="font-size: 1.85rem; font-weight: 800; color: #0f172a;"><c:out value="${metrics.upcomingInterviews != null ? metrics.upcomingInterviews : (stats.upcomingInterviews != null ? stats.upcomingInterviews : 0)}"/></div>
                    <span style="font-size: 0.8rem; color: #06b6d4; font-weight: 500;">View Schedules &rarr;</span>
                </div>
            </a>

            <!-- Selected Applications -->
            <a href="${pageContext.request.contextPath}/applications" style="text-decoration:none; color:inherit;">
                <div class="metric-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-top: 4px solid #10b981; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase;">Offers / Selected</span>
                        <div style="width: 36px; height: 36px; border-radius: 50%; background: #ecfdf5; color: #10b981; display: flex; align-items: center; justify-content: center;">
                            <i class="fa fa-award"></i>
                        </div>
                    </div>
                    <div style="font-size: 1.85rem; font-weight: 800; color: #0f172a;"><c:out value="${metrics.selectedApplications != null ? metrics.selectedApplications : (stats.selectedApplications != null ? stats.selectedApplications : 0)}"/></div>
                    <span style="font-size: 0.8rem; color: #10b981; font-weight: 500;">Successful &rarr;</span>
                </div>
            </a>
        </div>

        <!-- Quick Actions and Guidance -->
        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px; margin-bottom: 40px;">
            <!-- Guidance Card -->
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
                <h3 style="font-size: 1.15rem; font-weight: 700; color: #1e3a8a; margin-top: 0; margin-bottom: 14px; display: flex; align-items: center; gap: 8px;">
                    <i class="fa fa-info-circle" style="color: #2563eb;"></i> Candidate Guidelines &amp; Protocol
                </h3>
                <ul style="color: #475569; font-size: 0.95rem; line-height: 1.7; padding-left: 20px; margin: 0;">
                    <li><strong>Eligibility:</strong> You are authorized to apply for recruitment drives and internships open to external university candidates.</li>
                    <li><strong>Academic Verification:</strong> Your submitted CGPA, Degree, and University records have been verified by the placement administration. Any changes require administrative re-verification.</li>
                    <li><strong>Interview Attendance:</strong> If invited for online or offline rounds, ensure timely attendance. Details and round links will appear in your <em>My Interviews</em> section.</li>
                    <li><strong>Communication:</strong> Official placement drives will correspond via your registered email address (<c:out value="${student.applicantEmail}"/>).</li>
                </ul>
            </div>

            <!-- Quick Action Shortcuts -->
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; flex-direction: column; justify-content: space-between;">
                <h3 style="font-size: 1.15rem; font-weight: 700; color: #1e3a8a; margin-top: 0; margin-bottom: 14px; display: flex; align-items: center; gap: 8px;">
                    <i class="fa fa-bolt" style="color: #d97706;"></i> Quick Actions
                </h3>
                <div style="display: flex; flex-direction: column; gap: 10px;">
                    <a href="${pageContext.request.contextPath}/jobs" class="btn" style="background: #1e3a8a; color: #ffffff; text-decoration: none; padding: 10px 14px; border-radius: 6px; font-weight: 600; font-size: 0.9rem; text-align: center; display: block;">
                        <i class="fa fa-search"></i> Browse All Openings
                    </a>
                    <a href="${pageContext.request.contextPath}/applications" class="btn" style="background: #f8fafc; color: #1e3a8a; border: 1px solid #cbd5e1; text-decoration: none; padding: 10px 14px; border-radius: 6px; font-weight: 600; font-size: 0.9rem; text-align: center; display: block;">
                        <i class="fa fa-list-check"></i> Check Application Status
                    </a>
                    <a href="${pageContext.request.contextPath}/interviews" class="btn" style="background: #f8fafc; color: #1e3a8a; border: 1px solid #cbd5e1; text-decoration: none; padding: 10px 14px; border-radius: 6px; font-weight: 600; font-size: 0.9rem; text-align: center; display: block;">
                        <i class="fa fa-calendar-alt"></i> View Interview Schedule
                    </a>
                </div>
            </div>
        </div>

    </div>
</main>

<!-- Profile Edit Modal -->
<div id="profileEditModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #ffffff; border-radius: 12px; width: 100%; max-width: 500px; padding: 28px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); margin: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px;">
            <h3 style="margin: 0; color: #1e3a8a; font-weight: 700; font-size: 1.25rem;">
                <i class="fa fa-user-edit"></i> Update Contact &amp; Semester
            </h3>
            <button type="button" onclick="closeProfileEditModal()" style="background: transparent; border: none; font-size: 1.25rem; color: #64748b; cursor: pointer;">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/external/dashboard" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
            <input type="hidden" name="action" value="updateProfile" />

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 6px;">
                    Mobile Phone Number <span style="color:#dc2626;">*</span>
                </label>
                <input type="tel" name="phone" value="<c:out value='${student.phone}'/>" required class="form-control" style="width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box;" />
            </div>

            <div style="margin-bottom: 20px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 6px;">
                    Current Semester <span style="color:#dc2626;">*</span>
                </label>
                <input type="number" name="currentSemester" value="<c:out value='${student.currentSemester}'/>" min="1" max="12" required class="form-control" style="width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box;" />
            </div>

            <div style="background: #f8fafc; border-left: 4px solid #f59e0b; padding: 12px; border-radius: 4px; margin-bottom: 20px; font-size: 0.85rem; color: #64748b;">
                <i class="fa fa-lock"></i> Note: University name, Degree, CGPA, and Graduation Year are locked for verification integrity. To update those, contact the placement cell.
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px;">
                <button type="button" onclick="closeProfileEditModal()" style="background: #e2e8f0; color: #475569; border: none; padding: 9px 18px; border-radius: 6px; font-weight: 600; cursor: pointer;">Cancel</button>
                <button type="submit" style="background: #1e3a8a; color: #ffffff; border: none; padding: 9px 20px; border-radius: 6px; font-weight: 600; cursor: pointer;">Save Changes</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openProfileEditModal() {
        var modal = document.getElementById('profileEditModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }
    function closeProfileEditModal() {
        var modal = document.getElementById('profileEditModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }
    // Close when clicking outside modal box
    window.addEventListener('click', function(event) {
        var modal = document.getElementById('profileEditModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
</script>

<jsp:include page="common/footer.jsp"/>
