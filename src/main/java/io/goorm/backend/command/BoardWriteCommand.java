package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BoardWriteCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    // 로그인 확인
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("front?command=login");
      return null;
    }

    // 글쓰기 폼을 보여주는 것이므로 별도 로직 없음
    return "/board/write.jsp";
  }
}
