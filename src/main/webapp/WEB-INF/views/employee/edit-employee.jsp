<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Employee | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container emp-form-container">
    <div class="emp-header">
        <h1>Edit Employee</h1>
        <a class="btn btn-secondary" href="${ctx}/employee/list">Back to List</a>
    </div>

    <form:form action="${ctx}/employee/save" method="post" modelAttribute="employee" cssClass="emp-form">
        <form:hidden path="id"/>
        <div class="form-grid">
            <div class="form-field">
                <label for="employeeCode">Employee Code *</label>
                <form:input path="employeeCode" maxlength="20" required="required"/>
                <form:errors path="employeeCode" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="name">Name *</label>
                <form:input path="name" maxlength="100" required="required"/>
                <form:errors path="name" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="email">Email *</label>
                <form:input path="email" type="email" maxlength="100" required="required"/>
                <form:errors path="email" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="phone">Phone</label>
                <form:input path="phone" type="tel" maxlength="20"/>
                <form:errors path="phone" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="department">Department *</label>
                <form:input path="department" maxlength="50" required="required"/>
                <form:errors path="department" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="designation">Designation *</label>
                <form:input path="designation" maxlength="50" required="required"/>
                <form:errors path="designation" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="salary">Salary *</label>
                <form:input path="salary" type="number" step="0.01" min="0" required="required"/>
                <form:errors path="salary" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="joiningDate">Joining Date *</label>
                <form:input path="joiningDate" type="date" required="required"/>
                <form:errors path="joiningDate" cssClass="field-error"/>
            </div>
            <div class="form-field">
                <label for="status">Status *</label>
                <form:select path="status" items="${statuses}" required="required"/>
                <form:errors path="status" cssClass="field-error"/>
            </div>
        </div>
        <div class="form-buttons">
            <button class="btn btn-primary" type="submit">Update Employee</button>
            <a class="btn btn-secondary" href="${ctx}/employee/list">Cancel</a>
        </div>
    </form:form>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
