<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="login" scope="request" />
<c:set var="pageTitle" value="${param.role == 'coordinator' ? 'Coordinator Sign In | Centurion University Placement Portal' : 'Sign In | Centurion University Placement Portal'}" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page-container" style="max-width: 480px; margin: 48px auto 60px auto;">

    <div class="card" style="padding: 36px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); border: 1px solid var(--border-light);">
        <div style="text-align: center; margin-bottom: 24px;">
            <img src="${pageContext.request.contextPath}/assets/images/college-logo.png" alt="Centurion University Logo" style="width: 64px; height: 64px; object-fit: contain; border-radius: 50%; box-shadow: 0 2px 8px rgba(0,0,0,0.12); margin-bottom: 12px;"/>
            <h1 class="page-title" style="font-size: 1.45rem; margin: 0 0 4px 0; color: var(--primary-navy); font-weight: 800;">Centurion University</h1>
            <div style="font-size: 0.82rem; font-weight: 700; color: var(--accent-gold); letter-spacing: 0.05em; text-transform: uppercase; margin-bottom: 6px;">Training &amp; Placement Cell</div>
            <c:choose>
                <c:when test="${param.role == 'coordinator'}">
                    <div style="display: inline-flex; align-items: center; gap: 6px; background: #0f243e; color: #ffffff; border: 1px solid var(--accent-gold); border-radius: 20px; padding: 4px 14px; font-size: 0.78rem; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 10px;">
                        <i class="fa-solid fa-user-shield" style="margin-right: 4px; color: var(--accent-gold);"></i> COORDINATOR PORTAL
                    </div>
                    <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                        Authorized sign-in for placement coordinators and staff administrators
                    </p>
                </c:when>
                <c:otherwise>
                    <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.88rem;">
                        Sign in to access candidate applications, recruitment drives, and interview reports
                    </p>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${not empty sessionScope.userId && sessionScope.userRole == 'STUDENT' && param.role == 'coordinator'}">
            <div class="alert alert-warning" style="margin-bottom: 20px;" role="alert">
                <i class="fa-solid fa-triangle-exclamation alert-icon"></i>
                <div class="alert-content">
                    <strong>Access Restriction Notice:</strong> You are currently signed in as a student (<strong><c:out value="${sessionScope.userName}"/></strong>). Coordinator operations require authorized Coordinator credentials.
                </div>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" style="margin-bottom: 20px;" role="alert">
                <i class="fa-solid fa-circle-exclamation alert-icon"></i>
                <div class="alert-content">
                    <strong>Authentication Notice:</strong> <c:out value="${errorMessage}"/>
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

        <form action="${pageContext.request.contextPath}/login" method="post" id="loginForm" onsubmit="return validateLoginForm();">
            <!-- CSRF Protection Token -->
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <div class="form-group" style="margin-bottom: 20px;">
                <label class="form-label" for="email">
                    <i class="fa-solid fa-envelope" style="color: var(--primary-navy); margin-right: 6px;"></i> Institutional Email <span style="color:var(--danger);">*</span>
                </label>
                <input type="email" id="email" name="email" class="form-control"
                       value="<c:out value='${email}'/>" required autofocus placeholder="e.g. student@college.edu or staff@college.edu"
                       autocomplete="email">
            </div>

            <div class="form-group" style="margin-bottom: 24px;">
                <label class="form-label" for="password">
                    <i class="fa-solid fa-lock" style="color: var(--primary-navy); margin-right: 6px;"></i> Password <span style="color:var(--danger);">*</span>
                </label>
                <div class="password-toggle-wrapper">
                    <input type="password" id="password" name="password" class="form-control"
                           required placeholder="Enter your password" autocomplete="current-password">
                    <button type="button" id="loginPassToggle" class="password-toggle-btn"
                            onclick="togglePasswordVisibility('password', 'loginPassToggle')"
                            aria-label="Toggle password visibility" title="Show or hide password">
                        <i class="fa-solid fa-eye"></i>
                    </button>
                </div>
            </div>

            <c:choose>
                <c:when test="${param.role == 'coordinator'}">
                    <button type="submit" class="btn btn-primary" style="width: 100%; padding: 13px; font-size: 1rem; font-weight: 700; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); background: #0f243e; border: 1.5px solid var(--accent-gold); color: #ffffff;">
                        <i class="fa-solid fa-user-shield" style="margin-right: 8px; color: var(--accent-gold);"></i> Sign In as Coordinator
                    </button>
                </c:when>
                <c:otherwise>
                    <button type="submit" class="btn btn-primary" style="width: 100%; padding: 13px; font-size: 1rem; font-weight: 700; border-radius: var(--radius-md); box-shadow: var(--shadow-sm);">
                        <i class="fa-solid fa-right-to-bracket" style="margin-right: 8px;"></i> Sign In to Portal
                    </button>
                </c:otherwise>
            </c:choose>
        </form>

        <div style="text-align: center; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--border-light); font-size: 0.9rem; color: var(--text-muted);">
            <c:choose>
                <c:when test="${param.role == 'coordinator'}">
                    <div style="background: rgba(15, 36, 62, 0.04); border: 1px solid rgba(230, 154, 24, 0.35); border-left: 4px solid var(--accent-gold); border-radius: var(--radius-md); padding: 14px 16px; margin-bottom: 16px; text-align: left;">
                        <div style="font-weight: 700; color: var(--primary-navy); font-size: 0.95rem; margin-bottom: 4px; display: flex; align-items: center; gap: 6px;">
                            <i class="fa-solid fa-user-tie" style="color: var(--accent-gold);"></i> New Coordinator?
                        </div>
                        <div style="font-size: 0.88rem; color: var(--text-muted); line-height: 1.4;">
                            Don't have a coordinator account?
                            <a href="${pageContext.request.contextPath}/coordinator/register" style="color: var(--primary-navy); font-weight: 700; text-decoration: underline; margin-left: 4px;">
                                Register Here <i class="fa-solid fa-arrow-right" style="font-size: 0.75rem; margin-left: 2px;"></i>
                            </a>
                        </div>
                    </div>
                    <div>
                        Looking for student access? <a href="${pageContext.request.contextPath}/login" style="color: var(--primary-navy); font-weight: 700; text-decoration: none;"><i class="fa-solid fa-right-to-bracket" style="margin-right: 4px;"></i> Student Sign In &rarr;</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="margin-bottom: 6px;">
                        New student applicant?
                        <a href="${pageContext.request.contextPath}/register" style="color: var(--primary-navy); font-weight: 700; text-decoration: none;"><i class="fa-solid fa-user-graduate" style="margin-right: 4px;"></i> CUTM Student Register &rarr;</a>
                        <span style="margin: 0 8px; color: var(--border-light);">|</span>
                        <a href="${pageContext.request.contextPath}/external/register" style="color: var(--accent-gold); font-weight: 700; text-decoration: none;"><i class="fa-solid fa-university" style="margin-right: 4px;"></i> External Student Register &rarr;</a>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/login?role=coordinator" style="color: var(--text-muted); font-weight: 600; text-decoration: none;"><i class="fa-solid fa-user-shield" style="margin-right: 4px;"></i> Coordinator Login</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</main>

<script>
    function validateLoginForm() {
        var email = document.getElementById("email").value.trim();
        var password = document.getElementById("password").value;
        if (!email || !password) {
            if (typeof PortalPopups !== 'undefined' && PortalPopups.showToast) {
                PortalPopups.showToast("Please enter both email and password.", "error");
            } else {
                alert("Please enter both email and password.");
            }
            return false;
        }
        return true;
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
