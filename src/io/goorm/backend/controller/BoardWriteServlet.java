package io.goorm.backend.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 게시글 작성 폼 표시 Servlet
 * Model 2 아키텍처의 컨트롤러 역할
 */
//@WebServlet("/board/write")
public class BoardWriteServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * GET 요청 처리 - 글쓰기 폼 표시
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // 글쓰기 폼 JSP로 포워딩
    request.getRequestDispatcher("/board/write.jsp").forward(request, response);
  }

  /**
   * POST 요청 처리 - GET과 동일하게 처리
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}
