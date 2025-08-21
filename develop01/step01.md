# Step 1: 사용자 인증 시스템 구현

## 개요

사용자 회원가입, 로그인, 로그아웃 기능을 구현합니다.

## 1단계: 사용자 모델 클래스 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/User.java` | 사용자 모델 클래스 |

**User.java 주요 필드:**

- `id`: 사용자 고유 ID (Long)
- `username`: 로그인 아이디 (String, Unique)
- `password`: 비밀번호 (String, 해시화된 값)
- `name`: 사용자 이름 (String)
- `createdAt`: 가입일시 (Timestamp)

---

## 2단계: 사용자 데이터 접근 클래스 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/UserDAO.java` | 사용자 데이터 접근 클래스 |

**UserDAO.java 주요 메서드:**

- `insertUser(User user)`: 사용자 등록
- `getUserByUsername(String username)`: 아이디로 사용자 조회
- `getUserById(Long id)`: ID로 사용자 조회

---

## 3단계: 사용자 인증 Command 클래스들 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/command/SignupCommand.java` | 회원가입 처리 |
| `src/main/java/io/goorm/backend/command/LoginCommand.java` | 로그인 처리 |
| `src/main/java/io/goorm/backend/command/LogoutCommand.java` | 로그아웃 처리 |

**SignupCommand.java 주요 기능:**

- 중복 아이디 검증
- 비밀번호 해시화 (MD5)
- 사용자 정보 DB 저장
- 회원가입 성공 시 로그인 페이지로 리다이렉트

**LoginCommand.java 주요 기능:**

- 아이디/비밀번호 검증
- 비밀번호 해시화하여 비교
- 로그인 성공 시 세션에 사용자 정보 저장
- 실패 시 에러 메시지와 함께 로그인 페이지 반환

**LogoutCommand.java 주요 기능:**

- 세션 무효화
- 로그인 페이지로 리다이렉트

---

## 4단계: 데이터베이스 테이블 생성

**SQL 스크립트:**

```sql
CREATE TABLE users (
    id IDENTITY PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);
```

---

## 5단계: HandlerMapping에 Command 등록

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/handler/HandlerMapping.java` | signup, login, logout Command 매핑 추가 |

**추가되는 매핑:**

- `"signup"` → `SignupCommand`
- `"login"` → `LoginCommand`
- `"logout"` → `LogoutCommand`

---

## 6단계: JSP 폼 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/webapp/signup.jsp` | 회원가입 폼 |
| `src/main/webapp/login.jsp` | 로그인 폼 |

**signup.jsp 주요 기능:**

- 아이디, 비밀번호, 이름 입력 필드
- 클라이언트 사이드 유효성 검사
- Tailwind CSS 스타일링

**login.jsp 주요 기능:**

- 아이디, 비밀번호 입력 필드
- 에러 메시지 표시
- Tailwind CSS 스타일링

---

## 7단계: 기존 JSP 파일 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/webapp/board/list.jsp` | 로그인 상태에 따른 글쓰기 버튼 표시 |
| `src/main/webapp/board/write.jsp` | 작성자 필드를 세션 사용자로 자동 설정 |
| `src/main/webapp/board/view.jsp` | 작성자만 수정/삭제 버튼 표시 |

**주요 변경사항:**

- `<c:if test="${not empty sessionScope.user}">` 조건부 렌더링
- `${sessionScope.user.name}` 사용자 이름 표시
- 로그인/로그아웃 링크 동적 표시

---

## 완료 체크리스트

- [ ] User 모델 클래스 생성
- [ ] UserDAO 클래스 생성
- [ ] SignupCommand, LoginCommand, LogoutCommand 생성
- [ ] users 테이블 생성
- [ ] HandlerMapping에 Command 등록
- [ ] signup.jsp, login.jsp 생성
- [ ] 기존 JSP 파일들 수정
- [ ] 회원가입 테스트
- [ ] 로그인/로그아웃 테스트
- [ ] 세션 기반 사용자 상태 표시 테스트

---

## 테스트 방법

1. **회원가입 테스트:**

   - `front?command=signup` 접근
   - 새로운 사용자 정보 입력 후 등록
   - 로그인 페이지로 리다이렉트 확인

2. **로그인 테스트:**

   - `front?command=login` 접근
   - 등록한 사용자 정보로 로그인
   - 세션에 사용자 정보 저장 확인

3. **로그아웃 테스트:**

   - 로그인 상태에서 `front?command=logout` 접근
   - 세션 무효화 및 로그인 페이지 리다이렉트 확인

4. **사용자 상태 표시 테스트:**
   - 로그인/비로그인 상태에서 상단 사용자 정보 표시 확인
   - 글쓰기 버튼 표시/숨김 확인
