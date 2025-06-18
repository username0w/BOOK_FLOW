package com.clover.bookflow.domain.member.integration;

import com.clover.bookflow.config.AbstractIntegrationTest;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberIntegrationTest extends AbstractIntegrationTest {

  // 단위테스트와 달리 통합테스트에서 필드 주입한 이유
  // 이유1. 생성자 주입이 통합에서 큰 이점 없다.
  // 통합테스트에서는 Spring 이 자동으로 모든 Bean 구성, 주입
  // Mock 객체나 직접 주입할 필요 적음
  // 생성자 주입의 장점(명확한 의존성, 불변성) 은 서비스/비즈니스 로직 쪽에서 훨씬 중요
  // 2. 테스트 코드의 가독성과 단순화
  // 생성자 만들고 this 할당보다 필드 주입이 더 간결, 덜 방해

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MemberRepository memberRepository;

  @BeforeEach
  void clean() {
    memberRepository.deleteAll();
  }

}
