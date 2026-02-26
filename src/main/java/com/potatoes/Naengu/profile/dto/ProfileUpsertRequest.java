package com.potatoes.Naengu.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpsertRequest (
        @NotBlank
        @Size(min = 1, max = 20)
        String nickname,

        @Size(max = 150)
        String bio,

        @Valid
        ProfileImageRequest profileImage

){
}
