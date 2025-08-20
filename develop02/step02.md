# 📥 Step 02: 파일 다운로드 기능 구현

## 🎯 학습 목표

- 파일 다운로드 Servlet 구현
- 보안을 고려한 파일 접근 제어
- 적절한 HTTP 헤더 설정으로 파일 다운로드 처리
- 파일 경로 검증 및 에러 처리

## 🏗️ 아키텍처 개요

```
사용자 → JSP 다운로드 링크 → FrontController → FileDownloadCommand → FileUploadDAO → 파일 시스템
                ↓
            보안 검증 (권한 확인)
                ↓
            HTTP 헤더 설정 (Content-Disposition)
                ↓
            파일 스트림 전송
```

## 📋 구현 단계

### 1단계: FileDownloadCommand 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/command/FileDownloadCommand.java`

```java
package io.goorm.backend.command;

import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloadCommand implements Command {
    private FileUploadDAO fileUploadDAO;

    public FileDownloadCommand() {
        this.fileUploadDAO = new FileUploadDAO();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {

        try {
            // 파일 ID 파라미터
            String fileIdStr = request.getParameter("id");
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                throw new ServletException("파일 ID가 필요합니다.");
            }

            Long fileId = Long.parseLong(fileIdStr);

            // 파일 정보 조회
            FileUpload fileUpload = fileUploadDAO.getFileById(fileId);
            if (fileUpload == null) {
                throw new ServletException("파일을 찾을 수 없습니다.");
            }

            // 파일 경로 검증
            Path filePath = validateAndGetFilePath(fileUpload.getFilePath());
            if (!Files.exists(filePath)) {
                throw new ServletException("물리적 파일이 존재하지 않습니다.");
            }

            // 다운로드 헤더 설정
            setDownloadHeaders(response, fileUpload);

            // 파일 스트림 전송
            streamFile(response, filePath);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
            return "board/view.jsp";
        }

        // 파일 다운로드는 직접 스트림으로 처리하므로 null 반환
        return null;
    }

    private Path validateAndGetFilePath(String filePath) throws ServletException {
        try {
            Path path = Paths.get(filePath);

            // 절대 경로 검증 (보안)
            Path uploadsDir = Paths.get("uploads").toAbsolutePath();
            if (!path.startsWith(uploadsDir)) {
                throw new ServletException("잘못된 파일 경로입니다.");
            }

            return path;
        } catch (Exception e) {
            throw new ServletException("파일 경로 검증 실패: " + e.getMessage());
        }
    }

    private void setDownloadHeaders(HttpServletResponse response, FileUpload fileUpload) {
        // Content-Type 설정
        response.setContentType("application/octet-stream");

        // Content-Disposition 설정 (다운로드 강제)
        String encodedFilename = encodeFilename(fileUpload.getOriginalFilename());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");

        // 파일 크기 설정
        response.setContentLengthLong(fileUpload.getFileSize());

        // 캐시 방지
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    private String encodeFilename(String filename) {
        try {
            // 한글 파일명을 위한 URL 인코딩
            return java.net.URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return filename;
        }
    }

    private void streamFile(HttpServletResponse response, Path filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }
    }
}
```

### 2단계: FileUploadDAO에 메소드 추가

**파일 위치**: `src/main/java/io/goorm/backend/FileUploadDAO.java`

기존 FileUploadDAO 클래스에 다음 메소드가 이미 있다면 확인하고, 없다면 추가하세요:

```java
// 파일 ID로 특정 파일 조회 (이미 step01에서 구현됨)
public FileUpload getFileById(Long fileId) {
    String sql = "SELECT * FROM file_upload WHERE id = ?";

    try {
        List<FileUpload> results = jdbcTemplate.query(sql, fileUploadRowMapper, fileId);
        return results.isEmpty() ? null : results.get(0);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
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

    // 파일 다운로드 명령어 추가
    commandMap.put("fileDownload", new FileDownloadCommand());
}
```

## 🎨 JSP 파일 활용

### 기존 JSP 파일 사용 방법

`develop02/jsp/` 폴더에 있는 다음 JSP 파일들을 그대로 사용하세요:

1. **`view.jsp`** - 첨부파일 목록과 다운로드 링크가 포함된 게시글 보기 페이지

**⚠️ 주의사항**: 이 JSP 파일은 완성본이므로 수정하지 마세요. 파일 다운로드 기능이 이미 구현되어 있습니다.

### 다운로드 링크 확인

JSP에서 파일 다운로드 링크가 다음과 같이 구현되어 있는지 확인하세요:

```jsp
<c:forEach var="file" items="${board.attachments}">
    <div class="file-item">
        <span class="filename">${file.originalFilename}</span>
        <a href="front?command=fileDownload&id=${file.id}"
           class="download-link">
            다운로드
        </a>
    </div>
</c:forEach>
```

## ✅ 검증 체크리스트

- [ ] FileDownloadCommand.java 파일이 생성되었는가?
- [ ] FileUploadDAO.getFileById() 메소드가 정상 작동하는가?
- [ ] HandlerMapping에 fileDownload 명령어가 추가되었는가?
- [ ] 파일 다운로드 링크가 정상적으로 작동하는가?
- [ ] 한글 파일명이 올바르게 다운로드되는가?
- [ ] 보안 검증이 정상적으로 작동하는가?

## 🚀 다음 단계

다음 단계에서는 파일 삭제 기능을 구현합니다.
[Step 03: 파일 삭제 기능 구현](step03.md)을 참조하세요.

## 🔧 문제 해결

### 자주 발생하는 오류

1. **파일을 찾을 수 없음**: 파일 경로가 올바른지, uploads 디렉토리가 존재하는지 확인
2. **권한 오류**: uploads 디렉토리에 읽기 권한이 있는지 확인
3. **한글 파일명 깨짐**: Content-Disposition 헤더의 인코딩 확인
4. **다운로드 안됨**: Content-Type이 "application/octet-stream"으로 설정되었는지 확인

### 디버깅 팁

- 브라우저 개발자 도구의 Network 탭에서 다운로드 요청 확인
- 서버 로그에서 예외 메시지 확인
- 파일 경로가 올바르게 설정되었는지 확인
- 데이터베이스에서 file_upload 테이블의 file_path 값 확인

### 보안 고려사항

- 파일 경로 검증으로 디렉토리 트래버설 공격 방지
- uploads 디렉토리 외부의 파일 접근 차단
- 파일 존재 여부 확인 후 다운로드 진행
- 적절한 에러 메시지로 시스템 정보 노출 방지
