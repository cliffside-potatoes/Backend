package com.potatoes.Naengu.fridge.dto.query;

import java.util.List;

public record FridgeOverviewResponse(
        List<StorageSectionResponse> items

) {}
