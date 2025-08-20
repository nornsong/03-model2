package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;
import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class BoardViewCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      String idStr = request.getParameter("id");
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

      // 첨부파일 정보 로드
      try {
        FileUploadDAO fileDAO = new FileUploadDAO();
        List<FileUpload> attachments = fileDAO.getFilesByBoardId(id);
        board.setAttachments(attachments);
        System.out.println("=== BoardViewCommand 첨부파일 로딩 ===");
        System.out.println("게시글 ID: " + id);
        System.out.println("첨부파일 개수: " + attachments.size());
        for (FileUpload file : attachments) {
          System.out.println("파일: " + file.getOriginalFilename() + " (크기: " + file.getFileSize() + ")");
        }
        System.out.println("================================");
      } catch (Exception e) {
        System.out.println("첨부파일 로딩 실패: " + e.getMessage());
        e.printStackTrace();
      }

      request.setAttribute("board", board);
      return "/board/view.jsp";

    } catch (NumberFormatException e) {
      request.setAttribute("error", "잘못된 게시글 ID입니다.");
      return "/board/list.jsp";
    } catch (Exception e) {
      request.setAttribute("error", "게시글을 불러오는데 실패했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }
}
