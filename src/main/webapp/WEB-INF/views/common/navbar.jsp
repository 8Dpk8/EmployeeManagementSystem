<%-- Shared layout: left sidebar + top bar. Included at the top of <body> on every page with:
     <%@ include file="/WEB-INF/views/common/navbar.jsp" %>
     The logged-in user and role come from Spring Security (sec: tags), not from our own session attributes. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="navCtx" value="${pageContext.request.contextPath}"/>

<%-- isAdmin = true/false, currentUser = "admin", role = "ADMIN" (authority "ROLE_ADMIN" without the prefix).
     Spring Security 7 also adds non-role authorities like "FACTOR_PASSWORD", so only ROLE_* is used. --%>
<sec:authorize access="hasRole('ADMIN')" var="isAdmin"/>
<sec:authentication property="name" var="currentUser"/>
<sec:authentication property="authorities" var="authorities"/>
<c:forEach var="authority" items="${authorities}">
    <c:if test="${fn:startsWith(authority.authority, 'ROLE_')}">
        <c:set var="role" value="${fn:substringAfter(authority.authority, 'ROLE_')}"/>
    </c:if>
</c:forEach>

<%@ include file="/WEB-INF/views/common/sidebar.jsp" %>

<header class="app-topbar">
    <%-- Only visible on small screens: opens the sidebar --%>
    <button class="btn btn-outline-secondary d-lg-none" type="button"
            data-bs-toggle="offcanvas" data-bs-target="#appSidebar" aria-controls="appSidebar" aria-label="Open menu">
        <i class="bi bi-list"></i>
    </button>
    <span class="fw-semibold text-secondary d-none d-sm-inline">Employee Management System</span>
    <div class="ms-auto d-flex align-items-center gap-2">
        <i class="bi bi-person-circle fs-5 text-secondary"></i>
        <span class="fw-semibold"><c:out value="${currentUser}"/></span>
        <span class="badge text-bg-primary"><c:out value="${role}"/></span>
    </div>
</header>