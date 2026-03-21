package com.potatoes.Naengu.reviewrecipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReviewImage;
import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewCursor;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewFeedResponse;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewImageRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeReviewQueryServiceTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeReviewRepository recipeReviewRepository;
    @Mock private RecipeReviewImageRepository recipeReviewImageRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private FileUploadService fileUploadService;

    @InjectMocks private RecipeReviewQueryService service;

    private static final long PROVIDER_ID = 12345L;
    private static final long RECIPE_ID = 1L;

    private Profile fakeProfile() {
        UserEntity user = new UserEntity();
        user.setProviderId(PROVIDER_ID);
        // Profile 생성자는 protected이므로 mock 사용
        Profile profile = org.mockito.Mockito.mock(Profile.class);
        given(profile.getId()).willReturn(10L);
        given(profile.getNickname()).willReturn("테스터");
        given(profile.getProfileImage()).willReturn(null); // 프로필 이미지 없음
        return profile;
    }

    private RecipeReview fakeReview(Profile profile) {
        RecipeReview review = org.mockito.Mockito.mock(RecipeReview.class);
        given(review.getId()).willReturn(100L);
        given(review.getProfile()).willReturn(profile);
        given(review.getContent()).willReturn("맛있어요!");
        given(review.getUpdatedAt()).willReturn(LocalDateTime.now());
        given(review.isHideLikeCount()).willReturn(false);
        given(review.getLikeCount()).willReturn(5);
        return review;
    }

    @Test
    @DisplayName("존재하지 않는 recipeId로 요청하면 RECIPE_NOT_FOUND 예외가 발생한다")
    void getFeed_존재하지않는_recipeId_예외() {
        // given
        given(recipeRepository.existsById(RECIPE_ID)).willReturn(false);

        RecipeReviewCursor cursor = new RecipeReviewCursor(null, null);

        // when & then
        assertThatThrownBy(() ->
                service.getFeed(PROVIDER_ID, 20, RECIPE_ID, ReviewSortType.LATEST, cursor)
        )
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(RecipeErrorCode.RECIPE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("존재하지 않는 userId로 요청하면 PROFILE_NOT_FOUND 예외가 발생한다")
    void getFeed_존재하지않는_userId_예외() {
        // given
        given(recipeRepository.existsById(RECIPE_ID)).willReturn(true);
        given(profileRepository.findByUserEntityProviderId(PROVIDER_ID)).willReturn(Optional.empty());

        RecipeReviewCursor cursor = new RecipeReviewCursor(null, null);

        // when & then
        assertThatThrownBy(() ->
                service.getFeed(PROVIDER_ID, 20, RECIPE_ID, ReviewSortType.LATEST, cursor)
        )
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(PostErrorCode.PROFILE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("리뷰가 없을 때 totalCount=0, 빈 items 리스트를 반환한다")
    void getFeed_리뷰_없을때_빈_목록_반환() {
        // given
        Profile profile = fakeProfile();
        given(recipeRepository.existsById(RECIPE_ID)).willReturn(true);
        given(profileRepository.findByUserEntityProviderId(PROVIDER_ID)).willReturn(Optional.of(profile));
        given(recipeReviewRepository.findByRecipeIdOrderByUpdatedAtDescIdDesc(anyLong(), any(Pageable.class)))
                .willReturn(List.of());
        given(recipeReviewRepository.countByRecipeId(RECIPE_ID)).willReturn(0L);

        RecipeReviewCursor cursor = new RecipeReviewCursor(null, null);

        // when
        RecipeReviewFeedResponse response = service.getFeed(PROVIDER_ID, 20, RECIPE_ID, ReviewSortType.LATEST, cursor);

        // then
        assertThat(response.totalCount()).isEqualTo(0);
        assertThat(response.items()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("리뷰가 있을 때 totalCount, items, 이미지 URL이 올바르게 반환된다")
    void getFeed_리뷰_있을때_정상_반환() {
        // given
        Profile profile = fakeProfile();
        RecipeReview review = fakeReview(profile);

        given(recipeRepository.existsById(RECIPE_ID)).willReturn(true);
        given(profileRepository.findByUserEntityProviderId(PROVIDER_ID)).willReturn(Optional.of(profile));
        given(recipeReviewRepository.findByRecipeIdOrderByUpdatedAtDescIdDesc(anyLong(), any(Pageable.class)))
                .willReturn(List.of(review));
        given(recipeReviewRepository.countByRecipeId(RECIPE_ID)).willReturn(1L);

        RecipeReviewImage image = org.mockito.Mockito.mock(RecipeReviewImage.class);
        given(image.getS3Key()).willReturn("review/img.jpg");
        given(recipeReviewImageRepository.findAllByRecipeReviewId(100L)).willReturn(List.of(image));

        given(fileUploadService.getPublicUrl("review/img.jpg")).willReturn("https://test.com/review/img.jpg");
        given(fileUploadService.getDefaultProfileImageUrl()).willReturn("https://test.com/default.jpg");

        RecipeReviewCursor cursor = new RecipeReviewCursor(null, null);

        // when
        RecipeReviewFeedResponse response = service.getFeed(PROVIDER_ID, 20, RECIPE_ID, ReviewSortType.LATEST, cursor);

        // then
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).content()).isEqualTo("맛있어요!");
        assertThat(response.items().get(0).recipeReviewImageUrls()).containsExactly("https://test.com/review/img.jpg");
        assertThat(response.items().get(0).profileImageUrl()).isEqualTo("https://test.com/default.jpg");
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("size+1개 리뷰가 있으면 hasNext=true이고 nextCursor가 반환된다")
    void getFeed_다음_페이지_있을때_hasNext_true() {
        // given
        Profile profile = fakeProfile();
        int requestSize = 2;

        // size+1 = 3개 반환 → hasNext true
        RecipeReview review1 = fakeReview(profile);
        RecipeReview review2 = org.mockito.Mockito.mock(RecipeReview.class);
        RecipeReview review3 = org.mockito.Mockito.mock(RecipeReview.class);
        given(review2.getId()).willReturn(101L);
        given(review2.getProfile()).willReturn(profile);
        given(review2.getContent()).willReturn("두번째 리뷰");
        given(review2.getUpdatedAt()).willReturn(LocalDateTime.now().minusMinutes(1));
        given(review2.isHideLikeCount()).willReturn(false);
        given(review2.getLikeCount()).willReturn(0);
        given(review3.getId()).willReturn(102L);

        given(recipeRepository.existsById(RECIPE_ID)).willReturn(true);
        given(profileRepository.findByUserEntityProviderId(PROVIDER_ID)).willReturn(Optional.of(profile));
        given(recipeReviewRepository.findByRecipeIdOrderByUpdatedAtDescIdDesc(anyLong(), any(Pageable.class)))
                .willReturn(List.of(review1, review2, review3)); // size+1개
        given(recipeReviewRepository.countByRecipeId(RECIPE_ID)).willReturn(10L);
        given(recipeReviewImageRepository.findAllByRecipeReviewId(anyLong())).willReturn(List.of());
        given(fileUploadService.getDefaultProfileImageUrl()).willReturn("https://test.com/default.jpg");

        RecipeReviewCursor cursor = new RecipeReviewCursor(null, null);

        // when
        RecipeReviewFeedResponse response = service.getFeed(PROVIDER_ID, requestSize, RECIPE_ID, ReviewSortType.LATEST, cursor);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.items()).hasSize(requestSize); // size개만 반환
        assertThat(response.nextCursor()).isNotNull();
    }
}
