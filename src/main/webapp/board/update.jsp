<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시글 수정 - Model 2</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <div class="container mx-auto px-4 py-8">
        <!-- 헤더 영역 -->
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-gray-800">게시글 수정 (Model 2)</h1>
            
            <!-- 사용자 상태 표시 -->
            <div class="flex items-center space-x-4">
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <!-- 로그인한 상태 -->
                        <span class="text-gray-700 text-sm">
                            안녕하세요, <span class="font-semibold text-blue-600">${sessionScope.user.name}</span>님!
                        </span>
                        <a href="front?command=logout" 
                           class="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded text-sm font-medium">
                            로그아웃
                        </a>
                    </c:when>
                    <c:otherwise>
                        <!-- 로그인하지 않은 상태 -->
                        <a href="front?command=login" 
                           class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded text-sm font-medium">
                            로그인
                        </a>
                        <a href="front?command=signup" 
                           class="bg-purple-500 hover:bg-purple-600 text-white px-3 py-2 rounded text-sm font-medium">
                            회원가입
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <div class="mb-4">
            <a href="front?command=boardView&id=${board.id}" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2">
                상세보기
            </a>
            <a href="front?command=boardList" class="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded">
                목록으로
            </a>
        </div>

        <!-- 에러 메시지 표시 -->
        <c:if test="${not empty error}">
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                ${error}
            </div>
        </c:if>

        <div class="bg-white shadow-md rounded-lg p-6">
            <form action="front?command=boardUpdate&id=${board.id}" method="post" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">번호</label>
                    <input type="text" value="${board.id}" readonly 
                           class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500">
                </div>
                
                <div>
                    <label for="title" class="block text-sm font-medium text-gray-700 mb-2">제목</label>
                    <input type="text" id="title" name="title" value="${board.title}" required 
                           class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">작성자</label>
                    <input type="text" value="${board.authorName}" readonly 
                           class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500">
                    <input type="hidden" name="author" value="${board.author}">
                </div>
                
                <div>
                    <label for="content" class="block text-sm font-medium text-gray-700 mb-2">내용</label>
                    <textarea id="content" name="content" rows="8" required 
                              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">${board.content}</textarea>
                </div>
                
                <div class="flex justify-end space-x-3">
                    <button type="submit" class="bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded">
                        수정하기
                    </button>
                </div>
            </form>
        </div>
        
        <div class="mt-6 text-sm text-gray-600">
            <p>Model 2 아키텍처 - Servlet + JSP + JSTL</p>
            <p>특징: Servlet이 수정 처리, JSP는 수정 폼 표시, JSTL로 기존 데이터 표시</p>
        </div>
    </div>
</body>
</html>
