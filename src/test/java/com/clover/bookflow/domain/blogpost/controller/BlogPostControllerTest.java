package com.clover.bookflow.domain.blogpost.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.SecurityConfig;
import com.clover.bookflow.domain.auth.security.JwtAuthenticationFilter;
import com.clover.bookflow.domain.auth.security.annotation.WithMockCustomUser;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostCreateRequest;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostUpdateRequest;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostResponse;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostSimpleResponse;
import com.clover.bookflow.domain.blogpost.service.BlogPostService;
import com.clover.bookflow.domain.book.dto.BookInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(BlogPostController.class)
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class BlogPostControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private BlogPostService blogPostService;
  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;
  @Autowired
  private WebApplicationContext context;

  private static final String BASE_URL = "/api/v1/blog-posts";
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

  @DisplayName("블로그 글 생성 성공")
  @Test
  @WithMockCustomUser
  void createBlogPost_success() throws Exception {
    // given
    BlogPostCreateRequest request = new BlogPostCreateRequest("제목", "내용", List.of(1L, 2L));
    List<BookInfoResponse> books = List.of(
        new BookInfoResponse(1L, "책 제목 1", "작가 1"),
        new BookInfoResponse(2L, "책 제목 2", "작가 2")
    );

    BlogPostResponse response =
        new BlogPostResponse(
            1L,
            "제목",
            "내용",
            "author",
            LocalDateTime.now(),
            LocalDateTime.now(),
            books
        );
    given(blogPostService.createBlogPost(any(), eq(1L))).willReturn(response);

    // when & then
    testHelper
        .postRequest(BASE_URL, request)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.title").value("제목"))
        .andExpect(jsonPath("$.data.content").value("내용"))
        .andDo(
            document(
                "blog-post/create",
                requestFields(
                    fieldWithPath("title").description("블로그 제목"),
                    fieldWithPath("content").description("블로그 내용"),
                    fieldWithPath("bookIds").description("연결된 책 ID 목록")),
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("블로그 글 ID"),
                    fieldWithPath("data.title").description("블로그 글 제목"),
                    fieldWithPath("data.content").description("블로그 글 내용"),
                    fieldWithPath("data.authorName").description("작성자 이름"),
                    fieldWithPath("data.createdAt").description("생성 일시"),
                    fieldWithPath("data.updatedAt").description("수정 일시"),
                    fieldWithPath("data.books").description("연결된 책 정보 목록"),
                    fieldWithPath("data.books[].id").description("책 ID"),
                    fieldWithPath("data.books[].title").description("책 제목"),
                    fieldWithPath("data.books[].author").description("책 작가"),
                    fieldWithPath("errors").description("에러 목록"))));
  }

  @DisplayName("블로그 글 단건 조회")
  @Test
  @WithMockCustomUser
  void getBlogPost_success() throws Exception {
    List<BookInfoResponse> books = List.of(
        new BookInfoResponse(1L, "책 제목 1", "작가 1"),
        new BookInfoResponse(2L, "책 제목 2", "작가 2")
    );

    BlogPostResponse response =
        new BlogPostResponse(
            1L,
            "제목",
            "내용",
            "author",
            LocalDateTime.now(),
            LocalDateTime.now(),
            books
        );

    given(blogPostService.getBlogPost(eq(1L), anyLong())).willReturn(response);

    mockMvc
        .perform(get(BASE_URL + "/{id}", 1L))
        .andExpect(status().isOk())
        .andDo(
            document(
                "blog-post/get",
                pathParameters(parameterWithName("id").description("블로그 글 ID")),
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("블로그 글 ID"),
                    fieldWithPath("data.title").description("블로그 글 제목"),
                    fieldWithPath("data.content").description("블로그 글 내용"),
                    fieldWithPath("data.authorName").description("작성자 이름"),
                    fieldWithPath("data.createdAt").description("생성 일시"),
                    fieldWithPath("data.updatedAt").description("수정 일시"),
                    fieldWithPath("data.books").description("연결된 책 정보 목록"),
                    fieldWithPath("data.books[].id").description("책 ID"),
                    fieldWithPath("data.books[].title").description("책 제목"),
                    fieldWithPath("data.books[].author").description("책 작가"),
                    fieldWithPath("errors").description("에러 목록"))));
  }

  @DisplayName("공개된 블로그 글 목록 조회")
  @Test
  void getPublicPosts() throws Exception {
    List<BlogPostSimpleResponse> posts =
        List.of(
            new BlogPostSimpleResponse(1L, "제목1", "summary1", "author1", LocalDateTime.now()),
            new BlogPostSimpleResponse(2L, "제목2", "summary2", "author2", LocalDateTime.now()));
    Page<BlogPostSimpleResponse> page =
        new PageImpl<>(posts, PageRequest.of(0, 10), posts.size());

    given(blogPostService.getPublicBlogPosts(any(Pageable.class))).willReturn(page);

    Map<String, String> params = Map.of("page", "0", "size", "10");

    testHelper
        .getRequest(BASE_URL + "/public", params)
        .andExpect(status().isOk())
        .andDo(
            document(
                "blog-post/list-public",
                queryParameters(
                    parameterWithName("page").description("페이지 번호"),
                    parameterWithName("size").description("페이지 크기")),
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content[].id").description("글 ID"),
                    fieldWithPath("data.content[].title").description("제목"),
                    fieldWithPath("data.content[].summary").description("요약"),
                    fieldWithPath("data.content[].authorName").description("작성자"),
                    fieldWithPath("data.content[].createdAt").description("작성일"),
                    fieldWithPath("data.pagination.page").description("현재 페이지 번호"),
                    fieldWithPath("data.pagination.size").description("페이지 크기"),
                    fieldWithPath("data.pagination.totalPages").description("전체 페이지 수"),
                    fieldWithPath("data.pagination.totalElements").description("전체 데이터 수"),
                    fieldWithPath("data.pagination.first").description("첫 페이지 여부"),
                    fieldWithPath("data.pagination.last").description("마지막 페이지 여부"),
                    fieldWithPath("errors").description("에러 목록"))));
  }


  @DisplayName("작성자 블로그 글 목록 조회")
  @Test
  void getPostsByAuthor() throws Exception {
    long authorId = 5L;
    List<BlogPostSimpleResponse> posts =
        List.of(
            new BlogPostSimpleResponse(1L, "제목1", "summary", "author", LocalDateTime.now()));
    Page<BlogPostSimpleResponse> page = new PageImpl<>(posts, PageRequest.of(0, 10), posts.size());

    given(blogPostService.getPublicPostsByAuthor(eq(authorId), any(Pageable.class)))
        .willReturn(page);

    mockMvc
        .perform(
            get(BASE_URL + "/author/{authorId}", authorId).param("page", "0").param("size", "5"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "blog-post/list-by-author",
                pathParameters(parameterWithName("authorId").description("작성자 ID")),
                queryParameters(
                    parameterWithName("page").description("페이지 번호"),
                    parameterWithName("size").description("페이지 크기")),
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content[].id").description("글 ID"),
                    fieldWithPath("data.content[].title").description("제목"),
                    fieldWithPath("data.content[].summary").description("요약"),
                    fieldWithPath("data.content[].authorName").description("작성자"),
                    fieldWithPath("data.content[].createdAt").description("작성일"),
                    fieldWithPath("data.pagination.page").description("현재 페이지 번호"),
                    fieldWithPath("data.pagination.size").description("페이지 크기"),
                    fieldWithPath("data.pagination.totalPages").description("전체 페이지 수"),
                    fieldWithPath("data.pagination.totalElements").description("전체 데이터 수"),
                    fieldWithPath("data.pagination.first").description("첫 페이지 여부"),
                    fieldWithPath("data.pagination.last").description("마지막 페이지 여부"),
                    fieldWithPath("errors").description("에러 목록"))));
  }

  @DisplayName("내 블로그 글 목록 조회")
  @Test
  @WithMockCustomUser
  void getMyPosts_success() throws Exception {
    List<BlogPostSimpleResponse> posts =
        List.of(
            new BlogPostSimpleResponse(1L, "제목1", "summary", "author", LocalDateTime.now()));
    Page<BlogPostSimpleResponse> page = new PageImpl<>(posts, PageRequest.of(0, 10), posts.size());

    given(blogPostService.getMyBlogPosts(anyLong(), any(Pageable.class)))
        .willReturn(page);

    mockMvc
        .perform(get(BASE_URL + "/me").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andDo(document("blog-post/list-my",
            queryParameters(
                parameterWithName("page").description("페이지 번호"),
                parameterWithName("size").description("페이지 크기")),
            responseFields(
                fieldWithPath("success").description("요청 성공 여부"),
                fieldWithPath("code").description("응답 코드"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("data.content[].id").description("글 ID"),
                fieldWithPath("data.content[].title").description("제목"),
                fieldWithPath("data.content[].summary").description("요약"),
                fieldWithPath("data.content[].authorName").description("작성자"),
                fieldWithPath("data.content[].createdAt").description("작성일"),
                fieldWithPath("data.pagination.page").description("현재 페이지 번호"),
                fieldWithPath("data.pagination.size").description("페이지 크기"),
                fieldWithPath("data.pagination.totalPages").description("전체 페이지 수"),
                fieldWithPath("data.pagination.totalElements").description("전체 데이터 수"),
                fieldWithPath("data.pagination.first").description("첫 페이지 여부"),
                fieldWithPath("data.pagination.last").description("마지막 페이지 여부"),
                fieldWithPath("errors").description("에러 목록"))));
  }

  @DisplayName("블로그 글 수정")
  @Test
  @WithMockCustomUser
  void updateBlogPost_success() throws Exception {
    BlogPostUpdateRequest request = new BlogPostUpdateRequest("수정된 제목", "수정된 내용", List.of(1L, 2L));
    List<BookInfoResponse> books = List.of(
        new BookInfoResponse(1L, "책 제목 1", "작가 1"),
        new BookInfoResponse(2L, "책 제목 2", "작가 2")
    );

    BlogPostResponse response =
        new BlogPostResponse(
            1L,
            "수정된 제목",
            "수정된 내용",
            "author",
            LocalDateTime.now(),
            LocalDateTime.now(),
            books
        );

    given(blogPostService.updateBlogPost(eq(1L), any(), anyLong())).willReturn(response);

    mockMvc
        .perform(
            put(BASE_URL + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(
            document(
                "blog-post/update",
                pathParameters(parameterWithName("id").description("블로그 ID")),
                requestFields(
                    fieldWithPath("title").description("제목"),
                    fieldWithPath("content").description("내용"),
                    fieldWithPath("bookIds").description("책 ID 목록")),
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("블로그 글 ID"),
                    fieldWithPath("data.title").description("블로그 글 제목"),
                    fieldWithPath("data.content").description("블로그 글 내용"),
                    fieldWithPath("data.authorName").description("작성자 이름"),
                    fieldWithPath("data.createdAt").description("생성 일시"),
                    fieldWithPath("data.updatedAt").description("수정 일시"),
                    fieldWithPath("data.books").description("연결된 책 정보 목록"),
                    fieldWithPath("data.books[].id").description("책 ID"),
                    fieldWithPath("data.books[].title").description("책 제목"),
                    fieldWithPath("data.books[].author").description("책 작가"),
                    fieldWithPath("errors").description("에러 목록"))));
  }

  @DisplayName("블로그 글 삭제")
  @Test
  @WithMockCustomUser
  void deleteBlogPost_success() throws Exception {
    doNothing().when(blogPostService).deleteBlogPost(eq(1L), anyLong());

    mockMvc
        .perform(delete(BASE_URL + "/{id}", 1L))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "blog-post/delete",
                pathParameters(parameterWithName("id").description("삭제할 블로그 ID"))));
  }
}
