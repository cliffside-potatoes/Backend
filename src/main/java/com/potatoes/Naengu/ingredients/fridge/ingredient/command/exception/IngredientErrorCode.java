package com.potatoes.Naengu.ingredients.fridge.ingredient.command.exception;

import org.springframework.http.HttpStatus;

public enum IngredientErrorCode {
    FRIDGE_INGREDIENT_DUPLICATE("FRIDGE_INGREDIENT_DUPLICATE", "이미 등록된 재료입니다.", HttpStatus.CONFLICT),
    INGREDIENT_NOT_FOUND("INGREDIENT_NOT_FOUND", "존재하지 않는 재료입니다.", HttpStatus.NOT_FOUND),
    FRIDGE_CATEGORY_NOT_FOUND("FRIDGE_CATEGORY_NOT_FOUND", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    FRIDGE_INGREDIENT_UPDATE_EMPTY("FRIDGE_INGREDIENT_UPDATE_EMPTY", "수정할 값이 없습니다.", HttpStatus.BAD_REQUEST),
    FRIDGE_INGREDIENT_FORBIDDEN("FRIDGE_INGREDIENT_FORBIDDEN", "해당 냉장고 재료에 접근할 수 없습니다.", HttpStatus.FORBIDDEN);



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
