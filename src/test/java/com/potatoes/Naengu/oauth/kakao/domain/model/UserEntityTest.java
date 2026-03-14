package com.potatoes.Naengu.oauth.kakao.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    @DisplayName("기본 생성 시 deleted는 false다")
    void default_deleted_is_false() {
        UserEntity user = new UserEntity();

        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("withdraw() 호출 시 deleted가 true로 변경된다")
    void withdraw_sets_deleted_true() {
        UserEntity user = new UserEntity();
        user.setProviderId(12345L);

        user.withdraw();

        assertThat(user.isDeleted()).isTrue();
    }
}
