package com.potatoes.Naengu.auth.service;

import com.potatoes.Naengu.auth.token.TokenReader;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TokenReader tokenReader;
    private final ProfileRepository profileRepository;


    public AuthService(
            @Qualifier("jwtTokenProvider") TokenReader tokenReader,
            ProfileRepository profileRepository)
    {
        this.tokenReader = tokenReader;
        this.profileRepository = profileRepository;
    }

    public Long resolveFridgeId(HttpServletRequest request) {
        String token = tokenReader.resolveBearerToken(request);

        if (token == null) {
            throw new RuntimeException("Missing token");
        }

        Long providerId = tokenReader.extractSubjectAsLong(token);

        Profile profile = profileRepository.findByUserEntityProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return profile.getFridge().getId();

    }

}
