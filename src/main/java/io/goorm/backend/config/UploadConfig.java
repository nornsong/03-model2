package io.goorm.backend.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class UploadConfig {
  private static UploadConfig instance;
  private Properties properties;

  // 업로드 경로
  private String rootPath;
  private String filesPath;
  private String imagesPath;
  private String webImagesPath;

  // 허용 확장자
  private List<String> allowedFileExtensions;
  private List<String> allowedImageExtensions;

  // 파일 크기 제한
  private long maxFileSize;
  private long maxImageSize;
  private int bufferSize;

  private UploadConfig() {
    loadProperties();
    createDirectories();
  }

  public static UploadConfig getInstance() {
    if (instance == null) {
      instance = new UploadConfig();
    }
    return instance;
  }

  private void loadProperties() {
    properties = new Properties();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream("upload.properties")) {
      if (input == null) {
        throw new RuntimeException("upload.properties 파일을 찾을 수 없습니다.");
      }
      properties.load(input);

      // 경로 설정
      rootPath = properties.getProperty("upload.root.path");
      filesPath = properties.getProperty("upload.files.path");
      imagesPath = properties.getProperty("upload.images.path");
      webImagesPath = properties.getProperty("upload.web.images.path");

      // 확장자 설정
      String fileExts = properties.getProperty("upload.allowed.files");
      String imageExts = properties.getProperty("upload.allowed.images");

      allowedFileExtensions = Arrays.asList(fileExts.split(","));
      allowedImageExtensions = Arrays.asList(imageExts.split(","));

      // 크기 제한 설정
      maxFileSize = Long.parseLong(properties.getProperty("upload.max.file.size"));
      maxImageSize = Long.parseLong(properties.getProperty("upload.max.image.size"));
      bufferSize = Integer.parseInt(properties.getProperty("upload.buffer.size"));

    } catch (IOException e) {
      throw new RuntimeException("업로드 설정 파일 로드 실패", e);
    }
  }

  private void createDirectories() {
    try {
      Files.createDirectories(Paths.get(rootPath));
      Files.createDirectories(Paths.get(filesPath));
      Files.createDirectories(Paths.get(imagesPath));
    } catch (IOException e) {
      throw new RuntimeException("업로드 디렉토리 생성 실패", e);
    }
  }

  // Getter 메서드들
  public String getRootPath() {
    return rootPath;
  }

  public String getFilesPath() {
    return filesPath;
  }

  public String getImagesPath() {
    return imagesPath;
  }

  public String getWebImagesPath() {
    return webImagesPath;
  }

  public List<String> getAllowedFileExtensions() {
    return allowedFileExtensions;
  }

  public List<String> getAllowedImageExtensions() {
    return allowedImageExtensions;
  }

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public long getMaxImageSize() {
    return maxImageSize;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  // 파일 타입 판별
  public boolean isImageFile(String filename) {
    String extension = getFileExtension(filename).toLowerCase();
    return allowedImageExtensions.contains(extension);
  }

  public boolean isFileFile(String filename) {
    String extension = getFileExtension(filename).toLowerCase();
    return allowedFileExtensions.contains(extension);
  }

  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
  }
}
