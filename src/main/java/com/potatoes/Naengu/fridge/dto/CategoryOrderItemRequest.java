package com.potatoes.Naengu.fridge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CategoryOrderItemRequest(

        @NotNull
        Long categoryId,

        @NotNull
        @Min(1)
        Integer position
) {
}
