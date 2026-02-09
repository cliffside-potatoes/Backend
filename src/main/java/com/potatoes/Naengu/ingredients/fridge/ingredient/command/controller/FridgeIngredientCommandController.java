package com.potatoes.Naengu.ingredients.fridge.ingredient.command.controller;

import com.potatoes.Naengu.ingredients.fridge.ingredient.command.dto.CreateFridgeIngredientRequest;
import com.potatoes.Naengu.ingredients.fridge.ingredient.command.dto.CreateFridgeIngredientResponse;
import com.potatoes.Naengu.ingredients.fridge.ingredient.command.service.FridgeIngredientCommandService;
import com.potatoes.Naengu.ingredients.shared.api.Api;
import com.potatoes.Naengu.ingredients.shared.auth.AuthFridgeId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FridgeIngredientCommandController {

    private final FridgeIngredientCommandService service;

    public FridgeIngredientCommandController(FridgeIngredientCommandService service) {
        this.service = service;
    }

    @PostMapping("/ingredients")
    public ResponseEntity<Api<CreateFridgeIngredientResponse>> create(
            @AuthFridgeId Long fridgeId,
            @Valid @RequestBody CreateFridgeIngredientRequest request
    ) {
        Long id = service.create(fridgeId, request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Api.success(new CreateFridgeIngredientResponse(id)));
    }
}
