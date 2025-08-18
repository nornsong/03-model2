<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>404 - 페이지를 찾을 수 없습니다</title>
    <style>
        body { font-family: sans-serif; background: #f5f5f5; padding: 20px; }
        h1 { color: #e74c3c; }
        pre { background: #fff; border: 1px solid #ccc; padding: 10px; }
    </style>
</head>
<body>
    <h1>404 - 페이지를 찾을 수 없습니다</h1>
    <p>요청하신 페이지가 존재하지 않습니다.</p>
    <hr>
    <h3>에러 정보</h3>
    <p><b>Status Code:</b> <%= request.getAttribute("javax.servlet.error.status_code") %></p>
    <p><b>Request URI:</b> <%= request.getAttribute("javax.servlet.error.request_uri") %></p>
    <p><b>Servlet Name:</b> <%= request.getAttribute("javax.servlet.error.servlet_name") %></p>
</body>
</html>
