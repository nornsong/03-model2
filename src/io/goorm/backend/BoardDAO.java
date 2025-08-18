package io.goorm.backend;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.DataSource;

/**
 * Board Data Access Object
 * 2000년대 초반 Model 2 스타일 - 데이터베이스 풀 사용
 */
public class BoardDAO {

  /**
   * JNDI를 통해 데이터베이스 풀에서 커넥션 획득
   */
  private Connection getConnection() throws Exception {
    try {
      // JNDI를 통해 DataSource 획득 (데이터베이스 풀)
      Context initContext = new InitialContext();
      Context envContext = (Context) initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource) envContext.lookup("jdbc/BoardDB");

      return ds.getConnection();
    } catch (NamingException e) {
      // JNDI 실패 시 직접 연결 (폴백)
      Class.forName("org.h2.Driver");
      return DriverManager.getConnection("jdbc:h2:file:D:/devEnv/h2/data/goorm_db;AUTO_SERVER=TRUE", "sa", "");
    }
  }

  /**
   * 게시글 목록 조회
   */
  public List<Board> getBoardList() throws Exception {
    List<Board> boards = new ArrayList<>();
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = getConnection();
      String sql = "SELECT * FROM board ORDER BY created_at DESC";
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        Board board = new Board();
        board.setId(rs.getLong("id"));
        board.setTitle(rs.getString("title"));
        board.setContent(rs.getString("content"));
        board.setAuthor(rs.getString("author"));
        board.setCreatedAt(rs.getTimestamp("created_at"));
        boards.add(board);
      }
    } finally {
      closeResources(conn, pstmt, rs);
    }
    return boards;
  }

  /**
   * 게시글 작성
   */
  public int insertBoard(Board board) throws Exception {
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = getConnection();
      String sql = "INSERT INTO board (title, content, author, created_at) VALUES (?, ?, ?, NOW())";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, board.getTitle());
      pstmt.setString(2, board.getContent());
      pstmt.setString(3, board.getAuthor());

      return pstmt.executeUpdate();
    } finally {
      closeResources(conn, pstmt, null);
    }
  }

  /**
   * 게시글 상세 조회
   */
  public Board getBoardById(Long id) throws Exception {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = getConnection();
      String sql = "SELECT * FROM board WHERE id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, id);
      rs = pstmt.executeQuery();

      if (rs.next()) {
        Board board = new Board();
        board.setId(rs.getLong("id"));
        board.setTitle(rs.getString("title"));
        board.setContent(rs.getString("content"));
        board.setAuthor(rs.getString("author"));
        board.setCreatedAt(rs.getTimestamp("created_at"));
        return board;
      }
      return null;
    } finally {
      closeResources(conn, pstmt, rs);
    }
  }

  /**
   * 리소스 정리
   */
  private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
    try {
      if (rs != null)
        rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      if (stmt != null)
        stmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      if (conn != null)
        conn.close(); // 데이터베이스 풀로 반환
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
