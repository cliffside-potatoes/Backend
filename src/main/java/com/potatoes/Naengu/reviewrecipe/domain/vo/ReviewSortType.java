package com.potatoes.Naengu.reviewrecipe.domain.vo;

public enum ReviewSortType {
    LATEST("최신순");

    private final String type;

    ReviewSortType(String type) {
        this.type = type;
    }
}
