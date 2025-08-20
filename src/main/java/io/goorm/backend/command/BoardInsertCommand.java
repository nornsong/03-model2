package io.goorm.backend.command;

import io.goorm.backend.Board;
import io.goorm.backend.BoardDAO;
import io.goorm.backend.User;
import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.config.UploadConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.Collection;

public class BoardInsertCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 로그인 확인
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect("front?command=login");
        return null;
      }

      // POST 요청 처리
      request.setCharacterEncoding("UTF-8");

      String title = request.getParameter("title");
      String content = request.getParameter("content");

      if (title == null || title.trim().isEmpty()) {
        request.setAttribute("error", "제목을 입력해주세요.");
        request.setAttribute("title", title);
        request.setAttribute("content", content);
        return "/board/write.jsp";
      }

      // 세션에서 사용자 정보 가져오기
      User user = (User) session.getAttribute("user");

      Board board = new Board();
      board.setTitle(title);
      board.setAuthor(user.getId().toString()); // 세션의 사용자 ID 사용
      board.setContent(content);

      BoardDAO dao = new BoardDAO();
      boolean insertResult = dao.insertBoard(board);

      if (insertResult) {
        // 게시글 등록 성공 시 첨부파일 처리
        try {
          processFileUploads(request, board.getId());
        } catch (Exception e) {
          System.out.println("파일 업로드 처리 중 오류: " + e.getMessage());
          e.printStackTrace();
        }

        // 목록으로 리다이렉트
        response.sendRedirect("front?command=boardList");
        return null; // 리다이렉트 시 null 반환
      } else {
        request.setAttribute("error", "게시글 등록에 실패했습니다.");
        return "/board/write.jsp";
      }

    } catch (Exception e) {
      request.setAttribute("error", "게시글 등록에 실패했습니다: " + e.getMessage());
      return "/board/write.jsp";
    }
  }

  // 파일 업로드 처리 메서드
  private void processFileUploads(HttpServletRequest request, Long boardId)
      throws IOException, javax.servlet.ServletException {
    System.out.println("=== BoardInsertCommand 파일 업로드 처리 시작 ===");
    System.out.println("게시글 ID: " + boardId);

    // multipart 요청인지 확인
    String contentType = request.getContentType();
    if (contentType == null || !contentType.startsWith("multipart/form-data")) {
      System.out.println("multipart 요청이 아님: " + contentType);
      return;
    }

    try {
      // 업로드 설정 가져오기
      UploadConfig config = UploadConfig.getInstance();
      System.out.println("업로드 설정 로드 완료");

      // 파일 파트들 가져오기
      Collection<Part> fileParts = request.getParts();
      System.out.println("파일 파트 개수: " + fileParts.size());

      for (Part part : fileParts) {
        if (part.getName().equals("files") && part.getSize() > 0) {
          String fileName = getSubmittedFileName(part);
          System.out.println("처리 중인 파일: " + fileName);

          if (fileName != null && !fileName.trim().isEmpty()) {
            // 파일 저장 및 DB 기록
            saveFile(part, fileName, boardId, config);
          }
        }
      }

      System.out.println("=== 파일 업로드 처리 완료 ===");

    } catch (Exception e) {
      System.out.println("파일 업로드 처리 중 오류: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // 파일 저장 메서드
  private void saveFile(Part part, String fileName, Long boardId, UploadConfig config) throws IOException {
    // 파일 확장자 확인
    String extension = getFileExtension(fileName).toLowerCase();
    boolean isImage = config.isImageFile(fileName);

    // 저장 경로 결정
    String uploadDir = isImage ? config.getImagesPath() : config.getFilesPath();
    String storedFileName = UUID.randomUUID().toString() + extension;
    String filePath = uploadDir + File.separator + storedFileName;

    System.out.println("파일 저장 경로: " + filePath);

    // 디렉토리 생성
    File dir = new File(uploadDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    // 파일 저장
    part.write(filePath);

    // DB에 파일 정보 저장
    FileUpload fileUpload = new FileUpload();
    fileUpload.setBoardId(boardId);
    fileUpload.setOriginalFilename(fileName);
    fileUpload.setStoredFilename(storedFileName);
    fileUpload.setFilePath(filePath);
    fileUpload.setFileSize(part.getSize());
    fileUpload.setContentType(part.getContentType());
    fileUpload.setFileType(part.getContentType());
    fileUpload.setUploadDate(new java.sql.Timestamp(System.currentTimeMillis()));

    FileUploadDAO fileDAO = new FileUploadDAO();
    boolean saveResult = fileDAO.insertFileUpload(fileUpload);

    System.out.println("파일 DB 저장 결과: " + saveResult);
  }

  // 파일명에서 확장자 추출
  private String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
  }

  // Part에서 파일명 가져오기 (Java 8 호환)
  private String getSubmittedFileName(Part part) {
    String contentDisp = part.getHeader("content-disposition");
    String[] tokens = contentDisp.split(";");
    for (String token : tokens) {
      if (token.trim().startsWith("filename")) {
        return token.substring(token.indexOf("=") + 2, token.length() - 1);
      }
    }
    return null;
  }
}
