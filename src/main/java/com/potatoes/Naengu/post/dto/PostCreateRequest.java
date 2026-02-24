package com.potatoes.Naengu.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest (
        @NotBlank
        @Size(min = 1, max = 500)
        String content,

        @Valid
        @Size(max = 5, message = "images는 최대 5개까지 가능합니다.")
        List<PostImageRequest> images
){
}
