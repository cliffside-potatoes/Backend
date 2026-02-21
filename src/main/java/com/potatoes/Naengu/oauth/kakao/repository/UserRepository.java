package com.potatoes.Naengu.oauth.kakao.repository;


import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByProviderId(Long providerId);
}
