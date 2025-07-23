package com.clover.bookflow.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest // Spring context 로딩
@ExtendWith(SpringExtension.class) // JUnit5 에서 Spring 확장 지원 (@SpringBootTest가 이미 포함하지만 명시하는 경우도 있음)
public abstract class AbstractIntegrationTest {

  @Container
  protected static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:9.1")
      .withDatabaseName("bookflow_testcontainer")
      .withUsername("testuser")
      .withPassword("testpassword");

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mySQLContainer::getUsername);
    registry.add("spring.datasource.password", mySQLContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", mySQLContainer::getDriverClassName);
    registry.add("spring.jpa.database-platform",
        () -> "org.hibernate.dialect.MySQL8Dialect");
  }

}


