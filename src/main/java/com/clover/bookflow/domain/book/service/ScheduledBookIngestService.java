package com.clover.bookflow.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduledBookIngestService {

  private final AdminBookIngestService adminBookIngestService;

  // Todo: 편집자 추천 : 월 1 회

  // 베스트셀러
  @Scheduled(cron = "0 0 2 ? * MON")
  public void ingestWeeklyBestsellers() {
    adminBookIngestService.ingestBestSellers();
  }

  // Todo: 주목 신간 : 매주 수 2시


}
