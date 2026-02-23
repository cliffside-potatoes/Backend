package com.potatoes.Naengu.ingredients.dictionary.ingredient.exception;

import com.potatoes.Naengu.ingredients.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum IngredientErrorCode implements ErrorCode {
    INGREDIENT_DUPLICATE(
            "INGREDIENT_DUPLICATE",
            "이미 존재하는 재료입니다.",
            HttpStatus.CONFLICT
    ),

    INGREDIENT_NOT_FOUND(
            "INGREDIENT_NOT_FOUND",
            "존재하지 않는 재료입니다.",
            HttpStatus.NOT_FOUND
    ) {};


    private final String code;
    private final String message;
    private final HttpStatus status;

    IngredientErrorCode(String code, String message, HttpStatus status) {
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
