<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mark Attendance | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container emp-form-container">
    <div class="emp-header">
        <h1>Mark Attendance</h1>
        <div class="emp-actions">
            <a class="btn btn-secondary" href="${ctx}/dashboard">Dashboard</a>
            <a class="btn btn-secondary" href="${ctx}/attendance/list">Attendance Records</a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <p class="alert alert-success"><c:out value="${message}"/></p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="alert alert-error"><c:out value="${error}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${empty employees}">
            <p class="emp-empty">No active employees. <a href="${ctx}/employee/add">Add an employee</a> first.</p>
        </c:when>
        <c:otherwise>
            <%-- One form, two buttons: each button posts to a different URL via formaction --%>
            <form method="post" action="${ctx}/attendance/check-in" class="emp-form">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="form-grid">
                    <div class="form-field">
                        <label for="employeeId">Employee *</label>
                        <select id="employeeId" name="employeeId" required>
                            <option value="">-- Select employee --</option>
                            <c:forEach var="emp" items="${employees}">
                                <option value="${emp.id}"><c:out value="${emp.employeeCode} - ${emp.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-field">
                        <label for="date">Date *</label>
                        <input id="date" name="date" type="date" value="${today}" required>
                    </div>
                    <div class="form-field">
                        <label for="time">Time *</label>
                        <input id="time" name="time" type="time" value="${now}" required>
                    </div>
                </div>
                <div class="form-buttons">
                    <button class="btn btn-primary" type="submit" formaction="${ctx}/attendance/check-in">Check In</button>
                    <button class="btn btn-danger" type="submit" formaction="${ctx}/attendance/check-out">Check Out</button>
                </div>
            </form>
        </c:otherwise>
    </c:choose>

    <h2 class="section-title">Today's Attendance (<c:out value="${today}"/>)</h2>
    <c:choose>
        <c:when test="${empty todayAttendances}">
            <p class="emp-empty">Nobody has checked in today yet.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="emp-table">
                    <thead>
                    <tr>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Check In</th>
                        <th>Check Out</th>
                        <th class="num">Working Hours</th>
                        <th>Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="a" items="${todayAttendances}">
                        <tr>
                            <td><c:out value="${a.employee.employeeCode}"/></td>
                            <td><c:out value="${a.employee.name}"/></td>
                            <td><c:out value="${a.checkInFormatted}"/></td>
                            <td><c:out value="${a.checkOutFormatted}"/></td>
                            <td class="num">
                                <c:choose>
                                    <c:when test="${a.workingHours != null}"><fmt:formatNumber value="${a.workingHours}" minFractionDigits="2" maxFractionDigits="2"/></c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </td>
                            <td><span class="badge badge-${a.status == 'PRESENT' ? 'active' : 'inactive'}"><c:out value="${a.status}"/></span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
