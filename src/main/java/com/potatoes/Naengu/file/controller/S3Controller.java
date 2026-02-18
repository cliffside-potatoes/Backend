package com.potatoes.Naengu.file.controller;

import com.potatoes.Naengu.file.dto.ImageRequestDTO;
import com.potatoes.Naengu.file.dto.PresignedUrlResponseDTO;
import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final FileUploadService fileUploadService;

    /**
     * S3에게 pre-signed URL (권한) 요청하는 엔드포인트
     * 프론트에서 이 URL을 받아서 AWS S3에 직접 업로드함.
     *
     * @param imageDTO 파일 이름 정보를 담은 DTO
     * @return AWS S3에 업로드할 수 있는 Presigned URL
     */
    @PostMapping("/presigned/{type}")
    public ResponseEntity<PresignedUrlResponseDTO> createPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String type,
            @RequestBody ImageRequestDTO imageDTO) {

        // S3 내 저장될 폴더 경로
        String path = switch(type){
            case "post" -> "public/post";
            //아래에 더 추가하기
            default -> throw new IllegalArgumentException("지원하지 않는 type");

        };

        String s3Key = path + "/" + imageDTO.getImageName();  // 업로드될 S3 Key 생성

        PresignedUrlResponseDTO presignedUrl = fileUploadService.getPreSignedUrl(path,imageDTO.getImageName());

        return ResponseEntity.ok(presignedUrl);  // Presigned URL과 S3 Key 반환
    }




}
