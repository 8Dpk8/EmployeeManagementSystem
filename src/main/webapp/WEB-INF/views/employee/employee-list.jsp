<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Employees | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container">
    <div class="emp-header">
        <h1>Employees</h1>
        <div class="emp-actions">
            <a class="btn btn-secondary" href="${ctx}/dashboard">Dashboard</a>
            <a class="btn btn-primary" href="${ctx}/employee/add">+ Add Employee</a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <p class="alert alert-success"><c:out value="${message}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${empty employees}">
            <p class="emp-empty">No employees found. Click "Add Employee" to create one.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="emp-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Department</th>
                        <th>Designation</th>
                        <th class="num">Salary</th>
                        <th>Joining Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="emp" items="${employees}">
                        <tr>
                            <td><c:out value="${emp.id}"/></td>
                            <td><c:out value="${emp.employeeCode}"/></td>
                            <td><c:out value="${emp.name}"/></td>
                            <td><c:out value="${emp.email}"/></td>
                            <td><c:out value="${emp.phone}"/></td>
                            <td><c:out value="${emp.department}"/></td>
                            <td><c:out value="${emp.designation}"/></td>
                            <td class="num"><fmt:formatNumber value="${emp.salary}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td><c:out value="${emp.joiningDate}"/></td>
                            <td>
                                <span class="badge badge-${emp.status == 'ACTIVE' ? 'active' : 'inactive'}">
                                    <c:out value="${emp.status}"/>
                                </span>
                            </td>
                            <td class="row-actions">
                                <a class="btn btn-small btn-secondary" href="${ctx}/employee/edit/${emp.id}">Edit</a>
                                <form action="${ctx}/employee/delete/${emp.id}" method="post"
                                      onsubmit="return confirm('Delete this employee?');">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                    <button class="btn btn-small btn-danger" type="submit">Delete</button>
                                </form>
                            </td>
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
