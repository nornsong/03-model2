package io.goorm.backend.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.User;
import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;

public class FileDeleteCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 로그인 확인
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        return sendErrorResponse(response, "로그인이 필요합니다.");
      }

      // 파일 ID 파라미터 확인
      String fileIdStr = request.getParameter("fileId");
      if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
        return sendErrorResponse(response, "파일 ID가 필요합니다.");
      }

      Long fileId = Long.parseLong(fileIdStr);

      // FileUploadDAO에서 파일 정보 조회
      FileUploadDAO fileUploadDAO = new FileUploadDAO();
      FileUpload fileUpload = fileUploadDAO.getFileById(fileId);

      if (fileUpload == null) {
        return sendErrorResponse(response, "파일을 찾을 수 없습니다.");
      }

      // 권한 확인 (게시글 작성자만 삭제 가능)
      User user = (User) session.getAttribute("user");
      BoardDAO boardDAO = new BoardDAO();
      Board board = boardDAO.getBoardById(fileUpload.getBoardId());

      if (board == null) {
        return sendErrorResponse(response, "게시글을 찾을 수 없습니다.");
      }

      if (!board.getAuthor().equals(user.getId().toString())) {
        return sendErrorResponse(response, "파일을 삭제할 권한이 없습니다.");
      }

      // 실제 파일 삭제 및 데이터베이스에서 제거
      if (fileUploadDAO.deleteFile(fileId)) {
        return sendSuccessResponse(response, "파일이 삭제되었습니다.");
      } else {
        return sendErrorResponse(response, "파일 삭제에 실패했습니다.");
      }

    } catch (NumberFormatException e) {
      return sendErrorResponse(response, "잘못된 파일 ID입니다.");
    } catch (Exception e) {
      return sendErrorResponse(response, "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  private String sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", message);

    Gson gson = new Gson();
    String jsonResponse = gson.toJson(result);

    PrintWriter out = response.getWriter();
    out.print(jsonResponse);
    out.flush();

    return null; // AJAX 응답이므로 null 반환
  }

  private String sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> result = new HashMap<>();
    result.put("success", false);
    result.put("message", message);

    Gson gson = new Gson();
    String jsonResponse = gson.toJson(result);

    PrintWriter out = response.getWriter();
    out.print(jsonResponse);
    out.flush();

    return null; // AJAX 응답이므로 null 반환
  }
}
