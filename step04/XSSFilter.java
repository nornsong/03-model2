package io.goorm.backend.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * XSS(Cross-Site Scripting) 공격 방지를 위한 필터
 * 모든 요청의 파라미터와 헤더 값을 검사하여 악성 스크립트를 제거합니다.
 */
@WebFilter("/*")
public class XSSFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // 필터 초기화 - 로그 출력
    System.out.println("XSSFilter 초기화 완료");
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
    // 필터 소멸 - 로그 출력
    System.out.println("XSSFilter 소멸");
  }

  /**
   * XSS 방지를 위한 요청 래퍼 클래스
   * HttpServletRequest의 메서드를 오버라이드하여 XSS 방지 로직을 적용
   */
  private static class XSSRequestWrapper extends HttpServletRequestWrapper {

    public XSSRequestWrapper(HttpServletRequest request) {
      super(request);
    }

    /**
     * 단일 파라미터 값에 XSS 방지 적용
     */
    @Override
    public String getParameter(String parameter) {
      String value = super.getParameter(parameter);
      return stripXSS(value);
    }

    /**
     * 다중 파라미터 값에 XSS 방지 적용
     */
    @Override
    public String[] getParameterValues(String parameter) {
      String[] values = super.getParameterValues(parameter);
      if (values == null)
        return null;

      int count = values.length;
      String[] encodedValues = new String[count];
      for (int i = 0; i < count; i++) {
        encodedValues[i] = stripXSS(values[i]);
      }
      return encodedValues;
    }

    /**
     * 헤더 값에 XSS 방지 적용
     */
    @Override
    public String getHeader(String name) {
      String value = super.getHeader(name);
      return stripXSS(value);
    }

    /**
     * XSS 패턴을 제거하고 안전한 문자열로 변환
     * 
     * @param value 원본 문자열
     * @return XSS 방지가 적용된 안전한 문자열
     */
    private String stripXSS(String value) {
      if (value == null)
        return null;

      // 악성 스크립트 패턴 제거
      value = value.replaceAll("", "");

      // HTML 태그를 HTML 엔티티로 변환
      value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

      // JavaScript 이벤트 핸들러 제거
      value = value.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
      value = value.replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "");
      value = value.replaceAll("(?i)<.*?\\s+on.*?=.*?>", "");

      // 위험한 속성 제거 (중복 제거)
      value = value.replaceAll("(?i)<.*?\\s+on.*?=.*?>", "");

      // 추가 보안 패턴
      value = value.replaceAll("(?i)javascript:", "");
      value = value.replaceAll("(?i)vbscript:", "");
      value = value.replaceAll("(?i)expression\\(", "");
      value = value.replaceAll("(?i)applet", "");
      value = value.replaceAll("(?i)embed", "");
      value = value.replaceAll("(?i)object", "");
      value = value.replaceAll("(?i)iframe", "");
      value = value.replaceAll("(?i)frame", "");
      value = value.replaceAll("(?i)frameset", "");
      value = value.replaceAll("(?i)meta", "");
      value = value.replaceAll("(?i)link", "");

      // 위험한 CSS 표현식 제거
      value = value.replaceAll("(?i)expression\\s*\\(", "");
      value = value.replaceAll("(?i)url\\s*\\(", "");

      // SQL 인젝션 방지 (추가 보안)
      value = value.replaceAll("(?i)select", "");
      value = value.replaceAll("(?i)insert", "");
      value = value.replaceAll("(?i)update", "");
      value = value.replaceAll("(?i)delete", "");
      value = value.replaceAll("(?i)drop", "");
      value = value.replaceAll("(?i)union", "");
      value = value.replaceAll("(?i)exec", "");
      value = value.replaceAll("(?i)execute", "");

      return value;
    }
  }
}
