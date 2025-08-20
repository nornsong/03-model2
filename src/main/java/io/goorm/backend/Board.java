package io.goorm.backend;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

/**
 * Board Value Object
 * 2000년대 초반 J2EE 패턴 스타일
 */
public class Board {
  private Long id;
  private String title;
  private String content;
  private String author; // 사용자 ID (문자열)
  private String authorName; // 사용자 이름 (JOIN으로 가져옴)
  private Timestamp createdAt;
  private List<FileUpload> attachments;

  // 기본 생성자
  public Board() {
  }

  // 매개변수 생성자
  public Board(String title, String content, String author) {
    this.title = title;
    this.content = content;
    this.author = author;
    this.attachments = new ArrayList<>();
  }

  // Getter 메서드들
  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public String getAuthor() {
    return author;
  }

  public String getAuthorName() {
    return authorName;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public List<FileUpload> getAttachments() {
    return attachments != null ? attachments : new ArrayList<>();
  }

  // Setter 메서드들
  public void setId(Long id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public void setAttachments(List<FileUpload> attachments) {
    this.attachments = attachments;
  }

  // 첨부파일 추가 메서드
  public void addAttachment(FileUpload attachment) {
    if (this.attachments == null) {
      this.attachments = new ArrayList<>();
    }
    this.attachments.add(attachment);
  }

  // toString 메서드
  @Override
  public String toString() {
    return "Board{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", content='" + content + '\'' +
        ", author='" + author + '\'' +
        ", createdAt=" + createdAt +
        ", attachments=" + (attachments != null ? attachments.size() : 0) + "개" +
        '}';
  }
}
