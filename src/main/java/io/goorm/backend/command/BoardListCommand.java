package io.goorm.backend.command;

import io.goorm.backend.BoardDAO;
import io.goorm.backend.Board;
import io.goorm.backend.model.Pagination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class BoardListCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 검색 파라미터 처리
      String searchType = request.getParameter("searchType"); // title, content, author
      String searchKeyword = request.getParameter("searchKeyword");

      // 페이지 파라미터 처리
      int currentPage = 1;
      int pageSize = 10; // 페이지당 10개 게시글

      String pageParam = request.getParameter("page");
      if (pageParam != null && !pageParam.trim().isEmpty()) {
        try {
          currentPage = Integer.parseInt(pageParam);
          if (currentPage < 1)
            currentPage = 1;
        } catch (NumberFormatException e) {
          currentPage = 1;
        }
      }

      BoardDAO boardDAO = new BoardDAO();
      int totalCount;
      List<Board> boards;

      // 검색어가 있는 경우와 없는 경우 구분
      if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
        totalCount = boardDAO.getSearchBoardCount(searchType, searchKeyword);
        boards = boardDAO.searchBoardWithPagination(searchType, searchKeyword, currentPage, pageSize);
      } else {
        totalCount = boardDAO.getTotalBoardCount();
        boards = boardDAO.getBoardListWithPagination(currentPage, pageSize);
      }

      // 페이지네이션 객체 생성
      Pagination pagination = new Pagination(currentPage, pageSize, totalCount);

      // 요청 속성에 데이터 설정
      request.setAttribute("boards", boards);
      request.setAttribute("pagination", pagination);
      request.setAttribute("searchType", searchType);
      request.setAttribute("searchKeyword", searchKeyword);

      return "/board/list.jsp";

    } catch (Exception e) {
      e.printStackTrace();
      request.setAttribute("error", "게시글 목록을 불러오는 중 오류가 발생했습니다.");
      return "/board/list.jsp";
    }
  }
}
