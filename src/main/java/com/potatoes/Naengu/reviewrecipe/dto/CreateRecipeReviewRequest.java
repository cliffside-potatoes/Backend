package com.potatoes.Naengu.reviewrecipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRecipeReviewRequest(
        @Valid
        @Size(max = 5)
        List<RecipeReviewImageRequest> images,

        @NotBlank
        @Size(max = 50)
        String content

) {}
