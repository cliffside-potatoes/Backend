package com.potatoes.Naengu.fridge.dto.query;

import java.util.List;

public record StorageSectionResponse(
        String storageType,
        List<FridgeCategoryResponse> categories
) {}
