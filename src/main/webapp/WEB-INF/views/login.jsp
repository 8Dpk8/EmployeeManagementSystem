<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | Employee Attendance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-page">
<main class="login-card">
    <h1>Employee Management System</h1>
    <c:if test="${not empty error}"><p class="login-error"><c:out value="${error}"/></p></c:if>
    <c:if test="${not empty message}"><p class="login-message"><c:out value="${message}"/></p></c:if>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="login-field">
            <label for="username">Username or Email</label>
            <input id="username" name="username" type="text" autocomplete="username" required>
        </div>
        <div class="login-field">
            <label for="password">Password</label>
            <input id="password" name="password" type="password" autocomplete="current-password" required>
        </div>
        <label class="remember-me" for="remember-me">
            <input id="remember-me" name="remember-me" type="checkbox">
            <span>Remember Me</span>
        </label>
        <button class="login-button" type="submit">Login</button>
    </form>
    <a class="forgot-password" href="${pageContext.request.contextPath}/forgot-password">Forgot Password?</a>
</main>
</body>
</html>