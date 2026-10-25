
<%@ page import="com.college.placement.student.Student" %>
<%!
private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
}
%>
<%
Student student = (Student) request.getAttribute("student");

if (student == null) {
    response.sendError(
        HttpServletResponse.SC_NOT_FOUND,
        "Student not found."
    );
    return;
}

String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Student</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            margin: 0;
            padding: 25px;
            color: #222;
        }

        main {
            max-width: 700px;
            margin: auto;
        }

        .panel {
            background: white;
            padding: 24px;
            margin-top: 20px;
            border-radius: 9px;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 14px;
        }

        input {
            width: 100%;
            box-sizing: border-box;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        button, .back {
            display: inline-block;
            padding: 11px 15px;
            border: 0;
            border-radius: 5px;
            color: white;
            background: #2563eb;
            text-decoration: none;
            cursor: pointer;
        }

        .back {
            background: #64748b;
        }

        @media (max-width: 600px) {
            body { padding: 12px; }
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>

<body>
<main>
    <h1>Edit Student</h1>

    <a class="back" href="<%=ctx%>/students">
        Back to Student Management
    </a>

    <section class="panel">
        <form method="post" action="<%=ctx%>/students">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id"
                   value="<%=student.getId()%>">

            <div class="grid">
                <input type="text" name="name"
                       placeholder="Student name"
                       maxlength="150"
                       value="<%=esc(student.getName())%>" required>

                <input type="email" name="email"
                       placeholder="Email"
                       maxlength="150"
                       value="<%=esc(student.getEmail())%>" required>

                <input type="text" name="phone"
                       placeholder="Phone"
                       maxlength="30"
                       value="<%=esc(student.getPhone())%>">

                <input type="text" name="department"
                       placeholder="Department"
                       maxlength="100"
                       value="<%=esc(student.getDepartment())%>">

                <input type="number" name="graduationYear"
                       placeholder="Graduation year"
                       min="1900" max="2200"
                       value="<%=student.getGraduationYear()%>"
                       required>
            </div>

            <p>
                <button type="submit">Save Changes</button>
            </p>
        </form>
    </section>
</main>
</body>
</html>
