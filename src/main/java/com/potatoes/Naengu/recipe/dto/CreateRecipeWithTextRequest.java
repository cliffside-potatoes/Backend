package com.potatoes.Naengu.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;


public record CreateRecipeWithTextRequest(
        @NotNull
        @Size(min = 1)
        List<@Valid CreateRecipeStepRequest> steps

) {}
