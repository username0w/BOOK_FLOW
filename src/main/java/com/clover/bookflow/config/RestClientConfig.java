package com.clover.bookflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient() {
    // RestClient 인스턴스 커스터마이징
    return RestClient.builder()
        .baseUrl("https://www.aladin.co.kr/ttb/api")
        .build();
  }

}
