package com.potatoes.Naengu.ingredient.exception;

import com.potatoes.Naengu.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum IngredientAliasErrorCode implements ErrorCode {

    INGREDIENT_ALIAS_DUPLICATE(
            "INGREDIENT_ALIAS_DUPLICATE",
            "이미 존재하는 재료 별칭입니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    IngredientAliasErrorCode(String code, String message, HttpStatus status) {
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
