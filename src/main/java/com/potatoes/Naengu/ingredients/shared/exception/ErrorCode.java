package com.potatoes.Naengu.ingredients.shared.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String code();

    String message();

    HttpStatus status();
}
