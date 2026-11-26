<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
    <link rel="stylesheet" href="${ctx}/css/dashboard.css">
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="dash-main">
    <div class="mb-4">
        <h1 class="h3 mb-1">Welcome, <c:out value="${currentUser}"/></h1>
        <p class="text-secondary mb-0">Role: <c:out value="${role}"/> &middot; Today is <c:out value="${today}"/></p>
    </div>

    <%-- ===== 1. Summary cards (isAdmin comes from navbar.jsp; non-admins see the counts but the employee cards are not links) ===== --%>
    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
            <div class="card dash-card card-blue h-100">
                <div class="card-body">
                    <div class="dash-icon"><i class="bi bi-people-fill"></i></div>
                    <div>
                        <div class="dash-card-label">Total Employees</div>
                        <div class="dash-card-value"><c:out value="${totalEmployees}"/></div>
                    </div>
                    <%-- stretched-link makes the whole card clickable --%>
                    <c:if test="${isAdmin}"><a class="stretched-link" href="${ctx}/employee/list" aria-label="View employees"></a></c:if>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card dash-card card-green h-100">
                <div class="card-body">
                    <div class="dash-icon"><i class="bi bi-person-check-fill"></i></div>
                    <div>
                        <div class="dash-card-label">Present Today</div>
                        <div class="dash-card-value"><c:out value="${presentToday}"/></div>
                    </div>
                    <a class="stretched-link" href="${ctx}/attendance/list?date=${today}" aria-label="View today's attendance"></a>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card dash-card card-orange h-100">
                <div class="card-body">
                    <div class="dash-icon"><i class="bi bi-hourglass-split"></i></div>
                    <div>
                        <div class="dash-card-label">Pending Leaves</div>
                        <div class="dash-card-value"><c:out value="${pendingLeaves}"/></div>
                    </div>
                    <a class="stretched-link" href="${ctx}/leave/list?status=PENDING" aria-label="View pending leaves"></a>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card dash-card card-purple h-100">
                <div class="card-body">
                    <div class="dash-icon"><i class="bi bi-diagram-3-fill"></i></div>
                    <div>
                        <div class="dash-card-label">Departments</div>
                        <div class="dash-card-value"><c:out value="${departments}"/></div>
                    </div>
                    <c:if test="${isAdmin}"><a class="stretched-link" href="${ctx}/employee/list" aria-label="View employees"></a></c:if>
                </div>
            </div>
        </div>
    </div>
    <%-- ===== 2. Charts. dashboard.js reads data-chart-url, fetches the JSON and draws each <canvas> ===== --%>
    <div id="dashboardCharts" class="row g-3" data-chart-url="${ctx}/dashboard/chart-data">
        <div class="col-xl-8">
            <div class="card chart-card h-100">
                <div class="card-header"><i class="bi bi-graph-up me-2"></i>Attendance Trend <small class="text-secondary">(last 7 days)</small></div>
                <div class="card-body">
                    <div class="chart-box"><canvas id="attendanceChart" aria-label="Attendance trend chart" role="img"></canvas></div>
                    <p class="chart-empty d-none">No attendance recorded in the last 7 days.</p>
                </div>
            </div>
        </div>
        <div class="col-xl-4">
            <div class="card chart-card h-100">
                <div class="card-header"><i class="bi bi-pie-chart me-2"></i>Department Distribution</div>
                <div class="card-body">
                    <div class="chart-box"><canvas id="departmentChart" aria-label="Department distribution chart" role="img"></canvas></div>
                    <p class="chart-empty d-none">No employees yet.</p>
                </div>
            </div>
        </div>
        <div class="col-xl-8">
            <div class="card chart-card h-100">
                <div class="card-header"><i class="bi bi-bar-chart me-2"></i>Leave Trend <small class="text-secondary">(last 6 months)</small></div>
                <div class="card-body">
                    <div class="chart-box"><canvas id="leaveChart" aria-label="Leave trend chart" role="img"></canvas></div>
                    <p class="chart-empty d-none">No leave requests in the last 6 months.</p>
                </div>
            </div>
        </div>

        <%-- ===== 3. Quick links ===== --%>
        <div class="col-xl-4">
            <div class="card chart-card h-100">
                <div class="card-header"><i class="bi bi-lightning-charge me-2"></i>Quick Actions</div>
                <div class="list-group list-group-flush">
                    <a class="list-group-item list-group-item-action" href="${ctx}/attendance/mark"><i class="bi bi-clock me-2"></i>Mark Attendance</a>
                    <a class="list-group-item list-group-item-action" href="${ctx}/attendance/list"><i class="bi bi-list-check me-2"></i>Attendance Records</a>
                    <a class="list-group-item list-group-item-action" href="${ctx}/leave/apply"><i class="bi bi-calendar-plus me-2"></i>Apply Leave</a>
                    <a class="list-group-item list-group-item-action" href="${ctx}/leave/list"><i class="bi bi-calendar2-week me-2"></i>Leave Requests</a>
                    <c:if test="${isAdmin}">
                        <a class="list-group-item list-group-item-action" href="${ctx}/employee/add"><i class="bi bi-person-plus me-2"></i>Add Employee</a>
                        <a class="list-group-item list-group-item-action" href="${ctx}/payroll/list"><i class="bi bi-cash-stack me-2"></i>Generate Payroll</a>
                        <a class="list-group-item list-group-item-action" href="${ctx}/report"><i class="bi bi-file-earmark-bar-graph me-2"></i>Reports</a>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
    <div id="chartError" class="alert alert-error mt-3 d-none">Could not load chart data. Please refresh the page.</div>
</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
<script src="${ctx}/webjars/chart.js/dist/chart.umd.min.js"></script>
<script src="${ctx}/js/chart.js"></script>
<script src="${ctx}/js/dashboard.js"></script>
</body>
</html>