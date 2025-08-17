# 03-model2 빌드 및 배포 가이드

## 📋 프로젝트 개요

**03-model2**는 2000년대 초반 **Model 2 아키텍처**를 구현한 Java 웹 애플리케이션입니다.

- **Servlet**: 컨트롤러 역할 (요청 처리, DAO 호출)
- **JSP**: 뷰 역할 (화면 출력)
- **DAO**: 모델 역할 (비즈니스 로직, 데이터 액세스)
- **데이터베이스 풀**: JNDI를 통한 커넥션 풀링

## 🏗️ 빌드 과정

### 1단계: Java 소스 컴파일

```bash
# src 디렉토리로 이동
cd src

# Java 소스 컴파일 (클래스패스에 servlet-api.jar 필요)
javac -cp ".;../lib/servlet-api.jar" io/goorm/backend/*.java
javac -cp ".;../lib/servlet-api.jar" io/goorm/backend/controller/*.java

# 컴파일된 클래스 파일들을 webapp/WEB-INF/classes로 복사
mkdir -p ../webapp/WEB-INF/classes/io/goorm/backend
mkdir -p ../webapp/WEB-INF/classes/io/goorm/backend/controller
copy io\goorm\backend\*.class ..\webapp\WEB-INF\classes\io\goorm\backend\
copy io\goorm\backend\controller\*.class ..\webapp\WEB-INF\classes\io\goorm\backend\controller\
```

### 2단계: 필요한 라이브러리 준비

```bash
# webapp/WEB-INF/lib 디렉토리 생성
mkdir -p ../webapp/WEB-INF/lib

# 필요한 JAR 파일들을 lib 디렉토리에 복사
# - h2-2.x.x.jar (H2 Database)
# - jstl-1.2.jar (JSTL)
# - standard-1.1.2.jar (JSTL 표준 라이브러리)
# - servlet-api.jar (Servlet API - 컴파일 시에만 필요, 배포 시에는 불필요)
```

### 3단계: WAR 파일 생성

```bash
# webapp 디렉토리로 이동
cd ../webapp

# WAR 파일 생성
jar -cvf ../03-model2.war *

# 또는 수동으로 디렉토리 구조 생성
mkdir -p 03-model2
copy * 03-model2\
cd 03-model2
jar -cvf ..\03-model2.war *
```

## 🚀 배포 과정

### Tomcat 배포

```bash
# Tomcat webapps 디렉토리에 WAR 파일 복사
copy 03-model2.war %TOMCAT_HOME%\webapps\

# 또는 ROOT 디렉토리에 직접 배포
copy 03-model2.war %TOMCAT_HOME%\webapps\ROOT.war
```

### 데이터베이스 풀 설정 (선택사항)

Tomcat의 `conf/server.xml` 또는 `conf/context.xml`에 데이터베이스 풀 설정:

```xml
<!-- conf/context.xml -->
<Context>
    <Resource name="jdbc/BoardDB"
              auth="Container"
              type="javax.sql.DataSource"
              maxTotal="20"
              maxIdle="10"
              maxWaitMillis="-1"
              username="sa"
              password=""
              driverClassName="org.h2.Driver"
              url="jdbc:h2:./goorm_db"/>
</Context>
```

## 📁 최종 배포 구조

```
%TOMCAT_HOME%/webapps/ROOT/
├── WEB-INF/
│   ├── classes/
│   │   └── io/goorm/backend/
│   │       ├── Board.class
│   │       ├── BoardDAO.class
│   │       └── controller/
│   │           ├── BoardListServlet.class
│   │           ├── BoardWriteServlet.class
│   │           ├── BoardInsertServlet.class
│   │           └── BoardViewServlet.class
│   ├── lib/
│   │   ├── h2-2.x.x.jar
│   │   ├── jstl-1.2.jar
│   │   └── standard-1.1.2.jar
│   └── web.xml
├── board/
│   ├── list.jsp
│   ├── write.jsp
│   └── view.jsp
└── index.jsp
```

## 🔧 실행 및 테스트

### 접근 URL

- **메인 페이지**: `http://localhost:8080/`
- **게시글 목록**: `http://localhost:8080/board/list`
- **글쓰기**: `http://localhost:8080/board/write`
- **게시글 보기**: `http://localhost:8080/board/view?id=1`

### 데이터베이스 확인

- H2 Database 콘솔: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:./goorm_db`
- 사용자명: `sa`
- 비밀번호: (빈 값)

## ⚠️ 주의사항

### 컴파일 시

- `servlet-api.jar`가 클래스패스에 있어야 함
- Java 8~11 호환성 확인

### 배포 시

- `servlet-api.jar`는 배포하지 않음 (Tomcat에 이미 포함)
- JSTL 라이브러리 필수 포함
- 데이터베이스 풀 설정 시 JNDI 이름 확인

### 런타임 시

- 데이터베이스 풀 설정이 없으면 직접 연결로 폴백
- JSTL 태그 라이브러리 경로 확인

## 🎯 Model 2의 특징

### 장점

- **역할 분리**: Servlet(컨트롤러), JSP(뷰), DAO(모델)
- **코드 재사용**: DAO를 통한 비즈니스 로직 분리
- **유지보수성**: 각 컴포넌트의 독립적 수정 가능
- **확장성**: 새로운 기능 추가 용이

### 2000년대 초반 특징

- **Servlet 2.4+**: `@WebServlet` 어노테이션 지원
- **JSP 2.0**: EL(Expression Language) 지원
- **JSTL 1.2**: 표준 태그 라이브러리
- **데이터베이스 풀**: JNDI를 통한 커넥션 풀링
- **MVC 패턴**: Model-View-Controller 아키텍처

이 가이드를 따라하면 2000년대 초반 Model 2 아키텍처의 Java 웹 애플리케이션을 성공적으로 빌드하고 배포할 수 있습니다.
