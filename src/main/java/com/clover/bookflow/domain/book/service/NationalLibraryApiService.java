package com.clover.bookflow.domain.book.service;

import com.clover.bookflow.domain.book.client.NationalLibraryApiClient;
import com.clover.bookflow.domain.book.dto.BookAllSaveRequestDto;
import com.clover.bookflow.domain.book.dto.NatLibRecResponseDto;
import com.clover.bookflow.domain.book.dto.NatLibRecResponseDto.Item;
import com.clover.bookflow.global.converter.NationalLibraryXmlParser;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NationalLibraryApiService {

  private final NationalLibraryApiClient nationalLibraryApiClient;

  public List<BookAllSaveRequestDto> fetchRecommendedBooks() {
    String books = nationalLibraryApiClient.fetchRecommendedBooks();
    log.info("fetched recommended books : {}", books);
    List<BookAllSaveRequestDto> bookAllList = new ArrayList<>();

    try {
      NatLibRecResponseDto response = NationalLibraryXmlParser.parse(books);
      log.info("parsed books : {}", response.getTotalCount());

      bookAllList = response.getList().stream()
          .map(list -> {
            Item item = list.getItem();
            // HTML 디코딩 (Apache Commons Lang 사용)
            String decodedMokcha = StringEscapeUtils.unescapeHtml4(item.getRecommokcha());
            String decodedContents = StringEscapeUtils.unescapeHtml4(
                item.getRecomcontens());
            String plainMokcha = Jsoup.parse(decodedMokcha).text();
            String plainContents = Jsoup.parse(decodedContents).text();

            // 필요한 추가 가공 처리

            return new BookAllSaveRequestDto(
                item.getDrCode(),
                item.getDrCodeName(),
                item.getRecomtitle(),
                item.getRecomauthor(),
                item.getRecompublisher(),
                item.getRecomisbn(),
                item.getRecomfilepath(),
                plainMokcha,
                plainContents,
                item.getPublishYear(),
                item.getMokchFilePath()
            );
          })
          .toList();

      // saveDtoList를 DB 저장 등 다음 처리로 넘기기

    } catch (Exception e) {
      log.error("국립도서관 추천도서 파싱 중 오류", e);
      // 예외 처리
    }

    return bookAllList;
  }

  /**
   * 현재 API 응답 정보가 부족하여 사용 보류 중입니다.
   * 추후 API 응답 개선 시 재사용 가능합니다.
   */
//  public List<BookDetailSaveRequestDto> fetchBookDetails(List<BookSaveRequestDto> books) {
//    List<BookDetailSaveRequestDto> bookDetails = new ArrayList<>();
//    for (BookSaveRequestDto book : books) {
//      // 각 책에 대해 API 호출
//      try {
//        // 필요한 인자 넘기기 (isbn)
//        log.info("상세 정보 API 호출 대상 : {}", book.isbn());
//        String responseString = nationalLibraryApiClient.fetchBookDetailString(book.isbn());
//        log.info(responseString);
//        NationalLibraryResponseDto response = nationalLibraryApiClient.fetchBookDetail(book.isbn());
//        Doc doc = response.docs().isEmpty() ? null : response.docs().getFirst();
//        log.info("수신된 정보 : {}", doc != null ? doc.eaIsbn() : "응답 없음");
//
//        if (doc != null) {
//          BookDetailSaveRequestDto dto = BookDetailSaveRequestDto.complete(
//              doc.eaIsbn(),
//              doc.ddc(),
//              doc.kdc(),
//              doc.bookTbCntUrl(),
//              doc.bookTbCnt(),
//              doc.bookIntroductionUrl(),
//              doc.bookIntroduction(),
//              doc.bookSummaryUrl(),
//              doc.bookSummary(),
//              doc.subject(),
//              doc.page()
//          );
//          bookDetails.add(dto);
//        } else {
//          // isbn, 상태 저장
//          bookDetails.add(BookDetailSaveRequestDto.noDetail(book.isbn()));
//          log.warn("No detail found for ISBN: {}", book.isbn());
//        }
//      } catch (Exception e) {
//        // isbn, 상태 저장
//        bookDetails.add(BookDetailSaveRequestDto.error(book.isbn()));
//        log.error("Failed to fetch details for book: {}", book.isbn(), e);
//      }
//    }
//    return bookDetails;
//  }

}
