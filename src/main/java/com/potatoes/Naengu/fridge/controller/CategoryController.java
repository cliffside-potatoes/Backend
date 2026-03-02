package com.potatoes.Naengu.fridge.controller;

import com.potatoes.Naengu.fridge.dto.CreateCategoryRequest;
import com.potatoes.Naengu.fridge.dto.CreateCategoryResponse;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryRequest;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryResponse;
import com.potatoes.Naengu.fridge.service.CategoryService;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.auth.annotation.AuthFridge;
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
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping("/ingredients/categories")
    public ResponseEntity<Api<CreateCategoryResponse>> create(
            @AuthFridge Fridge fridge,
            @Valid @RequestBody CreateCategoryRequest request)
    {
        Long id = service.create(fridge, request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateCategoryResponse(id)));
    }

    @PatchMapping("/ingredients/categories/{fridgeCategoryId}")
    public ResponseEntity<Api<UpdateCategoryResponse>> update(
            @AuthFridge Fridge fridge,
            @PathVariable Long fridgeCategoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Long updatedId = service.update(fridge, request.toCommand(fridgeCategoryId));
        return ResponseEntity.ok(
                Api.success(new UpdateCategoryResponse(updatedId))
        );
    }

    @DeleteMapping("/ingredients/categories/{fridgeCategoryId}")
    public ResponseEntity<Api<Void>> delete(
            @AuthFridge Fridge fridge,
            @PathVariable Long fridgeCategoryId
    ) {
        service.delete(fridge, fridgeCategoryId);
        return ResponseEntity.ok(Api.success());
    }

}
