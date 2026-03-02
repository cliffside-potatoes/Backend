package com.potatoes.Naengu.recipe.exception;

import com.potatoes.Naengu.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeIngredientErrorCode implements ErrorCode {

    RECIPE_INGREDIENT_DUPLICATE(
            "RECIPE_INGREDIENT_DUPLICATE",
            "이미 레시피에 등록된 재료입니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    RecipeIngredientErrorCode(String code, String message, HttpStatus status) {
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
