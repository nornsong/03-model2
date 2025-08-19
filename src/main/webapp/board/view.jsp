<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시글 보기 - Model 2</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <div class="container mx-auto px-4 py-8">
        <h1 class="text-3xl font-bold text-gray-800 mb-6">게시글 보기 (Model 2)</h1>
        
        <div class="mb-4">
            <a href="front?command=boardList" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2">
                목록으로
            </a>
            <a href="front?command=boardWrite" class="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mr-2">
                글쓰기
            </a>
            <a href="front?command=boardUpdate&id=${board.id}" class="bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded mr-2">
                수정
            </a>
            <a href="front?command=boardDelete&id=${board.id}" class="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded">
                삭제
            </a>
        </div>

        <!-- 에러 메시지 표시 -->
        <c:if test="${not empty error}">
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                ${error}
            </div>
        </c:if>

        <!-- 게시글 내용 표시 -->
        <c:if test="${not empty board}">
            <div class="bg-white shadow-md rounded-lg p-6">
                <div class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">번호</label>
                        <div class="text-lg text-gray-900">${board.id}</div>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">제목</label>
                        <div class="text-lg text-gray-900">${board.title}</div>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">작성자</label>
                        <div class="text-lg text-gray-900">${board.author}</div>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">작성일</label>
                        <div class="text-lg text-gray-900">${board.createdAt}</div>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">내용</label>
                        <div class="text-lg text-gray-900 whitespace-pre-wrap">${board.content}</div>
                    </div>
                </div>
            </div>
        </c:if>
        
        <div class="mt-6 text-sm text-gray-600">
            <p>Model 2 아키텍처 - Servlet + JSP + JSTL</p>
            <p>특징: Servlet이 데이터 조회, JSP는 화면 출력, JSTL로 조건부 출력 처리</p>
        </div>
    </div>
</body>
</html>
