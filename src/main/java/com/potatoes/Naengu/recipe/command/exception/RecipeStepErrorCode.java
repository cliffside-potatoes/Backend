package com.potatoes.Naengu.recipe.command.exception;

import com.potatoes.Naengu.ingredients.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeStepErrorCode implements ErrorCode {

    RECIPE_STEP_DUPLICATE_ORDER(
            "RECIPE_STEP_DUPLICATE_ORDER",
            "동일한 순서의 레시피 단계가 존재합니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    RecipeStepErrorCode(String code, String message, HttpStatus status) {
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
