<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Leave Requests | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container">
    <div class="emp-header">
        <h1>Leave Requests</h1>
        <div class="emp-actions">
            <a class="btn btn-secondary" href="${ctx}/dashboard">Dashboard</a>
            <a class="btn btn-primary" href="${ctx}/leave/apply">+ Apply Leave</a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <p class="alert alert-success"><c:out value="${message}"/></p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="alert alert-error"><c:out value="${error}"/></p>
    </c:if>

    <form method="get" action="${ctx}/leave/list" class="filter-bar">
        <label for="status">Status</label>
        <select id="status" name="status">
            <option value="">All</option>
            <c:forEach var="s" items="${statuses}">
                <option value="${s}" ${s == selectedStatus ? 'selected' : ''}>${s}</option>
            </c:forEach>
        </select>
        <button class="btn btn-primary btn-small" type="submit">Filter</button>
    </form>

    <c:choose>
        <c:when test="${empty leaves}">
            <p class="emp-empty">No leave requests found.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="emp-table">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Type</th>
                        <th>From</th>
                        <th>To</th>
                        <th class="num">Days</th>
                        <th>Reason</th>
                        <th>Status</th>
                        <c:if test="${canApprove}"><th>Actions</th></c:if>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="l" items="${leaves}">
                        <tr>
                            <td><c:out value="${l.id}"/></td>
                            <td><c:out value="${l.employee.employeeCode}"/></td>
                            <td><c:out value="${l.employee.name}"/></td>
                            <td><c:out value="${l.leaveType}"/></td>
                            <td><c:out value="${l.startDate}"/></td>
                            <td><c:out value="${l.endDate}"/></td>
                            <td class="num"><c:out value="${l.days}"/></td>
                            <td class="cell-wrap"><c:out value="${l.reason}"/></td>
                            <td><span class="badge badge-${l.status == 'APPROVED' ? 'active' : (l.status == 'PENDING' ? 'pending' : 'rejected')}"><c:out value="${l.status}"/></span></td>
                            <c:if test="${canApprove}">
                                <td class="row-actions">
                                    <c:if test="${l.status == 'PENDING'}">
                                        <form action="${ctx}/leave/approve/${l.id}" method="post">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <button class="btn btn-small btn-primary" type="submit">Approve</button>
                                        </form>
                                        <form action="${ctx}/leave/reject/${l.id}" method="post"
                                              onsubmit="return confirm('Reject this leave request?');">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <button class="btn btn-small btn-danger" type="submit">Reject</button>
                                        </form>
                                    </c:if>
                                </td>
                            </c:if>
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
