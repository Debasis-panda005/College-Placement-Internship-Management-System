
<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.internship.Internship" %>
<%!
private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;")
            .replace("'", "&#39;");
}
%>
<%
List<Internship> internships =
    (List<Internship>) request.getAttribute("internships");
String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Manage Internships</title>
<style>
body{font-family:Arial;background:#f4f6f9;margin:0;padding:25px;color:#222}
main{max-width:1200px;margin:auto}
.panel{background:white;padding:20px;margin:20px 0;border-radius:9px}
.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}
input,textarea,select{box-sizing:border-box;width:100%;padding:11px;border:1px solid #ccc;border-radius:5px}
button,.back{display:inline-block;padding:10px 14px;background:#2563eb;color:white;border:0;border-radius:5px;text-decoration:none;cursor:pointer}
.delete{background:#dc2626}
.table-wrap{overflow-x:auto}
table{width:100%;border-collapse:collapse;background:white}
th,td{text-align:left;padding:11px;border-bottom:1px solid #ddd}
th{background:#eef2ff}
@media(max-width:650px){.grid{grid-template-columns:1fr}body{padding:12px}}
</style>
</head>
<body>
<main>
<h1>Manage Internships</h1>
<p><a class="back" href="<%=ctx%>/admin/dashboard">Back to Dashboard</a></p>

<section class="panel">
<h2>Add Internship</h2>
<form method="post" action="<%=ctx%>/internships">
<input type="hidden" name="action" value="add">
<div class="grid">
<input name="title" placeholder="Internship title" maxlength="255" required>
<input name="company" placeholder="Company name" maxlength="255" required>
<input name="location" placeholder="Location" maxlength="255">
<input name="stipend" placeholder="Stipend" maxlength="100">
<input name="duration" placeholder="Duration" maxlength="100">
<select name="status">
<option value="Active">Active</option>
<option value="Inactive">Inactive</option>
</select>
</div>
<p><textarea name="description" rows="4" placeholder="Description"></textarea></p>
<button type="submit">Add Internship</button>
</form>
</section>

<section class="panel">
<h2>Internship Records</h2>
<div class="table-wrap">
<table>
<thead><tr><th>ID</th><th>Title</th><th>Company</th><th>Location</th>
<th>Stipend</th><th>Duration</th><th>Status</th><th>Description</th><th>Action</th></tr></thead>
<tbody>
<% if (internships == null || internships.isEmpty()) { %>
<tr><td colspan="9">No internships found.</td></tr>
<% } else { for (Internship i : internships) { %>
<tr>
<td><%=i.getId()%></td>
<td><%=esc(i.getTitle())%></td>
<td><%=esc(i.getCompany())%></td>
<td><%=esc(i.getLocation())%></td>
<td><%=esc(i.getStipend())%></td>
<td><%=esc(i.getDuration())%></td>
<td><%=esc(i.getStatus())%></td>
<td><%=esc(i.getDescription())%></td>
<td>
<form method="post" action="<%=ctx%>/internships"
 onsubmit="return confirm('Delete this internship?');">
<input type="hidden" name="action" value="delete">
<input type="hidden" name="id" value="<%=i.getId()%>">
<button class="delete" type="submit">Delete</button>
</form>
</td>
</tr>
<% } } %>
</tbody>
</table>
</div>
</section>
</main>
</body>
</html>
