<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>500 - 서버 내부 오류</title>
    <style>
        body { font-family: sans-serif; background: #fff0f0; padding: 20px; }
        h1 { color: #c0392b; }
        pre { background: #fff; border: 1px solid #ccc; padding: 10px; white-space: pre-wrap; }
    </style>
</head>
<body>
    <h1>500 - 서버 내부 오류</h1>
    <p>요청을 처리하는 중 오류가 발생했습니다.</p>
    <hr>
    <h3>에러 메시지</h3>
    <p><b>Message:</b> <%= exception.getMessage() %></p>

    <h3>스택 트레이스</h3>
    <pre>
<%
    if (exception != null) {
        exception.printStackTrace(new java.io.PrintWriter(out));
    }
%>
    </pre>
</body>
</html>
