package com.potatoes.Naengu.ingredients.shared.exception;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode{
    INVALID_INPUT(
            "INVALID_INPUT",
            "Invalid input",
            HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CommonErrorCode(String code, String message, HttpStatus status) {
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
