# Step 1: 파일 업로드 기본 구조 설정

## 개요

파일 업로드를 위한 기본 구조와 설정을 구성합니다.

## 1단계: 업로드 설정 파일 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/resources/upload.properties` | 업로드 설정 파일 |

**upload.properties 주요 설정:**

```properties
# 업로드 루트 경로 (절대 경로 사용 권장)
upload.root.path=D:/workspace-goorm-new/03-model2/uploads
upload.files.path=D:/workspace-goorm-new/03-model2/uploads/files
upload.images.path=D:/workspace-goorm-new/03-model2/uploads/images

# 웹에서 접근 가능한 이미지 경로
upload.web.images.path=/uploads/images

# 허용 가능한 파일 확장자 (화이트리스트)
upload.allowed.files=.pdf,.doc,.docx,.xls,.xlsx,.zip,.rar,.txt,.csv
upload.allowed.images=.jpg,.jpeg,.png,.gif,.webp,.bmp

# 파일 크기 제한 (바이트 단위)
upload.max.file.size=10485760
upload.max.image.size=5242880
upload.buffer.size=8192
```

---

## 2단계: 업로드 설정 관리 클래스 생성

**생성되는 파일:**
| 파일 경로 | 설명 |
|-----------|------|
| `src/main/java/io/goorm/backend/config/UploadConfig.java` | 업로드 설정 관리 클래스 |

**UploadConfig.java 주요 기능:**

- Properties 파일 로딩
- 업로드 경로 관리
- 허용 확장자 검증
- 파일 크기 제한 관리
- Singleton 패턴으로 인스턴스 관리

**주요 메서드:**

- `getInstance()`: Singleton 인스턴스 반환
- `getRootPath()`: 업로드 루트 경로 반환
- `getFilesPath()`: 일반 파일 업로드 경로 반환
- `getImagesPath()`: 이미지 파일 업로드 경로 반환
- `isImageFile(String filename)`: 이미지 파일 여부 확인
- `isFileFile(String filename)`: 일반 파일 여부 확인

---

## 3단계: Front Controller에 Multipart 설정 추가

**수정되는 파일:**
| 파일 경로 | 수정 내용 |
|-----------|-----------|
| `src/main/java/io/goorm/backend/controller/FrontController.java` | `@MultipartConfig` 어노테이션 추가 |

**FrontController.java 추가 설정:**

```java
@WebServlet("/front")
@MultipartConfig(
    maxFileSize = 10485760,      // 10MB
    maxRequestSize = 52428800,   // 50MB
    fileSizeThreshold = 2097152  // 2MB
)
public class FrontController extends HttpServlet {
    // ... 기존 코드
}
```

**@MultipartConfig 설정 설명:**

- `maxFileSize`: 개별 파일 최대 크기
- `maxRequestSize`: 전체 요청 최대 크기
- `fileSizeThreshold`: 메모리에 저장할 임계값

---

## 4단계: 업로드 디렉토리 구조 생성

**생성되는 디렉토리:**

```
D:/workspace-goorm-new/03-model2/uploads/
├── files/          # 일반 파일 저장 (보안상 웹 접근 불가)
└── images/         # 이미지 파일 저장 (웹에서 직접 접근 가능)
```

**디렉토리 특징:**

- `files/`: 일반 문서, 압축파일 등 (Java를 통한 다운로드)
- `images/`: 이미지 파일 (웹에서 직접 링크로 접근)

---

## 5단계: Tomcat 설정으로 이미지 디렉토리 웹 접근 허용

**Tomcat 설정 (IntelliJ):**

1. Run/Debug Configurations → Tomcat Server → Edit Configurations
2. Deployment 탭 → Application context 설정
3. 이미지 디렉토리를 웹에서 접근 가능하도록 설정

**또는 web.xml에 설정:**

```xml
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/uploads/*</url-pattern>
</servlet-mapping>
```

---

## 완료 체크리스트

- [ ] upload.properties 파일 생성
- [ ] UploadConfig 클래스 생성
- [ ] FrontController에 @MultipartConfig 추가
- [ ] 업로드 디렉토리 구조 생성
- [ ] Tomcat 설정으로 이미지 디렉토리 웹 접근 허용
- [ ] 설정 파일 로딩 테스트

---

## 테스트 방법

1. **설정 파일 로딩 테스트:**

   - 애플리케이션 실행
   - 서버 콘솔에서 UploadConfig 로그 확인
   - 업로드 경로가 올바르게 설정되었는지 확인

2. **디렉토리 생성 테스트:**

   - 애플리케이션 실행 후 업로드 디렉토리 자동 생성 확인
   - `D:/workspace-goorm-new/03-model2/uploads/` 경로에 디렉토리 생성 확인

3. **Multipart 설정 테스트:**
   - `@MultipartConfig` 어노테이션이 정상적으로 적용되었는지 확인
   - multipart 요청 처리 가능 여부 확인

---

## 다음 단계

기본 구조 설정이 완료되면 다음 단계인 **파일 업로드 모델 및 DAO 구현**을 진행합니다.
