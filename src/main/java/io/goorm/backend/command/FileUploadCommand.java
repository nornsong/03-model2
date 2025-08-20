package io.goorm.backend.command;

import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.config.UploadConfig;
import io.goorm.backend.util.FileUtils;
import io.goorm.backend.util.UploadValidator;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.sql.Timestamp;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 10, // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class FileUploadCommand implements Command {
  private FileUploadDAO fileUploadDAO;
  private UploadConfig uploadConfig;
  private UploadValidator uploadValidator;

  public FileUploadCommand() {
    this.fileUploadDAO = new FileUploadDAO();
    this.uploadConfig = UploadConfig.getInstance();
    this.uploadValidator = new UploadValidator();
  }

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 게시글 ID 파라미터
      String boardIdStr = request.getParameter("boardId");
      if (boardIdStr == null || boardIdStr.trim().isEmpty()) {
        request.setAttribute("error", "게시글 ID가 필요합니다.");
        return "board/write.jsp";
      }

      Long boardId = Long.parseLong(boardIdStr);

      // 파일 파트들 처리
      for (Part part : request.getParts()) {
        if (part.getName().equals("file") && part.getSize() > 0) {
          processFileUpload(part, boardId);
        }
      }

      request.setAttribute("message", "파일 업로드가 완료되었습니다.");
      return "board/write.jsp";

    } catch (Exception e) {
      e.printStackTrace();
      request.setAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
      return "board/write.jsp";
    }
  }

  private void processFileUpload(Part part, Long boardId) throws Exception {
    // 원본 파일명
    String originalFilename = FileUtils.getSubmittedFileName(part);
    if (originalFilename == null || originalFilename.trim().isEmpty()) {
      return;
    }

    // 파일 검증
    if (!uploadValidator.isValidFile(part, originalFilename)) {
      throw new Exception("허용되지 않는 파일입니다: " + originalFilename);
    }

    // 파일 타입 결정 및 저장 경로 설정
    String fileType = uploadConfig.isImageFile(originalFilename) ? "image" : "file";
    String uploadDir = "image".equals(fileType) ? uploadConfig.getImagesPath() : uploadConfig.getFilesPath();

    // 저장할 파일명 생성
    String storedFilename = FileUtils.generateStoredFilename(originalFilename);

    // 업로드 디렉토리에 파일 저장
    String filePath = uploadDir + "/" + storedFilename;
    if (!FileUtils.saveFile(part, filePath)) {
      throw new Exception("파일 저장에 실패했습니다.");
    }

    // FileUpload 객체 생성
    FileUpload fileUpload = new FileUpload();
    fileUpload.setBoardId(boardId);
    fileUpload.setOriginalFilename(originalFilename);
    fileUpload.setStoredFilename(storedFilename);
    fileUpload.setFilePath(filePath);
    fileUpload.setFileSize(part.getSize());
    fileUpload.setContentType(part.getContentType());
    fileUpload.setUploadDate(new Timestamp(System.currentTimeMillis()));
    fileUpload.setFileType(fileType);

    // 웹 URL 설정 (이미지인 경우)
    if ("image".equals(fileType)) {
      String webUrl = uploadConfig.getWebImagesPath() + "/" + storedFilename;
      fileUpload.setWebUrl(webUrl);
    }

    // 데이터베이스에 저장
    if (!fileUploadDAO.insertFileUpload(fileUpload)) {
      throw new Exception("데이터베이스 저장에 실패했습니다.");
    }
  }
}
