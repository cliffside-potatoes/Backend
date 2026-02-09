package com.potatoes.Naengu.auth.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String providerId;

    private String email;

    private String role;

    public UserEntity(String providerId, String email, String role){
        this.providerId = providerId;
        this.email = email;
        this.role = role;
    }
}
