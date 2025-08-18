# Step05: 리치 텍스트 에디터 통합

## 🎯 목표

게시글 작성 시 단순한 텍스트 입력 대신 리치 텍스트 에디터를 사용하여 텍스트 서식, 이미지 삽입, 링크 등의 고급 편집 기능을 제공합니다.

## ⚠️ 중요: 리치 텍스트 에디터의 필요성

### 기존 시스템의 문제점

- **텍스트 서식 부족**: 굵게, 기울임, 색상 등 텍스트 스타일링 불가
- **이미지 삽입 어려움**: 이미지를 게시글에 직접 삽입할 수 없음
- **링크 관리 복잡**: URL을 텍스트로만 표시하여 가독성 저하
- **사용자 경험**: 전문적인 문서 작성 도구 부재

### 리치 텍스트 에디터의 장점

- **직관적인 편집**: 마이크로소프트 워드와 유사한 편집 경험
- **다양한 서식**: 텍스트 스타일링, 목록, 표 등 풍부한 편집 기능
- **미디어 지원**: 이미지, 동영상, 링크 등의 멀티미디어 콘텐츠 지원
- **사용자 만족도**: 전문적인 문서 작성 도구로 만족도 향상

## 📚 이론 포인트 리마인드

### 1. 리치 텍스트 에디터의 종류

- **CKEditor**: 가장 인기 있는 오픈소스 에디터, 기능이 풍부하고 커스터마이징 자유도 높음
- **TinyMCE**: 가벼우면서도 강력한 기능을 제공하는 에디터
- **Quill**: 모던한 인터페이스와 모듈화된 구조의 에디터
- **Summernote**: 부트스트랩 기반의 심플한 에디터

### 2. 에디터 통합 방식

- **CDN 방식**: 외부 CDN에서 JavaScript 파일을 로드하여 사용
- **로컬 설치**: 에디터 파일을 프로젝트에 직접 포함하여 사용
- **API 방식**: 에디터의 API를 통해 동적으로 에디터 생성 및 제어

### 3. 보안 고려사항

- **XSS 방지**: HTML 태그 필터링 및 허용된 태그만 사용
- **파일 업로드**: 이미지 업로드 시 파일 타입 및 크기 검증
- **콘텐츠 검증**: 서버에서 HTML 콘텐츠의 유효성 검증

## 🚀 실습 단계별 진행

### 1단계: CKEditor CDN 방식으로 통합

#### write.jsp 수정 (기존 textarea를 CKEditor로 교체)

```jsp
<!-- 기존 textarea 부분을 다음과 같이 수정 -->

<!-- CKEditor CDN 추가 (head 섹션 또는 페이지 상단) -->
<script src="https://cdn.ckeditor.com/ckeditor5/40.1.0/classic/ckeditor.js"></script>

<!-- 게시글 내용 입력 섹션 수정 -->
<div class="mb-6">
    <label class="block text-sm font-medium text-gray-700 mb-2">
        게시글 내용 <span class="text-red-500">*</span>
    </label>

    <!-- CKEditor가 렌더링될 div -->
    <div id="editor" class="min-h-64 border border-gray-300 rounded-md focus-within:ring-2 focus-within:ring-blue-500">
        <!-- 에디터가 여기에 렌더링됩니다 -->
    </div>

    <!-- 기존 textarea (숨김 처리) -->
    <textarea name="content" id="content" class="hidden" required>${board.content}</textarea>

    <!-- 에디터 도구 설명 -->
    <p class="mt-2 text-sm text-gray-500">
        <svg class="w-4 h-4 inline mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        텍스트 서식, 이미지 삽입, 링크 등의 기능을 사용할 수 있습니다.
    </p>
</div>

<!-- CKEditor 초기화 및 설정 -->
<script>
// CKEditor 초기화
ClassicEditor
    .create(document.querySelector('#editor'), {
        // 에디터 설정
        toolbar: {
            items: [
                'heading',
                '|',
                'bold',
                'italic',
                'underline',
                'strikethrough',
                '|',
                'fontSize',
                'fontColor',
                'fontBackgroundColor',
                '|',
                'alignment',
                '|',
                'numberedList',
                'bulletedList',
                '|',
                'indent',
                'outdent',
                '|',
                'link',
                'blockQuote',
                'insertTable',
                '|',
                'undo',
                'redo'
            ]
        },
        // 언어 설정
        language: 'ko',
        // 이미지 업로드 설정
        simpleUpload: {
            uploadUrl: 'front?command=imageUpload',
            headers: {
                'X-CSRF-TOKEN': '${sessionScope.csrfToken}'
            }
        },
        // 테이블 설정
        table: {
            contentToolbar: [
                'tableColumn',
                'tableRow',
                'mergeTableCells'
            ]
        }
    })
    .then(editor => {
        // 에디터 인스턴스 저장
        window.editor = editor;

        // 에디터 내용이 변경될 때마다 hidden textarea에 값 설정
        editor.model.document.on('change:data', () => {
            const data = editor.getData();
            document.getElementById('content').value = data;
        });

        // 기존 내용이 있으면 에디터에 설정
        const existingContent = document.getElementById('content').value;
        if (existingContent) {
            editor.setData(existingContent);
        }

        console.log('CKEditor 초기화 완료');
    })
    .catch(error => {
        console.error('CKEditor 초기화 실패:', error);
        // 에디터 로드 실패 시 기존 textarea 표시
        document.getElementById('content').classList.remove('hidden');
        document.getElementById('editor').classList.add('hidden');
    });

// 폼 제출 시 에디터 내용을 textarea에 복사
document.querySelector('form').addEventListener('submit', function(e) {
    if (window.editor) {
        const content = window.editor.getData();
        document.getElementById('content').value = content;

        // 내용 검증
        if (!content || content.trim() === '') {
            e.preventDefault();
            alert('게시글 내용을 입력해주세요.');
            return false;
        }
    }
});
</script>
```

### 2단계: 이미지 업로드 처리 Command 생성

#### ImageUploadCommand.java 생성

```java
package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

/**
 * CKEditor 이미지 업로드 처리를 위한 Command
 */
public class ImageUploadCommand implements Command {

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            // multipart 요청 처리
            if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
                return handleImageUpload(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request type");
                return null;
            }
        } catch (Exception e) {
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
     * 이미지 업로드 처리
     */
    private String handleImageUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // multipart 파싱
        String boundary = extractBoundary(request.getContentType());
        if (boundary == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid boundary");
            return null;
        }

        // 업로드 디렉토리 생성
        String uploadDir = "uploads/images";
        Files.createDirectories(Paths.get(uploadDir));

        // 파일명 생성
        String fileName = UUID.randomUUID().toString() + ".jpg";
        String filePath = uploadDir + "/" + fileName;

        // 파일 데이터 읽기 및 저장
        try (InputStream input = request.getInputStream();
             OutputStream output = Files.newOutputStream(Paths.get(filePath))) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        // CKEditor 응답 형식으로 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String imageUrl = request.getContextPath() + "/" + filePath;
        String jsonResponse = String.format(
            "{\"url\":\"%s\",\"uploaded\":true,\"fileName\":\"%s\"}",
            imageUrl, fileName
        );

        response.getWriter().write(jsonResponse);
        return null;
    }

    /**
     * Content-Type에서 boundary 추출
     */
    private String extractBoundary(String contentType) {
        if (contentType == null) return null;

        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length());
            }
        }
        return null;
    }
}
```

### 3단계: HandlerMapping에 ImageUploadCommand 등록

#### HandlerMapping.java 수정

```java
// 기존 HandlerMapping 클래스에 다음 내용 추가

public HandlerMapping() {
    commandMap = new HashMap<>();

    // 기존 command들...
    commandMap.put("boardList", new BoardListCommand());
    commandMap.put("boardView", new BoardViewCommand());
    commandMap.put("boardWrite", new BoardWriteCommand());
    commandMap.put("boardInsert", new BoardInsertCommand());
    commandMap.put("boardUpdate", new BoardUpdateCommand());
    commandMap.put("boardDelete", new BoardDeleteCommand());

    // 새로운 command 추가
    commandMap.put("imageUpload", new ImageUploadCommand());

    // develop01, develop02의 command들...
    commandMap.put("signup", new SignupCommand());
    commandMap.put("login", new LoginCommand());
    commandMap.put("logout", new LogoutCommand());
    commandMap.put("fileUpload", new FileUploadCommand());
    commandMap.put("chunkUpload", new ChunkUploadCommand());
}
```

### 4단계: view.jsp에서 리치 텍스트 콘텐츠 표시

#### view.jsp 수정 (기존 내용 표시 부분 수정)

```jsp
<!-- 기존 게시글 내용 표시 부분을 다음과 같이 수정 -->

<div class="mb-6">
    <label class="block text-sm font-medium text-gray-700 mb-2">게시글 내용</label>
    <div class="bg-white border border-gray-300 rounded-md p-4 min-h-32">
        <!-- 리치 텍스트 콘텐츠를 안전하게 표시 -->
        <div class="prose max-w-none">
            ${board.content}
        </div>
    </div>
</div>

<!-- XSS 방지를 위한 스크립트 추가 -->
<script>
// HTML 콘텐츠를 안전하게 표시하는 함수
function sanitizeHtml(html) {
    const div = document.createElement('div');
    div.innerHTML = html;

    // 허용된 태그만 유지
    const allowedTags = ['p', 'br', 'strong', 'b', 'em', 'i', 'u', 's', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
                         'ul', 'ol', 'li', 'blockquote', 'a', 'img', 'table', 'thead', 'tbody', 'tr', 'th', 'td'];

    // 허용되지 않은 태그 제거
    const elements = div.querySelectorAll('*');
    elements.forEach(element => {
        if (!allowedTags.includes(element.tagName.toLowerCase())) {
            element.replaceWith(element.innerHTML);
        }
    });

    return div.innerHTML;
}

// 페이지 로드 시 콘텐츠 정리
document.addEventListener('DOMContentLoaded', function() {
    const contentDiv = document.querySelector('.prose');
    if (contentDiv) {
        const originalContent = contentDiv.innerHTML;
        contentDiv.innerHTML = sanitizeHtml(originalContent);
    }
});
</script>
```

### 5단계: TinyMCE 대안 에디터 추가 (선택사항)

#### write.jsp에 TinyMCE 옵션 추가

```jsp
<!-- CKEditor 대신 TinyMCE를 사용하고 싶은 경우 -->

<!-- TinyMCE CDN -->
<script src="https://cdn.tiny.cloud/1/no-api-key/tinymce/6/tinymce.min.js" referrerpolicy="origin"></script>

<!-- 에디터 초기화 스크립트 수정 -->
<script>
// TinyMCE 초기화 (CKEditor 대신 사용 시)
tinymce.init({
    selector: '#editor',
    language: 'ko_KR',
    height: 400,
    plugins: [
        'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
        'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
        'insertdatetime', 'media', 'table', 'help', 'wordcount'
    ],
    toolbar: 'undo redo | formatselect | bold italic underline strikethrough | ' +
             'alignleft aligncenter alignright alignjustify | ' +
             'bullist numlist outdent indent | link image | removeformat',
    menubar: 'file edit view insert format tools table help',
    content_style: 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; font-size: 14px; }',

    // 이미지 업로드 설정
    images_upload_url: 'front?command=imageUpload',
    images_upload_handler: function (blobInfo, success, failure) {
        const formData = new FormData();
        formData.append('file', blobInfo.blob(), blobInfo.filename());

        fetch('front?command=imageUpload', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(result => {
            if (result.uploaded) {
                success(result.url);
            } else {
                failure('Upload failed');
            }
        })
        .catch(error => {
            failure('Upload error: ' + error.message);
        });
    }
});
</script>
```

## 📝 완료 체크리스트

- [ ] write.jsp에 CKEditor 통합 (textarea를 에디터로 교체)
- [ ] ImageUploadCommand 생성 및 이미지 업로드 처리
- [ ] HandlerMapping에 imageUpload command 등록
- [ ] view.jsp에서 리치 텍스트 콘텐츠 안전하게 표시
- [ ] 에디터 기능 테스트 (서식, 이미지 삽입, 링크 등)

## ⚠️ 주의사항

### 1. 보안 고려사항

- **XSS 방지**: HTML 콘텐츠의 안전한 표시 및 필터링
- **파일 업로드 보안**: 이미지 파일 타입 및 크기 검증
- **CSRF 보호**: 이미지 업로드 시 CSRF 토큰 검증

### 2. 성능 고려사항

- **에디터 로딩**: CDN 방식 사용 시 네트워크 상태에 따른 로딩 시간
- **이미지 최적화**: 업로드된 이미지의 크기 및 품질 최적화
- **캐싱**: 에디터 리소스의 적절한 캐싱 설정

### 3. 사용자 경험

- **에디터 로딩 실패**: 에디터 로드 실패 시 기존 textarea로 폴백
- **반응형 디자인**: 모바일 환경에서의 에디터 사용성 고려
- **접근성**: 키보드 네비게이션 및 스크린 리더 지원

## 🎯 테스트 방법

### 1. 기본 에디터 기능 테스트

- 텍스트 서식 (굵게, 기울임, 밑줄, 취소선)
- 제목 스타일 (H1, H2, H3 등)
- 목록 (번호 목록, 글머리 기호 목록)
- 링크 삽입 및 편집

### 2. 고급 기능 테스트

- 이미지 삽입 및 업로드
- 테이블 생성 및 편집
- 인용구 삽입
- 텍스트 색상 및 배경색 변경

### 3. 보안 테스트

- XSS 스크립트 삽입 시도
- 허용되지 않은 HTML 태그 입력
- 이미지 파일 타입 검증
- 파일 크기 제한 확인

## 🔗 다음 단계

리치 텍스트 에디터 통합 완료로 `develop03`의 모든 기능이 완성되었습니다!

이제 다음 단계로 진행할 수 있습니다:

1. **스프링부트 전환**: Spring Boot + Spring Security로 마이그레이션
2. **클라우드 배포**: AWS, Azure 등 클라우드 환경 배포
3. **모니터링**: 성능 모니터링 및 로깅 시스템 구축

이제 전문적인 문서 작성이 가능한 리치 텍스트 에디터로 사용자 경험을 크게 향상시킬 수 있습니다!
