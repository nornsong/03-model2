package io.goorm.backend.command;

import io.goorm.backend.BoardDAO;
import io.goorm.backend.Board;
import io.goorm.backend.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BoardDeleteCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      String idStr = request.getParameter("id");
      String confirm = request.getParameter("confirm");

      if (idStr == null || idStr.trim().isEmpty()) {
        request.setAttribute("error", "게시글 ID가 필요합니다.");
        return "/board/list.jsp";
      }

      Long id = Long.parseLong(idStr);

      // 로그인 확인
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect("front?command=login");
        return null;
      }

      // 권한 확인 (본인이 작성한 글만 삭제 가능)
      BoardDAO dao = new BoardDAO();
      Board board = dao.getBoardById(id);
      if (board == null) {
        request.setAttribute("error", "존재하지 않는 게시글입니다.");
        return "/board/list.jsp";
      }

      User user = (User) session.getAttribute("user");
      if (!board.getAuthor().equals(user.getId().toString())) {
        request.setAttribute("error", "본인이 작성한 글만 삭제할 수 있습니다.");
        return "/board/view.jsp";
      }

      if ("true".equals(confirm)) {
        // 삭제 확인됨 - 실제 삭제 처리
        boolean deleted = dao.deleteBoard(id);

        if (deleted) {
          response.sendRedirect("front?command=boardList");
          return null;
        } else {
          request.setAttribute("error", "게시글 삭제에 실패했습니다.");
          return "/board/list.jsp";
        }
      } else {
        // 삭제 확인 페이지로 이동
        request.setAttribute("id", id);
        return "/board/delete.jsp";
      }

    } catch (NumberFormatException e) {
      request.setAttribute("error", "잘못된 게시글 ID입니다.");
      return "/board/list.jsp";
    } catch (Exception e) {
      request.setAttribute("error", "게시글 삭제에 실패했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }
}
