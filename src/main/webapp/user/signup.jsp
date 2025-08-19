<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입 - Model 2</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8">
            <div>
                <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
                    회원가입
                </h2>
                <p class="mt-2 text-center text-sm text-gray-600">
                    새로운 계정을 만들어보세요
                </p>
            </div>

            <!-- 에러 메시지 표시 -->
            <c:if test="${not empty error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                    ${error}
                </div>
            </c:if>

            <!-- 성공 메시지 표시 -->
            <c:if test="${not empty success}">
                <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
                    ${success}
                </div>
            </c:if>

            <form class="mt-8 space-y-6" action="front?command=signup" method="post" id="signupForm">
                <div class="rounded-md shadow-sm -space-y-px">
                    <div>
                        <label for="username" class="sr-only">아이디</label>
                        <input id="username" name="username" type="text" required 
                               class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                               placeholder="아이디" value="${username}">
                    </div>
                    <div>
                        <label for="password" class="sr-only">비밀번호</label>
                        <input id="password" name="password" type="password" required 
                               class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                               placeholder="비밀번호">
                    </div>
                    <div>
                        <label for="confirmPassword" class="sr-only">비밀번호 확인</label>
                        <input id="confirmPassword" name="confirmPassword" type="password" required 
                               class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                               placeholder="비밀번호 확인">
                    </div>
                    <div>
                        <label for="name" class="sr-only">이름</label>
                        <input id="name" name="name" type="text" required 
                               class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                               placeholder="이름" value="${name}">
                    </div>
                    <div>
                        <label for="email" class="sr-only">이메일</label>
                        <input id="email" name="email" type="email" 
                               class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                               placeholder="이메일 (선택사항)" value="${email}">
                    </div>
                </div>

                <div>
                    <button type="submit" 
                            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                        회원가입
                    </button>
                </div>

                <div class="text-center">
                    <a href="front?command=login" class="font-medium text-blue-600 hover:text-blue-500">
                        이미 계정이 있으신가요? 로그인하기
                    </a>
                </div>
            </form>
        </div>
    </div>

    <script>
        // 폼 제출 전 유효성 검사
        document.getElementById('signupForm').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('비밀번호가 일치하지 않습니다.');
                return false;
            }
            
            if (password.length < 6) {
                e.preventDefault();
                alert('비밀번호는 6자 이상이어야 합니다.');
                return false;
            }
        });
    </script>
</body>
</html>
