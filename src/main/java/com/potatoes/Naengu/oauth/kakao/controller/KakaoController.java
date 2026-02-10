package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import com.potatoes.Naengu.oauth.kakao.dto.KakaoUserInfoResponse;
import com.potatoes.Naengu.oauth.kakao.service.KakaoOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RequiredArgsConstructor
@RestController
public class KakaoController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/oauth/kakao/auth-code")
    public void loginForm(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            @RequestParam(required = false) String state
    ){
        String authCode = code;
        KakaoTokenResponse tokenResponse = kakaoOAuthService.getAccessToken(authCode);
        // 사용자 정보 응답
        Map<String, Object> userInfo = kakaoOAuthService.getUserInfo(tokenResponse.accessToken());

        System.out.println("디버깅중 >> "+ userInfo);
    }


}
