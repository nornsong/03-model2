# 3단계: Gradle 빌드시스템 추가

## 🎯 목표

**현재 빌드시스템이 없는 상태**에서 Gradle을 사용한 자동화된 빌드 시스템을 **수동으로 추가**

## ⚠️ 중요: IntelliJ에는 자동 변환 메뉴가 없습니다!

**Eclipse**에는 "Convert to Gradle Project" 메뉴가 있지만, **IntelliJ IDEA**에는 해당 메뉴가 없습니다.  
따라서 **수동으로 Gradle 파일들을 만들고 프로젝트를 다시 불러와야** 합니다.

## 📋 준비사항

- 1단계 완료 (Front-end Controller Pattern 적용)
- 2단계 완료 (JdbcTemplate 적용)
- IntelliJ IDEA Ultimate 사용

## 📚 이론 및 배경 설명

### **현재 빌드시스템이 없는 상태의 불편함**

#### 1. 라이브러리 수동 관리의 번거로움

- **JAR 파일 수동 다운로드**: Spring, H2 등 필요한 라이브러리를 직접 다운로드
- **버전 관리 어려움**: 각 라이브러리의 버전을 일일이 확인하고 호환성 체크
- **의존성 충돌**: 라이브러리 간 의존성 문제 발생 시 수동으로 해결
- **프로젝트 공유 시 복잡성**: 다른 개발자와 프로젝트 공유 시 라이브러리 설정을 별도로 안내

#### 2. 빌드 프로세스의 비효율성

- **수동 컴파일**: IntelliJ의 빌드 시스템에만 의존
- **WAR 파일 생성**: Artifacts에서 수동으로 설정하고 생성
- **배포 시 오류**: 라이브러리 누락이나 경로 문제로 배포 실패 가능성

#### 3. 팀 협업의 어려움

- **환경 설정 불일치**: 개발자마다 다른 라이브러리 버전 사용 가능성
- **새 팀원 온보딩**: 프로젝트 설정에 시간이 오래 걸림

### **Gradle 도입의 장점**

#### 1. 의존성 자동 관리

- **중앙 저장소 활용**: Maven Central에서 자동으로 최신 버전 다운로드
- **의존성 해결**: 라이브러리 간 의존성을 자동으로 분석하고 해결
- **버전 관리**: `build.gradle`에서 한 번에 모든 라이브러리 버전 관리

#### 2. 빌드 프로세스 자동화

- **표준화된 빌드**: `gradlew build` 명령어로 일관된 빌드
- **WAR 자동 생성**: `gradlew war` 명령어로 배포용 파일 자동 생성
- **증분 빌드**: 변경된 부분만 다시 컴파일하여 빌드 시간 단축

#### 3. 프로젝트 표준화

- **Maven 표준 구조**: `src/main/java`, `src/main/webapp` 등 표준 디렉토리 구조
- **크로스 플랫폼**: Windows, macOS, Linux에서 동일하게 동작
- **CI/CD 통합**: Jenkins, GitHub Actions 등과 쉽게 연동

### **IntelliJ Ultimate에서 Gradle 사용의 이점**

#### 1. IDE와의 완벽한 통합

- **Gradle 탭**: 의존성, 태스크, 빌드 상태를 한눈에 확인
- **실시간 동기화**: `build.gradle` 변경 시 자동으로 프로젝트 동기화
- **코드 완성**: Gradle DSL에 대한 IntelliSense 지원

#### 2. 개발 생산성 향상

- **자동 리프레시**: 라이브러리 추가/제거 시 자동으로 프로젝트 구조 업데이트
- **빌드 모니터링**: 빌드 진행 상황을 실시간으로 확인
- **에러 추적**: 빌드 실패 시 정확한 원인과 해결 방법 제시

### **Gradle 변환 후 변화점**

#### **자동으로 유지되는 것:**

- ✅ **Application context**: `/` (루트로 설정되어 있음)
- ✅ **URL**: `http://localhost:8080/` (루트로 접근)
- ✅ **포트**: 8080 (변경 없음)

#### **자동으로 변경되는 것:**

- 🔄 **소스 폴더**: `src/` → `src/main/java/` (자동 변환)
- 🔄 **웹 리소스**: `webapp/` → `src/main/webapp/` (자동 변환)
- 🔄 **컴파일 출력**: `out/` → `build/classes/` (자동 변환)

#### **확인이 필요한 것:**

- ⚠️ **Deployment 경로**: Tomcat 설정에서 `src/main/webapp/` 폴더 사용 확인

## 🚀 실습 단계별 진행

### 0단계: 현재 상태 확인

**현재 빌드시스템이 없는 상태인지 확인합니다.**

#### 0-1. Project 창에서 확인

1. 왼쪽 Project 창에서 프로젝트 루트 폴더 `03-model2` 확인
2. `build.gradle`, `settings.gradle` 파일이 **없는지** 확인
3. `gradle` 폴더가 **없는지** 확인

#### 0-2. Gradle 탭 확인

1. 오른쪽 Gradle 탭이 **보이지 않는지** 확인
2. 만약 Gradle 탭이 보인다면 이미 Gradle 프로젝트입니다

### 1단계: Gradle 설정 파일 생성 (수동)

**프로젝트 루트에 Gradle 설정 파일들을 하나씩 만듭니다.**

#### 1-1. build.gradle 파일 생성

1. Project 창에서 `03-model2` 폴더 **우클릭**
2. `New` → `File` 선택
3. 파일명에 `build.gradle` 입력 후 `Enter` 키
4. 생성된 파일이 프로젝트 루트에 있는지 확인

#### 1-2. settings.gradle 파일 생성

1. Project 창에서 `03-model2` 폴더 **우클릭**
2. `New` → `File` 선택
3. 파일명에 `settings.gradle` 입력 후 `Enter` 키
4. 생성된 파일이 프로젝트 루트에 있는지 확인

### 2단계: Gradle 설정 파일 내용 작성

**생성된 파일들에 내용을 입력합니다.**

#### 2-1. build.gradle 내용 입력

1. `build.gradle` 파일을 **더블클릭**하여 열기
2. 다음 내용을 **전체 복사하여 붙여넣기**:

```gradle
plugins {
    id 'java'
    id 'war'
}

group = 'io.goorm.backend'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    // Spring Framework
    implementation 'org.springframework:spring-core:5.3.20'
    implementation 'org.springframework:spring-context:5.3.20'
    implementation 'org.springframework:spring-jdbc:5.3.20'
    implementation 'org.springframework:spring-tx:5.3.20'

    // Database
    implementation 'com.h2database:h2:2.1.214'

    // Servlet & JSP - provided scope 사용 (Tomcat에서 제공)
    providedCompile 'javax.servlet:javax.servlet-api:4.0.1'
    providedCompile 'javax.servlet.jsp:javax.servlet.jsp-api:2.2'
    implementation 'javax.servlet:jstl:1.2'

    // Test
    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.2'
}

test {
    useJUnitPlatform()
}

// WAR 파일 설정
war {
    archiveBaseName = '03-model2'
    archiveVersion = '1.0'
}
```

#### 2-2. settings.gradle 내용 입력

1. `settings.gradle` 파일을 **더블클릭**하여 열기
2. 다음 내용을 **전체 복사하여 붙여넣기**:

```gradle
rootProject.name = '03-model2'
```

### 3단계: IntelliJ에서 Gradle 프로젝트로 인식시키기

**Gradle 파일들을 생성했으니 IntelliJ가 인식하도록 합니다.**

#### 3-1. 프로젝트 재로드

1. 상단 메뉴 `File` → `Reload All from Disk` 클릭
2. 또는 `File` → `Invalidate Caches and Restart` 클릭

#### 3-2. Gradle 프로젝트로 인식 확인

1. 오른쪽 `Gradle` 탭이 나타나는지 확인
2. Gradle 탭에서 프로젝트가 `03-model2`로 표시되는지 확인
3. Dependencies가 자동으로 다운로드되는지 확인

### 4단계: 기존 라이브러리 정리

#### 4-1. lib 폴더 정리

- `webapp/WEB-INF/lib/` 폴더의 모든 JAR 파일 삭제
- Gradle이 의존성을 자동으로 다운로드

#### 4-2. 프로젝트 설정 업데이트

```
Project Structure → Modules → Dependencies
├── 기존 JAR 파일들 제거
└── Gradle 의존성으로 대체됨
```

## 📝 완료 체크리스트

- [ ] Gradle 설정 파일들 수동 생성 (build.gradle, settings.gradle)
- [ ] IntelliJ에서 프로젝트 재로드하여 Gradle 인식
- [ ] Gradle 탭에서 의존성 자동 다운로드 확인
- [ ] 프로젝트 구조 자동 변환 확인
- [ ] 기존 lib 폴더 정리
- [ ] Gradle 빌드 테스트
- [ ] WAR 파일 생성 테스트

## ⚠️ 주의사항

- **Gradle 설정 파일 생성 후 프로젝트 재로드 필수**
  - `build.gradle`, `settings.gradle` 파일 생성 후
  - `File → Reload All from Disk` 실행
- **Gradle 변환 후 기존 수동 라이브러리 제거 필수**
  - `webapp/WEB-INF/lib/` 폴더의 모든 JAR 파일 삭제
  - Project Structure → Modules → Dependencies에서 기존 JAR들 제거
- **Spring Framework 버전 호환성 확인**
- **Tomcat 서버 설정 재확인 필요**
  - Deployment 탭에서 WAR 파일 경로 확인
  - Application context 설정 유지
- **Gradle 동기화 완료 확인**
  - Gradle 탭에서 새로고침 버튼 클릭
  - 의존성 다운로드 완료까지 대기

## 🔄 Gradle 명령어

### 기본 명령어

```bash
# 프로젝트 빌드
./gradlew build

# WAR 파일 생성
./gradlew war

# 의존성 다운로드
./gradlew dependencies

# 프로젝트 정리
./gradlew clean
```

### IntelliJ에서 실행

```
View → Tool Windows → Gradle
├── Tasks → build → build (더블클릭)
├── Tasks → war → war (더블클릭)
└── Dependencies → 새로고침
```

## 🎯 다음 단계

- 파일 업로드 기능 구현
- 회원가입/로그인/로그아웃 기능 구현
- Spring MVC로 전환 고려
