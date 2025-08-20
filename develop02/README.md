# Develop02: 파일 업로드 시스템

## 🎯 목표

기존 게시판 시스템에 파일 업로드 기능을 추가하여 사용자가 게시글과 함께 파일을 첨부할 수 있도록 합니다.

## 📋 구현할 기능

### 1. 파일 업로드

- 게시글 작성 시 파일 첨부 기능
- 파일과 이미지를 분리하여 저장 (uploads/files, uploads/images)
- 파일 크기 제한 (일반 파일 10MB, 이미지 5MB)
- 화이트리스트 방식의 파일 확장자 제한
- 설정 파일을 통한 업로드 경로 및 제한사항 관리

### 2. 파일 관리

- 업로드된 파일 목록 표시
- 파일 다운로드 기능
- 파일 삭제 기능 (게시글 삭제 시 연동)
- 파일 정보 표시 (파일명, 크기, 업로드일)

### 3. 보안 및 검증

- 파일 타입 검증 (MIME 타입, 확장자)
- 파일 크기 검증
- 화이트리스트 방식의 파일 확장자 제한
- 업로드 경로 보안 (디렉토리 트래버설 공격 방지)
- 이미지 폴더는 웹에서 직접 접근 가능, 일반 파일은 보안 접근

## 📁 추가될 파일들

### Java 클래스

- `FileUpload.java` - 파일 정보 모델
- `FileUploadDAO.java` - 파일 데이터 접근
- `FileUploadServlet.java` - 파일 업로드 처리
- `FileDownloadCommand.java` - 파일 다운로드 처리
- `FileDeleteCommand.java` - 파일 삭제 처리
- `UploadConfig.java` - 업로드 설정 관리
- `Board.java` - 첨부파일 목록 필드 추가
- `BoardDAO.java` - 파일 첨부 정보 조회 기능 추가

### JSP 파일

- `list.jsp` - 게시글 목록 (파일 첨부 표시)
- `view.jsp` - 게시글 상세보기 (파일 다운로드)
- `write.jsp` - 게시글 작성 (파일 업로드 폼)

### 데이터베이스

- `file_upload` 테이블 생성 (id, board_id, original_filename, stored_filename, file_path, file_size, content_type, upload_date, file_type, web_url)
- 외래키 제약조건 (board_id → board.id)
- 인덱스 생성 (board_id, file_type, stored_filename)

## 🔐 보안 고려사항

- 파일 업로드 경로 보안 (외부 접근 차단)
- 허용된 파일 형식만 업로드 가능
- 파일 크기 제한으로 서버 리소스 보호
- 업로드된 파일의 실행 권한 제거
- **XSS 공격 방지** (Servlet Filter 적용)
- 입력값 검증 및 필터링

## 🚀 구현 순서

1. **업로드 설정**: upload.properties 및 UploadConfig 클래스 생성
2. **데이터베이스 설계**: file_upload 테이블 생성
3. **파일 모델**: FileUpload 클래스 및 FileUploadDAO 구현
4. **게시글 모델 확장**: Board 클래스에 첨부파일 목록 필드 추가
5. **업로드 시스템**: FileUploadServlet 구현
6. **다운로드 시스템**: FileDownloadCommand 구현
7. **삭제 시스템**: FileDeleteCommand 구현
8. **XSS 방지**: XSSFilter 구현 및 web.xml 설정
9. **테스트**: 전체 파일 업로드 시스템 동작 확인

## 📚 구현 가이드

각 단계별 상세 구현 가이드가 제공됩니다:

- **Step 01**: 실무 수준 파일 업로드 시스템 구현
- **Step 02**: 파일 다운로드 기능 구현
- **Step 03**: 파일 삭제 기능 구현
- **Step 04**: XSS 방지 및 보안 강화

## 🎨 JSP 활용 방법

### 완성된 JSP 파일 제공

`jsp/` 폴더에 파일 업로드 기능이 완벽하게 구현된 JSP 파일들이 제공됩니다.

### 제공되는 파일들

- **게시글 목록**: [jsp/list.jsp](jsp/list.jsp) - 파일 첨부 개수 표시
- **게시글 상세보기**: [jsp/view.jsp](jsp/view.jsp) - 파일 다운로드 및 삭제
- **게시글 작성**: [jsp/write.jsp](jsp/write.jsp) - 파일 업로드 폼

### 특징

- **Tailwind CSS**: 모던하고 반응형 디자인
- **JSTL/EL**: 서버 사이드 로직과 데이터 바인딩
- **파일 관리**: 업로드, 다운로드, 삭제 기능 완성
- **사용자 경험**: 직관적인 파일 관리 인터페이스

### 사용 방법

1. **백엔드 구현**: Step 01~04 가이드에 따라 Java 클래스 구현
2. **JSP 적용**: `jsp/` 폴더의 완성된 JSP 파일들을 그대로 사용
3. **수정 불필요**: JSP 파일들은 완성본이므로 수정하지 않음
