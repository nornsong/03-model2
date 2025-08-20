package io.goorm.backend.util;

import javax.servlet.http.Part;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtils {

  // Part에서 파일명 추출
  public static String getSubmittedFileName(Part part) {
    String contentDisp = part.getHeader("content-disposition");
    String[] tokens = contentDisp.split(";");
    for (String token : tokens) {
      if (token.trim().startsWith("filename")) {
        return token.substring(token.indexOf("=") + 2, token.length() - 1);
      }
    }
    return null;
  }

  // 파일 확장자 추출
  public static String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
  }

  // UUID 기반 파일명 생성
  public static String generateStoredFilename(String originalFilename) {
    String extension = getFileExtension(originalFilename);
    return UUID.randomUUID().toString() + extension;
  }

  // 파일 저장
  public static boolean saveFile(Part part, String filePath) {
    try {
      Path path = Paths.get(filePath);
      Files.copy(part.getInputStream(), path);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
