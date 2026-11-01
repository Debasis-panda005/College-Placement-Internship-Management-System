/**
 * Portal Popups, Confirmation Modals and Toast Notifications
 * Enhances user experience while server-side validation remains authoritative.
 */

var PortalPopups = (function() {

    function ensureModalContainer() {
        var existing = document.getElementById("portalModalOverlay");
        if (existing) return existing;

        var overlay = document.createElement("div");
        overlay.id = "portalModalOverlay";
        overlay.className = "portal-modal-overlay";
        overlay.innerHTML =
            '<div class="portal-modal-box" role="dialog" aria-modal="true">' +
            '  <div class="portal-modal-header">' +
            '    <div class="portal-modal-icon" id="portalModalIcon">⚠️</div>' +
            '    <h3 id="portalModalTitle">Confirm Action</h3>' +
            '  </div>' +
            '  <div class="portal-modal-body" id="portalModalMessage">Are you sure?</div>' +
            '  <div class="portal-modal-footer">' +
            '    <button type="button" class="btn-modal btn-modal-cancel" id="portalModalCancel">Cancel</button>' +
            '    <button type="button" class="btn-modal btn-modal-confirm" id="portalModalConfirm">Confirm</button>' +
            '  </div>' +
            '</div>';

        document.body.appendChild(overlay);
        return overlay;
    }

    function confirmAction(options, onConfirm) {
        var overlay = ensureModalContainer();
        var titleEl = document.getElementById("portalModalTitle");
        var msgEl = document.getElementById("portalModalMessage");
        var iconEl = document.getElementById("portalModalIcon");
        var confirmBtn = document.getElementById("portalModalConfirm");
        var cancelBtn = document.getElementById("portalModalCancel");

        titleEl.textContent = options.title || "Confirm Action";
        msgEl.innerHTML = options.message || "Are you sure you want to proceed?";
        iconEl.textContent = options.icon || "⚠️";
        confirmBtn.textContent = options.confirmText || "Confirm";
        confirmBtn.className = "btn-modal btn-modal-confirm " + (options.btnClass || "btn-primary-action");

        overlay.classList.add("active");

        function close() {
            overlay.classList.remove("active");
            confirmBtn.onclick = null;
            cancelBtn.onclick = null;
        }

        cancelBtn.onclick = function() {
            close();
            if (options.onCancel) options.onCancel();
        };

        confirmBtn.onclick = function() {
            close();
            if (onConfirm) onConfirm();
        };
    }

    function showToast(message, type) {
        var container = document.getElementById("portalToastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "portalToastContainer";
            container.className = "portal-toast-container";
            document.body.appendChild(container);
        }

        var toast = document.createElement("div");
        toast.className = "portal-toast toast-" + (type || "info");

        var icon = "ℹ️";
        if (type === "success") icon = "✅";
        if (type === "error" || type === "danger") icon = "❌";
        if (type === "warning") icon = "⚠️";

        toast.innerHTML =
            '<span class="toast-icon">' + icon + '</span>' +
            '<div class="toast-content">' + message + '</div>' +
            '<button type="button" class="toast-close">&times;</button>';

        container.appendChild(toast);

        toast.querySelector(".toast-close").onclick = function() {
            toast.classList.add("fade-out");
            setTimeout(function() { toast.remove(); }, 300);
        };

        setTimeout(function() {
            if (toast.parentNode) {
                toast.classList.add("fade-out");
                setTimeout(function() { toast.remove(); }, 300);
            }
        }, 5000);
    }

    // Attach confirmation handlers to standard action forms
    function initAutoConfirm() {
        document.addEventListener("submit", function(e) {
            var form = e.target;
            if (form.getAttribute("data-confirmed") === "true") return;

            var actionType = form.getAttribute("data-action-type");
            var customMsg = form.getAttribute("data-confirm-message");

            if (!actionType && !customMsg) return;

            e.preventDefault();

            var title = "Confirm Operation";
            var message = customMsg || "Are you sure you wish to execute this action?";
            var icon = "⚠️";
            var btnClass = "btn-primary-action";

            if (actionType === "shortlist") {
                title = "Shortlist Candidate";
                message = "Are you sure you want to shortlist this student for interviews?";
                icon = "📋";
            } else if (actionType === "select") {
                title = "Final Selection";
                message = "Are you sure you want to mark this candidate as <strong>SELECTED</strong>? This will generate the official placement report.";
                icon = "🎉";
                btnClass = "btn-success-action";
            } else if (actionType === "reject") {
                title = "Reject Application";
                message = "Are you sure you want to <strong>REJECT</strong> this application? This action will close the selection process.";
                icon = "⛔";
                btnClass = "btn-danger-action";
            } else if (actionType === "delete") {
                title = "Confirm Deletion";
                message = "Are you sure you want to delete this record? This action cannot be undone.";
                icon = "🗑️";
                btnClass = "btn-danger-action";
            } else if (actionType === "cancelRound") {
                title = "Cancel Interview Round";
                message = "Are you sure you want to cancel this interview round?";
                icon = "🚫";
                btnClass = "btn-danger-action";
            }

            confirmAction({
                title: title,
                message: message,
                icon: icon,
                btnClass: btnClass
            }, function() {
                form.setAttribute("data-confirmed", "true");
                form.submit();
            });
        });

        // Convert existing server alert banners into floating toasts as well
        var alertError = document.querySelector(".alert-error, .alert-danger");
        if (alertError && alertError.textContent.trim()) {
            showToast(alertError.textContent.trim(), "error");
        }
        var alertSuccess = document.querySelector(".alert-success");
        if (alertSuccess && alertSuccess.textContent.trim()) {
            showToast(alertSuccess.textContent.trim(), "success");
        }
    }

    return {
        confirm: confirmAction,
        toast: showToast,
        init: initAutoConfirm
    };
})();

document.addEventListener("DOMContentLoaded", function() {
    PortalPopups.init();
});
