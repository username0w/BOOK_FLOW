package com.clover.bookflow.domain.book.client;

import com.clover.bookflow.domain.book.dto.AladinResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component // 클라이언트 여기서 직접 생성하지 않고 설정 클래스나 자동 구성에서 미리 만든 것 주입 받아 사용
public class AladinApiClient { // 실제 API 호출

  private final RestClient restClient;

  @Value("${aladin.ttbkey}")
  private String ttbKey;

  public AladinApiClient(RestClient restClient) {
    this.restClient = restClient;
  }

  public AladinResponseDto fetchBestSellers() {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/ItemList.aspx")
            .queryParam("ttbkey", ttbKey)
            .queryParam("QueryType", "Bestseller")
            .queryParam("MaxResults", 10)
            .queryParam("start", 1)
            .queryParam("SearchTarget", "Book")
            .queryParam("Start", 1)
            .queryParam("Cover", "MidBig")
            .queryParam("Output", "JS")
            .queryParam("Version", "20131101")
            .queryParam("Year", 2024)
            .queryParam("Month", 5)
            .queryParam("Week", 1)
            .build())
        .retrieve()
        .body(AladinResponseDto.class);
  }

}
