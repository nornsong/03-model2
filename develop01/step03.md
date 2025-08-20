# 3단계: 권한 제어 구현

## 🎯 목표

로그인한 사용자만 게시글을 작성하고, 본인이 작성한 글만 수정/삭제할 수 있도록 권한 제어를 구현합니다.

## ⚠️ 중요: 기존 시스템의 한계

**현재 상황**: 로그인/로그아웃은 가능하지만 권한 제어 없음

- **보안 취약**: 비로그인 사용자도 게시글 작성/수정/삭제 가능
- **권한 혼재**: 본인이 작성하지 않은 글도 수정/삭제 가능
- **사용자 혼란**: 누가 어떤 글을 썼는지 구분 어려움

## 📚 이론 포인트 리마인드

### **권한 제어 시스템의 핵심**

- **인증 기반 접근**: 로그인한 사용자만 특정 기능 이용
- **작성자 확인**: 게시글의 작성자와 현재 로그인한 사용자 비교
- **세션 검증**: HttpSession을 통한 사용자 상태 확인
- **UI 조건부 표시**: 권한에 따른 버튼/링크 표시/숨김

### **보안 고려사항**

- **서버 사이드 검증**: 클라이언트 검증만으로는 부족
- **세션 무효화**: 로그아웃 시 즉시 권한 상실
- **XSS 방지**: 사용자 입력값의 안전한 출력
- **CSRF 방지**: 요청의 유효성 검증

## 📋 준비사항

- 2단계 완료 (로그인/로그아웃 기능 구현)
- Board 테이블의 author 필드에 사용자 ID 저장
- users 테이블과 JOIN으로 사용자 이름 표시

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**로그인/로그아웃 기능이 정상 작동하는지 확인합니다.**

1. `http://localhost:8080/front?command=login` 접속 확인
2. 테스트 사용자로 로그인/로그아웃 기능 확인
3. 게시판 목록에서 로그인 상태 표시 확인

### 1단계: Board 클래스 수정

**Board 클래스에 authorName 필드를 추가합니다.**

```java
// src/main/java/io/goorm/backend/Board.java
public class Board {
    private Long id;
    private String title;
    private String content;
    private String author;        // 사용자 ID (문자열)
    private String authorName;    // 사용자 이름 (JOIN으로 가져옴)
    private Timestamp createdAt;
    private List<FileUpload> attachments;

    // getter, setter 메서드 추가
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
```

### 2단계: BoardDAO 수정

**BoardDAO에서 JOIN을 사용하여 사용자 이름을 가져오도록 수정합니다.**

```java
// src/main/java/io/goorm/backend/BoardDAO.java

// 게시글 목록 조회 (JOIN으로 사용자 이름 포함)
public List<Board> getBoardList() {
    String sql = "SELECT b.*, u.name as author_name FROM board b " +
                 "LEFT JOIN users u ON b.author = u.id " +
                 "ORDER BY b.created_at DESC";
    return jdbcTemplate.query(sql, boardRowMapper);
}

// 게시글 상세 조회 (JOIN으로 사용자 이름 포함)
public Board getBoardById(Long id) {
    String sql = "SELECT b.*, u.name as author_name FROM board b " +
                 "LEFT JOIN users u ON b.author = u.id " +
                 "WHERE b.id = ?";
    // ... 기존 로직
}

// RowMapper 수정
private RowMapper<Board> boardRowMapper = (rs, rowNum) -> {
    Board board = new Board();
    board.setId(rs.getLong("id"));
    board.setTitle(rs.getString("title"));
    board.setContent(rs.getString("content"));
    board.setAuthor(rs.getString("author"));
    board.setAuthorName(rs.getString("author_name")); // JOIN으로 가져온 사용자 이름
    board.setCreatedAt(rs.getTimestamp("created_at"));
    return board;
};
```

### 3단계: Command 클래스 권한 제어 구현

**각 Command 클래스에 로그인 확인과 권한 검증을 추가합니다.**

#### 3-1. BoardWriteCommand 수정

```java
// src/main/java/io/goorm/backend/command/BoardWriteCommand.java
@Override
public String execute(HttpServletRequest request, HttpServletResponse response) {
    // 로그인 확인
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect("front?command=login");
        return null;
    }

    // 글쓰기 폼을 보여주는 것이므로 별도 로직 없음
    return "/board/write.jsp";
}
```

#### 3-2. BoardInsertCommand 수정

```java
// src/main/java/io/goorm/backend/command/BoardInsertCommand.java
@Override
public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("front?command=login");
            return null;
        }

        // POST 요청 처리
        request.setCharacterEncoding("UTF-8");
        String title = request.getParameter("title");
        String content = request.getParameter("content");

        if (title == null || title.trim().isEmpty()) {
            request.setAttribute("error", "제목을 입력해주세요.");
            request.setAttribute("title", title);
            request.setAttribute("content", content);
            return "/board/write.jsp";
        }

        // 세션에서 사용자 정보 가져오기
        User user = (User) session.getAttribute("user");

        Board board = new Board();
        board.setTitle(title);
        board.setAuthor(user.getId().toString()); // 세션의 사용자 ID 사용
        board.setContent(content);

        BoardDAO dao = new BoardDAO();
        dao.insertBoard(board);

        // 목록으로 리다이렉트
        response.sendRedirect("front?command=boardList");
        return null;
    } catch (Exception e) {
        request.setAttribute("error", "게시글 등록에 실패했습니다: " + e.getMessage());
        return "/board/write.jsp";
    }
}
```

### 4단계: JSP 폼 수정

**글쓰기 폼에서 작성자 필드를 수정 불가능하게 만들기:**

```jsp
<!-- src/main/webapp/board/write.jsp -->
<div class="mb-4">
    <label class="block text-gray-700 text-sm font-bold mb-2">작성자</label>
    <input type="text" value="${sessionScope.user.name}" readonly
           class="bg-gray-100 border border-gray-300 text-gray-700 px-3 py-2 rounded w-full">
    <input type="hidden" name="author" value="${sessionScope.user.id}">
</div>
```

**수정 폼에서도 작성자 필드를 수정 불가능하게 만들기:**

```jsp
<!-- src/main/webapp/board/update.jsp -->
<div class="mb-4">
    <label class="block text-gray-700 text-sm font-bold mb-2">작성자</label>
    <input type="text" value="${board.authorName}" readonly
           class="bg-gray-100 border border-gray-300 text-gray-700 px-3 py-2 rounded w-full">
    <input type="hidden" name="author" value="${board.author}">
</div>
```

**게시글 목록/상세에서 수정/삭제 버튼 조건부 표시:**

```jsp
<!-- src/main/webapp/board/view.jsp -->
<c:if test="${sessionScope.user.id == board.author}">
    <a href="front?command=boardUpdate&id=${board.id}"
       class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
        수정
    </a>
    <a href="front?command=boardDelete&id=${board.id}"
       class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
       onclick="return confirm('정말 삭제하시겠습니까?')">
        삭제
    </a>
</c:if>
```

### 5단계: 완료 체크리스트

**구현 완료된 기능들:**

- [x] Board 클래스에 authorName 필드 추가
- [x] BoardDAO에서 JOIN으로 사용자 이름 가져오기
- [x] BoardWriteCommand에 로그인 확인 추가
- [x] BoardInsertCommand에 권한 제어 추가
- [x] BoardUpdateCommand에 권한 제어 추가
- [x] BoardDeleteCommand에 권한 제어 추가
- [x] JSP 폼에서 작성자 필드 수정 불가능하게 만들기
- [x] 수정/삭제 버튼 조건부 표시

### 6단계: 테스트 방법

**권한 제어 기능 테스트:**

1.  **비로그인 사용자 테스트:**

    - 글쓰기 페이지 접근 시 로그인 페이지로 리다이렉트
    - 게시글 수정/삭제 시도 시 로그인 페이지로 리다이렉트

2.  **로그인 사용자 테스트:**

    - 글쓰기 폼에서 작성자 필드가 로그인한 사용자 이름으로 표시
    - 작성자 필드 수정 불가능 확인

3.  **권한 테스트:**

    - 본인이 작성한 글만 수정/삭제 가능
    - 다른 사용자가 작성한 글 수정/삭제 시도 시 권한 없음 메시지

4.  **UI 테스트:**

    - 본인이 작성한 글에만 수정/삭제 버튼 표시
    - 다른 사용자 글에는 수정/삭제 버튼 숨김
      return null;
      }

                  // 작성자 확인
                  int currentUserId = (Integer) session.getAttribute("userId");
                  if (board.getAuthorId() != currentUserId) {
                      request.setAttribute("error", "본인이 작성한 글만 수정할 수 있습니다.");
                      return "/board/view.jsp";
                  }

                  request.setAttribute("board", board);
                  return "/board/update.jsp";
              } else {
                  // POST 요청 - 게시글 수정
                  // (기존 로직에 작성자 확인 추가)
              }
          } catch (Exception e) {
              request.setAttribute("error", "게시글 수정 중 오류가 발생했습니다.");
              return "/board/list.jsp";
          }

      }

````

### 5단계: JSP 파일 수정

**게시글 목록** (`src/main/webapp/board/list.jsp`) 수정:

```jsp
<!-- 글쓰기 버튼을 로그인한 사용자에게만 표시 -->
<c:if test="${not empty sessionScope.user}">
    <a href="front?command=boardWrite" class="write-btn">글쓰기</a>
</c:if>

<!-- 게시글 목록에서 작성자 표시 -->
                        <td>${board.authorName}</td>
````

**게시글 상세보기** (`src/main/webapp/board/view.jsp`) 수정:

````jsp
<!-- 수정/삭제 버튼을 작성자에게만 표시 -->
                <c:if test="${sessionScope.userId == board.authorId}">
    <a href="front?command=boardUpdate&id=${board.id}">수정</a>
    <a href="front?command=boardDelete&id=${board.id}"
       onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
</c:if>

### 7단계: AuthFilter 생성 (선택사항)

**전역 권한 검증을 위한 필터** (`src/main/java/io/goorm/backend/filter/AuthFilter.java`):

```java
package io.goorm.backend.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/board/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String command = request.getParameter("command");

        // 로그인이 필요한 명령어들
        if (isLoginRequired(command)) {
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                httpResponse.sendRedirect("front?command=login");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isLoginRequired(String command) {
        return "boardWrite".equals(command) ||
               "boardUpdate".equals(command) ||
               "boardDelete".equals(command);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
````

## 📝 완료 체크리스트

- [x] Board 클래스에 authorName 필드 추가
- [x] BoardDAO에서 JOIN으로 사용자 이름 가져오기
- [x] BoardWriteCommand에 로그인 확인 추가
- [x] BoardInsertCommand에 권한 제어 추가
- [x] BoardUpdateCommand에 권한 제어 추가
- [x] BoardDeleteCommand에 권한 제어 추가
- [x] JSP 폼에서 작성자 필드 수정 불가능하게 만들기
- [x] 수정/삭제 버튼 조건부 표시
- [ ] AuthFilter 생성 (선택사항)

## ⚠️ 주의사항

- **세션 검증**: 모든 권한 확인 시 세션 상태 확인 필수
- **작성자 확인**: 게시글 수정/삭제 시 반드시 작성자 확인
- **UI 일관성**: 권한에 따른 버튼 표시/숨김 일관성 유지
- **데이터베이스 JOIN**: users 테이블과의 JOIN으로 사용자 이름 표시

## 🎯 테스트 방법

1. **비로그인 상태**: 게시글 작성/수정/삭제 시도 시 로그인 페이지로 리다이렉트
2. **로그인 상태**: 정상적으로 게시글 작성, 작성자 필드에 사용자 이름 표시
3. **작성자 확인**: 본인이 작성한 글만 수정/삭제 가능
4. **권한 제한**: 다른 사용자 글 수정/삭제 시도 시 권한 없음 메시지
5. **UI 표시**: 권한에 따른 버튼 표시/숨김 확인

## 🎨 JSP 소스 참고

**HTML/Tailwind CSS 버전**의 사용자 정보 표시가 `jsp/userInfo.html`에 제공됩니다.

### 특징

- **사용자 상태 표시**: 로그인/로그아웃 상태에 따른 UI 변화
- **권한별 버튼**: 사용자 권한에 따른 조건부 표시
- **반응형 디자인**: 모든 디바이스에서 최적화된 표시

**참고**: 이 HTML 파일을 JSP로 변환하여 사용하거나, 디자인 참고용으로 활용할 수 있습니다.

## 🔗 다음 단계

권한 제어 기능이 완료되면 **develop01의 모든 기능이 완성**됩니다.

---

**3단계 완료 후**: 전체 사용자 인증 및 권한 제어 시스템이 정상 작동하는지 확인하고, 다음 단계인 **develop02 (파일 업로드)** 구현을 진행합니다.
