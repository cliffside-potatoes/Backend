package com.potatoes.Naengu.recipe.exception;

import com.potatoes.Naengu.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeErrorCode implements ErrorCode {
    INVALID_CURSOR(
            "INVALID_CURSOR",
            "커서 값이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_SORT_TYPE(
            "INVALID_SORT_TYPE",
            "지원하지 않는 정렬 방식입니다.",
            HttpStatus.BAD_REQUEST
    ),

    RECIPE_TYPE_MISMATCH(
            "RECIPE_TYPE_MISMATCH",
            "레시피 타입이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),

    RECIPE_NOT_FOUND(
            "RECIPE_NOT_FOUND",
            "존재하지 않는 레시피 입니다.",
            HttpStatus.NOT_FOUND
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    RecipeErrorCode(String code, String message, HttpStatus status) {
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
