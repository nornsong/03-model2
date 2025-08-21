# Step 2: 사용자 인증 구현

## 개요

사용자 로그인/로그아웃 기능과 세션 관리를 구현합니다.

## 1단계: 로그인 Command 클래스 구현

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/LoginCommand.java` | 로그인 처리 로직 구현 |

**LoginCommand.java 주요 기능:**

- 아이디/비밀번호 검증
- 비밀번호 해시화하여 비교
- 로그인 성공 시 세션에 사용자 정보 저장
- 실패 시 에러 메시지와 함께 로그인 페이지 반환

---

## 2단계: 로그아웃 Command 클래스 구현

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/LogoutCommand.java` | 로그아웃 처리 로직 구현 |

**LogoutCommand.java 주요 기능:**

- 세션 무효화 (`session.invalidate()`)
- 로그인 페이지로 리다이렉트

---

## 3단계: 세션 기반 사용자 상태 관리

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/LoginCommand.java` | 세션에 사용자 정보 저장 |
| `src/main/webapp/board/list.jsp` | 로그인 상태에 따른 UI 변경 |
| `src/main/webapp/board/write.jsp` | 세션 사용자 정보 활용 |
| `src/main/webapp/board/view.jsp` | 세션 사용자 정보 활용 |

**세션 저장 정보:**

- `session.setAttribute("user", user)` - 전체 User 객체 저장
- `session.setAttribute("userId", user.getId())` - 사용자 ID
- `session.setAttribute("userName", user.getName())` - 사용자 이름

---

## 4단계: 로그인 상태에 따른 UI 변경

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/webapp/board/list.jsp` | 로그인 상태에 따른 글쓰기 버튼 표시/숨김 |
| `src/main/webapp/board/write.jsp` | 작성자 필드를 세션 사용자로 자동 설정 |
| `src/main/webapp/board/view.jsp` | 로그인 상태에 따른 수정/삭제 버튼 표시 |

**JSTL 조건부 렌더링:**

```jsp
<!-- 로그인한 사용자에게만 글쓰기 버튼 표시 -->
<c:if test="${not empty sessionScope.user}">
    <a href="front?command=boardWrite" class="btn">글쓰기</a>
</c:if>

<!-- 로그인하지 않은 사용자에게는 로그인 링크 표시 -->
<c:if test="${empty sessionScope.user}">
    <a href="front?command=login" class="btn">로그인</a>
</c:if>
```

---

## 5단계: 사용자 권한 기반 접근 제어

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/BoardWriteCommand.java` | 로그인 확인 추가 |
| `src/main/java/io/goorm/backend/command/BoardInsertCommand.java` | 로그인 확인, 세션 사용자 ID 사용 |

**권한 확인 로직:**

```java
// 로그인 확인
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("user") == null) {
    response.sendRedirect("front?command=login");
    return null;
}

// 세션에서 사용자 정보 가져오기
User user = (User) session.getAttribute("user");
board.setAuthor(user.getId().toString()); // 세션의 사용자 ID 사용
```

---

## 6단계: 세션 보안 강화

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/LogoutCommand.java` | 세션 완전 무효화 |
| `src/main/java/io/goorm/backend/command/LoginCommand.java` | 세션 하이재킹 방지 |

**보안 고려사항:**

- 세션 타임아웃 설정
- 세션 ID 재생성
- HTTPS 사용 권장

---

## 완료 체크리스트

- [ ] LoginCommand 로그인 처리 로직 구현
- [ ] LogoutCommand 로그아웃 처리 로직 구현
- [ ] 세션에 사용자 정보 저장
- [ ] 로그인 상태에 따른 UI 변경
- [ ] 글쓰기 시 로그인 확인
- [ ] 세션 기반 사용자 권한 관리
- [ ] 로그인/로그아웃 테스트
- [ ] 세션 상태 테스트

---

## 테스트 방법

1. **로그인 테스트:**

   - `front?command=login` 접근
   - 등록한 사용자 정보로 로그인
   - 세션에 사용자 정보 저장 확인

2. **로그아웃 테스트:**

   - 로그인 상태에서 `front?command=logout` 접근
   - 세션 무효화 및 로그인 페이지 리다이렉트 확인

3. **UI 변경 테스트:**

   - 로그인/비로그인 상태에서 상단 사용자 정보 표시 확인
   - 글쓰기 버튼 표시/숨김 확인

4. **권한 제어 테스트:**
   - 비로그인 상태에서 글쓰기 시도 시 로그인 페이지로 리다이렉트 확인
   - 로그인 상태에서 정상적으로 글쓰기 가능한지 확인
