# 03-model2: Model 2 아키텍처

## 📋 프로젝트 개요

**03-model2**는 Model 2 (MVC) 아키텍처를 구현한 Java 웹 애플리케이션입니다.
Front Controller 패턴, JdbcTemplate, 사용자 인증, 파일 업로드 등 현대적인 웹 개발 기법을 단계별로 구현합니다.

## 🏗️ 아키텍처 구조

### Front Controller + Command 패턴

```
사용자 요청 → FrontController → HandlerMapping → Command → JSP
```

### 주요 컴포넌트

- **FrontController**: 모든 요청을 받아서 적절한 Command로 분배
- **Command**: 비즈니스 로직 처리
- **DAO**: 데이터베이스 접근
- **JSP**: 화면 표시

## 🚀 구현된 기능

### 1. 기본 게시판 시스템

- 게시글 목록/상세보기/작성/수정/삭제
- JdbcTemplate을 사용한 데이터 접근
- Tailwind CSS 기반 모던 UI

### 2. 사용자 인증 및 권한 제어 (develop01)

- 회원가입/로그인/로그아웃
- 세션 기반 사용자 상태 관리
- 게시글 작성/수정/삭제 권한 제어
- 작성자만 수정/삭제 가능

### 3. 파일 업로드 시스템 (develop02)

- 다중 파일 업로드 (최대 5개)
- 이미지와 일반 파일 분리 저장
- 파일 타입 및 크기 검증
- 보안을 고려한 파일 관리

## 📁 프로젝트 구조

```
03-model2/
├── src/main/java/io/goorm/backend/
│   ├── Board.java              # 게시글 모델
│   ├── User.java               # 사용자 모델
│   ├── FileUpload.java         # 파일 업로드 모델
│   ├── BoardDAO.java           # 게시글 데이터 접근
│   ├── UserDAO.java            # 사용자 데이터 접근
│   ├── FileUploadDAO.java      # 파일 데이터 접근
│   ├── controller/
│   │   └── FrontController.java # Front Controller
│   ├── command/                # Command 클래스들
│   │   ├── BoardListCommand.java
│   │   ├── BoardViewCommand.java
│   │   ├── BoardWriteCommand.java
│   │   ├── BoardInsertCommand.java
│   │   ├── BoardUpdateCommand.java
│   │   ├── BoardDeleteCommand.java
│   │   ├── SignupCommand.java
│   │   ├── LoginCommand.java
│   │   ├── LogoutCommand.java
│   │   └── FileDownloadCommand.java
│   ├── handler/
│   │   └── HandlerMapping.java # Command 매핑
│   ├── config/
│   │   ├── DatabaseConfig.java # 데이터베이스 설정
│   │   └── UploadConfig.java   # 파일 업로드 설정
│   └── filter/
│       └── XSSFilter.java      # XSS 방지 필터
├── webapp/
│   ├── board/                  # 게시판 JSP
│   │   ├── list.jsp           # 게시글 목록
│   │   ├── view.jsp           # 게시글 상세보기
│   │   ├── write.jsp          # 게시글 작성
│   │   └── update.jsp         # 게시글 수정
│   ├── signup.jsp             # 회원가입
│   ├── login.jsp              # 로그인
│   └── index.jsp              # 메인 페이지
├── develop01/                  # 사용자 인증 가이드
├── develop02/                  # 파일 업로드 가이드
└── build.gradle               # Gradle 빌드 설정
```

## 🔧 기술 스택

- **Java**: Servlet, JSP, JSTL
- **Spring**: JdbcTemplate
- **데이터베이스**: H2 Database
- **UI**: Tailwind CSS
- **빌드**: Gradle
- **보안**: XSS 방지 필터

## 🚀 실행 방법

### 1. 프로젝트 설정

```bash
# Gradle 프로젝트로 import
# IntelliJ IDEA에서 build.gradle 파일 열기
```

### 2. 데이터베이스 설정

```sql
-- H2 데이터베이스 실행
-- users, board, file_upload 테이블 생성
```

### 3. Tomcat 실행

```bash
# IntelliJ IDEA에서 Tomcat 서버 설정
# 애플리케이션 실행
```

### 4. 접근

- **메인**: `http://localhost:8080/`
- **게시판**: `http://localhost:8080/front?command=boardList`
- **회원가입**: `http://localhost:8080/front?command=signup`
- **로그인**: `http://localhost:8080/front?command=login`

## 📚 개발 가이드

### develop01: 사용자 인증

- step01.md: 회원가입 구현
- step02.md: 로그인/로그아웃 구현
- step03.md: 권한 제어 구현

### develop02: 파일 업로드

- step01.md: 기본 구조 설정
- step02.md: 모델 및 DAO 구현
- step03.md: 게시글 작성 시 파일 업로드
- step04.md: 첨부파일 표시 및 다운로드

## 🎯 주요 특징

- **Front Controller 패턴**: 모든 요청을 중앙에서 처리
- **Command 패턴**: 비즈니스 로직을 명령 객체로 캡슐화
- **JdbcTemplate**: Spring의 데이터 접근 추상화
- **사용자 인증**: 세션 기반 로그인 및 권한 관리
- **파일 업로드**: 보안을 고려한 다중 파일 처리
- **모던 UI**: Tailwind CSS를 사용한 반응형 디자인

## 🔮 다음 단계

현재 구현된 기능을 기반으로:

- 페이지네이션 시스템
- 검색 기능
- 이미지 썸네일 생성
- Spring Boot 전환

---

**03-model2**는 전통적인 Java 웹 개발부터 현대적인 웹 개발 기법까지 단계별로 학습할 수 있는 프로젝트입니다.
