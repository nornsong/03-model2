<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>게시판 목록 - Model 2 + 파일 업로드</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50 min-h-screen">
    <!-- 헤더 영역 -->
    <header class="bg-white shadow-sm border-b border-gray-200">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <!-- 로고/제목 -->
                <div class="flex items-center">
                    <h1 class="text-xl font-semibold text-gray-900">게시판 시스템</h1>
                    <span class="ml-2 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full">파일 업로드 지원</span>
                </div>

                <!-- 사용자 상태 표시 -->
                <div class="flex items-center space-x-4">
                    <c:choose>
                        <c:when test="${not empty sessionScope.user}">
                            <!-- 로그인한 상태 -->
                            <span class="text-gray-700 text-sm">
                                안녕하세요, <span class="font-semibold text-blue-600">${sessionScope.userName}</span>님!
                            </span>
                            <a href="front?command=logout" 
                               class="text-gray-700 hover:text-red-600 px-3 py-2 rounded-md text-sm font-medium">
                                로그아웃
                            </a>
                        </c:when>
                        <c:otherwise>
                            <!-- 로그인하지 않은 상태 -->
                            <a href="front?command=login" 
                               class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium">
                                로그인
                            </a>
                            <a href="front?command=signup" 
                               class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                                회원가입
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </header>

    <!-- 메인 콘텐츠 -->
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div class="px-4 py-6 sm:px-0">
            <!-- 페이지 헤더 -->
            <div class="flex justify-between items-center mb-6">
                <div>
                    <h2 class="text-2xl font-bold text-gray-900">게시글 목록</h2>
                    <p class="text-sm text-gray-600 mt-1">파일 첨부 기능이 지원되는 게시판입니다</p>
                </div>
                
                <!-- 글쓰기 버튼 (로그인한 사용자에게만 표시) -->
                <c:if test="${not empty sessionScope.user}">
                    <a href="front?command=boardWrite" 
                       class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium flex items-center">
                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                        </svg>
                        글쓰기
                    </a>
                </c:if>
            </div>

            <!-- 에러 메시지 표시 -->
            <c:if test="${not empty error}">
                <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4" role="alert">
                    <div class="flex">
                        <svg class="w-5 h-5 text-red-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
                        </svg>
                        <span class="block sm:inline">${error}</span>
                    </div>
                </div>
            </c:if>

            <!-- 성공 메시지 표시 -->
            <c:if test="${not empty success}">
                <div class="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded mb-4" role="alert">
                    <div class="flex">
                        <svg class="w-5 h-5 text-green-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                        </svg>
                        <span class="block sm:inline">${success}</span>
                    </div>
                </div>
            </c:if>

            <!-- 게시글 목록 테이블 -->
            <div class="bg-white shadow overflow-hidden sm:rounded-md">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">번호</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제목</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성자</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">첨부파일</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성일</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty boards}">
                                <tr>
                                    <td colspan="5" class="px-6 py-4 text-center text-gray-500">
                                        <div class="flex flex-col items-center">
                                            <svg class="w-12 h-12 text-gray-300 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                                            </svg>
                                            등록된 게시글이 없습니다.
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="board" items="${boards}">
                                    <tr class="hover:bg-gray-50">
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${board.id}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            <a href="front?command=boardView&id=${board.id}" 
                                               class="text-blue-600 hover:text-blue-900 font-medium">
                                                ${board.title}
                                            </a>
                                            <!-- 새 글 표시 (24시간 이내) -->
                                            <c:if test="${board.isNew}">
                                                <span class="ml-2 px-2 py-1 text-xs bg-red-100 text-red-800 rounded-full">NEW</span>
                                            </c:if>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            <c:choose>
                                                                        <c:when test="${not empty board.authorName}">
                            ${board.authorName}
                                                </c:when>
                                                <c:otherwise>
                                                    ${board.author}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            <!-- 첨부파일 표시 -->
                                            <c:choose>
                                                <c:when test="${not empty board.fileCount and board.fileCount > 0}">
                                                    <div class="flex items-center">
                                                        <svg class="w-4 h-4 text-blue-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                                            <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                                                        </svg>
                                                        <span class="text-blue-600 font-medium">${board.fileCount}개</span>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-gray-400">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:choose>
                                                <c:when test="${not empty board.regDate}">
                                                    <fmt:formatDate value="${board.regDate}" pattern="yyyy-MM-dd HH:mm"/>
                                                </c:when>
                                                <c:otherwise>
                                                    ${board.createdAt}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- 페이지네이션 (선택사항) -->
            <c:if test="${not empty pagination}">
                <div class="mt-6 flex items-center justify-between">
                    <div class="text-sm text-gray-700">
                        총 <span class="font-medium">${pagination.totalCount}</span>개의 게시글
                    </div>
                    <div class="flex space-x-2">
                        <c:if test="${pagination.hasPrevious}">
                            <a href="front?command=boardList&page=${pagination.previousPage}" 
                               class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50">
                                이전
                            </a>
                        </c:if>
                        <c:forEach var="pageNum" items="${pagination.pageNumbers}">
                            <c:choose>
                                <c:when test="${pageNum == pagination.currentPage}">
                                    <span class="px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 border border-blue-300 rounded-md">
                                        ${pageNum}
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <a href="front?command=boardList&page=${pageNum}" 
                                       class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50">
                                        ${pageNum}
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        <c:if test="${pagination.hasNext}">
                            <a href="front?command=boardList&page=${pagination.nextPage}" 
                               class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50">
                                다음
                            </a>
                        </c:if>
                    </div>
                </div>
            </c:if>

            <!-- 페이지 정보 -->
            <div class="mt-6 text-sm text-gray-600">
                <p>Model 2 아키텍처 - Servlet + JSP + JSTL + 사용자 인증 + 파일 업로드</p>
                <p>특징: Front Controller Pattern, Command Pattern, 사용자 권한 제어, 파일 첨부 시스템</p>
            </div>
        </div>
    </main>
</body>
</html>
