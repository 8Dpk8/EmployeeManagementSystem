<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Attendance Records | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container">
    <div class="emp-header">
        <h1>Attendance Records</h1>
        <div class="emp-actions">
            <a class="btn btn-secondary" href="${ctx}/dashboard">Dashboard</a>
            <a class="btn btn-primary" href="${ctx}/attendance/mark">Mark Attendance</a>
        </div>
    </div>

    <form method="get" action="${ctx}/attendance/list" class="filter-bar">
        <label for="date">Date</label>
        <input id="date" name="date" type="date" value="${selectedDate}">
        <button class="btn btn-primary btn-small" type="submit">Filter</button>
        <a class="btn btn-secondary btn-small" href="${ctx}/attendance/list">Show All</a>
    </form>

    <c:choose>
        <c:when test="${empty attendances}">
            <p class="emp-empty">No attendance records found.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="emp-table">
                    <thead>
                    <tr>
                        <th>Date</th>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Department</th>
                        <th>Check In</th>
                        <th>Check Out</th>
                        <th class="num">Working Hours</th>
                        <th>Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="a" items="${attendances}">
                        <tr>
                            <td><c:out value="${a.date}"/></td>
                            <td><c:out value="${a.employee.employeeCode}"/></td>
                            <td><c:out value="${a.employee.name}"/></td>
                            <td><c:out value="${a.employee.department}"/></td>
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
