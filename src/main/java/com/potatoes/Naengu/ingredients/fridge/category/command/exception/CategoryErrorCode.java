package com.potatoes.Naengu.ingredients.fridge.category.command.exception;

import org.springframework.http.HttpStatus;

public enum CategoryErrorCode {

    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CATEGORY_DUPLICATE("CATEGORY_DUPLICATE", "이미 존재하는 카테고리입니다.", HttpStatus.CONFLICT),

    CATEGORY_FORBIDDEN("CATEGORY_FORBIDDEN", "해당 카테고리에 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    CATEGORY_UPDATE_EMPTY("CATEGORY_UPDATE_EMPTY", "수정할 값이 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CategoryErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus status() {
        return status;
    }

}
