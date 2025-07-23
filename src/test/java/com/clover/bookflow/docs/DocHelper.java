package com.clover.bookflow.docs;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;

public class DocHelper {

  public static ResourceSnippetParameters build(
      String tag,
      String summary,
      String description,
      List<FieldDescriptor> requestFields,
      List<FieldDescriptor> responseFields
  ) {
    return builder()
        .tag(tag)
        .summary(summary)
        .description(description)
        .requestFields(requestFields != null ? requestFields : List.of())
        .responseFields(responseFields != null ? responseFields : List.of())
        .build();
  }

}
