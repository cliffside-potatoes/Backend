package com.potatoes.Naengu.ingredients.fridge.category.command.service;

import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_DUPLICATE;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_FORBIDDEN;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_NOT_FOUND;
import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.CATEGORY_UPDATE_EMPTY;
import static org.assertj.core.api.Assertions.*;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.CreateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.command.command.UpdateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@DataJpaTest
@Import(CategoryCommandService.class)
class CategoryCommandServiceDataJpaTest {

    @Autowired
    private CategoryCommandService service;

    @Autowired
    private FridgeCategoryRepository repository;

    @Test
    @DisplayName("카테고리를 생성하면 저장되고 id를 반환한다 (orderIndex는 1부터 시작)")
    void create_success() {
        Long fridgeId = 1L;
        CreateCategoryCommand command = new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED
        );

        Long savedId = service.create(fridgeId, command);

        FridgeCategory saved = repository.findById(savedId).orElseThrow();
        assertThat(saved.getId()).isEqualTo(savedId);
        assertThat(saved.getFridgeId()).isEqualTo(fridgeId);
        assertThat(saved.getStorageType()).isEqualTo(StorageType.REFRIGERATED);
        assertThat(saved.getName()).isEqualTo("고기");
        assertThat(saved.getOrderIndex()).isEqualTo(1);
        assertThat(saved.getColor()).isEqualTo(CategoryColor.RED);
    }

    @Test
    @DisplayName("같은 fridgeId + storageType + name이 이미 있으면 CATEGORY_DUPLICATE 예외가 발생한다")
    void create_duplicate_throws() {
        Long fridgeId = 1L;
        CreateCategoryCommand command = new CreateCategoryCommand(
                StorageType.FROZEN,
                "만두",
                CategoryColor.BLUE
        );

        service.create(fridgeId, command);

        assertThatThrownBy(() -> service.create(fridgeId, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_DUPLICATE.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_DUPLICATE.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_DUPLICATE.message());
                });
    }

    @Test
    @DisplayName("fridgeId가 다르면 같은 storageType + name 이어도 중복이 아니다")
    void create_not_duplicate_when_fridgeId_differs() {
        CreateCategoryCommand command = new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED
        );

        Long id1 = service.create(1L, command);
        Long id2 = service.create(2L, command);

        assertThat(id1).isNotEqualTo(id2);
        assertThat(repository.findById(id1).orElseThrow().getFridgeId()).isEqualTo(1L);
        assertThat(repository.findById(id2).orElseThrow().getFridgeId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("orderIndex는 같은 fridgeId + storageType 안에서만 증가한다")
    void create_orderIndex_increase_by_storageType() {
        Long fridgeId = 1L;

        Long r1 = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "채소", CategoryColor.GREEN
        ));
        Long r2 = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "유제품", CategoryColor.RED
        ));
        Long f1 = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.FROZEN, "아이스크림", CategoryColor.BLUE
        ));

        assertThat(repository.findById(r1).orElseThrow().getOrderIndex()).isEqualTo(1);
        assertThat(repository.findById(r2).orElseThrow().getOrderIndex()).isEqualTo(2);
        assertThat(repository.findById(f1).orElseThrow().getOrderIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("카테고리를 수정하면 변경사항이 반영되고 id를 반환한다")
    void update_success() {
        Long fridgeId = 1L;
        Long categoryId = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                StorageType.FROZEN,
                "육류",
                CategoryColor.BLUE
        );

        Long updatedId = service.update(fridgeId, command);

        FridgeCategory updated = repository.findById(updatedId).orElseThrow();
        assertThat(updated.getId()).isEqualTo(categoryId);
        assertThat(updated.getFridgeId()).isEqualTo(fridgeId);
        assertThat(updated.getStorageType()).isEqualTo(StorageType.FROZEN);
        assertThat(updated.getName()).isEqualTo("육류");
        assertThat(updated.getColor()).isEqualTo(CategoryColor.BLUE);
    }

    @Test
    @DisplayName("수정 요청에 변경사항이 하나도 없으면 CATEGORY_UPDATE_EMPTY 예외가 발생한다")
    void update_empty_throws() {
        Long fridgeId = 1L;
        Long categoryId = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.update(fridgeId, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_UPDATE_EMPTY.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_UPDATE_EMPTY.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_UPDATE_EMPTY.message());
                });
    }

    @Test
    @DisplayName("존재하지 않는 categoryId를 수정하면 CATEGORY_NOT_FOUND 예외가 발생한다")
    void update_not_found_throws() {
        Long fridgeId = 1L;

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                9999L,
                StorageType.FROZEN,
                "육류",
                CategoryColor.BLUE
        );

        assertThatThrownBy(() -> service.update(fridgeId, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_NOT_FOUND.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_NOT_FOUND.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("fridgeId가 다르면 수정할 수 없고 CATEGORY_FORBIDDEN 예외가 발생한다")
    void update_forbidden_throws() {
        Long ownerFridgeId = 1L;
        Long otherFridgeId = 2L;

        Long categoryId = service.create(ownerFridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                StorageType.FROZEN,
                "육류",
                CategoryColor.BLUE
        );

        assertThatThrownBy(() -> service.update(otherFridgeId, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_FORBIDDEN.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_FORBIDDEN.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_FORBIDDEN.message());
                });
    }

    @Test
    @DisplayName("수정 시 다른 카테고리와 (fridgeId + storageType + name)이 겹치면 CATEGORY_DUPLICATE 예외가 발생한다")
    void update_duplicate_throws() {
        Long fridgeId = 1L;

        Long id1 = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        Long id2 = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "채소", CategoryColor.GREEN
        ));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                id2,
                StorageType.REFRIGERATED,
                "고기",
                CategoryColor.RED
        );

        assertThatThrownBy(() -> service.update(fridgeId, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_DUPLICATE.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_DUPLICATE.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_DUPLICATE.message());
                });
    }

    @Test
    @DisplayName("name을 공백으로 수정하려 하면 VALIDATION_ERROR 예외가 발생한다")
    void update_blank_name_throws_validation_error() {
        Long fridgeId = 1L;

        Long categoryId = service.create(fridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                categoryId,
                null,
                "   ",
                null
        );

        assertThatThrownBy(() -> service.update(fridgeId, command))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo("VALIDATION_ERROR");
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getMessage()).isEqualTo("name은 공백일 수 없습니다.");
                });
    }

    @Test
    @DisplayName("존재하지 않는 categoryId를 삭제하면 CATEGORY_NOT_FOUND 예외가 발생한다")
    void delete_not_found_throws() {
        Long fridgeId = 1L;

        assertThatThrownBy(() -> service.delete(fridgeId, 9999L))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_NOT_FOUND.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_NOT_FOUND.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_NOT_FOUND.message());
                });
    }

    @Test
    @DisplayName("fridgeId가 다르면 삭제할 수 없고 CATEGORY_FORBIDDEN 예외가 발생한다")
    void delete_forbidden_throws() {
        Long ownerFridgeId = 1L;
        Long otherFridgeId = 2L;

        Long categoryId = service.create(ownerFridgeId, new CreateCategoryCommand(
                StorageType.REFRIGERATED, "고기", CategoryColor.RED
        ));

        assertThatThrownBy(() -> service.delete(otherFridgeId, categoryId))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException e = (ApiException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(CATEGORY_FORBIDDEN.code());
                    assertThat(e.getStatus()).isEqualTo(CATEGORY_FORBIDDEN.status());
                    assertThat(e.getMessage()).isEqualTo(CATEGORY_FORBIDDEN.message());
                });
    }
}