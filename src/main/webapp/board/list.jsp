<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시판 목록 - Model 2</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <div class="container mx-auto px-4 py-8">
        <!-- 헤더 영역 -->
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-gray-800">게시판 목록 (Model 2)</h1>
            
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
        
        <!-- 글쓰기 버튼 - 로그인한 사용자에게만 표시 -->
        <c:if test="${not empty sessionScope.user}">
            <div class="mb-4">
                <a href="front?command=boardWrite" class="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
                    글쓰기
                </a>
            </div>
        </c:if>

        <!-- 검색 기능 -->
        <div class="mb-6 bg-white p-4 rounded-lg shadow-sm border">
            <form action="front" method="get" class="flex items-center space-x-4">
                <input type="hidden" name="command" value="boardList">
                
                <select name="searchType" class="border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option value="title" ${searchType == 'title' ? 'selected' : ''}>제목</option>
                    <option value="content" ${searchType == 'content' ? 'selected' : ''}>내용</option>
                    <option value="author" ${searchType == 'author' ? 'selected' : ''}>작성자</option>
                </select>
                
                <input type="text" name="searchKeyword" value="${searchKeyword}" 
                       placeholder="검색어를 입력하세요" 
                       class="flex-1 border border-gray-300 rounded-md px-3 py-2 text-sm">
                
                <button type="submit" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium">
                    검색
                </button>
                
                <c:if test="${not empty searchKeyword}">
                    <a href="front?command=boardList" class="text-gray-500 hover:text-gray-700 text-sm">
                        검색 초기화
                    </a>
                </c:if>
            </form>
        </div>

        <!-- 에러 메시지 표시 -->
        <c:if test="${not empty error}">
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                ${error}
            </div>
        </c:if>

        <!-- 게시글 목록 테이블 -->
        <div class="bg-white shadow-md rounded-lg overflow-hidden">
            <table class="min-w-full">
                <thead class="bg-gray-50">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">번호</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제목</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성자</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성일</th>
                    </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                    <c:choose>
                        <c:when test="${empty boards}">
                            <tr>
                                <td colspan="4" class="px-6 py-4 text-center text-gray-500">
                                    <c:choose>
                                        <c:when test="${not empty searchKeyword}">
                                            검색 결과가 없습니다.
                                        </c:when>
                                        <c:otherwise>
                                            등록된 게시글이 없습니다.
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="board" items="${boards}" varStatus="status">
                                <tr class="hover:bg-gray-50">
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                        <c:if test="${not empty pagination}">
                                            ${pagination.totalCount - (pagination.currentPage - 1) * pagination.pageSize - status.index}
                                        </c:if>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                        <a href="front?command=boardView&id=${board.id}" class="text-blue-600 hover:text-blue-900">${board.title}</a>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${board.authorName}</td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${board.createdAt}</td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- 페이지네이션 UI -->
        <c:if test="${not empty pagination and pagination.totalPages > 1}">
            <div class="mt-6 flex items-center justify-between">
                <!-- 전체 게시글 수 표시 -->
                <div class="text-sm text-gray-700">
                    총 <span class="font-medium">${pagination.totalCount}</span>개의 게시글
                    (${pagination.currentPage} / ${pagination.totalPages} 페이지)
                </div>

                <!-- 페이지 네비게이션 -->
                <div class="flex items-center space-x-2">
                    <!-- 이전 페이지 -->
                    <c:if test="${pagination.hasPrevious}">
                        <a href="front?command=boardList&page=${pagination.previousPage}&searchType=${searchType}&searchKeyword=${searchKeyword}"
                           class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                            이전
                        </a>
                    </c:if>

                    <!-- 페이지 번호들 -->
                    <c:forEach var="pageNum" items="${pagination.pageNumbers}">
                        <c:choose>
                            <c:when test="${pageNum == pagination.currentPage}">
                                <span class="px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 border border-blue-300 rounded-md">
                                    ${pageNum}
                                </span>
                            </c:when>
                            <c:otherwise>
                                <a href="front?command=boardList&page=${pageNum}&searchType=${searchType}&searchKeyword=${searchKeyword}"
                                   class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                                    ${pageNum}
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <!-- 다음 페이지 -->
                    <c:if test="${pagination.hasNext}">
                        <a href="front?command=boardList&page=${pagination.nextPage}&searchType=${searchType}&searchKeyword=${searchKeyword}"
                           class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                            다음
                        </a>
                    </c:if>
                </div>
            </div>
        </c:if>
        
        <div class="mt-6 text-sm text-gray-600">
            <p>Model 2 아키텍처 - Servlet + JSP + JSTL</p>
            <p>특징: Servlet이 컨트롤러 역할, JSP는 뷰 역할, JSTL로 출력 로직 단순화</p>
        </div>
    </div>
</body>
</html>
