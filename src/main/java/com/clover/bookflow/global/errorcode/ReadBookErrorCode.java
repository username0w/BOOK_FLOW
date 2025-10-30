package com.clover.bookflow.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReadBookErrorCode implements ErrorCode {

    READ_BOOK_NOT_FOUND("READ_BOOK_001", "읽은 책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_ISBN("READ_BOOK_002", "유효하지 않은 ISBN입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_READ_BOOK("READ_BOOK_003", "이미 등록된 읽은 책입니다.", HttpStatus.CONFLICT),
    ACCESS_DENIED("READ_BOOK_004", "본인의 읽은 도서만 수정/삭제할 수 있습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    // enum은 특별한 클래스라서, 생성자를 명시적으로 작성하는 게 가독성에 좋고, Lombok 어노테이션과 충돌 가능성도 줄어듦
    ReadBookErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
