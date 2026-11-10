<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="login" scope="request" />
<c:set var="pageTitle" value="Coordinator Registration | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page-container" style="max-width: 740px; margin: 40px auto 60px auto;">

    <div class="card" style="padding: 36px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); border: 1px solid var(--border-light);">
        <div style="text-align: center; margin-bottom: 24px;">
            <img src="${pageContext.request.contextPath}/assets/images/college-logo.png" alt="Centurion University Logo" style="width: 64px; height: 64px; object-fit: contain; border-radius: 50%; box-shadow: 0 2px 8px rgba(0,0,0,0.12); margin-bottom: 12px;"/>
            <h1 class="page-title" style="font-size: 1.55rem; margin: 0 0 4px 0; color: var(--primary-navy); font-weight: 800;">Placement Coordinator Registration</h1>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--accent-gold); letter-spacing: 0.05em; text-transform: uppercase; margin-bottom: 8px;">Centurion University Training &amp; Placement Cell</div>
            
            <div style="display: inline-flex; align-items: center; gap: 6px; background: #0f243e; color: #ffffff; border: 1px solid var(--accent-gold); border-radius: 20px; padding: 4px 14px; font-size: 0.78rem; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 12px;">
                <i class="fa-solid fa-user-shield" style="margin-right: 4px; color: var(--accent-gold);"></i> COORDINATOR ACCESS REQUEST
            </div>

            <p class="page-subtitle" style="margin: 0 auto; color: var(--text-muted); font-size: 0.9rem; max-width: 580px; line-height: 1.5;">
                Submit your official placement coordinator registration details. All new coordinator accounts are created in <strong>Pending Approval</strong> status and must be verified and approved by a system administrator prior to portal login.
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

        <!-- Information Notice -->
        <div style="background: rgba(230, 154, 24, 0.08); border-left: 4px solid var(--accent-gold); border-radius: var(--radius-md); padding: 14px 18px; margin-bottom: 24px;">
            <div style="display: flex; align-items: flex-start; gap: 10px;">
                <i class="fa-solid fa-shield-halved" style="color: var(--accent-gold); font-size: 1.2rem; margin-top: 2px;"></i>
                <div style="font-size: 0.88rem; color: var(--navy-900); line-height: 1.45;">
                    <strong>Security Verification Notice:</strong> For regulatory and institutional governance, Coordinator Portal credentials are strictly provisioned upon authorization by an existing administrator.
                </div>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/coordinator/register" method="post" id="coordinatorRegisterForm" onsubmit="return validateCoordinatorForm();">
            <!-- CSRF Protection Token -->
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <!-- Section 1: Coordinator Credentials -->
            <div style="background: var(--bg-subtle); padding: 14px 18px; border-radius: var(--radius-md); border-left: 4px solid var(--primary-navy); margin-bottom: 18px;">
                <h3 style="font-size: 1rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px; font-weight: 700;">
                    <i class="fa-solid fa-id-badge" style="color: var(--accent-gold);"></i> 1. Coordinator Personal &amp; Institutional Credentials
                </h3>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                <div class="form-group">
                    <label class="form-label" for="name">
                        <i class="fa-solid fa-user" style="color: var(--primary-navy); margin-right: 4px;"></i> Full Name <span style="color:var(--danger);">*</span>
                    </label>
                    <input type="text" id="name" name="name" class="form-control"
                           value="<c:out value='${name}'/>" required placeholder="e.g. Dr. Rajesh Mohanty" autocomplete="name" autofocus>
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">
                        <i class="fa-solid fa-envelope" style="color: var(--primary-navy); margin-right: 4px;"></i> Institutional Email <span style="color:var(--danger);">*</span>
                    </label>
                    <input type="email" id="email" name="email" class="form-control"
                           value="<c:out value='${email}'/>" required placeholder="e.g. rajesh.m@college.edu" autocomplete="email">
                    <small style="color: var(--text-muted); font-size: 0.78rem;">Must be your official university email domain</small>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                <div class="form-group">
                    <label class="form-label" for="employeeId">
                        <i class="fa-solid fa-id-card" style="color: var(--primary-navy); margin-right: 4px;"></i> Employee ID <span style="color:var(--danger);">*</span>
                    </label>
                    <input type="text" id="employeeId" name="employeeId" class="form-control"
                           value="<c:out value='${employeeId}'/>" required placeholder="e.g. CUTM-EMP-1042">
                </div>

                <div class="form-group">
                    <label class="form-label" for="phone">
                        <i class="fa-solid fa-phone" style="color: var(--primary-navy); margin-right: 4px;"></i> Phone Number <span style="color:var(--danger);">*</span>
                    </label>
                    <input type="tel" id="phone" name="phone" class="form-control"
                           value="<c:out value='${phone}'/>" required placeholder="e.g. 9876543210" autocomplete="tel">
                </div>
            </div>

            <!-- Section 2: Department & Designation -->
            <div style="background: var(--bg-subtle); padding: 14px 18px; border-radius: var(--radius-md); border-left: 4px solid var(--accent-gold); margin-bottom: 18px; margin-top: 24px;">
                <h3 style="font-size: 1rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px; font-weight: 700;">
                    <i class="fa-solid fa-building-columns" style="color: var(--accent-gold);"></i> 2. Department &amp; Academic Role
                </h3>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                <div class="form-group">
                    <label class="form-label" for="department">
                        <i class="fa-solid fa-sitemap" style="color: var(--primary-navy); margin-right: 4px;"></i> Department <span style="color:var(--danger);">*</span>
                    </label>
                    <input type="text" id="department" name="department" class="form-control" list="deptList"
                           value="<c:out value='${department}'/>" required placeholder="Select or type department">
                    <datalist id="deptList">
                        <option value="Training &amp; Placement Cell">
                        <option value="Computer Science &amp; Engineering">
                        <option value="Electronics &amp; Communication">
                        <option value="Mechanical Engineering">
                        <option value="Civil Engineering">
                        <option value="Electrical &amp; Electronics">
                        <option value="Management Studies">
                        <option value="School of Applied Sciences">
                    </datalist>
                </div>

                <div class="form-group">
                    <label class="form-label" for="designation">
                        <i class="fa-solid fa-briefcase" style="color: var(--primary-navy); margin-right: 4px;"></i> Designation <span style="color:var(--danger);">*</span>
                    </label>
                    <input type="text" id="designation" name="designation" class="form-control" list="desigList"
                           value="<c:out value='${designation}'/>" required placeholder="e.g. Assistant Professor / Placement Officer">
                    <datalist id="desigList">
                        <option value="Head Placement Coordinator">
                        <option value="Placement Officer">
                        <option value="Assistant Professor &amp; Coordinator">
                        <option value="Associate Professor">
                        <option value="Professor">
                        <option value="Industry Relations Officer">
                    </datalist>
                </div>
            </div>

            <!-- Section 3: Password & Security -->
            <div style="background: var(--bg-subtle); padding: 14px 18px; border-radius: var(--radius-md); border-left: 4px solid var(--primary-navy); margin-bottom: 18px; margin-top: 24px;">
                <h3 style="font-size: 1rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px; font-weight: 700;">
                    <i class="fa-solid fa-lock" style="color: var(--accent-gold);"></i> 3. Account Password &amp; Verification
                </h3>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px;">
                <div class="form-group">
                    <label class="form-label" for="password">
                        <i class="fa-solid fa-key" style="color: var(--primary-navy); margin-right: 4px;"></i> Password <span style="color:var(--danger);">*</span>
                    </label>
                    <div class="password-toggle-wrapper">
                        <input type="password" id="password" name="password" class="form-control"
                               required placeholder="Minimum 8 characters" autocomplete="new-password" oninput="checkPasswordStrength();">
                        <button type="button" id="passToggle" class="password-toggle-btn"
                                onclick="togglePasswordVisibility('password', 'passToggle')"
                                aria-label="Toggle password visibility">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                    <div id="passwordStrengthFeedback" style="font-size: 0.78rem; margin-top: 4px; color: var(--text-muted);">
                        Must be at least 8 characters
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="confirmPassword">
                        <i class="fa-solid fa-shield-check" style="color: var(--primary-navy); margin-right: 4px;"></i> Confirm Password <span style="color:var(--danger);">*</span>
                    </label>
                    <div class="password-toggle-wrapper">
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                               required placeholder="Re-type password" autocomplete="new-password" oninput="checkPasswordMatch();">
                        <button type="button" id="confirmPassToggle" class="password-toggle-btn"
                                onclick="togglePasswordVisibility('confirmPassword', 'confirmPassToggle')"
                                aria-label="Toggle password visibility">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                    <div id="passwordMatchFeedback" style="font-size: 0.78rem; margin-top: 4px;"></div>
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 13px; font-size: 1rem; font-weight: 700; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); background: #0f243e; border: 1.5px solid var(--accent-gold); color: #ffffff;">
                <i class="fa-solid fa-paper-plane" style="margin-right: 8px; color: var(--accent-gold);"></i> Submit Coordinator Registration Request
            </button>
        </form>

        <div style="text-align: center; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--border-light); font-size: 0.9rem; color: var(--text-muted);">
            Already have an approved account? 
            <a href="${pageContext.request.contextPath}/login?role=coordinator" style="color: var(--primary-navy); font-weight: 700; text-decoration: none;">
                <i class="fa-solid fa-user-shield" style="margin-right: 4px;"></i> Back to Coordinator Sign In &rarr;
            </a>
        </div>
    </div>

</main>

<script>
    function checkPasswordStrength() {
        var pwd = document.getElementById("password").value;
        var feedback = document.getElementById("passwordStrengthFeedback");
        if (!pwd) {
            feedback.textContent = "Must be at least 8 characters";
            feedback.style.color = "var(--text-muted)";
            return;
        }
        if (pwd.length < 8) {
            feedback.textContent = "Weak: minimum 8 characters required (" + pwd.length + "/8)";
            feedback.style.color = "var(--danger)";
        } else {
            feedback.textContent = "Valid password length";
            feedback.style.color = "var(--success)";
        }
    }

    function checkPasswordMatch() {
        var pwd = document.getElementById("password").value;
        var confirmPwd = document.getElementById("confirmPassword").value;
        var feedback = document.getElementById("passwordMatchFeedback");
        if (!confirmPwd) {
            feedback.textContent = "";
            return;
        }
        if (pwd === confirmPwd) {
            feedback.textContent = "Passwords match";
            feedback.style.color = "var(--success)";
        } else {
            feedback.textContent = "Passwords do not match";
            feedback.style.color = "var(--danger)";
        }
    }

    function validateCoordinatorForm() {
        var name = document.getElementById("name").value.trim();
        var email = document.getElementById("email").value.trim();
        var employeeId = document.getElementById("employeeId").value.trim();
        var department = document.getElementById("department").value.trim();
        var designation = document.getElementById("designation").value.trim();
        var phone = document.getElementById("phone").value.trim();
        var password = document.getElementById("password").value;
        var confirmPassword = document.getElementById("confirmPassword").value;

        if (!name || !email || !employeeId || !department || !designation || !phone || !password || !confirmPassword) {
            alert("All required fields must be completed.");
            return false;
        }

        var emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        if (!emailPattern.test(email)) {
            alert("Please enter a valid institutional email address.");
            document.getElementById("email").focus();
            return false;
        }

        if (password.length < 8) {
            alert("Password must contain at least 8 characters.");
            document.getElementById("password").focus();
            return false;
        }

        if (password !== confirmPassword) {
            alert("Passwords do not match. Please re-enter your password confirmation.");
            document.getElementById("confirmPassword").focus();
            return false;
        }

        return true;
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
