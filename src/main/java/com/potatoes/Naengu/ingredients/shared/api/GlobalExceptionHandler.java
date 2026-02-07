package com.potatoes.Naengu.ingredients.shared.api;

import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_DUPLICATE;

import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Api<Void>> handlerApiException(ApiException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(Api.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Api<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Api.error("VALIDATION_ERROR", message));
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Api<Void>> handleConstraintViolation(ConstraintViolationException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Api.error("VALIDATION_ERROR", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Api<Void>> handleNotReadable(HttpMessageNotReadableException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Api.error("INVALID_REQUEST", "요청 JSON을 올바르게 작성해주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Api<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e){

        return ResponseEntity
                .status(CATEGORY_DUPLICATE.status())
                .body(Api.error(CATEGORY_DUPLICATE.code(), CATEGORY_DUPLICATE.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Api<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Api.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));

    }
}
