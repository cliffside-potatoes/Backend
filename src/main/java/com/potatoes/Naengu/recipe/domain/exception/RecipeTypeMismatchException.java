package com.potatoes.Naengu.recipe.domain.exception;

public class RecipeTypeMismatchException extends IllegalArgumentException {

    public RecipeTypeMismatchException(String message) {
        super(message);
    }
}
