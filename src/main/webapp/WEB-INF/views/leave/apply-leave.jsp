<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Apply Leave | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container emp-form-container">
    <div class="emp-header">
        <h1>Apply Leave</h1>
        <div class="emp-actions">
            <a class="btn btn-secondary" href="${ctx}/dashboard">Dashboard</a>
            <a class="btn btn-secondary" href="${ctx}/leave/list">Leave Requests</a>
        </div>
    </div>

    <c:if test="${not empty error}">
        <p class="alert alert-error"><c:out value="${error}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${empty employees}">
            <p class="emp-empty">No active employees. <a href="${ctx}/employee/add">Add an employee</a> first.</p>
        </c:when>
        <c:otherwise>
            <form:form action="${ctx}/leave/apply" method="post" modelAttribute="leave" cssClass="emp-form">
                <div class="form-grid">
                    <div class="form-field">
                        <label for="employeeId">Employee *</label>
                        <form:select path="employeeId" required="required">
                            <form:option value="" label="-- Select employee --"/>
                            <c:forEach var="emp" items="${employees}">
                                <form:option value="${emp.id}"><c:out value="${emp.employeeCode} - ${emp.name}"/></form:option>
                            </c:forEach>
                        </form:select>
                        <form:errors path="employeeId" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="leaveType">Leave Type *</label>
                        <form:select path="leaveType" required="required">
                            <form:option value="" label="-- Select type --"/>
                            <form:options items="${leaveTypes}"/>
                        </form:select>
                        <form:errors path="leaveType" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="startDate">Start Date *</label>
                        <form:input path="startDate" type="date" required="required"/>
                        <form:errors path="startDate" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="endDate">End Date *</label>
                        <form:input path="endDate" type="date" required="required"/>
                        <form:errors path="endDate" cssClass="field-error"/>
                    </div>
                </div>
                <div class="form-field form-field-full">
                    <label for="reason">Reason *</label>
                    <form:textarea path="reason" rows="4" maxlength="500" required="required"/>
                    <form:errors path="reason" cssClass="field-error"/>
                </div>
                <div class="form-buttons">
                    <button class="btn btn-primary" type="submit">Apply Leave</button>
                    <a class="btn btn-secondary" href="${ctx}/leave/list">Cancel</a>
                </div>
            </form:form>
        </c:otherwise>
    </c:choose>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
