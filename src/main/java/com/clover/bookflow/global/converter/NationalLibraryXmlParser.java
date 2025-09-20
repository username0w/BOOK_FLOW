package com.clover.bookflow.global.converter;

import com.clover.bookflow.domain.book.dto.NatLibRecResponseDto;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class NationalLibraryXmlParser {

  public static NatLibRecResponseDto parse(String xml) throws Exception {
    XmlMapper xmlMapper = new XmlMapper();
    return xmlMapper.readValue(xml, NatLibRecResponseDto.class);
  }
}
