# 📁 Step 01: 실무 수준 파일 업로드 시스템 구현

## 🎯 학습 목표

- 실무에서 사용하는 파일 업로드 구조 구현
- 파일과 이미지를 분리하여 저장 및 접근
- 화이트리스트 방식의 파일 확장자 제한
- 보안을 고려한 파일 업로드 처리
- IntelliJ + Tomcat에서 이미지 직접 접근 설정

## 🏗️ 아키텍처 개요

```
사용자 → JSP 폼 → FrontController → FileUploadCommand → 파일 타입 분류 → 적절한 폴더 저장
                ↓
            확장자 검증 (화이트리스트)
                ↓
            MIME 타입 검증
                ↓
            파일: uploads/files/ (보안 접근)
            이미지: uploads/images/ (웹 직접 접근)
```

## 📁 폴더 구조

```
📁 uploads/                    # 루트 업로드 폴더 (설정 가능)
├── 📁 files/                 # 일반 파일 (보안 접근)
│   ├── 📄 document.pdf
│   ├── 📊 report.xlsx
│   └── 📦 data.zip
└── 📁 images/                # 이미지 파일 (웹 직접 접근)
    ├── 🖼️ thumbnail.jpg
    ├── 👤 profile.png
    └── 🎨 banner.gif
```

## 📋 구현 단계

### 1단계: 업로드 설정 파일 생성

**파일 위치**: `src/main/resources/upload.properties`

```properties
# 업로드 루트 경로 (절대 경로 사용 권장)
upload.root.path=D:/uploads
upload.files.path=${upload.root.path}/files
upload.images.path=${upload.root.path}/images

# 웹에서 접근 가능한 이미지 경로
upload.web.images.path=/uploads/images

# 허용 가능한 파일 확장자 (화이트리스트)
upload.allowed.files=.pdf,.doc,.docx,.xls,.xlsx,.zip,.rar,.txt,.csv
upload.allowed.images=.jpg,.jpeg,.png,.gif,.webp,.bmp

# 파일 크기 제한 (바이트 단위)
upload.max.file.size=10485760
upload.max.image.size=5242880

# 업로드 버퍼 크기
upload.buffer.size=8192
```

### 2단계: 업로드 설정 관리 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/config/UploadConfig.java`

```java
package io.goorm.backend.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class UploadConfig {
    private static UploadConfig instance;
    private Properties properties;

    // 업로드 경로
    private String rootPath;
    private String filesPath;
    private String imagesPath;
    private String webImagesPath;

    // 허용 확장자
    private List<String> allowedFileExtensions;
    private List<String> allowedImageExtensions;

    // 파일 크기 제한
    private long maxFileSize;
    private long maxImageSize;
    private int bufferSize;

    private UploadConfig() {
        loadProperties();
        createDirectories();
    }

    public static UploadConfig getInstance() {
        if (instance == null) {
            instance = new UploadConfig();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("upload.properties")) {
            if (input == null) {
                throw new RuntimeException("upload.properties 파일을 찾을 수 없습니다.");
            }
            properties.load(input);

            // 경로 설정
            rootPath = properties.getProperty("upload.root.path");
            filesPath = properties.getProperty("upload.files.path");
            imagesPath = properties.getProperty("upload.images.path");
            webImagesPath = properties.getProperty("upload.web.images.path");

            // 확장자 설정
            String fileExts = properties.getProperty("upload.allowed.files");
            String imageExts = properties.getProperty("upload.allowed.images");

            allowedFileExtensions = Arrays.asList(fileExts.split(","));
            allowedImageExtensions = Arrays.asList(imageExts.split(","));

            // 크기 제한 설정
            maxFileSize = Long.parseLong(properties.getProperty("upload.max.file.size"));
            maxImageSize = Long.parseLong(properties.getProperty("upload.max.image.size"));
            bufferSize = Integer.parseInt(properties.getProperty("upload.buffer.size"));

        } catch (IOException e) {
            throw new RuntimeException("업로드 설정 파일 로드 실패", e);
        }
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(rootPath));
            Files.createDirectories(Paths.get(filesPath));
            Files.createDirectories(Paths.get(imagesPath));
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패", e);
        }
    }

    // Getter 메서드들
    public String getRootPath() { return rootPath; }
    public String getFilesPath() { return filesPath; }
    public String getImagesPath() { return imagesPath; }
    public String getWebImagesPath() { return webImagesPath; }
    public List<String> getAllowedFileExtensions() { return allowedFileExtensions; }
    public List<String> getAllowedImageExtensions() { return allowedImageExtensions; }
    public long getMaxFileSize() { return maxFileSize; }
    public long getMaxImageSize() { return maxImageSize; }
    public int getBufferSize() { return bufferSize; }

    // 파일 타입 판별
    public boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return allowedImageExtensions.contains(extension);
    }

    public boolean isFileFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return allowedFileExtensions.contains(extension);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
}
```

### 3단계: FileUpload 모델 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/FileUpload.java`

```java
package io.goorm.backend;

import java.sql.Timestamp;
import java.util.Objects;

public class FileUpload {
    private Long id;
    private Long boardId;
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private Timestamp uploadDate;
    private String fileType;        // "file" 또는 "image"
    private String webUrl;          // 웹에서 접근 가능한 URL (이미지인 경우)

    // 기본 생성자
    public FileUpload() {}

    // 전체 필드 생성자
    public FileUpload(Long id, Long boardId, String originalFilename,
                     String storedFilename, String filePath, Long fileSize,
                     String contentType, Timestamp uploadDate, String fileType, String webUrl) {
        this.id = id;
        this.boardId = boardId;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadDate = uploadDate;
        this.fileType = fileType;
        this.webUrl = webUrl;
    }

    // Getter 메서드들
    public Long getId() { return id; }
    public Long getBoardId() { return boardId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public String getFilePath() { return filePath; }
    public Long getFileSize() { return fileSize; }
    public String getContentType() { return contentType; }
    public Timestamp getUploadDate() { return uploadDate; }
    public String getFileType() { return fileType; }
    public String getWebUrl() { return webUrl; }

    // Setter 메서드들
    public void setId(Long id) { this.id = id; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setUploadDate(Timestamp uploadDate) { this.uploadDate = uploadDate; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setWebUrl(String webUrl) { this.webUrl = webUrl; }

    // 파일 타입 확인 메서드
    public boolean isImage() { return "image".equals(fileType); }
    public boolean isFile() { return "file".equals(fileType); }

    @Override
    public String toString() {
        return "FileUpload{" +
                "id=" + id +
                ", boardId=" + boardId +
                ", originalFilename='" + originalFilename + '\'' +
                ", storedFilename='" + storedFilename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", uploadDate=" + uploadDate +
                ", fileType='" + fileType + '\'' +
                ", webUrl='" + webUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileUpload that = (FileUpload) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### 4단계: FileUploadDAO 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/FileUploadDAO.java`

```java
package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

public class FileUploadDAO {
    private JdbcTemplate jdbcTemplate;

    public FileUploadDAO() {
        this.jdbcTemplate = new JdbcTemplate(DBConnection.getDataSource());
    }

    // 파일 업로드 정보 저장
    public boolean insertFileUpload(FileUpload fileUpload) {
        String sql = "INSERT INTO file_upload (board_id, original_filename, stored_filename, " +
                    "file_path, file_size, content_type, upload_date, file_type, web_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int result = jdbcTemplate.update(sql,
                fileUpload.getBoardId(),
                fileUpload.getOriginalFilename(),
                fileUpload.getStoredFilename(),
                fileUpload.getFilePath(),
                fileUpload.getFileSize(),
                fileUpload.getContentType(),
                fileUpload.getUploadDate(),
                fileUpload.getFileType(),
                fileUpload.getWebUrl()
            );
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시글 ID로 첨부파일 목록 조회
    public List<FileUpload> getFilesByBoardId(Long boardId) {
        String sql = "SELECT * FROM file_upload WHERE board_id = ? ORDER BY upload_date DESC";

        try {
            return jdbcTemplate.query(sql, fileUploadRowMapper, boardId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 파일 ID로 특정 파일 조회
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

    // RowMapper 정의
    private RowMapper<FileUpload> fileUploadRowMapper = (rs, rowNum) -> {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setId(rs.getLong("id"));
        fileUpload.setBoardId(rs.getLong("board_id"));
        fileUpload.setOriginalFilename(rs.getString("original_filename"));
        fileUpload.setStoredFilename(rs.getString("stored_filename"));
        fileUpload.setFilePath(rs.getString("file_path"));
        fileUpload.setFileSize(rs.getLong("file_size"));
        fileUpload.setContentType(rs.getString("content_type"));
        fileUpload.setUploadDate(rs.getTimestamp("upload_date"));
        fileUpload.setFileType(rs.getString("file_type"));
        fileUpload.setWebUrl(rs.getString("web_url"));
        return fileUpload;
    };
}
```

### 5단계: Board 모델 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/Board.java`

```java
package io.goorm.backend;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class Board {
    private Long id;
    private String title;
    private String content;
    private String author;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<FileUpload> attachments;

    // 기본 생성자
    public Board() {}

    // 전체 필드 생성자
    public Board(Long id, String title, String content, String author,
                 Timestamp createdAt, Timestamp updatedAt, List<FileUpload> attachments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    // Getter 메서드들
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public List<FileUpload> getAttachments() {
        return attachments != null ? attachments : new ArrayList<>();
    }

    // Setter 메서드들
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setAuthor(String author) { this.author = author; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public void setAttachments(List<FileUpload> attachments) {
        this.attachments = attachments;
    }

    // 첨부파일 추가 메서드
    public void addAttachment(FileUpload attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
    }

    // 첨부파일 제거 메서드
    public void removeAttachment(FileUpload attachment) {
        if (this.attachments != null) {
            this.attachments.remove(attachment);
        }
    }

    // 첨부파일 개수 조회
    public int getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }

    @Override
    public String toString() {
        return "Board{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", attachments=" + attachments +
                '}';
    }
}
```

### 6단계: BoardDAO 클래스 생성

**파일 위치**: `src/main/java/io/goorm/backend/BoardDAO.java`

```java
package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

public class BoardDAO {
    private JdbcTemplate jdbcTemplate;
    private FileUploadDAO fileUploadDAO;

    public BoardDAO() {
        this.jdbcTemplate = new JdbcTemplate(DBConnection.getDataSource());
        this.fileUploadDAO = new FileUploadDAO();
    }

    // 게시글 ID로 조회 (파일 첨부 정보 포함)
    public Board getBoardById(Long id) {
        String sql = "SELECT * FROM board WHERE id = ?";

        try {
            List<Board> results = jdbcTemplate.query(sql, boardRowMapper, id);
            if (!results.isEmpty()) {
                Board board = results.get(0);
                // 첨부파일 정보 조회
                List<FileUpload> attachments = fileUploadDAO.getFilesByBoardId(id);
                board.setAttachments(attachments);
                return board;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 게시글 목록 조회
    public List<Board> getAllBoards() {
        String sql = "SELECT * FROM board ORDER BY created_at DESC";

        try {
            return jdbcTemplate.query(sql, boardRowMapper);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 게시글 등록
    public boolean insertBoard(Board board) {
        String sql = "INSERT INTO board (title, content, author, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try {
            int result = jdbcTemplate.update(sql,
                board.getTitle(),
                board.getContent(),
                board.getAuthor(),
                board.getCreatedAt(),
                board.getUpdatedAt()
            );
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시글 수정
    public boolean updateBoard(Board board) {
        String sql = "UPDATE board SET title = ?, content = ?, updated_at = ? WHERE id = ?";

        try {
            int result = jdbcTemplate.update(sql,
                board.getTitle(),
                board.getContent(),
                board.getUpdatedAt(),
                board.getId()
            );
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시글 삭제
    public boolean deleteBoard(Long id) {
        String sql = "DELETE FROM board WHERE id = ?";

        try {
            int result = jdbcTemplate.update(sql, id);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // RowMapper 정의
    private RowMapper<Board> boardRowMapper = (rs, rowNum) -> {
        Board board = new Board();
        board.setId(rs.getLong("id"));
        board.setTitle(rs.getString("title"));
        board.setContent(rs.getString("content"));
        board.setAuthor(rs.getString("author"));
        board.setCreatedAt(rs.getTimestamp("created_at"));
        board.setUpdatedAt(rs.getTimestamp("updated_at"));
        return board;
    };
}
```

### 7단계: 파일 업로드 유틸리티 클래스 생성

#### A. 파일 검증 유틸리티

**파일 위치**: `src/main/java/io/goorm/backend/util/UploadValidator.java`

```java
package io.goorm.backend.util;

import io.goorm.backend.config.UploadConfig;
import javax.servlet.http.Part;

public class UploadValidator {
    private UploadConfig uploadConfig;

    public UploadValidator() {
        this.uploadConfig = UploadConfig.getInstance();
    }

    // 파일 확장자 검증
    public boolean isValidFileExtension(String filename) {
        String extension = FileUtils.getFileExtension(filename).toLowerCase();
        return uploadConfig.getAllowedFileExtensions().contains(extension) ||
               uploadConfig.getAllowedImageExtensions().contains(extension);
    }

    // 파일 크기 검증
    public boolean isValidFileSize(long fileSize, String filename) {
        if (uploadConfig.isImageFile(filename)) {
            return fileSize <= uploadConfig.getMaxImageSize();
        } else {
            return fileSize <= uploadConfig.getMaxFileSize();
        }
    }

    // MIME 타입 검증
    public boolean isValidMimeType(String contentType, String filename) {
        if (uploadConfig.isImageFile(filename)) {
            return contentType.startsWith("image/");
        } else {
            return !contentType.startsWith("image/");
        }
    }

    // 전체 파일 검증
    public boolean isValidFile(Part part, String filename) {
        return isValidFileExtension(filename) &&
               isValidFileSize(part.getSize(), filename) &&
               isValidMimeType(part.getContentType(), filename);
    }
}
```

#### B. 파일 처리 유틸리티

**파일 위치**: `src/main/java/io/goorm/backend/util/FileUtils.java`

```java
package io.goorm.backend.util;

import javax.servlet.http.Part;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtils {

    // Part에서 파일명 추출
    public static String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return null;
    }

    // 파일 확장자 추출
    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    // UUID 기반 파일명 생성
    public static String generateStoredFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    // 파일 저장
    public static boolean saveFile(Part part, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.copy(part.getInputStream(), path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

### 8단계: 파일 업로드 Command 생성

**파일 위치**: `src/main/java/io/goorm/backend/command/FileUploadCommand.java`

```java
package io.goorm.backend.command;

import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.config.UploadConfig;
import io.goorm.backend.util.FileUtils;
import io.goorm.backend.util.UploadValidator;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.sql.Timestamp;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 10,  // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class FileUploadCommand implements Command {
    private FileUploadDAO fileUploadDAO;
    private UploadConfig uploadConfig;
    private UploadValidator uploadValidator;

    public FileUploadCommand() {
        this.fileUploadDAO = new FileUploadDAO();
        this.uploadConfig = UploadConfig.getInstance();
        this.uploadValidator = new UploadValidator();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 게시글 ID 파라미터
            String boardIdStr = request.getParameter("boardId");
            if (boardIdStr == null || boardIdStr.trim().isEmpty()) {
                request.setAttribute("error", "게시글 ID가 필요합니다.");
                return "board/write.jsp";
            }

            Long boardId = Long.parseLong(boardIdStr);

            // 파일 파트들 처리
            for (Part part : request.getParts()) {
                if (part.getName().equals("file") && part.getSize() > 0) {
                    processFileUpload(part, boardId);
                }
            }

            request.setAttribute("message", "파일 업로드가 완료되었습니다.");
            return "board/write.jsp";

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return "board/write.jsp";
        }
    }

    private void processFileUpload(Part part, Long boardId) throws Exception {
        // 원본 파일명
        String originalFilename = FileUtils.getSubmittedFileName(part);
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return;
        }

        // 파일 검증
        if (!uploadValidator.isValidFile(part, originalFilename)) {
            throw new Exception("허용되지 않는 파일입니다: " + originalFilename);
        }

        // 파일 타입 결정 및 저장 경로 설정
        String fileType = uploadConfig.isImageFile(originalFilename) ? "image" : "file";
        String uploadDir = "image".equals(fileType) ? uploadConfig.getImagesPath() : uploadConfig.getFilesPath();

        // 저장할 파일명 생성
        String storedFilename = FileUtils.generateStoredFilename(originalFilename);

        // 업로드 디렉토리에 파일 저장
        String filePath = uploadDir + "/" + storedFilename;
        if (!FileUtils.saveFile(part, filePath)) {
            throw new Exception("파일 저장에 실패했습니다.");
        }

        // FileUpload 객체 생성
        FileUpload fileUpload = new FileUpload();
        fileUpload.setBoardId(boardId);
        fileUpload.setOriginalFilename(originalFilename);
        fileUpload.setStoredFilename(storedFilename);
        fileUpload.setFilePath(filePath);
        fileUpload.setFileSize(part.getSize());
        fileUpload.setContentType(part.getContentType());
        fileUpload.setUploadDate(new Timestamp(System.currentTimeMillis()));
        fileUpload.setFileType(fileType);

        // 웹 URL 설정 (이미지인 경우)
        if ("image".equals(fileType)) {
            String webUrl = uploadConfig.getWebImagesPath() + "/" + storedFilename;
            fileUpload.setWebUrl(webUrl);
        }

        // 데이터베이스에 저장
        if (!fileUploadDAO.insertFileUpload(fileUpload)) {
            throw new Exception("데이터베이스 저장에 실패했습니다.");
        }
    }
}
```

### 9단계: 데이터베이스 테이블 생성

**파일 위치**: `src/main/resources/sql/create_file_upload_table.sql`

```sql
-- 파일 업로드 테이블 생성
CREATE TABLE IF NOT EXISTS file_upload (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_type VARCHAR(10) NOT NULL DEFAULT 'file',
    web_url VARCHAR(500),
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_board_id ON file_upload(board_id);
CREATE INDEX idx_stored_filename ON file_upload(stored_filename);
CREATE INDEX idx_file_type ON file_upload(file_type);
CREATE INDEX idx_board_id_file_type ON file_upload(board_id, file_type);


```

### 11단계: IntelliJ + Tomcat 설정

#### A. 업로드 폴더 생성

1. 프로젝트 루트에 `uploads` 폴더 생성
2. `uploads/images` 폴더 생성
3. `uploads/files` 폴더 생성

#### B. Tomcat 설정

1. **IntelliJ** → **Run/Debug Configurations** → **Tomcat Server** → **Deployment**
2. **Application context** 설정: `/` (루트)
3. **Deployment** 탭에서 **uploads/images** 폴더를 웹 리소스로 추가

#### C. web.xml 설정 (선택사항)

**파일 위치**: `src/main/webapp/WEB-INF/web.xml`

```xml
<!-- 정적 리소스 매핑 -->
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/uploads/images/*</url-pattern>
</servlet-mapping>
```

### 10단계: HandlerMapping에 명령어 추가

**파일 위치**: `src/main/java/io/goorm/backend/handler/HandlerMapping.java`

```java
// 기존 코드에 추가
commandMap.put("fileUpload", new FileUploadCommand());
```

## 🎨 JSP 파일 활용

### 기존 JSP 파일 사용 방법

`develop02/jsp/` 폴더에 있는 다음 JSP 파일들을 그대로 사용하세요:

1. **`write.jsp`** - 파일 업로드 폼이 포함된 게시글 작성 페이지
2. **`view.jsp`** - 첨부파일 목록과 다운로드/이미지 표시가 포함된 게시글 보기 페이지
3. **`list.jsp`** - 첨부파일 개수 표시가 포함된 게시글 목록 페이지

**⚠️ 주의사항**: 이 JSP 파일들은 완성본이므로 수정하지 마세요. 파일 업로드 기능이 이미 구현되어 있습니다.

### JSP에서 파일 업로드 요청

JSP에서 파일 업로드를 다음과 같이 요청합니다:

```jsp
<form action="front?command=fileUpload" method="post" enctype="multipart/form-data">
    <input type="hidden" name="boardId" value="${board.id}">
    <input type="file" name="file" multiple>
    <button type="submit">파일 업로드</button>
</form>
```

### 이미지 표시 예시

JSP에서 이미지 파일을 다음과 같이 표시할 수 있습니다:

```jsp
<c:forEach var="file" items="${board.attachments}">
    <div class="file-item">
        <c:choose>
            <c:when test="${file.image}">
                <!-- 이미지 파일: 직접 표시 -->
                <img src="${file.webUrl}" alt="${file.originalFilename}"
                     class="uploaded-image" style="max-width: 200px;">
                <span class="filename">${file.originalFilename}</span>
            </c:when>
            <c:otherwise>
                <!-- 일반 파일: 다운로드 링크 -->
                <span class="filename">${file.originalFilename}</span>
                <a href="front?command=fileDownload&id=${file.id}"
                   class="download-link">다운로드</a>
            </c:otherwise>
        </c:choose>
    </div>
</c:forEach>
```

## ✅ 검증 체크리스트

- [ ] upload.properties 파일이 생성되었는가?
- [ ] UploadConfig.java 파일이 생성되었는가?
- [ ] FileUpload.java 파일이 생성되었는가?
- [ ] FileUploadDAO.java 파일이 생성되었는가?
- [ ] Board.java 파일이 생성되었는가?
- [ ] BoardDAO.java 파일이 생성되었는가?
- [ ] UploadValidator.java 파일이 생성되었는가?
- [ ] FileUtils.java 파일이 생성되었는가?
- [ ] FileUploadCommand.java 파일이 생성되었는가?
- [ ] HandlerMapping에 fileUpload 명령어가 추가되었는가?
- [ ] 데이터베이스 테이블이 생성되었는가?
- [ ] uploads 폴더 구조가 생성되었는가?
- [ ] IntelliJ + Tomcat 설정이 완료되었는가?
- [ ] 이미지 파일이 웹에서 직접 접근 가능한가?

## 🚀 다음 단계

다음 단계에서는 파일 다운로드 기능을 구현합니다.
[Step 02: 파일 다운로드 기능 구현](step02.md)을 참조하세요.

## 🔧 문제 해결

### 자주 발생하는 오류

1. **설정 파일 오류**: upload.properties 파일 경로와 내용 확인
2. **폴더 권한 오류**: uploads 폴더에 쓰기 권한 확인
3. **Tomcat 설정 오류**: Deployment 설정에서 이미지 폴더 매핑 확인
4. **데이터베이스 오류**: 테이블 컬럼 추가 확인

### 디버깅 팁

- 브라우저 개발자 도구의 Network 탭에서 파일 업로드 요청 확인
- 서버 로그에서 예외 메시지 확인
- 파일 시스템에서 uploads 폴더 구조 확인
- 데이터베이스에서 file_upload 테이블 구조 확인

### 보안 고려사항

- 화이트리스트 방식의 파일 확장자 제한
- MIME 타입 이중 검증
- 파일 크기 제한
- 파일 경로 검증으로 디렉토리 트래버설 공격 방지
- 이미지 폴더만 웹에서 접근 가능하도록 설정
