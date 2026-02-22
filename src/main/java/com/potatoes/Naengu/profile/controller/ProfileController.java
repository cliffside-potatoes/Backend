package com.potatoes.Naengu.profile.controller;

import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.profile.dto.ProfileUpsertRequest;
import com.potatoes.Naengu.profile.dto.ProfileUpsertResponse;
import com.potatoes.Naengu.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/profiles")
    public ResponseEntity<ProfileUpsertResponse> upsert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpsertRequest req
    ){
        String userId = userDetails.getUsername();
        ProfileUpsertResponse upsert = profileService.upsert(Long.parseLong(userId), req);

        return ResponseEntity.ok(upsert);
    }

}
