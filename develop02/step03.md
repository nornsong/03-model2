# 🗑️ Step 03: 파일 삭제 기능 구현

## 🎯 학습 목표

- 파일 삭제 Servlet 구현
- 사용자 권한 검증 (파일 소유자만 삭제 가능)
- 물리적 파일 삭제 및 데이터베이스 정리
- 보안을 고려한 삭제 처리

## 🏗️ 아키텍처 개요

```
사용자 → JSP 삭제 버튼 → FileDeleteCommand → 권한 검증 → FileUploadDAO → 파일 시스템
                ↓
            세션 기반 사용자 확인
                ↓
            파일 소유자 권한 확인
                ↓
            물리적 파일 삭제 + DB 정리
```

## 📋 구현 단계

### 1단계: FileDeleteCommand 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/command/FileDeleteCommand.java`

```java
package io.goorm.backend.command;

import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/file/delete")
public class FileDeleteCommand extends HttpServlet {
    private FileUploadDAO fileUploadDAO;

    public FileDeleteCommand() {
        this.fileUploadDAO = new FileUploadDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
                return;
            }

            User user = (User) session.getAttribute("user");

            // 파일 ID 파라미터
            String fileIdStr = request.getParameter("fileId");
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                throw new ServletException("파일 ID가 필요합니다.");
            }

            Long fileId = Long.parseLong(fileIdStr);

            // 파일 정보 조회
            FileUpload fileUpload = fileUploadDAO.getFileById(fileId);
            if (fileUpload == null) {
                throw new ServletException("파일을 찾을 수 없습니다.");
            }

            // 권한 확인 (파일 소유자만 삭제 가능)
            if (!hasPermissionToDelete(user, fileUpload)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\": false, \"message\": \"파일을 삭제할 권한이 없습니다.\"}");
                return;
            }

            // 물리적 파일 삭제
            if (!deletePhysicalFile(fileUpload)) {
                throw new ServletException("물리적 파일 삭제에 실패했습니다.");
            }

            // 데이터베이스에서 파일 정보 삭제
            if (!fileUploadDAO.deleteFile(fileId)) {
                throw new ServletException("데이터베이스에서 파일 정보 삭제에 실패했습니다.");
            }

            // 성공 응답
            response.getWriter().write("{\"success\": true, \"message\": \"파일이 성공적으로 삭제되었습니다.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private boolean hasPermissionToDelete(User user, FileUpload fileUpload) {
        // 관리자 권한 확인 (선택사항)
        if (user.getUsername().equals("admin")) {
            return true;
        }

        // 게시글 작성자 확인 (BoardDAO를 통해 게시글 정보 조회 필요)
        // 여기서는 간단히 파일 업로드 시점의 사용자 정보로 확인
        // 실제 구현에서는 Board 테이블의 author와 비교해야 함

        // 임시로 true 반환 (실제 구현 시 수정 필요)
        return true;
    }

    private boolean deletePhysicalFile(FileUpload fileUpload) {
        try {
            Path filePath = Paths.get(fileUpload.getFilePath());

            // 파일 경로 검증 (보안)
            Path uploadsDir = Paths.get("uploads").toAbsolutePath();
            if (!filePath.startsWith(uploadsDir)) {
                return false;
            }

            // 파일 존재 확인
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }

            return true; // 파일이 이미 존재하지 않는 경우도 성공으로 처리
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

### 2단계: FileUploadDAO에 삭제 메소드 추가

**파일 위치**: `src/main/java/io/goorm/backend/FileUploadDAO.java`

기존 FileUploadDAO 클래스에 다음 메소드를 추가하세요:

```java
// 파일 삭제
public boolean deleteFile(Long fileId) {
    String sql = "DELETE FROM file_upload WHERE id = ?";

    try {
        int result = jdbcTemplate.update(sql, fileId);
        return result > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

### 3단계: HandlerMapping에 명령어 추가

**파일 위치**: `src/main/java/io/goorm/backend/handler/HandlerMapping.java`

기존 HandlerMapping 클래스의 생성자에 다음 매핑을 추가하세요:

```java
public HandlerMapping() {
    commandMap = new HashMap<>();
    // 기존 명령어들...
    commandMap.put("boardList", new BoardListCommand());
    commandMap.put("boardView", new BoardViewCommand());
    commandMap.put("boardWrite", new BoardWriteCommand());
    commandMap.put("boardInsert", new BoardInsertCommand());
    commandMap.put("boardUpdate", new BoardUpdateCommand());
    commandMap.put("boardDelete", new BoardDeleteCommand());
    commandMap.put("signup", new SignupCommand());
    commandMap.put("login", new LoginCommand());
    commandMap.put("logout", new LogoutCommand());
    commandMap.put("fileDownload", new FileDownloadCommand());

    // 파일 삭제 명령어 추가
    commandMap.put("fileDelete", new FileDeleteCommand());
}
```

### 4단계: 권한 검증 로직 개선 (선택사항)

더 정확한 권한 검증을 위해 `hasPermissionToDelete` 메소드를 다음과 같이 수정할 수 있습니다:

```java
private boolean hasPermissionToDelete(User user, FileUpload fileUpload) {
    try {
        // BoardDAO를 통해 게시글 정보 조회
        BoardDAO boardDAO = new BoardDAO();
        Board board = boardDAO.getBoardById(fileUpload.getBoardId());

        if (board == null) {
            return false;
        }

        // 관리자 권한 확인
        if (user.getUsername().equals("admin")) {
            return true;
        }

        // 게시글 작성자 확인
        return user.getUsername().equals(board.getAuthor());

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

## 🎨 JSP 파일 활용

### 기존 JSP 파일 사용 방법

`develop02/jsp/` 폴더에 있는 다음 JSP 파일들을 그대로 사용하세요:

1. **`view.jsp`** - 첨부파일 목록과 삭제 버튼이 포함된 게시글 보기 페이지

**⚠️ 주의사항**: 이 JSP 파일은 완성본이므로 수정하지 마세요. 파일 삭제 기능이 이미 구현되어 있습니다.

### 삭제 버튼 확인

JSP에서 파일 삭제 버튼이 다음과 같이 구현되어 있는지 확인하세요:

```jsp
<c:forEach var="file" items="${board.attachments}">
    <div class="file-item">
        <span class="filename">${file.originalFilename}</span>
        <a href="front?command=fileDownload&id=${file.id}"
           class="download-link">다운로드</a>

        <!-- 로그인한 사용자만 삭제 버튼 표시 -->
        <c:if test="${not empty sessionScope.user}">
            <button onclick="deleteFile(${file.id})"
                    class="delete-btn">삭제</button>
        </c:if>
    </div>
</c:forEach>

<!-- 파일 삭제 JavaScript 함수 -->
<script>
function deleteFile(fileId) {
    if (confirm('정말로 이 파일을 삭제하시겠습니까?')) {
        fetch('front?command=fileDelete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'fileId=' + fileId
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('파일이 삭제되었습니다.');
                location.reload(); // 페이지 새로고침
            } else {
                alert('파일 삭제 실패: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('파일 삭제 중 오류가 발생했습니다.');
        });
    }
}
</script>
```

## ✅ 검증 체크리스트

- [ ] FileDeleteCommand.java 파일이 생성되었는가?
- [ ] FileUploadDAO.deleteFile() 메소드가 정상 작동하는가?
- [ ] HandlerMapping에 fileDelete 명령어가 추가되었는가?
- [ ] 파일 삭제 버튼이 정상적으로 작동하는가?
- [ ] 권한 검증이 정상적으로 작동하는가?
- [ ] 물리적 파일과 데이터베이스 정보가 모두 삭제되는가?

## 🔧 문제 해결

### 자주 발생하는 오류

1. **권한 오류**: 로그인 상태와 사용자 권한 확인
2. **파일 삭제 실패**: 파일 경로와 권한 확인
3. **데이터베이스 오류**: 외래키 제약조건 확인
4. **JavaScript 오류**: 브라우저 콘솔에서 에러 메시지 확인

### 디버깅 팁

- 브라우저 개발자 도구의 Network 탭에서 삭제 요청 확인
- 서버 로그에서 예외 메시지 확인
- 데이터베이스에서 file_upload 테이블 데이터 확인
- 파일 시스템에서 uploads 디렉토리 내용 확인

### 보안 고려사항

- 로그인 상태 확인으로 인증되지 않은 사용자 차단
- 파일 소유자 권한 확인으로 무단 삭제 방지
- 파일 경로 검증으로 디렉토리 트래버설 공격 방지
- 적절한 에러 메시지로 시스템 정보 노출 방지
- CSRF 토큰 사용 고려 (향후 보안 강화 시)
