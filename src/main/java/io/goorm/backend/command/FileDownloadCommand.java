package io.goorm.backend.command;

import io.goorm.backend.FileUpload;
import io.goorm.backend.FileUploadDAO;
import io.goorm.backend.config.UploadConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class FileDownloadCommand implements Command {
  private FileUploadDAO fileUploadDAO;

  public FileDownloadCommand() {
    this.fileUploadDAO = new FileUploadDAO();
  }

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 파일 ID 파라미터
      String fileIdStr = request.getParameter("id");
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

      // 파일 경로 검증
      Path filePath = validateAndGetFilePath(fileUpload.getFilePath());
      if (!Files.exists(filePath)) {
        request.setAttribute("error", "물리적 파일이 존재하지 않습니다.");
        return "board/view.jsp";
      }

      // 다운로드 헤더 설정
      setDownloadHeaders(response, fileUpload);

      // 파일 스트림 전송
      streamFile(response, filePath);

    } catch (Exception e) {
      e.printStackTrace();
      request.setAttribute("error", "파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
      return "board/view.jsp";
    }

    // 파일 다운로드는 직접 스트림으로 처리하므로 null 반환
    return null;
  }

  private Path validateAndGetFilePath(String filePath) {
    Path path = Paths.get(filePath);

    // 업로드 루트 경로 검증 (보안) - properties에서 읽어오기
    String uploadRoot = UploadConfig.getInstance().getRootPath();

    // 디버깅을 위한 로깅
    System.out.println("=== FileDownloadCommand 디버깅 ===");
    System.out.println("요청된 파일 경로: " + filePath);
    System.out.println("업로드 루트 경로: " + uploadRoot);
    System.out.println("정규화된 경로: " + path.normalize());
    System.out.println("루트 경로 Path: " + Paths.get(uploadRoot));
    System.out.println("경로가 루트로 시작하는가: " + path.normalize().startsWith(Paths.get(uploadRoot)));
    System.out.println("================================");

    if (!path.normalize().startsWith(Paths.get(uploadRoot))) {
      throw new SecurityException("허용되지 않는 파일 경로입니다.");
    }

    return path;
  }

  private void setDownloadHeaders(HttpServletResponse response, FileUpload fileUpload) {
    response.setContentType("application/octet-stream");
    response.setContentLengthLong(fileUpload.getFileSize());

    // 한글 파일명 처리
    String encodedFilename = encodeFilename(fileUpload.getOriginalFilename());
    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
    response.setHeader("Cache-Control", "no-cache");
  }

  private void streamFile(HttpServletResponse response, Path filePath) throws IOException {
    try (InputStream inputStream = Files.newInputStream(filePath);
        OutputStream outputStream = response.getOutputStream()) {

      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      outputStream.flush();
    }
  }

  private String encodeFilename(String filename) {
    try {
      return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
          .replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      return filename;
    }
  }
}
