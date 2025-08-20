package io.goorm.backend.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Pattern;

public class XSSFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // 필터 초기화
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // XSS 방지 래퍼로 요청을 감싸서 전달
    XSSRequestWrapper wrappedRequest = new XSSRequestWrapper((HttpServletRequest) request);
    chain.doFilter(wrappedRequest, response);
  }

  @Override
  public void destroy() {
    // 필터 소멸
  }

  // XSS 방지를 위한 요청 래퍼 클래스
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
      if (values == null) {
        return null;
      }

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
      if (value == null) {
        return null;
      }

      // HTML 태그 제거
      value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

      // 스크립트 태그 제거
      value = value.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
      value = value.replaceAll("(?i)<script.*?>", "");

      // 이벤트 핸들러 제거
      value = value.replaceAll("(?i)on\\w+\\s*=", "");

      // javascript: 프로토콜 제거
      value = value.replaceAll("(?i)javascript:", "");

      // vbscript: 프로토콜 제거
      value = value.replaceAll("(?i)vbscript:", "");

      // expression() 제거
      value = value.replaceAll("(?i)expression\\s*\\(", "");

      // eval() 제거
      value = value.replaceAll("(?i)eval\\s*\\(", "");

      // <iframe> 제거
      value = value.replaceAll("(?i)<iframe.*?>.*?</iframe.*?>", "");

      // <object> 제거
      value = value.replaceAll("(?i)<object.*?>.*?</object.*?>", "");

      // <embed> 제거
      value = value.replaceAll("(?i)<embed.*?>", "");

      return value;
    }
  }
}
