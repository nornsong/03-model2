# Step04: 간단한 검색 기능 구현

## 🎯 목표

게시판에서 제목, 내용, 작성자를 기준으로 간단한 검색 기능을 구현하여 사용자가 원하는 게시글을 쉽게 찾을 수 있도록 합니다.

## ⚠️ 중요: 검색 기능의 필요성

### 기존 시스템의 문제점

- **게시글 증가**: 게시글이 많아질수록 원하는 내용을 찾기 어려움
- **사용자 경험**: 스크롤만으로는 효율적인 정보 검색 불가
- **콘텐츠 활용도**: 좋은 게시글이 묻혀서 재사용되지 않음

### 검색 기능의 장점

- **빠른 정보 접근**: 키워드로 원하는 내용을 즉시 찾기 가능
- **사용자 만족도**: 원하는 정보를 쉽게 찾을 수 있어 만족도 향상
- **콘텐츠 재발견**: 과거 게시글의 재활용 가능성 증대

## 📚 이론 포인트 리마인드

### 1. 검색 방식의 종류

- **정확 일치**: 입력한 키워드와 정확히 일치하는 경우만 검색
- **부분 일치**: 키워드가 포함된 모든 경우 검색 (LIKE 연산자)
- **전체 텍스트 검색**: 전문 검색 (MySQL FULLTEXT, PostgreSQL tsvector)

### 2. SQL 검색 쿼리

- **LIKE 연산자**: `WHERE title LIKE '%키워드%'`
- **OR 조건**: 여러 필드에서 동시 검색
- **정렬**: 검색 결과를 관련성이나 날짜순으로 정렬

### 3. 검색 성능 고려사항

- **인덱스**: 검색 필드에 적절한 인덱스 설정
- **검색 범위**: 너무 광범위한 검색은 성능 저하
- **결과 제한**: 검색 결과 수 제한으로 응답 속도 향상

## 🚀 실습 단계별 진행

### 1단계: BoardDAO에 검색 메서드 추가

#### BoardDAO.java 수정

```java
// 기존 BoardDAO 클래스에 다음 메서드들 추가

/**
 * 검색 조건에 맞는 게시글 수 조회
 */
public int getSearchBoardCount(String searchType, String searchKeyword) {
    String sql = "";

    switch (searchType) {
        case "title":
            sql = "SELECT COUNT(*) FROM board WHERE title LIKE ?";
            break;
        case "content":
            sql = "SELECT COUNT(*) FROM board WHERE content LIKE ?";
            break;
        case "author":
            sql = "SELECT COUNT(*) FROM board b JOIN user u ON b.author_id = u.id WHERE u.name LIKE ?";
            break;
        case "all":
            sql = "SELECT COUNT(*) FROM board b JOIN user u ON b.author_id = u.id " +
                  "WHERE b.title LIKE ? OR b.content LIKE ? OR u.name LIKE ?";
            break;
        default:
            sql = "SELECT COUNT(*) FROM board";
    }

    try {
        if ("all".equals(searchType)) {
            String keyword = "%" + searchKeyword + "%";
            return jdbcTemplate.queryForObject(sql, Integer.class, keyword, keyword, keyword);
        } else if (!searchType.isEmpty()) {
            String keyword = "%" + searchKeyword + "%";
            return jdbcTemplate.queryForObject(sql, Integer.class, keyword);
        } else {
            return jdbcTemplate.queryForObject(sql, Integer.class);
        }
    } catch (Exception e) {
        e.printStackTrace();
        return 0;
    }
}

/**
 * 검색 조건에 맞는 게시글 목록 조회 (페이징 포함)
 */
public List<Board> searchBoardWithPagination(String searchType, String searchKeyword,
                                           int startRow, int pageSize) {
    String sql = "";
    Object[] params;

    switch (searchType) {
        case "title":
            sql = "SELECT b.*, u.name as author_name FROM board b " +
                  "JOIN user u ON b.author_id = u.id " +
                  "WHERE b.title LIKE ? ORDER BY b.id DESC LIMIT ? OFFSET ?";
            params = new Object[]{"%" + searchKeyword + "%", pageSize, startRow};
            break;
        case "content":
            sql = "SELECT b.*, u.name as author_name FROM board b " +
                  "JOIN user u ON b.author_id = u.id " +
                  "WHERE b.content LIKE ? ORDER BY b.id DESC LIMIT ? OFFSET ?";
            params = new Object[]{"%" + searchKeyword + "%", pageSize, startRow};
            break;
        case "author":
            sql = "SELECT b.*, u.name as author_name FROM board b " +
                  "JOIN user u ON b.author_id = u.id " +
                  "WHERE u.name LIKE ? ORDER BY b.id DESC LIMIT ? OFFSET ?";
            params = new Object[]{"%" + searchKeyword + "%", pageSize, startRow};
            break;
        case "all":
            sql = "SELECT b.*, u.name as author_name FROM board b " +
                  "JOIN user u ON b.author_id = u.id " +
                  "WHERE b.title LIKE ? OR b.content LIKE ? OR u.name LIKE ? " +
                  "ORDER BY b.id DESC LIMIT ? OFFSET ?";
            params = new Object[]{"%" + searchKeyword + "%", "%" + searchKeyword + "%",
                                "%" + searchKeyword + "%", pageSize, startRow};
            break;
        default:
            sql = "SELECT b.*, u.name as author_name FROM board b " +
                  "JOIN user u ON b.author_id = u.id " +
                  "ORDER BY b.id DESC LIMIT ? OFFSET ?";
            params = new Object[]{pageSize, startRow};
    }

    try {
        return jdbcTemplate.query(sql, params, boardRowMapper);
    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}
```

### 2단계: BoardListCommand에 검색 기능 통합

#### BoardListCommand.java 수정

```java
// 기존 BoardListCommand 클래스의 execute 메서드 수정

@Override
public String execute(HttpServletRequest request, HttpServletResponse response) {
    // 검색 파라미터 처리
    String searchType = request.getParameter("searchType");
    String searchKeyword = request.getParameter("searchKeyword");

    // 검색 조건이 없으면 기본값 설정
    if (searchType == null || searchType.trim().isEmpty()) {
        searchType = "";
    }
    if (searchKeyword == null) {
        searchKeyword = "";
    }

    // 페이징 파라미터 처리
    String pageStr = request.getParameter("page");
    int currentPage = 1;
    if (pageStr != null && !pageStr.trim().isEmpty()) {
        try {
            currentPage = Integer.parseInt(pageStr);
            if (currentPage < 1) currentPage = 1;
        } catch (NumberFormatException e) {
            currentPage = 1;
        }
    }

    int pageSize = 10; // 페이지당 게시글 수
    int startRow = (currentPage - 1) * pageSize;

    // 검색 결과 조회
    List<Board> boardList;
    int totalCount;

    if (searchType.isEmpty() || searchKeyword.trim().isEmpty()) {
        // 검색 조건이 없으면 전체 목록 조회
        boardList = boardDAO.getBoardListWithPagination(startRow, pageSize);
        totalCount = boardDAO.getTotalBoardCount();
    } else {
        // 검색 조건이 있으면 검색 결과 조회
        boardList = boardDAO.searchBoardWithPagination(searchType, searchKeyword, startRow, pageSize);
        totalCount = boardDAO.getSearchBoardCount(searchType, searchKeyword);
    }

    // 페이징 정보 계산
    int totalPages = (int) Math.ceil((double) totalCount / pageSize);
    if (totalPages < 1) totalPages = 1;

    // 페이징 객체 생성
    Pagination pagination = new Pagination();
    pagination.setCurrentPage(currentPage);
    pagination.setPageSize(pageSize);
    pagination.setTotalCount(totalCount);
    pagination.setTotalPages(totalPages);

    // request에 데이터 설정
    request.setAttribute("boardList", boardList);
    request.setAttribute("pagination", pagination);
    request.setAttribute("searchType", searchType);
    request.setAttribute("searchKeyword", searchKeyword);

    return "board/list.jsp";
}
```

### 3단계: list.jsp에 검색 UI 추가

#### list.jsp 수정 (기존 페이징 UI 위에 추가)

```jsp
<!-- 검색 폼 추가 -->
<div class="mb-6 bg-white rounded-lg shadow p-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4">게시글 검색</h3>

    <form action="front" method="get" class="space-y-4">
        <input type="hidden" name="command" value="boardList">

        <div class="flex flex-wrap gap-4 items-end">
            <!-- 검색 타입 선택 -->
            <div class="flex-1 min-w-48">
                <label class="block text-sm font-medium text-gray-700 mb-2">검색 범위</label>
                <select name="searchType" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="all" ${searchType == 'all' ? 'selected' : ''}>전체</option>
                    <option value="title" ${searchType == 'title' ? 'selected' : ''}>제목</option>
                    <option value="content" ${searchType == 'content' ? 'selected' : ''}>내용</option>
                    <option value="author" ${searchType == 'author' ? 'selected' : ''}>작성자</option>
                </select>
            </div>

            <!-- 검색어 입력 -->
            <div class="flex-1 min-w-64">
                <label class="block text-sm font-medium text-gray-700 mb-2">검색어</label>
                <input type="text" name="searchKeyword" value="${searchKeyword}"
                       placeholder="검색할 내용을 입력하세요"
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>

            <!-- 검색 버튼 -->
            <div class="flex gap-2">
                <button type="submit"
                        class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
                    검색
                </button>
                <a href="front?command=boardList"
                   class="px-6 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500">
                    초기화
                </a>
            </div>
        </div>
    </form>

    <!-- 검색 결과 요약 -->
    <c:if test="${not empty searchKeyword and searchKeyword != ''}">
        <div class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
            <p class="text-sm text-blue-800">
                <strong>"${searchKeyword}"</strong> 검색 결과:
                <span class="font-semibold">${pagination.totalCount}건</span>
                <c:if test="${searchType != 'all'}">
                    (${searchType == 'title' ? '제목' : searchType == 'content' ? '내용' : '작성자'} 검색)
                </c:if>
            </p>
        </div>
    </c:if>
</div>

<!-- 기존 게시글 목록 테이블 위에 검색 결과 표시 -->
<c:if test="${not empty searchKeyword and searchKeyword != '' and pagination.totalCount == 0}">
    <div class="mb-6 p-6 bg-yellow-50 border border-yellow-200 rounded-lg text-center">
        <svg class="w-12 h-12 text-yellow-400 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.47-.881-6.08-2.33"></path>
        </svg>
        <p class="text-lg font-medium text-yellow-800 mb-2">검색 결과가 없습니다</p>
        <p class="text-sm text-yellow-700">
            다른 검색어를 사용하거나 검색 범위를 변경해보세요.
        </p>
        <a href="front?command=boardList" class="inline-block mt-3 px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700">
            전체 목록 보기
        </a>
    </div>
</c:if>
```

### 4단계: HandlerMapping에 검색 관련 Command 등록

#### HandlerMapping.java 수정

```java
// 기존 HandlerMapping 클래스에 다음 내용 추가 (이미 있다면 생략)

// 검색 기능은 BoardListCommand에 통합되어 있으므로 추가 등록 불필요
// 기존 boardList command가 검색 기능을 포함하여 처리
```

## 📝 완료 체크리스트

- [ ] BoardDAO에 검색 메서드 추가 (`getSearchBoardCount`, `searchBoardWithPagination`)
- [ ] BoardListCommand에 검색 기능 통합
- [ ] list.jsp에 검색 UI 추가 (검색 폼, 결과 요약, 결과 없음 메시지)
- [ ] 검색 기능 테스트 완료 (제목, 내용, 작성자, 전체 검색)

## ⚠️ 주의사항

### 1. 성능 고려사항

- **인덱스 설정**: 검색 필드에 적절한 인덱스 설정 필요
- **검색 범위 제한**: 너무 광범위한 검색은 성능 저하 가능
- **결과 수 제한**: 페이징을 통한 결과 수 제한으로 응답 속도 향상

### 2. 사용자 경험

- **검색 결과 표시**: 검색어와 결과 수를 명확하게 표시
- **결과 없음 처리**: 검색 결과가 없을 때 적절한 안내 메시지
- **검색 조건 유지**: 페이징 시에도 검색 조건 유지

### 3. 검색 품질

- **부분 일치**: LIKE 연산자로 부분 일치 검색 지원
- **대소문자**: 필요시 대소문자 구분 없이 검색
- **특수문자**: 검색어에 특수문자가 포함된 경우 처리

## 🎯 테스트 방법

### 1. 기본 검색 기능 테스트

- 제목 검색: 게시글 제목에 포함된 키워드로 검색
- 내용 검색: 게시글 내용에 포함된 키워드로 검색
- 작성자 검색: 작성자 이름으로 검색
- 전체 검색: 제목, 내용, 작성자에서 동시 검색

### 2. 검색 결과 테스트

- 검색 결과 수 표시 확인
- 검색 결과가 없을 때 메시지 표시 확인
- 페이징과 검색 결과 연동 확인

### 3. 검색 UI 테스트

- 검색 폼 동작 확인
- 검색 조건 초기화 기능 확인
- 검색 결과 요약 정보 표시 확인

## 🔗 다음 단계

간단한 검색 기능 구현 완료 후:

1. **step05**: 리치 텍스트 에디터 통합 (CKEditor, TinyMCE 등)

이제 사용자가 원하는 게시글을 쉽게 찾을 수 있는 검색 기능으로 게시판의 활용도를 크게 향상시킬 수 있습니다!
