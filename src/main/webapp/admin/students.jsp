<%@ page import="java.util.List" %>
<%@ page import="com.college.placement.student.Student" %>

<%!
private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;")
            .replace("'", "&#39;");
}
%>

<%
List<Student> students =
    (List<Student>) request.getAttribute("students");
String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Manage Students</title>

<style>
body{font-family:Arial;background:#f4f6f9;margin:0;padding:25px;color:#222}
main{max-width:1100px;margin:auto}
.panel{background:white;padding:20px;margin:20px 0;border-radius:9px}
.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}
input{padding:11px;box-sizing:border-box;width:100%;border:1px solid #ccc;border-radius:5px}
button,.back,.edit{padding:10px 14px;background:#2563eb;color:white;border:0;border-radius:5px;text-decoration:none;cursor:pointer;display:inline-block}
.delete{background:#dc2626}
.edit{background:#16a34a}
.table-wrap{overflow-x:auto}
table{width:100%;border-collapse:collapse;background:white}
th,td{text-align:left;padding:11px;border-bottom:1px solid #ddd}
th{background:#eef2ff}
.actions{display:flex;gap:7px;align-items:center;flex-wrap:wrap}
.actions form{margin:0}
@media(max-width:650px){.grid{grid-template-columns:1fr}body{padding:12px}}
</style>
</head>

<body>
<main>

<h1>Manage Students</h1>

<p>
<a class="back" href="<%=ctx%>/admin/dashboard">Back to Dashboard</a>
</p>

<section class="panel">
<h2>Add Student</h2>

<form method="post" action="<%=ctx%>/students">
<input type="hidden" name="action" value="add">

<div class="grid">
<input name="name" placeholder="Student name" maxlength="150" required>
<input type="email" name="email" placeholder="Email" maxlength="150" required>
<input name="phone" placeholder="Phone" maxlength="30">
<input name="department" placeholder="Department" maxlength="100">
<input type="number" name="graduationYear"
       placeholder="Graduation year" min="1900" max="2200" required>
</div>

<p><button type="submit">Add Student</button></p>
</form>
</section>

<section class="panel">
<h2>Student Records</h2>

<div class="table-wrap">
<table>
<thead>
<tr>
<th>ID</th>
<th>Name</th>
<th>Email</th>
<th>Phone</th>
<th>Department</th>
<th>Graduation Year</th>
<th>Action</th>
</tr>
</thead>

<tbody>
<% if (students == null || students.isEmpty()) { %>
<tr>
<td colspan="7">No students found.</td>
</tr>
<% } else {
    for (Student s : students) { %>
<tr>
<td><%=s.getId()%></td>
<td><%=esc(s.getName())%></td>
<td><%=esc(s.getEmail())%></td>
<td><%=esc(s.getPhone())%></td>
<td><%=esc(s.getDepartment())%></td>
<td><%=s.getGraduationYear()%></td>

<td>
<div class="actions">

<a class="edit"
   href="<%=ctx%>/students?action=edit&amp;id=<%=s.getId()%>">
   Edit
</a>

<form method="post" action="<%=ctx%>/students"
      onsubmit="return confirm('Delete this student record?');">
<input type="hidden" name="action" value="delete">
<input type="hidden" name="id" value="<%=s.getId()%>">
<button class="delete" type="submit">Delete</button>
</form>

</div>
</td>
</tr>
<%  }
} %>
</tbody>
</table>
</div>
</section>

</main>
</body>
</html>