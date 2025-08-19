# 1단계: 프론트엔드 컨트롤러 패턴 적용

## 🎯 목표

기존의 각 서블릿이 독립적으로 URL을 매핑하는 방식에서, 모든 요청을 하나의 `FrontController`가 받아서 내부적으로 라우팅하는 방식으로 변경합니다.

## ⚠️ 중요: 기존 서블릿 구조의 문제점

**현재 상황**: 각 서블릿이 독립적으로 URL 매핑

- `BoardListServlet` → `/board/list`
- `BoardWriteServlet` → `/board/write`
- `BoardInsertServlet` → `/board/insert`
- `BoardViewServlet` → `/board/view`

**문제점**:

- 새로운 기능 추가 시마다 서블릿과 URL 매핑 추가 필요
- 공통 로직(인증, 로깅 등) 적용이 어려움
- 코드 중복 발생

## 📚 이론 포인트 리마인드

### **프론트엔드 컨트롤러 패턴의 핵심**

- **단일 진입점**: 모든 요청이 `/front`로 들어옴
- **내부 라우팅**: `command` 파라미터로 적절한 핸들러 선택
- **공통 처리**: 인증, 로깅, 예외 처리 등을 한 곳에서 관리

### **Command Pattern의 장점**

- **확장성**: 새로운 기능을 Command 클래스로 쉽게 추가
- **유지보수성**: 각 기능이 독립적인 클래스로 분리
- **테스트 용이성**: 각 Command를 독립적으로 테스트 가능

### **변경 후 구조**

- `FrontController` → `/front?command=boardList`
- `FrontController` → `/front?command=boardWrite`
- `FrontController` → `/front?command=boardInsert`
- `FrontController` → `/front?command=boardView`

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**기존 서블릿들이 정상 작동하는지 확인합니다.**

1. 브라우저에서 `http://localhost:8080/board/list` 접속 확인
2. 게시글 목록이 정상적으로 표시되는지 확인
3. 글쓰기, 상세보기 등 모든 기능이 작동하는지 확인

### 1단계: Command 인터페이스 생성

`src/io/goorm/backend/command/` 폴더를 생성하고 `Command.java` 인터페이스를 만듭니다.

```java
package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Command {
    String execute(HttpServletRequest request, HttpServletResponse response);
}
```

### 2단계: 기존 서블릿을 Command 클래스로 변환

#### 2-1. BoardListServlet → BoardListCommand 변환 예시

**기존 BoardListServlet의 핵심 로직**:

```java
// 기존 BoardListServlet에서 가져올 부분
List<Board> boardList = dao.getBoardList();
request.setAttribute("boardList", boardList);
```

**변환된 BoardListCommand**:

```java
package io.goorm.backend.command;

import io.goorm.backend.BoardDAO;
import io.goorm.backend.Board;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class BoardListCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 기존 서블릿의 핵심 로직을 그대로 가져옴
            BoardDAO dao = new BoardDAO();
            List<Board> boardList = dao.getBoardList();

            request.setAttribute("boardList", boardList);
            return "/board/list.jsp";

        } catch (Exception e) {
            request.setAttribute("error", "게시글 목록을 불러오는데 실패했습니다: " + e.getMessage());
            return "/board/list.jsp";
        }
    }
}
```

**변환 포인트**:

1. `extends HttpServlet` → `implements Command`
2. `doGet()`, `doPost()` → `execute()` 메서드
3. 기존 비즈니스 로직은 그대로 유지
4. 반환값: JSP 경로 문자열

#### 2-2. 나머지 서블릿 변환 과제

**과제 1: BoardWriteServlet → BoardWriteCommand**

- **힌트**: 글쓰기 폼을 보여주는 것이므로 별도 로직 없음
- **반환값**: `/board/write.jsp`

**과제 2: BoardInsertServlet → BoardInsertCommand**

- **힌트**: 기존 서블릿의 POST 처리 로직을 `execute()` 메서드로 이동
- **주의사항**: 리다이렉트 시 `return null` 처리

**과제 3: BoardViewServlet → BoardViewCommand**

- **힌트**: 기존 서블릿의 게시글 조회 로직을 `execute()` 메서드로 이동
- **에러 처리**: ID 검증, 존재하지 않는 게시글 처리

**변환 규칙**:

- 기존 서블릿의 비즈니스 로직을 그대로 가져오기
- `Command` 인터페이스의 `execute()` 메서드 시그니처에 맞추기
- 적절한 JSP 경로 반환하기

### 3단계: HandlerMapping 클래스 생성

`src/io/goorm/backend/handler/` 폴더를 생성하고 `HandlerMapping.java`를 만듭니다.

```java
package io.goorm.backend.handler;

import io.goorm.backend.command.*;

import java.util.HashMap;
import java.util.Map;

public class HandlerMapping {

    private Map<String, Command> commandMap;

    public HandlerMapping() {
        commandMap = new HashMap<>();
        commandMap.put("boardList", new BoardListCommand());
        commandMap.put("boardWrite", new BoardWriteCommand());
        commandMap.put("boardInsert", new BoardInsertCommand());
        commandMap.put("boardView", new BoardViewCommand());
    }

    public Command getCommand(String commandName) {
        return commandMap.get(commandName);
    }
}
```

### 4단계: FrontController 생성

`src/io/goorm/backend/controller/` 폴더에 `FrontController.java`를 생성합니다.

```java
package io.goorm.backend.controller;

import io.goorm.backend.handler.HandlerMapping;
import io.goorm.backend.command.Command;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/front")
public class FrontController extends HttpServlet {

    private HandlerMapping handlerMapping;

    @Override
    public void init() throws ServletException {
        handlerMapping = new HandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String command = request.getParameter("command");

        if (command == null || command.trim().isEmpty()) {
            // 기본값으로 게시글 목록으로 이동
            response.sendRedirect("front?command=boardList");
            return;
        }

        try {
            Command handler = handlerMapping.getCommand(command);

            if (handler == null) {
                request.setAttribute("error", "존재하지 않는 명령입니다: " + command);
                response.sendRedirect("front?command=boardList");
                return;
            }

            String viewPage = handler.execute(request, response);

            if (viewPage != null) {
                // 포워딩
                RequestDispatcher dispatcher = request.getRequestDispatcher(viewPage);
                dispatcher.forward(request, response);
            }
            // viewPage가 null이면 리다이렉트가 이미 처리됨

        } catch (Exception e) {
            request.setAttribute("error", "요청 처리 중 오류가 발생했습니다: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/board/list.jsp");
            dispatcher.forward(request, response);
        }
    }
}
```

### 5단계: 기존 서블릿 삭제 및 web.xml 간소화

#### 5-1. 기존 서블릿 파일 삭제

다음 서블릿 파일들을 삭제합니다:

- `src/io/goorm/backend/controller/BoardListServlet.java`
- `src/io/goorm/backend/controller/BoardWriteServlet.java`
- `src/io/goorm/backend/controller/BoardInsertServlet.java`
- `src/io/goorm/backend/controller/BoardViewServlet.java`

**이유**: 이제 `FrontController`가 모든 요청을 처리하므로 개별 서블릿이 필요 없습니다.

#### 5-2. web.xml 간소화

`webapp/WEB-INF/web.xml`을 다음과 같이 간소화합니다:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <display-name>03-model2</display-name>

</web-app>
```

**간소화 이유**:

- JSTL 라이브러리는 `WEB-INF/lib`에 JAR 파일만 넣으면 자동 인식
- `<taglib>` 설정이 불필요
- 서블릿 매핑도 `@WebServlet` 애노테이션으로 처리

### 6단계: JSP 파일의 링크 수정

#### index.jsp 수정

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>게시판</title>
</head>
<body>
    <h1>게시판</h1>
    <a href="front?command=boardList">게시글 목록</a>
    <a href="front?command=boardWrite">글쓰기</a>
</body>
</html>
```

#### list.jsp 수정

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>게시글 목록</title>
</head>
<body>
    <h1>게시글 목록</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
    </c:if>

    <a href="front?command=boardWrite">글쓰기</a>

    <table border="1">
        <tr>
            <th>번호</th>
            <th>제목</th>
            <th>작성자</th>
            <th>작성일</th>
        </tr>
        <c:forEach var="board" items="${boardList}">
            <tr>
                <td>${board.id}</td>
                <td><a href="front?command=boardView&id=${board.id}">${board.title}</a></td>
                <td>${board.writer}</td>
                <td>${board.regDate}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
```

#### write.jsp 수정

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>글쓰기</title>
</head>
<body>
    <h1>글쓰기</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
    </c:if>

    <form action="front?command=boardInsert" method="post">
        <p>
            <label>제목:</label>
            <input type="text" name="title" value="${title}" required>
        </p>
        <p>
            <label>작성자:</label>
            <input type="text" name="author" value="${author}" required>
        </p>
        <p>
            <label>내용:</label>
            <textarea name="content" rows="5" cols="50">${content}</textarea>
        </p>
        <p>
            <input type="submit" value="등록">
            <a href="front?command=boardList">목록</a>
        </p>
    </form>
</body>
</html>
```

#### view.jsp 수정

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>게시글 보기</title>
</head>
<body>
    <h1>게시글 보기</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">${error}</p>
        <a href="front?command=boardList">목록으로</a>
    </c:if>

    <c:if test="${empty error}">
        <table border="1">
            <tr>
                <th>번호</th>
                <td>${board.id}</td>
            </tr>
            <tr>
                <th>제목</th>
                <td>${board.title}</td>
            </tr>
            <tr>
                <th>작성자</th>
                <td>${board.author}</td>
            </tr>
            <tr>
                <th>작성일</th>
                <td>${board.regDate}</td>
            </tr>
            <tr>
                <th>내용</th>
                <td>${board.content}</td>
            </tr>
        </table>

        <p>
            <a href="front?command=boardWrite">글쓰기</a>
            <a href="front?command=boardList">목록</a>
        </p>
    </c:if>
</body>
</html>
```

## 📝 완료 체크리스트

- [ ] Command 인터페이스 생성
- [ ] BoardListCommand 구현 및 테스트
- [ ] 나머지 Command 클래스들 구현
- [ ] HandlerMapping 클래스 생성
- [ ] FrontController 생성 및 테스트
- [ ] 기존 서블릿 파일들 삭제
- [ ] web.xml 간소화
- [ ] JSP 파일들의 링크 수정
- [ ] 전체 기능 테스트

## ⚠️ 주의사항

- **기존 서블릿 삭제 전에 FrontController가 정상 작동하는지 확인**
- **Command 클래스에서 리다이렉트 시 `return null` 처리 필수**
- **JSP 파일의 모든 링크를 `front?command=` 형식으로 수정**
- **web.xml에서 불필요한 설정 제거 (JSTL은 자동 인식)**

## 🎯 테스트 방법

1. **게시글 목록**: `http://localhost:8080/front?command=boardList`
2. **글쓰기 폼**: `http://localhost:8080/front?command=boardWrite`
3. **글쓰기 처리**: 폼에서 데이터 입력 후 제출
4. **게시글 상세보기**: 목록에서 제목 클릭

## 🎯 과제 정답

### 과제 1: BoardWriteCommand.java

```java
package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardWriteCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        // 글쓰기 폼을 보여주는 것이므로 별도 로직 없음
        return "/board/write.jsp";
    }
}
```

### 과제 2: BoardInsertCommand.java

```java
package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardInsertCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            // POST 요청 처리
            request.setCharacterEncoding("UTF-8");

            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String content = request.getParameter("content");

            if (title == null || title.trim().isEmpty()) {
                request.setAttribute("error", "제목을 입력해주세요.");
                request.setAttribute("title", title);
                request.setAttribute("author", author);
                request.setAttribute("content", content);
                return "/board/write.jsp";
            }

            Board board = new Board();
            board.setTitle(title);
            board.setAuthor(author);
            board.setContent(content);

            BoardDAO dao = new BoardDAO();
            dao.insertBoard(board);

            // 목록으로 리다이렉트
            response.sendRedirect("front?command=boardList");
            return null; // 리다이렉트 시 null 반환

        } catch (Exception e) {
            request.setAttribute("error", "게시글 등록에 실패했습니다: " + e.getMessage());
            return "/board/write.jsp";
        }
    }
}
```

### 과제 3: BoardViewCommand.java

```java
package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardViewCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                request.setAttribute("error", "게시글 ID가 필요합니다.");
                return "/board/list.jsp";
            }

            Long id = Long.parseLong(idStr);
            BoardDAO dao = new BoardDAO();
            Board board = dao.getBoardById(id);

            if (board == null) {
                request.setAttribute("error", "존재하지 않는 게시글입니다.");
                return "/board/list.jsp";
            }

            request.setAttribute("board", board);
            return "/board/view.jsp";

        } catch (NumberFormatException e) {
            request.setAttribute("error", "잘못된 게시글 ID입니다.");
            return "/board/list.jsp";
        } catch (Exception e) {
            request.setAttribute("error", "게시글을 불러오는데 실패했습니다: " + e.getMessage());
            return "/board/list.jsp";
        }
    }
}
```

---

**1단계 완료 후**: 프론트엔드 컨트롤러 패턴이 정상 작동하는지 확인하고, 다음 단계인 JdbcTemplate 적용을 진행합니다.
