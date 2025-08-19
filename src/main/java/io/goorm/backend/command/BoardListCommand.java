package io.goorm.backend.command;

import io.goorm.backend.BoardDAO;
import io.goorm.backend.Board;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class BoardListCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 기존 서블릿의 핵심 로직을 그대로 가져옴
      BoardDAO dao = new BoardDAO();
      List<Board> boardList = dao.getBoardList();

      request.setAttribute("boardList", boardList);
      return "/board/list.jsp";

    } catch (Exception e) {
      request.setAttribute("error", "게시글 목록을 불러오는데 실패했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }
}
