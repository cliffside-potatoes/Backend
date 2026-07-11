package com.potatoes.Naengu.fridge.domain.vo;

import lombok.Getter;

@Getter
public enum CategoryColor {
    COLOR_1("#90caf9"),
    COLOR_2("#ce93d8"),
    COLOR_3("#a5d6a7"),
    COLOR_4("#fff59d"),
    COLOR_5("#ffab91"),

    COLOR_6("#b0bec5"),
    COLOR_7("#80cbc4"),
    COLOR_8("#f48fb1"),
    COLOR_9("#e1bee7"),
    COLOR_10("#c5e1a5"),

    COLOR_11("#ffe082"),
    COLOR_12("#ffcc80"),
    COLOR_13("#b39ddb"),
    COLOR_14("#81d4fa"),
    COLOR_15("#f8bbd0"),

    COLOR_16("#d7ccc8"),
    COLOR_17("#cfd8dc"),
    COLOR_18("#ffcdd2"),
    COLOR_19("#c8e6c9"),
    COLOR_20("#bbdefb");

    private final String hex;

    CategoryColor(String hex) {
        this.hex = hex;
    }
}
