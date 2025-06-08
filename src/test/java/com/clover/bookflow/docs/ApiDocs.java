package com.clover.bookflow.docs;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class ApiDocs {

  public static List<FieldDescriptor> commonFields() {
    return List.of(
        fieldWithPath("success").description("요청 성공 여부"),
        fieldWithPath("code").description("응답 코드"),
        fieldWithPath("message").description("응답 메시지"),
        fieldWithPath("data").description("응답 데이터").type(JsonFieldType.OBJECT).optional(),
        fieldWithPath("errors").description("에러 상세 정보").optional()
    );
  }

  public static List<FieldDescriptor> errorDetailFields() {
    return List.of(
        fieldWithPath("errors[].field").type(JsonFieldType.STRING).description("오류 필드").optional(),
        fieldWithPath("errors[].message").type(JsonFieldType.STRING).description("오류 메시지")
            .optional()
    );
  }

  public static List<FieldDescriptor> combine(List<FieldDescriptor> base,
      FieldDescriptor... extra) {
    return Stream.concat(base.stream(), Arrays.stream(extra)).collect(Collectors.toList());
  }
}