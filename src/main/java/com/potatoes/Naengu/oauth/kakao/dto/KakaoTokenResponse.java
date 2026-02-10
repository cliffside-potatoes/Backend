package com.potatoes.Naengu.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;


public record KakaoTokenResponse (
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("refresh_token_expires_in")
        Long refreshTokenExpiresIn
){
}
