package com.clover.bookflow.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

public record AladinResponseDto(
    String version,
    String logo,
    String title,
    String link,
    String pubDate,
    int totalResults,
    int startIndex,
    int itemsPerPage,
    String query,
    int searchCategoryId,
    String searchCategoryName,

    @JsonProperty("item")
    List<Item> items
) {

  public record Item(
      String title,
      String link,
      String author,
      LocalDate pubDate,
      String description,
      String isbn,
      String isbn13,
      long itemId,
      int priceSales,
      int priceStandard,
      String mallType,
      String stockStatus,
      int mileage,
      String cover,
      int categoryId,
      String categoryName,
      String publisher,
      int salesPoint,
      boolean adult,
      boolean fixedPrice,
      int customerReviewRank,
      String bestDuration,
      int bestRank,
      SeriesInfo seriesInfo,
      SubInfo subInfo
  ) {

  }

  public record SeriesInfo(
      long seriesId,
      String seriesLink,
      String seriesName
  ) {

  }

  public record SubInfo() {

  }
}