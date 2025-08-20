package io.goorm.backend.controller;

import io.goorm.backend.handler.HandlerMapping;
import io.goorm.backend.command.Command;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/front")
@MultipartConfig(maxFileSize = 10485760, // 10MB
    maxRequestSize = 52428800, // 50MB
    fileSizeThreshold = 2097152 // 2MB
)
public class FrontController extends HttpServlet {

  private HandlerMapping handlerMapping;

  @Override
  public void init() throws ServletException {
    handlerMapping = new HandlerMapping();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String command = request.getParameter("command");

    if (command == null || command.trim().isEmpty()) {
      // 기본값으로 게시글 목록으로 이동
      response.sendRedirect("front?command=boardList");
      return;
    }

    try {
      Command handler = handlerMapping.getCommand(command);

      if (handler == null) {
        request.setAttribute("error", "존재하지 않는 명령입니다: " + command);
        response.sendRedirect("front?command=boardList");
        return;
      }

      String viewPage = handler.execute(request, response);

      if (viewPage != null) {
        // 포워딩
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPage);
        dispatcher.forward(request, response);
      }
      // viewPage가 null이면 리다이렉트가 이미 처리됨

    } catch (Exception e) {
      request.setAttribute("error", "요청 처리 중 오류가 발생했습니다: " + e.getMessage());
      RequestDispatcher dispatcher = request.getRequestDispatcher("/board/list.jsp");
      dispatcher.forward(request, response);
    }
  }
}
