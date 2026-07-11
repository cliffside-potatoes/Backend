package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateCategoryOrderRequest(

        @NotNull
        StorageType storageType,

        @NotEmpty
        List<@Valid CategoryOrderItemRequest> orders
) {
}
