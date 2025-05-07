package com.clover.bookflow.common;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class ApiDocs {

  public static final FieldDescriptor[] COMMON_FIELDS = {
      fieldWithPath("success").description("요청 성공 여부"),
      fieldWithPath("code").description("응답 코드"),
      fieldWithPath("message").description("응답 메시지"),
      fieldWithPath("data").description("응답 데이터 (null일 수 있음 / 빈 배열일 수 있음)")
          .type(JsonFieldType.OBJECT).optional(),
      fieldWithPath("errors").description("입력 오류 상세 정보 배열 (빈 배열일 수 있음)")
  };

  public static final FieldDescriptor[] ERROR_DETAIL_FIELDS = {
      fieldWithPath("errors[].field").description("오류 발생 필드"),
      fieldWithPath("errors[].message").description("오류 메시지")
  };

  public static FieldDescriptor[] combineFields(FieldDescriptor[] base,
      FieldDescriptor... additional) {
    return Stream.concat(
        Arrays.stream(base),
        Arrays.stream(additional)
    ).toArray(FieldDescriptor[]::new);
  }

}