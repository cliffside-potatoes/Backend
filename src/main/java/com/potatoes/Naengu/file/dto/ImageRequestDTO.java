package com.potatoes.Naengu.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestDTO {
    @Schema(description = "업로드할 파일명", example = "photo.jpg")
    private String imageName;
}
