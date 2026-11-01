<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="register" scope="request" />
<c:set var="pageTitle" value="Student Registration | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page-container" style="max-width: 760px; margin: 40px auto 60px auto;">

    <div class="card" style="padding: 36px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); border: 1px solid var(--border-light);">
        <div style="text-align: center; margin-bottom: 24px;">
            <img src="${pageContext.request.contextPath}/assets/images/college-logo.png" alt="Centurion University Logo" style="width: 64px; height: 64px; object-fit: contain; border-radius: 50%; box-shadow: 0 2px 8px rgba(0,0,0,0.12); margin-bottom: 12px;"/>
            <h1 class="page-title" style="font-size: 1.55rem; margin: 0 0 4px 0; color: var(--primary-navy); font-weight: 800;">Student Applicant Registration</h1>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--accent-gold); letter-spacing: 0.05em; text-transform: uppercase; margin-bottom: 6px;">Centurion University of Technology and Management</div>
            <p class="page-subtitle" style="margin: 0; color: var(--text-muted); font-size: 0.9rem;">
                Create your verified applicant account to access campus recruitment drives, internships, and dynamic interviews.
            </p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" style="margin-bottom: 20px;" role="alert">
                <i class="fa-solid fa-circle-exclamation alert-icon"></i>
                <div class="alert-content">
                    <strong>Registration Notice:</strong> <c:out value="${errorMessage}"/>
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

        <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm" onsubmit="return validateRegisterForm();">
            <!-- CSRF Protection Token -->
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <!-- Section 1 -->
            <div style="background: var(--bg-subtle); padding: 18px 20px; border-radius: var(--radius-md); border-left: 4px solid var(--primary-navy); margin-bottom: 20px;">
                <h3 style="font-size: 1.05rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-id-card"></i> 1. Account &amp; Identity Credentials
                </h3>
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="name">Full Name <span style="color:var(--danger);">*</span></label>
                <input type="text" id="name" name="name" class="form-control"
                       value="<c:out value='${name}'/>" required placeholder="e.g. Ananya Sharma" autocomplete="name">
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="email">Institutional Email Address <span style="color:var(--danger);">*</span></label>
                <input type="email" id="email" name="email" class="form-control"
                       value="<c:out value='${email}'/>" required placeholder="e.g. ananya.s@college.edu" autocomplete="email">
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 24px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="password">Password <span style="color:var(--danger);">*</span> (min. 8 chars)</label>
                    <div class="password-toggle-wrapper">
                        <input type="password" id="password" name="password" minlength="8" class="form-control"
                               required placeholder="Create secure password" autocomplete="new-password">
                        <button type="button" id="regPassToggle" class="password-toggle-btn"
                                onclick="togglePasswordVisibility('password', 'regPassToggle')"
                                aria-label="Toggle password visibility" title="Show/hide password">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="confirmPassword">Confirm Password <span style="color:var(--danger);">*</span></label>
                    <div class="password-toggle-wrapper">
                        <input type="password" id="confirmPassword" name="confirmPassword" minlength="8" class="form-control"
                               required placeholder="Repeat password" autocomplete="new-password">
                        <button type="button" id="regConfirmPassToggle" class="password-toggle-btn"
                                onclick="togglePasswordVisibility('confirmPassword', 'regConfirmPassToggle')"
                                aria-label="Toggle password visibility" title="Show/hide password">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Section 2 -->
            <div style="background: var(--bg-subtle); padding: 18px 20px; border-radius: var(--radius-md); border-left: 4px solid var(--accent-gold); margin-bottom: 20px;">
                <h3 style="font-size: 1.05rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-graduation-cap"></i> 2. Academic Enrollment Details
                </h3>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 16px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="rollNumber">Roll / Registration Number <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="rollNumber" name="rollNumber" class="form-control"
                           value="<c:out value='${rollNumber}'/>" required placeholder="e.g. 2022BCSE042">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="department">Department / Branch <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="department" name="department" class="form-control"
                           value="<c:out value='${department}'/>" required placeholder="e.g. Computer Science &amp; Engineering">
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 24px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="cgpa">Current Cumulative CGPA <span style="color:var(--danger);">*</span> (0.00 – 10.00)</label>
                    <input type="number" step="0.01" min="0.00" max="10.00" id="cgpa" name="cgpa" class="form-control"
                           value="<c:out value='${cgpa}'/>" required placeholder="e.g. 8.45">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="graduationYear">Graduation Year <span style="color:var(--danger);">*</span></label>
                    <input type="number" min="2000" max="2100" id="graduationYear" name="graduationYear" class="form-control"
                           value="<c:out value='${graduationYear}'/>" required placeholder="e.g. 2026">
                </div>
            </div>

            <!-- Section 3 -->
            <div style="background: var(--bg-subtle); padding: 18px 20px; border-radius: var(--radius-md); border-left: 4px solid var(--accent-teal); margin-bottom: 20px;">
                <h3 style="font-size: 1.05rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-briefcase"></i> 3. Contact &amp; Professional Profile (Optional)
                </h3>
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="phone">Contact Phone Number</label>
                <input type="tel" id="phone" name="phone" class="form-control"
                       value="<c:out value='${phone}'/>" placeholder="e.g. +91 9876543210" autocomplete="tel">
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="skills">Technical &amp; Domain Skills</label>
                <input type="text" id="skills" name="skills" class="form-control"
                       value="<c:out value='${skills}'/>" placeholder="e.g. Java, Spring Boot, MySQL, REST APIs, Git, React">
            </div>

            <div class="form-group" style="margin-bottom: 28px;">
                <label class="form-label" for="resumeUrl">Resume / Portfolio Cloud URL</label>
                <input type="url" id="resumeUrl" name="resumeUrl" class="form-control"
                       value="<c:out value='${resumeUrl}'/>" placeholder="e.g. https://drive.google.com/file/d/... or GitHub profile">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 14px; font-size: 1.05rem; font-weight: 700; border-radius: var(--radius-md); box-shadow: var(--shadow-sm);">
                <i class="fa-solid fa-user-check" style="margin-right: 8px;"></i> Complete Student Registration
            </button>
        </form>

        <div style="text-align: center; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--border-light); font-size: 0.9rem; color: var(--text-muted);">
            Already have an active applicant account? <a href="${pageContext.request.contextPath}/login" style="color: var(--primary-navy); font-weight: 700; text-decoration: none;">Sign in here &rarr;</a>
        </div>
    </div>

</main>

<script>
    function validateRegisterForm() {
        var name = document.getElementById("name").value.trim();
        var email = document.getElementById("email").value.trim();
        var password = document.getElementById("password").value;
        var confirmPassword = document.getElementById("confirmPassword").value;
        var rollNumber = document.getElementById("rollNumber").value.trim();
        var department = document.getElementById("department").value.trim();
        var cgpa = parseFloat(document.getElementById("cgpa").value);
        var year = parseInt(document.getElementById("graduationYear").value, 10);

        function showMsg(msg) {
            if (typeof PortalPopups !== 'undefined' && PortalPopups.showToast) {
                PortalPopups.showToast(msg, "error");
            } else {
                alert(msg);
            }
        }

        if (!name || !email || !password || !confirmPassword || !rollNumber || !department) {
            showMsg("Please fill in all mandatory fields marked with an asterisk (*).");
            return false;
        }

        if (password.length < 8) {
            showMsg("Password must contain at least 8 characters.");
            return false;
        }

        if (password !== confirmPassword) {
            showMsg("Password confirmation does not match the entered password.");
            return false;
        }

        if (isNaN(cgpa) || cgpa < 0.0 || cgpa > 10.0) {
            showMsg("Please enter a valid CGPA between 0.00 and 10.00.");
            return false;
        }

        if (isNaN(year) || year < 2000 || year > 2100) {
            showMsg("Please enter a valid graduation year between 2000 and 2100.");
            return false;
        }

        return true;
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
