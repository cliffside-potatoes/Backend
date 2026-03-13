package com.potatoes.Naengu.post.exception;

import com.potatoes.Naengu.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PostErrorCode implements ErrorCode {

    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND", "프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CURSOR("INVALID_CURSOR", "cursorCreatedAt과 cursorId는 함께 전달되어야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_SORT_TYPE("INVALID_SORT_TYPE", "지원하지 않는 정렬 방식입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    PostErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override public String code() { return code; }
    @Override public String message() { return message; }
    @Override public HttpStatus status() { return status; }
}
