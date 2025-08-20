package io.goorm.backend.command;

import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.User;
import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDeleteCommand implements Command {
  private FileUploadDAO fileUploadDAO;
  private BoardDAO boardDAO;

  public FileDeleteCommand() {
    this.fileUploadDAO = new FileUploadDAO();
    this.boardDAO = new BoardDAO();
  }

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 로그인 확인
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        request.setAttribute("error", "로그인이 필요합니다.");
        return "board/view.jsp";
      }

      User user = (User) session.getAttribute("user");

      // 파일 ID 파라미터
      String fileIdStr = request.getParameter("fileId");
      if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
        request.setAttribute("error", "파일 ID가 필요합니다.");
        return "board/view.jsp";
      }

      Long fileId = Long.parseLong(fileIdStr);

      // 파일 정보 조회
      FileUpload fileUpload = fileUploadDAO.getFileById(fileId);
      if (fileUpload == null) {
        request.setAttribute("error", "파일을 찾을 수 없습니다.");
        return "board/view.jsp";
      }

      // 권한 확인 (파일 소유자만 삭제 가능)
      if (!hasPermissionToDelete(user, fileUpload)) {
        request.setAttribute("error", "파일을 삭제할 권한이 없습니다.");
        return "board/view.jsp";
      }

      // 물리적 파일 삭제
      if (!deletePhysicalFile(fileUpload)) {
        throw new ServletException("물리적 파일 삭제에 실패했습니다.");
      }

      // 데이터베이스에서 파일 정보 삭제
      if (!fileUploadDAO.deleteFile(fileId)) {
        throw new ServletException("데이터베이스에서 파일 정보 삭제에 실패했습니다.");
      }

      // 성공 메시지 설정
      request.setAttribute("message", "파일이 성공적으로 삭제되었습니다.");
      return "board/view.jsp";

    } catch (Exception e) {
      e.printStackTrace();
      request.setAttribute("error", "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
      return "board/view.jsp";
    }
  }

  private boolean hasPermissionToDelete(User user, FileUpload fileUpload) {
    try {
      // 게시글 정보 조회
      Board board = boardDAO.getBoardById(fileUpload.getBoardId());
      if (board == null) {
        return false;
      }

      // 게시글 작성자와 로그인한 사용자가 같은지 확인
      return user.getUsername().equals(board.getAuthor());
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private boolean deletePhysicalFile(FileUpload fileUpload) {
    try {
      Path filePath = Paths.get(fileUpload.getFilePath());

      // 업로드 루트 경로 검증 (보안)
      String uploadRoot = "D:/workspace-goorm-new/03-model2/uploads";
      if (!filePath.normalize().startsWith(Paths.get(uploadRoot))) {
        throw new SecurityException("허용되지 않는 파일 경로입니다.");
      }

      if (Files.exists(filePath)) {
        return Files.deleteIfExists(filePath);
      }
      return true; // 파일이 이미 존재하지 않는 경우
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
