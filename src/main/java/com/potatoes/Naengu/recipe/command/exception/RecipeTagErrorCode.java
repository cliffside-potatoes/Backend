package com.potatoes.Naengu.recipe.command.exception;

import com.potatoes.Naengu.ingredients.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeTagErrorCode implements ErrorCode {

    TAG_NOT_FOUND(
            "TAG_NOT_FOUND",
            "태그를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    TAG_DUPLICATE(
            "TAG_DUPLICATE",
            "이미 존재하는 태그입니다.",
            HttpStatus.CONFLICT
    ),

    RECIPE_TAG_DUPLICATE(
            "RECIPE_TAG_DUPLICATE",
            "이미 레시피에 등록된 태그입니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    RecipeTagErrorCode(String code, String message, HttpStatus status) {
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
