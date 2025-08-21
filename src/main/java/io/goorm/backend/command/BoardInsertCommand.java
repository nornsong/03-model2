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
      System.out.println("=== BoardInsertCommand 실행 시작 ===");
      System.out.println("요청 메서드: " + request.getMethod());
      System.out.println("Content-Type: " + request.getContentType());
      System.out.println("Character Encoding: " + request.getCharacterEncoding());

      // 로그인 확인
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        System.out.println("로그인되지 않음 - 로그인 페이지로 리다이렉트");
        response.sendRedirect("front?command=login");
        return null;
      }

      // POST 요청 처리
      request.setCharacterEncoding("UTF-8");

      // multipart 요청인지 확인
      String contentType = request.getContentType();
      boolean isMultipart = contentType != null && contentType.startsWith("multipart/form-data");
      System.out.println("Multipart 요청 여부: " + isMultipart);

      // 파라미터 읽기 시도
      String title = null;
      String content = null;

      if (isMultipart) {
        // multipart 요청일 때는 Part API 사용
        System.out.println("Multipart 요청 - Part API로 파라미터 읽기");
        try {
          Collection<Part> allParts = request.getParts();
          System.out.println("getParts() 호출 결과 - Part 개수: " + allParts.size());

          if (allParts.isEmpty()) {
            System.out.println("⚠️ 경고: getParts()가 빈 컬렉션을 반환했습니다!");
            System.out.println("이는 보통 @MultipartConfig 어노테이션이 없거나 설정이 잘못되었을 때 발생합니다.");
          }

          for (Part part : allParts) {
            System.out.println("--- Part 처리 중 ---");
            System.out.println("Part 이름: [" + part.getName() + "]");
            System.out.println("Part 크기: " + part.getSize());
            System.out.println("Part Content-Type: [" + part.getContentType() + "]");

            if (part.getName().equals("title")) {
              title = getPartContent(part);
              System.out.println("✅ Part에서 title 읽기 성공: [" + title + "]");
            } else if (part.getName().equals("content")) {
              content = getPartContent(part);
              System.out.println("✅ Part에서 content 읽기 성공: [" + content + "]");
            } else {
              System.out.println("❓ 다른 Part: " + part.getName());
            }
            System.out.println("---------------");
          }
        } catch (Exception e) {
          System.out.println("❌ Part API로 파라미터 읽기 실패: " + e.getMessage());
          e.printStackTrace();
        }
      } else {
        // 일반 요청일 때는 getParameter 사용
        System.out.println("일반 요청 - getParameter로 파라미터 읽기");
        title = request.getParameter("title");
        content = request.getParameter("content");
      }

      System.out.println("=== 파라미터 읽기 결과 ===");
      System.out.println("제목: [" + title + "]");
      System.out.println("내용: [" + content + "]");
      System.out.println("제목 null 여부: " + (title == null));
      System.out.println("내용 null 여부: " + (content == null));
      System.out.println("제목 길이: " + (title != null ? title.length() : "N/A"));
      System.out.println("내용 길이: " + (content != null ? content.length() : "N/A"));
      System.out.println("==========================");

      if (title == null || title.trim().isEmpty()) {
        System.out.println("제목 벨리데이션 실패 - 제목이 비어있음");
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
      Long boardId = dao.insertBoard(board);

      if (boardId != null) {
        // 게시글 등록 성공 시 첨부파일 처리
        try {
          processFileUploads(request, boardId);
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

      // Part별 상세 정보 로깅
      for (Part part : allParts) {
        System.out.println("--- Part 정보 ---");
        System.out.println("Part 이름: " + part.getName());
        System.out.println("Part 크기: " + part.getSize());
        System.out.println("Part Content-Type: " + part.getContentType());
        System.out.println("Part 헤더들:");
        for (String headerName : part.getHeaderNames()) {
          System.out.println("  " + headerName + ": " + part.getHeader(headerName));
        }
        System.out.println("---------------");
      }

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
