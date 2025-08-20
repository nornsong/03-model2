package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class BoardWriteCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 로그인 확인
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect("front?command=login");
        return null;
      }

      // 글쓰기 폼을 보여주는 것이므로 별도 로직 없음
      return "/board/write.jsp";
    } catch (IOException e) {
      request.setAttribute("error", "로그인 페이지로 이동 중 오류가 발생했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }
}
