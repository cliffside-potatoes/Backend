package com.potatoes.Naengu.reviewrecipe.exception;

import com.potatoes.Naengu.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeReviewErrorCode implements ErrorCode {
    PROFILE_NOT_FOUND(
            "PROFILE_NOT_FOUND",
            "프로필을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    RECIPE_REVIEW_DUPLICATE(
            "RECIPE_REVIEW__DUPLICATE",
            "이미 해당 레시피에 대한 리뷰가 존재합니다.",
            HttpStatus.CONFLICT
    ),
    INVALID_CURSOR(
            "INVALID_CURSOR",
            "잘못된 cursor 입니다.",
            HttpStatus.BAD_REQUEST
    );


    private final String code;
    private final String message;
    private final HttpStatus status;

    RecipeReviewErrorCode(String code, String message, HttpStatus status) {
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
