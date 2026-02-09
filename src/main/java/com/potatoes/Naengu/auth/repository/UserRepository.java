package com.potatoes.Naengu.auth.repository;

import com.potatoes.Naengu.auth.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

}
