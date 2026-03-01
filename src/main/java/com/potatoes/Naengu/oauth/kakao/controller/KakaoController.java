package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import com.potatoes.Naengu.oauth.kakao.dto.LoginResponse;
import com.potatoes.Naengu.oauth.kakao.dto.LoginSuccessResponse;
import com.potatoes.Naengu.oauth.kakao.service.JwtTokenProvider;
import com.potatoes.Naengu.oauth.kakao.service.KakaoOAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;


@RequiredArgsConstructor
@RestController
public class KakaoController {

    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    //프론트에서 카카오 로그인 버튼을 눌렀을때 카카오 로그인 페이지로 이동
    @GetMapping("/login")
    public void redirectToKakao(HttpServletResponse response) throws IOException {

        String kakaoAuthUrl = kakaoOAuthService.buildKakaoLoginUrl();

        response.sendRedirect(kakaoAuthUrl);
    }


    //카카오에서 인가 코드를 전달 이후 프로세스
    @GetMapping("/oauth/kakao/auth-code")
    public void loginForm(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            @RequestParam(required = false) String state,
            HttpServletResponse response
    ) throws IOException {

        String authCode = code;
        KakaoTokenResponse tokenResponse = kakaoOAuthService.getAccessToken(authCode);
        Map<String, Object> userInfo = kakaoOAuthService.getUserInfo(tokenResponse.accessToken());

        LoginResponse kakaoUserResponse = kakaoOAuthService.kakaoUserLogin(userInfo);
        
        //System.out.println(" 사용자 신원 확인 테스트" + kakaoUserResponse.getToken().getRefreshToken());
        // RefreshToken 쿠키로 저장 (SameSite=None은 Java Cookie API 미지원으로 헤더 직접 설정)
        String cookieValue = String.format(
                "refreshToken=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=None; Secure", //잠깐 지우기
                kakaoUserResponse.getToken().getRefreshToken(),
                60 * 60 * 24 * 14
        );
        response.setHeader("Set-Cookie", cookieValue);

        // 302 리다이렉트로 프론트엔드로 이동

        String redirectUrl = frontendUrl + "/main";
        if(kakaoUserResponse.isNewMember()){
            //새로운 회원일 경우, 프로필로 리다이렉트
            redirectUrl = frontendUrl + "/new-info";
        }

        response.sendRedirect(redirectUrl);
    }

    // 프론트에서 access token을 받아가는 API
    @PostMapping("/oauth/token")
    public ResponseEntity<LoginSuccessResponse> getAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        // refresh token 검증 및 사용자 ID 추출
        jwtTokenProvider.validateToken(refreshToken);
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        // 새 access token 발급
        LoginSuccessResponse responseBody = kakaoOAuthService.generateAccessToken(memberId);

        return ResponseEntity.ok(responseBody);
    }


}
