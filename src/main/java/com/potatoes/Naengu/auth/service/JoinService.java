package com.potatoes.Naengu.auth.service;

import com.potatoes.Naengu.auth.domain.model.UserEntity;
import com.potatoes.Naengu.auth.dto.JoinDTO;
import com.potatoes.Naengu.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final UserRepository userRepository;

    public JoinService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void joinProcess(JoinDTO joinDTO){
        String email = joinDTO.getEmail();
        String providerId = joinDTO.getProviderId();

        if(userRepository.existsByProviderId(providerId)) return;
        //String providerId, String email, String role
        UserEntity data = new UserEntity( providerId, email, "ROLE_USER");

        userRepository.save(data);

    }
}
