package com.potatoes.Naengu.profile.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.profile.dto.ProfileGetResponse;
import com.potatoes.Naengu.profile.dto.ProfileUpsertRequest;
import com.potatoes.Naengu.profile.dto.ProfileUpsertResponse;
import com.potatoes.Naengu.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/profiles")
    public ResponseEntity<Api<ProfileUpsertResponse>> upsert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpsertRequest req
    ){
        String userId = userDetails.getUsername();
        ProfileUpsertResponse result = profileService.upsert(Long.parseLong(userId), req);

        HttpStatus status = result.isNew() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(Api.success(result));
    }

    @GetMapping("/profiles")
    public ResponseEntity<Api<ProfileGetResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ProfileGetResponse result = profileService.getProfile(userId);
        return ResponseEntity.ok(Api.success(result));
    }

}
