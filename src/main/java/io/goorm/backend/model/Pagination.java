package io.goorm.backend.model;

import java.util.List;
import java.util.ArrayList;

/**
 * 페이지네이션 정보를 담는 모델 클래스
 */
public class Pagination {
  private int currentPage; // 현재 페이지
  private int pageSize; // 페이지당 게시글 수
  private int totalCount; // 전체 게시글 수
  private int totalPages; // 전체 페이지 수
  private int startPage; // 시작 페이지 번호
  private int endPage; // 끝 페이지 번호
  private boolean hasPrevious; // 이전 페이지 존재 여부
  private boolean hasNext; // 다음 페이지 존재 여부
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
  public int getCurrentPage() {
    return currentPage;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public int getStartPage() {
    return startPage;
  }

  public int getEndPage() {
    return endPage;
  }

  public boolean isHasPrevious() {
    return hasPrevious;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public List<Integer> getPageNumbers() {
    return pageNumbers;
  }

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
