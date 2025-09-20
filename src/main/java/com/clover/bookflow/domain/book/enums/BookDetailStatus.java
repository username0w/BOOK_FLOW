package com.clover.bookflow.domain.book.enums;

public enum BookDetailStatus {
  READY, // 적재 대기 (book 생성 시)
  PROCESSING, // 적재 시도 중 (큐에 넣었거나 워커가 처리 중)
  COMPLETE, // 상세 적재 성공
  NO_DETAIL, // 상세 데이터 없음
  ERROR, // 오류 발생
  FAILED // 재시도 후 실패 확정
}
