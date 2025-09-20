package com.clover.bookflow.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record NationalLibraryResponseDto(
    @JsonProperty("TOTAL_COUNT") String totalCount,
    @JsonProperty("PAGE_NO") String pageNo,
    @JsonProperty("docs") List<Doc> docs
) {

  public record Doc(
      @JsonProperty("PUBLISHER") String publisher,
      @JsonProperty("DDC") String ddc,
      @JsonProperty("UPDATE_DATE") String updateDate,
      @JsonProperty("EA_ADD_CODE") String eaAddCode,
      @JsonProperty("PUBLISHER_URL") String publisherUrl,
      @JsonProperty("AUTHOR") String author,
      @JsonProperty("SERIES_TITLE") String seriesTitle,
      @JsonProperty("KDC") String kdc,
      @JsonProperty("EDITION_STMT") String editionStmt,
      @JsonProperty("BOOK_TB_CNT_URL") String bookTbCntUrl,
      @JsonProperty("BOOK_TB_CNT") String bookTbCnt,
      @JsonProperty("BOOK_INTRODUCTION_URL") String bookIntroductionUrl,
      @JsonProperty("BOOK_INTRODUCTION") String bookIntroduction,
      @JsonProperty("BOOK_SUMMARY_URL") String bookSummaryUrl,
      @JsonProperty("BOOK_SUMMARY") String bookSummary,
      @JsonProperty("TITLE_URL") String titleUrl,
      @JsonProperty("SET_ISBN") String setIsbn,
      @JsonProperty("REAL_PUBLISH_DATE") String realPublishDate,
      @JsonProperty("PRE_PRICE") String prePrice,
      @JsonProperty("DEPOSIT_YN") String depositYn,
      @JsonProperty("BOOK_SIZE") String bookSize,
      @JsonProperty("EBOOK_YN") String ebookYn,
      @JsonProperty("REAL_PRICE") String realPrice,
      @JsonProperty("FORM") String form,
      @JsonProperty("CONTROL_NO") String controlNo,
      @JsonProperty("SERIES_NO") String seriesNo,
      @JsonProperty("EA_ISBN") String eaIsbn,
      @JsonProperty("INPUT_DATE") String inputDate,
      @JsonProperty("SET_EXPRESSION") String setExpression,
      @JsonProperty("VOL") String vol,
      @JsonProperty("CIP_YN") String cipYn,
      @JsonProperty("SUBJECT") String subject,
      @JsonProperty("BIB_YN") String bibYn,
      @JsonProperty("TITLE") String title,
      @JsonProperty("PUBLISH_PREDATE") String publishPredate,
      @JsonProperty("SET_ADD_CODE") String setAddCode,
      @JsonProperty("PAGE") String page,
      @JsonProperty("RELATED_ISBN") String relatedIsbn,
      @JsonProperty("FORM_DETAIL") String formDetail
  ) {

  }
}
