package io.goorm.backend;

import java.sql.Timestamp;
import java.util.Objects;

public class FileUpload {
  private Long id;
  private Long boardId;
  private String originalFilename;
  private String storedFilename;
  private String filePath;
  private Long fileSize;
  private String contentType;
  private Timestamp uploadDate;
  private String fileType; // "file" 또는 "image"
  private String webUrl; // 웹에서 접근 가능한 URL (이미지인 경우)

  // 기본 생성자
  public FileUpload() {
  }

  // 전체 필드 생성자
  public FileUpload(Long id, Long boardId, String originalFilename,
      String storedFilename, String filePath, Long fileSize,
      String contentType, Timestamp uploadDate, String fileType, String webUrl) {
    this.id = id;
    this.boardId = boardId;
    this.originalFilename = originalFilename;
    this.storedFilename = storedFilename;
    this.filePath = filePath;
    this.fileSize = fileSize;
    this.contentType = contentType;
    this.uploadDate = uploadDate;
    this.fileType = fileType;
    this.webUrl = webUrl;
  }

  // Getter 메서드들
  public Long getId() {
    return id;
  }

  public Long getBoardId() {
    return boardId;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public String getStoredFilename() {
    return storedFilename;
  }

  public String getFilePath() {
    return filePath;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public String getContentType() {
    return contentType;
  }

  public Timestamp getUploadDate() {
    return uploadDate;
  }

  public String getFileType() {
    return fileType;
  }

  public String getWebUrl() {
    return webUrl;
  }

  // Setter 메서드들
  public void setId(Long id) {
    this.id = id;
  }

  public void setBoardId(Long boardId) {
    this.boardId = boardId;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  public void setStoredFilename(String storedFilename) {
    this.storedFilename = storedFilename;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setUploadDate(Timestamp uploadDate) {
    this.uploadDate = uploadDate;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public void setWebUrl(String webUrl) {
    this.webUrl = webUrl;
  }

  // 파일 타입 확인 메서드
  public boolean isImage() {
    return "image".equals(fileType);
  }

  public boolean isFile() {
    return "file".equals(fileType);
  }

  @Override
  public String toString() {
    return "FileUpload{" +
        "id=" + id +
        ", boardId=" + boardId +
        ", originalFilename='" + originalFilename + '\'' +
        ", storedFilename='" + storedFilename + '\'' +
        ", filePath='" + filePath + '\'' +
        ", fileSize=" + fileSize +
        ", contentType='" + contentType + '\'' +
        ", uploadDate=" + uploadDate +
        ", fileType='" + fileType + '\'' +
        ", webUrl='" + webUrl + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FileUpload that = (FileUpload) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
