<%-- Left sidebar. Included by navbar.jsp, which sets navCtx, isAdmin and role.
     Desktop (>= lg): always visible, fixed on the left.
     Mobile (< lg): hidden "offcanvas" panel opened by the menu button in navbar.jsp. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- JSPs are reached by a server-side forward, so the URL the user actually opened is in this request attribute --%>
<c:set var="currentUri" value="${requestScope['jakarta.servlet.forward.request_uri']}"/>
<c:if test="${empty currentUri}">
    <c:set var="currentUri" value="${pageContext.request.requestURI}"/>
</c:if>

<aside class="offcanvas-lg offcanvas-start app-sidebar" data-bs-theme="dark" tabindex="-1"
       id="appSidebar" aria-labelledby="appSidebarLabel">
    <div class="offcanvas-header sidebar-brand">
        <a id="appSidebarLabel" class="text-white text-decoration-none fs-5 fw-bold" href="${navCtx}/dashboard">
            <i class="bi bi-buildings me-2"></i>EMS
        </a>
        <button type="button" class="btn-close d-lg-none" data-bs-dismiss="offcanvas"
                data-bs-target="#appSidebar" aria-label="Close"></button>
    </div>

    <div class="offcanvas-body sidebar-body">
        <nav class="nav nav-pills flex-column gap-1">
            <a class="nav-link ${fn:contains(currentUri, '/dashboard') ? 'active' : ''}" href="${navCtx}/dashboard">
                <i class="bi bi-speedometer2"></i> Dashboard
            </a>
            <%-- Employees / Payroll / Reports are ADMIN only (also enforced by SecurityConfig) --%>
            <c:if test="${isAdmin}">
                <a class="nav-link ${fn:contains(currentUri, '/employee') ? 'active' : ''}" href="${navCtx}/employee/list">
                    <i class="bi bi-people"></i> Employees
                </a>
            </c:if>
            <a class="nav-link ${fn:contains(currentUri, '/attendance') ? 'active' : ''}" href="${navCtx}/attendance/mark">
                <i class="bi bi-calendar-check"></i> Attendance
            </a>
            <a class="nav-link ${fn:contains(currentUri, '/leave') ? 'active' : ''}" href="${navCtx}/leave/list">
                <i class="bi bi-airplane"></i> Leave
            </a>
            <c:if test="${isAdmin}">
                <a class="nav-link ${fn:contains(currentUri, '/payroll') ? 'active' : ''}" href="${navCtx}/payroll/list">
                    <i class="bi bi-cash-stack"></i> Payroll
                </a>
                <a class="nav-link ${fn:contains(currentUri, '/report') ? 'active' : ''}" href="${navCtx}/report">
                    <i class="bi bi-bar-chart-line"></i> Reports
                </a>
            </c:if>
        </nav>

        <%-- Spring Security logout must be a POST with the CSRF token --%>
        <form class="mt-auto pt-3 border-top border-secondary" action="${navCtx}/logout" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button class="nav-link sidebar-logout w-100 text-start" type="submit">
                <i class="bi bi-box-arrow-right"></i> Logout
            </button>
        </form>
    </div>
</aside>