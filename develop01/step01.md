# 1단계: 회원가입 기능 구현

## 🎯 목표

사용자 회원가입 기능을 구현하여 새로운 사용자가 시스템에 가입할 수 있도록 합니다.

## ⚠️ 중요: 기존 시스템의 한계

**현재 상황**: 모든 사용자가 익명으로 게시글 작성

- **보안 문제**: 작성자 식별 불가, 악의적 게시글 방지 어려움
- **권한 관리**: 게시글 수정/삭제 시 본인 확인 불가
- **사용자 관리**: 개인화된 서비스 제공 불가

## 📚 이론 포인트 리마인드

### **회원가입 시스템의 핵심**

- **사용자 식별**: 고유한 username으로 사용자 구분
- **비밀번호 보안**: 단방향 해시로 저장하여 원본 복원 불가
- **데이터 검증**: 중복 아이디, 필수 필드 검증
- **세션 관리**: 가입 후 자동 로그인 또는 로그인 페이지 이동

### **보안 고려사항**

- **비밀번호 해시**: MD5, SHA-256 등 사용 (실제 운영에서는 bcrypt 권장)
- **SQL 인젝션 방지**: PreparedStatement 사용 (JdbcTemplate에서 자동 처리)
- **XSS 방지**: JSTL 사용으로 자동 이스케이프

## 📋 준비사항

- 3단계 완료 (Gradle 빌드시스템 추가)
- Spring Framework 라이브러리 사용 가능
- H2 데이터베이스 연결 확인

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**기존 게시판 시스템이 정상 작동하는지 확인합니다.**

1. `http://localhost:8080/front?command=boardList` 접속 확인
2. 게시글 목록, 글쓰기, 상세보기 기능 확인
3. Gradle 빌드 및 Tomcat 실행 확인

### 1단계: 데이터베이스 테이블 생성

**H2 데이터베이스에 users 테이블을 생성합니다.**

```sql
CREATE TABLE IF NOT EXISTS users (
    id IDENTITY PRIMARY KEY,           -- BIGINT + auto-increment의 H2 축약형
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



```

### 2단계: User 모델 클래스 생성

`src/main/java/io/goorm/backend/User.java` 생성:

```java
package io.goorm.backend;

import java.sql.Timestamp;

public class User {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private Timestamp regDate;

    // 생성자, getter, setter 메서드들
    // (기본 생성자, 모든 필드 생성자, getter/setter)
}
```

### 3단계: UserDAO 클래스 생성

`src/main/java/io/goorm/backend/UserDAO.java` 생성:

```java
package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import io.goorm.backend.config.DatabaseConfig;

public class UserDAO {
    private JdbcTemplate jdbcTemplate;

    public UserDAO() {
        this.jdbcTemplate = new JdbcTemplate(DatabaseConfig.getDataSource());
    }

         // 사용자 등록
     public boolean insertUser(User user) {
         String sql = "INSERT INTO users (username, password, name, email) VALUES (?, ?, ?, ?)";
         try {
             int result = jdbcTemplate.update(sql,
                 user.getUsername(),
                 user.getPassword(),
                 user.getName(),
                 user.getEmail());
             return result > 0;
         } catch (Exception e) {
             return false;
         }
     }

     // 사용자명으로 사용자 조회 (중복 확인용)
     public User getUserByUsername(String username) {
         String sql = "SELECT * FROM users WHERE username = ?";
         try {
             return jdbcTemplate.queryForObject(sql, userRowMapper, username);
         } catch (Exception e) {
             return null;
         }
     }

    // RowMapper 정의
    private RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRegDate(rs.getTimestamp("reg_date"));
        return user;
    };
}
```

### 4단계: SignupCommand 클래스 생성

`src/main/java/io/goorm/backend/command/SignupCommand.java` 생성:

```java
package io.goorm.backend.command;

import io.goorm.backend.User;
import io.goorm.backend.UserDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;

public class SignupCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getMethod().equals("GET")) {
                // 회원가입 폼 표시
                return "/user/signup.jsp";
            } else {
                // POST 요청 - 회원가입 처리
                request.setCharacterEncoding("UTF-8");

                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String name = request.getParameter("name");
                String email = request.getParameter("email");

                // 유효성 검사
                if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    name == null || name.trim().isEmpty()) {
                    request.setAttribute("error", "필수 필드를 모두 입력해주세요.");
                    return "/user/signup.jsp";
                }

                // 중복 사용자명 확인
                UserDAO userDAO = new UserDAO();
                if (userDAO.getUserByUsername(username) != null) {
                    request.setAttribute("error", "이미 사용 중인 아이디입니다.");
                    return "/user/signup.jsp";
                }

                // 사용자 생성 및 저장
                User user = new User();
                user.setUsername(username);
                user.setPassword(hashPassword(password)); // 비밀번호 해시
                user.setName(name);
                user.setEmail(email);

                if (userDAO.insertUser(user)) {
                    // 회원가입 성공 - 로그인 페이지로 이동
                    response.sendRedirect("front?command=login");
                    return null;
                } else {
                    request.setAttribute("error", "회원가입에 실패했습니다.");
                    return "/user/signup.jsp";
                }
            }

        } catch (Exception e) {
            request.setAttribute("error", "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "/user/signup.jsp";
        }
    }

    // 간단한 비밀번호 해시 함수 (실제 운영에서는 bcrypt 등 사용)
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
            return password; // 해시 실패 시 원본 반환
        }
    }
}
```

### 5단계: HandlerMapping에 SignupCommand 추가

`src/main/java/io/goorm/backend/handler/HandlerMapping.java` 수정:

```java
// 기존 코드에 추가
commandMap.put("signup", new SignupCommand());
```

### 6단계: 회원가입 JSP 파일 생성

`src/main/webapp/user/signup.jsp` 생성:

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
    <h1>회원가입</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
    </c:if>

    <form action="front?command=signup" method="post">
        <p>
            <label>아이디:</label>
            <input type="text" name="username" required>
        </p>
        <p>
            <label>비밀번호:</label>
            <input type="password" name="password" required>
        </p>
        <p>
            <label>이름:</label>
            <input type="text" name="name" required>
        </p>
        <p>
            <label>이메일:</label>
            <input type="email" name="email">
        </p>
        <p>
            <input type="submit" value="회원가입">
            <a href="front?command=login">로그인</a>
        </p>
    </form>
</body>
</html>
```

## 📝 완료 체크리스트

- [ ] users 테이블 생성
- [ ] User 모델 클래스 생성
- [ ] UserDAO 클래스 생성
- [ ] SignupCommand 클래스 생성
- [ ] HandlerMapping에 signup 추가
- [ ] signup.jsp 파일 생성
- [ ] 회원가입 기능 테스트

## ⚠️ 주의사항

- **비밀번호 해시**: 실제 운영에서는 MD5 대신 bcrypt, PBKDF2 등 사용
- **중복 검증**: 사용자명 중복 확인 필수
- **유효성 검사**: 필수 필드 검증 및 적절한 에러 메시지
- **데이터베이스 연결**: H2 데이터베이스가 실행 중인지 확인

## 🎯 테스트 방법

1. **회원가입 폼**: `http://localhost:8080/front?command=signup`
2. **데이터 입력**: 아이디, 비밀번호, 이름, 이메일 입력
3. **중복 아이디 테스트**: 같은 아이디로 다시 가입 시도
4. **성공 시나리오**: 정상 가입 후 로그인 페이지 이동 확인

## 🎨 JSP 소스 참고

**HTML/Tailwind CSS 버전**의 회원가입 폼이 `jsp/signup.html`에 제공됩니다.

### 특징

- **모던한 디자인**: Tailwind CSS를 사용한 깔끔한 UI
- **클라이언트 검증**: JavaScript를 통한 실시간 폼 검증
- **반응형 레이아웃**: 모바일과 데스크톱 모두 지원
- **사용자 경험**: 직관적인 에러 메시지 및 피드백

**참고**: 이 HTML 파일을 JSP로 변환하여 사용하거나, 디자인 참고용으로 활용할 수 있습니다.

## 🔗 다음 단계

회원가입 기능이 완료되면 다음 단계인 **로그인/로그아웃 기능 구현**을 진행합니다.

---

**1단계 완료 후**: 회원가입이 정상 작동하는지 확인하고, 다음 단계인 로그인/로그아웃 구현을 진행합니다.
