<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="external-register" scope="request" />
<c:set var="pageTitle" value="External Student Registration | Centurion University Placement Portal" scope="request" />

<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page-container" style="max-width: 800px; margin: 40px auto 60px auto;">

    <div class="card" style="padding: 36px 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); border: 1px solid var(--border-light); background: #ffffff;">
        <div style="text-align: center; margin-bottom: 24px;">
            <img src="${pageContext.request.contextPath}/assets/images/college-logo.png" alt="Centurion University Logo" style="width: 64px; height: 64px; object-fit: contain; border-radius: 50%; box-shadow: 0 2px 8px rgba(0,0,0,0.12); margin-bottom: 12px;"/>
            <h1 class="page-title" style="font-size: 1.6rem; margin: 0 0 6px 0; color: var(--primary-navy); font-weight: 800;">
                External Student Registration &amp; Recruitment Portal
            </h1>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--accent-gold); letter-spacing: 0.05em; text-transform: uppercase; margin-bottom: 8px;">
                Centurion University Directorate of Training &amp; Placement
            </div>
            <p class="page-subtitle" style="margin: 0 auto; color: var(--text-muted); font-size: 0.92rem; max-width: 680px; line-height: 1.5;">
                For students enrolled at other colleges and universities seeking to participate in pooled campus placement drives, corporate internships, and recruitment opportunities.
            </p>
        </div>

        <!-- Institutional Note -->
        <div style="background: rgba(15, 36, 62, 0.04); border-left: 4px solid var(--primary-navy); padding: 14px 18px; border-radius: var(--radius-md); margin-bottom: 24px; font-size: 0.88rem; color: var(--text-main);">
            <div style="display: flex; align-items: flex-start; gap: 10px;">
                <i class="fa-solid fa-circle-info" style="color: var(--primary-navy); margin-top: 3px; font-size: 1.05rem;"></i>
                <div>
                    <strong>Verification Requirement:</strong> External student profiles initially undergo administrative verification. Once verified, you will be able to apply for open campus recruitment drives.
                    <div style="margin-top: 4px;">
                        Are you a Centurion University (CUTM) student?
                        <a href="${pageContext.request.contextPath}/register" style="color: var(--accent-gold); font-weight: 700; text-decoration: underline;">
                            Register via Internal Student Portal &rarr;
                        </a>
                    </div>
                </div>
            </div>
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

        <form action="${pageContext.request.contextPath}/external/register" method="post" enctype="multipart/form-data" id="externalRegisterForm" onsubmit="return validateExternalRegisterForm();">
            <!-- CSRF Protection Token -->
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

            <!-- Section 1: Account Credentials & Contact -->
            <div style="background: var(--bg-subtle); padding: 14px 18px; border-radius: var(--radius-md); border-left: 4px solid var(--primary-navy); margin-bottom: 18px;">
                <h3 style="font-size: 1.05rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-user-circle"></i> 1. Applicant Identity &amp; Login Credentials
                </h3>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 16px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="name">Full Name <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="name" name="name" class="form-control"
                           value="<c:out value='${name}'/>" required placeholder="e.g. Subhashree Dash" autocomplete="name">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="email">Email Address <span style="color:var(--danger);">*</span></label>
                    <input type="email" id="email" name="email" class="form-control"
                           value="<c:out value='${email}'/>" required placeholder="e.g. subhashree.dash@univ.edu" autocomplete="email">
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="phone">Mobile Phone Number <span style="color:var(--danger);">*</span></label>
                <input type="tel" id="phone" name="phone" class="form-control"
                       value="<c:out value='${phone}'/>" required placeholder="e.g. +91 9876543210" autocomplete="tel">
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 24px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="password">Password <span style="color:var(--danger);">*</span> (min. 8 characters)</label>
                    <div class="password-toggle-wrapper">
                        <input type="password" id="password" name="password" minlength="8" class="form-control"
                               required placeholder="Create secure password" autocomplete="new-password">
                        <button type="button" id="extPassToggle" class="password-toggle-btn"
                                onclick="togglePasswordVisibility('password', 'extPassToggle')"
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
                        <button type="button" id="extConfirmPassToggle" class="password-toggle-btn"
                                onclick="togglePasswordVisibility('confirmPassword', 'extConfirmPassToggle')"
                                aria-label="Toggle password visibility" title="Show/hide password">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Section 2: College & Academic Background -->
            <div style="background: var(--bg-subtle); padding: 14px 18px; border-radius: var(--radius-md); border-left: 4px solid var(--accent-gold); margin-bottom: 18px;">
                <h3 style="font-size: 1.05rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-university"></i> 2. College &amp; Academic Enrollment Information
                </h3>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 16px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="collegeName">College / University Name <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="collegeName" name="collegeName" class="form-control"
                           value="<c:out value='${collegeName}'/>" required placeholder="e.g. National Institute of Technology, Rourkela">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="collegeLocation">College Location (City, State) <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="collegeLocation" name="collegeLocation" class="form-control"
                           value="<c:out value='${collegeLocation}'/>" required placeholder="e.g. Rourkela, Odisha">
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 16px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="degree">Degree / Course <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="degree" name="degree" class="form-control"
                           value="<c:out value='${degree}'/>" required placeholder="e.g. B.Tech / M.Tech / MCA / B.Sc">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="department">Department / Branch <span style="color:var(--danger);">*</span></label>
                    <input type="text" id="department" name="department" class="form-control"
                           value="<c:out value='${department}'/>" required placeholder="e.g. Computer Science and Engineering">
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px;">
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="currentSemester">Current Semester <span style="color:var(--danger);">*</span> (1–12)</label>
                    <input type="number" min="1" max="12" id="currentSemester" name="currentSemester" class="form-control"
                           value="<c:out value='${currentSemester}'/>" required placeholder="e.g. 6">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="cgpa">Cumulative CGPA <span style="color:var(--danger);">*</span> (0.00–10.00)</label>
                    <input type="number" step="0.01" min="0.00" max="10.00" id="cgpa" name="cgpa" class="form-control"
                           value="<c:out value='${cgpa}'/>" required placeholder="e.g. 8.65">
                </div>
                <div class="form-group" style="margin: 0;">
                    <label class="form-label" for="graduationYear">Graduation Year <span style="color:var(--danger);">*</span></label>
                    <input type="number" min="2000" max="2100" id="graduationYear" name="graduationYear" class="form-control"
                           value="<c:out value='${graduationYear}'/>" required placeholder="e.g. 2026">
                </div>
            </div>

            <!-- Section 3: Resume Document Upload -->
            <div style="background: var(--bg-subtle); padding: 14px 18px; border-radius: var(--radius-md); border-left: 4px solid var(--accent-teal); margin-bottom: 18px;">
                <h3 style="font-size: 1.05rem; color: var(--primary-navy); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-file-pdf"></i> 3. Resume &amp; Professional Verification Document
                </h3>
            </div>

            <div class="form-group" style="margin-bottom: 24px;">
                <label class="form-label" for="resume">Upload Resume Document <span style="color:var(--danger);">*</span> (PDF, DOC, DOCX &bull; Max 5 MB)</label>
                <input type="file" id="resume" name="resume" class="form-control" accept=".pdf,.doc,.docx" required style="padding: 10px; background: #ffffff;">
                <div style="font-size: 0.82rem; color: var(--text-muted); margin-top: 6px;">
                    <i class="fa-solid fa-shield-halved" style="margin-right: 4px; color: var(--accent-teal);"></i>
                    Your resume is securely stored and used by placement coordinators to verify academic qualifications during campus drives.
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 14px; font-size: 1.05rem; font-weight: 700; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); background: var(--primary-navy); border-color: var(--primary-navy);">
                <i class="fa-solid fa-user-plus" style="margin-right: 8px;"></i> Submit External Registration Request
            </button>
        </form>

        <div style="text-align: center; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--border-light); font-size: 0.9rem; color: var(--text-muted);">
            Already have an approved external account?
            <a href="${pageContext.request.contextPath}/login" style="color: var(--primary-navy); font-weight: 700; text-decoration: none;">
                Sign in to Placement Portal &rarr;
            </a>
        </div>
    </div>

</main>

<script>
    function validateExternalRegisterForm() {
        var name = document.getElementById("name").value.trim();
        var email = document.getElementById("email").value.trim();
        var phone = document.getElementById("phone").value.trim();
        var password = document.getElementById("password").value;
        var confirmPassword = document.getElementById("confirmPassword").value;
        var collegeName = document.getElementById("collegeName").value.trim();
        var collegeLocation = document.getElementById("collegeLocation").value.trim();
        var degree = document.getElementById("degree").value.trim();
        var department = document.getElementById("department").value.trim();
        var semester = parseInt(document.getElementById("currentSemester").value, 10);
        var cgpa = parseFloat(document.getElementById("cgpa").value);
        var year = parseInt(document.getElementById("graduationYear").value, 10);
        var resumeInput = document.getElementById("resume");

        function showMsg(msg) {
            if (typeof PortalPopups !== 'undefined' && PortalPopups.showToast) {
                PortalPopups.showToast(msg, "error");
            } else {
                alert(msg);
            }
        }

        if (!name || !email || !phone || !password || !confirmPassword || !collegeName || !collegeLocation || !degree || !department) {
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

        if (isNaN(semester) || semester < 1 || semester > 12) {
            showMsg("Current semester must be a valid number between 1 and 12.");
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

        if (!resumeInput.files || resumeInput.files.length === 0) {
            showMsg("Please select a resume file to upload (PDF, DOC, or DOCX).");
            return false;
        }

        var file = resumeInput.files[0];
        if (file.size > 5 * 1024 * 1024) {
            showMsg("Resume file exceeds the 5 MB maximum size limit.");
            return false;
        }

        var fileName = file.name.toLowerCase();
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".doc") && !fileName.endsWith(".docx")) {
            showMsg("Only PDF, DOC, or DOCX files are allowed for resume upload.");
            return false;
        }

        return true;
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
