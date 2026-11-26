<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reports | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container">
    <div class="emp-header">
        <h1>Reports</h1>
    </div>
    <p class="emp-empty">Reports module is coming soon. This page is visible to ADMIN only.</p>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
