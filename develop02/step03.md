# Step 3: 게시글 작성 시 파일 업로드

## 개요

게시글 작성 시 파일을 첨부할 수 있는 기능을 구현합니다.

## 1단계: BoardInsertCommand 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/BoardInsertCommand.java` | 파일 업로드 처리 로직 추가, Part API로 파라미터 읽기 |

**BoardInsertCommand.java 주요 변경사항:**

- multipart 요청 감지 및 처리
- Part API를 사용한 텍스트 필드 읽기
- 파일 업로드 처리 로직 추가
- 파일 타입별 저장 경로 분리

**multipart 파라미터 읽기:**

```java
if (isMultipart) {
    // Part API로 파라미터 읽기
    Collection<Part> allParts = request.getParts();
    for (Part part : allParts) {
        if (part.getName().equals("title")) {
            title = getPartContent(part);
        } else if (part.getName().equals("content")) {
            content = getPartContent(part);
        }
    }
} else {
    // 일반 getParameter 사용
    title = request.getParameter("title");
    content = request.getParameter("content");
}
```

**파일 업로드 처리:**

```java
// 게시글 등록 성공 시 첨부파일 처리
if (insertResult) {
    try {
        processFileUploads(request, board.getId());
    } catch (Exception e) {
        System.out.println("파일 업로드 처리 중 오류: " + e.getMessage());
        e.printStackTrace();
    }
}
```

---

## 2단계: BoardDAO 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/BoardDAO.java` | insertBoard 후 생성된 ID를 board 객체에 설정 |

**BoardDAO.java 주요 변경사항:**

- `KeyHolder`를 사용하여 생성된 ID 가져오기
- `board.setId(generatedId.longValue())`로 생성된 ID 설정
- 파일 업로드 시 올바른 board_id 사용

**KeyHolder 사용:**

```java
KeyHolder keyHolder = new GeneratedKeyHolder();
int result = jdbcTemplate.update(connection -> {
    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    ps.setString(1, board.getTitle());
    ps.setString(2, board.getContent());
    ps.setString(3, board.getAuthor());
    return ps;
}, keyHolder);

if (result > 0) {
    Number generatedId = keyHolder.getKey();
    if (generatedId != null) {
        board.setId(generatedId.longValue());
    }
    return true;
}
```

---

## 3단계: 파일 업로드 처리 메서드 구현

**BoardInsertCommand.java에 추가되는 메서드:**

**processFileUploads():**

```java
private void processFileUploads(HttpServletRequest request, Long boardId) {
    // multipart 요청인지 확인
    String contentType = request.getContentType();
    if (!contentType.startsWith("multipart/form-data")) {
        return;
    }

    try {
        Collection<Part> allParts = request.getParts();
        for (Part part : allParts) {
            if (part.getName().equals("files") && part.getSize() > 0) {
                String fileName = getSubmittedFileName(part);
                if (fileName != null && !fileName.trim().isEmpty()) {
                    saveFile(part, fileName, boardId);
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

**saveFile():**

```java
private void saveFile(Part part, String fileName, Long boardId) {
    // 파일 확장자 확인
    String extension = getFileExtension(fileName).toLowerCase();
    boolean isImage = uploadConfig.isImageFile(fileName);

    // 저장 경로 결정
    String uploadDir = isImage ? uploadConfig.getImagesPath() : uploadConfig.getFilesPath();
    String storedFileName = UUID.randomUUID().toString() + extension;
    String filePath = uploadDir + File.separator + storedFileName;

    // 디렉토리 생성
    File dir = new File(uploadDir);
    if (!dir.exists()) {
        dir.mkdirs();
    }

    // 파일 저장
    part.write(filePath);

    // DB에 파일 정보 저장
    FileUpload fileUpload = new FileUpload();
    fileUpload.setBoardId(boardId);
    fileUpload.setOriginalFilename(fileName);
    fileUpload.setStoredFilename(storedFileName);
    fileUpload.setFilePath(filePath);
    fileUpload.setFileSize(part.getSize());
    fileUpload.setContentType(part.getContentType());
    fileUpload.setFileType(part.getContentType());
    fileUpload.setUploadDate(new Timestamp(System.currentTimeMillis()));

    FileUploadDAO fileDAO = new FileUploadDAO();
    fileDAO.insertFileUpload(fileUpload);
}
```

---

## 4단계: write.jsp 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/webapp/board/write.jsp` | `enctype="multipart/form-data"` 추가, 파일 입력 필드 추가 |

**write.jsp 주요 변경사항:**

- `<form enctype="multipart/form-data">` 추가
- 파일 입력 필드 추가
- 작성자 필드를 세션 사용자로 자동 설정

**파일 업로드 폼:**

```jsp
<form action="front?command=boardInsert" method="post" enctype="multipart/form-data">
    <!-- 제목, 내용 필드 -->

    <!-- 작성자 필드 (자동 설정) -->
    <input type="text" value="${sessionScope.user.name}" readonly>
    <input type="hidden" name="author" value="${sessionScope.user.id}">

    <!-- 파일 업로드 필드 -->
    <input type="file" name="files" multiple
           accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.txt,.zip,.rar">

    <button type="submit">등록하기</button>
</form>
```

---

## 5단계: 파일 업로드 유틸리티 메서드

**BoardInsertCommand.java에 추가되는 유틸리티 메서드:**

**getPartContent():**

```java
private String getPartContent(Part part) throws IOException {
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(part.getInputStream(), "UTF-8"))) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }
}
```

**getFileExtension():**

```java
private String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
}
```

**getSubmittedFileName():**

```java
private String getSubmittedFileName(Part part) {
    String contentDisp = part.getHeader("content-disposition");
    String[] tokens = contentDisp.split(";");
    for (String token : tokens) {
        if (token.trim().startsWith("filename")) {
            return token.substring(token.indexOf("=") + 2, token.length() - 1);
        }
    }
    return null;
}
```

---

## 완료 체크리스트

- [ ] BoardInsertCommand에 multipart 요청 처리 추가
- [ ] Part API를 사용한 텍스트 필드 읽기 구현
- [ ] 파일 업로드 처리 메서드 구현
- [ ] BoardDAO에서 생성된 ID 반환 구현
- [ ] write.jsp에 multipart 폼 설정 추가
- [ ] 파일 입력 필드 추가
- [ ] 파일 업로드 테스트

---

## 테스트 방법

1. **multipart 요청 처리 테스트:**

   - 게시글 작성 폼에서 제목/내용 입력
   - 파일 첨부
   - 등록하기 버튼 클릭
   - 서버 콘솔에서 multipart 처리 로그 확인

2. **파일 업로드 테스트:**

   - 이미지 파일과 일반 파일 첨부
   - 업로드 디렉토리에 파일 저장 확인
   - 데이터베이스에 파일 정보 저장 확인

3. **파라미터 읽기 테스트:**
   - multipart 요청에서 제목/내용이 올바르게 읽히는지 확인
   - Part API와 getParameter() 차이점 확인

---

## 다음 단계

게시글 작성 시 파일 업로드가 완료되면 다음 단계인 **게시글 상세보기에서 첨부파일 표시**를 진행합니다.
