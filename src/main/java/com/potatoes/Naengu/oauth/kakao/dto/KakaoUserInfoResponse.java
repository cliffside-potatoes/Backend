package com.potatoes.Naengu.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record KakaoUserInfoResponse (
        @JsonProperty("id")
        Long id,

        @JsonProperty("connected_at")
        String connectedAt,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount,

        @JsonProperty("properties")
        Map<String, Object> properties
){
    public record KakaoAccount(
            @JsonProperty("profile_nickname_needs_agreement")
            Boolean profileNicknameNeedsAgreement,

            @JsonProperty("profile_image_needs_agreement")
            Boolean profileImageNeedsAgreement,

            @JsonProperty("profile")
            Profile profile
    ) {

        public record Profile(
                @JsonProperty("nickname")
                String nickname,

                @JsonProperty("thumbnail_image_url")
                String thumbnailImageUrl,

                @JsonProperty("profile_image_url")
                String profileImageUrl,

                @JsonProperty("is_default_image")
                Boolean isDefaultImage,

                @JsonProperty("is_default_nickname")
                Boolean isDefaultNickname
        ) {
        }
    }
}
