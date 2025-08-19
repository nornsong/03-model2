<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>게시글 삭제</title>
</head>
<body>
    <h1>게시글 삭제</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
    </c:if>

    <p>정말로 이 게시글을 삭제하시겠습니까?</p>
    <p>게시글 번호: ${id}</p>

    <p>
        <a href="front?command=boardDelete&id=${id}&confirm=true" 
           onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
        <a href="front?command=boardView&id=${id}">취소</a>
        <a href="front?command=boardList">목록</a>
    </p>
</body>
</html>
