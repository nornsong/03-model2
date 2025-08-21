# Develop02: 파일 업로드 시스템

## 개요

게시판에 파일 업로드 기능을 추가하여 사용자가 게시글 작성 시 파일을 첨부할 수 있도록 합니다.

## 구현된 기능

### 1. 파일 업로드 기본 구조

- **업로드 설정 관리**: `upload.properties`와 `UploadConfig` 클래스로 중앙화된 설정
- **디렉토리 구조**: 파일과 이미지를 분리하여 저장
  - `uploads/files/`: 일반 파일 저장 (보안상 웹 접근 불가)
  - `uploads/images/`: 이미지 파일 저장 (웹에서 직접 접근 가능)
- **Multipart 설정**: `FrontController`에 `@MultipartConfig` 추가

### 2. 파일 업로드 모델 및 DAO

- **FileUpload 모델**: 파일 메타데이터 저장을 위한 모델 클래스
- **FileUploadDAO**: 파일 업로드 정보의 CRUD 작업
- **데이터베이스 테이블**: `file_upload` 테이블로 파일 정보 관리
- **Board 연관관계**: 게시글과 첨부파일의 1:N 관계

### 3. 게시글 작성 시 파일 업로드

- **multipart 요청 처리**: Part API를 사용한 텍스트 필드와 파일 읽기
- **파일 타입 분류**: 이미지와 일반 파일을 자동으로 구분하여 저장
- **UUID 기반 파일명**: 보안을 위한 고유 파일명 생성
- **KeyHolder 사용**: 게시글 등록 후 생성된 ID를 파일 업로드에 활용

### 4. 게시글 상세보기에서 첨부파일 표시

- **첨부파일 로딩**: `BoardViewCommand`에서 첨부파일 정보 함께 로드
- **파일 타입별 표시**: 이미지는 직접 표시, 일반 파일은 다운로드 링크
- **FileDownloadCommand**: 파일 다운로드 처리 및 보안 검증
- **스타일링**: 첨부파일 목록을 위한 CSS 스타일 적용

## 주요 클래스

| 클래스명              | 역할                             | 위치                                      |
| --------------------- | -------------------------------- | ----------------------------------------- |
| `UploadConfig`        | 업로드 설정 관리                 | `src/main/java/io/goorm/backend/config/`  |
| `FileUpload`          | 파일 업로드 모델                 | `src/main/java/io/goorm/backend/`         |
| `FileUploadDAO`       | 파일 데이터 접근                 | `src/main/java/io/goorm/backend/`         |
| `BoardInsertCommand`  | 게시글 작성 및 파일 업로드       | `src/main/java/io/goorm/backend/command/` |
| `BoardViewCommand`    | 게시글 상세보기 및 첨부파일 로딩 | `src/main/java/io/goorm/backend/command/` |
| `FileDownloadCommand` | 파일 다운로드 처리               | `src/main/java/io/goorm/backend/command/` |

## 설정 파일

| 파일명              | 역할                     | 위치                       |
| ------------------- | ------------------------ | -------------------------- |
| `upload.properties` | 업로드 경로 및 제한 설정 | `src/main/resources/`      |
| `web.xml`           | 서블릿 및 필터 설정      | `src/main/webapp/WEB-INF/` |

## 데이터베이스

### file_upload 테이블

```sql
CREATE TABLE file_upload (
    id IDENTITY PRIMARY KEY,
    board_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    file_type VARCHAR(100),
    web_url VARCHAR(500),
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);
```

## 업로드 제한

### 허용 확장자

- **이미지**: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`, `.bmp`
- **일반 파일**: `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.zip`, `.rar`, `.txt`, `.csv`

### 파일 크기 제한

- **일반 파일**: 최대 10MB
- **이미지 파일**: 최대 5MB
- **전체 요청**: 최대 50MB

## 보안 고려사항

- **화이트리스트 방식**: 허용된 확장자만 업로드 가능
- **파일 경로 검증**: 디렉토리 트래버설 공격 방지
- **UUID 파일명**: 원본 파일명 노출 방지
- **업로드 디렉토리 분리**: 일반 파일은 웹에서 직접 접근 불가
- **MIME 타입 검증**: 파일 확장자와 실제 타입 이중 검증

## 사용 방법

### 1. 게시글 작성 시 파일 첨부

```jsp
<form action="front?command=boardInsert" method="post" enctype="multipart/form-data">
    <input type="text" name="title" placeholder="제목">
    <textarea name="content" placeholder="내용"></textarea>
    <input type="file" name="files" multiple>
    <button type="submit">등록하기</button>
</form>
```

### 2. 첨부파일 표시

```jsp
<c:forEach var="file" items="${board.attachments}">
    <c:choose>
        <c:when test="${file.contentType.startsWith('image/')}">
            <img src="/uploads/images/${file.storedFilename}" alt="${file.originalFilename}">
        </c:when>
        <c:otherwise>
            <a href="front?command=fileDownload&id=${file.id}">${file.originalFilename} 다운로드</a>
        </c:otherwise>
    </c:choose>
</c:forEach>
```

## 구현 단계

1. **Step 1**: 파일 업로드 기본 구조 설정
2. **Step 2**: 파일 업로드 모델 및 DAO 구현
3. **Step 3**: 게시글 작성 시 파일 업로드
4. **Step 4**: 게시글 상세보기에서 첨부파일 표시

## 테스트 방법

1. **파일 업로드 테스트**: 다양한 타입의 파일 첨부 및 저장 확인
2. **파일 표시 테스트**: 이미지 직접 표시 및 일반 파일 다운로드 링크 확인
3. **보안 테스트**: 허용되지 않는 파일 확장자 업로드 시도
4. **에러 처리 테스트**: 파일 업로드/다운로드 실패 시나리오 확인

## 주의사항

- **업로드 경로**: `upload.properties`의 경로 설정이 실제 시스템 경로와 일치해야 함
- **Tomcat 설정**: 이미지 디렉토리를 웹에서 접근 가능하도록 설정 필요
- **파일 권한**: 업로드 디렉토리에 쓰기 권한이 있어야 함
- **데이터베이스**: `file_upload` 테이블이 생성되어 있어야 함
