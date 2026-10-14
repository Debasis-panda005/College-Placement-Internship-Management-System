<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Add Internship</title>

  <style>
    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      padding: 40px 20px;
      font-family: Arial, sans-serif;
      background: #f3f6fb;
      color: #1e293b;
    }

    .form-card {
      max-width: 650px;
      margin: 0 auto;
      padding: 30px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    }

    h2 {
      margin-top: 0;
      margin-bottom: 25px;
      color: #1d4ed8;
    }

    label {
      display: block;
      margin-bottom: 7px;
      font-weight: 600;
    }

    input, textarea, select {
      width: 100%;
      padding: 10px;
      margin: 0 0 18px;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 14px;
    }

    textarea {
      min-height: 85px;
      resize: vertical;
    }

    input:focus, textarea:focus, select:focus {
      outline: none;
      border-color: #2563eb;
    }

    button {
      padding: 11px 18px;
      background: #2563eb;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }

    button:hover {
      background: #1d4ed8;
    }

    .cancel {
      margin-left: 12px;
      color: #475569;
      text-decoration: none;
    }
  </style>
</head>

<body>
<div class="form-card">

  <h2>Add Internship</h2>

  <form action="${pageContext.request.contextPath}/internships"
        method="post">

    <label>Title:</label>
    <input name="title" required maxlength="150">

    <label>Company:</label>
    <input name="company" required maxlength="150">

    <label>Location:</label>
    <input name="location">

    <label>Description:</label>
    <textarea name="description"></textarea>

    <label>Stipend:</label>
    <input name="stipend" placeholder="e.g. ₹10,000/month">

    <label>Duration:</label>
    <input name="duration" placeholder="e.g. 3 months">

    <label>Eligibility:</label>
    <textarea name="eligibility"></textarea>

    <label>Application Deadline:</label>
    <input type="date" name="applicationDeadline">

    <label>Status:</label>
    <select name="status">
      <option value="Active">Active</option>
      <option value="Closed">Closed</option>
    </select>

    <button type="submit">Save Internship</button>

    <a class="cancel"
       href="${pageContext.request.contextPath}/internships">
      Cancel
    </a>
  </form>

</div>
</body>
</html>
