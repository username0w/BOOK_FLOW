package com.clover.bookflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test") // 애플리케이션 컨텍스트 정상적 로드 확인 목적. h2 사용 가능
@SpringBootTest
class BookflowApplicationTests {

  @Test
  void contextLoads() {
  }

}
