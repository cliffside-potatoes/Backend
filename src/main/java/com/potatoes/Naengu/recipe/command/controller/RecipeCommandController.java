package com.potatoes.Naengu.recipe.command.controller;

import com.potatoes.Naengu.ingredients.shared.api.Api;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeCommand;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeRequest;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeResponse;
import com.potatoes.Naengu.recipe.command.mapper.CreateRecipeRequestMapper;
import com.potatoes.Naengu.recipe.command.service.RecipeCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeCommandController {

    private final RecipeCommandService service;

    public RecipeCommandController(RecipeCommandService service) {
        this.service = service;
    }

    @PostMapping("/recipes")
    public ResponseEntity<Api<CreateRecipeResponse>> create(
            @Valid @RequestBody CreateRecipeRequest request
    ) {
        CreateRecipeCommand command = CreateRecipeRequestMapper.toCommand(request);
        Long id = service.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateRecipeResponse(id)));
    }
}
