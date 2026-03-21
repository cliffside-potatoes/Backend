package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.exception.UserErrorCode;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;

    public void withdraw(Long providerId) {
        UserEntity user = userRepository.findByProviderIdAndDeletedFalse(providerId)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));

        user.withdraw();
    }
}
