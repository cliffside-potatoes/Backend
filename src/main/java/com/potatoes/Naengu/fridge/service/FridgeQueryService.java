package com.potatoes.Naengu.fridge.service;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import com.potatoes.Naengu.fridge.domain.model.FridgeIngredient;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.fridge.dto.query.FridgeCategoryResponse;
import com.potatoes.Naengu.fridge.dto.query.FridgeIngredientResponse;
import com.potatoes.Naengu.fridge.dto.query.FridgeOverviewResponse;
import com.potatoes.Naengu.fridge.dto.query.StorageSectionResponse;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FridgeQueryService {

    private final ProfileRepository profileRepository;
    private final FridgeCategoryRepository fridgeCategoryRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;

    public FridgeQueryService(ProfileRepository profileRepository, FridgeCategoryRepository fridgeCategoryRepository,
                              FridgeIngredientRepository fridgeIngredientRepository) {
        this.profileRepository = profileRepository;
        this.fridgeCategoryRepository = fridgeCategoryRepository;
        this.fridgeIngredientRepository = fridgeIngredientRepository;
    }


    @Transactional(readOnly = true)
    public FridgeOverviewResponse overview(Long userId) {

        Profile profile = loadProfile(userId);
        Fridge fridge = profile.getFridge();
        List<FridgeCategory> fridgeCategories = fridgeCategoryRepository.findAllByFridgeIdOrderByStorageTypeAscOrderIndexAsc(
                fridge.getId());

        List<FridgeIngredient> fridgeIngredients = fridgeIngredientRepository.findAllByFridgeCategoryIn(
                fridgeCategories);

        Map<Long, List<FridgeIngredientResponse>> ingredientsByCategoryId = fridgeIngredients.stream()
                .collect(Collectors.groupingBy(
                        fridgeIngredient -> fridgeIngredient.getFridgeCategory().getId(),
                        Collectors.mapping(
                                this::toIngredientResponse,
                                Collectors.toList()
                        )
                ));

        List<FridgeCategoryResponse> refrigeratedCategories = fridgeCategories.stream()
                .filter(category -> category.getStorageType() == StorageType.REFRIGERATED)
                .map(category -> toCategoryResponse(category, ingredientsByCategoryId))
                .toList();

        List<FridgeCategoryResponse> frozenCategories = fridgeCategories.stream()
                .filter(category -> category.getStorageType() == StorageType.FROZEN)
                .map(category -> toCategoryResponse(category, ingredientsByCategoryId))
                .toList();

        return new FridgeOverviewResponse(List.of(
                new StorageSectionResponse(StorageType.REFRIGERATED.getName(), refrigeratedCategories),
                new StorageSectionResponse(StorageType.FROZEN.getName(), frozenCategories)
        ));

    }

    private Profile loadProfile(Long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(PostErrorCode.PROFILE_NOT_FOUND));
    }

    private FridgeCategoryResponse toCategoryResponse(
            FridgeCategory category,
            Map<Long,List<FridgeIngredientResponse>> ingredientsByCategoryId
    ) {
        return new FridgeCategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getOrderIndex(),
                ingredientsByCategoryId.getOrDefault(category.getId(), List.of())
        );

    }

    private FridgeIngredientResponse toIngredientResponse(FridgeIngredient fridgeIngredient) {
        return new FridgeIngredientResponse(fridgeIngredient.getId(), fridgeIngredient.getIngredient().getName());
    }
}
