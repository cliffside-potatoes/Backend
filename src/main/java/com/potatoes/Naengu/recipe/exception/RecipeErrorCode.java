package com.potatoes.Naengu.recipe.exception;

import com.potatoes.Naengu.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeErrorCode implements ErrorCode {
    RECIPE_TYPE_MISMATCH(
            "RECIPE_TYPE_MISMATCH",
            "레시피 타입이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
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
