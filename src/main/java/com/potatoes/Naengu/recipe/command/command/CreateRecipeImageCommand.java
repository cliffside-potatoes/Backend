package com.potatoes.Naengu.recipe.command.command;


public record CreateRecipeImageCommand(
        String s3Key,
        String contentType,
        Long size,
        String accessType
) {

}
