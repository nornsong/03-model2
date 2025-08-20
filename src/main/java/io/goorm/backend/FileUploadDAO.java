package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import io.goorm.backend.config.DatabaseConfig;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

public class FileUploadDAO {
  private JdbcTemplate jdbcTemplate;

  public FileUploadDAO() {
    this.jdbcTemplate = new JdbcTemplate(DatabaseConfig.getDataSource());
  }

  // 파일 업로드 정보 저장
  public boolean insertFileUpload(FileUpload fileUpload) {
    String sql = "INSERT INTO file_upload (board_id, original_filename, stored_filename, " +
        "file_path, file_size, content_type, upload_date, file_type, web_url) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try {
      int result = jdbcTemplate.update(sql,
          fileUpload.getBoardId(),
          fileUpload.getOriginalFilename(),
          fileUpload.getStoredFilename(),
          fileUpload.getFilePath(),
          fileUpload.getFileSize(),
          fileUpload.getContentType(),
          fileUpload.getUploadDate(),
          fileUpload.getFileType(),
          fileUpload.getWebUrl());
      return result > 0;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // 게시글 ID로 첨부파일 목록 조회
  public List<FileUpload> getFilesByBoardId(Long boardId) {
    String sql = "SELECT * FROM file_upload WHERE board_id = ? ORDER BY upload_date DESC";

    try {
      return jdbcTemplate.query(sql, fileUploadRowMapper, boardId);
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  // 파일 ID로 특정 파일 조회
  public FileUpload getFileById(Long fileId) {
    String sql = "SELECT * FROM file_upload WHERE id = ?";

    try {
      List<FileUpload> results = jdbcTemplate.query(sql, fileUploadRowMapper, fileId);
      FileUpload file = results.isEmpty() ? null : results.get(0);

      // 디버깅을 위한 로깅
      if (file != null) {
        System.out.println("=== FileUploadDAO 디버깅 ===");
        System.out.println("파일 ID: " + fileId);
        System.out.println("파일 경로: " + file.getFilePath());
        System.out.println("파일 타입: " + file.getFileType());
        System.out.println("저장된 파일명: " + file.getStoredFilename());
        System.out.println("===========================");
      } else {
        System.out.println("파일을 찾을 수 없음: " + fileId);
      }

      return file;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  // 파일 삭제
  public boolean deleteFile(Long fileId) {
    String sql = "DELETE FROM file_upload WHERE id = ?";

    try {
      int result = jdbcTemplate.update(sql, fileId);
      return result > 0;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // RowMapper 정의
  private RowMapper<FileUpload> fileUploadRowMapper = (rs, rowNum) -> {
    FileUpload fileUpload = new FileUpload();
    fileUpload.setId(rs.getLong("id"));
    fileUpload.setBoardId(rs.getLong("board_id"));
    fileUpload.setOriginalFilename(rs.getString("original_filename"));
    fileUpload.setStoredFilename(rs.getString("stored_filename"));
    fileUpload.setFilePath(rs.getString("file_path"));
    fileUpload.setFileSize(rs.getLong("file_size"));
    fileUpload.setContentType(rs.getString("content_type"));
    fileUpload.setUploadDate(rs.getTimestamp("upload_date"));
    fileUpload.setFileType(rs.getString("file_type"));
    fileUpload.setWebUrl(rs.getString("web_url"));
    return fileUpload;
  };
}
