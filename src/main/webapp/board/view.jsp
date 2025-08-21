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
        <!-- 헤더 영역 -->
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-gray-800">게시글 보기 (Model 2)</h1>
            
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
            <a href="front?command=boardList" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2">
                목록으로
            </a>
            
            <!-- 글쓰기 버튼 - 로그인한 사용자에게만 표시 -->
            <c:if test="${not empty sessionScope.user}">
                <a href="front?command=boardWrite" class="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mr-2">
                    글쓰기
                </a>
            </c:if>
            
            <!-- 수정/삭제 버튼 - 본인이 작성한 글에만 표시 -->
            <c:if test="${not empty sessionScope.user and sessionScope.user.id == board.author}">
                <a href="front?command=boardUpdate&id=${board.id}" class="bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded mr-2">
                    수정
                </a>
                <a href="front?command=boardDelete&id=${board.id}" class="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
                   onclick="return confirm('정말 삭제하시겠습니까?')">
                    삭제
                </a>
            </c:if>
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
                        <div class="text-lg text-gray-900">${board.authorName}</div>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">작성일</label>
                        <div class="text-lg text-gray-900">${board.createdAt}</div>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">내용</label>
                        <div class="text-lg text-gray-900 whitespace-pre-wrap">${board.content}</div>
                    </div>
                    
                    <!-- 첨부파일 표시 -->
                    <c:if test="${not empty board.attachments and board.attachments.size() > 0}">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">첨부파일</label>
                            <div class="space-y-2">
                                <c:forEach var="file" items="${board.attachments}">
                                    <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg" data-file-id="${file.id}">
                                        <div class="flex items-center space-x-3">
                                            <!-- 파일 타입별 아이콘 -->
                                            <c:choose>
                                                <c:when test="${file.fileType.startsWith('image/')}">
                                                    <!-- 이미지 파일: 직접 링크로 표시 -->
                                                    <svg class="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path>
                                                    </svg>
                                                    <div>
                                                        <p class="text-sm font-medium text-gray-900">${file.originalFilename}</p>
                                                        <p class="text-xs text-gray-500">${file.fileSize} bytes</p>
                                                        <!-- 이미지는 직접 링크로 접근 -->
                                                        <a href="/uploads/images/${file.storedFilename}" target="_blank" 
                                                           class="text-blue-600 hover:text-blue-800 text-xs">이미지 보기</a>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <!-- 일반 파일: Java를 통한 다운로드 -->
                                                    <svg class="w-5 h-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                                                    </svg>
                                                    <div>
                                                        <p class="text-sm font-medium text-gray-900">${file.originalFilename}</p>
                                                        <p class="text-xs text-gray-500">${file.fileSize} bytes</p>
                                                        <!-- 일반 파일은 Java 다운로드 -->
                                                        <a href="front?command=fileDownload&id=${file.id}" 
                                                           class="text-blue-600 hover:text-blue-800 text-xs">다운로드</a>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        
                                        <!-- 파일 삭제 버튼 - 본인이 작성한 글에만 표시 -->
                                        <c:if test="${not empty sessionScope.user and sessionScope.user.id == board.author}">
                                            <button type="button" 
                                                    onclick="deleteFile(${file.id})"
                                                    class="text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-100 transition-colors">
                                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                                </svg>
                                            </button>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </c:if>
        
        <div class="mt-6 text-sm text-gray-600">
            <p>Model 2 아키텍처 - Servlet + JSP + JSTL</p>
            <p>특징: Servlet이 데이터 조회, JSP는 화면 출력, JSTL로 조건부 출력 처리</p>
        </div>
    </div>
    
    <!-- 파일 삭제 AJAX JavaScript -->
    <script>
        function deleteFile(fileId) {
            if (!confirm('정말로 이 파일을 삭제하시겠습니까?')) {
                return;
            }
            
            // AJAX 요청으로 파일 삭제
            fetch('front?command=fileDelete&fileId=' + fileId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // 성공 시 해당 파일 요소 제거
                    const fileElement = document.querySelector(`[data-file-id="${fileId}"]`);
                    if (fileElement) {
                        fileElement.remove();
                    }
                    
                    // 파일이 없으면 기존 첨부파일 섹션 숨기기
                    const remainingFiles = document.querySelectorAll('[data-file-id]');
                    if (remainingFiles.length === 0) {
                        const attachmentsSection = document.querySelector('.attachments-section');
                        if (attachmentsSection) {
                            attachmentsSection.style.display = 'none';
                        }
                    }
                    
                    alert('파일이 삭제되었습니다.');
                } else {
                    alert('파일 삭제에 실패했습니다: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('파일 삭제 중 오류가 발생했습니다.');
            });
        }
    </script>
</body>
</html>
