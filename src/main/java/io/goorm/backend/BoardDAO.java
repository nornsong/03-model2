package io.goorm.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import io.goorm.backend.config.DatabaseConfig;

import java.util.List;
import java.util.ArrayList;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.PreparedStatement;

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
    board.setAuthorName(rs.getString("author_name")); // JOIN으로 가져온 사용자 이름
    board.setCreatedAt(rs.getTimestamp("created_at"));
    return board;
  };

  /**
   * 게시글 목록 조회
   */
  public List<Board> getBoardList() {
    String sql = "SELECT b.*, u.name as author_name FROM board b " +
        "LEFT JOIN users u ON b.author = u.id " +
        "ORDER BY b.created_at DESC";
    return jdbcTemplate.query(sql, boardRowMapper);
  }

  /**
   * 게시글 상세 조회
   */
  public Board getBoardById(Long id) {
    String sql = "SELECT b.*, u.name as author_name FROM board b " +
        "LEFT JOIN users u ON b.author = u.id " +
        "WHERE b.id = ?";
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
  public Long insertBoard(Board board) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    String sql = "INSERT INTO board (title, content, author) VALUES (?, ?, ?)";

    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(sql, new String[] { "id" });
      ps.setString(1, board.getTitle());
      ps.setString(2, board.getContent());
      ps.setString(3, board.getAuthor());
      return ps;
    }, keyHolder);

    Long generatedId = keyHolder.getKey().longValue();
    board.setId(generatedId);
    System.out.println("생성된 게시글 ID: " + board.getId());

    return generatedId; // 생성된 board_id 반환
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
   * 전체 게시글 수 조회
   */
  public int getTotalBoardCount() {
    String sql = "SELECT COUNT(*) FROM board";
    try {
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
      return count != null ? count : 0;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * 페이지네이션을 적용한 게시글 목록 조회
   */
  public List<Board> getBoardListWithPagination(int page, int pageSize) {
    String sql = "SELECT b.*, u.name as author_name FROM board b " +
        "LEFT JOIN users u ON b.author = u.id " +
        "ORDER BY b.created_at DESC " +
        "LIMIT ? OFFSET ?";

    int offset = (page - 1) * pageSize;
    return jdbcTemplate.query(sql, boardRowMapper, pageSize, offset);
  }

  /**
   * 검색 결과 게시글 수 조회
   */
  public int getSearchBoardCount(String searchType, String searchKeyword) {
    String sql = "SELECT COUNT(*) FROM board WHERE ";

    switch (searchType) {
      case "title":
        sql += "title LIKE ?";
        break;
      case "content":
        sql += "content LIKE ?";
        break;
      case "author":
        sql += "author IN (SELECT id FROM users WHERE name LIKE ?)";
        break;
      default:
        sql += "title LIKE ?";
    }

    try {
      String searchPattern = "%" + searchKeyword + "%";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class, searchPattern);
      return count != null ? count : 0;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * 검색 결과에 페이지네이션 적용
   */
  public List<Board> searchBoardWithPagination(String searchType, String searchKeyword, int page, int pageSize) {
    String sql = "SELECT b.*, u.name as author_name FROM board b " +
        "LEFT JOIN users u ON b.author = u.id " +
        "WHERE ";

    switch (searchType) {
      case "title":
        sql += "b.title LIKE ? ";
        break;
      case "content":
        sql += "b.content LIKE ? ";
        break;
      case "author":
        sql += "b.author IN (SELECT id FROM users WHERE name LIKE ?) ";
        break;
      default:
        sql += "b.title LIKE ? ";
    }

    sql += "ORDER BY b.created_at DESC LIMIT ? OFFSET ?";

    int offset = (page - 1) * pageSize;
    String searchPattern = "%" + searchKeyword + "%";

    return jdbcTemplate.query(sql, boardRowMapper, searchPattern, pageSize, offset);
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
