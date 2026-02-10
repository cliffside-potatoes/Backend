package com.potatoes.Naengu.oauth.kakao.repository;


import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

}
