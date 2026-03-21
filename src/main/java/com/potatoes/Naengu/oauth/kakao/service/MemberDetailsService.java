package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public MemberDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String providerId) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByProviderIdAndDeletedFalse(Long.valueOf(providerId))
                .orElseThrow(() -> new UsernameNotFoundException("회원 없음"));

        return new CustomUserDetails(user);
    }
}
