package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import io.goorm.backend.config.DatabaseConfig;

import java.util.List;
import java.util.ArrayList;

/**
 * Board Data Access Object
 * Spring JdbcTemplate 사용으로 개선된 버전
 */
public class BoardDAO {

  private JdbcTemplate jdbcTemplate;

  public BoardDAO() {
    this.jdbcTemplate = new JdbcTemplate(DatabaseConfig.getDataSource());
  }

  // RowMapper 정의
  private RowMapper<Board> boardRowMapper = (rs, rowNum) -> {
    Board board = new Board();
    board.setId(rs.getLong("id"));
    board.setTitle(rs.getString("title"));
    board.setContent(rs.getString("content"));
    board.setAuthor(rs.getString("author"));
    board.setCreatedAt(rs.getTimestamp("created_at"));
    return board;
  };

  /**
   * 게시글 목록 조회
   */
  public List<Board> getBoardList() {
    String sql = "SELECT * FROM board ORDER BY created_at DESC";
    return jdbcTemplate.query(sql, boardRowMapper);
  }

  /**
   * 게시글 상세 조회
   */
  public Board getBoardById(Long id) {
    String sql = "SELECT * FROM board WHERE id = ?";
    try {
      Board board = jdbcTemplate.queryForObject(sql, boardRowMapper, id);
      if (board != null) {
        // 첨부파일 정보도 함께 조회
        loadAttachments(board);
      }
      return board;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 게시글 작성
   */
  public boolean insertBoard(Board board) {
    String sql = "INSERT INTO board (title, content, author, created_at) VALUES (?, ?, ?, NOW())";
    int result = jdbcTemplate.update(sql, board.getTitle(), board.getContent(), board.getAuthor());
    return result > 0;
  }

  /**
   * 게시글 수정
   */
  public boolean updateBoard(Board board) {
    String sql = "UPDATE board SET title = ?, content = ? WHERE id = ?";
    int result = jdbcTemplate.update(sql, board.getTitle(), board.getContent(), board.getId());
    return result > 0;
  }

  /**
   * 게시글 삭제
   */
  public boolean deleteBoard(Long id) {
    String sql = "DELETE FROM board WHERE id = ?";
    int result = jdbcTemplate.update(sql, id);
    return result > 0;
  }

  /**
   * 제목으로 게시글 검색
   */
  public List<Board> searchByTitle(String keyword) {
    String sql = "SELECT * FROM board WHERE title LIKE ? ORDER BY created_at DESC";
    String searchKeyword = "%" + keyword + "%";
    return jdbcTemplate.query(sql, boardRowMapper, searchKeyword);
  }

  /**
   * 게시글의 첨부파일 정보 로드
   */
  private void loadAttachments(Board board) {
    try {
      FileUploadDAO fileUploadDAO = new FileUploadDAO();
      List<FileUpload> attachments = fileUploadDAO.getFilesByBoardId(board.getId());
      board.setAttachments(attachments);
    } catch (Exception e) {
      e.printStackTrace();
      board.setAttachments(new ArrayList<>());
    }
  }
}
