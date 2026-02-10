package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@RestController
public class KakaoController {

    @GetMapping("/oauth/kakao/auth-code")
    public void loginForm(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            @RequestParam(required = false) String state
    ){
        String authCode = code;
        KakaoTokenResponse tokenResponse = getAccessToken(authCode);

        System.out.println("Kakao token response2 = " + tokenResponse);
    }

    //service로 추후 분리
    @Value("${kakao.oauth.client-id}")
    private String kakaoClientId;

    @Value("${kakao.oauth.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoRedirectUri;



    private KakaoTokenResponse getAccessToken(String authCode) {

        // 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        // body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", authCode);
        body.add("client_secret", kakaoClientSecret);
        // Http요청 객체
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        // Kakao API 호출
        ResponseEntity<KakaoTokenResponse> response =
                new RestTemplate().exchange(
                        "https://kauth.kakao.com/oauth/token",
                        HttpMethod.POST,
                        httpEntity,
                        KakaoTokenResponse.class);



        return response.getBody();
    }
}
