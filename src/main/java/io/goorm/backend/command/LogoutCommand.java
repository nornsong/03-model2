package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 로그아웃 처리 Command
 */
public class LogoutCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 세션 무효화
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }

      // 메인 페이지로 리다이렉트
      response.sendRedirect(request.getContextPath() + "/");
      return null;

    } catch (Exception e) {
      // 에러 발생 시에도 메인 페이지로 이동
      try {
        response.sendRedirect(request.getContextPath() + "/");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      return null;
    }
  }
}
