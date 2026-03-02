package com.potatoes.Naengu.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.potatoes.Naengu.global.exception.ErrorCode;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Api<T>(
        String resultCode,
        String resultMessage,
        T data
) {

    public static <T> Api<T> success(T data) {
        return new Api<>("OK", "SUCCESS", data);
    }

    public static Api<Void> success() {
        return new Api<>("OK", "SUCCESS", null);
    }

    public static Api<Void> error(String code, String message) {
        return new Api<>(code, message, null);
    }

    public static Api<Void> error(ErrorCode errorCode) {
        return new Api<>(errorCode.code(), errorCode.message(), null);
    }

}
