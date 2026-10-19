<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Add Internship</title>
  <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="compact-page">

<div class="compact-container">

<div class="details-page-header">
  <h1>Add New Internship</h1>
</div>

<form method="post"
      action="${pageContext.request.contextPath}/internships"
      class="add-internship-form">

  <input type="hidden" name="action" value="add">

  <div class="form-grid">
    <div class="form-group">
      <label for="title">Internship Title *</label>
      <input type="text"
             id="title"
             name="title"
             required
             maxlength="150">
    </div>

    <div class="form-group">
      <label for="company">Company *</label>
      <input type="text"
             id="company"
             name="company"
             required
             maxlength="150">
    </div>

    <div class="form-group">
      <label for="location">Location</label>
      <input type="text"
             id="location"
             name="location"
             maxlength="150">
    </div>

    <div class="form-group">
      <label for="applicationDeadline">Application Deadline</label>
      <input type="date"
             id="applicationDeadline"
             name="applicationDeadline">
    </div>

    <div class="form-group">
      <label for="stipend">Stipend</label>
      <input type="text"
             id="stipend"
             name="stipend"
             placeholder="e.g. ₹10,000/month">
    </div>

    <div class="form-group">
      <label for="duration">Duration</label>
      <input type="text"
             id="duration"
             name="duration"
             placeholder="e.g. 3 months">
    </div>

    <div class="form-group">
      <label for="status">Status</label>
      <select id="status" name="status">
        <option value="Active" selected>Active</option>
        <option value="Inactive">Inactive</option>
      </select>
    </div>

    <div class="form-group">
      <label for="eligibility">Eligibility</label>
      <textarea id="eligibility"
                name="eligibility"
                placeholder="e.g. B.Tech / MCA"></textarea>
    </div>

    <div class="form-group form-group-full">
      <label for="description">Description</label>
      <textarea id="description"
                name="description"></textarea>
    </div>
  </div>

  <div class="form-actions">
    <button type="submit">Save Internship</button>
    <a class="btn btn-secondary"
       href="${pageContext.request.contextPath}/internships">
      Cancel
    </a>
  </div>

</form>

</div>

</body>
</html>
