package io.goorm.backend.controller;

import io.goorm.backend.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 게시글 상세보기 Servlet
 * Model 2 아키텍처의 컨트롤러 역할
 */
@WebServlet("/board/view")
public class BoardViewServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * GET 요청 처리 - 게시글 상세보기
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      // 게시글 ID 파라미터 추출
      String idParam = request.getParameter("id");

      if (idParam == null || idParam.trim().isEmpty()) {
        // ID가 없으면 목록 페이지로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/board/list");
        return;
      }

      Long id = Long.parseLong(idParam.trim());

      // DAO를 통해 게시글 조회
      BoardDAO dao = new BoardDAO();
      Board board = dao.getBoardById(id);

      if (board != null) {
        // 게시글을 찾았으면 JSP로 전달
        request.setAttribute("board", board);
        request.getRequestDispatcher("/board/view.jsp").forward(request, response);
      } else {
        // 게시글을 찾지 못했으면 에러 메시지와 함께 목록 페이지로 포워딩
        request.setAttribute("error", "해당 게시글을 찾을 수 없습니다.");
        request.getRequestDispatcher("/board/list.jsp").forward(request, response);
      }

    } catch (NumberFormatException e) {
      // ID 파라미터가 숫자가 아닌 경우
      request.setAttribute("error", "잘못된 게시글 ID입니다.");
      request.getRequestDispatcher("/board/list.jsp").forward(request, response);
    } catch (Exception e) {
      // 기타 오류 발생 시
      request.setAttribute("error", "게시글 조회 중 오류가 발생했습니다: " + e.getMessage());
      request.getRequestDispatcher("/error.jsp").forward(request, response);
    }
  }

  /**
   * POST 요청 처리 - GET과 동일하게 처리
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}
