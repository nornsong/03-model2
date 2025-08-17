# 03-model2 빌드 및 배포 가이드

## 📋 프로젝트 개요

**03-model2**는 2000년대 초반 **Model 2 아키텍처**를 구현한 Java 웹 애플리케이션입니다.

- **Servlet**: 컨트롤러 역할 (요청 처리, DAO 호출)
- **JSP**: 뷰 역할 (화면 출력)
- **DAO**: 모델 역할 (비즈니스 로직, 데이터 액세스)
- **데이터베이스 풀**: JNDI를 통한 커넥션 풀링

## 🚀 개발 환경 설정

### IntelliJ IDEA Ultimate 설정

#### 1. Git에서 프로젝트 가져오기

- **File** → **New** → **Project from Version Control**
- **Git URL**: `https://github.com/topolo-edu/03-model2.git`
- **Directory**: 원하는 로컬 경로 선택
- **Clone** 클릭하여 프로젝트 다운로드

#### 2. 프로젝트 구조 설정

```
File → Project Structure → Modules
├── Sources 탭
│   └── src/io/goorm/backend/ → Sources로 설정
├── Dependencies 탭
│   └── lib/ 폴더의 JAR 파일들을 수동으로 추가
└── Web 탭
    └── Web Resource Directory: webapp/ 설정
```

#### 3. 라이브러리 수동 추가

```
File → Project Structure → Libraries
├── + 버튼 클릭 → Java
├── lib/ 폴더 선택
└── 필요한 JAR 파일들 선택:
    - h2-2.x.x.jar (H2 Database)
    - jstl-1.2.jar (JSTL)
    - standard-1.1.2.jar (JSTL 표준 라이브러리)
```

### Tomcat 서버 설정

#### IntelliJ에서 Tomcat 설정

```
Run → Edit Configurations
├── + 버튼 클릭 → Tomcat Server → Local
├── Application Server: 기존 설치된 Tomcat 9.x 경로 선택
├── Deployment 탭
│   └── + 버튼 → Artifact → 03-model2:war exploded
└── Application context: / (루트 경로)
```

## 🏗️ 빌드 과정

### 1단계: 프로젝트 빌드

```
Build → Build Project (Ctrl + F9)
또는
Build → Rebuild Project
```

### 2단계: Artifact 생성 확인

```
File → Project Structure → Artifacts
├── 03-model2:war exploded
└── Output Directory 확인
```

### 3단계: 필요한 라이브러리 복사

```
webapp/WEB-INF/lib/ 폴더에 다음 JAR 파일들 복사:
├── h2-2.x.x.jar
├── jstl-1.2.jar
├── standard-1.1.2.jar
└── (servlet-api.jar는 배포 시 불필요)
```

## 🚀 실행 및 테스트

### IntelliJ에서 실행

```
Run → Run '03-model2' (Shift + F10)
또는
Run → Debug '03-model2' (Shift + F9)
```

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

## 📁 프로젝트 구조

```
03-model2/
├── src/
│   └── io/goorm/backend/
│       ├── Board.java              # Board VO 클래스
│       ├── BoardDAO.java           # Board DAO 클래스
│       └── controller/             # Servlet 컨트롤러들
│           ├── BoardListServlet.java
│           ├── BoardWriteServlet.java
│           ├── BoardInsertServlet.java
│           └── BoardViewServlet.java
├── webapp/
│   ├── WEB-INF/
│   │   ├── web.xml                 # 웹 애플리케이션 설정
│   │   ├── lib/                    # 필요한 JAR 라이브러리들
│   │   │   ├── h2-2.x.x.jar
│   │   │   ├── jstl-1.2.jar
│   │   │   └── standard-1.1.2.jar
│   │   └── classes/                # 컴파일된 Java 클래스들 (자동 생성)
│   ├── board/                      # 게시판 관련 JSP 페이지들
│   │   ├── list.jsp
│   │   ├── write.jsp
│   │   └── view.jsp
│   └── index.jsp
└── lib/                            # 개발 시 필요한 라이브러리들
    ├── h2-2.x.x.jar
    ├── jstl-1.2.jar
    └── standard-1.1.2.jar
```

## 🔧 데이터베이스 풀 설정 (선택사항)

### Tomcat context.xml 설정

```
%TOMCAT_HOME%/conf/context.xml
또는
webapp/META-INF/context.xml
```

```xml
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

## ⚠️ 주의사항

### 개발 시

- IntelliJ에서 Tomcat 서버 설정 확인
- 프로젝트 구조에서 Sources와 Web 설정 확인

### 배포 시

- JSTL 라이브러리 필수 포함
- 데이터베이스 풀 설정 시 JNDI 이름 확인

### 런타임 시

- 데이터베이스 풀 설정이 없으면 직접 연결로 폴백
- JSTL 태그 라이브러리 경로 확인
- Tomcat 로그에서 에러 메시지 확인

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

## 🔄 다음 단계: Gradle 프로젝트로 전환

이 프로젝트는 나중에 Gradle 프로젝트로 전환할 수 있습니다. IntelliJ IDEA Ultimate의 메뉴를 통해 한 번에 변환할 수 있습니다.

### 1. IDE 메뉴를 통한 Gradle 변환

#### 변환 과정

1. **File** → **New** → **Project from Existing Sources**
2. 프로젝트 루트 폴더 선택
3. **Import project from external model** 선택
4. **Gradle** 선택
5. **Next** 클릭하여 설정 완료

#### 주의사항

- **기존 IntelliJ 프로젝트를 닫고** 새로운 Gradle 프로젝트로 열어야 함
- 기존 설정은 백업 후 새로운 프로젝트로 재설정 필요

### 2. 변환 전후 비교

| 구분                    | 수동 라이브러리 관리            | Gradle 프로젝트                   |
| ----------------------- | ------------------------------- | --------------------------------- |
| **의존성 관리**         | lib/ 폴더에 JAR 파일 수동 추가  | build.gradle에서 자동 관리        |
| **라이브러리 다운로드** | 수동으로 다운로드 후 복사       | Maven Central에서 자동 다운로드   |
| **버전 관리**           | 파일명으로 버전 확인            | build.gradle에서 명시적 버전 관리 |
| **빌드 과정**           | IntelliJ 빌드 도구 사용         | gradlew 명령어로 빌드             |
| **IDE 설정**            | Project Structure에서 수동 설정 | Gradle 설정 자동 인식             |

### 3. 디렉토리 구조 비교

#### 기존 구조 (수동 라이브러리 관리)

```
03-model2/
├── src/
├── webapp/
├── lib/                    # 개발용 라이브러리
│   ├── h2-2.x.x.jar
│   ├── jstl-1.2.jar
│   └── standard-1.1.2.jar
└── .idea/                  # IntelliJ 설정
```

#### Gradle 구조 (자동 의존성 관리)

```
03-model2/
├── src/
├── webapp/
├── build.gradle           # Gradle 설정 파일
├── gradle.properties      # Gradle 속성
├── gradlew                # Gradle Wrapper (Linux/Mac)
├── gradlew.bat            # Gradle Wrapper (Windows)
├── .gradle/               # Gradle 캐시 (자동 생성)
├── build/                 # 빌드 결과물 (자동 생성)
└── .idea/                 # IntelliJ 설정 (Gradle 프로젝트)
```

### 4. 변환 후 필요한 작업

#### 새로운 IntelliJ에서 열기

1. **File** → **Open**
2. 변환된 프로젝트 폴더 선택
3. **Open as Project** 선택
4. IntelliJ가 Gradle 프로젝트로 인식

#### 설정 재구성

- **Tomcat 서버 설정**: Run Configuration 재설정
- **Deployment**: 03-model2:war exploded 재선택
- **Application context**: / (루트 경로) 재설정

### 5. 장점

1. **의존성 자동 관리**: Maven Central에서 라이브러리 자동 다운로드
2. **버전 관리**: 라이브러리 버전을 build.gradle에서 중앙 관리
3. **빌드 자동화**: `gradlew build` 명령으로 자동 빌드
4. **IDE 통합**: IntelliJ에서 Gradle 프로젝트로 완벽 인식
5. **팀 협업**: build.gradle 파일로 프로젝트 설정 공유

### 6. 변환 시기

- **현재**: 수동 라이브러리 관리 방식으로 기본 개념 학습
- **다음 단계**: Gradle 변환 후 현대적인 빌드 도구 체험
- **실무**: Gradle/Maven 등 빌드 도구 사용이 표준

현재는 수동 라이브러리 관리 방식으로 진행하여 기본 개념을 이해한 후, 다음 단계에서 Gradle의 편리함을 체험할 수 있습니다.

---

이 가이드를 따라하면 IntelliJ IDEA Ultimate에서 Tomcat을 설정하여 2000년대 초반 Model 2 아키텍처의 Java 웹 애플리케이션을 개발할 수 있습니다.
