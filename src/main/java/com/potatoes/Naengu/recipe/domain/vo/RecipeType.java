package com.potatoes.Naengu.recipe.domain.vo;

import lombok.Getter;

@Getter
public enum RecipeType {

    TEXT("텍스트 레시피"),
    LINK("링크 레시피");

    private final String description;

    RecipeType(String description) {
        this.description = description;
    }

    public static RecipeType from(String value) {
        try {
            return RecipeType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 레시피 타입 입니다: " + value);
        }

    }
}
