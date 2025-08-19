<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>게시글 수정</title>
</head>
<body>
    <h1>게시글 수정</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
    </c:if>

    <form action="front?command=boardUpdate&id=${board.id}" method="post">
        <p>
            <label>번호:</label>
            <input type="text" value="${board.id}" readonly>
        </p>
        <p>
            <label>제목:</label>
            <input type="text" name="title" value="${board.title}" required>
        </p>
        <p>
            <label>작성자:</label>
            <input type="text" value="${board.author}" readonly>
        </p>
        <p>
            <label>내용:</label>
            <textarea name="content" rows="5" cols="50">${board.content}</textarea>
        </p>
        <p>
            <input type="submit" value="수정">
            <a href="front?command=boardView&id=${board.id}">취소</a>
            <a href="front?command=boardList">목록</a>
        </p>
    </form>
</body>
</html>
