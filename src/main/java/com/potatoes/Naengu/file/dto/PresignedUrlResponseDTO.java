package com.potatoes.Naengu.file.dto;

import lombok.Getter;

@Getter
public class PresignedUrlResponseDTO {
    private final String presignedUrl;
    private final String s3Key;

    public PresignedUrlResponseDTO(String presignedUrl, String s3Key) {
        this.presignedUrl = presignedUrl;
        this.s3Key = s3Key;
    }
}
