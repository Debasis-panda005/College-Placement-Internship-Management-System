<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activeNav" value="home" scope="request" />
<c:set var="pageTitle" value="College Placement &amp; Internship Portal | Centurion University of Technology and Management" scope="request" />

<!-- Professional Navigation Header -->
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

    <!-- Hero Section -->
    <section class="hero-section">
        <div class="hero-content">
            <span class="hero-badge">Center for Corporate Relations &bull; Batch 2026</span>
            <h1 class="hero-heading">Your Career. Your Future.<br>Our Commitment.</h1>
            <p class="hero-subtitle">
                Explore premier campus placement opportunities, discover leading corporate partners, and begin your professional career through our comprehensive Placement &amp; Internship Management Portal.
            </p>
            <div class="hero-actions">
                <a href="${pageContext.request.contextPath}/jobs" class="btn-hero-primary">
                    🔍 Explore Opportunities
                </a>
                <c:choose>
                    <c:when test="${empty sessionScope.userId}">
                        <a href="${pageContext.request.contextPath}/login" class="btn-hero-secondary">
                            <i class="fa-solid fa-right-to-bracket" style="margin-right: 6px;"></i> Student Login
                        </a>
                        <a href="${pageContext.request.contextPath}/register" class="btn-hero-secondary" title="Centurion University (CUTM) Students">
                            <i class="fa-solid fa-user-graduate" style="margin-right: 6px;"></i> CUTM Student Registration
                        </a>
                        <a href="${pageContext.request.contextPath}/external/register" class="btn-hero-secondary" style="border-color: var(--accent-gold); color: #fff; background: rgba(230, 154, 24, 0.18);" title="Students from Other Universities / Colleges">
                            <i class="fa-solid fa-university" style="margin-right: 6px; color: var(--accent-gold);"></i> External Student Registration
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/applications" class="btn-hero-secondary">
                            📋 View My Applications
                        </a>
                    </c:otherwise>
                </c:choose>
                <a href="#statistics" class="btn-hero-secondary">
                    📊 Placement Statistics
                </a>
            </div>
        </div>
    </section>

    <!-- Auto-scrolling Recruiter Logo Strip -->
    <section class="portal-container" style="margin-top: -30px; position: relative; z-index: 10;">
        <div class="logo-ticker-wrapper">
            <div style="padding: 0 20px 8px 20px; font-size: 11px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em; text-align: center;">
                Illustrative Corporate Recruitment Partners &bull; Industry Leaders
            </div>
            <div class="logo-ticker-track">
                <!-- Group 1 -->
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/tcs.svg" alt="TCS Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/infosys.svg" alt="Infosys Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/wipro.svg" alt="Wipro Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/accenture.svg" alt="Accenture Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/cognizant.svg" alt="Cognizant Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/capgemini.svg" alt="Capgemini Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/hcltech.svg" alt="HCLTech Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/deloitte.svg" alt="Deloitte Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/ibm.svg" alt="IBM Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/techmahindra.svg" alt="Tech Mahindra Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/microsoft.svg" alt="Microsoft Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/amazon.svg" alt="Amazon Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/oracle.svg" alt="Oracle Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/ltimindtree.svg" alt="LTIMindtree Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/genpact.svg" alt="Genpact Logo"/></div>
                <!-- Duplicate for seamless looping -->
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/tcs.svg" alt="TCS Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/infosys.svg" alt="Infosys Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/wipro.svg" alt="Wipro Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/accenture.svg" alt="Accenture Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/cognizant.svg" alt="Cognizant Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/capgemini.svg" alt="Capgemini Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/hcltech.svg" alt="HCLTech Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/deloitte.svg" alt="Deloitte Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/ibm.svg" alt="IBM Logo"/></div>
                <div class="ticker-item"><img src="${pageContext.request.contextPath}/assets/images/logos/techmahindra.svg" alt="Tech Mahindra Logo"/></div>
            </div>
        </div>
    </section>

    <!-- Placement Highlights & Analytics Dashboard -->
    <section class="portal-container" id="statistics">
        <div style="text-align: center; margin-bottom: 25px;">
            <span style="font-size: 12px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em;">
                Institutional Performance Metrics
            </span>
            <h2 style="font-size: 28px; margin-top: 4px; margin-bottom: 8px;">
                Placement Highlights &amp; Previous Year Statistics
            </h2>
            <p style="color: var(--text-muted); max-width: 650px; margin: 0 auto;">
                Explore year-on-year recruitment trends, compensation benchmarks, and hiring milestones across participating academic programs.
            </p>
        </div>

        <!-- Academic Year Selector -->
        <div class="year-selector-bar">
            <span style="font-size: 13px; font-weight: 700; color: var(--secondary-navy); margin-right: 6px;">Select Academic Year:</span>
            <button type="button" class="btn-year-select" data-year="2022-23" onclick="PlacementAnalytics.selectYear('2022-23')">2022–23</button>
            <button type="button" class="btn-year-select" data-year="2023-24" onclick="PlacementAnalytics.selectYear('2023-24')">2023–24</button>
            <button type="button" class="btn-year-select" data-year="2024-25" onclick="PlacementAnalytics.selectYear('2024-25')">2024–25</button>
            <button type="button" class="btn-year-select active" data-year="2025-26" onclick="PlacementAnalytics.selectYear('2025-26')">2025–26 (Current Drive)</button>
        </div>

        <div style="text-align: center; font-size: 13px; color: var(--text-muted); margin-bottom: 10px;">
            Displaying metrics for Academic Year: <strong id="selectedYearBadge" style="color: var(--primary-navy);">2025-26</strong>
        </div>

        <!-- Placement KPI Cards -->
        <div class="kpi-grid">
            <div class="kpi-card kpi-blue">
                <div class="kpi-value" id="kpiCompanies">45</div>
                <div class="kpi-label">Companies Visited</div>
            </div>
            <div class="kpi-card kpi-green">
                <div class="kpi-value" id="kpiPlaced">360</div>
                <div class="kpi-label">Students Placed</div>
            </div>
            <div class="kpi-card kpi-gold">
                <div class="kpi-value" id="kpiPercentage">80%</div>
                <div class="kpi-label">Placement Percentage</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value" id="kpiAverage">5.8 LPA</div>
                <div class="kpi-label">Average Package</div>
            </div>
            <div class="kpi-card kpi-gold">
                <div class="kpi-value" id="kpiHighest">22.0 LPA</div>
                <div class="kpi-label">Highest Package</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value" id="kpiLowest">3.2 LPA</div>
                <div class="kpi-label">Lowest Package</div>
            </div>
        </div>

        <!-- 4 Placement Analytics Charts -->
        <div class="charts-grid">
            <div class="chart-card">
                <h4>Year-wise Placement Percentage Trend</h4>
                <div class="chart-subtitle">Consistent growth in overall student placement conversions (%)</div>
                <div class="chart-canvas-wrapper">
                    <canvas id="chartPlacementPct"></canvas>
                </div>
            </div>

            <div class="chart-card">
                <h4>Average, Highest &amp; Lowest Salary Packages</h4>
                <div class="chart-subtitle">Annual compensation progression across recruiters (in LPA)</div>
                <div class="chart-canvas-wrapper">
                    <canvas id="chartLpaComparison"></canvas>
                </div>
            </div>

            <div class="chart-card">
                <h4>Recruiting Organizations Visited</h4>
                <div class="chart-subtitle">Expansion of corporate campus recruitment drives each academic year</div>
                <div class="chart-canvas-wrapper">
                    <canvas id="chartCompaniesVisited"></canvas>
                </div>
            </div>

            <div class="chart-card">
                <h4>Students Placed vs. Eligible Pool</h4>
                <div class="chart-subtitle">Placement absorption ratio against graduating cohorts</div>
                <div class="chart-canvas-wrapper">
                    <canvas id="chartStudentsPlaced"></canvas>
                </div>
            </div>
        </div>
    </section>

    <!-- Recruiting Partners Showcase (15 Companies) -->
    <section class="portal-container recruiter-showcase-section" id="recruiters">
        <div style="text-align: center; margin-bottom: 25px;">
            <span style="font-size: 12px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em;">
                Industry Engagement &bull; Placement Network
            </span>
            <h2 style="font-size: 28px; margin-top: 4px; margin-bottom: 8px;">
                Our Recruiting Partners
            </h2>
            <p style="color: var(--text-muted); max-width: 700px; margin: 0 auto;">
                Prominent corporate recruiters and global technology enterprises participating in campus talent acquisition.
                <br><small style="color: #94a3b8;">(Illustrative partner showcase for demonstration purposes)</small>
            </p>
        </div>

        <div class="logo-grid">
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/tcs.svg" alt="TCS Logo"/>
                <div class="company-name">Tata Consultancy Services</div>
                <div class="company-sector">IT Services &bull; SDE &amp; Ninja</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/infosys.svg" alt="Infosys Logo"/>
                <div class="company-name">Infosys Ltd.</div>
                <div class="company-sector">Consulting &bull; SP / DSE</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/wipro.svg" alt="Wipro Logo"/>
                <div class="company-name">Wipro Technologies</div>
                <div class="company-sector">IT Solutions &bull; Turbo / Elite</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/accenture.svg" alt="Accenture Logo"/>
                <div class="company-name">Accenture</div>
                <div class="company-sector">Strategy &bull; ASE / FSE</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/cognizant.svg" alt="Cognizant Logo"/>
                <div class="company-name">Cognizant</div>
                <div class="company-sector">Digital Business &bull; GenC</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/capgemini.svg" alt="Capgemini Logo"/>
                <div class="company-name">Capgemini</div>
                <div class="company-sector">Cloud &bull; Senior Analyst</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/hcltech.svg" alt="HCLTech Logo"/>
                <div class="company-name">HCLTech</div>
                <div class="company-sector">Engineering &bull; Graduate Trainee</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/deloitte.svg" alt="Deloitte Logo"/>
                <div class="company-name">Deloitte USI</div>
                <div class="company-sector">Audit &amp; Risk &bull; Advisory</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/ibm.svg" alt="IBM Logo"/>
                <div class="company-name">IBM India</div>
                <div class="company-sector">Cloud &bull; Associate Developer</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/techmahindra.svg" alt="Tech Mahindra Logo"/>
                <div class="company-name">Tech Mahindra</div>
                <div class="company-sector">Telecom &bull; Associate Software Eng</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/microsoft.svg" alt="Microsoft Logo"/>
                <div class="company-name">Microsoft</div>
                <div class="company-sector">Product &bull; Software Engineer</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/amazon.svg" alt="Amazon Logo"/>
                <div class="company-name">Amazon India</div>
                <div class="company-sector">E-Commerce &bull; SDE I</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/oracle.svg" alt="Oracle Logo"/>
                <div class="company-name">Oracle</div>
                <div class="company-sector">Database &bull; Server Technologies</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/ltimindtree.svg" alt="LTIMindtree Logo"/>
                <div class="company-name">LTIMindtree</div>
                <div class="company-sector">Digital Solutions &bull; Specialist</div>
            </div>
            <div class="company-logo-card">
                <img src="${pageContext.request.contextPath}/assets/images/logos/genpact.svg" alt="Genpact Logo"/>
                <div class="company-name">Genpact</div>
                <div class="company-sector">Analytics &bull; Management Trainee</div>
            </div>
        </div>

        <!-- Sample Company-Wise Placement Breakdown Table (Demo Data) -->
        <div class="portal-card" style="margin-top: 30px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; flex-wrap:wrap; gap:10px;">
                <div>
                    <h3 style="margin:0; font-size:18px;">Company-Wise Placement Distribution</h3>
                    <div style="font-size:12px; color:var(--text-muted);">Illustrative selection breakdown for recent recruitment cycles</div>
                </div>
                <span class="status-badge badge-scheduled">Demo Placement Dataset</span>
            </div>

            <div class="table-responsive-wrapper">
                <table class="portal-table">
                    <thead>
                        <tr>
                            <th>Recruiting Company</th>
                            <th>Academic Drive</th>
                            <th>Job Role / Profile</th>
                            <th>Students Selected</th>
                            <th>Package Range (LPA)</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>Tata Consultancy Services</strong></td>
                            <td>2025–26</td>
                            <td>Software Engineer (Digital &amp; Prime)</td>
                            <td>45 Candidates</td>
                            <td>3.8 – 7.5 LPA</td>
                            <td><span class="status-badge badge-passed">Completed</span></td>
                        </tr>
                        <tr>
                            <td><strong>Infosys Ltd.</strong></td>
                            <td>2025–26</td>
                            <td>Specialist Programmer (SP)</td>
                            <td>40 Candidates</td>
                            <td>4.0 – 9.5 LPA</td>
                            <td><span class="status-badge badge-passed">Completed</span></td>
                        </tr>
                        <tr>
                            <td><strong>Wipro Technologies</strong></td>
                            <td>2025–26</td>
                            <td>Project Engineer (Turbo)</td>
                            <td>35 Candidates</td>
                            <td>3.5 – 6.5 LPA</td>
                            <td><span class="status-badge badge-passed">Completed</span></td>
                        </tr>
                        <tr>
                            <td><strong>Accenture</strong></td>
                            <td>2024–25</td>
                            <td>Associate Software Engineer</td>
                            <td>38 Candidates</td>
                            <td>4.5 – 8.5 LPA</td>
                            <td><span class="status-badge badge-passed">Completed</span></td>
                        </tr>
                        <tr>
                            <td><strong>Deloitte USI</strong></td>
                            <td>2025–26</td>
                            <td>Technology Analyst &amp; Advisory</td>
                            <td>18 Candidates</td>
                            <td>6.5 – 11.5 LPA</td>
                            <td><span class="status-badge badge-passed">Completed</span></td>
                        </tr>
                        <tr>
                            <td><strong>Microsoft</strong></td>
                            <td>2025–26</td>
                            <td>Software Development Engineer</td>
                            <td>6 Candidates</td>
                            <td>14.0 – 22.0 LPA</td>
                            <td><span class="status-badge badge-passed">Completed</span></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </section>

    <!-- Placement Process Section (8 Steps) -->
    <section class="portal-container" id="process">
        <div style="text-align: center; margin-bottom: 30px;">
            <span style="font-size: 12px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em;">
                Structured Career Pathway
            </span>
            <h2 style="font-size: 28px; margin-top: 4px; margin-bottom: 8px;">
                Campus Placement &amp; Internship Process
            </h2>
            <p style="color: var(--text-muted); max-width: 650px; margin: 0 auto;">
                A transparent, eight-stage merit workflow ensuring fair opportunities and smooth coordination between students and corporate partners.
            </p>
        </div>

        <div class="process-timeline">
            <a href="${pageContext.request.contextPath}/register" class="process-step-card" title="Navigate to Student Registration">
                <div class="step-number">1</div>
                <div class="step-title">Student Registration</div>
                <p class="step-desc">Students register an authenticated user account with email verification and secure credential hashing.</p>
            </a>

            <a href="${pageContext.request.contextPath}/register" class="process-step-card" title="Navigate to Profile Completion">
                <div class="step-number">2</div>
                <div class="step-title">Profile Completion</div>
                <p class="step-desc">Detailed academic profile submission including Roll Number, Department, CGPA, and resume profile links.</p>
            </a>

            <a href="${pageContext.request.contextPath}/jobs" class="process-step-card" title="Navigate to Job Eligibility &amp; Opportunities">
                <div class="step-number">3</div>
                <div class="step-title">Eligibility Check</div>
                <p class="step-desc">Automatic server-side validation against job criteria: minimum CGPA cut-off, department, and deadline.</p>
            </a>

            <a href="${pageContext.request.contextPath}/applications" class="process-step-card" title="Navigate to Opportunity Application &amp; Tracking">
                <div class="step-number">4</div>
                <div class="step-title">Opportunity Application</div>
                <p class="step-desc">One-click application submission with duplicate prevention and real-time status tracking.</p>
            </a>

            <a href="${pageContext.request.contextPath}/shortlist" class="process-step-card" title="Navigate to Candidate Shortlisting">
                <div class="step-number">5</div>
                <div class="step-title">Candidate Shortlisting</div>
                <p class="step-desc">Placement coordinators review eligible applicant pools and approve candidates for interview drives.</p>
            </a>

            <a href="${pageContext.request.contextPath}/interviews" class="process-step-card" title="Navigate to Dynamic Interview Rounds Management">
                <div class="step-number">6</div>
                <div class="step-title">Interview Rounds</div>
                <p class="step-desc">Configurable multi-round evaluations: Aptitude, Coding, Technical 1/2, and HR rounds with history logging.</p>
            </a>

            <a href="${pageContext.request.contextPath}/applications" class="process-step-card" title="Navigate to Application Management &amp; Final Selection">
                <div class="step-number">7</div>
                <div class="step-title">Final Selection</div>
                <p class="step-desc">Authorized placement officer records final SELECTION or REJECTION determination with feedback remarks.</p>
            </a>

            <a href="${pageContext.request.contextPath}/interviews" class="process-step-card" title="Navigate to Interview Evaluation PDF Report">
                <div class="step-number">8</div>
                <div class="step-title">Automatic PDF Report</div>
                <p class="step-desc">Instant generation of official signed evaluation reports ready for viewing and downloadable PDF archival.</p>
            </a>
        </div>
    </section>

    <!-- About Placement Cell Section -->
    <section class="portal-container" id="about">
        <div class="portal-card" style="padding: 40px; background: linear-gradient(to right, #ffffff, #f8fafc);">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 40px; align-items: center;">
                <div>
                    <span style="font-size: 12px; font-weight: 700; color: var(--accent-gold); text-transform: uppercase; letter-spacing: 0.08em;">
                        About Our Mission
                    </span>
                    <h2 style="font-size: 26px; margin-top: 6px; margin-bottom: 16px;">
                        Training &amp; Placement Cell
                    </h2>
                    <p style="color: var(--text-main); line-height: 1.7; margin-bottom: 16px;">
                        The Training and Placement Cell at Centurion University of Technology and Management functions as the central liaison between academia and industry. Our dedicated team of coordinators works throughout the academic year to empower students with technical competences, industry readiness, and verified hiring pipelines.
                    </p>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-top: 20px;">
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span style="color: var(--success); font-weight: bold;">✔</span>
                            <span style="font-size: 14px; font-weight: 600;">Technical Skill Workshops</span>
                        </div>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span style="color: var(--success); font-weight: bold;">✔</span>
                            <span style="font-size: 14px; font-weight: 600;">Mock Interview Bootcamps</span>
                        </div>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span style="color: var(--success); font-weight: bold;">✔</span>
                            <span style="font-size: 14px; font-weight: 600;">Summer Internship Drives</span>
                        </div>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span style="color: var(--success); font-weight: bold;">✔</span>
                            <span style="font-size: 14px; font-weight: 600;">Corporate Mentorship</span>
                        </div>
                    </div>
                </div>

                <div style="background: var(--bg-subtle); padding: 30px; border-radius: var(--radius-lg); border: 1px solid var(--border-light);">
                    <h3 style="font-size: 18px; margin-bottom: 14px; color: var(--primary-navy);">Student Career Support</h3>
                    <p style="font-size: 14px; color: var(--text-muted); line-height: 1.6;">
                        Are you a student preparing for campus recruitment? Ensure your profile is up to date with verified CGPA and resume details before upcoming drive deadlines.
                    </p>
                    <div style="margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary"><i class="fa-solid fa-user-graduate" style="margin-right: 6px;"></i> CUTM Student Register</a>
                        <a href="${pageContext.request.contextPath}/external/register" class="btn btn-accent"><i class="fa-solid fa-university" style="margin-right: 6px;"></i> External Student Register</a>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline"><i class="fa-solid fa-right-to-bracket" style="margin-right: 6px;"></i> Student Login</a>
                        <a href="${pageContext.request.contextPath}/jobs" class="btn btn-outline"><i class="fa-solid fa-briefcase" style="margin-right: 6px;"></i> View Open Drives</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Chart Scripts (Local & Offline Capable) -->
    <script src="${pageContext.request.contextPath}/assets/js/chart.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/placement-analytics-data.js"></script>

    <!-- Institutional Footer -->
    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
