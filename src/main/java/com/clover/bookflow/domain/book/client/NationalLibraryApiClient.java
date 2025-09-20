package com.clover.bookflow.domain.book.client;

import com.clover.bookflow.domain.book.dto.NationalLibraryResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class NationalLibraryApiClient {

  private final WebClient webClient;

  @Value("${nl.key}")
  private String key;

  public NationalLibraryApiClient(WebClient webClient) {
    this.webClient = webClient;
  }

  public String fetchRecommendedBooks() {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("NL/search/openApi/saseoApi.do")
            .queryParam("Key", key)
            .queryParam("startRowNumApi", 1)
            .queryParam("endRowNumApi", 100)
            .queryParam("start_date", "20240501")
            .queryParam("end_date", "20240531")
            .build())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public NationalLibraryResponseDto fetchBookDetail(String isbn) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/seoji/SearchApi.do")
            .queryParam("cert_key", key)
            .queryParam("result_style", "json")
            .queryParam("page_no", "1")
            .queryParam("page_size", "10")
            .queryParam("isbn", isbn).build())
        .retrieve().bodyToMono(NationalLibraryResponseDto.class).block();
  }


  public String fetchBookDetailString(String isbn) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("cert_key", key)
            .queryParam("result_style", "json")
            .queryParam("page_no", "1")
            .queryParam("page_size", "10")
            .queryParam("isbn", isbn).build())
        .retrieve().bodyToMono(String.class).block();
  }
}
