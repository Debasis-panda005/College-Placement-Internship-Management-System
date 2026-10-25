
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Company Management</title>

<style>
    * { box-sizing: border-box; }

    body {
        margin: 0;
        font-family: Arial, sans-serif;
        background: #f4f6f9;
        color: #1f2937;
    }

    header {
        background: #1f2937;
        color: white;
        padding: 18px 24px;
        font-size: 20px;
        font-weight: bold;
    }

    main {
        max-width: 1150px;
        margin: 30px auto;
        padding: 0 20px;
    }

    .topbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 12px;
        margin-bottom: 22px;
    }

    h1 { margin: 0 0 8px; }

    .muted { color: #64748b; font-size: 14px; }

    .panel {
        background: white;
        border: 1px solid #e1e5eb;
        border-radius: 10px;
        padding: 24px;
        margin-bottom: 24px;
        box-shadow: 0 3px 10px rgba(0,0,0,.04);
    }

    .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 16px;
    }

    label {
        display: block;
        font-size: 14px;
        font-weight: bold;
        margin-bottom: 7px;
    }

    input {
        width: 100%;
        padding: 11px;
        border: 1px solid #cbd5e1;
        border-radius: 6px;
        font-size: 14px;
    }

    input:focus {
        outline: 2px solid #bfdbfe;
        border-color: #2563eb;
    }

    .actions {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 12px;
        margin-top: 20px;
    }

    button, .button {
        display: inline-block;
        padding: 10px 15px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        text-decoration: none;
        font-size: 14px;
    }

    .primary { background: #2563eb; color: white; }
    .secondary { background: #e2e8f0; color: #1f2937; }
    .edit { color: #0757e8; }
    .delete { color: #dc2626; background: transparent; padding: 5px; }

    .table-wrap { overflow-x: auto; }

    table {
        width: 100%;
        border-collapse: collapse;
        min-width: 700px;
    }

    th, td {
        padding: 13px 12px;
        border-bottom: 1px solid #e5e7eb;
        text-align: left;
        font-size: 14px;
    }

    th { background: #f8fafc; }

    .message {
        padding: 12px 15px;
        background: #dcfce7;
        color: #166534;
        border-radius: 6px;
        margin-bottom: 20px;
    }

    .empty {
        padding: 25px;
        text-align: center;
        color: #64748b;
    }

    .inline-form { display: inline; }

    @media (max-width: 650px) {
        .form-grid { grid-template-columns: 1fr; }
        .panel { padding: 17px; }
    }
</style>
</head>

<body>
<header>College Placement &amp; Internship Management System</header>

<main>
    <div class="topbar">
        <div>
            <h1>Company Management</h1>
            <div class="muted">
                Register companies and manage placement partners.
            </div>
        </div>

        <div style="text-align: left; margin: 15px 0;">
    <a href="${pageContext.request.contextPath}/admin/dashboard"
   style="display: inline-block;
          background-color: #2563eb;
          color: #ffffff;
          padding: 10px 18px;
          text-decoration: none;
          border-radius: 5px;
          font-weight: 600;
          border: none;">
    	Back to Dashboard
	</a>
		</div>
    </div>

    <c:if test="${param.success == '1'}">
        <div class="message">
            Your company operation was processed successfully.
        </div>
    </c:if>

    <section class="panel">
        <c:choose>
            <c:when test="${not empty editCompany}">
                <h2>Edit Company</h2>
            </c:when>
            <c:otherwise>
                <h2>Add Company</h2>
            </c:otherwise>
        </c:choose>

        <form method="post"
              action="${pageContext.request.contextPath}/companies">

            <input type="hidden" name="action" value="save">
            <input type="hidden" name="id"
                   value="<c:out value='${editCompany.id}'/>">

            <div class="form-grid">
                <div>
                    <label for="name">Company Name *</label>
                    <input id="name" name="name" type="text"
                           maxlength="150" required
                           value="<c:out value='${editCompany.name}'/>">
                </div>

                <div>
                    <label for="email">Contact Email</label>
                    <input id="email" name="email" type="email"
                           maxlength="150"
                           value="<c:out value='${editCompany.email}'/>">
                </div>

                <div>
                    <label for="phone">Contact Phone</label>
                    <input id="phone" name="phone" type="tel"
                           maxlength="30"
                           value="<c:out value='${editCompany.phone}'/>">
                </div>

                <div>
                    <label for="location">Location</label>
                    <input id="location" name="location" type="text"
                           maxlength="150"
                           value="<c:out value='${editCompany.location}'/>">
                </div>

                <div>
                    <label for="website">Website</label>
                    <input id="website" name="website" type="text"
                           maxlength="255" placeholder="https://example.com"
                           value="<c:out value='${editCompany.website}'/>">
                </div>
            </div>

            <div class="actions">
                <button class="primary" type="submit">
                    <c:choose>
                        <c:when test="${not empty editCompany}">
                            Update Company
                        </c:when>
                        <c:otherwise>
                            Add Company
                        </c:otherwise>
                    </c:choose>
                </button>

                <c:if test="${not empty editCompany}">
                    <a class="button secondary"
                       href="${pageContext.request.contextPath}/companies">
                        Cancel
                    </a>
                </c:if>
            </div>
        </form>
    </section>

    <section class="panel">
        <h2>Registered Companies</h2>
        <p class="muted">Companies currently stored in the database.</p>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Company</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Location</th>
                        <th>Website</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <c:choose>
                        <c:when test="${empty companies}">
                            <tr>
                                <td colspan="7" class="empty">
                                    No companies registered yet.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="company" items="${companies}">
                                <tr>
                                    <td><c:out value="${company.id}"/></td>
                                    <td><c:out value="${company.name}"/></td>
                                    <td><c:out value="${company.email}"/></td>
                                    <td><c:out value="${company.phone}"/></td>
                                    <td><c:out value="${company.location}"/></td>
                                    <td><c:out value="${company.website}"/></td>
                                    <td>
                                        <a class="edit"
                                           href="${pageContext.request.contextPath}/companies?action=edit&amp;id=${company.id}">
                                            Edit
                                        </a>

                                        <form class="inline-form" method="post"
                                              action="${pageContext.request.contextPath}/companies"
                                              onsubmit="return confirm('Delete this company?');">
                                            <input type="hidden" name="action"
                                                   value="delete">
                                            <input type="hidden" name="id"
                                                   value="<c:out value='${company.id}'/>">
                                            <button class="delete" type="submit">
                                                Delete
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
