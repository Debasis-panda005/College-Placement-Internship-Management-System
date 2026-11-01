<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${not empty param.pageTitle ? param.pageTitle : 'College Placement & Internship Management Portal'}"/></title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Font Awesome Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" crossorigin="anonymous">
    <!-- Master Portal Stylesheets (Context Path Aware) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/portal-theme.css">
    <!-- Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/college-logo.svg">
</head>
<body>

<header class="portal-header">
    <div class="top-bar">
        <div>
            <span><i class="fa fa-graduation-cap"></i> Centurion University of Technology and Management</span> &nbsp;|&nbsp;
            <span><i class="fa fa-briefcase"></i> Directorate of Training &amp; Placement</span>
        </div>
        <div class="tagline">
            Connecting Through Knowledge &bull; Shaping Lives, Empowering Communities
        </div>
    </div>

    <div class="main-nav-container">
        <a href="${pageContext.request.contextPath}/" class="brand-identity" title="Centurion University Placement Portal">
            <img src="${pageContext.request.contextPath}/assets/images/college-logo.png" alt="Centurion University Logo" class="brand-logo-img"/>
            <div class="brand-text">
                <span class="brand-title">CENTURION UNIVERSITY</span>
                <span class="brand-dept">OF TECHNOLOGY AND MANAGEMENT</span>
                <span class="brand-subtitle">TRAINING &amp; PLACEMENT CELL</span>
            </div>
        </a>

        <!-- Mobile Navigation Toggle -->
        <button type="button" class="mobile-menu-toggle" onclick="toggleMobileNav()" aria-label="Toggle navigation menu" aria-expanded="false">
            <i class="fa fa-bars"></i>
        </button>

        <nav id="portalNav">
            <ul class="nav-links-list">
                <li class="nav-link-item ${param.activeNav == 'home' || activeNav == 'home' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/"><i class="fa fa-home"></i> Home</a>
                </li>

                <c:choose>
                    <%-- STUDENT NAVIGATION --%>
                    <c:when test="${sessionScope.userRole == 'STUDENT'}">
                        <li class="nav-link-item ${param.activeNav == 'jobs' || activeNav == 'jobs' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/jobs"><i class="fa fa-briefcase"></i> Opportunities</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'applications' || activeNav == 'applications' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/applications"><i class="fa fa-file-alt"></i> My Applications</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'shortlist' || activeNav == 'shortlist' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/shortlist"><i class="fa fa-star"></i> Shortlisted</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'interviews' || activeNav == 'interviews' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/interviews"><i class="fa fa-calendar-check"></i> My Interviews</a>
                        </li>
                        <li class="nav-link-item">
                            <span class="nav-user-badge"><i class="fa fa-user-graduate"></i> <c:out value="${sessionScope.userName}"/></span>
                        </li>
                        <li class="nav-link-item">
                            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline; margin:0; padding:0;">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                                <button type="submit" class="nav-logout-btn"><i class="fa fa-sign-out-alt"></i> Logout</button>
                            </form>
                        </li>
                    </c:when>

                    <%-- COORDINATOR OR ADMIN NAVIGATION --%>
                    <c:when test="${sessionScope.userRole == 'COORDINATOR' || sessionScope.userRole == 'ADMIN'}">
                        <li class="nav-link-item ${param.activeNav == 'dashboard' || activeNav == 'dashboard' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fa fa-chart-line"></i> Dashboard</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'jobs' || activeNav == 'jobs' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/jobs"><i class="fa fa-briefcase"></i> Opportunities</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'applications' || activeNav == 'applications' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/applications"><i class="fa fa-users"></i> Applications</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'shortlist' || activeNav == 'shortlist' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/shortlist"><i class="fa fa-clipboard-check"></i> Shortlist</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'interviews' || activeNav == 'interviews' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/interviews"><i class="fa fa-calendar-alt"></i> Interviews</a>
                        </li>
                        <li class="nav-link-item">
                            <span class="nav-user-badge">
                                <c:choose>
                                    <c:when test="${sessionScope.userRole == 'ADMIN'}"><i class="fa fa-shield-alt"></i> Admin</c:when>
                                    <c:otherwise><i class="fa fa-user-tie"></i> Coordinator</c:otherwise>
                                </c:choose>:
                                <c:out value="${sessionScope.userName}"/>
                            </span>
                        </li>
                        <li class="nav-link-item">
                            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline; margin:0; padding:0;">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>" />
                                <button type="submit" class="nav-logout-btn"><i class="fa fa-sign-out-alt"></i> Logout</button>
                            </form>
                        </li>
                    </c:when>

                    <%-- PUBLIC / UNAUTHENTICATED NAVIGATION --%>
                    <c:otherwise>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/#about"><i class="fa fa-info-circle"></i> About</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/#statistics"><i class="fa fa-chart-bar"></i> Statistics</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/#recruiters"><i class="fa fa-building"></i> Recruiters</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/#process"><i class="fa fa-tasks"></i> Process</a>
                        </li>
                        <li class="nav-link-item ${param.activeNav == 'jobs' || activeNav == 'jobs' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/jobs"><i class="fa fa-briefcase"></i> Openings</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/#contact"><i class="fa fa-envelope"></i> Contact</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/login" class="btn-nav-highlight"><i class="fa fa-lock"></i> Student Sign In</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/login?role=coordinator" class="btn-nav-coordinator" title="Coordinator Portal Sign In"><span aria-hidden="true" style="margin-right: 4px;">🔐</span> Coordinator Login</a>
                        </li>
                        <li class="nav-link-item">
                            <a href="${pageContext.request.contextPath}/register" class="btn-nav-gold"><i class="fa fa-user-plus"></i> Register</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </nav>
    </div>
</header>
