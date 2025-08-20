# 2단계: 로그인/로그아웃 기능 구현

## 🎯 목표

사용자 로그인/로그아웃 기능을 구현하여 인증된 사용자만 시스템을 이용할 수 있도록 합니다.

## ⚠️ 중요: 기존 시스템의 한계

**현재 상황**: 회원가입은 가능하지만 로그인/로그아웃 기능 없음

- **세션 관리**: 사용자 로그인 상태 유지 불가
- **인증 검증**: 로그인한 사용자와 비로그인 사용자 구분 불가
- **보안 취약**: 인증 없이 모든 기능 접근 가능

## 📚 이론 포인트 리마인드

### **로그인/로그아웃 시스템의 핵심**

- **세션 관리**: HttpSession을 사용한 사용자 상태 유지
- **인증 검증**: 아이디/비밀번호 매칭으로 사용자 확인
- **보안 처리**: 로그인 실패 시 적절한 에러 메시지
- **상태 전환**: 로그인/로그아웃 시 적절한 페이지 이동

### **보안 고려사항**

- **세션 하이재킹 방지**: 세션 ID 보안 관리
- **비밀번호 검증**: 해시된 비밀번호와 입력값 비교
- **로그아웃 처리**: 세션 무효화 및 리소스 정리
- **에러 메시지**: 보안을 위해 구체적인 실패 원인 노출 금지

## 📋 준비사항

- 1단계 완료 (회원가입 기능 구현)
- users 테이블에 테스트 사용자 데이터 존재
- Spring Framework 라이브러리 사용 가능

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**회원가입 기능이 정상 작동하는지 확인합니다.**

1. `http://localhost:8080/front?command=signup` 접속 확인
2. 테스트 사용자로 회원가입 완료
3. H2 데이터베이스에서 users 테이블 데이터 확인

### 1단계: UserDAO에 로그인 메서드 추가

`src/main/java/io/goorm/backend/UserDAO.java` 수정:

```java
// 기존 코드에 추가

// 로그인 검증 (아이디/비밀번호 확인)
public User login(String username, String password) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try {
        return jdbcTemplate.queryForObject(sql, userRowMapper, username, password);
    } catch (Exception e) {
        return null;
    }
}

// 사용자 ID로 사용자 조회
public User getUserById(Long userId) {
            String sql = "SELECT * FROM users WHERE id = ?";
    try {
        return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
    } catch (Exception e) {
        return null;
    }
}
```

### 2단계: LoginCommand 클래스 생성

`src/main/java/io/goorm/backend/command/LoginCommand.java` 생성:

```java
package io.goorm.backend.command;

import io.goorm.backend.User;
import io.goorm.backend.UserDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;

public class LoginCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getMethod().equals("GET")) {
                // 로그인 폼 표시
                return "/user/login.jsp";
            } else {
                // POST 요청 - 로그인 처리
                request.setCharacterEncoding("UTF-8");

                String username = request.getParameter("username");
                String password = request.getParameter("password");

                // 유효성 검사
                if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                    request.setAttribute("error", "아이디와 비밀번호를 모두 입력해주세요.");
                    return "/user/login.jsp";
                }

                // 비밀번호 해시
                String hashedPassword = hashPassword(password);

                // 로그인 검증
                UserDAO userDAO = new UserDAO();
                User user = userDAO.login(username, hashedPassword);

                if (user != null) {
                    // 로그인 성공 - 세션에 사용자 정보 저장
                    HttpSession session = request.getSession();
                    session.setAttribute("user", user);

                    // 게시판 목록으로 이동
                    response.sendRedirect("front?command=boardList");
                    return null;
                } else {
                    // 로그인 실패
                    request.setAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
                    return "/user/login.jsp";
                }
            }

        } catch (Exception e) {
            request.setAttribute("error", "로그인 처리 중 오류가 발생했습니다.");
            return "/user/login.jsp";
        }
    }

    // 비밀번호 해시 함수 (SignupCommand와 동일)
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }
}
```

### 3단계: LogoutCommand 클래스 생성

`src/main/java/io/goorm/backend/command/LogoutCommand.java` 생성:

```java
package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 게시판 목록으로 이동
            response.sendRedirect("front?command=boardList");
            return null;

        } catch (Exception e) {
            // 에러 발생 시에도 게시판 목록으로 이동
            try {
                response.sendRedirect("front?command=boardList");
            } catch (Exception ex) {
                // 최후의 수단으로 에러 페이지 반환
                return "/error.jsp";
            }
            return null;
        }
    }
}
```

### 4단계: HandlerMapping에 Command 추가

`src/main/java/io/goorm/backend/handler/HandlerMapping.java` 수정:

```java
// 기존 코드에 추가
commandMap.put("login", new LoginCommand());
commandMap.put("logout", new LogoutCommand());
```

### 5단계: 로그인 JSP 파일 생성

`src/main/webapp/user/login.jsp` 생성:

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>로그인</title>
</head>
<body>
    <h1>로그인</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
    </c:if>

    <form action="front?command=login" method="post">
        <p>
            <label>아이디:</label>
            <input type="text" name="username" required>
        </p>
        <p>
            <label>비밀번호:</label>
            <input type="password" name="password" required>
        </p>
        <p>
            <input type="submit" value="로그인">
            <a href="front?command=signup">회원가입</a>
        </p>
    </form>

    <p>
        <a href="front?command=boardList">← 게시판으로 돌아가기</a>
    </p>
</body>
</html>
```

### 6단계: 기존 JSP에 로그인 상태 표시 추가

`src/main/webapp/board/list.jsp` 수정 (헤더 부분):

```jsp
<!-- 기존 코드 상단에 추가 -->
<div style="text-align: right; margin: 10px;">
    <c:choose>
        <c:when test="${not empty sessionScope.user}">
                         안녕하세요, ${sessionScope.user.name}님!
            <a href="front?command=logout">로그아웃</a>
        </c:when>
        <c:otherwise>
            <a href="front?command=login">로그인</a> |
            <a href="front?command=signup">회원가입</a>
        </c:otherwise>
    </c:choose>
</div>
```

## 📝 완료 체크리스트

- [ ] UserDAO에 로그인 메서드 추가
- [ ] LoginCommand 클래스 생성
- [ ] LogoutCommand 클래스 생성
- [ ] HandlerMapping에 login, logout 추가
- [ ] login.jsp 파일 생성
- [ ] 기존 JSP에 로그인 상태 표시 추가
- [ ] 로그인/로그아웃 기능 테스트

## ⚠️ 주의사항

- **세션 관리**: 로그인 성공 시 세션에 사용자 정보 저장 필수
- **비밀번호 해시**: 회원가입과 동일한 해시 알고리즘 사용
- **에러 메시지**: 보안을 위해 구체적인 실패 원인 노출 금지
- **세션 무효화**: 로그아웃 시 반드시 세션 무효화

## 🎯 테스트 방법

1. **로그인 폼**: `http://localhost:8080/front?command=login`
2. **정상 로그인**: 가입한 아이디/비밀번호로 로그인
3. **잘못된 정보**: 잘못된 아이디/비밀번호로 로그인 시도
4. **로그아웃**: 로그인 후 로그아웃 기능 확인
5. **세션 상태**: 게시판 목록에서 로그인 상태 표시 확인

## 🎨 JSP 소스 참고

**HTML/Tailwind CSS 버전**의 로그인 폼이 `jsp/login.html`에 제공됩니다.

### 특징

- **깔끔한 디자인**: Tailwind CSS를 사용한 모던한 UI
- **사용자 경험**: 직관적인 폼 레이아웃
- **반응형 디자인**: 모든 디바이스에서 최적화된 표시

**참고**: 이 HTML 파일을 JSP로 변환하여 사용하거나, 디자인 참고용으로 활용할 수 있습니다.

## 🔗 다음 단계

로그인/로그아웃 기능이 완료되면 다음 단계인 **권한 제어 구현**을 진행합니다.

---

**2단계 완료 후**: 로그인/로그아웃이 정상 작동하는지 확인하고, 다음 단계인 권한 제어 구현을 진행합니다.
