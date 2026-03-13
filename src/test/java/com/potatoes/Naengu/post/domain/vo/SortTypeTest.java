package com.potatoes.Naengu.post.domain.vo;

import static com.potatoes.Naengu.post.exception.PostErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SortTypeTest {

    @Test
    @DisplayName("LATEST 문자열로 SortType을 가져올 수 있다")
    void from_latest_uppercase_returns_latest() {
        assertThat(SortType.from("LATEST")).isEqualTo(SortType.LATEST);
    }

    @Test
    @DisplayName("소문자 latest로도 SortType을 가져올 수 있다")
    void from_latest_lowercase_returns_latest() {
        assertThat(SortType.from("latest")).isEqualTo(SortType.LATEST);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 방식이면 INVALID_SORT_TYPE 예외가 발생한다")
    void from_invalid_value_throws() {
        assertThatThrownBy(() -> SortType.from("POPULAR"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_SORT_TYPE);
                    assertThat(code.status()).isEqualTo(INVALID_SORT_TYPE.status());
                    assertThat(code.message()).isEqualTo(INVALID_SORT_TYPE.message());
                });
    }
}
