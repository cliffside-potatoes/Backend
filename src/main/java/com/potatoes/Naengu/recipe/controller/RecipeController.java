package com.potatoes.Naengu.recipe.controller;

import com.potatoes.Naengu.auth.annotation.AuthFridge;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.recipe.dto.CreateRecipeCommand;
import com.potatoes.Naengu.recipe.dto.CreateRecipeRequest;
import com.potatoes.Naengu.recipe.dto.CreateRecipeResponse;
import com.potatoes.Naengu.recipe.mapper.CreateRecipeRequestMapper;
import com.potatoes.Naengu.recipe.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeController {

    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @PostMapping("/recipes")
    public ResponseEntity<Api<CreateRecipeResponse>> create(
            @AuthFridge Fridge fridge,
            @Valid @RequestBody CreateRecipeRequest request
    ) {
        CreateRecipeCommand command = CreateRecipeRequestMapper.toCommand(request);
        Long id = service.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateRecipeResponse(id)));
    }
}
