package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardViewCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      String idStr = request.getParameter("id");
      if (idStr == null || idStr.trim().isEmpty()) {
        request.setAttribute("error", "게시글 ID가 필요합니다.");
        return "/board/list.jsp";
      }

      Long id = Long.parseLong(idStr);
      BoardDAO dao = new BoardDAO();
      Board board = dao.getBoardById(id);

      if (board == null) {
        request.setAttribute("error", "존재하지 않는 게시글입니다.");
        return "/board/list.jsp";
      }

      request.setAttribute("board", board);
      return "/board/view.jsp";

    } catch (NumberFormatException e) {
      request.setAttribute("error", "잘못된 게시글 ID입니다.");
      return "/board/list.jsp";
    } catch (Exception e) {
      request.setAttribute("error", "게시글을 불러오는데 실패했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }
}
