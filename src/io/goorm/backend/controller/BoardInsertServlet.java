package io.goorm.backend.controller;

import io.goorm.backend.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 게시글 등록 처리 Servlet
 * Model 2 아키텍처의 컨트롤러 역할
 */
@WebServlet("/board/insert")
public class BoardInsertServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * POST 요청 처리 - 게시글 등록
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // 문자 인코딩 설정
    request.setCharacterEncoding("UTF-8");

    try {
      // 폼 파라미터 추출
      String title = request.getParameter("title");
      String content = request.getParameter("content");
      String author = request.getParameter("author");

      // 입력값 검증
      if (title == null || title.trim().isEmpty() ||
          content == null || content.trim().isEmpty() ||
          author == null || author.trim().isEmpty()) {

        request.setAttribute("error", "모든 필드를 입력해주세요.");
        request.getRequestDispatcher("/board/write.jsp").forward(request, response);
        return;
      }

      // Board 객체 생성
      Board board = new Board(title.trim(), content.trim(), author.trim());

      // DAO를 통해 게시글 등록
      BoardDAO dao = new BoardDAO();
      int result = dao.insertBoard(board);

      if (result > 0) {
        // 성공 시 목록 페이지로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/board/list");
      } else {
        // 실패 시 에러 메시지와 함께 글쓰기 폼으로 포워딩
        request.setAttribute("error", "게시글 등록에 실패했습니다.");
        request.getRequestDispatcher("/board/write.jsp").forward(request, response);
      }

    } catch (Exception e) {
      // 오류 발생 시 에러 메시지와 함께 글쓰기 폼으로 포워딩
      request.setAttribute("error", "오류가 발생했습니다: " + e.getMessage());
      request.getRequestDispatcher("/board/write.jsp").forward(request, response);
    }
  }

  /**
   * GET 요청 처리 - POST 요청으로 리다이렉트
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendRedirect(request.getContextPath() + "/board/write");
  }
}
