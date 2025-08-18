# Step04: XSS 방지 및 보안 강화

## 🎯 목표

파일 업로드 시스템에 XSS(Cross-Site Scripting) 공격 방지 기능을 추가하여 보안을 강화합니다.

## ⚠️ 중요: XSS 공격의 위험성

### XSS 공격이란?

- **정의**: 악성 스크립트를 웹페이지에 삽입하여 사용자 정보를 탈취하거나 악의적인 행위를 수행하는 공격
- **위험성**:
  - 사용자 세션 정보 탈취
  - 개인정보 유출
  - 악성 코드 실행
  - 피싱 공격 유도

### 공격 예시:

```html
<!-- 악성 사용자가 게시글에 삽입한 스크립트 -->
<script>
  alert("XSS 공격!");
</script>
<img src="x" onerror="alert('XSS')" />
<a href="javascript:alert('XSS')">클릭하세요</a>
```

## 📚 이론 포인트 리마인드

### 1. XSS 방지 방법

- **입력값 검증**: 허용된 문자만 입력받기
- **출력값 이스케이프**: 특수문자를 HTML 엔티티로 변환
- **HTTP 헤더 설정**: Content Security Policy 등

### 2. Servlet Filter

- **역할**: 요청/응답을 가로채서 전처리/후처리
- **장점**: 전역적으로 보안 적용 가능
- **구현**: `Filter` 인터페이스 구현

## 🚀 실습 단계별 진행

### 1단계: XSS 방지 이론 학습

#### XSS 공격 유형

1. **Reflected XSS**: URL 파라미터에 스크립트 삽입
2. **Stored XSS**: 데이터베이스에 저장된 악성 스크립트
3. **DOM-based XSS**: 클라이언트 사이드에서 발생

#### 방지 원칙

- **입력 시점**: 허용된 문자만 받기
- **저장 시점**: 데이터베이스에 저장 전 검증
- **출력 시점**: 화면에 표시 전 이스케이프

### 2단계: 개별처리 방법 예제코드

#### JSP에서 fn:escapeXml 사용

```jsp
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 안전하지 않은 출력 -->
<p>${board.title}</p>

<!-- 안전한 출력 (XSS 방지) -->
<p>${fn:escapeXml(board.title)}</p>
<p>${fn:escapeXml(board.content)}</p>
```

#### JavaScript에서 이스케이프

```javascript
function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

// 사용 예시
const userInput = '<script>alert("XSS")</script>';
const safeOutput = escapeHtml(userInput);
console.log(safeOutput); // &lt;script&gt;alert("XSS")&lt;/script&gt;
```

#### Java에서 이스케이프

```java
public class HtmlEscapeUtil {
    public static String escapeHtml(String input) {
        if (input == null) return null;

        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
```

### 3단계: XSSFilter 구현

#### XSSFilter.java 생성

```java
package io.goorm.backend.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Pattern;

@WebFilter("/*")
public class XSSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {

        // XSS 방지 래퍼로 요청 감싸기
        XSSRequestWrapper wrappedRequest = new XSSRequestWrapper((HttpServletRequest) request);

        // 다음 필터 또는 서블릿으로 전달
        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        // 필터 소멸
    }

    // XSS 방지 래퍼 클래스
    private static class XSSRequestWrapper extends HttpServletRequestWrapper {

        public XSSRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String parameter) {
            String value = super.getParameter(parameter);
            return stripXSS(value);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);
            if (values == null) return null;

            int count = values.length;
            String[] encodedValues = new String[count];
            for (int i = 0; i < count; i++) {
                encodedValues[i] = stripXSS(values[i]);
            }
            return encodedValues;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return stripXSS(value);
        }

        // XSS 패턴 제거
        private String stripXSS(String value) {
            if (value == null) return null;

            // 악성 스크립트 패턴 제거
            value = value.replaceAll("", "");

            // HTML 태그 제거
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

            // JavaScript 이벤트 제거
            value = value.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
            value = value.replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "");
            value = value.replaceAll("(?i)<.*?\\s+on.*?=.*?>", "");

            // 위험한 속성 제거
            value = value.replaceAll("(?i)<.*?\\s+on.*?=.*?>", "");
            value = value.replaceAll("(?i)<.*?\\s+on.*?=.*?>", "");

            return value;
        }
    }
}
```

### 4단계: web.xml에 필터 설정

#### web.xml 수정

```xml
<!-- XSS 방지 필터 설정 -->
<filter>
    <filter-name>XSSFilter</filter-name>
    <filter-class>io.goorm.backend.filter.XSSFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>XSSFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

### 5단계: 테스트 및 검증

#### 테스트 시나리오

1. **정상 입력**: 일반 텍스트 입력 후 정상 출력 확인
2. **XSS 시도**: `<script>alert('XSS')</script>` 입력 후 이스케이프 확인
3. **HTML 태그**: `<b>굵은 글씨</b>` 입력 후 이스케이프 확인
4. **JavaScript 이벤트**: `onclick="alert('XSS')"` 입력 후 제거 확인

## 📝 완료 체크리스트

- [ ] XSS 공격 원리 이해
- [ ] 개별처리 방법 예제코드 학습
- [ ] XSSFilter.java 클래스 생성
- [ ] web.xml에 필터 설정 추가
- [ ] XSS 방지 테스트 완료
- [ ] 보안 강화 효과 확인

## ⚠️ 주의사항

### 1. 필터 순서

- **XSSFilter**는 다른 필터보다 **먼저** 실행되어야 함
- `web.xml`에서 필터 순서 확인

### 2. 성능 고려

- 모든 요청에 대해 XSS 검사 수행
- 정규식 패턴 최적화 필요

### 3. 한글 처리

- UTF-8 인코딩 설정 확인
- 한글 문자가 깨지지 않도록 주의

### 4. 테스트 범위

- GET/POST 파라미터 모두 테스트
- 헤더 값도 XSS 방지 적용 확인

## 🎯 테스트 방법

### 1. 기본 테스트

```bash
# 정상 입력 테스트
curl "http://localhost:8080/front?command=boardInsert&title=테스트&content=내용"

# XSS 시도 테스트
curl "http://localhost:8080/front?command=boardInsert&title=<script>alert('XSS')</script>&content=<img src=x onerror=alert('XSS')>"
```

### 2. 브라우저 테스트

- 게시글 작성 폼에서 XSS 코드 입력
- 저장 후 목록/상세보기에서 이스케이프 확인
- 개발자 도구에서 HTML 소스 확인

### 3. 로그 확인

- 필터 동작 로그 확인
- 요청/응답 처리 과정 모니터링

## 🔗 다음 단계

XSS 방지 기능 구현 완료 후:

1. **develop03 생성**: 페이지네이션, 이미지 썸네일, 대용량 업로드
2. **스프링부트 전환**: Spring Security로 보안 강화
3. **프로덕션 배포**: 실제 서비스 환경 적용

## 💡 추가 보안 고려사항

### 1. Content Security Policy (CSP)

```html
<meta
  http-equiv="Content-Security-Policy"
  content="default-src 'self'; script-src 'self' 'unsafe-inline'"
/>
```

### 2. HttpOnly 쿠키

```java
// 세션 쿠키에 HttpOnly 플래그 설정
Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
sessionCookie.setHttpOnly(true);
```

### 3. 입력값 검증 강화

```java
// 정규식을 통한 입력값 검증
Pattern validInput = Pattern.compile("^[a-zA-Z0-9가-힣\\s]+$");
if (!validInput.matcher(input).matches()) {
    throw new IllegalArgumentException("허용되지 않는 문자가 포함되어 있습니다.");
}
```

이제 XSS 방지 기능을 구현하여 보안이 강화된 파일 업로드 시스템을 완성할 수 있습니다!
