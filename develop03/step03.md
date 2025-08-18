# Step03: 대용량 파일 업로드 처리 (Java IO 스트림 실습)

## 🎯 목표

Java IO 스트림을 활용하여 대용량 파일을 청크 단위로 효율적으로 업로드하는 시스템을 구현합니다.
**IO 스트림의 기본 개념과 실무 활용법을 동시에 학습**합니다.

## ⚠️ 중요: 왜 IO 스트림을 배워야 하는가?

### 기존 시스템의 문제점

- **메모리 부족**: 대용량 파일을 메모리에 완전히 로드하여 OutOfMemoryError 발생
- **업로드 실패**: 네트워크 문제나 서버 재시작 시 전체 업로드 실패
- **사용자 경험**: 업로드 진행 상황을 알 수 없어 답답함
- **서버 부하**: 대용량 파일 처리로 인한 서버 응답 지연

### IO 스트림을 통한 해결

- **스트리밍 처리**: 파일을 작은 조각으로 나누어 순차적으로 처리
- **메모리 효율성**: 한 번에 전체 파일을 로드하지 않고 청크 단위로 처리
- **안정성**: 일부 청크 실패 시에도 다른 청크는 성공적으로 처리
- **진행률 표시**: 실시간으로 업로드 진행 상황을 사용자에게 제공

## 📚 이론 포인트 리마인드

### 1. Java IO 스트림의 핵심 개념

#### InputStream (입력 스트림)

- **역할**: 데이터를 읽어오는 통로
- **주요 메서드**: `read()`, `read(byte[])`, `read(byte[], int, int)`
- **특징**: 바이트 단위로 데이터를 읽음

#### OutputStream (출력 스트림)

- **역할**: 데이터를 쓰는 통로
- **주요 메서드**: `write()`, `write(byte[])`, `write(byte[], int, int)`
- **특징**: 바이트 단위로 데이터를 씀

#### BufferedInputStream/BufferedOutputStream

- **역할**: 버퍼를 사용하여 성능 향상
- **장점**: 작은 읽기/쓰기 작업을 모아서 한 번에 처리
- **사용법**: 기존 스트림을 감싸서 사용

### 2. 청크 업로드 원리

- **파일 분할**: 대용량 파일을 작은 조각(청크)으로 분할
- **순차 업로드**: 청크를 순서대로 서버에 전송
- **재조립**: 서버에서 받은 청크들을 원본 파일로 재조립
- **검증**: 각 청크의 무결성 및 순서 검증

### 3. 성능 최적화 포인트

- **청크 크기**: 1MB~5MB가 일반적으로 최적 (너무 작으면 오버헤드, 너무 크면 메모리 부담)
- **버퍼 크기**: 적절한 버퍼 크기로 읽기/쓰기 성능 향상
- **리소스 관리**: `try-with-resources`로 자동 리소스 해제

## 🚀 실습 단계별 진행

### 1단계: 청크 업로드 핸들러 생성 (IO 스트림 실습)

#### ChunkUploadHandler.java 생성

```java
package io.goorm.backend.service;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Java IO 스트림을 활용한 청크 업로드 처리 서비스
 * InputStream, OutputStream, BufferedInputStream 등을 실습
 */
public class ChunkUploadHandler {

    // 업로드 중인 파일들을 메모리에 저장 (실제로는 Redis 등 사용 권장)
    private static final ConcurrentHashMap<String, UploadSession> uploadSessions = new ConcurrentHashMap<>();

    // 청크 크기 (2MB - 일반적으로 최적)
    private static final int CHUNK_SIZE = 2 * 1024 * 1024;

    // 버퍼 크기 (8KB - IO 성능 최적화)
    private static final int BUFFER_SIZE = 8 * 1024;

    /**
     * 업로드 세션 정보를 담는 내부 클래스
     */
    private static class UploadSession {
        String uploadId;
        String fileName;
        long fileSize;
        int totalChunks;
        int uploadedChunks;
        String tempDir;

        UploadSession(String uploadId, String fileName, long fileSize) {
            this.uploadId = uploadId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
            this.uploadedChunks = 0;
            this.tempDir = "uploads/chunks/" + uploadId;
        }
    }

    /**
     * 새로운 업로드 세션 시작
     * Path API와 디렉토리 생성 실습
     */
    public String startUpload(String fileName, long fileSize) {
        String uploadId = UUID.randomUUID().toString();
        UploadSession session = new UploadSession(uploadId, fileName, fileSize);

        // 임시 디렉토리 생성 (Files.createDirectories 실습)
        try {
            Files.createDirectories(Paths.get(session.tempDir));
            uploadSessions.put(uploadId, session);
            return uploadId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 청크 데이터 업로드
     * ByteArrayInputStream과 Files.write 실습
     */
    public boolean uploadChunk(String uploadId, int chunkNumber, byte[] chunkData) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) return false;

        try {
            // 청크 파일 저장 (Files.write 실습)
            String chunkPath = session.tempDir + "/chunk_" + chunkNumber;
            Files.write(Paths.get(chunkPath), chunkData);

            session.uploadedChunks++;

            // 모든 청크가 업로드되었는지 확인
            if (session.uploadedChunks == session.totalChunks) {
                return assembleFile(session);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 청크들을 원본 파일로 재조립
     * FileInputStream, FileOutputStream, BufferedInputStream 실습
     */
    private boolean assembleFile(UploadSession session) {
        try {
            String outputPath = "uploads/files/" + session.uploadId + "_" + session.fileName;
            Path output = Paths.get(outputPath);

            Files.createDirectories(output.getParent());

            // try-with-resources로 리소스 자동 해제 실습
            try (FileOutputStream out = new FileOutputStream(output.toFile());
                 BufferedOutputStream bufferedOut = new BufferedOutputStream(out, BUFFER_SIZE)) {

                // 청크들을 순서대로 합치기
                for (int i = 1; i <= session.totalChunks; i++) {
                    String chunkPath = session.tempDir + "/chunk_" + i;
                    Path chunk = Paths.get(chunkPath);

                    if (Files.exists(chunk)) {
                        // FileInputStream과 BufferedInputStream으로 청크 읽기
                        try (FileInputStream chunkIn = new FileInputStream(chunk.toFile());
                             BufferedInputStream bufferedChunkIn = new BufferedInputStream(chunkIn, BUFFER_SIZE)) {

                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;

                            // 버퍼를 사용한 효율적인 읽기/쓰기 실습
                            while ((bytesRead = bufferedChunkIn.read(buffer)) != -1) {
                                bufferedOut.write(buffer, 0, bytesRead);
                            }
                        }
                    } else {
                        return false;
                    }
                }

                // 버퍼의 남은 데이터를 강제로 출력
                bufferedOut.flush();
            }

            // 임시 파일들 정리
            cleanupTempFiles(session);
            uploadSessions.remove(session.uploadId);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 임시 파일들 정리
     * Files.walk와 스트림 API 실습
     */
    private void cleanupTempFiles(UploadSession session) {
        try {
            Path tempDir = Paths.get(session.tempDir);
            if (Files.exists(tempDir)) {
                // Files.walk로 디렉토리 순회 실습
                Files.walk(tempDir)
                     .sorted((a, b) -> b.compareTo(a)) // 하위 파일부터 삭제
                     .forEach(path -> {
                         try {
                             Files.deleteIfExists(path);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 업로드 진행률 조회
     */
    public double getProgress(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) return 0.0;
        return (double) session.uploadedChunks / session.totalChunks;
    }
}
```

### 2단계: 청크 업로드 Command 생성

#### ChunkUploadCommand.java 생성

```java
package io.goorm.backend.command;

import io.goorm.backend.service.ChunkUploadHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 청크 업로드 처리를 위한 Command
 * ServletInputStream과 바이트 배열 처리 실습
 */
public class ChunkUploadCommand implements Command {

    private ChunkUploadHandler uploadHandler = new ChunkUploadHandler();

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "start":
                    return handleStartUpload(request, response);
                case "upload":
                    return handleChunkUpload(request, response);
                case "progress":
                    return handleProgressCheck(request, response);
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Upload failed");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 업로드 시작 처리
     */
    private String handleStartUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = request.getParameter("fileName");
        long fileSize = Long.parseLong(request.getParameter("fileSize"));

        String uploadId = uploadHandler.startUpload(fileName, fileSize);

        // JSON 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"uploadId\":\"" + uploadId + "\"}");

        return null; // 직접 응답하므로 null 반환
    }

    /**
     * 청크 업로드 처리
     * ServletInputStream으로 바이트 데이터 읽기 실습
     */
    private String handleChunkUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uploadId = request.getParameter("uploadId");
        int chunkNumber = Integer.parseInt(request.getParameter("chunkNumber"));

        // ServletInputStream으로 청크 데이터 읽기
        try (InputStream inputStream = request.getInputStream()) {
            // ByteArrayOutputStream으로 바이트 데이터 수집 실습
            byte[] chunkData = readInputStreamToByteArray(inputStream);

            boolean success = uploadHandler.uploadChunk(uploadId, chunkNumber, chunkData);

            // JSON 응답
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":" + success + "}");
        }

        return null;
    }

    /**
     * InputStream을 바이트 배열로 변환
     * ByteArrayOutputStream 실습
     */
    private byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192]; // 8KB 버퍼
            int bytesRead;

            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer.toByteArray();
        }
    }

    /**
     * 진행률 확인
     */
    private String handleProgressCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uploadId = request.getParameter("uploadId");
        double progress = uploadHandler.getProgress(uploadId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
            "{\"progress\":%.2f,\"percentage\":%d}",
            progress,
            (int)(progress * 100)
        ));

        return null;
    }
}
```

### 3단계: write.jsp에 청크 업로드 UI 추가

#### write.jsp 수정 (기존 파일 업로드 섹션에 추가)

```jsp
<!-- 청크 업로드 섹션 추가 -->
<div class="mt-6">
    <label class="block text-sm font-medium text-gray-700 mb-2">
        대용량 파일 업로드 <span class="text-gray-500">(50MB 이상)</span>
    </label>

    <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors">
        <svg class="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
        </svg>
        <p class="text-sm text-gray-600 mb-2">
            <span class="font-medium text-blue-600">클릭하여 대용량 파일 선택</span>
        </p>
        <p class="text-xs text-gray-500">
            Java IO 스트림을 사용한 안정적인 청크 업로드
        </p>
        <input type="file" id="chunkFileInput" class="mt-3">
    </div>

    <!-- 업로드 진행률 표시 -->
    <div id="chunkUploadProgress" class="mt-4 hidden">
        <div class="bg-gray-100 rounded-full h-2 mb-2">
            <div id="chunkProgressBar" class="bg-blue-600 h-2 rounded-full transition-all duration-300" style="width: 0%"></div>
        </div>
        <div class="flex justify-between text-sm text-gray-600">
            <span id="chunkProgressText">0%</span>
            <span id="chunkStatusText">업로드 준비 중...</span>
        </div>
    </div>
</div>

<script>
// 간단한 청크 업로드 JavaScript
let currentUploadId = null;

// 파일 선택 이벤트
document.getElementById('chunkFileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    // 50MB 이상인 파일만 청크 업로드
    if (file.size < 50 * 1024 * 1024) {
        alert('50MB 미만의 파일은 일반 업로드를 사용하세요.');
        return;
    }

    startChunkUpload(file);
});

// 청크 업로드 시작
async function startChunkUpload(file) {
    try {
        // 업로드 시작 요청
        const formData = new FormData();
        formData.append('action', 'start');
        formData.append('fileName', file.name);
        formData.append('fileSize', file.size);

        const response = await fetch('front?command=chunkUpload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();
        currentUploadId = result.uploadId;

        // UI 표시
        document.getElementById('chunkUploadProgress').classList.remove('hidden');
        document.getElementById('chunkStatusText').textContent = '업로드 시작...';

        // 청크 업로드 시작
        uploadChunks(file);

    } catch (error) {
        console.error('Upload start failed:', error);
        alert('업로드 시작에 실패했습니다.');
    }
}

// 청크 업로드 실행
async function uploadChunks(file) {
    const chunkSize = 2 * 1024 * 1024; // 2MB
    const totalChunks = Math.ceil(file.size / chunkSize);

    for (let i = 0; i < totalChunks; i++) {
        const start = i * chunkSize;
        const end = Math.min(start + chunkSize, file.size);
        const chunk = file.slice(start, end);

        try {
            await uploadChunk(i + 1, chunk);

            // 진행률 업데이트
            const progress = ((i + 1) / totalChunks) * 100;
            updateProgress(progress, `청크 ${i + 1}/${totalChunks} 업로드 중...`);

        } catch (error) {
            console.error(`Chunk ${i + 1} upload failed:`, error);
            alert(`청크 ${i + 1} 업로드에 실패했습니다.`);
            return;
        }
    }

    // 업로드 완료
    updateProgress(100, '업로드 완료!');
    alert('파일 업로드가 완료되었습니다.');
}

// 개별 청크 업로드
async function uploadChunk(chunkNumber, chunk) {
    const formData = new FormData();
    formData.append('action', 'upload');
    formData.append('uploadId', currentUploadId);
    formData.append('chunkNumber', chunkNumber);

    const response = await fetch('front?command=chunkUpload', {
        method: 'POST',
        body: formData
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error('Upload failed');
    }
}

// 진행률 업데이트
function updateProgress(progress, status) {
    document.getElementById('chunkProgressBar').style.width = progress + '%';
    document.getElementById('chunkProgressText').textContent = Math.round(progress) + '%';
    document.getElementById('chunkStatusText').textContent = status;
}
</script>
```

## 📝 완료 체크리스트

- [ ] ChunkUploadHandler 서비스 클래스 생성 (IO 스트림 실습)
- [ ] ChunkUploadCommand 생성 (ServletInputStream 실습)
- [ ] write.jsp에 청크 업로드 UI 추가
- [ ] 청크 업로드 및 진행률 표시 테스트 완료

## ⚠️ 주의사항

### 1. IO 스트림 사용 시 주의점

- **리소스 해제**: `try-with-resources` 사용으로 자동 해제
- **버퍼 크기**: 적절한 버퍼 크기로 성능 최적화
- **예외 처리**: IOException 적절히 처리

### 2. 성능 고려사항

- **청크 크기**: 2MB가 일반적으로 최적 (1MB~5MB 범위)
- **버퍼 크기**: 8KB 버퍼로 읽기/쓰기 성능 향상
- **메모리 관리**: 청크 단위로 처리하여 메모리 사용량 최소화

### 3. 보안 고려사항

- **파일 검증**: 업로드된 청크의 무결성 검증
- **용량 제한**: 전체 파일 크기 및 청크 크기 제한
- **접근 제어**: 업로드 권한 및 세션 관리

## 🚀 유용한 라이브러리 소개 (참고용)

### 1. JavaScript 기반 라이브러리

#### Resumable.js

```html
<script src="https://cdn.jsdelivr.net/npm/resumablejs@1.1.0/resumable.min.js"></script>
```

- **장점**: 청크 업로드 전용, 가벼움, 설정 간단
- **단점**: 기본 UI가 없어 커스터마이징 필요

#### Uppy

```html
<script src="https://releases.transloadit.com/uppy/v2.15.0/uppy.min.js"></script>
```

- **장점**: 현대적이고 기능이 풍부, 드래그앤드롭, 이미지 편집
- **단점**: 상대적으로 큰 파일 크기

### 2. Java 기반 라이브러리

#### Apache Commons FileUpload

```xml
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.5</version>
</dependency>
```

- **장점**: 안정적이고 검증된 라이브러리, 대용량 파일 처리 지원
- **단점**: 청크 업로드는 직접 구현 필요

### 3. 실무 권장사항

#### 학습 단계

- **권장**: 직접 구현 (위의 IO 스트림 실습)
- **이유**: Java 기본기 습득, IO 스트림 이해

#### 실무 적용

- **권장**: 검증된 라이브러리 사용
- **이유**: 안정성, 유지보수성, 추가 기능

## 🎯 테스트 방법

### 1. 기본 청크 업로드 테스트

- 50MB 이상 파일 업로드 시 청크 업로드 동작 확인
- 청크별 업로드 진행률 표시 확인
- 업로드 완료 후 파일 재조립 확인

### 2. IO 스트림 동작 테스트

- 메모리 사용량 모니터링으로 스트리밍 처리 확인
- 청크 크기별 업로드 속도 비교
- 버퍼 크기별 성능 차이 확인

### 3. 에러 처리 테스트

- 네트워크 오류 시 재시도 동작 확인
- 잘못된 청크 데이터 처리 확인
- 업로드 세션 만료 시 처리 확인

## 🔗 다음 단계

대용량 파일 업로드 처리 시스템 구현 완료 후:

1. **step04**: 간단한 검색 기능 구현 (제목, 내용, 작성자 검색)
2. **step05**: 리치 텍스트 에디터 통합 (CKEditor, TinyMCE 등)

## 💡 핵심 포인트

**먼저 기본기를 다지자!**

- **IO 스트림**: InputStream, OutputStream, BufferedInputStream 등 Java 기본기
- **청크 업로드**: 실제 활용 사례를 통한 개념 이해
- **라이브러리**: 기본기 습득 후 검증된 도구 사용

이제 Java IO 스트림의 기본기를 다지면서도 실용적인 대용량 파일 업로드 시스템을 구현할 수 있습니다!
