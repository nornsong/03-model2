# Step01: 페이지네이션 + 검색 시스템 구현

## 🎯 목표

게시글 목록에 페이지네이션과 검색 기능을 추가하여 대용량 데이터를 효율적으로 표시하고 사용자가 원하는 게시글을 빠르게 찾을 수 있도록 합니다.

## ⚠️ 중요: 페이지네이션과 검색의 필요성

### 기존 시스템의 문제점

- **성능 저하**: 모든 게시글을 한 번에 로드하여 메모리 사용량 증가
- **사용자 경험**: 긴 목록에서 원하는 게시글 찾기 어려움
- **네트워크 부하**: 불필요한 데이터 전송으로 응답 시간 증가
- **검색 부재**: 특정 조건으로 게시글을 찾을 수 없음

### 페이지네이션 + 검색의 장점

- **성능 향상**: 필요한 페이지만 로드하여 메모리 효율성 증대
- **사용자 편의성**: 명확한 페이지 구분과 빠른 검색으로 원하는 게시글 접근
- **확장성**: 게시글 수가 증가해도 일정한 응답 시간 유지
- **정확성**: 제목, 내용, 작성자별로 정확한 검색 결과 제공

## 📚 이론 포인트 리마인드

### 1. 페이지네이션 원리

- **OFFSET 방식**: `LIMIT offset, size`로 특정 범위 데이터 조회
- **커서 방식**: 마지막 ID 기준으로 다음 페이지 조회 (더 효율적)
- **페이지 크기**: 일반적으로 10~20개 게시글을 한 페이지에 표시

### 2. 검색 시스템 원리

- **LIKE 검색**: `WHERE title LIKE '%keyword%'` 패턴 매칭
- **JOIN 검색**: 사용자 테이블과 JOIN하여 작성자명으로 검색
- **검색 타입**: 제목, 내용, 작성자별로 다른 검색 로직 적용

### 3. 페이지네이션 계산

- **전체 페이지 수**: `(전체 게시글 수 + 페이지 크기 - 1) / 페이지 크기`
- **시작 인덱스**: `(현재 페이지 - 1) * 페이지 크기`
- **이전/다음 페이지**: 현재 페이지 기준으로 계산

## 🚀 실습 단계별 진행

### 1단계: Pagination 모델 클래스 생성

#### Pagination.java 생성

```java
package io.goorm.backend.model;

import java.util.List;
import java.util.ArrayList;

/**
 * 페이지네이션 정보를 담는 모델 클래스
 */
public class Pagination {
    private int currentPage;        // 현재 페이지
    private int pageSize;           // 페이지당 게시글 수
    private int totalCount;         // 전체 게시글 수
    private int totalPages;         // 전체 페이지 수
    private int startPage;          // 시작 페이지 번호
    private int endPage;            // 끝 페이지 번호
    private boolean hasPrevious;    // 이전 페이지 존재 여부
    private boolean hasNext;        // 다음 페이지 존재 여부
    private List<Integer> pageNumbers; // 표시할 페이지 번호들

    public Pagination(int currentPage, int pageSize, int totalCount) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        calculatePagination();
    }

    /**
     * 페이지네이션 정보 계산
     */
    private void calculatePagination() {
        // 전체 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 현재 페이지가 전체 페이지를 초과하지 않도록 조정
        if (this.currentPage > this.totalPages) {
            this.currentPage = this.totalPages;
        }
        if (this.currentPage < 1) {
            this.currentPage = 1;
        }

        // 시작/끝 페이지 계산 (최대 5개 페이지 번호 표시)
        int maxPageNumbers = 5;
        int halfPageNumbers = maxPageNumbers / 2;

        this.startPage = Math.max(1, this.currentPage - halfPageNumbers);
        this.endPage = Math.min(this.totalPages, this.currentPage + halfPageNumbers);

        // 시작/끝 페이지 조정
        if (this.endPage - this.startPage + 1 < maxPageNumbers) {
            if (this.startPage == 1) {
                this.endPage = Math.min(maxPageNumbers, this.totalPages);
            } else {
                this.startPage = Math.max(1, this.endPage - maxPageNumbers + 1);
            }
        }

        // 이전/다음 페이지 존재 여부
        this.hasPrevious = this.currentPage > 1;
        this.hasNext = this.currentPage < this.totalPages;

        // 페이지 번호 리스트 생성
        this.pageNumbers = new ArrayList<>();
        for (int i = this.startPage; i <= this.endPage; i++) {
            this.pageNumbers.add(i);
        }
    }

    // Getter 메서드들
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public int getTotalCount() { return totalCount; }
    public int getTotalPages() { return totalPages; }
    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }
    public boolean isHasPrevious() { return hasPrevious; }
    public boolean isHasNext() { return hasNext; }
    public List<Integer> getPageNumbers() { return pageNumbers; }

    /**
     * 이전 페이지 번호 반환
     */
    public int getPreviousPage() {
        return Math.max(1, currentPage - 1);
    }

    /**
     * 다음 페이지 번호 반환
     */
    public int getNextPage() {
        return Math.min(totalPages, currentPage + 1);
    }

    /**
     * 데이터베이스 쿼리용 시작 인덱스 반환
     */
    public int getStartIndex() {
        return (currentPage - 1) * pageSize;
    }

    /**
     * 데이터베이스 쿼리용 끝 인덱스 반환
     */
    public int getEndIndex() {
        return Math.min(currentPage * pageSize, totalCount);
    }
}
```

### 2단계: BoardDAO에 페이지네이션과 검색 메서드 추가

#### BoardDAO.java 수정

```java
// 기존 BoardDAO에 추가할 메서드들

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
```

### 3단계: BoardListCommand 수정하여 페이지네이션과 검색 적용

#### BoardListCommand.java 수정

```java
// 기존 BoardListCommand의 execute 메서드 수정

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
                if (currentPage < 1) currentPage = 1;
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
```

### 4단계: list.jsp에 검색 기능과 페이지네이션 UI 추가

#### list.jsp 수정 (기존 파일에 추가)

```jsp
<!-- 글쓰기 버튼 아래에 검색 기능 추가 -->

<!-- 검색 기능 -->
<div class="mb-6 bg-white p-4 rounded-lg shadow-sm border">
    <form action="front" method="get" class="flex items-center space-x-4">
        <input type="hidden" name="command" value="boardList">

        <select name="searchType" class="border border-gray-300 rounded-md px-3 py-2 text-sm">
            <option value="title" ${searchType == 'title' ? 'selected' : ''}>제목</option>
            <option value="content" ${searchType == 'content' ? 'selected' : ''}>내용</option>
            <option value="author" ${searchType == 'author' ? 'selected' : ''}>작성자</option>
        </select>

        <input type="text" name="searchKeyword" value="${searchKeyword}"
               placeholder="검색어를 입력하세요"
               class="flex-1 border border-gray-300 rounded-md px-3 py-2 text-sm">

        <button type="submit" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium">
            검색
        </button>

        <c:if test="${not empty searchKeyword}">
            <a href="front?command=boardList" class="text-gray-500 hover:text-gray-700 text-sm">
                검색 초기화
            </a>
        </c:if>
    </form>
</div>

<!-- 게시글 목록 테이블 수정 -->
<tbody class="bg-white divide-y divide-gray-200">
    <c:choose>
        <c:when test="${empty boards}">
            <tr>
                <td colspan="4" class="px-6 py-4 text-center text-gray-500">
                    <c:choose>
                        <c:when test="${not empty searchKeyword}">
                            검색 결과가 없습니다.
                        </c:when>
                        <c:otherwise>
                            등록된 게시글이 없습니다.
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:when>
        <c:otherwise>
            <c:forEach var="board" items="${boards}" varStatus="status">
                <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        ${pagination.totalCount - (pagination.currentPage - 1) * pagination.pageSize - status.index}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <a href="front?command=boardView&id=${board.id}" class="text-blue-600 hover:text-blue-900">${board.title}</a>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${board.authorName}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${board.createdAt}</td>
                </tr>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</tbody>

<!-- 테이블 아래에 페이지네이션 UI 추가 -->
<!-- 페이지네이션 UI -->
<c:if test="${not empty pagination and pagination.totalPages > 1}">
    <div class="mt-6 flex items-center justify-between">
        <!-- 전체 게시글 수 표시 -->
        <div class="text-sm text-gray-700">
            총 <span class="font-medium">${pagination.totalCount}</span>개의 게시글
            (${pagination.currentPage} / ${pagination.totalPages} 페이지)
        </div>

        <!-- 페이지 네비게이션 -->
        <div class="flex items-center space-x-2">
            <!-- 이전 페이지 -->
            <c:if test="${pagination.hasPrevious}">
                <a href="front?command=boardList&page=${pagination.previousPage}&searchType=${searchType}&searchKeyword=${searchKeyword}"
                   class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
                    </svg>
                    이전
                </a>
            </c:if>

            <!-- 페이지 번호들 -->
            <c:forEach var="pageNum" items="${pagination.pageNumbers}">
                <c:choose>
                    <c:when test="${pageNum == pagination.currentPage}">
                        <span class="px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 border border-blue-300 rounded-md">
                            ${pageNum}
                        </span>
                    </c:when>
                    <c:otherwise>
                        <a href="front?command=boardList&page=${pageNum}&searchType=${searchType}&searchKeyword=${searchKeyword}"
                           class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                            ${pageNum}
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <!-- 다음 페이지 -->
            <c:if test="${pagination.hasNext}">
                <a href="front?command=boardList&page=${pagination.nextPage}&searchType=${searchType}&searchKeyword=${searchKeyword}"
                   class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                    다음
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                    </svg>
                </a>
            </c:if>
        </div>
    </div>
</c:if>
```

## 📝 완료 체크리스트

- [x] Pagination.java 모델 클래스 생성
- [x] BoardDAO에 페이지네이션과 검색 메서드 추가
- [x] BoardListCommand 수정하여 페이지네이션과 검색 적용
- [x] list.jsp에 검색 기능과 페이지네이션 UI 추가
- [x] 검색 기능과 페이지네이션 연동
- [x] 페이지네이션과 검색 테스트 완료

## ⚠️ 주의사항

### 1. 성능 최적화

- **인덱스 설정**: `ORDER BY` 컬럼에 인덱스 필요
- **쿼리 최적화**: `COUNT(*)` 쿼리 성능 고려
- **검색 최적화**: LIKE 검색 시 인덱스 활용 방안 고려

### 2. 사용자 경험

- **페이지 크기**: 너무 크면 로딩 시간 증가, 너무 작으면 페이지 전환 빈번
- **페이지 번호 표시**: 너무 많은 페이지 번호는 UI 복잡성 증가
- **검색 결과**: 검색 결과가 없을 때의 명확한 안내
- **URL 관리**: 페이지 정보와 검색 조건을 URL에 반영하여 새로고침 시 유지

### 3. 에러 처리

- **잘못된 페이지 번호**: 범위를 벗어난 페이지 요청 시 처리
- **검색 결과 없음**: 검색 결과가 없을 때의 UI 처리
- **데이터 변경**: 페이지 이동 중 데이터 변경 시 동기화

## 🎯 테스트 방법

### 1. 기본 페이지네이션 테스트

- 게시글 목록 페이지 접속
- 페이지 번호 클릭하여 페이지 이동 확인
- 이전/다음 페이지 버튼 동작 확인

### 2. 검색 기능 테스트

- 제목, 내용, 작성자별 검색 실행
- 검색 결과 확인
- 검색어가 없을 때 전체 목록 표시 확인

### 3. 검색 + 페이지네이션 테스트

- 검색어 입력 후 검색 실행
- 검색 결과에 대한 페이지네이션 동작 확인
- 페이지 이동 시 검색 조건 유지 확인

### 4. 경계값 테스트

- 첫 페이지에서 이전 버튼 비활성화 확인
- 마지막 페이지에서 다음 버튼 비활성화 확인
- 존재하지 않는 페이지 번호 요청 시 처리 확인

## 🔗 다음 단계

페이지네이션과 검색 시스템 구현 완료 후:

1. **step02**: 대용량 파일 업로드 성능 비교 시스템
2. **step03**: 고급 기능 구현 (필요시)

이제 효율적인 페이지네이션과 강력한 검색 기능으로 대용량 게시글 목록을 부드럽게 처리할 수 있습니다!
