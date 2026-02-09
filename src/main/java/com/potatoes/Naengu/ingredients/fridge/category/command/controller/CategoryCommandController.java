package com.potatoes.Naengu.ingredients.fridge.category.command.controller;

import com.potatoes.Naengu.ingredients.fridge.category.command.dto.CreateCategoryRequest;
import com.potatoes.Naengu.ingredients.fridge.category.command.dto.CreateCategoryResponse;
import com.potatoes.Naengu.ingredients.fridge.category.command.dto.UpdateCategoryRequest;
import com.potatoes.Naengu.ingredients.fridge.category.command.dto.UpdateCategoryResponse;
import com.potatoes.Naengu.ingredients.fridge.category.command.service.CategoryCommandService;
import com.potatoes.Naengu.ingredients.shared.api.Api;
import com.potatoes.Naengu.ingredients.shared.auth.AuthFridgeId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryCommandController {

    private final CategoryCommandService service;

    public CategoryCommandController(CategoryCommandService service) {
        this.service = service;
    }

    @PostMapping("/ingredients/categories")
    public ResponseEntity<Api<CreateCategoryResponse>> create(
            @AuthFridgeId Long fridgeId,
            @Valid @RequestBody CreateCategoryRequest request)
    {
        Long id = service.create(fridgeId, request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateCategoryResponse(id)));
    }

    @PatchMapping("/ingredients/categories/{fridgeCategoryId}")
    public ResponseEntity<Api<UpdateCategoryResponse>> update(
            @AuthFridgeId Long fridgeId,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Long updatedId = service.update(fridgeId, request.toCommand(categoryId));
        return ResponseEntity.ok(
                Api.success(new UpdateCategoryResponse(updatedId))
        );
    }

    @DeleteMapping("/ingredients/categories/{fridgeCategoryId}")
    public ResponseEntity<Api<Void>> delete(
            @AuthFridgeId Long fridgeId,
            @PathVariable Long fridgeCategoryId
    ) {
        service.delete(fridgeId, fridgeCategoryId);
        return ResponseEntity.ok(Api.success());
    }

}
