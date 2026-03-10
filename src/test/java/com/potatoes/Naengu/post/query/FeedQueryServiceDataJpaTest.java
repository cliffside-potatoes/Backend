package com.potatoes.Naengu.post.query;

import static com.potatoes.Naengu.post.exception.PostErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.repository.FridgeRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.global.exception.ErrorCode;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.PostImage;
import com.potatoes.Naengu.post.dto.FeedQueryRequest;
import com.potatoes.Naengu.post.dto.FeedResponse;
import com.potatoes.Naengu.post.dto.MyFeedResponse;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@EntityScan("com.potatoes.Naengu")
@EnableJpaRepositories("com.potatoes.Naengu")
@Import(FeedQueryService.class)
class FeedQueryServiceDataJpaTest {

    @Autowired
    private FeedQueryService feedQueryService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FridgeRepository fridgeRepository;

    @MockitoBean
    private FileUploadService fileUploadService;

    @BeforeEach
    void setUp() {
        when(fileUploadService.getPublicUrl(any())).thenReturn("https://test.s3.com/image.png");
        when(fileUploadService.getDefaultProfileImageUrl()).thenReturn("https://test.s3.com/default.png");
    }

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────

    private UserEntity savedUser(Long providerId) {
        UserEntity user = new UserEntity();
        user.setProviderId(providerId);
        return userRepository.save(user);
    }

    private Profile savedProfile(UserEntity user, String nickname) {
        Fridge fridge = fridgeRepository.save(Fridge.crate());
        Profile profile = new Profile(user, nickname, null, fridge);
        return profileRepository.save(profile);
    }

    private Post savedPost(Profile profile, String content) {
        return postRepository.save(new Post(profile, content));
    }

    // ── 성공 케이스 ───────────────────────────────────────────────

    @Test
    @DisplayName("최초 진입 시 커서 없이 요청하면 최신 게시글 목록을 반환한다")
    void getFeed_first_page_no_cursor_returns_items() {
        Profile profile = savedProfile(savedUser(1L), "작성자");
        savedPost(profile, "첫 번째 게시글");
        savedPost(profile, "두 번째 게시글");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("조회 결과가 size보다 많으면 hasNext가 true이고 nextCursor가 존재한다")
    void getFeed_has_next_true_when_more_exists() {
        Profile profile = savedProfile(savedUser(1L), "작성자");
        savedPost(profile, "첫 번째");
        savedPost(profile, "두 번째");
        savedPost(profile, "세 번째");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(2, null, null, "LATEST"));

        assertThat(response.items()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("마지막 페이지이면 hasNext가 false이고 nextCursor가 null이다")
    void getFeed_has_next_false_on_last_page() {
        Profile profile = savedProfile(savedUser(1L), "작성자");
        savedPost(profile, "첫 번째");
        savedPost(profile, "두 번째");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(3, null, null, "LATEST"));

        assertThat(response.items()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }


//    @Test
//    @DisplayName("커서를 전달하면 해당 커서 이전의 게시글만 반환한다")
//    void getFeed_cursor_returns_only_older_posts() {
//        Profile profile = savedProfile(savedUser(1L), "작성자");
//        Post post1 = savedPost(profile, "첫 번째");
//        Post post2 = savedPost(profile, "두 번째");
//        Post post3 = savedPost(profile, "세 번째");
//
//
//        // 첫 번째 페이지 (size=1) → post3 반환
//        FeedResponse firstPage = feedQueryService.getFeed(1L, new FeedQueryRequest(1, null, null, "LATEST"));
//
//        System.out.println("cursorCreatedAt = " + firstPage.nextCursor().cursorCreatedAt());
//        System.out.println("post3.createdAt = " + post3.getCreatedAt());
//        System.out.println("post3.createdAt = " + post2.getCreatedAt());
//        System.out.println("post3.createdAt = " + post1.getCreatedAt());
//
//        assertThat(firstPage.items().get(0).id()).isEqualTo(post3.getId());
//
//        // 두 번째 페이지 → post2 반환
//        String cursorCreatedAt = firstPage.nextCursor().cursorCreatedAt();
//        Long cursorId = firstPage.nextCursor().cursorId();
//
//        FeedResponse secondPage = feedQueryService.getFeed(1L,
//                new FeedQueryRequest(1, cursorCreatedAt, cursorId, "LATEST"));
//
//        assertThat(secondPage.items()).hasSize(1);
//        assertThat(secondPage.items().get(0).id()).isEqualTo(post2.getId());
//    }

    @Test
    @DisplayName("nextCursor의 cursorId는 items의 마지막 요소 id와 같다")
    void getFeed_next_cursor_equals_last_item() {
        Profile profile = savedProfile(savedUser(1L), "작성자");
        savedPost(profile, "첫 번째");
        savedPost(profile, "두 번째");
        Post post3 = savedPost(profile, "세 번째");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(2, null, null, "LATEST"));

        // ORDER DESC → [post3, post2] 반환, nextCursor는 마지막 요소 post2 기준
        Long lastItemId = response.items().get(response.items().size() - 1).id();
        assertThat(response.nextCursor().cursorId()).isEqualTo(lastItemId);
    }

    @Test
    @DisplayName("본인이 작성한 게시글은 isMine이 true다")
    void getFeed_is_mine_true_for_own_post() {
        UserEntity user = savedUser(1L);
        Profile profile = savedProfile(user, "작성자");
        savedPost(profile, "내 게시글");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).isMine()).isTrue();
    }

    @Test
    @DisplayName("타인이 작성한 게시글은 isMine이 false다")
    void getFeed_is_mine_false_for_others_post() {
        UserEntity user1 = savedUser(1L);
        Profile profile1 = savedProfile(user1, "작성자");
        savedPost(profile1, "남의 게시글");

        UserEntity user2 = savedUser(2L);

        FeedResponse response = feedQueryService.getFeed(2L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).isMine()).isFalse();
    }

    @Test
    @DisplayName("hideLikeCount가 false이면 likeCount가 0으로 반환된다")
    void getFeed_show_like_count_returns_zero() {
        Profile profile = savedProfile(savedUser(1L), "작성자");
        savedPost(profile, "게시글");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).hideLikeCount()).isFalse();
        assertThat(response.items().get(0).likeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("프로필 이미지가 없는 작성자는 기본 이미지 URL이 반환된다")
    void getFeed_no_profile_image_uses_default_url() {
        UserEntity user = savedUser(1L);
        Fridge fridge = fridgeRepository.save(Fridge.crate());
        Profile profile = profileRepository.save(new Profile(user, "이미지없는유저", null, fridge));
        savedPost(profile, "게시글");

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).writer().profileImageUrl())
                .isEqualTo("https://test.s3.com/default.png");
        verify(fileUploadService).getDefaultProfileImageUrl();
    }

    @Test
    @DisplayName("게시글 이미지의 s3Key가 URL로 변환되어 반환된다")
    void getFeed_post_images_converted_to_urls() {
        Profile profile = savedProfile(savedUser(1L), "작성자");
        Post post = new Post(profile, "이미지 게시글");
        post.addImage(new PostImage("public/post/image.jpg", "image/jpeg", 1000L, "public"));
        postRepository.save(post);

        FeedResponse response = feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).images()).hasSize(1);
        assertThat(response.items().get(0).images().get(0)).isEqualTo("https://test.s3.com/image.png");
        verify(fileUploadService).getPublicUrl("public/post/image.jpg");
    }

    // ── 에러 케이스 ───────────────────────────────────────────────

    @Test
    @DisplayName("cursorCreatedAt만 전달하고 cursorId를 생략하면 INVALID_CURSOR 예외가 발생한다")
    void getFeed_only_cursor_created_at_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getFeed(1L, new FeedQueryRequest(10, "2026-01-01T00:00:00", null, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_CURSOR);
                    assertThat(code.status()).isEqualTo(INVALID_CURSOR.status());
                    assertThat(code.message()).isEqualTo(INVALID_CURSOR.message());
                });
    }

    @Test
    @DisplayName("cursorId만 전달하고 cursorCreatedAt을 생략하면 INVALID_CURSOR 예외가 발생한다")
    void getFeed_only_cursor_id_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, 100L, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_CURSOR);
                    assertThat(code.status()).isEqualTo(INVALID_CURSOR.status());
                    assertThat(code.message()).isEqualTo(INVALID_CURSOR.message());
                });
    }

    @Test
    @DisplayName("지원하지 않는 정렬 방식을 전달하면 INVALID_SORT_TYPE 예외가 발생한다")
    void getFeed_invalid_sort_type_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getFeed(1L, new FeedQueryRequest(10, null, null, "POPULAR")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_SORT_TYPE);
                    assertThat(code.status()).isEqualTo(INVALID_SORT_TYPE.status());
                    assertThat(code.message()).isEqualTo(INVALID_SORT_TYPE.message());
                });
    }

    @Test
    @DisplayName("cursorCreatedAt의 날짜 형식이 올바르지 않으면 INVALID_CURSOR 예외가 발생한다")
    void getFeed_invalid_cursor_date_format_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getFeed(1L, new FeedQueryRequest(10, "2026/01/01 00:00:00", 100L, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_CURSOR);
                    assertThat(code.status()).isEqualTo(INVALID_CURSOR.status());
                    assertThat(code.message()).isEqualTo(INVALID_CURSOR.message());
                });
    }

    // ── getMyFeed 성공 케이스 ─────────────────────────────────────

    @Test
    @DisplayName("마이피드 첫 페이지 요청 시 내 게시글만 반환되고 타인 게시글은 제외된다")
    void getMyFeed_first_page_returns_only_my_posts() {
        UserEntity user1 = savedUser(10L);
        Profile profile1 = savedProfile(user1, "나");
        savedPost(profile1, "내 첫 번째 게시글");
        savedPost(profile1, "내 두 번째 게시글");

        UserEntity user2 = savedUser(20L);
        Profile profile2 = savedProfile(user2, "타인");
        savedPost(profile2, "타인 게시글");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items()).hasSize(2);
        assertThat(response.items()).allMatch(item -> !item.content().equals("타인 게시글"));
    }

    @Test
    @DisplayName("마이피드 조회 시 게시글이 없으면 빈 목록과 hasNext=false를 반환한다")
    void getMyFeed_empty_when_no_posts() {
        savedProfile(savedUser(10L), "나");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("마이피드 조회 결과가 size보다 많으면 hasNext가 true이고 nextCursor가 존재한다")
    void getMyFeed_has_next_true_when_more_exists() {
        Profile profile = savedProfile(savedUser(10L), "나");
        savedPost(profile, "첫 번째");
        savedPost(profile, "두 번째");
        savedPost(profile, "세 번째");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(2, null, null, "LATEST"));

        assertThat(response.items()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("마이피드 마지막 페이지이면 hasNext가 false이고 nextCursor가 null이다")
    void getMyFeed_has_next_false_on_last_page() {
        Profile profile = savedProfile(savedUser(10L), "나");
        savedPost(profile, "첫 번째");
        savedPost(profile, "두 번째");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(3, null, null, "LATEST"));

        assertThat(response.items()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("마이피드 nextCursor의 cursorId는 items의 마지막 요소 id와 같다")
    void getMyFeed_next_cursor_equals_last_item() {
        Profile profile = savedProfile(savedUser(10L), "나");
        savedPost(profile, "첫 번째");
        savedPost(profile, "두 번째");
        savedPost(profile, "세 번째");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(2, null, null, "LATEST"));

        Long lastItemId = response.items().get(response.items().size() - 1).id();
        assertThat(response.nextCursor().cursorId()).isEqualTo(lastItemId);
    }

    @Test
    @DisplayName("마이피드 hideLikeCount가 false이면 likeCount가 0으로 반환된다")
    void getMyFeed_show_like_count_returns_zero() {
        Profile profile = savedProfile(savedUser(10L), "나");
        savedPost(profile, "게시글");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).hideLikeCount()).isFalse();
        assertThat(response.items().get(0).likeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("마이피드 게시글 이미지의 s3Key가 URL로 변환되어 반환된다")
    void getMyFeed_post_images_converted_to_urls() {
        Profile profile = savedProfile(savedUser(10L), "나");
        Post post = new Post(profile, "이미지 게시글");
        post.addImage(new PostImage("public/post/my-image.jpg", "image/jpeg", 1000L, "public"));
        postRepository.save(post);

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).images()).hasSize(1);
        assertThat(response.items().get(0).images().get(0)).isEqualTo("https://test.s3.com/image.png");
        verify(fileUploadService).getPublicUrl("public/post/my-image.jpg");
    }

    @Test
    @DisplayName("마이피드 글 생성 시 updatedAt과 createdAt이 동일하다")
    void getMyFeed_updated_at_equals_created_at_on_creation() {
        Profile profile = savedProfile(savedUser(10L), "나");
        savedPost(profile, "게시글");

        MyFeedResponse response = feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, null, "LATEST"));

        assertThat(response.items().get(0).updatedAt()).isEqualTo(response.items().get(0).createdAt());
    }

    // ── getMyFeed 에러 케이스 ─────────────────────────────────────

    @Test
    @DisplayName("마이피드 조회 시 프로필이 없으면 PROFILE_NOT_FOUND 예외가 발생한다")
    void getMyFeed_profile_not_found_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getMyFeed(9999L, new FeedQueryRequest(10, null, null, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(PROFILE_NOT_FOUND);
                    assertThat(code.status()).isEqualTo(PROFILE_NOT_FOUND.status());
                    assertThat(code.message()).isEqualTo(PROFILE_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("마이피드 cursorCreatedAt만 전달하고 cursorId를 생략하면 INVALID_CURSOR 예외가 발생한다")
    void getMyFeed_only_cursor_created_at_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, "2026-01-01T00:00:00", null, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_CURSOR);
                    assertThat(code.status()).isEqualTo(INVALID_CURSOR.status());
                    assertThat(code.message()).isEqualTo(INVALID_CURSOR.message());
                });
    }

    @Test
    @DisplayName("마이피드 cursorId만 전달하고 cursorCreatedAt을 생략하면 INVALID_CURSOR 예외가 발생한다")
    void getMyFeed_only_cursor_id_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, 100L, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_CURSOR);
                    assertThat(code.status()).isEqualTo(INVALID_CURSOR.status());
                    assertThat(code.message()).isEqualTo(INVALID_CURSOR.message());
                });
    }

    @Test
    @DisplayName("마이피드 지원하지 않는 정렬 방식을 전달하면 INVALID_SORT_TYPE 예외가 발생한다")
    void getMyFeed_invalid_sort_type_throws() {
        assertThatThrownBy(() ->
                feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, null, null, "POPULAR")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_SORT_TYPE);
                    assertThat(code.status()).isEqualTo(INVALID_SORT_TYPE.status());
                    assertThat(code.message()).isEqualTo(INVALID_SORT_TYPE.message());
                });
    }

    @Test
    @DisplayName("마이피드 cursorCreatedAt의 날짜 형식이 올바르지 않으면 INVALID_CURSOR 예외가 발생한다")
    void getMyFeed_invalid_cursor_date_format_throws() {
        savedProfile(savedUser(10L), "나");

        assertThatThrownBy(() ->
                feedQueryService.getMyFeed(10L, new FeedQueryRequest(10, "2026/01/01 00:00:00", 100L, "LATEST")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((ApiException) ex).getErrorCode();
                    assertThat(code).isEqualTo(INVALID_CURSOR);
                    assertThat(code.status()).isEqualTo(INVALID_CURSOR.status());
                    assertThat(code.message()).isEqualTo(INVALID_CURSOR.message());
                });
    }
}
