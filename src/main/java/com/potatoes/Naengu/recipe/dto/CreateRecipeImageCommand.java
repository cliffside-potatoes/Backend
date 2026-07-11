package com.potatoes.Naengu.recipe.dto;


public record CreateRecipeImageCommand(
        String s3Key,
        String contentType,
        Long size,
        String accessType
) {

}
