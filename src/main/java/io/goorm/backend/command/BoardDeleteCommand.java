package io.goorm.backend.command;

import io.goorm.backend.BoardDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

      if ("true".equals(confirm)) {
        // 삭제 확인됨 - 실제 삭제 처리
        BoardDAO dao = new BoardDAO();
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
