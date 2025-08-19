package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardUpdateCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      String idStr = request.getParameter("id");

      if (request.getMethod().equals("GET")) {
        // 수정 폼 표시
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
        return "/board/update.jsp";

      } else {
        // POST 요청 - 수정 처리
        request.setCharacterEncoding("UTF-8");

        Long id = Long.parseLong(idStr);
        String title = request.getParameter("title");
        String content = request.getParameter("content");

        if (title == null || title.trim().isEmpty()) {
          request.setAttribute("error", "제목을 입력해주세요.");
          return "/board/update.jsp";
        }

        Board board = new Board();
        board.setId(id);
        board.setTitle(title);
        board.setContent(content);

        BoardDAO dao = new BoardDAO();
        boolean success = dao.updateBoard(board);

        if (success) {
          // 수정 후 상세보기로 이동
          response.sendRedirect("front?command=boardView&id=" + id);
        } else {
          request.setAttribute("error", "게시글 수정에 실패했습니다.");
          return "/board/update.jsp";
        }
        return null;
      }

    } catch (NumberFormatException e) {
      request.setAttribute("error", "잘못된 게시글 ID입니다.");
      return "/board/list.jsp";
    } catch (Exception e) {
      request.setAttribute("error", "게시글 수정에 실패했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }
}
