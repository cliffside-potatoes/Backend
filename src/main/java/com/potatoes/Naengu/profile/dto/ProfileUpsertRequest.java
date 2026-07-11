package com.potatoes.Naengu.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpsertRequest (
        @Schema(example = "chulsoo")
        @NotBlank
        @Size(min = 1, max = 20)
        String nickname,

        @Schema(example = "안녕하세요. 철수입니다.")
        @Size(max = 150)
        String bio,

        @Valid
        ProfileImageRequest profileImage

){
}
