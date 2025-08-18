# Develop02: 파일 업로드 시스템

## 🎯 목표

기존 게시판 시스템에 파일 업로드 기능을 추가하여 사용자가 게시글과 함께 파일을 첨부할 수 있도록 합니다.

## 📋 구현할 기능

### 1. 파일 업로드

- 게시글 작성 시 파일 첨부 기능
- 다중 파일 업로드 지원 (최대 5개)
- 파일 크기 제한 (개별 파일 10MB, 전체 50MB)
- 허용 파일 형식 제한 (이미지, 문서, 압축파일 등)

### 2. 파일 관리

- 업로드된 파일 목록 표시
- 파일 다운로드 기능
- 파일 삭제 기능 (게시글 삭제 시 연동)
- 파일 정보 표시 (파일명, 크기, 업로드일)

### 3. 보안 및 검증

- 파일 타입 검증 (MIME 타입, 확장자)
- 파일 크기 검증
- 악성 파일 방지
- 업로드 경로 보안

## 📁 추가될 파일들

### Java 클래스

- `FileUpload.java` - 파일 정보 모델
- `FileUploadDAO.java` - 파일 데이터 접근
- `FileUploadCommand.java` - 파일 업로드 처리
- `FileDownloadCommand.java` - 파일 다운로드 처리
- `FileDeleteCommand.java` - 파일 삭제 처리

### JSP 파일

- `list.jsp` - 게시글 목록 (파일 첨부 표시)
- `view.jsp` - 게시글 상세보기 (파일 다운로드)
- `write.jsp` - 게시글 작성 (파일 업로드 폼)

### 데이터베이스

- `file_upload` 테이블 생성 (id, board_id, original_name, stored_name, file_size, file_type, upload_date)

## 🔐 보안 고려사항

- 파일 업로드 경로 보안 (외부 접근 차단)
- 허용된 파일 형식만 업로드 가능
- 파일 크기 제한으로 서버 리소스 보호
- 업로드된 파일의 실행 권한 제거
- **XSS 공격 방지** (Servlet Filter 적용)
- 입력값 검증 및 필터링

## 🚀 구현 순서

1. **데이터베이스 설계**: file_upload 테이블 생성
2. **파일 모델**: FileUpload 클래스 및 FileUploadDAO 구현
3. **업로드 시스템**: 파일 업로드 Command 구현
4. **다운로드 시스템**: 파일 다운로드 Command 구현
5. **UI 수정**: 기존 JSP에 파일 업로드/다운로드 기능 추가
6. **XSS 방지**: XSSFilter 구현 및 web.xml 설정
7. **테스트**: 전체 파일 업로드 시스템 동작 확인

## 📚 관련 문서

- **상세 구현 가이드**: [step01.md](step01.md) - 파일 업로드 기본 구조
- **상세 구현 가이드**: [step02.md](step02.md) - 파일 업로드 처리
- **상세 구현 가이드**: [step03.md](step03.md) - 파일 다운로드 및 관리
- **보안 강화**: [step04.md](step04.md) - XSS 방지 및 보안 강화

## 🎨 JSP 소스 (HTML/Tailwind CSS 버전)

각 단계에서 구현하는 JSP 파일의 **HTML/Tailwind CSS 버전**이 `jsp/` 폴더에 제공됩니다.

### 제공되는 파일들

- **게시글 목록**: [jsp/list.jsp](jsp/list.jsp) - 파일 첨부 표시 포함
- **게시글 상세보기**: [jsp/view.jsp](jsp/view.jsp) - 파일 다운로드 포함
- **게시글 작성**: [jsp/write.jsp](jsp/write.jsp) - 파일 업로드 폼 포함

### 특징

- **Tailwind CSS**: 모던하고 반응형 디자인
- **JSTL/EL**: 서버 사이드 로직과 데이터 바인딩
- **파일 업로드 UI**: 드래그 앤 드롭 지원
- **사용자 경험**: 직관적인 파일 관리 인터페이스

**참고**: 이 JSP 파일들은 `develop01`의 사용자 인증 및 권한 제어 기능과 연동되어 작동합니다.
