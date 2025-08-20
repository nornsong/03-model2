package io.goorm.backend.util;

import io.goorm.backend.config.UploadConfig;
import javax.servlet.http.Part;

public class UploadValidator {
  private UploadConfig uploadConfig;

  public UploadValidator() {
    this.uploadConfig = UploadConfig.getInstance();
  }

  // 파일 확장자 검증
  public boolean isValidFileExtension(String filename) {
    String extension = FileUtils.getFileExtension(filename).toLowerCase();
    return uploadConfig.getAllowedFileExtensions().contains(extension) ||
        uploadConfig.getAllowedImageExtensions().contains(extension);
  }

  // 파일 크기 검증
  public boolean isValidFileSize(long fileSize, String filename) {
    if (uploadConfig.isImageFile(filename)) {
      return fileSize <= uploadConfig.getMaxImageSize();
    } else {
      return fileSize <= uploadConfig.getMaxFileSize();
    }
  }

  // MIME 타입 검증
  public boolean isValidMimeType(String contentType, String filename) {
    if (uploadConfig.isImageFile(filename)) {
      return contentType.startsWith("image/");
    } else {
      return !contentType.startsWith("image/");
    }
  }

  // 전체 파일 검증
  public boolean isValidFile(Part part, String filename) {
    return isValidFileExtension(filename) &&
        isValidFileSize(part.getSize(), filename) &&
        isValidMimeType(part.getContentType(), filename);
  }
}
