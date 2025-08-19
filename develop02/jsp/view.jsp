<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>게시글 보기 - Model 2 + 파일 업로드</title>
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
    <main class="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div class="px-4 py-6 sm:px-0">
            <!-- 네비게이션 -->
            <div class="mb-6">
                <nav class="flex" aria-label="Breadcrumb">
                    <ol class="inline-flex items-center space-x-1 md:space-x-3">
                        <li class="inline-flex items-center">
                            <a href="front?command=boardList" 
                               class="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600">
                                <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                    <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"></path>
                                </svg>
                                게시판 목록
                            </a>
                        </li>
                        <li>
                            <div class="flex items-center">
                                <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path>
                                </svg>
                                <span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">게시글 보기</span>
                            </div>
                        </li>
                    </ol>
                </nav>
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

            <!-- 게시글 내용 표시 -->
            <c:if test="${not empty board}">
                <div class="bg-white shadow-md rounded-lg overflow-hidden">
                    <!-- 게시글 헤더 -->
                    <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
                        <div class="flex justify-between items-start">
                            <div class="flex-1">
                                <h1 class="text-2xl font-bold text-gray-900 mb-2">${board.title}</h1>
                                <div class="flex items-center space-x-4 text-sm text-gray-600">
                                    <span>번호: ${board.id}</span>
                                    <span>작성자: 
                                        <c:choose>
                                                                    <c:when test="${not empty board.authorName}">
                            ${board.authorName}
                                            </c:when>
                                            <c:otherwise>
                                                ${board.author}
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span>작성일: 
                                        <c:choose>
                                            <c:when test="${not empty board.regDate}">
                                                <fmt:formatDate value="${board.regDate}" pattern="yyyy-MM-dd HH:mm"/>
                                            </c:when>
                                            <c:otherwise>
                                                ${board.createdAt}
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                            
                            <!-- 수정/삭제 버튼 (작성자에게만 표시) -->
                            <c:if test="${sessionScope.userId == board.authorId}">
                                <div class="flex space-x-2">
                                    <a href="front?command=boardUpdate&id=${board.id}" 
                                       class="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-md text-sm font-medium">
                                        수정
                                    </a>
                                    <a href="front?command=boardDelete&id=${board.id}" 
                                       onclick="return confirm('정말 삭제하시겠습니까?')"
                                       class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm font-medium">
                                        삭제
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <!-- 첨부파일 섹션 -->
                    <c:if test="${not empty board.files and board.files.size() > 0}">
                        <div class="px-6 py-4 border-b border-gray-200 bg-blue-50">
                            <h3 class="text-sm font-medium text-gray-900 mb-3 flex items-center">
                                <svg class="w-4 h-4 mr-2 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                                </svg>
                                첨부파일 (${board.files.size()}개)
                            </h3>
                            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                                <c:forEach var="file" items="${board.files}">
                                    <div class="flex items-center p-3 bg-white rounded-lg border border-gray-200 hover:border-blue-300 transition-colors">
                                        <div class="flex-shrink-0 mr-3">
                                            <!-- 파일 타입별 아이콘 -->
                                            <c:choose>
                                                <c:when test="${file.fileType.startsWith('image/')}">
                                                    <svg class="w-8 h-8 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path>
                                                    </svg>
                                                </c:when>
                                                <c:when test="${file.fileType.startsWith('text/') or file.fileType.contains('document')}">
                                                    <svg class="w-8 h-8 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                                                    </svg>
                                                </c:when>
                                                <c:when test="${file.fileType.contains('zip') or file.fileType.contains('rar')}">
                                                    <svg class="w-8 h-8 text-purple-500" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" clip-rule="evenodd"></path>
                                                    </svg>
                                                </c:otherwise>
                                                    <svg class="w-8 h-8 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                                                    </svg>
                                                </c:choose>
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <p class="text-sm font-medium text-gray-900 truncate" title="${file.originalName}">
                                                ${file.originalName}
                                            </p>
                                            <p class="text-xs text-gray-500">
                                                <fmt:formatNumber value="${file.fileSize / 1024}" pattern="#,##0.0"/> KB
                                            </p>
                                        </div>
                                        <div class="flex-shrink-0 ml-2">
                                            <a href="front?command=fileDownload&fileId=${file.id}" 
                                               class="text-blue-600 hover:text-blue-800 p-1 rounded hover:bg-blue-100 transition-colors"
                                               title="다운로드">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                                                </svg>
                                            </a>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>

                    <!-- 게시글 내용 -->
                    <div class="px-6 py-6">
                        <div class="prose max-w-none">
                            <div class="text-gray-900 whitespace-pre-wrap leading-relaxed">${board.content}</div>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- 게시글이 없는 경우 -->
            <c:if test="${empty board}">
                <div class="bg-white shadow-md rounded-lg p-8 text-center">
                    <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    <h3 class="text-lg font-medium text-gray-900 mb-2">게시글을 찾을 수 없습니다</h3>
                    <p class="text-gray-500 mb-4">요청하신 게시글이 존재하지 않거나 삭제되었습니다.</p>
                    <a href="front?command=boardList" 
                       class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                        목록으로 돌아가기
                    </a>
                </div>
            </c:if>

            <!-- 하단 버튼 -->
            <div class="mt-6 flex justify-between items-center">
                <a href="front?command=boardList" 
                   class="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md text-sm font-medium">
                    목록으로
                </a>
                
                <div class="flex space-x-2">
                    <c:if test="${not empty sessionScope.user}">
                        <a href="front?command=boardWrite" 
                           class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                            글쓰기
                        </a>
                    </c:if>
                </div>
            </div>

            <!-- 페이지 정보 -->
            <div class="mt-8 text-sm text-gray-600 text-center">
                <p>Model 2 아키텍처 - Servlet + JSP + JSTL + 사용자 인증 + 파일 업로드</p>
                <p>특징: Front Controller Pattern, Command Pattern, 사용자 권한 제어, 파일 첨부 시스템</p>
            </div>
        </div>
    </main>
</body>
</html>
