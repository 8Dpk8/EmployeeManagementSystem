<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Payroll | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container">
    <div class="emp-header">
        <h1>Payroll</h1>
        <div class="emp-actions">
            <a class="btn btn-secondary" href="${ctx}/dashboard">Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <p class="alert alert-success"><c:out value="${message}"/></p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="alert alert-error"><c:out value="${error}"/></p>
    </c:if>

    <%-- ===== 1. Generate Payroll form ===== --%>
    <h2 class="section-title">Generate Payroll</h2>
    <c:choose>
        <c:when test="${empty employees}">
            <p class="emp-empty">No active employees. <a href="${ctx}/employee/add">Add an employee</a> first.</p>
        </c:when>
        <c:otherwise>
            <form:form action="${ctx}/payroll/generate" method="post" modelAttribute="payroll" cssClass="emp-form">
                <div class="form-grid">
                    <div class="form-field">
                        <label for="employeeId">Employee *</label>
                        <form:select path="employeeId" required="required">
                            <form:option value="" label="-- Select employee --"/>
                            <c:forEach var="emp" items="${employees}">
                                <%-- data-salary lets the JS below pre-fill Basic Salary from the employee record --%>
                                <form:option value="${emp.id}" data-salary="${emp.salary}"><c:out value="${emp.employeeCode} - ${emp.name}"/></form:option>
                            </c:forEach>
                        </form:select>
                        <form:errors path="employeeId" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="month">Month *</label>
                        <form:select path="month" required="required">
                            <c:forEach var="m" items="${months}">
                                <form:option value="${m.value}" label="${m}"/>
                            </c:forEach>
                        </form:select>
                        <form:errors path="month" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="year">Year *</label>
                        <form:input path="year" type="number" min="2000" max="2100" required="required"/>
                        <form:errors path="year" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="basicSalary">Basic Salary *</label>
                        <form:input path="basicSalary" type="number" step="0.01" min="0" required="required"/>
                        <form:errors path="basicSalary" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="bonus">Bonus</label>
                        <form:input path="bonus" type="number" step="0.01" min="0"/>
                        <form:errors path="bonus" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="deduction">Deduction</label>
                        <form:input path="deduction" type="number" step="0.01" min="0"/>
                        <form:errors path="deduction" cssClass="field-error"/>
                    </div>
                    <div class="form-field">
                        <label for="netPreview">Net Salary (Basic + Bonus - Deduction)</label>
                        <input id="netPreview" type="text" readonly value="0.00">
                    </div>
                </div>
                <div class="form-buttons">
                    <button class="btn btn-primary" type="submit">Generate Payroll</button>
                </div>
            </form:form>
        </c:otherwise>
    </c:choose>

    <%-- ===== 2. Filter + Payroll table ===== --%>
    <h2 class="section-title">Payroll Records</h2>
    <form method="get" action="${ctx}/payroll/list" class="filter-bar">
        <label for="filterMonth">Month</label>
        <select id="filterMonth" name="month">
            <option value="">All</option>
            <c:forEach var="m" items="${months}">
                <option value="${m.value}" ${m.value == selectedMonth ? 'selected' : ''}>${m}</option>
            </c:forEach>
        </select>
        <label for="filterYear">Year</label>
        <input id="filterYear" name="year" type="number" min="2000" max="2100" value="${selectedYear}">
        <button class="btn btn-primary btn-small" type="submit">Filter</button>
    </form>

    <c:choose>
        <c:when test="${empty payrolls}">
            <p class="emp-empty">No payroll records found.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="emp-table">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Month</th>
                        <th>Year</th>
                        <th class="num">Basic</th>
                        <th class="num">Bonus</th>
                        <th class="num">Deduction</th>
                        <th class="num">Net Salary</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${payrolls}">
                        <tr>
                            <td><c:out value="${p.id}"/></td>
                            <td><c:out value="${p.employee.employeeCode}"/></td>
                            <td><c:out value="${p.employee.name}"/></td>
                            <td><c:out value="${months[p.month - 1]}"/></td>
                            <td><c:out value="${p.year}"/></td>
                            <td class="num"><c:out value="${p.basicSalary}"/></td>
                            <td class="num"><c:out value="${p.bonus}"/></td>
                            <td class="num"><c:out value="${p.deduction}"/></td>
                            <td class="num"><strong><c:out value="${p.netSalary}"/></strong></td>
                            <td class="row-actions">
                                <a class="btn btn-small btn-secondary"
                                   href="${ctx}/download/salarySlip/${p.employee.id}?month=${p.month}&year=${p.year}">PDF Slip</a>
                                <form action="${ctx}/payroll/delete/${p.id}" method="post"
                                      onsubmit="return confirm('Delete this payroll record?');">
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

<script>
    // Live preview only - the server recalculates Net Salary when saving
    (function () {
        var employee = document.getElementById('employeeId');
        var basic = document.getElementById('basicSalary');
        var bonus = document.getElementById('bonus');
        var deduction = document.getElementById('deduction');
        var net = document.getElementById('netPreview');
        if (!employee) return;

        function num(input) { return parseFloat(input.value) || 0; }
        function updateNet() { net.value = (num(basic) + num(bonus) - num(deduction)).toFixed(2); }

        employee.addEventListener('change', function () {
            var salary = employee.options[employee.selectedIndex].getAttribute('data-salary');
            if (salary) basic.value = salary;
            updateNet();
        });
        [basic, bonus, deduction].forEach(function (el) { el.addEventListener('input', updateNet); });
        updateNet();
    })();
</script>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
