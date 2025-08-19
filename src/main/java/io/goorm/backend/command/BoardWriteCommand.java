package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardWriteCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    // 글쓰기 폼을 보여주는 것이므로 별도 로직 없음
    return "/board/write.jsp";
  }
}
