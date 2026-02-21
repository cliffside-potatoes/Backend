package com.potatoes.Naengu.oauth.kakao.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String nickname;
    private AuthTokens token;
    private boolean isNewMember;

    public LoginResponse(Long id, String nickname, AuthTokens token,boolean isNewMember) {
        this.id = id;
        this.nickname = nickname;
        this.token = token;
        this.isNewMember = isNewMember;
    }
}