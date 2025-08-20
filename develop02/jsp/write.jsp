<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>글쓰기 - Model 2 + 파일 업로드</title>
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
                                안녕하세요, <span class="font-semibold text-blue-600">${sessionScope.user.name}</span>님!
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
                                <span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">글쓰기</span>
                            </div>
                        </li>
                    </ol>
                </nav>
            </div>

            <!-- 페이지 헤더 -->
            <div class="mb-6">
                <h1 class="text-2xl font-bold text-gray-900">글쓰기</h1>
                <p class="text-sm text-gray-600 mt-1">파일을 첨부할 수 있는 게시글을 작성합니다</p>
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

            <!-- 글쓰기 폼 -->
            <div class="bg-white shadow-md rounded-lg overflow-hidden">
                <form action="front?command=boardInsert" method="post" enctype="multipart/form-data" class="space-y-6 p-6">
                    <!-- 제목 입력 -->
                    <div>
                        <label for="title" class="block text-sm font-medium text-gray-700 mb-2">
                            제목 <span class="text-red-500">*</span>
                        </label>
                        <input type="text" id="title" name="title" required 
                               value="${param.title}" 
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                               placeholder="게시글 제목을 입력하세요">
                    </div>
                    
                    <!-- 내용 입력 -->
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-2">
                            내용 <span class="text-red-500">*</span>
                        </label>
                        <textarea id="content" name="content" rows="8" required 
                                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                  placeholder="게시글 내용을 입력하세요">${param.content}</textarea>
                    </div>

                    <!-- 파일 업로드 섹션 -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                            첨부파일 <span class="text-gray-500">(선택사항)</span>
                        </label>
                        
                        <!-- 파일 업로드 영역 -->
                        <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors" 
                             id="dropZone">
                            <svg class="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
                            </svg>
                            <p class="text-sm text-gray-600 mb-2">
                                <span class="font-medium text-blue-600">클릭하여 파일 선택</span> 또는 여기로 파일을 끌어다 놓으세요
                            </p>
                            <p class="text-xs text-gray-500">
                                최대 5개 파일, 개별 파일 10MB, 전체 50MB까지 업로드 가능
                            </p>
                            <input type="file" id="fileInput" name="files" multiple 
                                   accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.txt,.zip,.rar"
                                   class="hidden">
                        </div>

                        <!-- 업로드된 파일 목록 -->
                        <div id="fileList" class="mt-4 space-y-2 hidden">
                            <h4 class="text-sm font-medium text-gray-700">업로드할 파일 목록:</h4>
                            <div id="fileItems" class="space-y-2"></div>
                        </div>

                        <!-- 파일 제한 안내 -->
                        <div class="mt-2 text-xs text-gray-500">
                            <p>허용 파일 형식: 이미지(JPG, PNG, GIF), 문서(PDF, DOC, TXT), 압축파일(ZIP, RAR)</p>
                        </div>
                    </div>

                    <!-- 버튼 영역 -->
                    <div class="flex justify-end space-x-3 pt-4 border-t border-gray-200">
                        <a href="front?command=boardList" 
                           class="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md text-sm font-medium">
                            취소
                        </a>
                        <button type="submit" 
                                class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-md text-sm font-medium">
                            등록하기
                        </button>
                    </div>
                </form>
            </div>

            <!-- 페이지 정보 -->
            <div class="mt-8 text-sm text-gray-600 text-center">
                <p>Model 2 아키텍처 - Servlet + JSP + JSTL + 사용자 인증 + 파일 업로드</p>
                <p>특징: Front Controller Pattern, Command Pattern, 사용자 권한 제어, 파일 첨부 시스템</p>
            </div>
        </div>
    </main>

    <script>
        // 파일 업로드 관련 JavaScript
        const dropZone = document.getElementById('dropZone');
        const fileInput = document.getElementById('fileInput');
        const fileList = document.getElementById('fileList');
        const fileItems = document.getElementById('fileItems');
        
        let uploadedFiles = [];

        // 파일 선택 클릭 이벤트
        dropZone.addEventListener('click', () => {
            fileInput.click();
        });

        // 파일 선택 변경 이벤트
        fileInput.addEventListener('change', handleFileSelect);

        // 드래그 앤 드롭 이벤트
        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('border-blue-400', 'bg-blue-50');
        });

        dropZone.addEventListener('dragleave', (e) => {
            e.preventDefault();
            dropZone.classList.remove('border-blue-400', 'bg-blue-50');
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('border-blue-400', 'bg-blue-50');
            
            const files = Array.from(e.dataTransfer.files);
            handleFiles(files);
        });

        // 파일 선택 처리
        function handleFileSelect(e) {
            const files = Array.from(e.target.files);
            handleFiles(files);
        }

        // 파일 처리
        function handleFiles(files) {
            const validFiles = files.filter(file => {
                // 파일 크기 검증 (10MB)
                if (file.size > 10 * 1024 * 1024) {
                    alert(`파일 "${file.name}"이 10MB를 초과합니다.`);
                    return false;
                }
                
                // 파일 형식 검증
                const allowedTypes = [
                    'image/jpeg', 'image/png', 'image/gif',
                    'application/pdf', 'application/msword', 
                    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                    'text/plain', 'application/zip', 'application/x-rar-compressed'
                ];
                
                if (!allowedTypes.includes(file.type)) {
                    alert(`파일 "${file.name}"은 지원되지 않는 형식입니다.`);
                    return false;
                }
                
                return true;
            });

            // 전체 크기 검증 (50MB)
            const totalSize = validFiles.reduce((sum, file) => sum + file.size, 0);
            if (totalSize > 50 * 1024 * 1024) {
                alert('전체 파일 크기가 50MB를 초과합니다.');
                return;
            }

            // 파일 개수 제한 (5개)
            if (uploadedFiles.length + validFiles.length > 5) {
                alert('최대 5개까지만 업로드할 수 있습니다.');
                return;
            }

            // 파일 추가
            validFiles.forEach(file => {
                uploadedFiles.push(file);
            });

            updateFileList();
        }

        // 파일 목록 업데이트
        function updateFileList() {
            if (uploadedFiles.length === 0) {
                fileList.classList.add('hidden');
                return;
            }

            fileList.classList.remove('hidden');
            fileItems.innerHTML = '';

            uploadedFiles.forEach((file, index) => {
                const fileItem = document.createElement('div');
                fileItem.className = 'flex items-center justify-between p-3 bg-gray-50 rounded-lg';
                
                const fileInfo = document.createElement('div');
                fileInfo.className = 'flex items-center space-x-3';
                
                // 파일 아이콘
                const icon = document.createElement('div');
                icon.innerHTML = getFileIcon(file.type);
                icon.className = 'flex-shrink-0';
                
                // 파일 정보
                const info = document.createElement('div');
                info.innerHTML = `
                    <p class="text-sm font-medium text-gray-900">${file.name}</p>
                    <p class="text-xs text-gray-500">${formatFileSize(file.size)}</p>
                `;
                
                fileInfo.appendChild(icon);
                fileInfo.appendChild(info);
                
                // 삭제 버튼
                const deleteBtn = document.createElement('button');
                deleteBtn.type = 'button';
                deleteBtn.className = 'text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-100 transition-colors';
                deleteBtn.innerHTML = `
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                    </svg>
                `;
                deleteBtn.onclick = () => removeFile(index);
                
                fileItem.appendChild(fileInfo);
                fileItem.appendChild(deleteBtn);
                fileItems.appendChild(fileItem);
            });
        }

        // 파일 삭제
        function removeFile(index) {
            uploadedFiles.splice(index, 1);
            updateFileList();
        }

        // 파일 아이콘 반환
        function getFileIcon(type) {
            if (type.startsWith('image/')) {
                return `<svg class="w-6 h-6 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path>
                </svg>`;
            } else if (type.startsWith('text/') || type.includes('document')) {
                return `<svg class="w-6 h-6 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                </svg>`;
            } else if (type.includes('zip') || type.includes('rar')) {
                return `<svg class="w-6 h-6 text-purple-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" clip-rule="evenodd"></path>
                </svg>`;
            } else {
                return `<svg class="w-6 h-6 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
                </svg>`;
            }
        }

        // 파일 크기 포맷팅
        function formatFileSize(bytes) {
            if (bytes === 0) return '0 Bytes';
            const k = 1024;
            const sizes = ['Bytes', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        }

        // 폼 제출 시 파일 검증
        document.querySelector('form').addEventListener('submit', function(e) {
            const title = document.getElementById('title').value.trim();
            const content = document.getElementById('content').value.trim();
            
            if (!title || !content) {
                e.preventDefault();
                alert('제목과 내용을 모두 입력해주세요.');
                return;
            }
        });
    </script>
</body>
</html>
