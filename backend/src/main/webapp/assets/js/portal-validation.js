/**
 * Client-Side Validation Engine for College Placement Portal
 * Complements and reflects authoritative server-side validation rules.
 */

var PortalValidation = (function() {

    function validateEmail(email) {
        var re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    }

    function validatePhone(phone) {
        if (!phone) return true; // optional unless required
        var re = /^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,6}$/;
        return re.test(phone.replace(/[\s-]/g, ''));
    }

    function initRegistrationValidation() {
        var regForm = document.getElementById("registerForm");
        if (!regForm) return;

        regForm.addEventListener("submit", function(e) {
            var errors = [];

            var name = regForm.elements["name"] ? regForm.elements["name"].value.trim() : "";
            var email = regForm.elements["email"] ? regForm.elements["email"].value.trim() : "";
            var password = regForm.elements["password"] ? regForm.elements["password"].value : "";
            var confirmPassword = regForm.elements["confirmPassword"] ? regForm.elements["confirmPassword"].value : "";
            var rollNumber = regForm.elements["rollNumber"] ? regForm.elements["rollNumber"].value.trim() : "";
            var department = regForm.elements["department"] ? regForm.elements["department"].value.trim() : "";
            var cgpa = regForm.elements["cgpa"] ? parseFloat(regForm.elements["cgpa"].value) : NaN;
            var gradYear = regForm.elements["graduationYear"] ? parseInt(regForm.elements["graduationYear"].value, 10) : NaN;
            var phone = regForm.elements["phone"] ? regForm.elements["phone"].value.trim() : "";

            if (!name) errors.push("Full name is required.");
            if (!email || !validateEmail(email)) errors.push("A valid email address is required.");
            if (!password || password.length < 8) errors.push("Password must be at least 8 characters long.");
            if (confirmPassword !== undefined && password !== confirmPassword) {
                errors.push("Passwords do not match.");
            }
            if (!rollNumber) errors.push("Registration / Roll number is required.");
            if (!department) errors.push("Department is required.");
            if (isNaN(cgpa) || cgpa < 0 || cgpa > 10) errors.push("CGPA must be between 0.00 and 10.00.");
            if (isNaN(gradYear) || gradYear < 2000 || gradYear > 2100) errors.push("Graduation year must be between 2000 and 2100.");
            if (phone && !validatePhone(phone)) errors.push("Phone number format is invalid.");

            if (errors.length > 0) {
                e.preventDefault();
                showValidationModal(errors);
            }
        });
    }

    function initJobValidation() {
        var jobForm = document.getElementById("jobForm");
        if (!jobForm) return;

        jobForm.addEventListener("submit", function(e) {
            var errors = [];
            var title = jobForm.elements["title"] ? jobForm.elements["title"].value.trim() : "";
            var company = jobForm.elements["company"] ? jobForm.elements["company"].value.trim() : "";
            var location = jobForm.elements["location"] ? jobForm.elements["location"].value.trim() : "";
            var minCgpaEl = jobForm.elements["minCgpa"];
            var deadlineEl = jobForm.elements["deadline"];

            if (!title) errors.push("Job/Internship title is required.");
            if (!company) errors.push("Company name is required.");
            if (!location) errors.push("Location is required.");

            if (minCgpaEl && minCgpaEl.value.trim()) {
                var cgpa = parseFloat(minCgpaEl.value.trim());
                if (isNaN(cgpa) || cgpa < 0 || cgpa > 10) {
                    errors.push("Minimum CGPA must be between 0.00 and 10.00.");
                }
            }

            if (deadlineEl && deadlineEl.value.trim()) {
                var deadline = new Date(deadlineEl.value.trim());
                var today = new Date();
                today.setHours(0, 0, 0, 0);
                if (deadline < today) {
                    errors.push("Application deadline cannot be in the past.");
                }
            }

            if (errors.length > 0) {
                e.preventDefault();
                showValidationModal(errors);
            }
        });
    }

    function initInterviewValidation() {
        var interviewForm = document.getElementById("interviewForm") || document.getElementById("scheduleRoundForm");
        if (!interviewForm) return;

        interviewForm.addEventListener("submit", function(e) {
            var errors = [];
            var dateEl = interviewForm.elements["interviewDate"];
            var timeEl = interviewForm.elements["interviewTime"];
            var modeEl = interviewForm.elements["mode"];

            if (dateEl && dateEl.value.trim()) {
                var date = new Date(dateEl.value.trim());
                var today = new Date();
                today.setHours(0, 0, 0, 0);
                if (date < today) {
                    errors.push("Interview date cannot be scheduled in the past.");
                }
            } else if (dateEl) {
                errors.push("Interview date is required.");
            }

            if (timeEl && !timeEl.value.trim()) {
                errors.push("Interview time is required.");
            }

            if (modeEl && !modeEl.value.trim()) {
                errors.push("Interview mode (ONLINE or OFFLINE) is required.");
            }

            if (errors.length > 0) {
                e.preventDefault();
                showValidationModal(errors);
            }
        });
    }

    function showValidationModal(errors) {
        if (typeof PortalPopups !== "undefined" && PortalPopups.toast) {
            PortalPopups.toast(errors.join("<br>"), "error");
        } else {
            alert(errors.join("\n"));
        }
    }

    function togglePasswordVisibility(inputId, toggleBtnId) {
        var input = document.getElementById(inputId);
        var btn = document.getElementById(toggleBtnId);
        if (!input) return;
        if (input.type === "password") {
            input.type = "text";
            if (btn) btn.innerHTML = '<i class="fa fa-eye-slash"></i>';
        } else {
            input.type = "password";
            if (btn) btn.innerHTML = '<i class="fa fa-eye"></i>';
        }
    }

    function toggleMobileNav() {
        var navList = document.querySelector(".nav-links-list");
        var toggleBtn = document.querySelector(".mobile-menu-toggle");
        if (navList) {
            navList.classList.toggle("mobile-open");
            if (toggleBtn) {
                var expanded = navList.classList.contains("mobile-open");
                toggleBtn.setAttribute("aria-expanded", expanded ? "true" : "false");
            }
        }
    }

    return {
        init: function() {
            initRegistrationValidation();
            initJobValidation();
            initInterviewValidation();
        },
        togglePassword: togglePasswordVisibility,
        toggleNav: toggleMobileNav
    };
})();

// Expose global shortcuts for inline event handlers
window.togglePasswordVisibility = PortalValidation.togglePassword;
window.toggleMobileNav = PortalValidation.toggleNav;

document.addEventListener("DOMContentLoaded", function() {
    PortalValidation.init();
});
