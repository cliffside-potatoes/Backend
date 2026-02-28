package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import com.potatoes.Naengu.oauth.kakao.dto.LoginResponse;
import com.potatoes.Naengu.oauth.kakao.dto.LoginSuccessResponse;
import com.potatoes.Naengu.oauth.kakao.service.KakaoOAuthDevService;
import com.potatoes.Naengu.oauth.kakao.service.KakaoOAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/dev")
public class KakaoDevController {
    private final KakaoOAuthDevService kakaoOAuthService;

    //프론트에서 카카오 로그인 버튼을 눌렀을때 카카오 로그인 페이지로 이동
    @GetMapping("/login")
    public void redirectToKakao(HttpServletResponse response) throws IOException {

        String kakaoAuthUrl = kakaoOAuthService.buildKakaoLoginUrl();
        System.out.println("확인중입니다.");
        response.sendRedirect(kakaoAuthUrl);
    }


    //카카오에서 인가 코드를 전달 이후 프로세스
    @GetMapping("/oauth/kakao/auth-code")
    public ResponseEntity<LoginSuccessResponse> loginForm(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            @RequestParam(required = false) String state,
            HttpServletResponse response
    ){

        //이부분 추후 서비스 로직으로 빼기
        String authCode = code;
        KakaoTokenResponse tokenResponse = kakaoOAuthService.getAccessToken(authCode);
        // 사용자 정보 응답
        Map<String, Object> userInfo = kakaoOAuthService.getUserInfo(tokenResponse.accessToken());

        //3. 카카오ID로 회원가입 & 로그인 처리
        LoginResponse kakaoUserResponse= kakaoOAuthService.kakaoUserLogin(userInfo);

        // 🔐 RefreshToken 쿠키로 저장
        Cookie refreshCookie = new Cookie("refreshToken",
                kakaoUserResponse.getToken().getRefreshToken());

        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/"); // `/` 이하 경로에서 쿠키사용 가능
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
        response.addCookie(refreshCookie);

        //AccessToken만 body에 담아서 반환
        LoginSuccessResponse responseBody =
                new LoginSuccessResponse(
                        kakaoUserResponse.getId(),
                        kakaoUserResponse.getNickname(),
                        "Bearer",
                        kakaoUserResponse.getToken().getAccessToken(),
                        3600L,
                        kakaoUserResponse.isNewMember()
                );


        return ResponseEntity.ok(responseBody);

    }
}
