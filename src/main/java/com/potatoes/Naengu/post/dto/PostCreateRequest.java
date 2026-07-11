package com.potatoes.Naengu.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest (
        @Schema(example = "오늘 만든 된장찌개 레시피 공유합니다!")
        @NotBlank
        @Size(min = 1, max = 500)
        String content,

        @Valid
        @Size(max = 5, message = "images는 최대 5개까지 가능합니다.")
        List<PostImageRequest> images
){
}
