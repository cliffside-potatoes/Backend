package com.potatoes.Naengu.recipe.domain.vo;

import lombok.Getter;

@Getter
public enum Difficulty {
    BEGINNER("초보 환영"),
    INTERMEDIATE("보통"),
    ADVANCED("어려움");

    private final String description;

    Difficulty(String description) {
        this.description = description;
    }
}
