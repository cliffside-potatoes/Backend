package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import lombok.RequiredArgsConstructor;
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
    }

    //이거 service로 빼기

    private KakaoTokenResponse getAccessToken(String authCode) {
        final String KAKAO_CLIENT_ID = "my-client-id";
        final String KAKAO_CLIENT_SECRET = "my-client-secret";
        final String REDIRECT_URI = "http://localhost:8080/kakao/auth-code";
        // 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        // body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", KAKAO_CLIENT_ID);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("code", authCode);
        body.add("client_secret", KAKAO_CLIENT_SECRET);
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
