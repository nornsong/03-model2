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

public class BoardUpdateCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      String idStr = request.getParameter("id");

      if (request.getMethod().equals("GET")) {
        // 수정 폼 표시
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

        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
          response.sendRedirect("front?command=login");
          return null;
        }

        // 권한 확인 (본인이 작성한 글만 수정 가능)
        User user = (User) session.getAttribute("user");
        if (!board.getAuthor().equals(user.getId().toString())) {
          request.setAttribute("error", "본인이 작성한 글만 수정할 수 있습니다.");
          return "/board/view.jsp";
        }

        request.setAttribute("board", board);
        return "/board/update.jsp";

      } else {
        // POST 요청 - 수정 처리
        request.setCharacterEncoding("UTF-8");

        // multipart 요청인지 확인
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/form-data");

        Long id = Long.parseLong(idStr);
        String title = null;
        String content = null;

        if (isMultipart) {
          // multipart 요청일 때는 Part API 사용
          try {
            Collection<Part> allParts = request.getParts();
            for (Part part : allParts) {
              if (part.getName().equals("title")) {
                title = getPartContent(part);
              } else if (part.getName().equals("content")) {
                content = getPartContent(part);
              }
            }
          } catch (Exception e) {
            System.out.println("Part API로 파라미터 읽기 실패: " + e.getMessage());
            e.printStackTrace();
          }
        } else {
          // 일반 요청일 때는 getParameter 사용
          title = request.getParameter("title");
          content = request.getParameter("content");
        }

        if (title == null || title.trim().isEmpty()) {
          request.setAttribute("error", "제목을 입력해주세요.");
          return "/board/update.jsp";
        }

        Board board = new Board();
        board.setId(id);
        board.setTitle(title);
        board.setContent(content);

        BoardDAO dao = new BoardDAO();
        boolean success = dao.updateBoard(board);

        if (success) {
          // 새 첨부파일 처리
          try {
            processFileUploads(request, id);
          } catch (Exception e) {
            System.out.println("파일 업로드 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
          }

          // 수정 후 상세보기로 이동
          response.sendRedirect("front?command=boardView&id=" + id);
        } else {
          request.setAttribute("error", "게시글 수정에 실패했습니다.");
          return "/board/update.jsp";
        }
        return null;
      }

    } catch (NumberFormatException e) {
      request.setAttribute("error", "잘못된 게시글 ID입니다.");
      return "/board/list.jsp";
    } catch (Exception e) {
      request.setAttribute("error", "게시글 수정에 실패했습니다: " + e.getMessage());
      return "/board/list.jsp";
    }
  }

  // 파일 업로드 처리 메서드
  private void processFileUploads(HttpServletRequest request, Long boardId)
      throws IOException, javax.servlet.ServletException {
    System.out.println("=== BoardUpdateCommand 파일 업로드 처리 시작 ===");
    System.out.println("게시글 ID: " + boardId);

    // multipart 요청인지 확인
    String contentType = request.getContentType();
    System.out.println("파일 업로드 처리 - Content-Type: " + contentType);

    if (contentType == null || !contentType.startsWith("multipart/form-data")) {
      System.out.println("multipart 요청이 아님: " + contentType);
      return;
    }

    try {
      // 업로드 설정 가져오기
      UploadConfig config = UploadConfig.getInstance();
      System.out.println("업로드 설정 로드 완료");

      // 모든 Part 가져오기
      Collection<Part> allParts = request.getParts();
      System.out.println("전체 Part 개수: " + allParts.size());

      // 파일 파트들만 처리
      int fileCount = 0;
      for (Part part : allParts) {
        if (part.getName().equals("files") && part.getSize() > 0) {
          fileCount++;
          String fileName = getSubmittedFileName(part);
          System.out.println("처리 중인 파일 " + fileCount + ": " + fileName);

          if (fileName != null && !fileName.trim().isEmpty()) {
            // 파일 저장 및 DB 기록
            saveFile(part, fileName, boardId, config);
          }
        }
      }

      System.out.println("총 처리된 파일 개수: " + fileCount);
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

  // Part에서 텍스트 내용 읽기
  private String getPartContent(Part part) throws IOException {
    try (java.io.BufferedReader reader = new java.io.BufferedReader(
        new java.io.InputStreamReader(part.getInputStream(), "UTF-8"))) {
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }
      return content.toString();
    }
  }
}
