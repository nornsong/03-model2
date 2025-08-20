<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Java Web History - Model 2</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <div class="container mx-auto px-4 py-8">
        <!-- 헤더 영역 -->
        <div class="flex justify-between items-center mb-8">
            <div class="text-center">
                <h1 class="text-4xl font-bold text-gray-800 mb-4">Java Web History - Model 2</h1>
                <p class="text-xl text-gray-600">2000년대 초반 Servlet + JSP + JSTL 아키텍처</p>
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
                           class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium">
                            로그아웃
                        </a>
                    </c:when>
                    <c:otherwise>
                        <!-- 로그인하지 않은 상태 -->
                        <a href="front?command=login" 
                           class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium">
                            로그인
                        </a>
                        <a href="front?command=signup" 
                           class="bg-purple-500 hover:bg-purple-600 text-white px-4 py-2 rounded-md text-sm font-medium">
                            회원가입
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="bg-white rounded-lg shadow-lg p-8 mb-8">
            <h2 class="text-2xl font-bold text-gray-800 mb-4">Model 2 아키텍처란?</h2>
            <div class="space-y-4 text-gray-600">
                <p>Model 2는 <strong>MVC (Model-View-Controller)</strong> 패턴을 Java 웹 개발에 적용한 아키텍처입니다.</p>
                
                <div class="grid md:grid-cols-3 gap-6 mt-6">
                    <div class="bg-blue-50 p-4 rounded-lg">
                        <h3 class="font-bold text-blue-800 mb-2">Model (DAO)</h3>
                        <p class="text-sm">비즈니스 로직과 데이터 액세스를 담당</p>
                        <ul class="text-sm mt-2 list-disc list-inside">
                            <li>BoardDAO 클래스</li>
                            <li>데이터베이스 풀 사용</li>
                            <li>PreparedStatement로 SQL 인젝션 방어</li>
                        </ul>
                    </div>
                    
                    <div class="bg-green-50 p-4 rounded-lg">
                        <h3 class="font-bold text-green-800 mb-2">View (JSP)</h3>
                        <p class="text-sm">사용자에게 보여지는 화면을 담당</p>
                        <ul class="text-sm mt-2 list-disc list-inside">
                            <li>JSTL을 사용한 출력</li>
                            <li>Java 코드 최소화</li>
                            <li>Servlet에서 전달받은 데이터 표시</li>
                        </ul>
                    </div>
                    
                    <div class="bg-purple-50 p-4 rounded-lg">
                        <h3 class="font-bold text-purple-800 mb-2">Controller (Servlet)</h3>
                        <p class="text-sm">사용자 요청을 처리하고 Model과 View를 연결</p>
                        <ul class="text-sm mt-2 list-disc list-inside">
                            <li>요청 파라미터 처리</li>
                            <li>DAO 호출 및 결과 저장</li>
                            <li>JSP로 포워딩</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <div class="grid md:grid-cols-2 gap-8 mb-8">
            <!-- 게시판 기능 -->
            <div class="bg-white rounded-lg shadow-lg p-6">
                <h3 class="text-xl font-bold text-gray-800 mb-4">게시판 기능</h3>
                <div class="space-y-3">
                    <a href="front?command=boardList" 
                       class="block w-full bg-blue-500 hover:bg-blue-600 text-white text-center py-3 px-4 rounded-lg font-semibold transition-colors">
                        게시글 목록 보기
                    </a>
                    <a href="front?command=boardWrite" 
                       class="block w-full bg-green-500 hover:bg-green-600 text-white text-center py-3 px-4 rounded-lg font-semibold transition-colors">
                        새 글 작성하기
                    </a>
                </div>
            </div>

            <!-- 사용자 관리 -->
            <div class="bg-white rounded-lg shadow-lg p-6">
                <h3 class="text-xl font-bold text-gray-800 mb-4">사용자 관리</h3>
                <div class="space-y-3">
                    <a href="front?command=signup" 
                       class="block w-full bg-purple-500 hover:bg-purple-600 text-white text-center py-3 px-4 rounded-lg font-semibold transition-colors">
                        회원가입
                    </a>
                    <a href="front?command=login" 
                       class="block w-full bg-indigo-500 hover:bg-indigo-600 text-white text-center py-3 px-4 rounded-lg font-semibold transition-colors">
                        로그인
                    </a>
                </div>
            </div>

            <!-- 기술 스택 -->
            <div class="bg-white rounded-lg shadow-lg p-6">
                <h3 class="text-xl font-bold text-gray-800 mb-4">기술 스택</h3>
                <div class="space-y-2 text-sm text-gray-600">
                    <div class="flex items-center">
                        <span class="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                        Java Servlet 2.4+
                    </div>
                    <div class="flex items-center">
                        <span class="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                        JSP 2.0 + JSTL 1.2
                    </div>
                    <div class="flex items-center">
                        <span class="w-2 h-2 bg-purple-500 rounded-full mr-2"></span>
                        데이터베이스 풀 (JNDI)
                    </div>
                    <div class="flex items-center">
                        <span class="w-2 h-2 bg-yellow-500 rounded-full mr-2"></span>
                        H2 Database
                    </div>
                    <div class="flex items-center">
                        <span class="w-2 h-2 bg-red-500 rounded-full mr-2"></span>
                        Tailwind CSS
                    </div>
                </div>
            </div>
        </div>

        <div class="bg-white rounded-lg shadow-lg p-8">
            <h3 class="text-2xl font-bold text-gray-800 mb-4">Model 2의 장점</h3>
            <div class="grid md:grid-cols-2 gap-6">
                <div>
                    <h4 class="font-bold text-gray-800 mb-2">코드 분리</h4>
                    <ul class="text-sm text-gray-600 space-y-1 list-disc list-inside">
                        <li>비즈니스 로직과 표현 로직 분리</li>
                        <li>각 컴포넌트의 역할이 명확</li>
                        <li>코드 재사용성 향상</li>
                    </ul>
                </div>
                <div>
                    <h4 class="font-bold text-gray-800 mb-2">유지보수성</h4>
                    <ul class="text-sm text-gray-600 space-y-1 list-disc list-inside">
                        <li>수정 시 영향 범위 최소화</li>
                        <li>테스트 용이성 향상</li>
                        <li>팀 개발 시 역할 분담 가능</li>
                    </ul>
                </div>
                <div>
                    <h4 class="font-bold text-gray-800 mb-2">확장성</h4>
                    <ul class="text-sm text-gray-600 space-y-1 list-disc list-inside">
                        <li>새로운 기능 추가 용이</li>
                        <li>컴포넌트별 독립적 개발</li>
                        <li>프레임워크 도입 기반</li>
                    </ul>
                </div>
                <div>
                    <h4 class="font-bold text-gray-800 mb-2">보안성</h4>
                    <ul class="text-sm text-gray-600 space-y-1 list-disc list-inside">
                        <li>PreparedStatement 사용</li>
                        <li>입력값 검증 및 필터링</li>
                        <li>권한 관리 용이</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
