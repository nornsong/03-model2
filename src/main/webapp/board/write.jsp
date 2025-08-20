<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>글쓰기 - Model 2</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <div class="container mx-auto px-4 py-8">
        <!-- 헤더 영역 -->
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-gray-800">글쓰기 (Model 2)</h1>
            
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
        </div>

        <!-- 에러 메시지 표시 -->
        <c:if test="${not empty error}">
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                ${error}
            </div>
        </c:if>

        <div class="bg-white shadow-md rounded-lg p-6">
                         <form action="front?command=boardInsert" method="post" enctype="multipart/form-data" class="space-y-4" onsubmit="return validateForm()">
                <div>
                    <label for="title" class="block text-sm font-medium text-gray-700 mb-2">제목</label>
                    <input type="text" id="title" name="title" required 
                           value="${param.title}" 
                           class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                
                <div>
                    <label for="author" class="block text-sm font-medium text-gray-700 mb-2">작성자</label>
                    <input type="text" id="author" name="author" required 
                           value="${sessionScope.user.name}" readonly
                           class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-100 text-gray-700">
                    <input type="hidden" name="author" value="${sessionScope.user.id}">
                </div>
                
                                 <div>
                     <label for="content" class="block text-sm font-medium text-gray-700 mb-2">내용</label>
                     <textarea id="content" name="content" rows="8" required 
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">${param.content}</textarea>
                 </div>
                 
                             <!-- 파일 업로드 섹션 -->
            <div>
                <label for="files" class="block text-sm font-medium text-gray-700 mb-2">
                    첨부파일 <span class="text-gray-500">(선택사항)</span>
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
                    <button type="submit" class="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
                        등록하기
                    </button>
                </div>
            </form>
        </div>
        
        <!-- 폼 검증 및 로깅 JavaScript -->
        <script>
            function validateForm() {
                var title = document.getElementById('title').value;
                var content = document.getElementById('content').value;
                var files = document.getElementById('files').files;
                
                console.log('=== 폼 제출 로깅 ===');
                console.log('제목:', title);
                console.log('내용:', content);
                console.log('파일 개수:', files.length);
                for (var i = 0; i < files.length; i++) {
                    console.log('파일 ' + (i+1) + ':', files[i].name, '크기:', files[i].size);
                }
                console.log('Content-Type:', document.querySelector('form').enctype);
                console.log('========================');
                
                if (title.trim() === '') {
                    alert('제목을 입력해주세요.');
                    return false;
                }
                return true;
            }
        </script>
        
        <div class="mt-6 text-sm text-gray-600">
            <p>Model 2 아키텍처 - Servlet + JSP + JSTL</p>
            <p>특징: Servlet이 폼 처리, JSP는 폼 표시, JSTL로 에러 메시지 표시</p>
        </div>
    </div>
</body>
</html>
