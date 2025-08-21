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
            <form action="front?command=boardUpdate&id=${board.id}" method="post" enctype="multipart/form-data" class="space-y-4">
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
                
                <!-- 기존 첨부파일 표시 -->
                <c:if test="${not empty board.attachments and board.attachments.size() > 0}">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">기존 첨부파일</label>
                        <div class="space-y-2">
                                                         <c:forEach var="file" items="${board.attachments}">
                                 <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg attachments-section" data-file-id="${file.id}">
                                    <div class="flex items-center space-x-3">
                                        <svg class="w-5 h-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                                            <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                                        </svg>
                                        <div>
                                            <p class="text-sm font-medium text-gray-900">${file.originalFilename}</p>
                                            <p class="text-xs text-gray-500">${file.fileSize} bytes</p>
                                        </div>
                                    </div>
                                    <button type="button" 
                                            onclick="deleteFile(${file.id})"
                                            class="text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-100 transition-colors">
                                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                        </svg>
                                    </button>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
                
                            <!-- 새 파일 업로드 섹션 -->
            <div>
                <label for="files" class="block text-sm font-medium text-gray-700 mb-2">
                    새 첨부파일 <span class="text-gray-500">(선택사항)</span>
                </label>
                
                <!-- 간단한 파일 선택 -->
                <input type="file" id="files" name="files" multiple 
                       accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.txt,.zip,.rar"
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                
                <!-- 파일 제한 안내 -->
                <div class="mt-2 text-xs text-gray-500">
                    <p>허용 파일 형식: 이미지(JPG, PNG, GIF), 문서(PDF, DOC, TXT), 압축파일(ZIP, RAR)</p>
                    <p>최대 5개 파일, 개별 파일 10MB까지 업로드 가능</p>
                </div>
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
