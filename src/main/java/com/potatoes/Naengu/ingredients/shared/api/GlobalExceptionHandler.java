package com.potatoes.Naengu.ingredients.shared.api;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.exception.IngredientAliasErrorCode;
import com.potatoes.Naengu.ingredients.dictionary.ingredient.exception.IngredientErrorCode;
import com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import com.potatoes.Naengu.ingredients.shared.exception.CommonErrorCode;
import com.potatoes.Naengu.ingredients.shared.exception.ErrorCode;
import com.potatoes.Naengu.ingredients.shared.exception.SystemErrorCode;
import com.potatoes.Naengu.recipe.command.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.command.exception.RecipeIngredientErrorCode;
import com.potatoes.Naengu.recipe.command.exception.RecipeStepErrorCode;
import com.potatoes.Naengu.recipe.command.exception.RecipeTagErrorCode;
import com.potatoes.Naengu.recipe.domain.exception.RecipeTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, ErrorCode> CONSTRAINT_ERROR_CODES = Map.of(
            "uk_fridge_category_fridge_storage_name_deleted", CategoryErrorCode.CATEGORY_DUPLICATE,
            "uk_tag_value", RecipeTagErrorCode.TAG_DUPLICATE,
            "uk_ingredient_name", IngredientErrorCode.INGREDIENT_DUPLICATE,
            "uk_ingredient_alias_alias_name", IngredientAliasErrorCode.INGREDIENT_ALIAS_DUPLICATE,
            "uk_recipe_tag_recipe_tag", RecipeTagErrorCode.RECIPE_TAG_DUPLICATE,
            "uk_recipe_ingredient_recipe_ingredient", RecipeIngredientErrorCode.RECIPE_INGREDIENT_DUPLICATE,
            "uk_recipe_step_parent_step", RecipeStepErrorCode.RECIPE_STEP_DUPLICATE_ORDER
    );

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Api<Void>> handlerApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();

        logClientError("ApiException", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.status())
                .body(Api.error(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Api<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        logClientError("ValidationError", CommonErrorCode.INVALID_INPUT, details);

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT.status())
                .body(Api.error(CommonErrorCode.INVALID_INPUT));
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Api<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String details = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        logClientError("ConstraintViolation", CommonErrorCode.INVALID_INPUT, details);

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT.status())
                .body(Api.error(CommonErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Api<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        String msg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        logClientError("HttpMessageNotReadable", CommonErrorCode.INVALID_INPUT, abbreviate(msg, 300));

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT.status())
                .body(Api.error(CommonErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Api<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorCode errorCode = resolveConstraintErrorCode(e);
        String constraintName = extractConstraintName(e);

        log.warn("DataIntegrityViolation. constraint={}, mappedCode={}, message={}",
                constraintName, errorCode.code(), e.getMessage(), e);

        if (constraintName != null && CONSTRAINT_ERROR_CODES.containsKey(constraintName)) {
            logClientError("DataIntegrityViolation", errorCode, "constraint=" + constraintName);
        } else {
            logServerError("DataIntegrityViolation(unknown)", errorCode, "constraint=" + constraintName, e);
        }

        return ResponseEntity
                .status(errorCode.status())
                .body(Api.error(errorCode));
    }

    private ErrorCode resolveConstraintErrorCode(DataIntegrityViolationException e) {
        String constraintName = extractConstraintName(e);
        if (constraintName == null) {
            return SystemErrorCode.DATABASE_INCONSISTENCY;
        }
        return CONSTRAINT_ERROR_CODES.getOrDefault(constraintName, SystemErrorCode.DATABASE_INCONSISTENCY);
    }

    private String extractConstraintName(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException violation) {
                return violation.getConstraintName();
            }
            current = current.getCause();
        }
        return null;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Api<Void>> handleIllegalArgument(IllegalArgumentException e) {
        logClientError("IllegalArgumentException", CommonErrorCode.INVALID_INPUT, abbreviate(e.getMessage(), 300));

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT.status())
                .body(Api.error(CommonErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(RecipeTypeMismatchException.class)
    public ResponseEntity<Api<Void>> handleRecipeTypeMismatch(RecipeTypeMismatchException e) {
        ErrorCode errorCode = RecipeErrorCode.RECIPE_TYPE_MISMATCH;
        logClientError("RecipeTypeMismatch", errorCode, abbreviate(e.getMessage(), 300));
        return ResponseEntity
                .status(errorCode.status())
                .body(Api.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Api<Void>> handleUnexpected(Exception e) {
        ErrorCode errorCode = SystemErrorCode.INTERNAL_ERROR;
        logServerError("Unexpected", errorCode, null, e);

        return ResponseEntity
                .status(errorCode.status())
                .body(Api.error(errorCode));
    }

    // -- logging helpers 메서드 --

    private void logClientError(String type, ErrorCode code, String details) {
        if (details == null || details.isBlank()) {
            log.warn("{}: code={}", type, code.code());
            return;
        }
        log.warn("{}: code={}, details={}", type, code.code(), details);
    }

    private void logServerError(String type, ErrorCode code, String details, Exception e) {
        if (details == null || details.isBlank()) {
            log.error("{}: code={}", type, code.code(), e);
            return;
        }
        log.error("{}: code={}, details={}", type, code.code(), details, e);
    }

    private String abbreviate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}
