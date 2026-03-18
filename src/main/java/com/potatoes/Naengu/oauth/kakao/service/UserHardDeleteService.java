package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.exception.UserErrorCode;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import com.potatoes.Naengu.post.repository.PostImageRepository;
import com.potatoes.Naengu.post.repository.PostRepository;
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
    private final PostImageRepository postImageRepository;
    private final FridgeCategoryRepository fridgeCategoryRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;

    public void hardDelete(Long providerId) {
        UserEntity user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));

        // FridgeIngredient/Category soft delete: @SoftDelete 필터로 soft-deleted Profile은 조회 안 됨
        // 이미 이전 시도에서 처리됐을 수 있으므로 best-effort
        profileRepository.findByUserEntityProviderId(providerId).ifPresent(profile -> {
            Fridge fridge = profile.getFridge();
            fridgeIngredientRepository.deleteAll(
                    fridgeIngredientRepository.findAllByFridgeCategory_Fridge(fridge)
            );
            fridgeCategoryRepository.deleteAll(
                    fridgeCategoryRepository.findAllByFridge(fridge)
            );
        });

        // PostImage → Post → RecipeReview → Profile 순서로 hard delete
        // native SQL로 @SoftDelete 필터 완전 우회 — soft-deleted 행도 포함해서 물리 삭제
        postImageRepository.hardDeleteAllByProviderId(providerId);
        postRepository.hardDeleteAllByProviderId(providerId);
        profileRepository.hardDeleteRecipeReviewImagesByProviderId(providerId);
        profileRepository.hardDeleteRecipeReviewsByProviderId(providerId);
        profileRepository.hardDeleteByProviderId(providerId);

        // UserEntity hard delete
        userRepository.delete(user);
    }
}
