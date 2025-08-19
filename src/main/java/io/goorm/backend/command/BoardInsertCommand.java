package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardInsertCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // POST 요청 처리
      request.setCharacterEncoding("UTF-8");

      String title = request.getParameter("title");
      String writer = request.getParameter("writer");
      String content = request.getParameter("content");

      if (title == null || title.trim().isEmpty()) {
        request.setAttribute("error", "제목을 입력해주세요.");
        request.setAttribute("title", title);
        request.setAttribute("writer", writer);
        request.setAttribute("content", content);
        return "/board/write.jsp";
      }

      Board board = new Board();
      board.setTitle(title);
      board.setWriter(writer);
      board.setContent(content);

      BoardDAO dao = new BoardDAO();
      dao.insertBoard(board);

      // 목록으로 리다이렉트
      response.sendRedirect("front?command=boardList");
      return null; // 리다이렉트 시 null 반환

    } catch (Exception e) {
      request.setAttribute("error", "게시글 등록에 실패했습니다: " + e.getMessage());
      return "/board/write.jsp";
    }
  }
}
