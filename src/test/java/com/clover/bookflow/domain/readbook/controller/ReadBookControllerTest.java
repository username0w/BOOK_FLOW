package com.clover.bookflow.domain.readbook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.SecurityConfig;
import com.clover.bookflow.domain.auth.security.JwtAuthenticationFilter;
import com.clover.bookflow.domain.auth.security.annotation.WithMockCustomUser;
import com.clover.bookflow.domain.readbook.dto.request.CreateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.request.UpdateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.response.ReadBookResponse;
import com.clover.bookflow.domain.readbook.service.ReadBookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(ReadBookController.class)
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class ReadBookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReadBookService readBookService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private WebApplicationContext context;

    private static final String BASE_URL = "/api/v1/read-books";
    private TestHelper testHelper;

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider) {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(documentationConfiguration(provider)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint())
                                .withResponseDefaults(prettyPrint()))
                        .build();

        testHelper = new TestHelper(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("should_returnCreatedReadBook_when_addReadBookRequestIsValid")
    @WithMockCustomUser
    void should_returnCreatedReadBook_when_addReadBookRequestIsValid() throws Exception {
        // given
        CreateReadBookRequest request = new CreateReadBookRequest("isbnofthebook", LocalDate.of(2024, 10, 15));
        ReadBookResponse response = new ReadBookResponse(
                1L, 10L, 1L, "Effective Java", "Joshua Bloch",
                "https://example.com/cover.jpg", LocalDate.of(2024, 10, 15));

        given(readBookService.addReadBook(any(CreateReadBookRequest.class), any(Long.class)))
                .willReturn(response);

        // when & then
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.title").value("Effective Java"))
                .andDo(document(
                        "read-book/create",
                        requestFields(
                                fieldWithPath("isbn").description("책 isbn"),
                                fieldWithPath("readDate").description("책을 읽은 날짜")
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.readBookId").description("읽은 책 ID"),
                                fieldWithPath("data.memberId").description("회원 ID"),
                                fieldWithPath("data.bookId").description("책 ID"),
                                fieldWithPath("data.title").description("책 제목"),
                                fieldWithPath("data.author").description("책 저자"),
                                fieldWithPath("data.coverImgUrl").description("책 표지 이미지 URL"),
                                fieldWithPath("data.readDate").description("읽은 날짜"),

                                fieldWithPath("errors").description("에러 목록")
                        )
                ))
                .andReturn();
    }

    @Test
    @DisplayName("should_updateReadBook_when_validUpdateRequest")
    @WithMockCustomUser
    void should_updateReadBook_when_validUpdateRequest() throws Exception {
        // given
        Long readBookId = 1L;
        UpdateReadBookRequest request = new UpdateReadBookRequest(LocalDate.of(2024, 11, 1));
        ReadBookResponse response = new ReadBookResponse(
                readBookId, 10L, 1L, "Effective Java", "Joshua Bloch",
                "https://example.com/cover.jpg", LocalDate.of(2024, 11, 1));

        given(readBookService.updateReadBook(eq(readBookId), any(UpdateReadBookRequest.class), any(Long.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put(BASE_URL + "/{readBookId}", readBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readBookId").value(1L))
                .andDo(document(
                        "read-book/update",
                        pathParameters(parameterWithName("readBookId").description("읽은 책 ID")),
                        requestFields(
                                fieldWithPath("readDate").description("수정된 읽은 날짜")
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.readBookId").description("읽은 책 ID"),
                                fieldWithPath("data.memberId").description("회원 ID"),
                                fieldWithPath("data.bookId").description("책 ID"),
                                fieldWithPath("data.title").description("책 제목"),
                                fieldWithPath("data.author").description("책 저자"),
                                fieldWithPath("data.coverImgUrl").description("책 표지 이미지 URL"),
                                fieldWithPath("data.readDate").description("수정된 읽은 날짜"),
                                fieldWithPath("errors").description("에러 목록")
                        )
                ))
                .andReturn();
    }

    @Test
    @DisplayName("should_deleteReadBook_when_validDeleteRequest")
    @WithMockCustomUser
    void should_deleteReadBook_when_validDeleteRequest() throws Exception {
        // given
        Long readBookId = 1L;

        // when & then
        mockMvc.perform(delete(BASE_URL + "/{readBookId}", readBookId))
                .andExpect(status().isNoContent())
                .andDo(document(
                        "read-book/delete",
                        pathParameters(parameterWithName("readBookId").description("삭제할 읽은 책 ID"))
                ));
    }

    @Test
    @DisplayName("should_returnPagedReadBooks_when_getMyReadBooks")
    @WithMockCustomUser
    void should_returnPagedReadBooks_when_getMyReadBooks() throws Exception {
        // given
        ReadBookResponse book1 = new ReadBookResponse(1L, 10L, 100L, "Book1", "Author1", "https://img1",
                LocalDate.now());
        ReadBookResponse book2 = new ReadBookResponse(2L, 10L, 101L, "Book2", "Author2", "https://img2",
                LocalDate.now());
        Page<ReadBookResponse> page = new PageImpl<>(List.of(book1, book2), PageRequest.of(0, 10), 2);

        given(readBookService.getReadBooksByMember(any(Long.class), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document(
                        "read-book/get-my-read-books",
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.content[].readBookId").description("읽은 책 ID"),
                                fieldWithPath("data.content[].memberId").description("회원 ID"),
                                fieldWithPath("data.content[].bookId").description("책 ID"),
                                fieldWithPath("data.content[].title").description("책 제목"),
                                fieldWithPath("data.content[].author").description("책 저자"),
                                fieldWithPath("data.content[].coverImgUrl").description("책 표지 이미지 URL"),
                                fieldWithPath("data.content[].readDate").description("읽은 날짜"),
                                fieldWithPath("data.pagination.page").description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.size").description("페이지 크기"),
                                fieldWithPath("data.pagination.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalElements").description("전체 데이터 수"),
                                fieldWithPath("data.pagination.first").description("첫 페이지 여부"),
                                fieldWithPath("data.pagination.last").description("마지막 페이지 여부"),
                                fieldWithPath("errors").description("에러 목록")
                        )
                ))
                .andReturn();
    }
}
