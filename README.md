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

## 🎯 서블릿 매핑 방식 비교

### 1. web.xml 방식 (전통적 방식)

```xml
<!-- web.xml에서 서블릿 매핑 -->
<servlet>
    <servlet-name>BoardWriteServlet</servlet-name>
    <servlet-class>io.goorm.backend.controller.BoardWriteServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>BoardWriteServlet</servlet-name>
    <url-pattern>/board/write</url-pattern>
</servlet-mapping>
```

**장점:**

- 중앙 집중식 설정 관리
- 배포 시 설정 변경 가능
- 기존 시스템과의 호환성

**단점:**

- 설정과 코드 분리로 유지보수 복잡
- XML 파일 크기 증가
- 컴파일 없이 설정 변경 불가

### 2. @WebServlet 애노테이션 방식 (현대적 방식)

```java
@WebServlet("/board/write")
public class BoardWriteServlet extends HttpServlet {
    // 서블릿 구현
}
```

**장점:**

- 코드와 설정의 일관성
- 가독성과 유지보수성 향상
- IDE 지원 (자동완성, 리팩토링)

**단점:**

- 설정 변경 시 재컴파일 필요
- 배포 시 설정 변경 불가
- Java 5+ (Servlet 2.5+) 필요

### 3. 혼재 사용 시 주의사항

⚠️ **중요**: 두 방식을 동시에 사용할 때는 **중복 매핑을 피해야 합니다**

```java
// ❌ 잘못된 예시 - 중복 매핑
@WebServlet("/board/write")  // 애노테이션으로 매핑
public class BoardWriteServlet extends HttpServlet {
    // ...
}
```

```xml
<!-- ❌ web.xml에도 같은 URL 매핑이 있으면 중복! -->
<servlet-mapping>
    <servlet-name>BoardWriteServlet</servlet-name>
    <url-pattern>/board/write</url-pattern>  <!-- 중복! -->
</servlet-mapping>
```

**올바른 사용법:**

- **강의 단계 1**: `@WebServlet` 주석처리 + `web.xml` 사용
- **강의 단계 2**: `@WebServlet` 활성화 + `web.xml` 매핑 주석처리

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

### 5. 서블릿 매핑 방식 비교

- **web.xml 방식**: 전통적이고 중앙 집중식
- **@WebServlet 방식**: 현대적이고 코드 중심
- **혼재 사용**: 주의사항과 올바른 활용법

### 6. 보안 및 사용자 관리

- **XSS 방지**: Servlet Filter를 통한 전역 보안
- **사용자 인증**: 로그인/로그아웃 및 세션 관리
- **권한 제어**: 게시글 작성/수정/삭제 권한 관리

### 7. 파일 업로드 시스템

- **다중 파일 업로드**: 최대 5개 파일 동시 업로드
- **보안 검증**: 파일 크기, 형식, 악성코드 검사
- **사용자 경험**: 드래그 앤 드롭 및 진행률 표시

### 8. 고급 기능 및 최적화

- **페이지네이션**: 대용량 데이터 효율적 표시
- **이미지 썸네일**: 자동 썸네일 생성 및 최적화
- **대용량 업로드**: Java IO 스트림을 활용한 청크 업로드
- **검색 기능**: 제목, 내용, 작성자별 통합 검색
- **리치 텍스트 에디터**: CKEditor 통합 및 이미지 업로드

### 9. Java IO 스트림 실무 활용

- **스트림 기반 파일 처리**: 메모리 효율적인 대용량 파일 처리
- **버퍼링 최적화**: BufferedInputStream/OutputStream 성능 향상
- **리소스 관리**: try-with-resources를 통한 안전한 리소스 해제
- **실무 패턴**: 파일 복사, 이동, 삭제 등 실제 업무 활용법

## 🔮 다음 단계

Model 2 아키텍처는 현대적인 Java 웹 프레임워크의 기반이 되었습니다:

- **Spring MVC**: Model 2를 기반으로 한 프레임워크
- **Struts**: Apache의 Model 2 구현체
- **JSF**: JavaServer Faces

이 프로젝트를 통해 **전통적인 Java 웹 개발의 핵심 개념**을 이해하고, **현대적인 프레임워크의 기반**을 다질 수 있습니다.

---

**03-model2**는 2000년대 초반 Java 웹 개발의 **진화된 아키텍처**를 보여주는 중요한 예제입니다.
MVC 패턴의 이해와 Servlet/JSP/JSTL의 활용을 통해 **실무에서 사용할 수 있는 웹 애플리케이션 개발 능력**을 기를 수 있습니다.

## 📁 프로젝트 구조

```
03-model2/
├── src/                    # Java 소스 코드
│   └── io/goorm/backend/
│       ├── Board.java     # 게시글 모델
│       ├── BoardDAO.java  # 데이터 접근 객체
│       ├── filter/        # 보안 필터
│       │   └── XSSFilter.java  # XSS 방지 필터
│       └── controller/    # 서블릿 컨트롤러
│           ├── BoardListServlet.java
│           ├── BoardViewServlet.java
│           ├── BoardWriteServlet.java
│           └── BoardInsertServlet.java
├── webapp/                # 웹 리소스
│   ├── board/            # 게시판 관련 JSP
│   │   ├── list.jsp      # 게시글 목록
│   │   ├── view.jsp      # 게시글 상세보기
│   │   └── write.jsp     # 게시글 작성
│   ├── index.jsp         # 메인 페이지
│   └── WEB-INF/
│       ├── web.xml       # 웹 애플리케이션 설정
│       └── lib/          # 라이브러리
├── develop01/             # 사용자 인증 및 권한 제어
│   ├── README.md         # 기능 개요
│   ├── step01.md         # 회원가입 구현
│   ├── step02.md         # 로그인/로그아웃 구현
│   ├── step03.md         # 권한 제어 구현
│   └── jsp/              # HTML/Tailwind CSS 버전
│       ├── signup.html   # 회원가입 폼
│       ├── login.html    # 로그인 폼
│       └── userInfo.html # 사용자 정보 표시
├── develop02/             # 파일 업로드 시스템 + 보안
│   ├── README.md         # 기능 개요
│   ├── step01.md         # 파일 업로드 기본 구조
│   ├── step02.md         # 파일 업로드 처리
│   ├── step03.md         # 파일 다운로드 및 관리
│   ├── step04.md         # XSS 방지 및 보안 강화
│   └── jsp/              # HTML/Tailwind CSS 버전
│       ├── list.jsp      # 파일 첨부 표시
│       ├── view.jsp      # 파일 다운로드
│       └── write.jsp     # 파일 업로드 폼
├── develop03/             # 고도화된 기능
│   ├── README.md         # 기능 개요
│   ├── step01.md         # 페이지네이션 시스템
│   ├── step02.md         # 이미지 썸네일 생성
│   ├── step03.md         # 대용량 파일 업로드 (Java IO 스트림 실습)
│   ├── step04.md         # 간단한 검색 기능
│   ├── step05.md         # 리치 텍스트 에디터 통합
│   └── theory/           # Java IO 스트림 이론 강의
│       ├── README.md     # 이론 강의 개요
│       ├── 01-basic-concepts.md      # 기본 개념
│       ├── 02-input-streams.md       # 입력 스트림
│       ├── 03-output-streams.md      # 출력 스트림
│       ├── 04-advanced-streams.md    # 고급 스트림
│       └── 05-practical-usage.md     # 실무 활용
├── conversion/            # 아키텍처 리팩토링
│   ├── summary.md        # 전체 과정 요약
│   ├── step01.md         # Front Controller Pattern
│   ├── step02.md         # JdbcTemplate 적용
│   └── step03.md         # Gradle 빌드시스템 추가
├── lib/                   # 외부 라이브러리
├── build.md               # 빌드 및 배포 가이드
└── README.md              # 프로젝트 개요
```

## 🎯 개발 단계별 진행

### **Phase 1: 기본 아키텍처 (conversion/)**

- Front Controller Pattern 적용
- JdbcTemplate으로 데이터 접근 개선
- Gradle 빌드시스템 추가

### **Phase 2: 사용자 관리 (develop01/)**

- 회원가입/로그인 시스템
- 세션 기반 인증
- 권한 기반 접근 제어

### **Phase 3: 파일 업로드 (develop02/)**

- 다중 파일 업로드
- 보안 검증 및 XSS 방지
- 사용자 친화적 UI/UX

### **Phase 4: 고도화 (develop03/)**

- 페이지네이션 및 검색
- 이미지 썸네일 생성
- 대용량 파일 처리 최적화 (Java IO 스트림 실습)
- 리치 텍스트 에디터 통합
- **Java IO 스트림 이론 강의**: 기본 개념부터 실무 활용까지

### **Phase 5: 스프링부트 전환**

- Spring Boot + Spring Security
- 현대적인 웹 프레임워크 적용
- 클라우드 배포 준비
