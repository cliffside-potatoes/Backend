package com.potatoes.Naengu.ingredients.fridge.category.command.service;

import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_DUPLICATE;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_FORBIDDEN;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_NAME_BLANK;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_NOT_FOUND;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_UPDATE_EMPTY;
import static org.assertj.core.api.Assertions.*;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.CreateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.command.command.UpdateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.Fridge;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.ingredients.fridge.repository.FridgeRepository;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import com.potatoes.Naengu.ingredients.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@EntityScan("com.potatoes.Naengu")
@EnableJpaRepositories("com.potatoes.Naengu")
@Import(CategoryCommandService.class)
class CategoryCommandServiceDataJpaTest {

    @Autowired
    private CategoryCommandService service;

    @Autowired
    private FridgeCategoryRepository repository;

    @Autowired
    private FridgeRepository fridgeRepository;

    private Fridge savedFridge() {
        return fridgeRepository.save(Fridge.crate());
    }

    @Test
    @DisplayName("카테고리를 생성하면 저장되고 id를 반환한다 (orderIndex는 1부터 시작)")
    void create_success() {
        Fridge fridge = savedFridge();

        CreateCategoryCommand command = new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED
        );

        Long savedId = service.create(fridge, command);

        FridgeCategory saved = repository.findById(savedId).orElseThrow();
        assertThat(saved.getId()).isEqualTo(savedId);
        assertThat(saved.getFridge()).isEqualTo(fridge);
        assertThat(saved.getStorageType()).isEqualTo(StorageType.REFRIGERATED);
        assertThat(saved.getName()).isEqualTo("고기");
        assertThat(saved.getOrderIndex()).isEqualTo(1);
        assertThat(saved.getColor()).isEqualTo(CategoryColor.RED);
    }

    @Test
    @DisplayName("같은 fridge + storageType + name이 이미 있으면 CATEGORY_DUPLICATE 예외가 발생한다")
    void create_duplicate_throws() {
        Fridge fridge = savedFridge();

        CreateCategoryCommand command = new CreateCategoryCommand(
                StorageType.FROZEN,
                "만두",
                CategoryColor.BLUE
        );

        service.create(fridge, command);

        assertThatThrownBy(() -> service.create(fridge, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_DUPLICATE);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_DUPLICATE.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_DUPLICATE.message());
                });
    }

    @Test
    @DisplayName("fridge가 다르면 같은 storageType + name 이어도 중복이 아니다")
    void create_not_duplicate_when_fridgeId_differs() {
        Fridge fridge1 = savedFridge();
        Fridge fridge2 = savedFridge();

        CreateCategoryCommand command = new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED
        );

        Long id1 = service.create(fridge1, command);
        Long id2 = service.create(fridge2, command);

        assertThat(id1).isNotEqualTo(id2);
        assertThat(repository.findById(id1).orElseThrow().getFridge().getId()).isEqualTo(fridge1.getId());
        assertThat(repository.findById(id2).orElseThrow().getFridge().getId()).isEqualTo(fridge2.getId());
    }

    @Test
    @DisplayName("orderIndex는 같은 fridge + storageType 안에서만 증가한다")
    void create_orderIndex_increase_by_storageType() {
        Fridge fridge = savedFridge();

        Long r1 = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "채소", CategoryColor.GREEN
        ));
        Long r2 = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "유제품", CategoryColor.RED
        ));
        Long f1 = service.create(fridge, new CreateCategoryCommand(
                StorageType.FROZEN, "아이스크림", CategoryColor.BLUE
        ));

        assertThat(repository.findById(r1).orElseThrow().getOrderIndex()).isEqualTo(1);
        assertThat(repository.findById(r2).orElseThrow().getOrderIndex()).isEqualTo(2);
        assertThat(repository.findById(f1).orElseThrow().getOrderIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("카테고리를 수정하면 변경사항이 반영되고 id를 반환한다")
    void update_success() {
        Fridge fridge = savedFridge();
        Long categoryId = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                StorageType.FROZEN,
                "육류",
                CategoryColor.BLUE
        );

        Long updatedId = service.update(fridge, command);

        FridgeCategory updated = repository.findById(updatedId).orElseThrow();
        assertThat(updated.getId()).isEqualTo(categoryId);
        assertThat(updated.getFridge().getId()).isEqualTo(fridge.getId());
        assertThat(updated.getStorageType()).isEqualTo(StorageType.FROZEN);
        assertThat(updated.getName()).isEqualTo("육류");
        assertThat(updated.getColor()).isEqualTo(CategoryColor.BLUE);
    }

    @Test
    @DisplayName("수정 요청에 변경사항이 하나도 없으면 CATEGORY_UPDATE_EMPTY 예외가 발생한다")
    void update_empty_throws() {
        Fridge fridge = savedFridge();

        Long categoryId = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.update(fridge, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_UPDATE_EMPTY);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_UPDATE_EMPTY.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_UPDATE_EMPTY.message());
                });
    }

    @Test
    @DisplayName("존재하지 않는 categoryId를 수정하면 CATEGORY_NOT_FOUND 예외가 발생한다")
    void update_not_found_throws() {
        Fridge fridge = savedFridge();

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                9999L,
                StorageType.FROZEN,
                "육류",
                CategoryColor.BLUE
        );

        assertThatThrownBy(() -> service.update(fridge, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_NOT_FOUND);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_NOT_FOUND.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("fridge가 다르면 수정할 수 없고 CATEGORY_FORBIDDEN 예외가 발생한다")
    void update_forbidden_throws() {
        Fridge owner = savedFridge();
        Fridge other = savedFridge();

        Long categoryId = service.create(owner, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                StorageType.FROZEN,
                "육류",
                CategoryColor.BLUE
        );

        assertThatThrownBy(() -> service.update(other, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_FORBIDDEN);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_FORBIDDEN.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_FORBIDDEN.message());
                });
    }

    @Test
    @DisplayName("수정 시 다른 카테고리와 (fridge + storageType + name)이 겹치면 CATEGORY_DUPLICATE 예외가 발생한다")
    void update_duplicate_throws() {
        Fridge fridge = savedFridge();

        Long id1 = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        Long id2 = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "채소", CategoryColor.GREEN
        ));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                id2,
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED
        );

        assertThatThrownBy(() -> service.update(fridge, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_DUPLICATE);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_DUPLICATE.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_DUPLICATE.message());
                });
    }

    @Test
    @DisplayName("name을 공백으로 수정하려 하면 VALIDATION_ERROR 예외가 발생한다")
    void update_blank_name_throws_validation_error() {
        Fridge fridge = savedFridge();

        Long categoryId = service.create(fridge, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                null,
                "   ",
                null
        );

        assertThatThrownBy(() -> service.update(fridge, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_NAME_BLANK);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_NAME_BLANK.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_NAME_BLANK.message());
                });
    }

    @Test
    @DisplayName("존재하지 않는 categoryId를 삭제하면 CATEGORY_NOT_FOUND 예외가 발생한다")
    void delete_not_found_throws() {
        Fridge fridge = savedFridge();

        assertThatThrownBy(() -> service.delete(fridge, 9999L))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_NOT_FOUND);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_NOT_FOUND.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("fridge가 다르면 삭제할 수 없고 CATEGORY_FORBIDDEN 예외가 발생한다")
    void delete_forbidden_throws() {
        Fridge owner = savedFridge();
        Fridge other = savedFridge();

        Long categoryId = service.create(owner, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        assertThatThrownBy(() -> service.delete(other, categoryId))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    ErrorCode errorCode = e.getErrorCode();
                    assertThat(errorCode).isEqualTo(CATEGORY_FORBIDDEN);
                    assertThat(errorCode.status()).isEqualTo(CATEGORY_FORBIDDEN.status());
                    assertThat(errorCode.message()).isEqualTo(CATEGORY_FORBIDDEN.message());
                });
    }
}