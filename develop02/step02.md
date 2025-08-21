# Step 2: 파일 업로드 모델 및 DAO

## 개요

파일 업로드를 위한 모델 클래스와 데이터 접근 객체를 구현합니다.

## 1단계: FileUpload 모델 클래스 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/FileUpload.java` | 파일 업로드 모델 클래스 |

**FileUpload.java 주요 필드:**

- `id`: 파일 고유 ID (Long)
- `boardId`: 게시글 ID (Long)
- `originalFilename`: 원본 파일명 (String)
- `storedFilename`: 저장된 파일명 (String, UUID 기반)
- `filePath`: 파일 저장 경로 (String)
- `fileSize`: 파일 크기 (Long)
- `contentType`: MIME 타입 (String)
- `uploadDate`: 업로드 일시 (Timestamp)
- `fileType`: 파일 타입 구분 (String, "file" 또는 "image")
- `webUrl`: 웹 접근 URL (String, 이미지인 경우)

---

## 2단계: FileUploadDAO 클래스 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/FileUploadDAO.java` | 파일 업로드 데이터 접근 클래스 |

**FileUploadDAO.java 주요 메서드:**

- `insertFileUpload(FileUpload fileUpload)`: 파일 업로드 정보 저장
- `getFilesByBoardId(Long boardId)`: 게시글 ID로 첨부파일 목록 조회
- `getFileById(Long fileId)`: 파일 ID로 특정 파일 조회
- `deleteFile(Long fileId)`: 파일 삭제

**주요 SQL 쿼리:**

```sql
-- 파일 업로드 정보 저장
INSERT INTO file_upload (board_id, original_filename, stored_filename,
                        file_path, file_size, content_type, upload_date, file_type, web_url)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)

-- 게시글 ID로 첨부파일 목록 조회
SELECT * FROM file_upload WHERE board_id = ? ORDER BY upload_date DESC

-- 파일 ID로 특정 파일 조회
SELECT * FROM file_upload WHERE id = ?

-- 파일 삭제
DELETE FROM file_upload WHERE id = ?
```

---

## 3단계: 데이터베이스 테이블 생성

**SQL 스크립트:**

```sql
-- 파일 업로드 정보를 저장하는 테이블
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

-- 인덱스 추가 (성능 향상)
CREATE INDEX idx_file_upload_board_id ON file_upload(board_id);
CREATE INDEX idx_file_upload_stored_filename ON file_upload(stored_filename);
```

**테이블 구조 설명:**

- `board_id`: 게시글과의 외래키 관계
- `original_filename`: 사용자가 업로드한 원본 파일명
- `stored_filename`: 서버에 저장된 고유 파일명 (UUID 기반)
- `file_path`: 물리적 파일 저장 경로
- `file_size`: 파일 크기 (바이트 단위)
- `content_type`: MIME 타입 (예: image/jpeg, application/pdf)
- `file_type`: 파일 구분 ("file" 또는 "image")
- `web_url`: 웹에서 접근 가능한 URL (이미지인 경우)

---

## 4단계: Board 모델 클래스 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/Board.java` | 첨부파일 목록 필드 추가 |

**Board.java 추가 필드:**

- `private List<FileUpload> attachments;` - 첨부파일 목록

**주요 메서드:**

- `getAttachments()`: 첨부파일 목록 반환
- `setAttachments(List<FileUpload> attachments)`: 첨부파일 목록 설정
- `addAttachment(FileUpload attachment)`: 첨부파일 추가
- `removeAttachment(FileUpload attachment)`: 첨부파일 제거

---

## 5단계: BoardDAO 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/BoardDAO.java` | 첨부파일 정보 로딩 추가 |

**BoardDAO.java 주요 변경사항:**

- `getBoardById()`: 게시글 조회 후 첨부파일 정보 함께 로드
- `loadAttachments()`: 첨부파일 정보 로딩 메서드 추가
- `FileUploadDAO` 의존성 추가

**loadAttachments() 메서드:**

```java
private void loadAttachments(Board board) {
    try {
        FileUploadDAO fileUploadDAO = new FileUploadDAO();
        List<FileUpload> attachments = fileUploadDAO.getFilesByBoardId(board.getId());
        board.setAttachments(attachments);
    } catch (Exception e) {
        e.printStackTrace();
        board.setAttachments(new ArrayList<>());
    }
}
```

---

## 완료 체크리스트

- [ ] FileUpload 모델 클래스 생성
- [ ] FileUploadDAO 클래스 생성
- [ ] file_upload 테이블 생성
- [ ] Board 클래스에 첨부파일 필드 추가
- [ ] BoardDAO에 첨부파일 로딩 기능 추가
- [ ] 데이터베이스 연결 테스트
- [ ] 첨부파일 CRUD 테스트

---

## 테스트 방법

1. **데이터베이스 테이블 생성 테스트:**

   - H2 데이터베이스에서 file_upload 테이블 생성 확인
   - 외래키 제약조건 확인

2. **DAO 메서드 테스트:**

   - FileUploadDAO의 insertFileUpload 메서드 테스트
   - getFilesByBoardId 메서드 테스트
   - getFileById 메서드 테스트

3. **Board와 FileUpload 연관관계 테스트:**
   - Board 조회 시 첨부파일 정보 함께 로딩 확인
   - 첨부파일 목록이 올바르게 설정되는지 확인

---

## 다음 단계

모델 및 DAO 구현이 완료되면 다음 단계인 **게시글 작성 시 파일 업로드**를 진행합니다.
