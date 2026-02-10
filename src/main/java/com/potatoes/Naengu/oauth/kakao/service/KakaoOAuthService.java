package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import com.potatoes.Naengu.oauth.kakao.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class KakaoOAuthService {
    @Value("${kakao.oauth.client-id}")
    private String kakaoClientId;

    @Value("${kakao.oauth.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoRedirectUri;

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String BEARER_PREFIX = "Bearer ";

    public KakaoTokenResponse getAccessToken(String authCode) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE,
                "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", authCode);
        body.add("client_secret", kakaoClientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<KakaoTokenResponse> response =
                new RestTemplate().exchange(
                        TOKEN_URL,
                        HttpMethod.POST,
                        request,
                        KakaoTokenResponse.class
                );

        return response.getBody();
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);

        ResponseEntity<KakaoUserInfoResponse> response =
                new RestTemplate().exchange(
                        USER_INFO_URL,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        KakaoUserInfoResponse.class
                );

        return response.getBody();
    }
}
