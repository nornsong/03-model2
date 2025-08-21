# Step 3: 권한 제어 구현

## 개요

게시글 작성, 수정, 삭제에 대한 사용자 권한을 제어합니다.

## 1단계: Board 클래스 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/Board.java` | authorName 필드 추가 |

**Board.java 추가 필드:**

- `private String authorName;` - JOIN으로 가져온 작성자 이름 (화면 표시용)
- `private List<FileUpload> attachments;` - 첨부파일 목록

---

## 2단계: BoardDAO 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/BoardDAO.java` | JOIN을 통한 작성자 이름 조회, 첨부파일 로딩 |

**BoardDAO.java 주요 변경사항:**

- `getBoardList()`: `LEFT JOIN users u ON b.author = u.id` 추가
- `getBoardById()`: JOIN을 통한 작성자 이름 조회
- `boardRowMapper`: `author_name` 컬럼을 `authorName` 필드에 매핑
- `loadAttachments()`: 첨부파일 정보 로딩

---

## 3단계: Command 클래스 권한 제어 구현

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/BoardWriteCommand.java` | 로그인 확인 추가 |
| `src/main/java/io/goorm/backend/command/BoardInsertCommand.java` | 로그인 확인, 세션 사용자 ID 사용 |
| `src/main/java/io/goorm/backend/command/BoardUpdateCommand.java` | 작성자 권한 확인 |
| `src/main/java/io/goorm/backend/command/BoardDeleteCommand.java` | 작성자 권한 확인 |

**BoardWriteCommand.java:**

```java
// 로그인 확인
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("user") == null) {
    response.sendRedirect("front?command=login");
    return null;
}
```

**BoardInsertCommand.java:**

```java
// 세션에서 사용자 정보 가져오기
User user = (User) session.getAttribute("user");
board.setAuthor(user.getId().toString()); // 세션의 사용자 ID 사용
```

**BoardUpdateCommand.java:**

```java
// 권한 확인 (작성자만 수정 가능)
if (!board.getAuthor().equals(user.getId().toString())) {
    request.setAttribute("error", "수정 권한이 없습니다.");
    return "/board/view.jsp";
}
```

**BoardDeleteCommand.java:**

```java
// 권한 확인 (작성자만 삭제 가능)
if (!board.getAuthor().equals(user.getId().toString())) {
    request.setAttribute("error", "삭제 권한이 없습니다.");
    return "/board/view.jsp";
}
```

---

## 4단계: JSP 폼 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/webapp/board/write.jsp` | 작성자 필드를 세션 사용자로 자동 설정 |
| `src/main/webapp/board/update.jsp` | 작성자 필드를 읽기 전용으로 설정 |
| `src/main/webapp/board/view.jsp` | 작성자만 수정/삭제 버튼 표시 |

**write.jsp 주요 변경사항:**

```jsp
<input type="text" value="${sessionScope.user.name}" readonly>
<input type="hidden" name="author" value="${sessionScope.user.id}">
```

**update.jsp 주요 변경사항:**

```jsp
<input type="text" value="${board.authorName}" readonly>
<input type="hidden" name="author" value="${board.author}">
```

**view.jsp 주요 변경사항:**

```jsp
<!-- 작성자만 수정/삭제 버튼 표시 -->
<c:if test="${not empty sessionScope.user and sessionScope.user.id == board.author}">
    <a href="front?command=boardUpdate&id=${board.id}">수정</a>
    <a href="front?command=boardDelete&id=${board.id}">삭제</a>
</c:if>
```

---

## 5단계: 데이터베이스 컬럼 정리

**기존 board 테이블의 author 컬럼:**

- `author` 컬럼: 사용자 ID 저장 (권한 확인용)
- `authorName` 필드: JOIN으로 가져온 사용자 이름 (화면 표시용)

**주의사항:**

- 기존 데이터가 문자열 "관리자" 등으로 되어 있다면 숫자 ID로 변경 필요
- SQL: `UPDATE board SET author = '1' WHERE author = '관리자';`

---

## 완료 체크리스트

- [ ] Board 클래스에 authorName 필드 추가
- [ ] BoardDAO에서 JOIN을 통한 작성자 이름 조회 구현
- [ ] BoardWriteCommand에 로그인 확인 추가
- [ ] BoardInsertCommand에서 세션 사용자 ID 사용
- [ ] BoardUpdateCommand에 작성자 권한 확인 추가
- [ ] BoardDeleteCommand에 작성자 권한 확인 추가
- [ ] write.jsp에서 작성자 필드 자동 설정
- [ ] update.jsp에서 작성자 필드 읽기 전용 설정
- [ ] view.jsp에서 권한에 따른 버튼 표시/숨김
- [ ] 권한 제어 테스트

---

## 테스트 방법

1. **로그인하지 않은 사용자 테스트:**

   - 글쓰기 시도 시 로그인 페이지로 리다이렉트 확인
   - 수정/삭제 버튼이 보이지 않는지 확인

2. **로그인한 사용자 테스트:**

   - 글쓰기 시 작성자 필드가 자동으로 설정되는지 확인
   - 본인이 작성한 글의 수정/삭제 버튼이 보이는지 확인

3. **권한 제어 테스트:**

   - 다른 사용자의 글을 수정/삭제 시도 시 권한 오류 메시지 확인
   - 본인의 글만 수정/삭제 가능한지 확인

4. **작성자 표시 테스트:**
   - 게시글 목록과 상세보기에서 작성자 이름이 올바르게 표시되는지 확인
   - JOIN을 통한 사용자 이름 조회가 정상 작동하는지 확인
