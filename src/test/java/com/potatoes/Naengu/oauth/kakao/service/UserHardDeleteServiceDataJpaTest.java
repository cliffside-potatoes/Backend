package com.potatoes.Naengu.oauth.kakao.service;

import static com.potatoes.Naengu.oauth.kakao.exception.UserErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import com.potatoes.Naengu.fridge.domain.model.FridgeIngredient;
import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.fridge.repository.FridgeRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.global.exception.ErrorCode;
import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import com.potatoes.Naengu.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Sql(scripts = "classpath:hard-delete-test-tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@EntityScan("com.potatoes.Naengu")
@EnableJpaRepositories("com.potatoes.Naengu")
@Import(UserHardDeleteService.class)
class UserHardDeleteServiceDataJpaTest {

    @Autowired private UserHardDeleteService service;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private FridgeRepository fridgeRepository;
    @Autowired private FridgeCategoryRepository fridgeCategoryRepository;
    @Autowired private FridgeIngredientRepository fridgeIngredientRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private PostRepository postRepository;

    private static final long PROVIDER_ID = 12345L;

    private UserEntity savedUser() {
        UserEntity user = new UserEntity();
        user.setProviderId(PROVIDER_ID);
        user.setNickName("테스터");
        return userRepository.save(user);
    }

    private Fridge savedFridge() {
        return fridgeRepository.save(Fridge.crate());
    }

    private Profile savedProfile(UserEntity user, Fridge fridge) {
        return profileRepository.save(new Profile(user, "테스터", null, fridge));
    }

    private FridgeCategory savedCategory(Fridge fridge) {
        return fridgeCategoryRepository.save(
                FridgeCategory.create(fridge, "고기", 1, StorageType.REFRIGERATED, CategoryColor.COLOR_1)
        );
    }

    private Ingredient savedIngredient() {
        return ingredientRepository.save(Ingredient.of("닭가슴살"));
    }

    private FridgeIngredient savedFridgeIngredient(FridgeCategory category, Ingredient ingredient) {
        return fridgeIngredientRepository.save(FridgeIngredient.create(category, ingredient));
    }

    private Post savedPost(Profile profile) {
        return postRepository.save(new Post(profile, "테스트 게시글"));
    }

    @Test
    @DisplayName("hardDelete 호출 시 UserEntity가 hard deleted 처리된다")
    void hardDelete_success() {
        UserEntity user = savedUser();
        Fridge fridge = savedFridge();
        savedProfile(user, fridge);

        service.hardDelete(PROVIDER_ID);

        assertThat(userRepository.findByProviderId(PROVIDER_ID)).isEmpty();
    }

    @Test
    @DisplayName("hardDelete 호출 시 Profile이 soft deleted 처리된다")
    void hardDelete_deletes_profile() {
        UserEntity user = savedUser();
        Fridge fridge = savedFridge();
        savedProfile(user, fridge);

        service.hardDelete(PROVIDER_ID);

        assertThat(profileRepository.findByUserEntityProviderId(PROVIDER_ID)).isEmpty();
    }

    @Test
    @DisplayName("hardDelete 호출 시 FridgeCategory와 FridgeIngredient가 soft deleted 처리된다")
    void hardDelete_deletes_fridgeCategory_and_ingredient() {
        UserEntity user = savedUser();
        Fridge fridge = savedFridge();
        savedProfile(user, fridge);
        FridgeCategory category = savedCategory(fridge);
        Ingredient ingredient = savedIngredient();
        savedFridgeIngredient(category, ingredient);

        service.hardDelete(PROVIDER_ID);

        assertThat(fridgeCategoryRepository.findAllByFridge(fridge)).isEmpty();
        assertThat(fridgeIngredientRepository.findAllByFridgeCategory_Fridge(fridge)).isEmpty();
    }

    @Test
    @DisplayName("hardDelete 호출 시 Post가 soft deleted 처리된다")
    void hardDelete_deletes_post() {
        UserEntity user = savedUser();
        Fridge fridge = savedFridge();
        Profile profile = savedProfile(user, fridge);
        savedPost(profile);

        service.hardDelete(PROVIDER_ID);

        assertThat(postRepository.findAllByProfile(profile)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 providerId로 요청하면 USER_NOT_FOUND 예외가 발생한다")
    void hardDelete_user_not_found() {
        assertThatThrownBy(() -> service.hardDelete(9999L))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(USER_NOT_FOUND);
                    assertThat(code.status()).isEqualTo(USER_NOT_FOUND.status());
                    assertThat(code.message()).isEqualTo(USER_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("Profile이 없는 사용자도 UserEntity hard delete가 정상 처리된다")
    void hardDelete_without_profile() {
        savedUser();

        service.hardDelete(PROVIDER_ID);

        assertThat(userRepository.findByProviderId(PROVIDER_ID)).isEmpty();
    }

    @Test
    @DisplayName("이미 탈퇴한(soft deleted) 사용자도 완전 삭제 요청 시 정상 처리된다")
    void hardDelete_already_withdrawn() {
        UserEntity user = savedUser();
        Fridge fridge = savedFridge();
        savedProfile(user, fridge);
        // 먼저 탈퇴 처리
        user.withdraw();
        userRepository.save(user);

        // findByProviderId로 soft deleted 사용자도 조회 가능
        service.hardDelete(PROVIDER_ID);

        assertThat(userRepository.findByProviderId(PROVIDER_ID)).isEmpty();
        assertThat(profileRepository.findByUserEntityProviderId(PROVIDER_ID)).isEmpty();
    }
}
