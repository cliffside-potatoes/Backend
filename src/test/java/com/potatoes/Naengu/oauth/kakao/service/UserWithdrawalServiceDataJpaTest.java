package com.potatoes.Naengu.oauth.kakao.service;

import static com.potatoes.Naengu.oauth.kakao.exception.UserErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.global.exception.ErrorCode;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@EntityScan("com.potatoes.Naengu")
@EnableJpaRepositories("com.potatoes.Naengu")
@Import(UserWithdrawalService.class)
class UserWithdrawalServiceDataJpaTest {

    @Autowired
    private UserWithdrawalService service;

    @Autowired
    private UserRepository userRepository;

    private static final long PROVIDER_ID = 12345L;

    private UserEntity savedUser() {
        UserEntity user = new UserEntity();
        user.setProviderId(PROVIDER_ID);
        user.setNickName("테스터");
        return userRepository.save(user);
    }

    @Test
    @DisplayName("정상 탈퇴 시 deleted가 true로 변경된다")
    void withdraw_success() {
        savedUser();

        service.withdraw(PROVIDER_ID);

        UserEntity updated = userRepository.findByProviderId(PROVIDER_ID).orElseThrow();
        assertThat(updated.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 providerId로 탈퇴하면 USER_NOT_FOUND 예외가 발생한다")
    void withdraw_not_found_throws() {
        assertThatThrownBy(() -> service.withdraw(9999L))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(USER_NOT_FOUND);
                    assertThat(code.status()).isEqualTo(USER_NOT_FOUND.status());
                    assertThat(code.message()).isEqualTo(USER_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("이미 탈퇴한 사용자로 탈퇴 요청하면 USER_NOT_FOUND 예외가 발생한다")
    void withdraw_already_deleted_throws() {
        UserEntity user = savedUser();
        user.withdraw();
        userRepository.save(user);

        assertThatThrownBy(() -> service.withdraw(PROVIDER_ID))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(USER_NOT_FOUND);
                });
    }
}
