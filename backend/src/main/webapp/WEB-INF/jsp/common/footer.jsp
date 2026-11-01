<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<footer class="portal-footer" id="contact">
    <div class="footer-content">
        <div class="footer-col">
            <div style="display:flex; align-items:center; gap:12px; margin-bottom:14px;">
                <img src="${pageContext.request.contextPath}/assets/images/college-logo.png" alt="Centurion University Logo" style="width:44px; height:44px; object-fit:contain; border-radius:50%;"/>
                <div>
                    <h4 style="margin:0; padding:0; border:none; font-size:14px; color:#ffffff; line-height:1.2;">Centurion University of Technology and Management</h4>
                    <span style="font-size:11px; font-weight:700; color:var(--accent-gold); letter-spacing:0.06em; text-transform:uppercase;">Training &amp; Placement Cell</span>
                </div>
            </div>
            <p style="font-size: 13px; line-height: 1.6; color: #94a3b8;">
                Training and Placement Cell dedicated to bridging the gap between student aspirations and global career opportunities through industry partnerships, continuous technical grooming, and merit-based recruitment drives.
            </p>
            <div style="font-size: 12px; color: #cbd5e1; line-height: 1.6;">
                🏛️ Centurion University of Technology and Management<br>
                🎓 Training &amp; Placement Cell<br>
                🌐 College Placement and Internship Management Portal
            </div>
        </div>

        <div class="footer-col">
            <h4>Quick Links</h4>
            <ul class="footer-links">
                <li><a href="${pageContext.request.contextPath}/">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/#about">About Placement Cell</a></li>
                <li><a href="${pageContext.request.contextPath}/#statistics">Placement Statistics</a></li>
                <li><a href="${pageContext.request.contextPath}/#recruiters">Recruiting Partners</a></li>
                <li><a href="${pageContext.request.contextPath}/#process">Placement Process</a></li>
                <li><a href="${pageContext.request.contextPath}/jobs">Current Openings</a></li>
            </ul>
        </div>

        <div class="footer-col">
            <h4>Student Portals</h4>
            <ul class="footer-links">
                <li><a href="${pageContext.request.contextPath}/login">Student Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register">New Student Registration</a></li>
                <li><a href="${pageContext.request.contextPath}/jobs?type=Internship">Internship Drives</a></li>
                <li><a href="${pageContext.request.contextPath}/jobs?type=Full Time">Full-Time Placement</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard">Staff / Admin Access</a></li>
            </ul>
        </div>

        <div class="footer-col">
            <h4>Compliance &amp; Advisory</h4>
            <p style="font-size: 12px; line-height: 1.5; color: #94a3b8;">
                All student applications and interview schedules are governed by the Centurion University Placement Policy. 
            </p>
            <div style="background: rgba(255,255,255,0.05); padding: 10px; border-radius: 6px; font-size: 11px; border: 1px solid rgba(255,255,255,0.1); color: #cbd5e1;">
                ℹ️ <strong>Demonstration Notice:</strong> Historical multi-year charts and figures presented on the public portal are illustrative demo datasets for portal functionality demonstration.
            </div>
        </div>
    </div>

    <div class="footer-bottom">
        <div>
            &copy; 2026 Centurion University of Technology and Management. All rights reserved. Training &amp; Placement Cell &bull; College Placement and Internship Management Portal.
        </div>
        <div>
            Built with Jakarta Servlets &bull; JSP &bull; JDBC &bull; MySQL &bull; Tomcat 11
        </div>
    </div>
</footer>

<!-- Shared JavaScript Utilities -->
<script src="${pageContext.request.contextPath}/assets/js/portal-popups.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/portal-validation.js"></script>
</body>
</html>
