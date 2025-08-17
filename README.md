# 03-model2: Model 2 아키텍처

## 📋 프로젝트 개요

**03-model2**는 2000년대 초반 **Model 2 아키텍처**를 구현한 Java 웹 애플리케이션입니다.
이전 프로젝트들과 달리 **MVC (Model-View-Controller)** 패턴을 적용하여 코드의 역할을 명확히 분리했습니다.

## 🏗️ 아키텍처 구조

### Model 2 (MVC) 패턴

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     View        │    │   Controller    │    │      Model      │
│     (JSP)       │◄──►│   (Servlet)     │◄──►│     (DAO)       │
│                 │    │                 │    │                 │
│ - 화면 출력     │    │ - 요청 처리     │    │ - 비즈니스 로직 │
│ - JSTL 사용     │    │ - DAO 호출      │    │ - 데이터 액세스 │
│ - Java 코드 최소│    │ - JSP 포워딩    │    │ - DB 풀 사용    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 컴포넌트 역할

- **Servlet (Controller)**: 사용자 요청 처리, DAO 호출, JSP로 포워딩
- **JSP (View)**: Servlet에서 전달받은 데이터를 화면에 출력
- **DAO (Model)**: 비즈니스 로직과 데이터베이스 접근 처리

## 📁 폴더 구조

```
03-model2/
├── src/
│   └── io/goorm/backend/
│       ├── Board.java              # Board VO 클래스
│       ├── BoardDAO.java           # Board DAO 클래스 (데이터베이스 풀 사용)
│       └── controller/             # Servlet 컨트롤러들
│           ├── BoardListServlet.java    # 게시글 목록 처리
│           ├── BoardWriteServlet.java   # 글쓰기 폼 표시
│           ├── BoardInsertServlet.java  # 게시글 등록 처리
│           └── BoardViewServlet.java    # 게시글 상세보기 처리
├── webapp/
│   ├── WEB-INF/
│   │   ├── web.xml                 # 웹 애플리케이션 설정
│   │   ├── classes/                # 컴파일된 Java 클래스들
│   │   └── lib/                    # 필요한 JAR 라이브러리들
│   ├── board/                      # 게시판 관련 JSP 페이지들
│   │   ├── list.jsp               # 게시글 목록
│   │   ├── write.jsp              # 글쓰기 폼
│   │   └── view.jsp               # 게시글 상세보기
│   └── index.jsp                  # 메인 페이지
├── build.md                        # 빌드 및 배포 가이드
└── README.md                       # 프로젝트 설명서
```

## 🚀 주요 기능

### 1. 게시글 목록 조회

- **URL**: `/board/list`
- **처리**: `BoardListServlet` → `BoardDAO.getBoardList()` → `list.jsp`
- **특징**: JSTL을 사용한 반복문 처리, 에러 메시지 표시

### 2. 게시글 작성

- **URL**: `/board/write` (폼), `/board/insert` (처리)
- **처리**: `BoardWriteServlet` → `write.jsp` (폼 표시)
- **처리**: `BoardInsertServlet` → `BoardDAO.insertBoard()` → 목록으로 리다이렉트
- **특징**: 입력값 검증, 에러 시 폼 유지

### 3. 게시글 상세보기

- **URL**: `/board/view?id={id}`
- **처리**: `BoardViewServlet` → `BoardDAO.getBoardById()` → `view.jsp`
- **특징**: ID 파라미터 검증, 존재하지 않는 게시글 처리

## 🔧 기술 스택

### 핵심 기술

- **Java Servlet 2.4+**: `@WebServlet` 어노테이션 지원
- **JSP 2.0**: EL(Expression Language) 지원
- **JSTL 1.2**: 표준 태그 라이브러리 (`<c:forEach>`, `<c:if>` 등)
- **H2 Database**: 인메모리/디스크 모드 지원

### 데이터베이스 풀

- **JNDI**: Java Naming and Directory Interface
- **DataSource**: 데이터베이스 커넥션 풀링
- **폴백 메커니즘**: JNDI 실패 시 직접 연결

### UI/UX

- **Tailwind CSS**: 모던하고 반응형 디자인
- **JSTL**: JSP에서 Java 코드 최소화
- **에러 처리**: 사용자 친화적인 에러 메시지

## 🆚 이전 프로젝트와의 차이점

### 01-servlet vs 03-model2

| 구분             | 01-servlet               | 03-model2            |
| ---------------- | ------------------------ | -------------------- |
| **아키텍처**     | Servlet 중심             | MVC 패턴             |
| **코드 분리**    | Servlet에 모든 로직 집중 | 역할별 컴포넌트 분리 |
| **데이터베이스** | 직접 연결                | 커넥션 풀 사용       |
| **화면 출력**    | Servlet에서 직접 출력    | JSP로 포워딩         |

### 02-model1 vs 03-model2

| 구분              | 02-model1          | 03-model2                      |
| ----------------- | ------------------ | ------------------------------ |
| **아키텍처**      | JSP 중심 (Model 1) | Servlet 중심 (Model 2)         |
| **요청 처리**     | JSP에서 직접 처리  | Servlet에서 처리 후 JSP로 전달 |
| **비즈니스 로직** | JSP 또는 DAO       | DAO에 집중                     |
| **코드 재사용**   | 제한적             | 높음                           |

## 🎯 Model 2의 장점

### 1. 코드 분리

- **역할 명확화**: 각 컴포넌트의 책임이 명확
- **독립적 개발**: 팀원별로 다른 컴포넌트 담당 가능
- **테스트 용이성**: 각 컴포넌트별 독립적 테스트

### 2. 유지보수성

- **수정 영향 최소화**: 한 컴포넌트 수정 시 다른 부분 영향 최소
- **코드 재사용**: DAO를 여러 Servlet에서 공유
- **확장성**: 새로운 기능 추가 시 기존 코드 영향 최소

### 3. 보안성

- **입력값 검증**: Servlet에서 중앙 집중식 검증
- **SQL 인젝션 방지**: PreparedStatement 사용
- **권한 관리**: Servlet에서 접근 제어 가능

## 🚀 실행 방법

### 1. 빌드

```bash
# Java 소스 컴파일
cd src
javac -cp ".;../lib/servlet-api.jar" io/goorm/backend/*.java
javac -cp ".;../lib/servlet-api.jar" io/goorm/backend/controller/*.java

# 클래스 파일 복사 및 WAR 생성
# (자세한 내용은 build.md 참조)
```

### 2. 배포

```bash
# Tomcat webapps에 WAR 파일 복사
copy 03-model2.war %TOMCAT_HOME%\webapps\ROOT.war
```

### 3. 접근

- **메인 페이지**: `http://localhost:8080/`
- **게시글 목록**: `http://localhost:8080/board/list`
- **글쓰기**: `http://localhost:8080/board/write`

## 📚 학습 포인트

### 1. MVC 패턴 이해

- **Model**: 데이터와 비즈니스 로직
- **View**: 사용자 인터페이스
- **Controller**: 사용자 요청 처리 및 조정

### 2. Servlet 생명주기

- **초기화**: `init()` 메서드
- **요청 처리**: `doGet()`, `doPost()` 메서드
- **소멸**: `destroy()` 메서드

### 3. JSTL 활용

- **반복문**: `<c:forEach>`
- **조건문**: `<c:if>`, `<c:choose>`
- **출력**: `${}` EL 표현식

### 4. 데이터베이스 풀

- **JNDI**: 자바 네이밍 서비스
- **DataSource**: 커넥션 풀 관리
- **폴백 메커니즘**: 대체 연결 방식

## 🔮 다음 단계

Model 2 아키텍처는 현대적인 Java 웹 프레임워크의 기반이 되었습니다:

- **Spring MVC**: Model 2를 기반으로 한 프레임워크
- **Struts**: Apache의 Model 2 구현체
- **JSF**: JavaServer Faces

이 프로젝트를 통해 **전통적인 Java 웹 개발의 핵심 개념**을 이해하고, **현대적인 프레임워크의 기반**을 다질 수 있습니다.

---

**03-model2**는 2000년대 초반 Java 웹 개발의 **진화된 아키텍처**를 보여주는 중요한 예제입니다.
MVC 패턴의 이해와 Servlet/JSP/JSTL의 활용을 통해 **실무에서 사용할 수 있는 웹 애플리케이션 개발 능력**을 기를 수 있습니다.
