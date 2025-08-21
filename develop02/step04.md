# Step 4: 게시글 상세보기에서 첨부파일 표시

## 개요

게시글 상세보기에서 첨부파일을 표시하고 다운로드할 수 있는 기능을 구현합니다.

## 1단계: BoardViewCommand 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/command/BoardViewCommand.java` | 첨부파일 정보 로딩 추가 |

**BoardViewCommand.java 주요 변경사항:**

- `FileUploadDAO`를 사용하여 첨부파일 정보 조회
- `board.setAttachments(attachments)`로 첨부파일 목록 설정
- 첨부파일 로딩 실패 시 적절한 에러 처리

**첨부파일 로딩 로직:**

```java
// 첨부파일 정보 로드
try {
    FileUploadDAO fileDAO = new FileUploadDAO();
    List<FileUpload> attachments = fileDAO.getFilesByBoardId(id);
    board.setAttachments(attachments);
    System.out.println("=== BoardViewCommand 첨부파일 로딩 ===");
    System.out.println("게시글 ID: " + id);
    System.out.println("첨부파일 개수: " + attachments.size());
    for (FileUpload file : attachments) {
        System.out.println("파일: " + file.getOriginalFilename() + " (크기: " + file.getFileSize() + ")");
    }
    System.out.println("================================");
} catch (Exception e) {
    System.out.println("첨부파일 로딩 실패: " + e.getMessage());
    e.printStackTrace();
}
```

---

## 2단계: view.jsp 수정

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/webapp/board/view.jsp` | 첨부파일 목록 표시 및 다운로드 링크 추가 |

**view.jsp 주요 변경사항:**

- 첨부파일 목록 표시 섹션 추가
- 이미지 파일과 일반 파일 구분하여 표시
- 이미지는 직접 링크, 일반 파일은 다운로드 링크

**첨부파일 표시 섹션:**

```jsp
<!-- 첨부파일 목록 -->
<c:if test="${not empty board.attachments}">
    <div class="attachments-section">
        <h3>첨부파일</h3>
        <div class="file-list">
            <c:forEach var="file" items="${board.attachments}">
                <div class="file-item">
                    <c:choose>
                        <c:when test="${file.contentType.startsWith('image/')}">
                            <!-- 이미지 파일: 직접 표시 -->
                            <div class="image-file">
                                <img src="/uploads/images/${file.storedFilename}"
                                     alt="${file.originalFilename}"
                                     class="uploaded-image"
                                     style="max-width: 200px; max-height: 200px;">
                                <div class="file-info">
                                    <span class="filename">${file.originalFilename}</span>
                                    <span class="file-size">(${file.fileSize} bytes)</span>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <!-- 일반 파일: 다운로드 링크 -->
                            <div class="regular-file">
                                <span class="file-icon">📄</span>
                                <span class="filename">${file.originalFilename}</span>
                                <span class="file-size">(${file.fileSize} bytes)</span>
                                <a href="front?command=fileDownload&id=${file.id}"
                                   class="download-link">다운로드</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>
```

---

## 3단계: FileDownloadCommand 구현

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/command/FileDownloadCommand.java` | 파일 다운로드 처리 Command |

**FileDownloadCommand.java 주요 기능:**

- 파일 ID로 파일 정보 조회
- 파일 경로 검증 및 보안 확인
- 적절한 HTTP 헤더 설정
- 파일 스트림 전송

**파일 다운로드 처리:**

```java
@Override
public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
        String fileIdStr = request.getParameter("id");
        if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
            throw new ServletException("파일 ID가 필요합니다.");
        }

        Long fileId = Long.parseLong(fileIdStr);
        FileUploadDAO fileDAO = new FileUploadDAO();
        FileUpload fileUpload = fileDAO.getFileById(fileId);

        if (fileUpload == null) {
            throw new ServletException("파일을 찾을 수 없습니다.");
        }

        // 파일 경로 검증
        String filePath = validateAndGetFilePath(fileUpload.getFilePath());
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ServletException("물리적 파일이 존재하지 않습니다.");
        }

        // 다운로드 헤더 설정
        setDownloadHeaders(response, fileUpload);

        // 파일 스트림 전송
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        return null; // 파일 다운로드는 직접 스트림으로 처리
    } catch (Exception e) {
        e.printStackTrace();
        request.setAttribute("error", "파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        return "/board/view.jsp";
    }
}
```

---

## 4단계: HandlerMapping에 Command 등록

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/handler/HandlerMapping.java` | fileDownload Command 매핑 추가 |

**HandlerMapping.java 추가 매핑:**

```java
// 파일 다운로드 명령어 추가
commandMap.put("fileDownload", new FileDownloadCommand());
```

---

## 5단계: 첨부파일 스타일링

**view.jsp에 추가되는 CSS 스타일:**

```jsp
<style>
.attachments-section {
    margin: 20px 0;
    padding: 15px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background-color: #f9fafb;
}

.file-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.file-item {
    padding: 10px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    background-color: white;
}

.image-file img {
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.file-info {
    margin-top: 8px;
    font-size: 14px;
    color: #6b7280;
}

.regular-file {
    display: flex;
    align-items: center;
    gap: 10px;
}

.file-icon {
    font-size: 20px;
}

.filename {
    font-weight: 500;
    color: #374151;
}

.file-size {
    color: #6b7280;
    font-size: 12px;
}

.download-link {
    color: #3b82f6;
    text-decoration: none;
    padding: 4px 8px;
    border: 1px solid #3b82f6;
    border-radius: 4px;
    font-size: 12px;
}

.download-link:hover {
    background-color: #3b82f6;
    color: white;
}
</style>
```

---

## 완료 체크리스트

- [ ] BoardViewCommand에 첨부파일 로딩 추가
- [ ] view.jsp에 첨부파일 표시 섹션 추가
- [ ] 이미지와 일반 파일 구분하여 표시
- [ ] FileDownloadCommand 구현
- [ ] HandlerMapping에 fileDownload 등록
- [ ] 첨부파일 스타일링 추가
- [ ] 파일 다운로드 테스트

---

## 테스트 방법

1. **첨부파일 표시 테스트:**

   - 첨부파일이 있는 게시글 상세보기 접근
   - 첨부파일 목록이 올바르게 표시되는지 확인
   - 이미지 파일이 직접 표시되는지 확인

2. **파일 다운로드 테스트:**

   - 일반 파일의 다운로드 링크 클릭
   - 파일이 정상적으로 다운로드되는지 확인
   - 파일명과 크기가 올바르게 표시되는지 확인

3. **에러 처리 테스트:**
   - 존재하지 않는 파일 ID로 다운로드 시도
   - 적절한 에러 메시지가 표시되는지 확인

---

## 다음 단계

게시글 상세보기에서 첨부파일 표시가 완료되면 **develop02의 모든 기능이 완성**됩니다.

---

## 보안 고려사항

- 파일 경로 검증으로 디렉토리 트래버설 공격 방지
- 업로드 디렉토리 외부의 파일 접근 차단
- 적절한 에러 메시지로 시스템 정보 노출 방지
- 파일 다운로드 시 권한 확인 고려 (필요시)
