package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.exception.UserErrorCode;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserHardDeleteService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final FridgeCategoryRepository fridgeCategoryRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;

    public void hardDelete(Long providerId) {
        // soft deleted 포함 조회 — 이미 탈퇴한 사용자도 완전 삭제 가능
        UserEntity user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));

        profileRepository.findByUserEntityProviderId(providerId).ifPresent(profile -> {
            Fridge fridge = profile.getFridge();

            // FridgeIngredient soft delete (@SoftDelete → UPDATE deleted=true)
            fridgeIngredientRepository.deleteAll(
                    fridgeIngredientRepository.findAllByFridgeCategory_Fridge(fridge)
            );

            // FridgeCategory soft delete (@SoftDelete → UPDATE deleted=true)
            fridgeCategoryRepository.deleteAll(
                    fridgeCategoryRepository.findAllByFridge(fridge)
            );

            // Post soft delete (@SoftDelete → UPDATE deleted=true)
            postRepository.deleteAll(
                    postRepository.findAllByProfile(profile)
            );

            // Profile soft delete (@SoftDelete → UPDATE deleted=true)
            profileRepository.delete(profile);
        });

        // UserEntity soft delete
        // Profile이 user_id FK로 물리적 행을 유지하므로 물리 삭제 시 FK 오류 발생
        // 실제 물리 삭제는 배치 작업에서 Profile 정리 후 처리
        user.withdraw();
    }
}
