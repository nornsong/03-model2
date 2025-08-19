package io.goorm.backend.controller;

import io.goorm.backend.*;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 게시글 목록 조회 Servlet
 * Model 2 아키텍처의 컨트롤러 역할
 */
//@WebServlet("/board/list")
public class BoardListServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * GET 요청 처리 - 게시글 목록 조회
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      // DAO를 통해 게시글 목록 조회
      BoardDAO dao = new BoardDAO();
      List<Board> boards = dao.getBoardList();

      // JSP로 전달할 데이터를 request에 저장
      request.setAttribute("boards", boards);

      // JSP로 포워딩 (뷰로 전달)
      request.getRequestDispatcher("/board/list.jsp").forward(request, response);

    } catch (Exception e) {
      // 오류 발생 시 에러 페이지로 포워딩
      request.setAttribute("error", "게시글 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
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
