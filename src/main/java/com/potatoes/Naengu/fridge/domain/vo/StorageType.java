package com.potatoes.Naengu.fridge.domain.vo;

import lombok.Getter;

@Getter
public enum StorageType {
    REFRIGERATED("냉장"),
    FROZEN("냉동");

    private final String name;

    StorageType(String name) {
        this.name = name;
    }


}
