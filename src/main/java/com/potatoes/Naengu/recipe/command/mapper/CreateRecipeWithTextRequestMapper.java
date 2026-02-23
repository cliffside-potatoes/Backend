package com.potatoes.Naengu.recipe.command.mapper;

import com.potatoes.Naengu.recipe.command.command.CreateRecipeStepCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeWithTextCommand;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeStepRequest;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeWithTextRequest;
import java.util.List;

public final class CreateRecipeWithTextRequestMapper {

    private CreateRecipeWithTextRequestMapper(){}

    public static CreateRecipeWithTextCommand toCommand(CreateRecipeWithTextRequest request) {
        if (request == null) {
            return null;
        }

        List<CreateRecipeStepCommand> steps = safeList(request.steps()).stream()
                .map(CreateRecipeWithTextRequestMapper::toStepCommand)
                .toList();

        return new CreateRecipeWithTextCommand(steps);
    }

    private static CreateRecipeStepCommand toStepCommand(CreateRecipeStepRequest step) {
        return new CreateRecipeStepCommand(step.stepOrder(), step.content());
    }

    private static <T> List<T> safeList(List<T> values) {
        if (values == null) {
            return List.of();
        }
        return values;
    }
}
