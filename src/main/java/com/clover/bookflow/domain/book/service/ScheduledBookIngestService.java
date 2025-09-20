package com.clover.bookflow.domain.book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledBookIngestService {

  private final AdminBookIngestService adminBookIngestService;

  // Todo: 편집자 추천 : 월 1 회

  // 베스트셀러
//  @Scheduled(cron = "0 0 2 ? * MON")
  @Scheduled(cron = "0 */1 * * * *") // 테스트용
  public void ingestWeeklyBestsellers() {
    log.info("스케줄러 실행됨");
    adminBookIngestService.ingestBestSellers();
  }

  // Todo: 주목 신간 : 매주 수 2시


}
