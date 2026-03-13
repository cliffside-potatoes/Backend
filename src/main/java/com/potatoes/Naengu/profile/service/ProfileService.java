package com.potatoes.Naengu.profile.service;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.dto.ProfileGetResponse;
import com.potatoes.Naengu.profile.dto.ProfileUpsertRequest;
import com.potatoes.Naengu.profile.dto.ProfileUpsertResponse;
import com.potatoes.Naengu.profile.exception.ProfileErrorCode;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public ProfileUpsertResponse upsert(Long userId, ProfileUpsertRequest req){
        //profile.userEntity.providerId
        Profile profile = profileRepository.findByUserEntityProviderId(userId).orElse(null);

        if(profile == null){
            //생성 (Insert)
            UserEntity user = userRepository.findByProviderId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

            Fridge fridge = Fridge.crate();
            Profile created = new Profile(user, req.nickname(), req.bio(), fridge);
            created.upsertProfileImageIfPresent(req.profileImage());
            Profile saved = profileRepository.save(created);

            return new ProfileUpsertResponse(saved.getId(), true);

        }

        //수정 (update)
        profile.updateNickname(req.nickname());
        profile.updateBioIfPresent(req.bio());
        profile.upsertProfileImageIfPresent(req.profileImage());

        return new ProfileUpsertResponse(profile.getId(), false);
    }

    @Transactional(readOnly = true)
    public ProfileGetResponse getProfile(Long userId) {
        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(ProfileErrorCode.PROFILE_NOT_FOUND));

        String imageUrl = profile.getProfileImage() != null
                ? fileUploadService.getPublicUrl(profile.getProfileImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        return ProfileGetResponse.from(profile, imageUrl);
    }
}
