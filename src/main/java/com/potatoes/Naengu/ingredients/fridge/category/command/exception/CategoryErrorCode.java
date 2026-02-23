package com.potatoes.Naengu.ingredients.fridge.category.command.exception;

import com.potatoes.Naengu.ingredients.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CategoryErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(
            "CATEGORY_NOT_FOUND",
            "카테고리를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    CATEGORY_DUPLICATE(
            "CATEGORY_DUPLICATE",
            "이미 존재하는 카테고리입니다.",
            HttpStatus.CONFLICT
    ),

    CATEGORY_FORBIDDEN(
            "CATEGORY_FORBIDDEN",
            "해당 카테고리에 대한 권한이 없습니다.",
            HttpStatus.FORBIDDEN
    ),

    CATEGORY_UPDATE_EMPTY(
            "CATEGORY_UPDATE_EMPTY",
            "수정할 값이 없습니다.",
            HttpStatus.BAD_REQUEST
    ),

    CATEGORY_NAME_BLANK(
            "CATEGORY_NAME_BLANK",
            "카테고리 이름은 공백일 수 없습니다.",
            HttpStatus.BAD_REQUEST
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    CategoryErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
