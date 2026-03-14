package com.potatoes.Naengu.post.domain.vo;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.exception.PostErrorCode;

import java.util.Arrays;

public enum SortType {
    LATEST;

    public static SortType from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ApiException(PostErrorCode.INVALID_SORT_TYPE));
    }
}
