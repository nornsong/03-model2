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
- Board 테이블에 author_id 필드 추가 필요
- 기존 게시글 데이터의 author_id 업데이트 필요

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**로그인/로그아웃 기능이 정상 작동하는지 확인합니다.**

1. `http://localhost:8080/front?command=login` 접속 확인
2. 테스트 사용자로 로그인/로그아웃 기능 확인
3. 게시판 목록에서 로그인 상태 표시 확인

### 1단계: Board 테이블 구조 수정

**기존 Board 테이블에 작성자 정보를 추가합니다.**

```sql
-- Board 테이블에 author_id 컬럼 추가
ALTER TABLE board ADD COLUMN author_id INT;

-- 기존 게시글의 author_id를 기본값으로 설정 (선택사항)
UPDATE board SET author_id = 1 WHERE author_id IS NULL;

-- 외래키 제약조건 추가 (선택사항)
ALTER TABLE board ADD CONSTRAINT fk_board_author
FOREIGN KEY (author_id) REFERENCES user(id);
```

### 2단계: Board 모델 클래스 수정

`src/main/java/io/goorm/backend/Board.java` 수정:

```java
// 기존 필드에 추가
    private int authorId;
    private String authorName; // 작성자 이름 (표시용)

// getter, setter 메서드 추가
    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
```

### 3단계: BoardDAO 수정

`src/main/java/io/goorm/backend/BoardDAO.java` 수정:

```java
// insertBoard 메서드 수정
public boolean insertBoard(Board board) {
    String sql = "INSERT INTO board (title, content, author_id, reg_date) VALUES (?, ?, ?, ?)";
    try {
        int result = jdbcTemplate.update(sql,
            board.getTitle(),
            board.getContent(),
            board.getAuthorId(),
            board.getRegDate());
        return result > 0;
    } catch (Exception e) {
        return false;
    }
}

// getBoardList 메서드 수정 (작성자 이름 포함)
public List<Board> getBoardList() {
            String sql = "SELECT b.*, u.name as author_name FROM board b " +
                     "LEFT JOIN user u ON b.author_id = u.id " +
                     "ORDER BY b.id DESC";
    try {
        return jdbcTemplate.query(sql, boardRowMapper);
    } catch (Exception e) {
        return new ArrayList<>();
    }
}

// getBoard 메서드 수정 (작성자 이름 포함)
public Board getBoard(int id) {
            String sql = "SELECT b.*, u.name as author_name FROM board b " +
                     "LEFT JOIN user u ON b.author_id = u.id " +
                     "WHERE b.id = ?";
    try {
        return jdbcTemplate.queryForObject(sql, boardRowMapper, id);
    } catch (Exception e) {
        return null;
    }
}

// RowMapper 수정
private RowMapper<Board> boardRowMapper = (rs, rowNum) -> {
    Board board = new Board();
    board.setId(rs.getInt("id"));
    board.setTitle(rs.getString("title"));
    board.setContent(rs.getString("content"));
            board.setAuthorId(rs.getInt("author_id"));
        board.setAuthorName(rs.getString("author_name"));
    board.setRegDate(rs.getTimestamp("reg_date"));
    return board;
};
```

### 4단계: 기존 Command 수정

**BoardWriteCommand 수정** (`src/main/java/io/goorm/backend/command/BoardWriteCommand.java`):

```java
@Override
public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
        if (request.getMethod().equals("GET")) {
            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                // 로그인하지 않은 사용자는 로그인 페이지로 이동
                response.sendRedirect("front?command=login");
                return null;
            }
            return "/board/write.jsp";
        } else {
            // POST 요청 - 게시글 작성
            request.setCharacterEncoding("UTF-8");

            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect("front?command=login");
                return null;
            }

            String title = request.getParameter("title");
            String content = request.getParameter("content");

            if (title == null || title.trim().isEmpty() ||
                content == null || content.trim().isEmpty()) {
                request.setAttribute("error", "제목과 내용을 모두 입력해주세요.");
                return "/board/write.jsp";
            }

            Board board = new Board();
            board.setTitle(title);
            board.setContent(content);
            board.setAuthorId((Integer) session.getAttribute("userId"));
            board.setRegDate(new Timestamp(System.currentTimeMillis()));

            BoardDAO boardDAO = new BoardDAO();
            if (boardDAO.insertBoard(board)) {
                response.sendRedirect("front?command=boardList");
                return null;
            } else {
                request.setAttribute("error", "게시글 작성에 실패했습니다.");
                return "/board/write.jsp";
            }
        }
    } catch (Exception e) {
        request.setAttribute("error", "게시글 작성 중 오류가 발생했습니다.");
        return "/board/write.jsp";
    }
}
```

**BoardUpdateCommand 수정** (`src/main/java/io/goorm/backend/command/BoardUpdateCommand.java`):

```java
@Override
public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
        if (request.getMethod().equals("GET")) {
            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect("front?command=login");
                return null;
            }

            int boardId = Integer.parseInt(request.getParameter("id"));
            BoardDAO boardDAO = new BoardDAO();
            Board board = boardDAO.getBoard(boardId);

            if (board == null) {
                response.sendRedirect("front?command=boardList");
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
```

### 5단계: JSP 파일 수정

**게시글 목록** (`src/main/webapp/board/list.jsp`) 수정:

```jsp
<!-- 글쓰기 버튼을 로그인한 사용자에게만 표시 -->
<c:if test="${not empty sessionScope.user}">
    <a href="front?command=boardWrite" class="write-btn">글쓰기</a>
</c:if>

<!-- 게시글 목록에서 작성자 표시 -->
                        <td>${board.authorName}</td>
```

**게시글 상세보기** (`src/main/webapp/board/view.jsp`) 수정:

```jsp
<!-- 수정/삭제 버튼을 작성자에게만 표시 -->
                <c:if test="${sessionScope.userId == board.authorId}">
    <a href="front?command=boardUpdate&id=${board.id}">수정</a>
    <a href="front?command=boardDelete&id=${board.id}"
       onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
</c:if>

<!-- 작성자 정보 표시 -->
            <p>작성자: ${board.authorName}</p>
```

### 6단계: AuthFilter 생성 (선택사항)

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
```

## 📝 완료 체크리스트

- [ ] Board 테이블에 author_id 컬럼 추가
- [ ] Board 모델 클래스에 작성자 정보 필드 추가
- [ ] BoardDAO 수정 (작성자 정보 포함)
- [ ] BoardWriteCommand에 로그인 확인 추가
- [ ] BoardUpdateCommand에 작성자 확인 추가
- [ ] JSP 파일에 권한별 UI 표시 추가
- [ ] AuthFilter 생성 (선택사항)
- [ ] 권한 제어 기능 테스트

## ⚠️ 주의사항

- **데이터베이스 구조 변경**: 기존 데이터 마이그레이션 필요
- **세션 검증**: 모든 권한 확인 시 세션 상태 확인 필수
- **작성자 확인**: 게시글 수정/삭제 시 반드시 작성자 확인
- **UI 일관성**: 권한에 따른 버튼 표시/숨김 일관성 유지

## 🎯 테스트 방법

1. **비로그인 상태**: 게시글 작성/수정/삭제 시도
2. **로그인 상태**: 정상적으로 게시글 작성
3. **작성자 확인**: 본인이 작성한 글만 수정/삭제 가능
4. **권한 제한**: 다른 사용자 글 수정/삭제 시도 시 거부
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
