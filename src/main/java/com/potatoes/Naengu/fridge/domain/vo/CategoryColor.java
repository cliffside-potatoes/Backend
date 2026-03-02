package com.potatoes.Naengu.fridge.domain.vo;

import lombok.Getter;

@Getter
public enum CategoryColor {
    RED("#FF0000"),
    GREEN("#00FF00"),
    BLUE("#0000FF");

    private final String rgb;

    CategoryColor(String rgb) {
        this.rgb = rgb;
    }
}
