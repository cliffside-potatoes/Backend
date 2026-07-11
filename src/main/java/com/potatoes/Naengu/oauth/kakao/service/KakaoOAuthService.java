package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.dto.AuthTokens;
import com.potatoes.Naengu.oauth.kakao.dto.KakaoTokenResponse;
import com.potatoes.Naengu.oauth.kakao.dto.KakaoUserInfoResponse;
import com.potatoes.Naengu.oauth.kakao.dto.LoginResponse;
import com.potatoes.Naengu.oauth.kakao.dto.LoginSuccessResponse;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class KakaoOAuthService {
    @Value("${kakao.oauth.client-id}")
    private String kakaoClientId;

    @Value("${kakao.oauth.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoRedirectUri;

    private final UserRepository userRepository;
    private final AuthTokensGenerator authTokensGenerator;

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

    public Map<String, Object> getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);

        ResponseEntity<KakaoUserInfoResponse> response =
                new RestTemplate().exchange(
                        USER_INFO_URL,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        KakaoUserInfoResponse.class
                );

        Map<String, Object> userInfo = extractUserInfoAsMap(response.getBody());


        return userInfo;
    }

    public Map<String, Object> extractUserInfoAsMap(KakaoUserInfoResponse kakao) {

        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("id", kakao.id());

        // 1) id (항상 핵심 식별자)
        userInfo.put("id", kakao.id());

        // 2) nickname (kakao_account.profile.nickname 우선, 없으면 properties.nickname fallback)
        String nickname =
                Optional.ofNullable(kakao.kakaoAccount())
                        .map(KakaoUserInfoResponse.KakaoAccount::profile)
                        .map(KakaoUserInfoResponse.KakaoAccount.Profile::nickname)
                        .orElseGet(() -> {
                            if (kakao.properties() == null) return null;
                            Object n = kakao.properties().get("nickname");
                            return n != null ? String.valueOf(n) : null;
                        });

        userInfo.put("nickname", nickname);

        return userInfo;
    }



    //3. 카카오ID로 회원가입 & 로그인 처리
    public LoginResponse kakaoUserLogin(Map<String, Object> userInfo){

        Long providerId= Long.valueOf(userInfo.get("id").toString());
        String nickName = userInfo.get("nickname").toString();

        UserEntity kakaoUser = userRepository.findByProviderId(providerId).orElse(null);

        boolean isNewMember = false;
        if (kakaoUser == null) {    //회원가입
            isNewMember = true;
            kakaoUser= new UserEntity();
            kakaoUser.setProviderId(providerId);
            kakaoUser.setNickName(nickName);
            userRepository.save(kakaoUser);
        } else if (kakaoUser.isDeleted()) {    //완전삭제 후 재가입
            isNewMember = true;
            kakaoUser.setDeleted(false);
            userRepository.save(kakaoUser);
        }

        //토큰 생성
        AuthTokens token=authTokensGenerator.generate(providerId.toString());

        return new LoginResponse(providerId,nickName,token,isNewMember);
        //return null;
    }

    public String buildKakaoLoginUrl() {

        return "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri;
    }

    // refresh token으로 새 access token 발급
    public LoginSuccessResponse generateAccessToken(Long providerId) {
        UserEntity user = userRepository.findByProviderIdAndDeletedFalse(providerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthTokens token = authTokensGenerator.generate(providerId.toString());

        return new LoginSuccessResponse(
                user.getProviderId(),
                user.getNickName(),
                "Bearer",
                token.getAccessToken(),
                token.getExpiresIn(),
                false
        );
    }

}
