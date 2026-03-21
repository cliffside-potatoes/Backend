package com.potatoes.Naengu.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CursorResponse(
        @Schema(example = "2026-01-19T12:30:00")
        String cursorCreatedAt,

        @Schema(example = "98")
        Long cursorId
) {}
