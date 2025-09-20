package com.clover.bookflow.domain.book.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "channel")
public class NatLibRecResponseDto {

  @JacksonXmlProperty(localName = "totalCount")
  private int totalCount;

  // 여러 개의 <list> 요소를 리스트로 받기 (useWrapping=false 중요!)
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "list")
  private List<ListItem> list;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ListItem {

    @JacksonXmlProperty(localName = "item")
    private Item item;

  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item {

    @JacksonXmlProperty(localName = "recomNo")
    private String recomNo;

    @JacksonXmlProperty(localName = "drCode")
    private String drCode;

    @JacksonXmlProperty(localName = "drCodeName")
    private String drCodeName;

    @JacksonXmlProperty(localName = "recomtitle")
    private String recomtitle;

    @JacksonXmlProperty(localName = "recomauthor")
    private String recomauthor;

    @JacksonXmlProperty(localName = "recompublisher")
    private String recompublisher;

    @JacksonXmlProperty(localName = "recomcallno")
    private String recomcallno;

    @JacksonXmlProperty(localName = "recomisbn")
    private String recomisbn;

    @JacksonXmlProperty(localName = "recomfilepath")
    private String recomfilepath;

    @JacksonXmlProperty(localName = "recommokcha")
    private String recommokcha;

    @JacksonXmlProperty(localName = "recomcontens")
    private String recomcontens;

    @JacksonXmlProperty(localName = "regdate")
    private String regdate;

    @JacksonXmlProperty(localName = "controlNo")
    private String controlNo;

    @JacksonXmlProperty(localName = "publishYear")
    private String publishYear;

    @JacksonXmlProperty(localName = "recomYear")
    private String recomYear;

    @JacksonXmlProperty(localName = "recomMonth")
    private String recomMonth;

    @JacksonXmlProperty(localName = "mokchFilePath")
    private String mokchFilePath;
  }
}