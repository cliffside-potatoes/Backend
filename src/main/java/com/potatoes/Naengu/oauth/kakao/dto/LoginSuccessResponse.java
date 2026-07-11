package com.potatoes.Naengu.oauth.kakao.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginSuccessResponse {
    private Long id;
    private String nickname;
    private String accessToken;
    long expiresIn;
    private boolean isNewMember;
    String grantType;

    public LoginSuccessResponse(Long id,
                                String nickname,
                                String grantType,
                                String accessToken,
                                long expiresIn,
                                boolean isNewMember
    ) {
        this.id = id;
        this.nickname = nickname;
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.isNewMember = isNewMember;
    }
}
