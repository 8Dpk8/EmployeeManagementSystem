<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Access Denied | Employee Attendance</title>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>
</head>
<body class="emp-page">
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>
<main class="emp-container emp-form-container">
    <h1>Access Denied</h1>
    <p class="alert alert-error">
        Your role (<c:out value="${role}"/>) is not allowed to open this page.
    </p>
    <a class="btn btn-primary" href="${ctx}/dashboard">Back to Dashboard</a>
</main>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
