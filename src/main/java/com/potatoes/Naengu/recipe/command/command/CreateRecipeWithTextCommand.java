package com.potatoes.Naengu.recipe.command.command;

import java.util.List;

public record CreateRecipeWithTextCommand(
        List<CreateRecipeStepCommand> steps
) {
    public CreateRecipeWithTextCommand{
        steps = safeCopy(steps);
    }

    private static <T> List<T> safeCopy(List<T> values) {
        return List.copyOf(values);
    }
}
