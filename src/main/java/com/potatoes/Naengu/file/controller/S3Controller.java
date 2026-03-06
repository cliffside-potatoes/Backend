package com.potatoes.Naengu.file.controller;

import com.potatoes.Naengu.file.dto.ImageRequestDTO;
import com.potatoes.Naengu.file.dto.PresignedUrlResponseDTO;
import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "File", description = "파일 업로드 API")
@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final FileUploadService fileUploadService;

    @Operation(summary = "Presigned URL 발급", description = "S3에 직접 업로드하기 위한 Presigned URL을 발급합니다. type: post, profile, recipe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 type"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/presigned/{type}")
    public ResponseEntity<Api<PresignedUrlResponseDTO>> createPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "업로드 대상 타입 (post, profile, recipe)", example = "post")
            @PathVariable String type,
            @RequestBody ImageRequestDTO imageDTO) {

        String path = switch (type) {
            case "post" -> "public/post";
            case "profile" -> "public/profile";
            case "recipe" -> "public/recipe";
            default -> throw new IllegalArgumentException("지원하지 않는 type: " + type);
        };

        PresignedUrlResponseDTO result = fileUploadService.getPreSignedUrl(path, imageDTO.getImageName());

        return ResponseEntity.ok(Api.success(result));
    }
}
