# 2단계: JdbcTemplate 적용으로 데이터베이스 접근 개선

## 🎯 목표

기존의 순수 JDBC 코드를 Spring의 JdbcTemplate을 사용하여 더 안전하고 간결하게 만들기

## ⚠️ 중요: 기존 JDBC 코드의 문제점

**현재 상황**: `BoardDAO`에서 직접 `PreparedStatement`, `ResultSet` 사용

- **코드 중복**: CRUD 작업마다 비슷한 JDBC 코드 반복
- **예외 처리 복잡**: SQLException, ClassNotFoundException 등 다양한 예외 처리
- **리소스 관리**: Connection, PreparedStatement, ResultSet의 수동 close 필요
- **SQL 인젝션 위험**: 잘못된 파라미터 처리 시 보안 취약점

## 📚 이론 포인트 리마인드

### **JdbcTemplate의 핵심 장점**

- **템플릿 메서드 패턴**: 반복적인 JDBC 코드 제거
- **자동 리소스 관리**: Connection, Statement 자동 close
- **통일된 예외 처리**: `DataAccessException`으로 래핑
- **SQL 인젝션 방지**: PreparedStatement 자동 사용

### **Spring Framework의 역할**

- **의존성 주입**: DataSource를 통한 데이터베이스 연결 관리
- **트랜잭션 관리**: `@Transactional` 어노테이션으로 간편한 트랜잭션 제어
- **예외 변환**: JDBC 예외를 Spring의 `DataAccessException`으로 변환

### **변경 후 구조**

- **기존**: `PreparedStatement`, `ResultSet` 직접 사용
- **변경**: `JdbcTemplate.query()`, `JdbcTemplate.update()` 메서드 사용
- **코드량**: 반복적인 JDBC 코드 대폭 감소
- **안전성**: SQL 인젝션 방지 및 자동 리소스 관리

## 📋 준비사항

- 1단계 완료 (Front-end Controller Pattern 적용)
- Spring Framework 라이브러리 추가 필요

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**기존 JDBC 코드가 정상 작동하는지 확인합니다.**

1. 브라우저에서 `http://localhost:8080/front?command=boardList` 접속 확인
2. 게시글 목록이 정상적으로 표시되는지 확인
3. 글쓰기, 상세보기 등 모든 기능이 작동하는지 확인

### 1단계: Spring 라이브러리 추가

`webapp/WEB-INF/lib/` 폴더에 다음 JAR 파일들을 추가:

- `spring-core-5.3.x.jar`
- `spring-context-5.3.x.jar`
- `spring-jdbc-5.3.x.jar`
- `spring-tx-5.3.x.jar`

**라이브러리 다운로드 방법**:

1. Maven Central Repository에서 Spring Framework 5.3.x 버전 다운로드
2. 또는 Spring 공식 사이트에서 다운로드
3. 프로젝트의 `lib` 폴더에 복사

### 2단계: DataSource 설정 클래스 생성

`src/io/goorm/backend/config/DatabaseConfig.java` 생성:

```java
package io.goorm.backend.config;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;

public class DatabaseConfig {

    public static DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:~/test;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
```

### 3단계: BoardDAO를 JdbcTemplate 사용으로 변환

기존 `BoardDAO.java`를 다음과 같이 수정:

```java
package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import io.goorm.backend.config.DatabaseConfig;

import java.util.List;

public class BoardDAO {

    private JdbcTemplate jdbcTemplate;

    public BoardDAO() {
        this.jdbcTemplate = new JdbcTemplate(DatabaseConfig.getDataSource());
    }

    // RowMapper 정의
    private RowMapper<Board> boardRowMapper = (rs, rowNum) -> {
        Board board = new Board();
        board.setId(rs.getInt("id"));
        board.setTitle(rs.getString("title"));
        board.setWriter(rs.getString("writer"));
        board.setContent(rs.getString("content"));
        board.setRegDate(rs.getTimestamp("reg_date"));
        return board;
    };

    public List<Board> getBoardList() {
        String sql = "SELECT * FROM board ORDER BY id DESC";
        return jdbcTemplate.query(sql, boardRowMapper);
    }

    public Board getBoardById(int id) {
        String sql = "SELECT * FROM board WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, boardRowMapper, id);
        } catch (Exception e) {
            return null;
        }
    }

    public void insertBoard(Board board) {
        String sql = "INSERT INTO board (title, writer, content, reg_date) VALUES (?, ?, ?, NOW())";
        jdbcTemplate.update(sql, board.getTitle(), board.getWriter(), board.getContent());
    }
}
```

**주요 변경사항**:

1. **JdbcTemplate 주입**: 생성자에서 DataSource를 통해 JdbcTemplate 생성
2. **RowMapper 정의**: ResultSet을 Board 객체로 변환하는 로직을 별도 메서드로 분리
3. **query() 메서드**: SELECT 쿼리 실행 시 RowMapper 사용
4. **update() 메서드**: INSERT, UPDATE, DELETE 쿼리 실행

### 4단계: 새로운 기능 추가 (과제)

#### 과제 1: BoardUpdateCommand 구현

게시글 수정 기능을 위한 `BoardUpdateCommand` 클래스를 구현하세요.

**요구사항**:

- `Command` 인터페이스 구현
- GET 요청: 수정 폼 표시
- POST 요청: 게시글 수정 처리
- 유효성 검사 포함
- 수정 후 상세보기로 이동

#### 과제 2: BoardDeleteCommand 구현

게시글 삭제 기능을 위한 `BoardDeleteCommand` 클래스를 구현하세요.

**요구사항**:

- `Command` 인터페이스 구현
- 삭제 확인 후 처리
- 삭제 후 목록으로 이동
- 존재하지 않는 게시글 처리

#### 과제 3: BoardDAO에 검색 기능 추가

`BoardDAO`에 제목으로 게시글을 검색하는 기능을 추가하세요.

**요구사항**:

- `searchByTitle(String keyword)` 메서드 구현
- 부분 일치 검색 (LIKE 사용)
- 검색 결과가 없을 때 빈 리스트 반환

## 📝 완료 체크리스트

- [ ] Spring 라이브러리 추가 완료
- [ ] DatabaseConfig 클래스 생성
- [ ] BoardDAO를 JdbcTemplate 사용으로 변환
- [ ] 과제 3개 완료
- [ ] 테스트 실행 및 검증

## ⚠️ 주의사항

- **Spring 라이브러리 버전은 호환성 확인 필요**
- **H2 데이터베이스 연결 정보 확인**
- **예외 처리 및 로깅 고려**
- **트랜잭션 관리 필요시 @Transactional 어노테이션 사용**

## 🎯 테스트 방법

1. **게시글 목록**: `http://localhost:8080/front?command=boardList`
2. **게시글 등록**: 글쓰기 후 등록 버튼 클릭
3. **게시글 상세보기**: 목록에서 제목 클릭
4. **새로운 기능 테스트**: 수정, 삭제, 검색 기능 확인

## 🎯 과제 정답

### 과제 1 정답: BoardUpdateCommand.java

```java
package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardUpdateCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            String idStr = request.getParameter("id");

            if (request.getMethod().equals("GET")) {
                // 수정 폼 표시
                if (idStr == null || idStr.trim().isEmpty()) {
                    request.setAttribute("error", "게시글 ID가 필요합니다.");
                    return "/board/list.jsp";
                }

                int id = Integer.parseInt(idStr);
                BoardDAO dao = new BoardDAO();
                Board board = dao.getBoardById(id);

                if (board == null) {
                    request.setAttribute("error", "존재하지 않는 게시글입니다.");
                    return "/board/list.jsp";
                }

                request.setAttribute("board", board);
                return "/board/update.jsp";

            } else {
                // POST 요청 - 수정 처리
                request.setCharacterEncoding("UTF-8");

                int id = Integer.parseInt(idStr);
                String title = request.getParameter("title");
                String content = request.getParameter("content");

                if (title == null || title.trim().isEmpty()) {
                    request.setAttribute("error", "제목을 입력해주세요.");
                    return "/board/update.jsp";
                }

                Board board = new Board();
                board.setId(id);
                board.setTitle(title);
                board.setContent(content);

                BoardDAO dao = new BoardDAO();
                dao.updateBoard(board);

                // 수정 후 상세보기로 이동
                response.sendRedirect("front?command=boardView&id=" + id);
                return null;
            }

        } catch (NumberFormatException e) {
            request.setAttribute("error", "잘못된 게시글 ID입니다.");
            return "/board/list.jsp";
        } catch (Exception e) {
            request.setAttribute("error", "게시글 수정에 실패했습니다: " + e.getMessage());
            return "/board/list.jsp";
        }
    }
}
```

### 과제 2 정답: BoardDeleteCommand.java

```java
package io.goorm.backend.command;

import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardDeleteCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            String idStr = request.getParameter("id");
            String confirm = request.getParameter("confirm");

            if (idStr == null || idStr.trim().isEmpty()) {
                request.setAttribute("error", "게시글 ID가 필요합니다.");
                return "/board/list.jsp";
            }

            int id = Integer.parseInt(idStr);

            if ("true".equals(confirm)) {
                // 삭제 확인됨 - 실제 삭제 처리
                BoardDAO dao = new BoardDAO();
                boolean deleted = dao.deleteBoard(id);

                if (deleted) {
                    response.sendRedirect("front?command=boardList");
                    return null;
                } else {
                    request.setAttribute("error", "게시글 삭제에 실패했습니다.");
                    return "/board/list.jsp";
                }
            } else {
                // 삭제 확인 페이지로 이동
                request.setAttribute("id", id);
                return "/board/delete.jsp";
            }

        } catch (NumberFormatException e) {
            request.setAttribute("error", "잘못된 게시글 ID입니다.");
            return "/board/list.jsp";
        } catch (Exception e) {
            request.setAttribute("error", "게시글 삭제에 실패했습니다: " + e.getMessage());
            return "/board/list.jsp";
        }
    }
}
```

### 과제 3 정답: BoardDAO에 검색 기능 추가

```java
// BoardDAO 클래스에 추가할 메서드
public List<Board> searchByTitle(String keyword) {
    String sql = "SELECT * FROM board WHERE title LIKE ? ORDER BY id DESC";
    String searchKeyword = "%" + keyword + "%";
    return jdbcTemplate.query(sql, boardRowMapper, searchKeyword);
}

// 게시글 수정 메서드도 추가
public void updateBoard(Board board) {
    String sql = "UPDATE board SET title = ?, content = ? WHERE id = ?";
    jdbcTemplate.update(sql, board.getTitle(), board.getContent(), board.getId());
}

// 게시글 삭제 메서드도 추가
public boolean deleteBoard(int id) {
    String sql = "DELETE FROM board WHERE id = ?";
    int result = jdbcTemplate.update(sql, id);
    return result > 0;
}
```

---

**2단계 완료 후**: JdbcTemplate을 사용한 데이터베이스 접근이 정상 작동하는지 확인하고, 다음 단계인 Gradle 빌드시스템 추가를 진행합니다.
