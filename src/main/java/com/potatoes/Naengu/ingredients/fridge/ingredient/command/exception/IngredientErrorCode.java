package com.potatoes.Naengu.ingredients.fridge.ingredient.command.exception;

import org.springframework.http.HttpStatus;

public enum IngredientErrorCode {
    FRIDGE_INGREDIENT_DUPLICATE("FRIDGE_INGREDIENT_DUPLICATE", "이미 등록된 재료입니다.", HttpStatus.CONFLICT),
    INGREDIENT_NOT_FOUND("INGREDIENT_NOT_FOUND", "존재하지 않는 재료입니다.", HttpStatus.NOT_FOUND),
    FRIDGE_CATEGORY_NOT_FOUND("FRIDGE_CATEGORY_NOT_FOUND", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND);


    private final String code;
    private final String message;
    private final HttpStatus status;

    IngredientErrorCode(String code, String message, HttpStatus status) {
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
