package com.clover.bookflow.domain.user.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.config.AbstractIntegrationTest;
import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @DisplayName("회원가입 성공")
  @Test
  void signup_success() throws Exception {
    UserSignupRequest request = new UserSignupRequest("hkd111@example.com", "password123", "길똥이");
    mockMvc.perform(post("/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request))
            .with(csrf())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("hkd111@example.com"))
        .andExpect(jsonPath("$.nickname").value("길똥이"));
  }


}
